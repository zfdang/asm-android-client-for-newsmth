package com.zfdang.asm.util.task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.zfdang.asm.data.Board;
import com.zfdang.asm.data.BoardNameComparator;
import com.zfdang.asm.viewmodel.HomeViewModel;

public class LoadCategoryTask extends AsyncTask<String, Integer, String> {
	private Context m_context;

	private HomeViewModel m_viewModel;

	public LoadCategoryTask(Context context, HomeViewModel viewModel) {
		this.m_context = context;
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(m_context);
		pdialog.setMessage("加载分类讨论区中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		// refactor logics to load categories:
		// 1. check saved categories file
		// 1.1 if it exists, load categories from it
		// 1.2 if it does not exist, load categories from web, and save it to file

		// 1.1 read categories from file
		try {
			FileInputStream is = m_context.openFileInput("CategoryList");
			ObjectInputStream ois = new ObjectInputStream(is);
			m_viewModel.setCategoryList((ArrayList<Board>) ois.readObject());
			is.close();
			Log.d("LoadCategoryTask", "succeed to load categories from file");
		} catch (Exception e) {
			Log.d("LoadCategoryTask", "failed to load categories from file");
			// e.printStackTrace();
		}

		if (m_viewModel.getCategoryList() == null) {
			Log.d("LoadCategoryTask", "load categories from web");
			// 1.2.1 read categories from web
			m_viewModel.updateCategoryList();
			ArrayList<Board> categoryList =  m_viewModel.getCategoryList();

			// 1.2.2 sort categoryList by board's English name
			Collections.sort(categoryList, new BoardNameComparator());
			// 1.2.3 remove duplicated board name in a sorted list
			// Log.d("Number of categories before dedup", Integer.toString(categoryList.size()));
			Iterator<Board> itr = categoryList.iterator();
			String previousID = null;
			while (itr.hasNext()) {
				Board current = (Board) itr.next();
				if(previousID == null)
				{
					// no previous board, for the first record
					previousID = current.getBoardID();
					continue;
				}
				if(current.getBoardID().equals(previousID))
				{
					// duplicated board, remove current board
					// Log.d("updateCategoryList", "remove duplicated board" + current.getEngName());
					itr.remove();
				}
				else
				{
					// valid board, save current board id
					previousID = current.getBoardID();
				}
			}
			// Log.d("Number of categories after dedup", Integer.toString(categoryList.size()));

			// 1.2.4 save categories to file
			try {
				FileOutputStream fos = m_context.openFileOutput("CategoryList",
						Context.MODE_PRIVATE);
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(categoryList);
				fos.close();
				Log.d("LoadCategoryTask", "save categories to file for future usage");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		m_viewModel.updateBoardInfo();

		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifyCategoryChanged();
	}
}
