package com.athena.asm.util.task;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.athena.asm.HomeActivity;
import com.athena.asm.data.Board;
import com.athena.asm.viewmodel.HomeViewModel;

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
		try {
			FileInputStream fis = m_context.openFileInput("CategoryList");
			ObjectInputStream ois = new ObjectInputStream(fis);
			m_viewModel.setCategoryList((List<Board>) ois.readObject());
			Log.d("com.athena.asm", "loading from file");
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (m_viewModel.getCategoryList() == null) {
			m_viewModel.updateCategoryList();
		}
		
		try {
			FileOutputStream fos = m_context.openFileOutput("CategoryList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(m_viewModel.getCategoryList());
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
		m_viewModel.notifyCategoryChanged();
	}
}
