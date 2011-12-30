package com.athena.asm.util.task;

import com.athena.asm.HomeActivity;
import com.athena.asm.PostListActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;

public class LoadPostTask extends AsyncTask<String, Integer, String> {
	private PostListActivity postListActivity;
	private ProgressDialog pdialog;
	private int boardType;
	private int action;
	private int startNumber;

	public LoadPostTask(PostListActivity activity, int boardType, int action, int startNumber) {
		this.postListActivity = activity;
		this.boardType = boardType;
		this.action = action;
		this.startNumber = startNumber;
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
			postListActivity.postList = postListActivity.smthSupport.getPostList(postListActivity.currentSubject, HomeActivity.application.getBlackList(), startNumber);
		}
		else {
			if (action == 0) {
				postListActivity.postList = postListActivity.smthSupport.getSinglePostList(postListActivity.currentSubject);
			}
			else {
				postListActivity.postList = postListActivity.smthSupport.getTopicPostList(postListActivity.currentSubject, action);
			}
		}
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		postListActivity.reloadPostList();
	}

}
