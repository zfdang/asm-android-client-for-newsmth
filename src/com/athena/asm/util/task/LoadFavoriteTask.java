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
		pdialog = new ProgressDialog(context);
		pdialog.setMessage("加载收藏中...");
		pdialog.show();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		try {
			FileInputStream fis = context.openFileInput("FavList");
			ObjectInputStream ois = new ObjectInputStream(fis);
			realFavList = (ArrayList<Board>) ois.readObject();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		realFavList = m_viewModel.updateFavList(realFavList);
		
		try {
			FileOutputStream fos = context.openFileOutput("FavList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(realFavList);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		m_viewModel.notifyFavListChanged();
	}
}
