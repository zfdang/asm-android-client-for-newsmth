package com.zfdang.asm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.zfdang.asm.fragment.SubjectListFragment;
import com.zfdang.asm.R;

public class SubjectListActivity extends SherlockFragmentActivity
								 implements ProgressDialogProvider, OnOpenActivityFragmentListener {
	
	private ProgressDialog m_pdialog;
	private SubjectListFragment m_fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.subject_list_activity);	

		// http://stackoverflow.com/questions/8181157/castingproblem-with-findfragmentbyid
		FragmentManager fm = getSupportFragmentManager();
		m_fragment = (SubjectListFragment) fm.findFragmentById(R.id.subject_list_fragment);

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setRequestedOrientation(aSMApplication.ORIENTATION);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	public void showProgressDialog() {
		if (m_pdialog == null) {
			m_pdialog = new ProgressDialog(this);
			m_pdialog.setMessage("加载版面列表中...");
			m_pdialog.show();
		}
	}
	
	public void dismissProgressDialog() {
		if (m_pdialog != null) {
			m_pdialog.cancel();
			m_pdialog = null;
		}
	}
	
	@Override
	public void onOpenActivityOrFragment(String target, Bundle bundle) {
		if (target.equals(ActivityFragmentTargets.POST_LIST)) {
			Intent intent = new Intent();
			intent.putExtras(bundle);
			intent.setClassName("com.zfdang.asm", PostListActivity.class.getName());
			startActivityForResult(intent, 0);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return m_fragment.onKeyDown(keyCode);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	// http://stackoverflow.com/questions/4500354/control-volume-keys
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    // disable the beep sound when volume up/down is pressed
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
	       return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}

}
