package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.athena.asm.data.Post;
import com.athena.asm.viewmodel.PostListViewModel;

public class ForwardPostToMailTask extends AsyncTask<String, Integer, String> {
	private ProgressDialog pdialog;
	
	private PostListViewModel m_viewModel;
	
	private Post m_post;
	
	private boolean m_result;
	
	private Context m_context;
	
	private int m_type;
	
	private String m_emailOrID;
	
	public static int FORWARD_TO_SELF = 0;
	public static int FORWARD_TO_EMAIL = 1;
	public static int FORWARD_TO_EMAIL_GROUP = 2;

	public ForwardPostToMailTask(Context ctx, PostListViewModel viewModel, Post post, int type, String recipient) {
		m_viewModel = viewModel;
		m_context = ctx;
		pdialog = new ProgressDialog(ctx);
		m_post = post;
		m_type = type;
		m_emailOrID = recipient;
	}

	@Override
	protected void onPreExecute() {
		pdialog.setMessage("转寄中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		if (m_type == FORWARD_TO_SELF) {
			m_result = m_viewModel.getSmthSupport().forwardPostToMailBox(m_post);
		} else if (m_type == FORWARD_TO_EMAIL) {
			m_result = m_viewModel.getSmthSupport().forwardPostToExternalMail(m_post, m_emailOrID);
		} else {
			m_result = m_viewModel.getSmthSupport().forwardGroupPostToExternalMail(m_post, m_emailOrID) ;
		}
		pdialog.cancel();
		return null;
	}

	@Override
    protected void onPostExecute(String result) {
	    if(m_emailOrID == null || m_emailOrID == ""){
	        m_emailOrID = "自己";
	    }
	    if (m_result) {
            Toast.makeText(m_context, "转寄成功! (To:" + m_emailOrID + ")", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(m_context, "转寄失败.. (To:" + m_emailOrID + ")", Toast.LENGTH_SHORT).show();
        }
    }
}
