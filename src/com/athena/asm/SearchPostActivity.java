package com.athena.asm;

import java.io.Serializable;
import java.util.List;

import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.viewmodel.SearchPostViewModel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchPostActivity extends Activity implements OnClickListener {
	public SmthSupport smthSupport;
	
	EditText titleEditText;
	EditText title2EditText;
	EditText title3EditText;
	EditText useridEditText;
	EditText dtEditText;
	
	CheckBox mgCheckBox;
	CheckBox agCheckBox;
	CheckBox ogCheckBox;
	
	Button startSearchButton;
	
	private SearchPostViewModel m_viewModel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_post);
		
		smthSupport = SmthSupport.getInstance();
		
		m_viewModel = (SearchPostViewModel)getLastNonConfigurationInstance();
		if (m_viewModel == null) {
			m_viewModel = new SearchPostViewModel();
		}
		
		
		m_viewModel.setBoardName(getIntent().getStringExtra(StringUtility.BOARD));
		m_viewModel.setBoardID(getIntent().getStringExtra(StringUtility.BID));
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(m_viewModel.getTitleText());
		
		if (HomeActivity.application.isNightTheme()) {
			((LinearLayout)titleTextView.getParent().getParent()).setBackgroundColor(getResources().getColor(R.color.body_background_night));
		}
		
		titleEditText = (EditText) findViewById(R.id.edittext_title);
		title2EditText = (EditText) findViewById(R.id.edittext_title2);
		title3EditText = (EditText) findViewById(R.id.edittext_title3);
		useridEditText = (EditText) findViewById(R.id.edittext_userid);
		dtEditText = (EditText) findViewById(R.id.edittext_dt);
		
		mgCheckBox = (CheckBox) findViewById(R.id.checkbox_mg);
		agCheckBox = (CheckBox) findViewById(R.id.checkbox_ag);
		ogCheckBox = (CheckBox) findViewById(R.id.checkbox_og);
		
		startSearchButton = (Button) findViewById(R.id.btn_start_post_search);
		startSearchButton.setOnClickListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		updateViewModel();
		return m_viewModel;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_start_post_search) {
			
			updateViewModel();
			List<Subject> subjects = m_viewModel.searchSubject();
			
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.SearchPostResultListActivity");
			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.SUBJECT_LIST, (Serializable)subjects);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
	
	private void updateViewModel() {
		m_viewModel.setTitle(titleEditText.getText().toString().trim());
		m_viewModel.setTitle2(title2EditText.getText().toString().trim());
		m_viewModel.setTitle3(title3EditText.getText().toString().trim());
		m_viewModel.setUserId(useridEditText.getText().toString().trim());
		m_viewModel.setDays(dtEditText.getText().toString().trim());
		m_viewModel.setMgFlag(mgCheckBox.isChecked());
		m_viewModel.setAgFlag(agCheckBox.isChecked());
		m_viewModel.setOgFlag(ogCheckBox.isChecked());
	}
}
