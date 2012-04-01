package com.athena.asm;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.athena.asm.Adapter.BoardTypeListAdapter;
import com.athena.asm.Adapter.SubjectListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadSubjectTask;
import com.athena.asm.viewmodel.SubjectListViewModel;
import com.athena.asm.viewmodel.BaseViewModel;
import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;

public class SubjectListActivity extends Activity
								implements OnClickListener, android.content.DialogInterface.OnClickListener,
								BaseViewModel.OnViewModelChangObserver {

	public SmthSupport smthSupport;

	private LayoutInflater inflater;

	private SubjectListViewModel m_viewModel;

	EditText pageNoEditText;
	TextView titleTextView;

	private boolean isFirstIn = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.subject_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		aSMApplication application = (aSMApplication) getApplication();
		m_viewModel = application.getSubjectListViewModel();
		m_viewModel.registerViewModelChangeObserver(this);
		m_viewModel.setIsInRotation(false);
		
		smthSupport = SmthSupport.getInstance();
		
		String defaultBoardType = HomeActivity.application.getDefaultBoardType();
		Board currentBoard = (Board) getIntent().getSerializableExtra(StringUtility.BOARD);
		boolean isNewBoard = m_viewModel.updateCurrentBoard(currentBoard, defaultBoardType);
		
		titleTextView = (TextView) findViewById(R.id.boardTitle);
		
		if (HomeActivity.application.isNightTheme()) {
			((LinearLayout)titleTextView.getParent().getParent()).setBackgroundColor(getResources().getColor(R.color.body_background_night));
		}

		pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");

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
		
		ImageButton writeImageButton = (ImageButton) findViewById(R.id.writePost);
		writeImageButton.setOnClickListener(this);
		ImageButton switchModeImageButton = (ImageButton) findViewById(R.id.switchBoardMode);
		switchModeImageButton.setOnClickListener(this);
		
		if (isNewBoard) {
			LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
			loadSubjectTask.execute();
		}
		else {
			reloadPostList();
		}		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver();
		
		//If we do exit(not called due to rotation here),
		//clear the cache of post list
		if (!m_viewModel.isInRotation()) {
			aSMApplication application = (aSMApplication) getApplication();
			application.getPostListViewModel().clear();
		}
		
		super.onDestroy();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		m_viewModel.setIsInRotation(true);
		return m_viewModel;
	}

	public void reloadPostList() {
		if (isFirstIn) {
			m_viewModel.gotoLastPage();
			pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
			isFirstIn = false;
		}

		PullToRefreshListView listView = (PullToRefreshListView) findViewById(R.id.subject_list);
		listView.onRefreshComplete();
		listView.setAdapter(new SubjectListAdapter(inflater, m_viewModel.getSubjectList()));
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable(StringUtility.SUBJECT, (Subject)view.getTag());
				bundle.putInt(StringUtility.BOARD_TYPE, m_viewModel.getBoardType());
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
		
		titleTextView.setText(m_viewModel.getTitleText());
		
		listView.requestFocus();

	}

	@Override
	public void onClick(View view) {
		boolean isToRefresh = true;
		if (view.getId() == R.id.btn_first_page) {
			m_viewModel.gotoFirstPage();
		} else if (view.getId() == R.id.btn_last_page) {
			m_viewModel.gotoLastPage();
		} else if (view.getId() == R.id.btn_pre_page) {
			m_viewModel.gotoPrevPage();
		} else if (view.getId() == R.id.btn_go_page) {
			int pageSet = Integer.parseInt(pageNoEditText.getText().toString());
			m_viewModel.setCurrentPageNumber(pageSet);
		} else if (view.getId() == R.id.btn_next_page) {
			m_viewModel.gotoNextPage();
		} else if (view.getId() == R.id.switchBoardMode) {
			isToRefresh = false;
			m_viewModel.toggleBoardType();
			isFirstIn = true;
			LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
			loadSubjectTask.execute();
		} else if (view.getId() == R.id.writePost) {
			isToRefresh = false;
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.WritePostActivity");
			intent.putExtra(
					StringUtility.URL,
					"http://www.newsmth.net/bbspst.php?board="
							+ m_viewModel.getCurrentBoard().getEngName());
			intent.putExtra(StringUtility.WRITE_TYPE, 0);
			intent.putExtra(StringUtility.IS_REPLY, false);
			startActivityForResult(intent, 0);
		}
		
		if (isToRefresh) {
			m_viewModel.updateBoardCurrentPage();
			pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
			LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
			loadSubjectTask.execute();
		}
		
	}
	
	private void refreshSubjectList() {
	    LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
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
				LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
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
			//String[] items = { "同主题", "普通模式", "文摘区", "保留区" };
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this);
			builder.setTitle(R.string.post_alert_title);
			//builder.setItems(items,this);
			builder.setAdapter(new BoardTypeListAdapter(m_viewModel.getBoardType(), inflater), this);
			AlertDialog alert = builder.create();
			alert.show();
			break;
		case REFRESH_SUBJECTLIST:
			loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
			loadSubjectTask.execute();
			break;
		case SEARCH_POST:
			Intent postIntent = new Intent();
			postIntent.setClassName("com.athena.asm",
					"com.athena.asm.SearchPostActivity");
			postIntent.putExtra(StringUtility.BOARD, m_viewModel.getCurrentBoard().getEngName());
			postIntent.putExtra(StringUtility.BID, m_viewModel.getCurrentBoard().getBoardID());
			startActivity(postIntent);
			break;
		case CREATE_ID:
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.WritePostActivity");
			intent.putExtra(
					StringUtility.URL,
					"http://www.newsmth.net/bbspst.php?board="
							+ m_viewModel.getCurrentBoard().getEngName());
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

	@Override
	public void onClick(DialogInterface dialog, int which) {
		isFirstIn = true;
		m_viewModel.setBoardType(which);
		LoadSubjectTask loadSubjectTask = new LoadSubjectTask(this, m_viewModel, isFirstIn);
		loadSubjectTask.execute();
		dialog.dismiss();
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params) {
		if (changedPropertyName.equals(SubjectListViewModel.SUBJECTLIST_PROPERTY_NAME)) {
			reloadPostList();
		}
	}
}
