package com.athena.asm;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;

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
	
	private String boardName;
	private String boardID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.search_post);
		
		smthSupport = SmthSupport.getInstance();
		
		TextView titleTextView = (TextView) findViewById(R.id.title);
		boardName = getIntent().getStringExtra(StringUtility.BOARD);
		titleTextView.setText(boardName + "版内文章搜索");
		if (HomeActivity.application.isNightTheme()) {
			((LinearLayout)titleTextView.getParent().getParent()).setBackgroundColor(getResources().getColor(R.color.body_background_night));
		}
		
		boardID = getIntent().getStringExtra(StringUtility.BID);
		
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
	public void onClick(View v) {
		if (v.getId() == R.id.btn_start_post_search) {
			String queryString = "board=" + boardName;
			String title = titleEditText.getText().toString().trim();
			String title2 = title2EditText.getText().toString().trim();
			String title3 = title3EditText.getText().toString().trim();
			String userid = useridEditText.getText().toString().trim();
			String dt = dtEditText.getText().toString().trim();
			try {
				queryString += "&title=" + URLEncoder.encode(title.replaceAll(" ", "+"), "GBK");
				queryString += "&title2=" + URLEncoder.encode(title2.replaceAll(" ", "+"), "GBK");
				queryString += "&title3=" + URLEncoder.encode(title3.replaceAll(" ", "+"), "GBK");
				queryString += "&userid=" + userid.replaceAll(" ", "+");
				queryString += "&dt=" + dt.replaceAll(" ", "+");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			boolean mg = mgCheckBox.isChecked();
			if (mg) {
				queryString += "&mg=on";
			}
			boolean ag = agCheckBox.isChecked();
			if (ag) {
				queryString += "&ag=on";
			}
			boolean og = ogCheckBox.isChecked();
			if (og) {
				queryString += "&og=on";
			}
			List<Subject> subjects = smthSupport.getSearchSubjectList(boardName, boardID, queryString);
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.SearchPostResultListActivity");
			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.SUBJECT_LIST, (Serializable)subjects);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
}
