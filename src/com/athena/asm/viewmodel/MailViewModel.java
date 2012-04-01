package com.athena.asm.viewmodel;

import java.util.List;

import com.athena.asm.data.Mail;
import com.athena.asm.util.SmthSupport;

public class MailViewModel extends BaseViewModel {
	
	private List<Mail> m_maillList;

	private int m_boxType = -1;
	
	private Mail m_currentMail;
	
	private SmthSupport m_smthSupport;
	
	public static final String MAILLIST_PROPERTY_NAME = "MailList";
	public static final String CURRENT_MAIL_CONTENT_PROPERTY_NAME = "CurrentMailContent";
	
	public MailViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public List<Mail> getMailList() {
		return m_maillList;
	}
	
	public void setMailList(List<Mail> mailList) {
		m_maillList = mailList;
	}
	
	public int getMailboxType() {
		return m_boxType;
	}
	
	public boolean tryUpdateMailboxType(int mailboxType) {
		boolean isToUpdate = m_maillList == null || m_boxType != mailboxType;
		if (isToUpdate) {
			m_boxType = mailboxType;
		}
		
		return isToUpdate;
	}
	
	public void setMailboxType(int mailboxType) {
		m_boxType = mailboxType;
	}
	
	public String getTitleText() {
		switch (m_boxType) {
		case 0:
			return "收件箱";
		case 1:
			return "发件箱";
		case 2:
			return "垃圾箱";
		default:
			return "";
		}
	}
	
	public String getCurrentMailTitle() {
		return m_currentMail.getTitle();
	}
	
	public int getPrevPageStartNumber() {
		return m_maillList.get(0).getNumber() - 20 + 1;
	}
	
	public int getNextPageStartNumber() {
		return m_maillList.get(m_maillList.size() - 1).getNumber() + 1;
	}
	
	public void notifyMailListChanged() {
		notifyViewModelChange(this, MAILLIST_PROPERTY_NAME);
	}
	
	public void notifyCurrentMailContentChanged() {
		notifyViewModelChange(this, CURRENT_MAIL_CONTENT_PROPERTY_NAME);
	}
	
	public void updateMailList(int mailboxType, int startNumber) {
		m_maillList = m_smthSupport.getMailList(mailboxType, startNumber);
	}
	
	public void getCurrentMailContent() {
		m_smthSupport.getMailContent(m_currentMail);		
	}
	
	public boolean tryUpdateCurrentMail(Mail mail) {
		boolean isNewMail = true;
		if (m_currentMail != null && m_currentMail.getContent() != null) {
			isNewMail = m_currentMail.getNumber() != mail.getNumber() ||
					    m_currentMail.getBoxType() != mail.getBoxType();
		}
		
		if (isNewMail) {
			m_currentMail = mail;
		}
		
		return isNewMail;
	}
	
	public Mail getCurrentMail() {
		return m_currentMail;
	}
	
	public void clear() {
		m_maillList = null;
		m_boxType = -1;
		m_currentMail = null;
	}
}
