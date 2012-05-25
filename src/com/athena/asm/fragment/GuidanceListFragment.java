package com.athena.asm.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.GuidanceListAdapter;
import com.athena.asm.data.Subject;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadGuidanceTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.GuidanceListViewModel;

public class GuidanceListFragment extends SherlockFragment
								 implements BaseViewModel.OnViewModelChangObserver {
	
	private GuidanceListViewModel m_viewModel;
	
	private LayoutInflater m_inflater;
	
	private ExpandableListView listView;
	
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
		listView = (ExpandableListView) layout
				.findViewById(R.id.guidance_list);
		
		aSMApplication application = (aSMApplication) getActivity().getApplication();
		m_viewModel = application.getGuidanceListViewModel();
		m_viewModel.registerViewModelChangeObserver(this);
		
		return listView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//reloadGuidanceList();
	}
	
	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver();
		super.onDestroy();
	}
	
	public void reloadGuidanceList() {
		if (m_viewModel.getGuidanceSectionNames() == null
				|| m_viewModel.getGuidanceSectionDetails() == null) {
			LoadGuidanceTask loadGuidanceTask = new LoadGuidanceTask(getActivity(),
					m_viewModel);
			loadGuidanceTask.execute();
		} else {
			
			listView.setAdapter(new GuidanceListAdapter(m_inflater, m_viewModel
					.getGuidanceSectionNames(), m_viewModel
					.getGuidanceSectionDetails()));
			listView.setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable(StringUtility.SUBJECT,
							(Subject) v.getTag());
					intent.putExtras(bundle);
					intent.setClassName("com.athena.asm",
							"com.athena.asm.PostListActivity");
					startActivity(intent);
					return true;
				}
			});
		}
	}
	
	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		if (changedPropertyName.equals(GuidanceListViewModel.GUIDANCE_PROPERTY_NAME)) {
			reloadGuidanceList();
		}
	}	
}
