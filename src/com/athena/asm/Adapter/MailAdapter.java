package com.athena.asm.Adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.MailBox;

public class MailAdapter extends BaseAdapter {

	private LayoutInflater m_inflater;
	private MailBox m_mailBox;

	public MailAdapter(LayoutInflater inflater, MailBox mailBox) {
		this.m_inflater = inflater;
		this.m_mailBox = mailBox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		} else {
			layout = m_inflater.inflate(R.layout.mail_list_section_header, null);
		}
		TextView boxNameTextView = (TextView) layout.findViewById(R.id.BoxName);
		TextView numberTextView = (TextView) layout
				.findViewById(R.id.mailNumber);
		boxNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.m_application.getGuidanceFontSize());
		numberTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.m_application.getGuidanceFontSize());
		switch (position) {
		case 0:
			boxNameTextView.setText(R.string.mail_inbox);
			String numberString = m_mailBox.getInboxNumber()+"封 ";
			if (m_mailBox.isHavingNewMail()) {
				numberString += "（新）";
			}
			numberTextView.setText(numberString);
			break;
		case 1:
			boxNameTextView.setText(R.string.mail_outbox);
			numberTextView.setText(m_mailBox.getOutboxNumber()+"封 ");
			break;
		case 2:
			boxNameTextView.setText(R.string.mail_trash);
			numberTextView.setText(m_mailBox.getTrashboxNumber()+"封 ");
			break;
		case 3:
			boxNameTextView.setText(R.string.mail_write_mail);
			numberTextView.setText("");
			break;
		case 4:
			boxNameTextView.setText(R.string.new_at);
			if (m_mailBox.isHavingNewAt()) {
				numberTextView.setText("新");
			} else {
				numberTextView.setText("");
			}
			break;
		case 5:
			boxNameTextView.setText(R.string.new_reply);
			if (m_mailBox.isHavingNewReply()) {
				numberTextView.setText("新");
			} else {
				numberTextView.setText("");
			}
			break;
		default:
			break;
		}
		
		if (HomeActivity.m_application.isNightTheme()) {
			boxNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			numberTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}

		return layout;
	}

	@Override
	public int getCount() {
		return 6;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
