package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.viewmodel.MailViewModel;

public class LoadMailContentTask extends AsyncTask<String, Integer, String> {
	private ProgressDialog pdialog;
	
	private MailViewModel m_viewModel;

	public LoadMailContentTask(Context context, MailViewModel viewModel) {
		pdialog = new ProgressDialog(context);
		m_viewModel = viewModel;
	}

	@Override
	protected void onPreExecute() {
		m_viewModel.m_isLoadingInProgress = true;
		pdialog.setMessage("努力加载中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		m_viewModel.getCurrentMailContent();		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		pdialog.cancel();
		m_viewModel.notifyCurrentMailContentChanged();
		m_viewModel.m_isLoadingInProgress = false;
	}

}
