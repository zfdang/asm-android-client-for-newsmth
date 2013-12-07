package com.athena.asm.Adapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.ClipboardManager;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.R;
import com.athena.asm.ReadMailActivity;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Mail;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.vt100.Vt100TerminalModel;

@SuppressWarnings("deprecation")
public class ReadMailAdapter extends BaseAdapter {
	private ReadMailActivity activity;
	private LayoutInflater inflater;
	private Mail m_currentMail;

	public ReadMailAdapter(ReadMailActivity activity, Mail currentMail, LayoutInflater inflater) {
		this.activity = activity;
		this.inflater = inflater;
		m_currentMail = currentMail;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		Mail mail = m_currentMail;

		layout = inflater.inflate(R.layout.post_list_item, null);
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		authorTextView.setText(mail.getSenderID());
		TextView titleTextView = (TextView) layout.findViewById(R.id.PostTitle);
		titleTextView.setText(mail.getTitle());
		TextView contentTextView = (TextView) layout
				.findViewById(R.id.PostContent);
		
		Vt100TerminalModel.handleContent(mail.getContent(), contentTextView);		
//		contentTextView.setText(Html.fromHtml(mail.getContent()));				
		contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getPostFontSize());
		
		TextView attachTextView = (TextView) layout.findViewById(R.id.PostAttach);
		attachTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView dateTextView = (TextView) layout.findViewById(R.id.PostDate);
		dateTextView.setText(StringUtility.getFormattedString(mail.getDate()));
		layout.setTag(mail);
		
		OnLongClickListener listener = new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (activity.m_smthSupport.getLoginStatus()) {
					RelativeLayout relativeLayout = null;
					if (v.getId() == R.id.PostContent) {
						relativeLayout = (RelativeLayout) v.getParent();
					}
					else {
						relativeLayout = (RelativeLayout) v;
					}
					final String authorID = (String) ((TextView)relativeLayout.findViewById(R.id.AuthorID)).getText();
					final Mail mail = (Mail) relativeLayout.getTag();
					final String[] items = { activity.getString(R.string.mail_reply),
							activity.getString(R.string.post_query_author),
							activity.getString(R.string.post_copy_author)};// ,
					AlertDialog.Builder builder = new AlertDialog.Builder(
							activity);
					builder.setTitle(R.string.post_alert_title);
					builder.setItems(items,
							new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
										int item) {
									switch (item) {
									case 0:
										Intent intent = new Intent();
										intent.setClassName("com.athena.asm",
												"com.athena.asm.WritePostActivity");
										try {
											intent.putExtra(
													StringUtility.URL,
													"http://www.newsmth.net/bbspstmail.php?dir="
															+ mail.getBoxDirString()
															+ "&userid=" + mail.getSenderID()
															+ "&num=" + mail.getNumber()
															+ "&file=" + mail.getValueString()
															+ "&title=" + URLEncoder.encode(mail.getTitle(), "GBK"));
											intent.putExtra(StringUtility.IS_REPLY, true);
										} catch (UnsupportedEncodingException e) {
											e.printStackTrace();
										}
										intent.putExtra(StringUtility.WRITE_TYPE, 1);
										activity.startActivity(intent);
										break;
									case 1:
										intent = new Intent();
										intent.setClassName("com.athena.asm",
												"com.athena.asm.ViewProfileActivity");
										intent.putExtra(StringUtility.USERID, authorID);
										activity.startActivity(intent);
										break;
									case 2:
										ClipboardManager clip = (ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
										clip.setText(authorID);
										Toast.makeText(activity.getApplicationContext(), "ID ： " + authorID + "已复制到剪贴板",
												Toast.LENGTH_SHORT).show();
									default:
										break;
									}
									dialog.dismiss();
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
				return false;
			}
		};

		contentTextView.setOnLongClickListener(listener);
		layout.setOnLongClickListener(listener);
		
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			contentTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			attachTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}

		return layout;
	}

	@Override
	public int getCount() {
		return 1;
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
