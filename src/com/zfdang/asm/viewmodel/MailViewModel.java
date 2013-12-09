package com.zfdang.asm.viewmodel;

import java.util.Iterator;
import java.util.List;

import com.zfdang.asm.data.Mail;
import com.zfdang.asm.util.SmthSupport;

public class MailViewModel extends BaseViewModel {
	
	private List<Mail> m_maillList;

	private int m_boxType = -1;
	
	private Mail m_currentMail;
	
	// for at & reply from mobile
	private int m_currentPageNo = 1;
	private int m_totalPageNo = 1;
	
	private SmthSupport m_smthSupport;
	
	public static final String MAILLIST_PROPERTY_NAME = "MailList";
	public static final String CURRENT_MAIL_CONTENT_PROPERTY_NAME = "CurrentMailContent";
	
	public MailViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public void markAllMessageRead() {
		if (getMailboxType() == 4) {
			m_smthSupport.markAllMessageRead(0);
		} else if (getMailboxType() == 5) {
			m_smthSupport.markAllMessageRead(1);
		}
	}
	
	public List<Mail> getMailList() {
		return m_maillList;
	}
	
	public void setMailList(List<Mail> mailList) {
		m_maillList = mailList;
	}
	
	public void setAllMailRead() {
		for (Iterator<Mail> iterator = m_maillList.iterator(); iterator.hasNext();) {
			Mail mail = iterator.next();
			mail.setUnread(false);
		}
	}
	
	public void setMailRead(int position) {
		m_maillList.get(position).setUnread(false);
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
		case 4:
			return "＠我";
		case 5:
			return "回我";
		default:
			return "";
		}
	}
	
	public String getCurrentMailTitle() {
		return m_currentMail.getTitle();
	}
	
	public int getFirstPageStartNumber() {
		if (m_boxType < 3) {
			return -1;
		} else {
			return 1;
		}
	}
	
	public int getLastPageStartNumber() {
		if (m_boxType < 3) {
			return 0;
		} else {
			return m_totalPageNo;
		}
	}
	
	public int getPrevPageStartNumber() {
		if (m_boxType < 3) {
			return m_maillList.get(m_maillList.size() - 1).getNumber() + 1;
		} else {
			m_currentPageNo--;
			if (m_currentPageNo == 0) {
				m_currentPageNo = 1;
			}
			return m_currentPageNo;
		}
	}
	
	public int getNextPageStartNumber() {
		if (m_boxType < 3) {
			return m_maillList.get(0).getNumber() - 20 + 1;
		} else {
			m_currentPageNo++;
			if (m_currentPageNo > m_totalPageNo) {
				m_currentPageNo = m_totalPageNo;
			}
			return m_currentPageNo;
		}
	}
	
	public void notifyMailListChanged() {
		notifyViewModelChange(this, MAILLIST_PROPERTY_NAME);
	}
	
	public void notifyCurrentMailContentChanged() {
		notifyViewModelChange(this, CURRENT_MAIL_CONTENT_PROPERTY_NAME);
	}
	
	public void updateMailList(int mailboxType, int startNumber) {
		if (mailboxType < 3) {
			m_maillList = m_smthSupport.getMailList(mailboxType, startNumber);
		} else {
			m_maillList = m_smthSupport.getReplyOrAtList(this, mailboxType, startNumber);
		}
		
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

	public int getTotalPageNo() {
		return m_totalPageNo;
	}

	public void setTotalPageNo(int m_totalPageNumber) {
		this.m_totalPageNo = m_totalPageNumber;
	}

	public int getCurrentPageNo() {
		return m_currentPageNo;
	}

	public void setCurrentPageNo(int m_currentPageNumber) {
		this.m_currentPageNo = m_currentPageNumber;
	}
}
