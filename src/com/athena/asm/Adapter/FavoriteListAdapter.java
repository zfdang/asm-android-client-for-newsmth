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

	static class ChildViewHolder{
		TextView categoryNameTextView;
		TextView moderatorIDTextView;
		TextView boardNameTextView;
	}

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

		ChildViewHolder holder;
		if (convertView == null) {
			convertView = m_inflater.inflate(R.layout.favorite_list_item, null);

			holder = new ChildViewHolder();
			holder.categoryNameTextView = (TextView) convertView.findViewById(R.id.CategoryName);
			holder.moderatorIDTextView = (TextView) convertView.findViewById(R.id.ModeratorID);
			holder.boardNameTextView = (TextView) convertView.findViewById(R.id.BoardName);
			convertView.setTag(R.id.tag_first, holder);
		} else {
			holder = (ChildViewHolder) convertView.getTag(R.id.tag_first);
		}
		holder.categoryNameTextView.setText(board.getCategoryName());
		holder.moderatorIDTextView.setText(board.getModerator());
		holder.boardNameTextView.setText("[" + board.getEngName() + "]"
				+ board.getChsName());
		holder.boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceSecondFontSize());

		convertView.setTag(R.id.tag_second, board);

		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			holder.categoryNameTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
			holder.moderatorIDTextView.setTextColor(convertView.getResources().getColor(R.color.blue_text_night));
			holder.boardNameTextView.setTextColor(convertView.getResources().getColor(R.color.status_text_night));
		}
		return convertView;
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
