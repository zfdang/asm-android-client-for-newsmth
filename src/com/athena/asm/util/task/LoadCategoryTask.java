package com.athena.asm.util.task;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.data.Board;
import com.athena.asm.data.BoardNameComparator;
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

	private void readBoadInfo(List<Board> boards, List<Board> fullBoards) {
		for (Iterator<Board> iterator = boards.iterator(); iterator.hasNext();) {
			Board board = (Board) iterator.next();
			if (board != null && board.getEngName() != null
					&& !board.isDirectory()) {
				fullBoards.add(board);
			}
			readBoadInfo(board.getChildBoards(), fullBoards);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String doInBackground(String... params) {
		if (!aSMApplication.getCurrentApplication().isLoadDefaultCategoryFile()) {
			try {
				Log.d("asm", "Load default category file");
				InputStream is = m_context.getResources().openRawResource(
						R.raw.categories);// .openFileInput("CategoryList");
				FileOutputStream fos = m_context.openFileOutput("CategoryList",
						Context.MODE_PRIVATE);

				BufferedInputStream bufr = new BufferedInputStream(is);
				BufferedOutputStream bufw = new BufferedOutputStream(fos);

				int len = 0;

				byte[] buf = new byte[1024];
				while ((len = bufr.read(buf)) != -1) {
					bufw.write(buf, 0, len);
					bufw.flush();
				}

				bufw.close();
				bufr.close();
				aSMApplication.getCurrentApplication().updateDefaultCategoryLoadStatus(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			FileInputStream is = m_context.openFileInput("CategoryList");
			ObjectInputStream ois = new ObjectInputStream(is);
			m_viewModel.setCategoryList((List<Board>) ois.readObject());
			Log.d("com.athena.asm", "loading from file");
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (m_viewModel.getCategoryList() == null) {
			m_viewModel.updateCategoryList();

			ArrayList<Board> fullBoards = new ArrayList<Board>();
			readBoadInfo(m_viewModel.getCategoryList(), fullBoards);
			HashSet<String> keySet = new HashSet<String>();
			for (Iterator<Board> iterator = fullBoards.iterator(); iterator
					.hasNext();) {
				Board board = (Board) iterator.next();
				if (!keySet.contains(board.getEngName())) {
					keySet.add(board.getEngName());
				} else {
					iterator.remove();
				}
			}
			Collections.sort(fullBoards, new BoardNameComparator());

			try {
				FileOutputStream fos = m_context.openFileOutput("CategoryList",
						Context.MODE_PRIVATE);
				ObjectOutputStream os = new ObjectOutputStream(fos);
				os.writeObject(fullBoards);
				fos.close();
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
