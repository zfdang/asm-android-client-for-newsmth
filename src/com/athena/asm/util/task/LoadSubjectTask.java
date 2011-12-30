package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.SubjectListActivity;

public class LoadSubjectTask extends AsyncTask<String, Integer, String> {
	private SubjectListActivity subjectListActivity;
	private ProgressDialog pdialog;
	private int boardType;
	private boolean isReloadPageNo;
	
	public LoadSubjectTask(SubjectListActivity activity, int boardType, boolean isReloadPageNo) {
		this.subjectListActivity = activity;
		this.boardType = boardType;
		this.isReloadPageNo = isReloadPageNo;
	}
	
	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(subjectListActivity);
		pdialog.setMessage("加载版面列表中...");
		pdialog.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		subjectListActivity.subjectList = subjectListActivity.smthSupport.getSubjectList(subjectListActivity.currentBoard, 
				boardType, isReloadPageNo, HomeActivity.application.getBlackList());
		pdialog.cancel();
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		subjectListActivity.reloadPostList();
	}

}
