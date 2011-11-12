package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;

public class LoadMailTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;

	public LoadMailTask(HomeActivity activity) {
		this.homeActivity = activity;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
	}
}
