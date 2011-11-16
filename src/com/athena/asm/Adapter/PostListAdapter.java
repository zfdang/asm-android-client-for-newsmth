package com.athena.asm.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.PostListActivity;
import com.athena.asm.R;
import com.athena.asm.data.Attachment;
import com.athena.asm.data.Post;
import com.athena.asm.util.StringUtility;

public class PostListAdapter extends BaseAdapter {

	private PostListActivity activity;
	private LayoutInflater inflater;
	private List<Post> postList;

	public PostListAdapter(PostListActivity activity, LayoutInflater inflater,
			List<Post> postList) {
		this.activity = activity;
		this.inflater = inflater;
		this.postList = postList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		Post post = postList.get(position);

		layout = inflater.inflate(R.layout.post_list_item, null);
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		authorTextView.setText(post.getAuthor());
		TextView titleTextView = (TextView) layout.findViewById(R.id.PostTitle);
		titleTextView.setText(post.getTitle());
		TextView contentTextView = (TextView) layout
				.findViewById(R.id.PostContent);
		contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
		String contentString = post.getContent();
		ArrayList<Attachment> attachments = post.getAttachFiles();
		for (int i = 0; i < attachments.size(); i++) {
			contentString += "<a href='" + attachments.get(i).getAttachUrl()
					+ "'>" + attachments.get(i).getName() + "</a><br/>";
		}
		contentTextView.setText(Html.fromHtml(contentString));
		TextView dateTextView = (TextView) layout.findViewById(R.id.PostDate);
		dateTextView.setText(post.getDate().toLocaleString());
		layout.setTag(post);
		
		OnLongClickListener listener = new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (activity.smthSupport.getLoginStatus()) {
					RelativeLayout relativeLayout = null;
					if (v.getId() == R.id.PostContent) {
						relativeLayout = (RelativeLayout) v.getParent();
					}
					else {
						relativeLayout = (RelativeLayout) v;
					}
					final String authorID = (String) ((TextView)relativeLayout.findViewById(R.id.AuthorID)).getText();
					final Post post = (Post) v.getTag();
					final String[] items = { activity.getString(R.string.post_reply_post),
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
										intent.putExtra(
												StringUtility.URL,
												"http://www.newsmth.net/bbspst.php?board="
														+ post.getBoard()
														+ "&reid="
														+ post.getSubjectID());
										activity.startActivity(intent);
										break;
									case 1:
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

		return layout;
	}

	@Override
	public int getCount() {
		return postList.size();
	}

	@Override
	public Object getItem(int position) {
		return postList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
