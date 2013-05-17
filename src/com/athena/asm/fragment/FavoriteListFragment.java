package com.athena.asm.fragment;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.athena.asm.ActivityFragmentTargets;
import com.athena.asm.OnOpenActivityFragmentListener;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.FavoriteListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.listener.OnKeyDownListener;
import com.athena.asm.util.ListViewUtil;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.EditFavoriteTask;
import com.athena.asm.util.task.LoadFavoriteTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;
import com.mobeta.android.dslv.DragSortListView;

public class FavoriteListFragment extends SherlockFragment implements BaseViewModel.OnViewModelChangObserver,
        OnKeyDownListener {

    private HomeViewModel m_viewModel;

    private LayoutInflater m_inflater;

    FavoriteListAdapter m_favoriteListAdapter;
    private DragSortListView m_listView;
    private DragSortListView.DropListener onDrop = new DragSortListView.DropListener() {
        @Override
        public void drop(int from, int to) {
            m_favoriteListAdapter.moveItem(from, to);
        }
    };

    private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
        @Override
        public void remove(int which) {
            // do nothing here, code in setOnItemLongClickListener
        }
    };

    private boolean m_isLoaded;

    private OnOpenActivityFragmentListener m_onOpenActivityFragmentListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_inflater = inflater;
        View layout = m_inflater.inflate(R.layout.favorite, null);
        m_listView = (DragSortListView) layout.findViewById(R.id.favorite_list);
        m_listView.setDropListener(onDrop);
        m_listView.setRemoveListener(onRemove);

        aSMApplication application = (aSMApplication) getActivity().getApplication();
        m_viewModel = application.getHomeViewModel();
        m_viewModel.registerViewModelChangeObserver(this);

        m_isLoaded = false;

        return m_listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity parentActivity = getSherlockActivity();
        if (parentActivity instanceof OnOpenActivityFragmentListener) {
            m_onOpenActivityFragmentListener = (OnOpenActivityFragmentListener) parentActivity;
        }

        if (m_viewModel.getCurrentTab() != null && m_viewModel.getCurrentTab().equals(StringUtility.TAB_FAVORITE)) {
            reloadFavorite();
        }
    }

    @Override
    public void onDestroy() {
        m_viewModel.unregisterViewModelChangeObserver(this);
        super.onDestroy();
    }

    public void reloadFavorite() {
        if (m_viewModel.getFavList() == null) {
            if (m_viewModel.m_isLoadingInProgress)
                return;
            LoadFavoriteTask loadFavoriteTask = new LoadFavoriteTask(getActivity(), m_viewModel);
            loadFavoriteTask.execute();
        } else {
            m_isLoaded = true;

            List<Board> favList = m_viewModel.getFavList();
            m_favoriteListAdapter = new FavoriteListAdapter(m_inflater, favList);
            m_listView.setAdapter(m_favoriteListAdapter);

            // click to open board
            m_listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
                    Bundle bundle = new Bundle();
                    Board board = m_favoriteListAdapter.getFavoriteBoards().get(pos);
                    bundle.putSerializable(StringUtility.BOARD, board);
                    aSMApplication.getCurrentApplication().addRecentBoard(board);

                    if (m_onOpenActivityFragmentListener != null) {
                        m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.SUBJECT_LIST,
                                bundle);
                    }
                    return;
                }
            });

            // long click to remove it from favorite
            m_listView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                    // get selected board
                    final Board board = m_favoriteListAdapter.getFavoriteBoards().get(pos);

                    // confirm dialog
                    Builder builder = new AlertDialog.Builder(getActivity());
                    String title = String.format("将版面\"%s\"从收藏夹中删除么？", board.getChsName());
                    builder.setTitle("收藏夹操作").setMessage(title);

                    builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditFavoriteTask task = new EditFavoriteTask(getActivity(), m_viewModel,
                                    board.getEngName(), board.getBoardID(), EditFavoriteTask.FAVORITE_DELETE);
                            task.execute();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog noticeDialog = builder.create();
                    noticeDialog.show();

                    return true;
                }
            });
        }
    }

    @Override
    public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object... params) {
        if (changedPropertyName.equals(HomeViewModel.FAVLIST_PROPERTY_NAME)) {
            reloadFavorite();
        } else if (changedPropertyName.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
            if (!m_isLoaded && m_viewModel.getCurrentTab() != null
                    && m_viewModel.getCurrentTab().equals(StringUtility.TAB_FAVORITE)) {
                reloadFavorite();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode) {
        return ListViewUtil.ScrollListViewByKey(m_listView, keyCode);
    }
}
