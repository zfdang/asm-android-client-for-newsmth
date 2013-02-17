package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.viewmodel.HomeViewModel;

public class LoadGuidanceTask extends AsyncTask<String, Integer, String> {
	private ProgressDialog pdialog;
	
	private HomeViewModel m_viewModel;

	public LoadGuidanceTask(Context ctx, HomeViewModel viewModel) {
		pdialog = new ProgressDialog(ctx);
		m_viewModel = viewModel;
	}

	@Override
	protected void onPreExecute() {
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
		m_viewModel.notifyGuidanceChanged();
	}
}
