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
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadCategoryTask extends AsyncTask<String, Integer, String> {
	private HomeActivity homeActivity;
	
	private HomeViewModel m_viewModel;

	public LoadCategoryTask(HomeActivity activity, HomeViewModel viewModel) {
		this.homeActivity = activity;
		m_viewModel = viewModel;
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
			m_viewModel.setCategoryList((List<Board>) ois.readObject());
			Log.d("com.athena.asm", "loading from file");
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (m_viewModel.categoryList() == null) {
			m_viewModel.updateCategoryList();
		}
		
		try {
			FileOutputStream fos = homeActivity.openFileOutput("CategoryList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(m_viewModel.categoryList());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		m_viewModel.updateBoardInfo();
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		homeActivity.reloadCategory(m_viewModel.categoryList(), 30);
	}
}
