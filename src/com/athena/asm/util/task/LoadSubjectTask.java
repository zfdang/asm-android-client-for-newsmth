package com.athena.asm.util.task;

import java.util.List;

import android.os.AsyncTask;

import com.athena.asm.data.Subject;
import com.athena.asm.viewmodel.SubjectListViewModel;

public class LoadSubjectTask extends AsyncTask<String, Integer, String> {
	private boolean isReloadPageNo;
	
	private SubjectListViewModel m_viewModel;
	
	public LoadSubjectTask(SubjectListViewModel viewModel) {
		m_viewModel = viewModel;
		this.isReloadPageNo = viewModel.isFirstIn();
	}
	
	@Override
	protected void onPreExecute() {
	}
	
	@Override
	protected String doInBackground(String... params) {
		List<Subject> subjectList = m_viewModel.getSubjectListFromSmth(isReloadPageNo);
		m_viewModel.setSubjectList(subjectList);
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifySubjectListChanged();
	}

}
