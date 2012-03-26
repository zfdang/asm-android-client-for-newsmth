package com.athena.asm.viewmodel;

import java.util.List;

import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;

public class SubjectListViewModel extends BaseViewModel {
	
	private Board m_currentBoard;
	private List<Subject> m_subjectList;
	
	private int m_currentPageNo = 1;
	private int m_boardType = 0;
	
	public static final String SUBJECTLIST_PROPERTY_NAME = "SubjectList";
	
	public Board currentBoard() {
		return m_currentBoard;
	}
	
	public void setCurrentBoard(Board currentBoard) {
		m_currentBoard = currentBoard;
	}
	
	public List<Subject> subjectList() {
		return m_subjectList;
	}
	
	public void setSubjectList(List<Subject> subjectList) {
		m_subjectList = subjectList;
	}
	
	public int currentPageNumber() {
		return m_currentPageNo;
	}
	
	public void setCurrentPageNumber(int pageNumber) {
		if (pageNumber < 1 || pageNumber > m_currentBoard.getTotalPageNo()) {
			return;
		}
		
		m_currentPageNo = pageNumber;
	}
	
	public int boardType() {
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
		if (m_currentPageNo < 1) {
			m_currentPageNo = 1;
		}
	}
	
	public void gotoPrevPage() {
		m_currentPageNo--;
		if (m_currentPageNo > m_currentBoard.getTotalPageNo()) {
			m_currentPageNo = m_currentBoard.getTotalPageNo();
		}
	}
	
	public String titleText() {
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
		if (m_boardType == 0) {
			m_boardType = 1;
		}
		else {
			m_boardType = 0;
		}
	}
	
	public void NotifySubjectListChanged() {
		m_changeObserver.OnViewModelChange(this, SUBJECTLIST_PROPERTY_NAME);
	}

}
