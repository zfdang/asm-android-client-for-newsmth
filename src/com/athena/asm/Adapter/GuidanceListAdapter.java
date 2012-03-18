package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.Subject;

public class GuidanceListAdapter extends BaseAdapter {

	private HomeActivity activity;
	private List<String> sections;
	private List<List<Subject>> subjects;
	private int itemType = 0;
	private int dataIndex = 0;

	public GuidanceListAdapter(HomeActivity activity, int type, int index,
			List<String> sectionList, List<List<Subject>> subjectList) {
		this.activity = activity;
		this.itemType = type;
		this.dataIndex = index;
		this.sections = sectionList;
		this.subjects = subjectList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		if (itemType == 0) {
			layout = activity.inflater.inflate(
					R.layout.guidance_list_section_header, null);
			TextView boardNameTextView = (TextView) layout
					.findViewById(R.id.SectionName);
			boardNameTextView.setText(sections.get(position));
			boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceFontSize());
			if (HomeActivity.application.isNightTheme()) {
				boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			}
		} else {
			Subject subject = subjects.get(dataIndex).get(position);
			layout = activity.inflater.inflate(R.layout.guidance_list_item,
					null);
			TextView boardNameTextView = (TextView) layout
					.findViewById(R.id.BoardName);
			boardNameTextView.setText(subject.getBoardChsName());
			TextView authorTextView = (TextView) layout
					.findViewById(R.id.AuthorID);
			authorTextView.setText(subject.getAuthor());
			TextView titleTextView = (TextView) layout
					.findViewById(R.id.SubjectTitle);
			titleTextView.setText(subject.getTitle());
			titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceSecondFontSize());
			layout.setTag(subject);
			
			if (HomeActivity.application.isNightTheme()) {
				boardNameTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
				titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
				authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			}
		}
		return layout;
	}

	@Override
	public int getCount() {
		if (itemType == 0) {
			return sections.size();
		} else {
			return subjects.get(dataIndex).size();
		}
	}

	@Override
	public Object getItem(int position) {
		if (itemType == 0) {
			return sections.get(position);
		} else {
			return subjects.get(dataIndex).get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
