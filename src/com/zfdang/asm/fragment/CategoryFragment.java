package com.zfdang.asm.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.zfdang.asm.ActivityFragmentTargets;
import com.zfdang.asm.OnOpenActivityFragmentListener;
import com.zfdang.asm.R;
import com.zfdang.asm.aSMApplication;
import com.zfdang.asm.Adapter.CategoryListAdapter;
import com.zfdang.asm.data.Board;
import com.zfdang.asm.listener.OnKeyDownListener;
import com.zfdang.asm.util.AutoCompleteAdapter;
import com.zfdang.asm.util.ListViewUtil;
import com.zfdang.asm.util.StringUtility;
import com.zfdang.asm.util.task.LoadCategoryTask;
import com.zfdang.asm.viewmodel.BaseViewModel;
import com.zfdang.asm.viewmodel.HomeViewModel;

public class CategoryFragment extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver, OnClickListener, OnKeyDownListener {

	private HomeViewModel m_viewModel;

	private LayoutInflater m_inflater;

	private View layout;
    private ListView m_listView;

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
		layout = m_inflater.inflate(R.layout.category, null);
		m_listView = (ListView)layout.findViewById(R.id.category_list);

		aSMApplication application = (aSMApplication) getActivity()
				.getApplication();
		m_viewModel = application.getHomeViewModel();
		m_viewModel.registerViewModelChangeObserver(this);

		m_isLoaded = false;

		return layout;
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
						StringUtility.TAB_CATEGORY)) {
			reloadCategory();
		}
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);
		super.onDestroy();
	}

	public void reloadCategory() {
		if (m_viewModel.getCategoryList() == null) {
			// it seems the following task might be called cocurrently
			// we might have to add Synchronized Methods
			LoadCategoryTask loadCategoryTask = new LoadCategoryTask(
					getActivity(), m_viewModel);
			loadCategoryTask.execute();
		} else {

			RelativeLayout relativeLayout = (RelativeLayout) layout
					.findViewById(R.id.board_relative_layout);

			Button goButton = (Button) relativeLayout
					.findViewById(R.id.btn_go_board);
			goButton.setOnClickListener(this);

			AutoCompleteTextView textView = (AutoCompleteTextView) relativeLayout
					.findViewById(R.id.search_board);
			textView.setCompletionHint("请输入版面中/英文名(支持部分查找)");
			ArrayAdapter<String> adapter = new AutoCompleteAdapter(getActivity(),
					android.R.layout.simple_dropdown_item_1line, m_viewModel.getBoardFullStrings());
			textView.setAdapter(adapter);

            ListView categoryList = (ListView) layout.findViewById(R.id.category_list);
            categoryList.setAdapter(new CategoryListAdapter(getActivity(), android.R.id.text1, m_viewModel
                    .getCategoryList(), getActivity().getLayoutInflater()));
			categoryList.setFastScrollEnabled(true);
			categoryList.setFastScrollAlwaysVisible(true);

            categoryList.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                    Bundle bundle = new Bundle();
                    Board board = ((CategoryListAdapter.ViewHolder) view.getTag()).board;
                    bundle.putSerializable(StringUtility.BOARD, board);
                    aSMApplication.getCurrentApplication().addRecentBoard(board);
                    if (m_onOpenActivityFragmentListener != null) {
                        m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.SUBJECT_LIST,
                                bundle);
                    }
                }

            });
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(HomeViewModel.CATEGORYLIST_PROPERTY_NAME))
		{
			reloadCategory();
		}
		else if (changedPropertyName.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME))
		{
			if (!m_isLoaded	&& m_viewModel.getCurrentTab() != null
					&& m_viewModel.getCurrentTab().equals(StringUtility.TAB_CATEGORY))
			{
				reloadCategory();
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_go_board) {
			AutoCompleteTextView textView = (AutoCompleteTextView) ((RelativeLayout) view
					.getParent()).findViewById(R.id.search_board);

			// this line of code has problem, since "IT业界特快" can't work in this method
			Board board = m_viewModel.getBoardHashMap().get(textView.getText().toString().toLowerCase());

			if (board == null) {
				new Handler().post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity().getApplicationContext(),
								"版面不存在.", Toast.LENGTH_SHORT).show();
					}
				});
				return;
			}

			InputMethodManager inputManager = (InputMethodManager) getActivity()
					.getSystemService(SherlockActivity.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);

			aSMApplication.getCurrentApplication().addRecentBoard(board);

			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.BOARD, board);
			if (m_onOpenActivityFragmentListener != null) {
				m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.SUBJECT_LIST, bundle);
			}
		}
	}

    @Override
    public boolean onKeyDown(int keyCode) {
        return ListViewUtil.ScrollListViewByKey(m_listView, keyCode);
    }
}
