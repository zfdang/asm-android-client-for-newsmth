package com.zfdang.asm.util.task;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.zfdang.asm.data.Post;
import com.zfdang.asm.util.SmthSupport;

public class OpenPostInBrowserTask extends AsyncTask<String, Integer, String> {
    private Post m_post;
    private Context m_context;

    public OpenPostInBrowserTask(Context context, Post post) {
        m_post = post;
        m_context = context;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(String... params) {

        String boardID = m_post.getBoardID();
        if (boardID == null || boardID.length() == 0 || boardID.equals("fake")) {
            boardID = SmthSupport.getInstance().getBoardIDFromName(m_post.getBoard());
            m_post.setBoardID(boardID);
        }
        String weburl = String.format("http://www.newsmth.net/bbscon.php?bid=%s&id=%s", m_post.getBoardID(),
                m_post.getSubjectID());
        Uri uri = Uri.parse(weburl);
        m_context.startActivity(new Intent(Intent.ACTION_VIEW, uri));

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
    }

}
