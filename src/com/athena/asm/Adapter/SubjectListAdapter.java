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

	static class ViewHolder {
		public TextView authorTextView;
		public TextView titleTextView;
		public TextView dateTextView;
	}

	public SubjectListAdapter(LayoutInflater inflater, List<Subject> subjectList) {
		this.m_inflater = inflater;
		this.m_subjectList = subjectList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.subject_list_item, null);

			// save all elements in ViewHolder
			holder = new ViewHolder();
			holder.authorTextView = (TextView) convertView.findViewById(R.id.AuthorID);
			holder.titleTextView = (TextView) convertView.findViewById(R.id.SubjectTitle);
			holder.dateTextView = (TextView) convertView.findViewById(R.id.SubjectPostDate);
			convertView.setTag(R.id.tag_first, holder);
		}
		else {
			// retrieve ViewHolder
			holder = (ViewHolder) convertView.getTag(R.id.tag_first);
		}
		
		// set subject
		Subject subject = m_subjectList.get(position);
		holder.authorTextView.setText(subject.getAuthor());

		// set title
		String titleString = subject.getTitle();
		if (subject.getType().toLowerCase().contains(Subject.TYPE_BOTTOM)) {
			boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;
			if (isLight) {
				titleString = "<font color='#f00000'>" + titleString + "</font>";
			} else {
				titleString = "<font color='#e9f7fe'>" + titleString + "</font>";
			}
			
		}
		holder.titleTextView.setText(Html.fromHtml(titleString));
		holder.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getSubjectFontSize());
		
		holder.dateTextView.setText(subject.getDateString());
		
		convertView.setTag(R.id.tag_second, subject);
		
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			holder.titleTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
			holder.authorTextView.setTextColor(convertView.getResources().getColor(R.color.blue_text_night));
		}

		return convertView;
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
