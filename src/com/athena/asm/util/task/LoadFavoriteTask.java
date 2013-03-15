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
import android.util.Log;

import com.athena.asm.data.Board;
import com.athena.asm.viewmodel.HomeViewModel;

public class LoadFavoriteTask extends AsyncTask<String, Integer, String> {
	private Context context;
	private ArrayList<Board> realFavList;
	
	private HomeViewModel m_viewModel;

	public LoadFavoriteTask(Context context, HomeViewModel viewModel) {
		this.context = context;
		this.realFavList = null;
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		m_viewModel.m_isLoadingInProgress = true;
		pdialog = new ProgressDialog(context);
		pdialog.setMessage("加载收藏中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		// 1. read fav from file first
		try {
			FileInputStream fis = context.openFileInput("FavList");
			ObjectInputStream ois = new ObjectInputStream(fis);
			realFavList = (ArrayList<Board>) ois.readObject();
			fis.close();
			// Log.d("LoadFavoriteTask", "succeed to load favlist from file");
		} catch (Exception e) {
			// Log.d("LoadFavoriteTask", "fail to load favlist from file");
			e.printStackTrace();
		}
		
		// 2. if fail to read from file, read from web
		boolean isLoadFromWeb = false;
		if (realFavList == null) {
			realFavList = m_viewModel.updateFavList(realFavList);
			// Log.d("LoadFavoriteTask", "load favlist from web");
			isLoadFromWeb = true;
		}
		else
		{
			m_viewModel.setFavList(realFavList);
		}

		// 3. save to file if load from web
		if (isLoadFromWeb) {
			try {
				FileOutputStream fos = context.openFileOutput("FavList", Context.MODE_PRIVATE);
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(realFavList);
				fos.close();
				// Log.d("LoadFavoriteTask", "succeed to save favlist to file");
			} catch (IOException e) {
				// Log.d("LoadFavoriteTask", "fail to save favlist to file");
				e.printStackTrace();
			}
		}
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifyFavListChanged();
		m_viewModel.m_isLoadingInProgress = false;
	}
}
