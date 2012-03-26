package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoginTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	private String userName;
	private String password;
	private boolean isLogined;
	
	private HomeViewModel m_viewModel;

	public LoginTask(HomeActivity activity, HomeViewModel viewModel, String userName, String password) {
		this.homeActivity = activity;
		this.userName = userName;
		this.password = password;
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("登陆中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		isLogined = m_viewModel.login(userName, password);
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.loginTaskDone(isLogined);
	}
}
