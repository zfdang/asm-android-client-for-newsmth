package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;

public class LoginTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	private String userName;
	private String password;
	private boolean isLogined;

	public LoginTask(HomeActivity activity, String userName, String password) {
		this.homeActivity = activity;
		this.userName = userName;
		this.password = password;
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
		homeActivity.smthSupport.setUserid(userName);
		homeActivity.smthSupport.setPasswd(password);
		isLogined = homeActivity.smthSupport.login();
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.loginTaskDone(isLogined);
	}
}
