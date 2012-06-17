package com.athena.asm.Adapter;

import java.util.List;

import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Subject;

public class SubjectListAdapter extends BaseAdapter {

	private LayoutInflater m_inflater;
	private List<Subject> m_subjectList;

	public SubjectListAdapter(LayoutInflater inflater, List<Subject> subjectList) {
		this.m_inflater = inflater;
		this.m_subjectList = subjectList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		}
		else {
			layout = m_inflater.inflate(R.layout.subject_list_item, null);
		}
		
		Subject subject = m_subjectList.get(position);
		
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		authorTextView.setText(subject.getAuthor());
		TextView titleTextView = (TextView) layout.findViewById(R.id.SubjectTitle);
		String titleString = subject.getTitle();
		if (subject.getType().toLowerCase().contains(Subject.TYPE_BOTTOM)) {
			boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;
			if (isLight) {
				titleString = "<font color='#f00000'>" + titleString + "</font>";
			} else {
				titleString = "<font color='#e9f7fe'>" + titleString + "</font>";
			}
			
		}
		titleTextView.setText(Html.fromHtml(titleString));
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getSubjectFontSize());
		
		TextView dateTextView = (TextView) layout.findViewById(R.id.SubjectPostDate);
		dateTextView.setText(subject.getDateString());
		
		layout.setTag(subject);
		
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
		}

		return layout;
	}

	@Override
	public int getCount() {
		return m_subjectList.size();
	}

	@Override
	public Object getItem(int position) {
		return m_subjectList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
