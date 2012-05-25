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
import com.athena.asm.data.Board;

public class FavoriteListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater inflater;
	private List<String> directories;
	private List<List<Board>> boards;

	public FavoriteListAdapter(LayoutInflater inflater, 
			List<String> directoryList, List<List<Board>> boardList) {
		this.inflater = inflater;
		this.directories = directoryList;
		this.boards = boardList;
	}

	public Object getChild(int groupPosition, int childPosition) {
        return boards.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
		Board board = boards.get(groupPosition).get(childPosition);
		View layout = inflater.inflate(R.layout.favorite_list_item,
				null);
		TextView categoryNameTextView = (TextView) layout
				.findViewById(R.id.CategoryName);
		categoryNameTextView.setText(board.getCategoryName());
		TextView moderatorIDTextView = (TextView) layout
				.findViewById(R.id.ModeratorID);
		moderatorIDTextView.setText(board.getModerator());
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.BoardName);
		boardNameTextView.setText("[" + board.getEngName() + "]"
				+ board.getChsName());
		boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.m_application.getGuidanceSecondFontSize());
		layout.setTag(board);
		if (HomeActivity.m_application.isNightTheme()) {
			categoryNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			moderatorIDTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return boards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return directories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return directories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View layout = inflater.inflate(
				R.layout.favorite_list_section_header, null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.BoardName);
		boardNameTextView.setText(directories.get(groupPosition));
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
