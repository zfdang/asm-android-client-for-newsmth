package com.athena.asm.Adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.data.Subject;

public class GuidanceExpandableListAdapter extends BaseExpandableListAdapter {
	private Activity activity;
	private List<String> group;
	private List<List<Subject>> child;

	public GuidanceExpandableListAdapter(Activity activity, List<String> group,
			List<List<Subject>> child) {
		this.activity = activity;
		this.group = group;
		this.child = child;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return child.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return child.get(groupPosition).size();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		Subject subject = child.get(groupPosition).get(childPosition);
		return getGenericView(subject);
	}

	@Override
	public Object getGroup(int groupPosition) {
		return group.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return group.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String s = group.get(groupPosition);
		return getGroupView(s);
	}

	private View getGroupView(String s) {
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.guidance_list_section_header,
				null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.SectionName);
		boardNameTextView.setText(s);
		return layout;
	}

	private View getGenericView(Subject subject) {
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.guidance_list_item, null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.BoardName);
		boardNameTextView.setText(subject.getBoardChsName());
		TextView authorTextView = (TextView) layout.findViewById(R.id.AuthorID);
		authorTextView.setText(subject.getAuthor());
		TextView titleTextView = (TextView) layout
				.findViewById(R.id.SubjectTitle);
		titleTextView.setText(subject.getTitle());
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}