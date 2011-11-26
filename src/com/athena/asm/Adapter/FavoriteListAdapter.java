package com.athena.asm.Adapter;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.data.Board;
import com.athena.asm.util.StringUtility;

public class FavoriteListAdapter extends BaseAdapter {

	private HomeActivity activity;
	private List<Board> boards;
	private int step;

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
			layout.setTag(board);
			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					activity.reloadFavorite(
							((Board) v.getTag()).getChildBoards(), ++step);
				}
			});
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
			layout.setTag(board);

			layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable(StringUtility.BOARD,
							(Board) v.getTag());
					intent.putExtras(bundle);
					intent.setClassName("com.athena.asm",
							"com.athena.asm.SubjectListActivity");
					activity.startActivity(intent);
				}
			});
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
