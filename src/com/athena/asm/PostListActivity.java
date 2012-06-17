package com.athena.asm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.athena.asm.util.StringUtility;

public class PostListActivity extends SherlockFragmentActivity {
	
	private ProgressDialog m_pdialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list_activity);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();

			Bundle b = new Bundle();
			b.putBoolean(StringUtility.REFRESH_BOARD, ((aSMApplication) getApplication()).getPostListViewModel().isToRefreshBoard());
			i.putExtras(b);

			this.setResult(RESULT_OK, i);
			this.finish();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	public void showProgressDialog() {
		if (m_pdialog == null) {
			m_pdialog = new ProgressDialog(this);
			m_pdialog.setMessage("努力加载中...");
			m_pdialog.show();
		}
	}
	
	public void dismissProgressDialog() {
		if (m_pdialog != null) {
			m_pdialog.cancel();
			m_pdialog = null;
		}
	}

}
