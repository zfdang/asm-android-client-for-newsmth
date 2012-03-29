package com.athena.asm.viewmodel;

import com.athena.asm.util.SmthSupport;

public class WritePostViewModel extends BaseViewModel {
	
	public static final int TYPE_POST = 0;
	public static final int TYPE_MAIL = 1;
	public static final int TYPE_POST_EDIT = 2;
	
	private SmthSupport m_smthSupport;
	
	private int m_writeType;
	private String m_toHandleUrl;
	private String m_postUrl = "";
	private String m_postTitle = "";
	private String m_postContent = "";
	
	private String m_mailUserId = "";
	private String m_mailNumber = "";
	private String m_mailDir = "";
	private String m_mailFile = "";
	
	private int m_sigNum = 0;
	private int m_selectedSigValue = 0;
	
	public WritePostViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public int getWriteType() {
		return m_writeType;
	}
	
	public void setWriteType(int writeType) {
		m_writeType = writeType;
	}
	
	public String getToHandlerUrl() {
		return m_toHandleUrl;
	}
	
	public void setToHandlerUrl(String toHandlerUrl) {
		m_toHandleUrl = toHandlerUrl;
	}
	
	public String getPostUrl() {
		return m_postUrl;
	}
	
	public void setPostUrl(String postUrl) {
		m_postUrl = postUrl;
	}
	
	public String getPostTitle() {
		return m_postTitle;
	}
	
	public void setPostTitile(String postTitle) {
		m_postTitle = postTitle;
	}
	
	public String getPostContent() {
		return m_postContent;
	}
	
	public void setPostContent(String postContent) {
		m_postContent = postContent;
	}
	
	public String getMailUserId() {
		return m_mailUserId;
	}
	
	public void setMailUserId(String userId) {
		m_mailUserId = userId;
	}
	
	public String getMailNumber() {
		return m_mailNumber;
	}
	
	public void setMailNumber(String mailNumber) {
		m_mailNumber = mailNumber;
	}
	
	public String getMailDir() {
		return m_mailDir;
	}
	
	public void setMailDir(String mailDir) {
		m_mailDir = mailDir;
	}
	
	public String getMailFile() {
		return m_mailFile;
	}
	
	public void setMailFile(String mailFile) {
		m_mailFile = mailFile;
	}
	
	public int getSigNumber() {
		return m_sigNum;
	}
	
	public void setSigNumber(int sigNumber) {
		m_sigNum = sigNumber;
	}
	
	public int getSelectedSigValue() {
		return m_selectedSigValue;
	}
	
	public void setSelectedSigValue(int selectedSigValue) {
		m_selectedSigValue = selectedSigValue;
	}
	
	public boolean sendPost() {
		final String sigParams = String.valueOf(m_selectedSigValue);
		boolean result = false;
		if (m_writeType == TYPE_POST) {
			result = m_smthSupport.sendPost(m_postUrl, m_postTitle, m_postContent, sigParams, false);
		} else if (m_writeType == TYPE_MAIL) {
			result = m_smthSupport.sendMail(m_postUrl, m_postTitle,
					m_mailUserId, m_mailNumber, m_mailDir, m_mailFile, sigParams, m_postContent);
		} else if (m_writeType == TYPE_POST_EDIT) {
			result = m_smthSupport.sendPost(m_postUrl, m_postTitle,m_postContent, sigParams, true);
		}
		
		return result;
	}

}
