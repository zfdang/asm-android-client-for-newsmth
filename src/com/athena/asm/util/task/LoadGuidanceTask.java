package com.athena.asm.util.task;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Subject;

public class LoadGuidanceTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;

	public LoadGuidanceTask(HomeActivity activity) {
		this.homeActivity = activity;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载首页导读中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		Object[] guidance = homeActivity.smthSupport.getGuidance();
		homeActivity.guidanceSectionNames = (List<String>) guidance[0];
		homeActivity.guidanceSectionDetails = (List<List<Subject>>) guidance[1];
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.reloadGuidanceList();
	}
}
