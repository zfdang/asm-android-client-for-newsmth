package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.SubjectListActivity;

public class LoadSubjectTask extends AsyncTask<String, Integer, String> {
	private SubjectListActivity subjectListActivity;
	private ProgressDialog pdialog;
	private int boardType;
	
	public LoadSubjectTask(SubjectListActivity activity, int boardType) {
		this.subjectListActivity = activity;
		this.boardType = boardType;
	}
	
	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(subjectListActivity);
		pdialog.setMessage("加载版面列表中...");
		pdialog.show();
	}
	
	@Override
	protected String doInBackground(String... params) {
		subjectListActivity.subjectList = subjectListActivity.smthSupport.getSubjectList(subjectListActivity.currentBoard, boardType);
		pdialog.cancel();
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		subjectListActivity.reloadPostList();
	}

}
