package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.ViewProfileActivity;
import com.athena.asm.data.Profile;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadProfileTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity = null;
	private ViewProfileActivity viewProfileActivity = null;
	private String userID;
	private Profile profile;
	private int step;
	private int type;
	
	private HomeViewModel m_viewModel;

	public LoadProfileTask(HomeActivity activity, HomeViewModel viewModel, String userID, int step) {
		this.homeActivity = activity;
		this.userID = userID;
		this.step = step;
		type = 0;
		m_viewModel = viewModel;
	}
	
	public LoadProfileTask(ViewProfileActivity activity, String userID, int step) {
		this.viewProfileActivity = activity;
		this.userID = userID;
		this.step = step;
		type = 1;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		if (type == 0) {
			pdialog = new ProgressDialog(homeActivity);
		}
		else {
			pdialog = new ProgressDialog(viewProfileActivity);
		}
		pdialog.setMessage("加载用户信息中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		if (type == 0) {
			profile = m_viewModel.getProfile(userID);
		}
		else {
			profile = viewProfileActivity.smthSupport.getProfile(userID);
		}
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		step++;
		if (type == 0) {
			homeActivity.reloadProfile(profile, step);
		}
		else {
			viewProfileActivity.reloadProfile(profile);
		}
	}
}
