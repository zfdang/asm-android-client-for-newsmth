package com.athena.asm.util.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileTask extends AsyncTask<String, Integer, String> {
    private String m_url;
    private String m_filepath;

    private Boolean m_status;
    Context m_context;
    private ProgressDialog pdialog;

    public DownloadFileTask(Context ctx, String url, String filename, String path) {
        m_context = ctx;

        m_url = url;
        File file2 = new File(new File(path), filename);
        m_filepath = file2.getPath();
    }

    @Override
    protected void onPreExecute() {
        pdialog = new ProgressDialog(m_context);
        pdialog.setMessage(String.format("下载图片:%s", m_url));
        pdialog.setIndeterminate(false);
        pdialog.setMax(100);
        pdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        pdialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(m_url);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100%
            // progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(m_filepath);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            m_status = true;
        } catch (Exception e) {
            Log.d("DownloadFileTask", e.toString());
            m_status = false;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        pdialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        pdialog.dismiss();
        if (m_status) {
            Toast.makeText(m_context, String.format("图片保存成功:%s", m_filepath), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(m_context, "保存时出现未知错误...", Toast.LENGTH_SHORT).show();
        }
    }
}
