package com.athena.asm.data;

import java.io.Serializable;
import java.util.Date;

public class Subject implements Serializable {
	private static final long serialVersionUID = 7351647738651826553L;
	public static final String TYPE_BOTTOM = "d"; // 置底所包含的标记字符
	private String subjectID;
	private String title;
	private String author;
	private String boardID;
	private String boardEngName;
	private String boardChsName;
	private Date date;
	private String type;

	private int totalPageNo;
	private int currentPageNo;

	public String getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(String subjectID) {
		this.subjectID = subjectID;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setBoardEngName(String boardEngName) {
		this.boardEngName = boardEngName;
	}

	public void setBoardID(String boardID) {
		this.boardID = boardID;
	}

	public String getBoardID() {
		return boardID;
	}

	public String getBoardEngName() {
		return boardEngName;
	}

	public String getBoardChsName() {
		return boardChsName;
	}

	public void setBoardChsName(String boardName) {
		this.boardChsName = boardName;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setTotalPageNo(int totalPageNo) {
		this.totalPageNo = totalPageNo;
	}

	public int getTotalPageNo() {
		return totalPageNo;
	}

	public void setCurrentPageNo(int page) {
		this.currentPageNo = page;
	}

	public int getCurrentPageNo() {
		return currentPageNo;
	}
}
