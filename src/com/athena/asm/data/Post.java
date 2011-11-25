package com.athena.asm.data;

import java.util.ArrayList;
import java.util.Date;

public class Post {
	private String subjectID;
	private String topicSubjectID;
	private String title;
	private String author;
	private String board;
	private String boardID;
	private Date date;
	private String content;
	private ArrayList<Attachment> attachFiles;

	@Override
	public String toString() {
		return this.content;
	}

	public String getSubjectID() {
		return subjectID;
	}

	public void setSubjectID(String subjectid) {
		this.subjectID = subjectid;
	}

	public void setTopicSubjectID(String topicSubjectID) {
		this.topicSubjectID = topicSubjectID;
	}

	public String getTopicSubjectID() {
		return topicSubjectID;
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

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setBoardID(String boardid) {
		this.boardID = boardid;
	}

	public String getBoardID() {
		return boardID;
	}

	public ArrayList<Attachment> getAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(ArrayList<Attachment> attachFiles) {
		this.attachFiles = attachFiles;
	}

}
