package com.zfdang.asm.data;

import java.io.Serializable;
import java.util.Date;

public class Mail implements Serializable {
	private static final long serialVersionUID = -3124521299194974452L;
	private boolean isUnread;
	private String valueString;
	private int number;
	private String status = "";
	private String senderID;
	private String title;
	private String dateString;
	private Date date;
	private String sizeString;
	private String boxString;
	private int boxType;
	private String boxDirString;
	private String content;
	
	public void setUnread(boolean isUnread) {
		this.isUnread = isUnread;
	}
	public boolean isUnread() {
		return isUnread;
	}
	public String getValueString() {
		return valueString;
	}
	public void setValueString(String valueString) {
		this.valueString = valueString;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getSenderID() {
		return senderID;
	}
	public void setSenderID(String senderID) {
		this.senderID = senderID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDateString() {
		return dateString;
	}
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	public void setSizeString(String sizeString) {
		this.sizeString = sizeString;
	}
	public String getSizeString() {
		return sizeString;
	}
	public void setBoxString(String boxString) {
		this.boxString = boxString;
	}
	public String getBoxString() {
		return boxString;
	}
	public void setBoxType(int boxType) {
		this.boxType = boxType;
	}
	public int getBoxType() {
		return boxType;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Date getDate() {
		return date;
	}
	public void setBoxDirString(String boxDirString) {
		this.boxDirString = boxDirString;
	}
	public String getBoxDirString() {
		return boxDirString;
	}	
}
