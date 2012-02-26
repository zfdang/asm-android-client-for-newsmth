package com.athena.asm.util.task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Board;

public class LoadFavoriteTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	private ArrayList<Board> realFavList;

	public LoadFavoriteTask(HomeActivity activity) {
		this.homeActivity = activity;
		this.realFavList = null;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载收藏中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		try {
			FileInputStream fis = homeActivity.openFileInput("FavList");
			ObjectInputStream ois = new ObjectInputStream(fis);
			realFavList = (ArrayList<Board>) ois.readObject();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (realFavList == null) {
			realFavList = new ArrayList<Board>();
			homeActivity.smthSupport.getFavorite("0", realFavList,0);
		}
		
		homeActivity.favList = new ArrayList<Board>();
		Board board = new Board();
		board.setDirectory(true);
		board.setDirectoryName("最近访问版面");
		board.setCategoryName("目录");
		//board.setChildBoards(new ArrayList<Board>(application.getRecentBoards()));
		homeActivity.favList.add(board);
		homeActivity.favList.addAll(realFavList);
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		try {
			FileOutputStream fos = homeActivity.openFileOutput("FavList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(realFavList);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		homeActivity.reloadFavorite(homeActivity.favList, 20);
	}
}
