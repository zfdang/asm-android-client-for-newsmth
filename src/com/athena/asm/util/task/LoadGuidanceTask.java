package com.athena.asm.util.task;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Subject;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadGuidanceTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	
	private HomeViewModel m_viewModel;

	public LoadGuidanceTask(HomeActivity activity, HomeViewModel viewModel) {
		this.homeActivity = activity;
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载首页导读中...");
		pdialog.show();
	}

	
	@Override
	protected String doInBackground(String... params) {
		m_viewModel.updateGuidance();
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.reloadGuidanceList();
	}
}
