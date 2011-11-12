package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Profile;

public class LoadProfileTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	private String userID;
	private Profile profile;
	private int step;

	public LoadProfileTask(HomeActivity activity, String userID, int step) {
		this.homeActivity = activity;
		this.userID = userID;
		this.step = step;
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
		profile = homeActivity.smthSupport.getProfile(userID);
		if (userID.equals(homeActivity.loginUserID)) {
			homeActivity.currentProfile = profile;
		}
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		step++;
		homeActivity.reloadProfile(profile, step);
	}
}
