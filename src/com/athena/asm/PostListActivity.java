package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.athena.asm.Adapter.PostListAdapter;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadPostTask;

public class PostListActivity extends Activity implements OnClickListener {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	public Subject currentSubject;
	public List<Post> postList;

	private boolean isToRefreshBoard = false;
	private int currentPageNo = 1;
	private int boardType = 0; // 1是普通，0是同主题
	EditText pageNoEditText;
	TextView totalPageNoTextView;
	Button firstButton;
	Button lastButton;
	Button preButton;
	Button goButton;
	Button nextButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();

		currentSubject = (Subject) getIntent().getSerializableExtra(
				StringUtility.SUBJECT);
		currentPageNo = currentSubject.getCurrentPageNo();

		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText(currentSubject.getTitle());

		pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setText(currentPageNo + "");

		firstButton = (Button) findViewById(R.id.btn_first_page);
		firstButton.setOnClickListener(this);
		lastButton = (Button) findViewById(R.id.btn_last_page);
		lastButton.setOnClickListener(this);
		preButton = (Button) findViewById(R.id.btn_pre_page);
		preButton.setOnClickListener(this);
		goButton = (Button) findViewById(R.id.btn_go_page);
		goButton.setOnClickListener(this);
		nextButton = (Button) findViewById(R.id.btn_next_page);
		nextButton.setOnClickListener(this);
		
		boardType = getIntent().getIntExtra(StringUtility.BOARD_TYPE, 0);

		LoadPostTask loadPostTask = new LoadPostTask(this, boardType, 0);
		loadPostTask.execute();
		// reloadPostList();
	}

	public void reloadPostList() {
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(new PostListAdapter(this, inflater, postList));
		totalPageNoTextView = (TextView) findViewById(R.id.textview_page_total_no);
		totalPageNoTextView.setText(" / " + currentSubject.getTotalPageNo());
		
		currentPageNo = currentSubject.getCurrentPageNo();
		pageNoEditText.setText(currentPageNo + "");
		listView.requestFocus();
		
		if (boardType == 0) {
			firstButton.setText(R.string.first_page);
			lastButton.setText(R.string.last_page);
			preButton.setText(R.string.pre_page);
			goButton.setVisibility(View.VISIBLE);
			nextButton.setText(R.string.next_page);
			pageNoEditText.setVisibility(View.VISIBLE);
			totalPageNoTextView.setVisibility(View.VISIBLE);
		}
		else {
			firstButton.setText(R.string.topic_first_page);
			lastButton.setText(R.string.topic_all_page);
			preButton.setText(R.string.topic_pre_page);
			goButton.setVisibility(View.GONE);
			nextButton.setText(R.string.topic_next_page);
			pageNoEditText.setVisibility(View.GONE);
			totalPageNoTextView.setVisibility(View.GONE);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			Bundle b = data.getExtras();
			isToRefreshBoard = b.getBoolean(StringUtility.REFRESH_BOARD);
			break;

		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
	    	
	    	Bundle b = new Bundle();
	    	b.putBoolean(StringUtility.REFRESH_BOARD, isToRefreshBoard);
	    	i.putExtras(b);
	    	
	    	this.setResult(RESULT_OK, i);
	    	this.finish();
	    	
	    	return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onClick(View view) {
		if (boardType == 0) { // 同主题导航
			if (view.getId() == R.id.btn_first_page) {
				if (currentPageNo == 1) {
					return;
				}
				currentPageNo = 1;
			} else if (view.getId() == R.id.btn_last_page) {
				if (currentPageNo == currentSubject.getTotalPageNo()) {
					return;
				}
				currentPageNo = currentSubject.getTotalPageNo();
			} else if (view.getId() == R.id.btn_pre_page) {
				currentPageNo--;
				if (currentPageNo < 1) {
					currentPageNo = 1;
					return;
				}
			} else if (view.getId() == R.id.btn_go_page) {
				int pageSet = Integer.parseInt(pageNoEditText.getText().toString());
				if (pageSet > currentSubject.getTotalPageNo()) {
					return;
				}
				currentPageNo = pageSet;
			} else if (view.getId() == R.id.btn_next_page) {
				currentPageNo++;
				if (currentPageNo > currentSubject.getTotalPageNo()) {
					currentPageNo = currentSubject.getTotalPageNo();
					return;
				}
			}
			currentSubject.setCurrentPageNo(currentPageNo);
			pageNoEditText.setText(currentPageNo + "");
			if (view.getParent() != null) {
				((View) view.getParent()).requestFocus();
			}
			
			LoadPostTask loadPostTask = new LoadPostTask(this, boardType, 0);
			loadPostTask.execute();
		}
		else {
			int action = 0;
			if (view.getId() == R.id.btn_first_page) {
				action = 1;
			} else if (view.getId() == R.id.btn_pre_page) {
				action = 2;
			} else if (view.getId() == R.id.btn_next_page) {
				action = 3;
			} else if (view.getId() == R.id.btn_last_page){
				boardType = 0;
				currentSubject.setSubjectID(currentSubject.getTopicSubjectID());
			}
			LoadPostTask loadPostTask = new LoadPostTask(this, boardType, action);
			loadPostTask.execute();
		}
	}
}
