package com.athena.asm.data;

import java.io.Serializable;
import java.util.ArrayList;

public class Board implements Serializable {
	private static final long serialVersionUID = -4618388540751724812L;
	private String boardID;
	private String boardEngName;
	private String boardChsName;
	private String categoryName;
	private String moderator;
	private boolean isDirectory;
	private String directoryName;
	private int currentPageNo;
	private int totalPageNo;
	private ArrayList<Board> childBoards = new ArrayList<Board>();

	@Override
	public String toString() {
		return this.boardEngName + "\t" + this.boardChsName;
	}

	public String getBoardID() {
		return boardID;
	}

	public void setBoardID(String boardID) {
		this.boardID = boardID;
	}

	public String getEngName() {
		return boardEngName;
	}

	public void setEngName(String name) {
		this.boardEngName = name;
	}

	public String getChsName() {
		return boardChsName;
	}

	public void setChsName(String cname) {
		this.boardChsName = cname;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setModerator(String moderator) {
		this.moderator = moderator;
	}

	public String getModerator() {
		return moderator;
	}

	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}

	public boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setCurrentPageNo(int currentPage) {
		this.currentPageNo = currentPage;
	}

	public int getCurrentPageNo() {
		return currentPageNo;
	}

	public void setTotalPageNo(int totalPageNo) {
		this.totalPageNo = totalPageNo;
	}

	public int getTotalPageNo() {
		return totalPageNo;
	}

	public void setChildBoards(ArrayList<Board> childBoards) {
		this.childBoards = childBoards;
	}

	public ArrayList<Board> getChildBoards() {
		return childBoards;
	}
}
