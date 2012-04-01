package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.viewmodel.HomeViewModel;

public class LoadMailTask extends AsyncTask<String, Integer, String> {
	
	private HomeViewModel m_viewModel;

	private ProgressDialog pdialog;
	
	public LoadMailTask(Context ctx, HomeViewModel viewModel) {
		m_viewModel = viewModel;
		pdialog = new ProgressDialog(ctx);
	}

	@Override
	protected void onPreExecute() {
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
		m_viewModel.notifyMailboxChanged();
	}
}
