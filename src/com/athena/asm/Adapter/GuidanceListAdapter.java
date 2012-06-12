package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.Subject;

public class GuidanceListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater m_inflater;
	private List<String> m_sections;
	private List<List<Subject>> m_subjects;

	public GuidanceListAdapter(LayoutInflater inflater,
			List<String> sectionList, List<List<Subject>> subjectList) {
		this.m_inflater = inflater;
		this.m_sections = sectionList;
		this.m_subjects = subjectList;
	}
	
	public Object getChild(int groupPosition, int childPosition) {
        return m_subjects.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
		Subject subject = m_subjects.get(groupPosition).get(childPosition);
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		} else {
			layout = m_inflater.inflate(R.layout.guidance_list_item, null);
		}
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.BoardName);
		boardNameTextView.setText(subject.getBoardChsName());
		TextView authorTextView = (TextView) layout
				.findViewById(R.id.AuthorID);
		authorTextView.setText(subject.getAuthor());
		TextView titleTextView = (TextView) layout
				.findViewById(R.id.SubjectTitle);
		titleTextView.setText(subject.getTitle());
		titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.m_application.getGuidanceSecondFontSize());
		layout.setTag(subject);
		
		if (HomeActivity.m_application.isNightTheme()) {
			boardNameTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			titleTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			authorTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
		}
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return m_subjects.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return m_sections.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return m_sections.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View layout = m_inflater.inflate(
				R.layout.guidance_list_section_header, null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.SectionName);
		boardNameTextView.setText(m_sections.get(groupPosition));
		boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.m_application.getGuidanceFontSize());
		if (HomeActivity.m_application.isNightTheme()) {
			boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
