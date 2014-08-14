package com.athena.asm.fragment;

import android.app.Activity;
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
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.GuidanceListAdapter;
import com.athena.asm.data.Subject;
import com.athena.asm.listener.OnKeyDownListener;
import com.athena.asm.util.ListViewUtil;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadGuidanceTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;

public class GuidanceListFragment extends SherlockFragment implements
		BaseViewModel.OnViewModelChangObserver, OnKeyDownListener {

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
		View layout = m_inflater.inflate(R.layout.guidance, null);
		m_listView = (ExpandableListView) layout.findViewById(R.id.guidance_list);

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
						StringUtility.TAB_GUIDANCE)) {
			reloadGuidanceList();
		}
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);
		super.onDestroy();
	}

	public void reloadGuidanceList() {
		if (m_viewModel.getGuidanceSectionNames() == null
				|| m_viewModel.getGuidanceSectionDetails() == null) {
			if(m_viewModel.m_isLoadingInProgress) return;
			LoadGuidanceTask loadGuidanceTask = new LoadGuidanceTask(
					getActivity(), m_viewModel);
			loadGuidanceTask.execute();
		} else {
			m_isLoaded = true;
			m_listView.setAdapter(new GuidanceListAdapter(m_inflater, m_viewModel
					.getGuidanceSectionNames(), m_viewModel
					.getGuidanceSectionDetails()));
			// expand first group -- top 10
			if(m_listView.getExpandableListAdapter().getGroupCount() > 0)
				m_listView.expandGroup(0);
			m_listView.setOnChildClickListener(new OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(StringUtility.SUBJECT, (Subject) v.getTag(R.id.tag_second));
					if (m_onOpenActivityFragmentListener != null) {
						m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.POST_LIST, bundle);
					}
					return true;
				}
			});
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(HomeViewModel.GUIDANCE_PROPERTY_NAME)) {
			reloadGuidanceList();
		} else if (changedPropertyName
				.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
			if (!m_isLoaded
					&& m_viewModel.getCurrentTab() != null
					&& m_viewModel.getCurrentTab().equals(
							StringUtility.TAB_GUIDANCE)) {
				reloadGuidanceList();
			}
		}
	}

    @Override
    public boolean onKeyDown(int keyCode) {
        return ListViewUtil.ScrollListViewByKey(m_listView, keyCode);
    }
}
