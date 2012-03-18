package com.athena.asm.Adapter;

import java.util.List;

import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.Mail;

public class MailListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Mail> mailList;

	public MailListAdapter(LayoutInflater inflater, List<Mail> mailList) {
		this.inflater = inflater;
		this.mailList = mailList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		}
		else {
			layout = inflater.inflate(R.layout.mail_list_item, null);
		}
		
		Mail mail = mailList.get(mailList.size() - position - 1);
		
		TextView authorTextView = (TextView) layout.findViewById(R.id.SenderID);
		if (mail.getStatus().length() > 0) {
			authorTextView.setText("【"+ mail.getStatus() + "】" + mail.getSenderID());
		}
		else {
			authorTextView.setText(mail.getSenderID());
		}
		TextView titleTextView = (TextView) layout.findViewById(R.id.MailTitle);
		String titleString = mail.getTitle();
		if (mail.isUnread()) {
			//TextPaint tp = titleTextView.getPaint();
			//tp.setFakeBoldText(true);
			titleString = "<font color='red'>" + titleString + "</font>";
		}
		else {
			//TextPaint tp = titleTextView.getPaint();
			//tp.setFakeBoldText(false);
		}
		titleTextView.setText(Html.fromHtml(titleString));
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceSecondFontSize());
		
		TextView dateTextView = (TextView) layout.findViewById(R.id.MailSendDate);
		dateTextView.setText(mail.getDateString());
		
		layout.setTag(mail);
		
		if (HomeActivity.application.isNightTheme()) {
			authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}

		return layout;
	}

	@Override
	public int getCount() {
		return mailList.size();
	}

	@Override
	public Object getItem(int position) {
		return mailList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
