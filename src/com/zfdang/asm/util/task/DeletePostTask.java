package com.zfdang.asm.util.task;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.zfdang.asm.listener.RefreshEventListener;
import com.zfdang.asm.util.SmthSupport;
import com.zfdang.asm.viewmodel.PostListViewModel;

public class DeletePostTask extends AsyncTask<String, Integer, String> {
	private ProgressDialog pdialog;

	private Context m_context;
	private PostListViewModel m_viewModel;
	private String m_boardName;
	private String m_postID;
	private List<RefreshEventListener> m_listners = new ArrayList<RefreshEventListener>();

	private boolean m_result;

	public DeletePostTask(Context ctx, PostListViewModel viewModel, String boardname, String postid, RefreshEventListener listener) {
		m_viewModel = viewModel;
		m_context = ctx;
		pdialog = new ProgressDialog(ctx);

		m_boardName = boardname;
		m_postID = postid;

		if(listener != null)
			m_listners.add(listener);
	}

	@Override
	protected void onPreExecute() {
		pdialog.setMessage("删除帖子中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		SmthSupport smthSupport = m_viewModel.getSmthSupport();
		m_result = smthSupport.deletePost(m_boardName, m_postID);
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (m_result) {
			// refresh current post
			String alert = String.format("帖子(id=%s)已删除!", m_postID);
			Toast.makeText(m_context, alert, Toast.LENGTH_SHORT).show();
	        for(RefreshEventListener listner: m_listners) {
				listner.refresh();
	        }
		} else {
			String alert = String.format("帖子(id=%s)删除失败!", m_postID);
			Toast.makeText(m_context, alert, Toast.LENGTH_SHORT).show();
		}
	}
}
