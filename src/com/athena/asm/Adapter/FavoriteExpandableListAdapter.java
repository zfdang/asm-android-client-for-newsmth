package com.athena.asm.Adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.data.Board;

public class FavoriteExpandableListAdapter extends BaseExpandableListAdapter {
	private Activity activity;
	private List<String> group;
	private List<List<Board>> child;

	public FavoriteExpandableListAdapter(Activity activity, List<String> group,
			List<List<Board>> child) {
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
		Board board = child.get(groupPosition).get(childPosition);
		return getGenericView(board);
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
		View layout = inflater.inflate(R.layout.favorite_list_section_header,
				null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.SectionName);
		boardNameTextView.setText(s);
		return layout;
	}

	private View getGenericView(Board board) {
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.favorite_list_item, null);
		TextView categoryNameTextView = (TextView) layout
				.findViewById(R.id.CategoryName);
		categoryNameTextView.setText(board.getCategoryName());
		TextView moderatorTextView = (TextView) layout
				.findViewById(R.id.ModeratorID);
		moderatorTextView.setText(board.getModerator());
		TextView boardTextView = (TextView) layout.findViewById(R.id.BoardName);
		boardTextView.setText(board.toString());
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