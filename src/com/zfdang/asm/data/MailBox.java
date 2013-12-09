package com.zfdang.asm.data;

public class MailBox {
	private int m_inboxNumber;
	private int m_outboxNumber;
	private int m_trashboxNumber;
	private boolean m_isHavingNewMail = false;
	private boolean m_isHavingNewAt = false;
	private boolean m_isHavingNewReply = false;
	
	public void setInboxNumber(int inboxNumber) {
		this.m_inboxNumber = inboxNumber;
	}
	public int getInboxNumber() {
		return m_inboxNumber;
	}
	public void setOutboxNumber(int outboxNumber) {
		this.m_outboxNumber = outboxNumber;
	}
	public int getOutboxNumber() {
		return m_outboxNumber;
	}
	public void setTrashboxNumber(int trashboxNumber) {
		this.m_trashboxNumber = trashboxNumber;
	}
	public int getTrashboxNumber() {
		return m_trashboxNumber;
	}
	public boolean isHavingNewMail() {
		return m_isHavingNewMail;
	}
	public void setHavingNewMail(boolean isHavingNewMail) {
		this.m_isHavingNewMail = isHavingNewMail;
	}
	public boolean isHavingNewAt() {
		return m_isHavingNewAt;
	}
	public void setHavingNewAt(boolean m_isHavingNewAt) {
		this.m_isHavingNewAt = m_isHavingNewAt;
	}
	public boolean isHavingNewReply() {
		return m_isHavingNewReply;
	}
	public void setHavingNewReply(boolean m_isHavingNewReply) {
		this.m_isHavingNewReply = m_isHavingNewReply;
	}
	
	
}
