package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
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

	private int currentPageNo = 1;
	private int boardType = 0;
	EditText pageNoEditText;
	TextView totalPageNoTextView;

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

		Button preButton = (Button) findViewById(R.id.btn_pre_page);
		preButton.setOnClickListener(this);
		Button goButton = (Button) findViewById(R.id.btn_go_page);
		goButton.setOnClickListener(this);
		Button nextButton = (Button) findViewById(R.id.btn_next_page);
		nextButton.setOnClickListener(this);
		
		boardType = getIntent().getIntExtra(StringUtility.BOARD_TYPE, 0);

		LoadPostTask loadPostTask = new LoadPostTask(this, boardType);
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
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_pre_page) {
			currentPageNo--;
			if (currentPageNo < 1) {
				currentPageNo = 1;
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
		
		LoadPostTask loadPostTask = new LoadPostTask(this, boardType);
		loadPostTask.execute();
	}
}
