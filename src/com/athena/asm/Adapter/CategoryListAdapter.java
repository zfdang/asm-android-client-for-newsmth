package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.PostListAdapter.ViewHolder;
import com.athena.asm.data.Board;
import com.athena.asm.data.Post;

public class CategoryListAdapter extends BaseAdapter {

	private LayoutInflater m_inflater;
	private List<Board> m_boards;
	
	public class ViewHolder {
		public TextView categoryNameTextView;
		public TextView moderatorIDTextView;
		public TextView boardNameTextView;
		public TextView attachTextView;
		public LinearLayout imageLayout;
		public TextView dateTextView;
		public Board board;
	}

	public CategoryListAdapter(LayoutInflater inflater, List<Board> boardList) {
		this.m_inflater = inflater;
		this.m_boards = boardList;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Board board = m_boards.get(position);
		View layout = null;
		ViewHolder holder;
		if (convertView != null) {
			layout = convertView;
			holder = (ViewHolder)layout.getTag();
		} else {
			layout = m_inflater.inflate(R.layout.category_list_item, null);
			holder = new ViewHolder();
			holder.categoryNameTextView = (TextView) layout.findViewById(R.id.CategoryName);
			holder.moderatorIDTextView = (TextView) layout.findViewById(R.id.ModeratorID);
			holder.boardNameTextView = (TextView) layout.findViewById(R.id.BoardName);
			
			holder.boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, aSMApplication.getCurrentApplication().getGuidanceSecondFontSize());

			if (aSMApplication.getCurrentApplication().isNightTheme()) {
				holder.categoryNameTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
				holder.moderatorIDTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
				holder.boardNameTextView.setTextColor(layout.getResources().getColor(
						R.color.status_text_night));
			}
			layout.setTag(holder);
		}
		
		holder.board = board;
		holder.categoryNameTextView.setText(board.getCategoryName());
		holder.moderatorIDTextView.setText(board.getModerator());
		holder.boardNameTextView.setText("[" + board.getEngName() + "]"
				+ board.getChsName());
		
		return layout;
	}

	@Override
	public int getCount() {
		return m_boards.size();
	}

	@Override
	public Object getItem(int position) {
		return m_boards.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
