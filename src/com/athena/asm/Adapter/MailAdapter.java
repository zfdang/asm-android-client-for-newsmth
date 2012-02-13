package com.athena.asm.Adapter;

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
		
//		if (position == 0 || position == 1 || position == 2) {
//			final int boxType = position;
//			layout.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent();
//					intent.putExtra(StringUtility.MAIL_BOX_TYPE, boxType);
//					intent.setClassName("com.athena.asm",
//							"com.athena.asm.MailListActivity");
//					activity.startActivity(intent);
//				}
//			});
//		}
//		else if (position == 3) {
//			layout.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent();
//					intent.setClassName("com.athena.asm",
//							"com.athena.asm.WritePostActivity");
//					intent.putExtra(
//							StringUtility.URL,
//							"http://www.newsmth.net/bbspstmail.php");
//					intent.putExtra(StringUtility.WRITE_TYPE, 1);
//					intent.putExtra(StringUtility.IS_REPLY, false);
//					activity.startActivity(intent);
//				}
//			});
//		}

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
