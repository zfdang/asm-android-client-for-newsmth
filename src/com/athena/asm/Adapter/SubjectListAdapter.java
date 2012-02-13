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
import com.athena.asm.data.Subject;

public class SubjectListAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private List<Subject> subjectList;

	public SubjectListAdapter(LayoutInflater inflater, List<Subject> subjectList) {
		this.inflater = inflater;
		this.subjectList = subjectList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		}
		else {
			layout = inflater.inflate(R.layout.subject_list_item, null);
		}
		
		Subject subject = subjectList.get(subjectList.size() - position - 1);
		
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		authorTextView.setText(subject.getAuthor());
		TextView titleTextView = (TextView) layout.findViewById(R.id.SubjectTitle);
		String titleString = subject.getTitle();
		if (subject.getType().toLowerCase().contains(Subject.TYPE_BOTTOM)) {
			titleString = "<font color='red'>" + titleString + "</font>";
		}
		titleTextView.setText(Html.fromHtml(titleString));
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getSubjectFontSize());
		TextView dateTextView = (TextView) layout.findViewById(R.id.SubjectPostDate);
		dateTextView.setText(subject.getDate().toLocaleString());
		
		layout.setTag(subject);
//		layout.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent();
//				Bundle bundle = new Bundle();
//				bundle.putSerializable(StringUtility.SUBJECT, (Subject)v.getTag());
//				bundle.putInt(StringUtility.BOARD_TYPE, activity.boardType);
//				intent.putExtras(bundle);
//				intent.setClassName("com.athena.asm", "com.athena.asm.PostListActivity");
//				//activity.startActivity(intent);
//				activity.startActivityForResult(intent, 0);
//			}
//		});

		return layout;
	}

	@Override
	public int getCount() {
		return subjectList.size();
	}

	@Override
	public Object getItem(int position) {
		return subjectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
