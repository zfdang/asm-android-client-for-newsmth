package com.athena.asm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.athena.asm.util.StringUtility;
import com.athena.asm.viewmodel.PostListViewModel;

public class PostListActivity extends SherlockFragmentActivity
							  implements ProgressDialogProvider,
							  OnOpenActivityFragmentListener {
	
	private ProgressDialog m_pdialog;
	
	private PostListViewModel m_viewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list_activity);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		aSMApplication application = (aSMApplication) getApplication();
		m_viewModel = application.getPostListViewModel();
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
	
	public void doFinishBackToBoard(String boardEngName, String boardChsName) {
		Intent i = new Intent();
		i.putExtra("board_eng_name", boardEngName);
		i.putExtra("board_chs_name", boardChsName);
		setResult(Activity.RESULT_OK, i);
		finish();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
			Bundle b = data.getExtras();
			m_viewModel.setIsToRefreshBoard(b.getBoolean(StringUtility.REFRESH_BOARD));
			break;

		default:
			break;
		}
	}

	@Override
	public void onOpenActivityOrFragment(String target, Bundle bundle) {
		Intent intent = new Intent();
		intent.putExtras(bundle);
		if (target.equals(ActivityFragmentTargets.WRITE_POST)) {
			int writeType = bundle.getInt(StringUtility.WRITE_TYPE);
			if (writeType == WritePostActivity.TYPE_MAIL) {
				intent.setClassName("com.athena.asm", WritePostActivity.class.getName());
				startActivity(intent);
			}
			else if (writeType == WritePostActivity.TYPE_POST ||
					 writeType == WritePostActivity.TYPE_POST_EDIT) {
				intent.setClassName("com.athena.asm", WritePostActivity.class.getName());
				startActivityForResult(intent, 0);
			}
		}
		else if (target.equals(ActivityFragmentTargets.VIEW_PROFILE)) {
			intent.setClassName("com.athena.asm", ViewProfileActivity.class.getName());
			startActivity(intent);
		}
		
	}

}
