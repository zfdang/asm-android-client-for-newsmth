package com.zfdang.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.zfdang.asm.viewmodel.MailViewModel;

public class LoadMailListTask extends AsyncTask<String, Integer, String> {
	private ProgressDialog pdialog;
	private int m_mailboxType;
	private int startNumber;
	
	private MailViewModel m_viewModel;
	
	public LoadMailListTask(Context context, MailViewModel viewModel, int startNumber) {
		this.startNumber = startNumber;
		
		m_viewModel = viewModel;
		m_mailboxType = viewModel.getMailboxType();
		
		pdialog = new ProgressDialog(context);
	}
	
	@Override
	protected void onPreExecute() {
		pdialog.setMessage("加载消息中...");
		pdialog.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		m_viewModel.updateMailList(m_mailboxType, startNumber);
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		pdialog.cancel();
		m_viewModel.notifyMailListChanged();
	}

}
