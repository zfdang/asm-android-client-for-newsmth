package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.ViewProfileActivity;
import com.athena.asm.data.Profile;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadProfileTask extends AsyncTask<String, Integer, String> {
	private ViewProfileActivity viewProfileActivity = null;
	private String userID;
	private Profile profile;
	private int step;
	private int type;
	private ProgressDialog pdialog;
	
	private HomeViewModel m_viewModel;

	public LoadProfileTask(Context ctx, HomeViewModel viewModel, String userID, int step) {
		this.userID = userID;
		this.step = step;
		type = 0;
		m_viewModel = viewModel;
		pdialog = new ProgressDialog(ctx);
	}
	
	public LoadProfileTask(ViewProfileActivity activity, String userID, int step) {
		this.viewProfileActivity = activity;
		this.userID = userID;
		this.step = step;
		type = 1;
		pdialog = new ProgressDialog(activity);
	}

	@Override
	protected void onPreExecute() {
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
			m_viewModel.notifyProfileChanged(profile, step);
		}
		else {
			viewProfileActivity.reloadProfile(profile);
		}
	}
}
