package com.athena.asm.util.task;

import com.athena.asm.PostListActivity;
import com.athena.asm.aSMApplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class LoadPostTask extends AsyncTask<String, Integer, String> {
	private PostListActivity postListActivity;
	private ProgressDialog pdialog;
	private int boardType;

	public LoadPostTask(PostListActivity activity, int boardType) {
		this.postListActivity = activity;
		this.boardType = boardType;
	}

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(postListActivity);
		pdialog.setMessage("努力加载中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		if (boardType == 0) {
			aSMApplication application = (aSMApplication)postListActivity.getApplication();
			postListActivity.postList = postListActivity.smthSupport.getPostList(postListActivity.currentSubject, application.getBlackList());
		}
		else {
			postListActivity.postList = postListActivity.smthSupport.getSinglePostList(postListActivity.currentSubject);
		}
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		postListActivity.reloadPostList();
	}

}
