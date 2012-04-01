package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.List;

import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;

public class PostListViewModel extends BaseViewModel {
	
	private Subject m_currentSubject;
	private List<Post> m_postList;

	private boolean m_isToRefreshBoard = false;
	private int m_currentPageNo = 1;
	private int m_boardType = 0; // 1是普通，0是同主题
	
	private boolean m_isPreloadFinished = false;
	private List<Post> m_preloadPostList;
	private Subject m_preloadSubject;
	
	public static final String POSTLIST_PROPERTY_NAME = "PostList";
	
	public Subject currentSubject() {
		return m_currentSubject;
	}
	
	public void setCurrentSubject(Subject currentSubject) {
		m_currentSubject = currentSubject;
	}
	
	public List<Post> postList() {
		return m_postList;
	}
	
	public void setPostList(List<Post> postList) {
		m_postList = postList;
		m_currentSubject.setTitle(m_postList.get(0).getTitle());
	}
	
	public void ensurePostExists() {
		if (m_postList == null) {
			m_postList = new ArrayList<Post>();
			Post post = new Post();
			post.setAuthor("guest");
			post.setSubjectID(m_currentSubject.getSubjectID());
			post.setBoardID(m_currentSubject.getBoardID());
			post.setBoard(m_currentSubject.getBoardEngName());
			post.setContent("无法加载该贴");
			m_postList.add(post);
		}
	}
	
	public int currentPageNumber() {
		return m_currentPageNo;
	}
	
	public void setCurrentPageNumber(int pageNumber) {
		if (pageNumber < 1 || pageNumber > m_currentSubject.getTotalPageNo()) {
			return;
		}
		
		m_currentPageNo = pageNumber;
	}
	
	public void updateCurrentPageNumberFromSubject() {
		m_currentPageNo = m_currentSubject.getCurrentPageNo();
	}
	
	public void updateSubjectCurrentPageNumberFromCurrentPageNumber() {
		m_currentSubject.setCurrentPageNo(m_currentPageNo);
	}
	
	public void updateSubjectIDFromTopicSubjectID() {
		m_currentSubject.setSubjectID(m_currentSubject.getTopicSubjectID());
	}
	
	public void setSubjectCurrentPageNumber(int pageNumber) {
		m_currentSubject.setCurrentPageNo(pageNumber);
	}
	
	public int nextPageNumber() {
		int nextPageNumber = m_currentPageNo + 1;
		if (nextPageNumber > m_currentSubject.getTotalPageNo()) {
			return -1;
		}
		
		return nextPageNumber;
	}
	
	public void gotoFirstPage() {
		m_currentPageNo = 1;
	}
	
	public void gotoLastPage() {
		m_currentPageNo = m_currentSubject.getTotalPageNo();
	}
	
	public void gotoNextPage() {
		m_currentPageNo++;
		if (m_currentPageNo > m_currentSubject.getTotalPageNo()) {
			m_currentPageNo = m_currentSubject.getTotalPageNo();
		}
	}
	
	public void gotoPrevPage() {
		m_currentPageNo--;
		if (m_currentPageNo < 1) {
			m_currentPageNo = 1;
		}
	}
	
	public String subjectTitle() {
		
		if (m_boardType == 0) {
			return "[" + m_currentPageNo + "/"
					+ m_currentSubject.getTotalPageNo() + "]"
					+ m_currentSubject.getTitle();
		}
		else {
			return m_currentSubject.getTitle();
		}
		
	}
	
	public int boardType() {
		return m_boardType;
	}
	
	public void setBoardType(int boardType) {
		m_boardType = boardType;
	}
	
	public Subject preloadSubject() {
		return m_preloadSubject;
	}
	
	public void updatePreloadSubjectFromCurrentSubject() {
		m_preloadSubject = new Subject(m_currentSubject);
	}
	
	public void updateCurrentSubjectFromPreloadSubject() {
		m_currentSubject = m_preloadSubject;
	}
	
	public List<Post> preloadPostList() {
		return m_preloadPostList;
	}
	
	public void setPreloadPostList(List<Post> preloadPostList) {
		m_preloadPostList = preloadPostList;
	}
	
	public void updatePostListFromPreloadPostList() {
		m_postList = m_preloadPostList;
		m_preloadPostList = null;
	}
	
	public boolean isPreloadFinished() {
		return m_isPreloadFinished;
	}
	
	public void setIsPreloadFinished(boolean isPreloadFinished) {
		m_isPreloadFinished = isPreloadFinished;
	}
	
	public boolean isToRefreshBoard() {
		return m_isToRefreshBoard;
	}
	
	public void setIsToRefreshBoard(boolean isToRefreshBoard) {
		m_isToRefreshBoard = isToRefreshBoard;
	}
	
	public boolean updateSubject(Subject subject) {
		
		boolean isNewSubject = true;
		
		if (m_currentSubject != null) {
			isNewSubject = !m_currentSubject.getSubjectID().equals(subject.getSubjectID()) ||
					   	    m_currentSubject.getCurrentPageNo() != subject.getCurrentPageNo();
		}
		
		if (isNewSubject) {
			setCurrentSubject(subject);
			updateCurrentPageNumberFromSubject();
			updatePreloadSubjectFromCurrentSubject();
		}
		
		return isNewSubject;
	}
	
	public void NotifyPostListChanged() {
		NotifyViewModelChange(this, POSTLIST_PROPERTY_NAME);
	}

}
