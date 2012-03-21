package com.athena.asm.util.task;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.WritePostActivity;
import com.athena.asm.util.StringUtility;

public class LoadWritePostTask extends AsyncTask<String, Integer, String> {
	private WritePostActivity writePostActivity;
	private int type;
	private String contentString = null;
	private boolean isReply = false;

	public LoadWritePostTask(WritePostActivity activity, int type) {
		this.writePostActivity = activity;
		this.type = type;
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
		if (type == WritePostActivity.TYPE_POST) {
			writePostActivity.postUrl = "http://www.newsmth.net/bbssnd.php";
	        Map<String, String> paramsMap = StringUtility.getUrlParams(writePostActivity.toHandleUrl);
	        if (paramsMap.containsKey("board")) {
	        	writePostActivity.postUrl += "?board=" + paramsMap.get("board");
	        }
	        if (paramsMap.containsKey("reid")) {
	        	writePostActivity.postUrl += "&reid=" + paramsMap.get("reid");
	            isReply = true;
	        }

	        contentString = writePostActivity.smthSupport.getUrlContent(writePostActivity.toHandleUrl);
		}
		else {
			writePostActivity.postUrl = "http://www.newsmth.net/bbssendmail.php";
	        Map<String, String> paramsMap = StringUtility.getUrlParams(writePostActivity.toHandleUrl);
	        if (paramsMap.containsKey("dir")) {
	        	writePostActivity.dir = paramsMap.get("dir");
	        	writePostActivity.postUrl += "?dir=" + writePostActivity.dir;
	        }
	        if (paramsMap.containsKey("userid")) {
	        	writePostActivity.userid = paramsMap.get("userid");
	        	writePostActivity.postUrl += "?userid=" + writePostActivity.userid;
	        }
	        if (paramsMap.containsKey("num")) {
	        	writePostActivity.num = paramsMap.get("num");
	        	writePostActivity.postUrl += "?num=" + writePostActivity.num;
	        }
	        if (paramsMap.containsKey("file")) {
	        	writePostActivity.file = paramsMap.get("file");
	        	writePostActivity.postUrl += "?file=" + writePostActivity.file;
	        }
	        if (paramsMap.containsKey("title")) {
	            try {
	            	writePostActivity.postTitle = URLDecoder.decode(paramsMap.get("title"), "GBK");
	                if (!writePostActivity.postTitle.contains("Re:")) {
	                	writePostActivity.postTitle = "Re: " + writePostActivity.postTitle;
	                }
	            } catch (UnsupportedEncodingException e) {
	                e.printStackTrace();
	            }
	            writePostActivity.postUrl += "?title=" + paramsMap.get("title");
	            
	        }

	        contentString = writePostActivity.smthSupport.getUrlContent(writePostActivity.toHandleUrl);
		}
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (type == WritePostActivity.TYPE_POST)
			writePostActivity.parsePostToHandleUrl(contentString, isReply);
		else {
			writePostActivity.parseMailToHandleUrl(contentString);
		}
		writePostActivity.finishWork();
	}
}
