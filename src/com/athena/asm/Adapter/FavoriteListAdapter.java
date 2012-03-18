package com.athena.asm.Adapter;

import java.util.List;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.Board;

public class FavoriteListAdapter extends BaseAdapter {

	private HomeActivity activity;
	private List<Board> boards;
	public int step;

	public FavoriteListAdapter(HomeActivity activity, List<Board> boardList,
			int viewStep) {
		this.activity = activity;
		this.boards = boardList;
		this.step = viewStep;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View layout = null;
		Board board = boards.get(position);

		if (board.isDirectory()) {
			layout = activity.inflater.inflate(
					R.layout.favorite_list_section_header, null);
			TextView boardNameTextView = (TextView) layout
					.findViewById(R.id.BoardName);
			boardNameTextView.setText(board.getDirectoryName());
			boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceFontSize());
			layout.setTag(board);
			if (HomeActivity.application.isNightTheme()) {
				boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			}
		} else {
			layout = activity.inflater.inflate(R.layout.favorite_list_item,
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
			boardNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, HomeActivity.application.getGuidanceSecondFontSize());
			layout.setTag(board);
			if (HomeActivity.application.isNightTheme()) {
				categoryNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
				moderatorIDTextView.setTextColor(layout.getResources().getColor(R.color.blue_text_night));
				boardNameTextView.setTextColor(layout.getResources().getColor(R.color.status_text_night));
			}
		}
		return layout;
	}

	@Override
	public int getCount() {
		return boards.size();
	}

	@Override
	public Object getItem(int position) {
		return boards.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
