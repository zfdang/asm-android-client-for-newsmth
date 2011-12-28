package com.athena.asm.Adapter;

import java.util.ArrayList;
import java.util.List;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.athena.asm.PostListActivity;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Attachment;
import com.athena.asm.data.Post;

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
		aSMApplication application = (aSMApplication) activity.getApplication();
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		TextView titleTextView = (TextView) layout.findViewById(R.id.PostTitle);
		if (post.getAuthor() == null) {
			titleTextView.setText("错误的文章号,原文可能已经被删除");
			return layout;
		}
		authorTextView.setText(post.getAuthor());

		titleTextView.setText(post.getTitle());
		TextView contentTextView = (TextView) layout
				.findViewById(R.id.PostContent);
		// contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
		contentTextView.setText(Html.fromHtml(post.getContent()));
		contentTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				application.getPostFontSize());

		TextView attachTextView = (TextView) layout
				.findViewById(R.id.PostAttach);
		attachTextView.setMovementMethod(LinkMovementMethod.getInstance());
		ArrayList<Attachment> attachments = post.getAttachFiles();
		String contentString = "";
		for (int i = 0; i < attachments.size(); i++) {
			contentString += "<a href='" + attachments.get(i).getAttachUrl()
					+ "'>" + attachments.get(i).getName() + "</a><br/><br/>";
		}
		attachTextView.setText(Html.fromHtml(contentString));

		TextView dateTextView = (TextView) layout.findViewById(R.id.PostDate);
		dateTextView.setText(post.getDate().toLocaleString());
		layout.setTag(post);

		contentTextView.setOnLongClickListener(activity);
		layout.setOnLongClickListener(activity);

		contentTextView.setOnTouchListener(activity);
		layout.setOnTouchListener(activity);

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
