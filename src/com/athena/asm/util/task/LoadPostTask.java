package com.athena.asm.util.task;

import java.util.List;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.PostListActivity;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;
import com.athena.asm.viewmodel.PostListViewModel;

public class LoadPostTask extends AsyncTask<String, Integer, String> {
	private PostListActivity postListActivity;
	private ProgressDialog pdialog;
	private int boardType;
	private int action;
	private boolean isSilent;
	private boolean isUsePreload;
	private Subject subject;
	
	private PostListViewModel m_viewModel;

	public LoadPostTask(PostListActivity activity, PostListViewModel viewModel, Subject subject, int action, 
			boolean isSilent, boolean isUsePreload) {
		this.postListActivity = activity;
		this.boardType = viewModel.getBoardType();
		this.action = action;
		this.isSilent = isSilent;
		this.isUsePreload = isUsePreload;
		this.subject = subject;
		
		m_viewModel = viewModel;
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
			m_viewModel.setPreloadPostList(getPostList());
			m_viewModel.setIsPreloadFinished(true);
		}
		else {
			if (isUsePreload && m_viewModel.isPreloadFinished() && m_viewModel.getPreloadPostList() != null) {
				m_viewModel.setIsPreloadFinished(false);
				m_viewModel.updatePostListFromPreloadPostList();
				m_viewModel.updateCurrentSubjectFromPreloadSubject();
			}
			else {
				m_viewModel.setPostList(getPostList());
			}
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (!isSilent) {
			pdialog.cancel();
			m_viewModel.notifyPostListChanged();
		}
	}

}
