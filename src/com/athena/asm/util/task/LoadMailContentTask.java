package com.athena.asm.util.task;

import com.athena.asm.ReadMailActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class LoadMailContentTask extends AsyncTask<String, Integer, String> {
	private ReadMailActivity readMailActivity;
	private ProgressDialog pdialog;

	public LoadMailContentTask(ReadMailActivity activity) {
		this.readMailActivity = activity;
	}

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(readMailActivity);
		pdialog.setMessage("努力加载中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		readMailActivity.smthSupport.getMailContent(readMailActivity.currentMail);		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		readMailActivity.loadMailContent();
	}

}
