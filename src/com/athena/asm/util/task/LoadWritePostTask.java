package com.athena.asm.util.task;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.WritePostActivity;
import com.athena.asm.util.StringUtility;
import com.athena.asm.viewmodel.WritePostViewModel;

public class LoadWritePostTask extends AsyncTask<String, Integer, String> {
	private WritePostActivity writePostActivity;
	private int type;
	private String contentString = null;
	private boolean isReply = false;
	
	private WritePostViewModel m_viewModel;

	public LoadWritePostTask(WritePostActivity activity, WritePostViewModel viewModel) {
		this.writePostActivity = activity;
		this.type = viewModel.getWriteType();
		m_viewModel = viewModel;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(writePostActivity);
		pdialog.setMessage("请稍候...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		String postUrl = "";
		if (type == WritePostActivity.TYPE_POST) {
			postUrl = "http://www.newsmth.net/bbssnd.php";
			Map<String, String> paramsMap = StringUtility
					.getUrlParams(m_viewModel.getToHandlerUrl());
			if (paramsMap.containsKey("board")) {
				postUrl += "?board=" + paramsMap.get("board");
			}
			if (paramsMap.containsKey("reid")) {
				postUrl += "&reid=" + paramsMap.get("reid");
				isReply = true;
			}
			
			m_viewModel.setPostUrl(postUrl);

			contentString = writePostActivity.smthSupport
					.getUrlContent(m_viewModel.getToHandlerUrl());
		} else if (type == WritePostActivity.TYPE_MAIL) {
			postUrl = "http://www.newsmth.net/bbssendmail.php";
			Map<String, String> paramsMap = StringUtility
					.getUrlParams(m_viewModel.getToHandlerUrl());
			if (paramsMap.containsKey("dir")) {
				m_viewModel.setMailDir(paramsMap.get("dir"));
				postUrl += "?dir=" + m_viewModel.getMailDir();
			}
			if (paramsMap.containsKey("userid")) {
				m_viewModel.setMailUserId(paramsMap.get("userid"));
				postUrl += "?userid="
						+ m_viewModel.getMailUserId();
			}
			if (paramsMap.containsKey("num")) {
				m_viewModel.setMailNumber(paramsMap.get("num"));
				postUrl += "?num=" + m_viewModel.getMailNumber();
			}
			if (paramsMap.containsKey("file")) {
				m_viewModel.setMailFile(paramsMap.get("file"));
				postUrl += "?file=" + m_viewModel.getMailFile();
			}
			if (paramsMap.containsKey("title")) {
				try {
					String postTitle;
					postTitle = URLDecoder.decode(
							paramsMap.get("title"), "GBK");
					if (!postTitle.contains("Re:")) {
						postTitle = "Re: "
								+ postTitle;
					}
					m_viewModel.setPostTitile(postTitle);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				postUrl += "?title=" + paramsMap.get("title");

			}
			
			m_viewModel.setPostUrl(postUrl);

			contentString = writePostActivity.smthSupport
					.getUrlContent(m_viewModel.getToHandlerUrl());
		} else if (type == WritePostActivity.TYPE_POST_EDIT) {
			postUrl = "http://www.newsmth.net/bbsedit.php";
			Map<String, String> paramsMap = StringUtility
					.getUrlParams(m_viewModel.getToHandlerUrl());
			if (paramsMap.containsKey("board")) {
				postUrl += "?board=" + paramsMap.get("board");
			}
			if (paramsMap.containsKey("id")) {
				postUrl += "&id=" + paramsMap.get("id");
			}
			postUrl += "&ftype=0";
			m_viewModel.setPostUrl(postUrl);
			
			contentString = writePostActivity.smthSupport
					.getUrlContent(m_viewModel.getToHandlerUrl());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (type == WritePostActivity.TYPE_POST) {
			writePostActivity.parsePostToHandleUrl(contentString, isReply);
		} else if (type == WritePostActivity.TYPE_MAIL) {
			writePostActivity.parseMailToHandleUrl(contentString);
		} else if (type == WritePostActivity.TYPE_POST_EDIT) {
			writePostActivity.parsePostEditToHandleUrl(contentString);
		}
		pdialog.cancel();
		writePostActivity.finishWork();
	}
}
