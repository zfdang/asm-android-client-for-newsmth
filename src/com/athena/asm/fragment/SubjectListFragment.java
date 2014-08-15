package com.athena.asm.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.ActivityFragmentTargets;
import com.athena.asm.OnOpenActivityFragmentListener;
import com.athena.asm.ProgressDialogProvider;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.BoardTypeListAdapter;
import com.athena.asm.Adapter.SubjectListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.EditFavoriteTask;
import com.athena.asm.util.task.LoadSubjectTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;
import com.athena.asm.viewmodel.SubjectListViewModel;

public class SubjectListFragment extends SherlockFragment implements OnClickListener,
        android.content.DialogInterface.OnClickListener, BaseViewModel.OnViewModelChangObserver {

    private LayoutInflater m_inflater;

    private SubjectListViewModel m_viewModel;

    private EditText m_pageNoEditText;
    private ListView m_listView;
    private SwipeRefreshLayout m_swipeView;

    private boolean m_isNewInstance = false;

    private ProgressDialogProvider m_progressDialogProvider;
    private OnOpenActivityFragmentListener m_onOpenActivityFragmentListener;

    public static int BOARD_TYPE_SUBJECT = 0;
    public static int BOARD_TYPE_NORMAL = 1;
    public static int BOARD_TYPE_DIGEST = 2;
    public static int BOARD_TYPE_MARK = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        m_isNewInstance = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        m_inflater = inflater;
        View subjectListView = inflater.inflate(R.layout.subject_list, null);

        m_listView = (ListView) subjectListView.findViewById(R.id.subject_list);

        m_swipeView = (SwipeRefreshLayout) subjectListView.findViewById(R.id.swipe_container);
        m_swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                // m_swipeView.setRefreshing(true);
                refreshSubjectList();
            }
        });

        aSMApplication application = (aSMApplication) getActivity().getApplication();
        m_viewModel = application.getSubjectListViewModel();
        m_viewModel.registerViewModelChangeObserver(this);

        m_pageNoEditText = (EditText) subjectListView.findViewById(R.id.edittext_page_no);
        m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");

        Button firstButton = (Button) subjectListView.findViewById(R.id.btn_first_page);
        firstButton.setOnClickListener(this);
        Button lastButton = (Button) subjectListView.findViewById(R.id.btn_last_page);
        lastButton.setVisibility(View.GONE);
        // lastButton.setOnClickListener(this);
        Button preButton = (Button) subjectListView.findViewById(R.id.btn_pre_page);
        preButton.setOnClickListener(this);
        Button goButton = (Button) subjectListView.findViewById(R.id.btn_go_page);
        goButton.setOnClickListener(this);
        goButton.setText(R.string.go_page);
        Button nextButton = (Button) subjectListView.findViewById(R.id.btn_next_page);
        nextButton.setOnClickListener(this);

        // ImageButton writeImageButton = (ImageButton)
        // subjectListView.findViewById(R.id.writePost);
        // writeImageButton.setOnClickListener(this);
        // ImageButton switchModeImageButton = (ImageButton)
        // subjectListView.findViewById(R.id.switchBoardMode);
        // switchModeImageButton.setOnClickListener(this);

        getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return subjectListView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity parentActivity = getSherlockActivity();
        if (parentActivity instanceof ProgressDialogProvider) {
            m_progressDialogProvider = (ProgressDialogProvider) parentActivity;
        }
        if (parentActivity instanceof OnOpenActivityFragmentListener) {
            m_onOpenActivityFragmentListener = (OnOpenActivityFragmentListener) parentActivity;
        }

        boolean isNewBoard = false;
        if (m_isNewInstance) {
            String defaultBoardType = ((aSMApplication) getActivity().getApplication()).getDefaultBoardType();
            Board currentBoard = (Board) getActivity().getIntent().getSerializableExtra(StringUtility.BOARD);
            isNewBoard = m_viewModel.updateCurrentBoard(currentBoard, defaultBoardType);
            m_viewModel.setIsFirstIn(isNewBoard);
        }
        m_isNewInstance = false;

        if (isNewBoard) {
            refreshSubjectList();
        } else {
            reloadSubjectList();
        }
    }

    @Override
    public void onDestroy() {
        m_viewModel.unregisterViewModelChangeObserver(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_first_page) {
            m_viewModel.gotoFirstPage();
        } else if (view.getId() == R.id.btn_last_page) {
            m_viewModel.gotoLastPage();
        } else if (view.getId() == R.id.btn_pre_page) {
            m_viewModel.gotoPrevPage();
        } else if (view.getId() == R.id.btn_go_page) {
            int pageSet = Integer.parseInt(m_pageNoEditText.getText().toString());
            m_viewModel.setCurrentPageNumber(pageSet);
        } else if (view.getId() == R.id.btn_next_page) {
            m_viewModel.gotoNextPage();
        }

        m_viewModel.updateBoardCurrentPage();
        m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
        refreshSubjectList();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
        case Activity.RESULT_OK:
            Bundle b = data.getExtras();
            boolean isToRefreshBoard = b.getBoolean(StringUtility.REFRESH_BOARD);
            if (isToRefreshBoard) {
                refreshSubjectList();
            }
            break;

        default:
            break;
        }
    }

    public void reloadSubjectList() {
        if (m_viewModel.getSubjectList() != null) {
            if (m_viewModel.isFirstIn()) {
                m_viewModel.gotoFirstPage();
                m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
                m_viewModel.setIsFirstIn(false);
            }

            m_listView.setAdapter(new SubjectListAdapter(m_inflater, m_viewModel.getSubjectList()));

            m_listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    if (m_onOpenActivityFragmentListener != null) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(StringUtility.SUBJECT, (Subject) view.getTag(R.id.tag_second));
                        bundle.putInt(StringUtility.BOARD_TYPE, m_viewModel.getBoardType());
                        m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.POST_LIST,
                                bundle);
                    }
                }
            });

            getActivity().setTitle(m_viewModel.getTitleText());

            m_listView.requestFocus();
        }
    }

    private void refreshSubjectList() {
        LoadSubjectTask loadSubjectTask = new LoadSubjectTask(m_viewModel);
        loadSubjectTask.execute();
        if (m_progressDialogProvider != null) {
            m_progressDialogProvider.showProgressDialog();
        }
    }

    public static final int SWITCH_BOARD_TYPE = Menu.FIRST;
    public static final int REFRESH_SUBJECTLIST = Menu.FIRST + 1;
    public static final int SEARCH_POST = Menu.FIRST + 2;
    public static final int NEW_POST = Menu.FIRST + 3;
    public static final int QUICK_SWITCH_BOARD_TYPE = Menu.FIRST + 4;
    public static final int SWITCH_STICKY = Menu.FIRST + 5;
    public static final int ADD_TO_FAVORITE = Menu.FIRST + 6;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // super.onCreateOptionsMenu(menu, inflater);
        boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;

        if (SmthSupport.getInstance().getLoginStatus()) {
            menu.add(0, NEW_POST, Menu.NONE, "发新贴").setIcon(isLight ? R.drawable.write_inverse : R.drawable.write)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(0, QUICK_SWITCH_BOARD_TYPE, Menu.NONE, "模式切换")
                .setIcon(isLight ? R.drawable.switcher_inverse : R.drawable.switcher)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, SWITCH_STICKY, Menu.NONE, "切换置底").setIcon(isLight ? R.drawable.sticky_inverse : R.drawable.sticky)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, SEARCH_POST, Menu.NONE, "搜索").setIcon(isLight ? R.drawable.search_inverse : R.drawable.search)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, REFRESH_SUBJECTLIST, Menu.NONE, "刷新")
                .setIcon(isLight ? R.drawable.refresh_inverse : R.drawable.refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, SWITCH_BOARD_TYPE, Menu.NONE, "切换到...").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, ADD_TO_FAVORITE, Menu.NONE, "收藏版面").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case android.R.id.home:
            getActivity().onBackPressed();
            break;
        case QUICK_SWITCH_BOARD_TYPE:
            m_viewModel.toggleBoardType();
            if(m_viewModel.getBoardType() == BOARD_TYPE_SUBJECT){
                Toast.makeText(getActivity(), "已切换到同主题模式", Toast.LENGTH_SHORT).show();
            } else if (m_viewModel.getBoardType() == BOARD_TYPE_NORMAL){
                Toast.makeText(getActivity(), "已切换到普通模式", Toast.LENGTH_SHORT).show();
            }
            m_viewModel.setIsFirstIn(true);
            refreshSubjectList();
            break;
        case SWITCH_BOARD_TYPE:
            // String[] items = { "同主题", "普通模式", "文摘区", "保留区" };
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.post_alert_title);
            // builder.setItems(items,this);
            builder.setAdapter(new BoardTypeListAdapter(m_viewModel.getBoardType(), m_inflater), this);
            AlertDialog alert = builder.create();
            alert.show();
            break;
        case SWITCH_STICKY:
            // switch the variable, but do not save it
            aSMApplication.getCurrentApplication().switchHidePinSubject();
            if(aSMApplication.getCurrentApplication().isHidePinSubject()){
                Toast.makeText(getActivity(), "置底文章已隐藏", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "显示置底文章", Toast.LENGTH_SHORT).show();
            }
            refreshSubjectList();
            break;
        case REFRESH_SUBJECTLIST:
            refreshSubjectList();
            break;
        case SEARCH_POST:
            Intent postIntent = new Intent();
            postIntent.setClassName("com.athena.asm", "com.athena.asm.SearchPostActivity");
            postIntent.putExtra(StringUtility.BOARD, m_viewModel.getCurrentBoard().getEngName());
            postIntent.putExtra(StringUtility.BID, m_viewModel.getCurrentBoard().getBoardID());
            startActivity(postIntent);
            break;
        case NEW_POST:
            Intent writeIntent = new Intent();
            writeIntent.setClassName("com.athena.asm", "com.athena.asm.WritePostActivity");
            writeIntent.putExtra(StringUtility.URL, "http://www.newsmth.net/bbspst.php?board="
                    + m_viewModel.getCurrentBoard().getEngName());
            writeIntent.putExtra(StringUtility.WRITE_TYPE, 0);
            writeIntent.putExtra(StringUtility.IS_REPLY, false);
            // startActivity(intent);
            startActivityForResult(writeIntent, 0);
            break;
        case ADD_TO_FAVORITE:
            Board board = m_viewModel.getCurrentBoard();
            HomeViewModel viewModel = aSMApplication.getCurrentApplication().getHomeViewModel();
            // groupid = "0", we always add the board to root group
            EditFavoriteTask task = new EditFavoriteTask(getActivity(), viewModel, "0", board.getEngName(), board.getBoardID(),
                    EditFavoriteTask.FAVORITE_ADD);
            task.execute();
            break;
        default:
            break;
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        m_viewModel.setIsFirstIn(true);
        m_viewModel.setBoardType(which);
        LoadSubjectTask loadSubjectTask = new LoadSubjectTask(m_viewModel);
        loadSubjectTask.execute();
        dialog.dismiss();
        if (m_progressDialogProvider != null) {
            m_progressDialogProvider.showProgressDialog();
        }
    }

    @Override
    public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object... params) {
        if (changedPropertyName.equals(SubjectListViewModel.SUBJECTLIST_PROPERTY_NAME)) {
            reloadSubjectList();
            if (m_progressDialogProvider != null) {
                // delay the dismiss action by 0.5 second
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        m_progressDialogProvider.dismissProgressDialog();
                    }
                }, 500);
            }
        }
    }

    private void setListOffsetByPage(int jump) {
        int offset = (int) (m_listView.getHeight() * 0.95);
        if (jump == -1) {
            m_listView.smoothScrollBy(-1 * offset, 500);
        } else {
            m_listView.smoothScrollBy(offset, 500);
        }
    }

    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            setListOffsetByPage(1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            setListOffsetByPage(-1);
            return true;
        }
        return false;
    }
}
