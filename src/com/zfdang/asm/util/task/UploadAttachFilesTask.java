package com.zfdang.asm.util.task;

import java.io.File;
import java.util.Iterator;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.zfdang.asm.AttachUploadActivity;

public class UploadAttachFilesTask extends AsyncTask<String, Integer, String> {
    private AttachUploadActivity activity = null;
    private int m_total;
    private int m_current;
    private boolean m_result;

    public UploadAttachFilesTask(AttachUploadActivity activity) {
        this.activity = activity;
        m_total = activity.m_attachArrayList.size();
        m_current = 0;
        m_result = true;
    }

    private ProgressDialog pdialog;

    @Override
    protected void onPreExecute() {
        pdialog = new ProgressDialog(activity);
        pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pdialog.setCancelable(false);
        pdialog.setMessage("上传附件中...");
        pdialog.setMax(m_total);
        pdialog.setProgress(0);
        pdialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        for (Iterator<File> iterator = activity.m_attachArrayList.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if (!activity.m_smthSupport.uploadAttachFile(file)) {
                m_result = false;
                break;
            }
            m_current++;
            pdialog.setProgress(m_current);
        }

        pdialog.cancel();
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        activity.uploadFinish(m_result);
    }
}
