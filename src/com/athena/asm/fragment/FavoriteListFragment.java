package com.athena.asm.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.athena.asm.ActivityFragmentTargets;
import com.athena.asm.OnOpenActivityFragmentListener;
import com.athena.asm.R;
import com.athena.asm.SubjectListActivity;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.FavoriteListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadFavoriteTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;

public class FavoriteListFragment extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver {

	private HomeViewModel m_viewModel;

	private LayoutInflater m_inflater;

	private ExpandableListView m_listView;

	private boolean m_isLoaded;
	
	private OnOpenActivityFragmentListener m_onOpenActivityFragmentListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		m_inflater = inflater;
		View layout = m_inflater.inflate(R.layout.favorite, null);
		m_listView = (ExpandableListView) layout.findViewById(R.id.favorite_list);

		aSMApplication application = (aSMApplication) getActivity()
				.getApplication();
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

		if (m_viewModel.getCurrentTab() != null
				&& m_viewModel.getCurrentTab().equals(
						StringUtility.TAB_FAVORITE)) {
			reloadFavorite();
		}
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);
		super.onDestroy();
	}

	private void extractSubDirectory(Board board, List<Board> currentBoardList) {
		for (Iterator<Board> iterator = board.getChildBoards().iterator(); iterator
				.hasNext();) {
			Board childBoard = iterator.next();
			if (childBoard.isDirectory()) {
				extractSubDirectory(childBoard, currentBoardList);
			} else {
				currentBoardList.add(childBoard);
			}
		}
	}

	public void reloadFavorite() {
		if (m_viewModel.getFavList() == null) {
			LoadFavoriteTask loadFavoriteTask = new LoadFavoriteTask(
					getActivity(), m_viewModel);
			loadFavoriteTask.execute();
		} else {
			m_isLoaded = true;
			List<String> directoryList = new ArrayList<String>();
			List<List<Board>> realBoardList = new ArrayList<List<Board>>();
			List<Board> rootBoardList = new ArrayList<Board>();

			boolean isRootBoardExist = false;
			for (Iterator<Board> iterator = m_viewModel.getFavList().iterator(); iterator
					.hasNext();) {
				Board board = iterator.next();
				if (board.isDirectory()) {
					directoryList.add(board.getDirectoryName());
					if (board.getDirectoryName().equals("最近访问版面")) {
						realBoardList.add(new ArrayList<Board>(
								aSMApplication.getCurrentApplication().getRecentBoards()));
					} else {
						List<Board> currentBoardList = new ArrayList<Board>();
						extractSubDirectory(board, currentBoardList);
						realBoardList.add(currentBoardList);
					}
				} else {
					isRootBoardExist = true;
					rootBoardList.add(board);
				}
			}

			if (isRootBoardExist) {
				directoryList.add("根目录");
				realBoardList.add(rootBoardList);
			}

			final FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(
					m_inflater, directoryList, realBoardList);
			m_listView.setAdapter(favoriteListAdapter);

			m_listView.setOnChildClickListener(new OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent,
						View view, int groupPosition, int childPosition, long id) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(StringUtility.BOARD,
							(Board) view.getTag());
					aSMApplication.getCurrentApplication().addRecentBoard((Board) view
							.getTag());
					
					if (m_onOpenActivityFragmentListener != null) {
						m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.SUBJECT_LIST, bundle);
					}
					return false;
				}
			});
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(HomeViewModel.FAVLIST_PROPERTY_NAME)) {
			reloadFavorite();
		} else if (changedPropertyName
				.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
			if (!m_isLoaded
					&& m_viewModel.getCurrentTab() != null
					&& m_viewModel.getCurrentTab().equals(
							StringUtility.TAB_FAVORITE)) {
				reloadFavorite();
			}
		}
	}
}
