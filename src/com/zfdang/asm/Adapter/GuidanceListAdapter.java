package com.zfdang.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.zfdang.asm.R;
import com.zfdang.asm.aSMApplication;
import com.zfdang.asm.data.Subject;

public class GuidanceListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater m_inflater;
	private List<String> m_sections;
	private List<List<Subject>> m_subjects;

	static class ChildViewHolder{
		TextView boardNameTextView;
		TextView authorTextView;
		TextView titleTextView;
	}

	static class GroupViewHolder{
		TextView sectionNameTextView;
	}

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
		ChildViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.guidance_list_item, null);

			holder = new ChildViewHolder();
			holder.boardNameTextView = (TextView) convertView.findViewById(R.id.BoardName);
			holder.authorTextView = (TextView) convertView.findViewById(R.id.AuthorID);
			holder.titleTextView = (TextView) convertView.findViewById(R.id.SubjectTitle);
			convertView.setTag(R.id.tag_first, holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag(R.id.tag_first);
		}
		holder.boardNameTextView.setText(subject.getBoardChsName());
		holder.authorTextView.setText(subject.getAuthor());
		holder.titleTextView.setText(subject.getTitle());
		holder.titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceSecondFontSize());

		convertView.setTag(R.id.tag_second, subject);

		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			holder.boardNameTextView.setTextColor(convertView.getResources().getColor(R.color.blue_text_night));
			holder.titleTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
			holder.authorTextView.setTextColor(convertView.getResources().getColor(R.color.blue_text_night));
		}
		return convertView;
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
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewHolder holder;
		if (convertView == null){
			convertView = m_inflater.inflate(R.layout.guidance_list_section_header, null);

			holder = new GroupViewHolder();
			holder.sectionNameTextView = (TextView) convertView.findViewById(R.id.SectionName);
			convertView.setTag(holder);
		} else {
			holder = (GroupViewHolder) convertView.getTag();
		}

		holder.sectionNameTextView.setText(m_sections.get(groupPosition));
		holder.sectionNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				aSMApplication.getCurrentApplication().getGuidanceFontSize());
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			holder.sectionNameTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
		}
		return convertView;
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
