package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.MailListActivity;

public class LoadMailListTask extends AsyncTask<String, Integer, String> {
	private MailListActivity mailListActivity;
	private ProgressDialog pdialog;
	private int boxType;
	private int startNumber;
	
	public LoadMailListTask(MailListActivity activity, int boxType, int startNumber) {
		this.mailListActivity = activity;
		this.boxType = boxType;
		this.startNumber = startNumber;
	}
	
	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(mailListActivity);
		pdialog.setMessage("加载邮件中...");
		pdialog.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		mailListActivity.maillList = mailListActivity.smthSupport.getMailList(boxType, startNumber);
		pdialog.cancel();
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		mailListActivity.reloadMailList();
	}

}
