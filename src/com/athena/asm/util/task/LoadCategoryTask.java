package com.athena.asm.util.task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Board;

public class LoadCategoryTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;

	public LoadCategoryTask(HomeActivity activity) {
		this.homeActivity = activity;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(homeActivity);
		pdialog.setMessage("加载分类讨论区中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		try {
			FileInputStream fis = homeActivity.openFileInput("CategoryList");
			ObjectInputStream ois = new ObjectInputStream(fis);
			homeActivity.categoryList = (List<Board>) ois.readObject();
			Log.d("com.athena.asm", "loading from file");
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (homeActivity.categoryList == null) {
			homeActivity.categoryList = new ArrayList<Board>();
			homeActivity.smthSupport.getCategory("TOP",	homeActivity.categoryList, false);
		}
		pdialog.cancel();
		return null;
	}
	
	private void readBoadInfo(List<Board> boards) {
		for (Iterator<Board> iterator = boards.iterator(); iterator.hasNext();) {
			Board board = (Board) iterator.next();
			if (board != null && board.getEngName() != null) {
				if (!homeActivity.boardFullStrings.contains(board.getEngName())) {
					homeActivity.boardFullStrings.add(board.getEngName());
				}
				homeActivity.boardHashMap.put(board.getEngName().toLowerCase(), board);
			}
			readBoadInfo(board.getChildBoards());
		}
	}

	@Override
	protected void onPostExecute(String result) {

		try {
			FileOutputStream fos = homeActivity.openFileOutput("CategoryList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(homeActivity.categoryList);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		homeActivity.boardFullStrings = new ArrayList<String>();
		homeActivity.boardHashMap = new HashMap<String, Board>();
		readBoadInfo(homeActivity.categoryList);
		homeActivity.reloadCategory(homeActivity.categoryList, 30);
	}
}
