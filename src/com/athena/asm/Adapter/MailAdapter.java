package com.athena.asm.Adapter;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.MailBox;

public class MailAdapter extends BaseAdapter {

	private HomeActivity activity;
	private MailBox mailBox;

	public MailAdapter(HomeActivity activity, MailBox mailBox) {
		this.activity = activity;
		this.mailBox = mailBox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		layout = activity.inflater.inflate(R.layout.mail_list_section_header,
				null);
		TextView boxNameTextView = (TextView) layout.findViewById(R.id.BoxName);
		TextView numberTextView = (TextView) layout
				.findViewById(R.id.mailNumber);
		boxNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceFontSize());
		numberTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceFontSize());
		switch (position) {
		case 0:
			boxNameTextView.setText(R.string.mail_inbox);
			numberTextView.setText(mailBox.getInboxNumber()+"封 ");
			break;
		case 1:
			boxNameTextView.setText(R.string.mail_outbox);
			numberTextView.setText(mailBox.getOutboxNumber()+"封 ");
			break;
		case 2:
			boxNameTextView.setText(R.string.mail_trash);
			numberTextView.setText(mailBox.getTrashboxNumber()+"封 ");
			break;
		case 3:
			boxNameTextView.setText(R.string.mail_write_mail);
			numberTextView.setText("");
			break;
		case 4:
			boxNameTextView.setText(R.string.mail_clear_trash);
			numberTextView.setText("");
			break;
		default:
			break;
		}
		
		if (HomeActivity.application.isNightTheme()) {
			boxNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			numberTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}

		return layout;
	}

	@Override
	public int getCount() {
		return 4;
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
