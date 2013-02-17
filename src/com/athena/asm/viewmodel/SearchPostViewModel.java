package com.athena.asm.viewmodel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;

public class SearchPostViewModel extends BaseViewModel {
	
	private SmthSupport m_smthSupport;
	
	private String m_boardName;
	private String m_boardID;
	
	private String m_title;
	private String m_title2;
	private String m_title3;
	private String m_userId;
	private String m_days;
	private boolean m_mgFlag;
	private boolean m_agFlag;
	private boolean m_ogFlag;
	
	
	public SearchPostViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public String getBoardName() {
		return m_boardName;
	}
	
	public void setBoardName(String boardName) {
		m_boardName = boardName;
	}
	
	public String getBoardID() {
		return m_boardID;
	}
	
	public void setBoardID(String boardID) {
		m_boardID = boardID;
	}
	
	public String getTitleText() {
		return m_boardName + "版内文章搜索";
	}
	
	public String getTitle() {
		return m_title;
	}
	
	public void setTitle(String title) {
		m_title = title;
	}
	
	public String getTitle2() {
		return m_title2;
	}
	
	public void setTitle2(String title2) {
		m_title2 = title2;
	}
	
	public String getTitle3() {
		return m_title3;
	}
	
	public void setTitle3(String title3) {
		m_title3 = title3;
	}
	
	public String getUserId() {
		return m_userId;
	}
	
	public void setUserId(String userId) {
		m_userId = userId;
	}
	
	public String getDays() {
		return m_days;
	}
	
	public void setDays(String days) {
		m_days = days;
	}
	
	public boolean getMgFlag() {
		return m_mgFlag;
	}
	
	public void setMgFlag(boolean mgFlag) {
		m_mgFlag = mgFlag;
	}
	
	public boolean getAgFlag() {
		return m_agFlag;
	}
	
	public void setAgFlag(boolean agFlag) {
		m_agFlag = agFlag;
	}
	
	public boolean getOgFlag() {
		return m_ogFlag;
	}
	
	public void setOgFlag(boolean ogFlag) {
		m_ogFlag = ogFlag;
	}
	
	public List<Subject> searchSubject() {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("board=").append(m_boardName);
		
		try {
			queryBuilder.append("&title=").append(URLEncoder.encode(m_title.replaceAll(" ", "+"), "GBK"));
			queryBuilder.append("&title2=").append(URLEncoder.encode(m_title2.replaceAll(" ", "+"), "GBK"));
			queryBuilder.append("&title3=").append(URLEncoder.encode(m_title3.replaceAll(" ", "+"), "GBK"));
			queryBuilder.append("&userid=").append(m_userId.replaceAll(" ", "+"));
			queryBuilder.append("&dt=").append(m_days.replaceAll(" ", "+"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if (m_mgFlag) {
			queryBuilder.append("&mg=on");
		}
		if (m_mgFlag) {
			queryBuilder.append("&ag=on");
		}
		if (m_ogFlag) {
			queryBuilder.append("&og=on");
		}
		
		return m_smthSupport.getSearchSubjectList(m_boardName, m_boardID, queryBuilder.toString());
		
	}
	
	

}
