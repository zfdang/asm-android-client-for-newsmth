package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.athena.asm.Adapter.SubjectListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadSubjectTask;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class SubjectListActivity extends Activity implements OnClickListener, android.content.DialogInterface.OnClickListener {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	public Board currentBoard;
	public List<Subject> subjectList;

	private int currentPageNo = 1;
	public int boardType = 0;
	EditText pageNoEditText;
	TextView totalPageNoTextView;

	private boolean isFirstIn = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.subject_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		smthSupport = SmthSupport.getInstance();

		currentBoard = (Board) getIntent().getSerializableExtra(
				StringUtility.BOARD);
		TextView titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setText("[" + currentBoard.getEngName() + "]"
				+ currentBoard.getChsName());

		pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setText(currentPageNo + "");

		Button firstButton = (Button) findViewById(R.id.btn_first_page);
		firstButton.setOnClickListener(this);
		Button lastButton = (Button) findViewById(R.id.btn_last_page);
		lastButton.setOnClickListener(this);
		Button preButton = (Button) findViewById(R.id.btn_pre_page);
		preButton.setOnClickListener(this);
		Button goButton = (Button) findViewById(R.id.btn_go_page);
		goButton.setOnClickListener(this);
		Button nextButton = (Button) findViewById(R.id.btn_next_page);
		nextButton.setOnClickListener(this);

		String defaultBoardType = HomeActivity.application.getDefaultBoardType();
		if (defaultBoardType.equals("001")) {
			boardType = 0;
		} else {
			boardType = 1;
		}
		LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
		loadSubjectTask.execute();
		// reloadPostList();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	public void reloadPostList() {
		if (isFirstIn) {
			currentPageNo = currentBoard.getTotalPageNo();
			pageNoEditText.setText(currentPageNo + "");
			isFirstIn = false;
		}

		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.subject_list);
		listView.setAdapter(new SubjectListAdapter(inflater, subjectList));
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable(StringUtility.SUBJECT, (Subject)view.getTag());
				bundle.putInt(StringUtility.BOARD_TYPE, boardType);
				intent.putExtras(bundle);
				intent.setClassName("com.athena.asm", "com.athena.asm.PostListActivity");
				//activity.startActivity(intent);
				startActivityForResult(intent, 0);
			}
		});
		
		listView.setOnRefreshListener(new OnRefreshListener() {
                    
                    @Override
                    public void onRefresh() {
                        refreshSubjectList();
                    }
                });
		
		totalPageNoTextView = (TextView) findViewById(R.id.textview_page_total_no);
		totalPageNoTextView.setText(" / " + currentBoard.getTotalPageNo());
		listView.requestFocus();

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_first_page) {
			currentPageNo = 1;
		} else if (view.getId() == R.id.btn_last_page) {
			currentPageNo = currentBoard.getTotalPageNo();
		} else if (view.getId() == R.id.btn_pre_page) {
			currentPageNo--;
			if (currentPageNo < 1) {
				currentPageNo = 1;
			}
		} else if (view.getId() == R.id.btn_go_page) {
			int pageSet = Integer.parseInt(pageNoEditText.getText().toString());
			if (pageSet > currentBoard.getTotalPageNo()) {
				return;
			}
			currentPageNo = pageSet;
		} else if (view.getId() == R.id.btn_next_page) {
			currentPageNo++;
			if (currentPageNo > currentBoard.getTotalPageNo()) {
				currentPageNo = currentBoard.getTotalPageNo();
			}
		}
		currentBoard.setCurrentPageNo(currentPageNo);
		pageNoEditText.setText(currentPageNo + "");
		LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
		loadSubjectTask.execute();
	}
	
	private void refreshSubjectList() {
	    LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
            loadSubjectTask.execute();
        }

	public static final int SWITCH_BOARD_TYPE = Menu.FIRST;
	//public static final int SWITCH_BOARD_AREA = Menu.FIRST + 1;
	public static final int REFRESH_SUBJECTLIST = Menu.FIRST + 1;
	public static final int SEARCH_POST = Menu.FIRST + 2;
	public static final int CREATE_ID = Menu.FIRST + 3;
	
	protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			Bundle b = data.getExtras();
			boolean isToRefreshBoard = b.getBoolean(StringUtility.REFRESH_BOARD);
			if (isToRefreshBoard) {
				LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
				loadSubjectTask.execute();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		/*if (boardType == 1) {
			menu.add(0, SWITCH_BOARD_TYPE, Menu.NONE, "切换为同主题");
		}
		else {
			menu.add(0, SWITCH_BOARD_TYPE, Menu.NONE, "切换为普通模式");
		}*/
		menu.add(0, SWITCH_BOARD_TYPE, Menu.NONE, "切换到...");
		menu.add(0, REFRESH_SUBJECTLIST, Menu.NONE, "刷新");
		menu.add(0, SEARCH_POST, Menu.NONE, "搜索");
		if (smthSupport.getLoginStatus()) {
			menu.add(0, CREATE_ID, Menu.NONE, "发新贴");
		}
		

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		LoadSubjectTask loadSubjectTask;
		switch (item.getItemId()) {
		/*case SWITCH_BOARD_TYPE:
			boardType = (boardType + 1) % 2;// switch type and excute refresh
			isFirstIn = true;
			loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
			loadSubjectTask.execute();
			break;*/
		case SWITCH_BOARD_TYPE:
			String[] items = { "同主题", "普通模式", "文摘区", "保留区" };
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setTitle(R.string.post_alert_title);
			builder.setItems(items,this);
			AlertDialog alert = builder.create();
			alert.show();
			break;
		case REFRESH_SUBJECTLIST:
			loadSubjectTask = new LoadSubjectTask(this, boardType, isFirstIn);
			loadSubjectTask.execute();
			break;
		case SEARCH_POST:
			Intent postIntent = new Intent();
			postIntent.setClassName("com.athena.asm",
					"com.athena.asm.SearchPostActivity");
			postIntent.putExtra(StringUtility.BOARD, currentBoard.getEngName());
			postIntent.putExtra(StringUtility.BID, currentBoard.getBoardID());
			startActivity(postIntent);
			break;
		case CREATE_ID:
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.WritePostActivity");
			intent.putExtra(
					StringUtility.URL,
					"http://www.newsmth.net/bbspst.php?board="
							+ currentBoard.getEngName());
			intent.putExtra(StringUtility.WRITE_TYPE, 0);
			intent.putExtra(StringUtility.IS_REPLY, false);
			//startActivity(intent);
			startActivityForResult(intent, 0);
			break;
		default:
			break;
		}
		return true;
	}
	
	/*@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (boardType == 1) {
			menu.getItem(0).setTitle("切换为同主题");
		}
		else {
			menu.getItem(0).setTitle("切换为普通模式");
		}
		
		return true;
	}*/

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isFirstIn = true;
		if (which == 0) {
			boardType = 0;
		}
		else {
			boardType = 1;
		}
		LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, which, isFirstIn);
		loadSubjectTask.execute();
		dialog.dismiss();
	}
}
