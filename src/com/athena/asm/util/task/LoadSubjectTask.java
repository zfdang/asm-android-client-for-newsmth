package com.athena.asm.util.task;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.SubjectListActivity;
import com.athena.asm.data.Subject;
import com.athena.asm.viewmodel.SubjectListViewModel;

public class LoadSubjectTask extends AsyncTask<String, Integer, String> {
	private SubjectListActivity subjectListActivity;
	private ProgressDialog pdialog;
	private boolean isReloadPageNo;
	
	private SubjectListViewModel m_viewModel;
	
	public LoadSubjectTask(SubjectListActivity activity, SubjectListViewModel viewModel) {
		this.subjectListActivity = activity;
		m_viewModel = viewModel;
		this.isReloadPageNo = viewModel.isFirstIn();
	}
	
	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(subjectListActivity);
		pdialog.setMessage("加载版面列表中...");
		pdialog.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		List<Subject> subjectList = subjectListActivity.smthSupport.getSubjectList(m_viewModel.getCurrentBoard(), 
				m_viewModel.getBoardType(), isReloadPageNo, HomeActivity.application.getBlackList());
		m_viewModel.setSubjectList(subjectList);
		pdialog.cancel();
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifySubjectListChanged();
	}

}
