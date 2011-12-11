package com.athena.asm.util.task;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Board;

public class LoadFavoriteTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;

	public LoadFavoriteTask(HomeActivity activity) {
		this.homeActivity = activity;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载收藏中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		homeActivity.favList = new ArrayList<Board>();
		Board board = new Board();
		board.setDirectory(true);
		board.setDirectoryName("最近访问版面");
		board.setCategoryName("目录");
		//board.setChildBoards(new ArrayList<Board>(application.getRecentBoards()));
		homeActivity.favList.add(board);
		homeActivity.smthSupport.getFavorite("0", homeActivity.favList,0);
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.reloadFavorite(homeActivity.favList, 20);
	}
}
