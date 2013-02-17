package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Board;

public class FavoriteListAdapter extends BaseExpandableListAdapter {

	private LayoutInflater m_inflater;
	private List<String> m_directories;
	private List<List<Board>> m_boards;

	public FavoriteListAdapter(LayoutInflater inflater, 
			List<String> directoryList, List<List<Board>> boardList) {
		this.m_inflater = inflater;
		this.m_directories = directoryList;
		this.m_boards = boardList;
	}

	public Object getChild(int groupPosition, int childPosition) {
        return m_boards.get(groupPosition).get(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
		Board board = m_boards.get(groupPosition).get(childPosition);
		View layout = null;
		if (convertView != null) {
			layout = convertView;
		} else {
			layout = m_inflater.inflate(R.layout.favorite_list_item, null);
		}
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
		boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceSecondFontSize());
		layout.setTag(board);
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			categoryNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			moderatorIDTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
			boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
		}
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return m_boards.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return m_directories.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return m_directories.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View layout = m_inflater.inflate(
				R.layout.favorite_list_section_header, null);
		TextView boardNameTextView = (TextView) layout
				.findViewById(R.id.BoardName);
		boardNameTextView.setText(m_directories.get(groupPosition));
		boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceFontSize());
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
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
