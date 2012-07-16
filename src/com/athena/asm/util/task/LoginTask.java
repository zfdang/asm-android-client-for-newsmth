package com.athena.asm.util.task;

import android.os.AsyncTask;

import com.athena.asm.viewmodel.HomeViewModel;

public class LoginTask extends AsyncTask<String, Integer, String> {
	private String userName;
	private String password;
	private boolean isLogined;
	
	private HomeViewModel m_viewModel;

	public LoginTask(HomeViewModel viewModel, String userName, String password) {
		this.userName = userName;
		this.password = password;
		m_viewModel = viewModel;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected String doInBackground(String... params) {
		isLogined = m_viewModel.login(userName, password);
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifyLoginChanged(isLogined);
	}
}
