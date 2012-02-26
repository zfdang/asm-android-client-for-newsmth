package com.athena.asm.util.task;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.PostListActivity;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;

public class LoadPostTask extends AsyncTask<String, Integer, String> {
	private PostListActivity postListActivity;
	private ProgressDialog pdialog;
	private int boardType;
	private int action;
	private boolean isSilent;
	private boolean isUsePreload;
	private Subject subject;

	public LoadPostTask(PostListActivity activity, Subject subject, int boardType, int action, 
			boolean isSilent, boolean isUsePreload) {
		this.postListActivity = activity;
		this.boardType = boardType;
		this.action = action;
		this.isSilent = isSilent;
		this.isUsePreload = isUsePreload;
		this.subject = subject;
	}

	@Override
	protected void onPreExecute() {
		if (!isSilent) {
			pdialog = new ProgressDialog(postListActivity);
			pdialog.setMessage("努力加载中...");
			pdialog.show();
		}
	}
	
	private List<Post> getPostList() {
		List<Post> postList = null;
		if (boardType == 0) {
			postList = postListActivity.smthSupport.getPostListFromMobile(subject, HomeActivity.application.getBlackList());
			//postListActivity.postList = postListActivity.smthSupport.getPostList(subject, HomeActivity.application.getBlackList(), startNumber);
		}
		else {
			if (action == 0) {
				postList = postListActivity.smthSupport.getSinglePostList(subject);
			}
			else {
				postList = postListActivity.smthSupport.getTopicPostList(subject, action);
			}
		}
		return postList;
	}

	@Override
	protected String doInBackground(String... params) {
		
		if (isSilent) {
			postListActivity.preloadPostList = getPostList();
			postListActivity.isPreloadFinish = true;
		}
		else {
			if (isUsePreload && postListActivity.isPreloadFinish && postListActivity.preloadPostList != null) {
				postListActivity.isPreloadFinish = false;
				postListActivity.postList = postListActivity.preloadPostList;
				postListActivity.preloadPostList = null;
				postListActivity.currentSubject = postListActivity.preloadSubject;
			}
			else {
				postListActivity.postList = getPostList();
			}
			
			pdialog.cancel();
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (!isSilent) {
			postListActivity.reloadPostList();
		}
	}

}
