package com.athena.asm.util.task;

import java.util.List;

import android.os.AsyncTask;

import com.athena.asm.aSMApplication;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;
import com.athena.asm.fragment.SubjectListFragment;
import com.athena.asm.viewmodel.PostListViewModel;

public class LoadPostTask extends AsyncTask<String, Integer, String> {
	private int m_boardType;
	private int m_action;
	private boolean m_isSilent;
	private boolean m_isUsePreload;
	private Subject m_subject;
	private int m_startNumber;
	private String m_url;
	
	private PostListViewModel m_viewModel;

	public LoadPostTask(PostListViewModel viewModel, Subject subject, int action, 
			boolean isSilent, boolean isUsePreload, int startNumber, String url) {
		m_boardType = viewModel.getBoardType();
		m_action = action;
		m_isSilent = isSilent;
		m_isUsePreload = isUsePreload;
		m_subject = subject;
		m_viewModel = viewModel;
		m_startNumber = startNumber;
		m_url = url;
	}

	@Override
	protected void onPreExecute() {
	}
	
	private List<Post> getPostList() {
		List<Post> postList = null;
		if (m_boardType == SubjectListFragment.BOARD_TYPE_SUBJECT) {
		    // view post in subject mode
			if (m_startNumber == 0) {
			    // expand from the first post, load from m
			    // http://m.newsmth.net/article/CouponsLife/375717
				postList = m_viewModel.getSmthSupport().getPostListFromMobile(m_subject, aSMApplication.getCurrentApplication().getBlackList(), m_boardType);
			} else {
			    // expand from other post, load from www
			    // http://www.newsmth.net/bbstcon.php?board=BasketballForum&gid=2956789&start=2956791
				postList = m_viewModel.getSmthSupport().getPostList(m_subject, aSMApplication.getCurrentApplication().getBlackList(), m_startNumber);
			}
		}
		else if (m_boardType == SubjectListFragment.BOARD_TYPE_NORMAL) {
		    // view post in normal mode
			if (m_action == Post.ACTION_DEFAULT) {
				if (m_url != null && m_url.length() > 0) {
				    // from reply or @
                    // m_url = "http://m.newsmth.net/refer/at/read?index=" + mail.getNumber();
                    // m_url = "http://m.newsmth.net/refer/reply/read?index=" + mail.getNumber();
					Subject newSubject = new Subject();
					postList = m_viewModel.getSmthSupport().getSinglePostListFromMobileUrl(newSubject, m_url);
					m_viewModel.updateSubject(newSubject);
				} else {
					postList = m_viewModel.getSmthSupport().getSinglePostList(m_subject);
				}
			}
			else {
			    // m_action might be:
			    //   ACTION_FIRST_POST_IN_SUBJECT
			    //   ACTION_PREVIOUS_POST_IN_SUBJECT
			    //   ACTION_NEXT_POST_IN_SUBJECT
				postList = m_viewModel.getSmthSupport().getTopicPostList(m_subject, m_action);
			}
		} else {
		    // digest or mark mode
			postList = m_viewModel.getSmthSupport().getPostListFromMobile(m_subject, aSMApplication.getCurrentApplication().getBlackList(), m_boardType);
		}
		return postList;
	}

	@Override
	protected String doInBackground(String... params) {
		
		if (m_isSilent) {
			m_viewModel.setPreloadPostList(getPostList());
			m_viewModel.setIsPreloadFinished(true);
		}
		else {
			if (m_isUsePreload && m_viewModel.isPreloadFinished() && m_viewModel.getPreloadPostList() != null) {
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
		if (!m_isSilent) {
			m_viewModel.notifyPostListChanged();
	        if(m_viewModel.getPostListAdapter() != null){
	            m_viewModel.getPostListAdapter().updateIndexer();
	        }
		}
	}
}
