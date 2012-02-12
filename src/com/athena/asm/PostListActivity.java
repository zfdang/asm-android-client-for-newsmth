package com.athena.asm;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.Adapter.PostListAdapter;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadPostTask;

public class PostListActivity extends Activity implements OnClickListener,
		OnTouchListener, OnLongClickListener {

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

	// 暂时的，为解决event继续dispatch而设
	private boolean isLongPressed;
	private int screenHeight;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();

		this.isLongPressed = false;
		this.screenHeight = getWindowManager().getDefaultDisplay().getHeight();

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

		listView = (ListView) findViewById(R.id.post_list);

		boardType = getIntent().getIntExtra(StringUtility.BOARD_TYPE, 0);

		LoadPostTask loadPostTask = new LoadPostTask(this, boardType, 0, 0);
		loadPostTask.execute();
		// reloadPostList();
	}

	public void reloadPostList() {
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
		} else {
			firstButton.setText(R.string.topic_first_page);
			lastButton.setText(R.string.topic_all_page);
			preButton.setText(R.string.topic_pre_page);
			goButton.setVisibility(View.GONE);
			nextButton.setText(R.string.topic_next_page);
			pageNoEditText.setVisibility(View.GONE);
			totalPageNoTextView.setVisibility(View.GONE);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
				int pageSet = Integer.parseInt(pageNoEditText.getText()
						.toString());
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

			LoadPostTask loadPostTask = new LoadPostTask(this, boardType, 0, 0);
			loadPostTask.execute();
		} else {
			int action = 0;
			int startNumber = 0;
			if (view.getId() == R.id.btn_first_page) {
				action = 1;
			} else if (view.getId() == R.id.btn_pre_page) {
				action = 2;
			} else if (view.getId() == R.id.btn_next_page) {
				action = 3;
			} else if (view.getId() == R.id.btn_last_page) {
				boardType = 0;
				startNumber = Integer.parseInt(currentSubject.getSubjectID());
				currentSubject.setSubjectID(currentSubject.getTopicSubjectID());
			}
			LoadPostTask loadPostTask = new LoadPostTask(this, boardType,
					action, startNumber);
			loadPostTask.execute();
		}
	}

	private void setListOffset(int jump) {
		int index = listView.getFirstVisiblePosition();
		Log.d("move", String.valueOf(index));
		int newIndex = index + jump;
		if (newIndex == -1) {
			newIndex = 0;
		} else if (listView.getItemAtPosition(newIndex) == null) {
			newIndex = index;
		}
		listView.setSelectionFromTop(newIndex, 0);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (HomeActivity.application.isTouchScroll()) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				isLongPressed = false;
			}
			if (event.getAction() == MotionEvent.ACTION_UP && !isLongPressed) {
				int touchY = (int) event.getRawY();
				float scale = screenHeight / 800;
				if (touchY > 60 * scale && touchY < 390 * scale) {
					Log.d("mouse", "up");
					if (v.getId() != R.id.PostListLayout) {
						setListOffset(-1);
					} else {
						setListOffset(-1);
					}
				} else if (touchY > 410 * scale && touchY < 740 * scale) {
					Log.d("mouse", "down");
					if (v.getId() != R.id.PostListLayout) {
						setListOffset(1);
					} else {
						setListOffset(1);
					}
				}
			}
		}
		return false;

	}

	@Override
	public boolean onLongClick(View v) {
		if (smthSupport.getLoginStatus()) {
			RelativeLayout relativeLayout = null;
			if (v.getId() == R.id.PostContent) {
				relativeLayout = (RelativeLayout) v.getParent();
			} else {
				relativeLayout = (RelativeLayout) v;
			}
			final String authorID = (String) ((TextView) relativeLayout
					.findViewById(R.id.AuthorID)).getText();
			final Post post = (Post) relativeLayout.getTag();
			final String[] items = { getString(R.string.post_reply_post),
					getString(R.string.post_reply_mail),
					getString(R.string.post_query_author),
					getString(R.string.post_copy_author),
					getString(R.string.post_copy_content),
					getString(R.string.post_foward_self)};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.post_alert_title);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent intent;
					switch (item) {
					case 0:
						intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.WritePostActivity");
						intent.putExtra(
								StringUtility.URL,
								"http://www.newsmth.net/bbspst.php?board="
										+ post.getBoard() + "&reid="
										+ post.getSubjectID());
						intent.putExtra(StringUtility.WRITE_TYPE, 0);
						intent.putExtra(StringUtility.IS_REPLY, true);
						// activity.startActivity(intent);
						startActivityForResult(intent, 0);
						break;
					case 1:
						intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.WritePostActivity");
						intent.putExtra(
								StringUtility.URL,
								"http://www.newsmth.net/bbspstmail.php?board="
										+ post.getBoard() + "&id="
										+ post.getSubjectID());
						intent.putExtra(StringUtility.WRITE_TYPE, 1);
						intent.putExtra(StringUtility.IS_REPLY, true);
						startActivity(intent);
						break;
					case 2:
						intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.ViewProfileActivity");
						intent.putExtra(StringUtility.USERID, authorID);
						startActivity(intent);
						break;
					case 3:
						ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						clip.setText(authorID);
						Toast.makeText(getApplicationContext(),
								"ID ： " + authorID + "已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						ClipboardManager clip2 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
						clip2.setText(post.getTextContent());
						Toast.makeText(getApplicationContext(),
								"帖子内容已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 5:
						boolean result = smthSupport.forwardPostToMailBox(post);
						if (result) {
							Toast.makeText(getApplicationContext(),
									"已转寄到自己信箱中",
									Toast.LENGTH_SHORT).show();
						}
						break;
					default:
						break;
					}
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		isLongPressed = true;
		return true;
	}
}
