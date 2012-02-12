package com.athena.asm.util.task;

import java.io.File;
import java.util.Iterator;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.athena.asm.AttachUploadActivity;

public class UploadAttachFilesTask extends AsyncTask<String, Integer, String> {
	private AttachUploadActivity activity = null;

	public UploadAttachFilesTask(AttachUploadActivity activity) {
		this.activity = activity;
	}

	private ProgressDialog pdialog;

	@Override
	protected void onPreExecute() {
		pdialog = new ProgressDialog(activity);
		pdialog.setMessage("上传附件中...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		for (Iterator<File> iterator = activity.attachArrayList.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			if (!activity.smthSupport.uploadAttachFile(file)) {
				break;
			}
		}
		
		pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		activity.uploadFinish();
	}
}
