package com.athena.asm.data;

public class MailBox {
	private int inboxNumber;
	private int outboxNumber;
	private int trashboxNumber;
	public void setInboxNumber(int inboxNumber) {
		this.inboxNumber = inboxNumber;
	}
	public int getInboxNumber() {
		return inboxNumber;
	}
	public void setOutboxNumber(int outboxNumber) {
		this.outboxNumber = outboxNumber;
	}
	public int getOutboxNumber() {
		return outboxNumber;
	}
	public void setTrashboxNumber(int trashboxNumber) {
		this.trashboxNumber = trashboxNumber;
	}
	public int getTrashboxNumber() {
		return trashboxNumber;
	}
	
	
}
