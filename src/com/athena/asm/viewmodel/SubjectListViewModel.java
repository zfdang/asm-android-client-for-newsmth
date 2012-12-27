package com.athena.asm.viewmodel;

import java.util.List;

import com.athena.asm.aSMApplication;
import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;
import com.athena.asm.fragment.SubjectListFragment;
import com.athena.asm.util.SmthSupport;

public class SubjectListViewModel extends BaseViewModel {
	
	private Board m_currentBoard;
	private List<Subject> m_subjectList;
	
	private int m_currentPageNo = 1;
	private int m_boardType = SubjectListFragment.BOARD_TYPE_SUBJECT;
	
	private boolean m_isFirstIn = true;
	
	private boolean m_isInRotation = false;
	
	private SmthSupport m_smthSupport;
	
	public static final String SUBJECTLIST_PROPERTY_NAME = "SubjectList";
	
	public SubjectListViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public Board getCurrentBoard() {
		return m_currentBoard;
	}
	
	public boolean updateCurrentBoard(Board board, String boardType) {
		boolean isNewBoard = true;
		if (m_currentBoard != null) {
			isNewBoard = !m_currentBoard.getEngName().equals(board.getEngName());
		}
		
		if (isNewBoard) {
			setCurrentBoard(board);
			if (boardType.equals("001")) {
				setBoardType(0);
			} else {
				setBoardType(1);
			}
		}
		
		return isNewBoard;
	}
	
	public void setCurrentBoard(Board currentBoard) {
		m_currentBoard = currentBoard;
	}
	
	public List<Subject> getSubjectList() {
		return m_subjectList;
	}
	
	public void setSubjectList(List<Subject> subjectList) {
		m_subjectList = subjectList;
	}
	
	public boolean isFirstIn() {
		return m_isFirstIn;
	}
	
	public void setIsFirstIn(boolean isFirstIn) {
		m_isFirstIn = isFirstIn;
	}
	
	public int getCurrentPageNumber() {
		return m_currentPageNo;
	}
	
	public void setCurrentPageNumber(int pageNumber) {
		if (pageNumber < 1 || pageNumber > m_currentBoard.getTotalPageNo()) {
			return;
		}
		
		m_currentPageNo = pageNumber;
	}
	
	public int getBoardType() {
		return m_boardType;
	}
	
	public void gotoFirstPage() {
		m_currentPageNo = 1;
	}
	
	public void gotoLastPage() {
		m_currentPageNo = m_currentBoard.getTotalPageNo();
	}
	
	public void gotoNextPage() {
		m_currentPageNo++;
		if (m_currentPageNo > m_currentBoard.getTotalPageNo()) {
			m_currentPageNo = m_currentBoard.getTotalPageNo();
		}
	}
	
	public void gotoPrevPage() {
		m_currentPageNo--;
		if (m_currentPageNo < 1) {
			m_currentPageNo = 1;
		}
	}
	
	public String getTitleText() {
		return "[" + m_currentPageNo + "/" +
				m_currentBoard.getTotalPageNo() + "]" + m_currentBoard.getChsName();
	}
	
	public void updateBoardCurrentPage() {
		m_currentBoard.setCurrentPageNo(m_currentPageNo);
	}
	
	public void setBoardType(int boardType) {
		m_boardType = boardType;
	}
	
	public void toggleBoardType() {
		if (m_boardType == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			m_boardType = SubjectListFragment.BOARD_TYPE_NORMAL;
		}
		else {
			m_boardType = SubjectListFragment.BOARD_TYPE_SUBJECT;
		}
	}
	
	public void notifySubjectListChanged() {
		notifyViewModelChange(this, SUBJECTLIST_PROPERTY_NAME);
	}
	
	public boolean isInRotation() {
		return m_isInRotation;
	}
	
	public void setIsInRotation(boolean isInRotation) {
		m_isInRotation = isInRotation;
	}
	
	public List<Subject> getSubjectListFromSmth(boolean isReloadPageNo) {
		return m_smthSupport.getSubjectListFromMobile(m_currentBoard, m_boardType, isReloadPageNo, aSMApplication.getCurrentApplication().getBlackList());
		//return m_smthSupport.getSubjectList(m_currentBoard, m_boardType, isReloadPageNo, aSMApplication.getCurrentApplication().getBlackList());
	}

}
