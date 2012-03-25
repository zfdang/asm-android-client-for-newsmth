package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadMailTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	
	private HomeViewModel m_viewModel;

	public LoadMailTask(HomeActivity activity, HomeViewModel viewModel) {
		this.homeActivity = activity;
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载邮箱信息中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		m_viewModel.updateMailbox();
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.loadMail();
	}
}
