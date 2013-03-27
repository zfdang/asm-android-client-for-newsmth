package com.athena.asm.util.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.athena.asm.util.SmthSupport;
import com.athena.asm.viewmodel.HomeViewModel;

public class EditFavoriteTask extends AsyncTask<String, Integer, String> {
	public static int FAVORITE_ADD = 0;
	public static int FAVORITE_DELETE = 1;
    
    private ProgressDialog pdialog;

	private HomeViewModel m_viewModel;
    private Context m_context;
	private String m_boardName;
	private String m_boardID;
	private int m_action;

	private boolean m_result;

	public EditFavoriteTask(Context ctx, HomeViewModel viewModel, String boardname, String boardid, int action) {
		m_context = ctx;
		m_viewModel = viewModel;
		pdialog = new ProgressDialog(ctx);

		m_boardName = boardname;
		m_boardID = boardid;
		
		m_action = action;
	}

	@Override
	protected void onPreExecute() {
		pdialog.setMessage("修改收藏夹...");
		pdialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
	    SmthSupport smthSupport = SmthSupport.getInstance();
	    if( m_action == FAVORITE_ADD){
	        m_result = smthSupport.addBoardToFavorite(m_boardName);
	    } else if (m_action == FAVORITE_DELETE ){
            m_result = smthSupport.removeBoardFromFavorite(m_boardName, m_boardID);
	    }
	    pdialog.cancel();
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		if (m_result) {
		    // show toast
		    if(m_action == FAVORITE_ADD) {
                String alert = String.format("版面\"%s\"已添加到收藏夹!", m_boardName);
                Toast.makeText(m_context, alert, Toast.LENGTH_SHORT).show();
	        } else if (m_action == FAVORITE_DELETE){
                String alert = String.format("版面\"%s\"已从收藏夹删除!", m_boardName);
                Toast.makeText(m_context, alert, Toast.LENGTH_SHORT).show();
	        }
            // refresh favorite list
            m_context.deleteFile("FavList");
            m_viewModel.setFavList(null);
            m_viewModel.notifyFavListChanged();
		} else {
			String alert = String.format("操作版面\"%s\"失败!", m_boardName);
			Toast.makeText(m_context, alert, Toast.LENGTH_SHORT).show();
		}
	}
}
