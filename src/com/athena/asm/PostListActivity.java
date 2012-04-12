package com.athena.asm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.athena.asm.viewmodel.PostListViewModel;
import com.athena.asm.viewmodel.BaseViewModel;

public class PostListActivity extends Activity implements OnClickListener,
		OnTouchListener, OnLongClickListener, OnGestureListener, BaseViewModel.OnViewModelChangObserver {

	public SmthSupport m_smthSupport;

	private LayoutInflater m_inflater;
	
	private PostListViewModel m_viewModel;

	EditText m_pageNoEditText;
	Button m_firstButton;
	Button m_lastButton;
	Button m_preButton;
	Button m_goButton;
	Button m_nextButton;

	TextView m_titleTextView;

	private int m_screenHeight;
	private ListView m_listView;

	private GestureDetector m_GestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.post_list);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_smthSupport = SmthSupport.getInstance();
		
		m_viewModel = (PostListViewModel)getLastNonConfigurationInstance();
		boolean isNewSubject = false;
		if (m_viewModel == null) {
			aSMApplication application = (aSMApplication) getApplication();
			m_viewModel = application.getPostListViewModel();
			
			Subject newSubject = (Subject) getIntent().getSerializableExtra(
					StringUtility.SUBJECT);
			isNewSubject = m_viewModel.updateSubject(newSubject);
		}
		m_viewModel.registerViewModelChangeObserver(this);
		
		this.m_screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		
		m_titleTextView = (TextView) findViewById(R.id.title);

		if (HomeActivity.m_application.isNightTheme()) {
			((LinearLayout) m_titleTextView.getParent().getParent())
					.setBackgroundColor(getResources().getColor(
							R.color.body_background_night));
		}

		m_pageNoEditText = (EditText) findViewById(R.id.edittext_page_no);
		m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");

		m_firstButton = (Button) findViewById(R.id.btn_first_page);
		m_firstButton.setOnClickListener(this);
		m_lastButton = (Button) findViewById(R.id.btn_last_page);
		m_lastButton.setOnClickListener(this);
		m_preButton = (Button) findViewById(R.id.btn_pre_page);
		m_preButton.setOnClickListener(this);
		m_goButton = (Button) findViewById(R.id.btn_go_page);
		m_goButton.setOnClickListener(this);
		m_nextButton = (Button) findViewById(R.id.btn_next_page);
		m_nextButton.setOnClickListener(this);

		m_listView = (ListView) findViewById(R.id.post_list);

		m_viewModel.setBoardType(getIntent().getIntExtra(StringUtility.BOARD_TYPE, 0));
		m_viewModel.setIsToRefreshBoard(false);

		m_GestureDetector = new GestureDetector(this);

		if (isNewSubject) {
			LoadPostTask loadPostTask = new LoadPostTask(this, m_viewModel, m_viewModel.getCurrentSubject(),
					0, false, false);
			loadPostTask.execute();
		}
		else {
			reloadPostList();
		}
		// reloadPostList();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		return m_viewModel;
	}
	
	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver();
		
		super.onDestroy();
	}

	public void reloadPostList() {
		if (m_viewModel.getPostList() == null) {
			
			m_viewModel.ensurePostExists();
			
			m_firstButton.setEnabled(false);
			m_preButton.setEnabled(false);
			m_nextButton.setEnabled(false);
			m_lastButton.setEnabled(false);
		}

		m_listView.setAdapter(new PostListAdapter(this, m_inflater, m_viewModel.getPostList()));

		m_viewModel.updateCurrentPageNumberFromSubject();
		m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
		m_listView.requestFocus();

		m_viewModel.setIsPreloadFinished(false);
		m_viewModel.updatePreloadSubjectFromCurrentSubject();

		if (m_viewModel.getBoardType() == 0) {
			m_goButton.setVisibility(View.VISIBLE);
			m_pageNoEditText.setVisibility(View.VISIBLE);
			m_firstButton.setText(R.string.first_page);
			m_lastButton.setText(R.string.last_page);
			m_preButton.setText(R.string.pre_page);
			m_nextButton.setText(R.string.next_page);

			m_titleTextView.setText(m_viewModel.getSubjectTitle());

		} else {
			m_goButton.setVisibility(View.GONE);
			m_pageNoEditText.setVisibility(View.GONE);
			m_firstButton.setText(R.string.topic_first_page);
			m_lastButton.setText(R.string.topic_all_page);
			m_preButton.setText(R.string.topic_pre_page);
			m_nextButton.setText(R.string.topic_next_page);

			m_titleTextView.setText(m_viewModel.getSubjectTitle());

		}

		if (m_viewModel.getBoardType() == 0) {
			int nextPage = m_viewModel.getNextPageNumber();
			if (nextPage > 0) {
				m_viewModel.getPreloadSubject().setCurrentPageNo(nextPage);
				LoadPostTask loadPostTask = new LoadPostTask(this, m_viewModel,
						m_viewModel.getPreloadSubject(), 0, true, false);
				loadPostTask.execute();
			}
		} else {
			LoadPostTask loadPostTask = new LoadPostTask(this, m_viewModel, m_viewModel.getPreloadSubject(),
					3, true, false);
			loadPostTask.execute();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case RESULT_OK:
			Bundle b = data.getExtras();
			m_viewModel.setIsToRefreshBoard(b.getBoolean(StringUtility.REFRESH_BOARD));
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
			b.putBoolean(StringUtility.REFRESH_BOARD, m_viewModel.isToRefreshBoard());
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
		boolean isNext = false;
		if (m_viewModel.getBoardType() == 0) { // 同主题导航

			if (view.getId() == R.id.btn_first_page) {
				m_viewModel.gotoFirstPage();
			} else if (view.getId() == R.id.btn_last_page) {
				m_viewModel.gotoLastPage();
			} else if (view.getId() == R.id.btn_pre_page) {
				m_viewModel.gotoPrevPage();
			} else if (view.getId() == R.id.btn_go_page) {
				int pageSet = Integer.parseInt(m_pageNoEditText.getText()
						.toString());
				m_viewModel.setCurrentPageNumber(pageSet);
			} else if (view.getId() == R.id.btn_next_page) {
				m_viewModel.gotoNextPage();
				isNext = true;
			}

			m_viewModel.updateSubjectCurrentPageNumberFromCurrentPageNumber();
			m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
			if (view.getParent() != null) {
				((View) view.getParent()).requestFocus();
			}

			LoadPostTask loadPostTask = new LoadPostTask(this, m_viewModel, m_viewModel.getCurrentSubject(),
					0, false, isNext);
			loadPostTask.execute();
		} else {
			int action = 0;
			// int startNumber = 0;
			if (view.getId() == R.id.btn_first_page) {
				action = 1;
			} else if (view.getId() == R.id.btn_pre_page) {
				action = 2;
			} else if (view.getId() == R.id.btn_next_page) {
				action = 3;
				isNext = true;
			} else if (view.getId() == R.id.btn_last_page) {
				m_viewModel.setBoardType(0);
				// startNumber =
				// Integer.parseInt(currentSubject.getSubjectID());
				m_viewModel.updateSubjectIDFromTopicSubjectID();
				m_viewModel.setSubjectCurrentPageNumber(1);
			}
			LoadPostTask loadPostTask = new LoadPostTask(this, m_viewModel, m_viewModel.getCurrentSubject(),
					action, false, isNext);
			loadPostTask.execute();
		}
	}

	private void setListOffset(int jump) {
		int index = m_listView.getFirstVisiblePosition();
		Log.d("move", String.valueOf(index));
		int newIndex = index + jump;
		if (newIndex == -1) {
			newIndex = 0;
		} else if (m_listView.getItemAtPosition(newIndex) == null) {
			newIndex = index;
		}
		m_listView.setSelectionFromTop(newIndex, 0);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		m_GestureDetector.onTouchEvent(event);
		return false;

	}

	@Override
	public boolean onLongClick(View v) {
		if (m_smthSupport.getLoginStatus()) {
			RelativeLayout relativeLayout = null;
			if (v.getId() == R.id.PostContent) {
				relativeLayout = (RelativeLayout) v.getParent();
			} else {
				relativeLayout = (RelativeLayout) v;
			}
			final String authorID = (String) ((TextView) relativeLayout
  					.findViewById(R.id.AuthorID)).getText();
  			final Post post = ((PostListAdapter.ViewHolder)relativeLayout.getTag()).post;
 			final Post firstPost = m_viewModel.getPostList().get(0);
			List<String> itemList = new ArrayList<String>();
			itemList.add(getString(R.string.post_reply_post));
			itemList.add(getString(R.string.post_reply_mail));
			itemList.add(getString(R.string.post_query_author));
			itemList.add(getString(R.string.post_copy_author));
			itemList.add(getString(R.string.post_copy_content));
			itemList.add(getString(R.string.post_foward_self));
 			itemList.add(getString(R.string.post_foward_external));
 			itemList.add(getString(R.string.post_group_foward_external));
			if (post.getAuthor().equals(m_smthSupport.userid)) {
				itemList.add(getString(R.string.post_edit_post));
			}
			final String[] items = new String[itemList.size()];
			itemList.toArray(items);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.post_alert_title);
			builder.setAdapter(new ArrayAdapter(PostListActivity.this,
                    R.layout.alert_narrow_item, items), new DialogInterface.OnClickListener() {
			//builder.setItems(items, new DialogInterface.OnClickListener() {
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
						intent.putExtra(StringUtility.WRITE_TYPE, WritePostActivity.TYPE_POST);
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
						Toast.makeText(getApplicationContext(), "帖子内容已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 5:
						boolean result = m_smthSupport.forwardPostToMailBox(post);
						if (result) {
							Toast.makeText(getApplicationContext(),
									"已转寄到自己信箱中", Toast.LENGTH_SHORT).show();
						}
						break;
					case 6:
						forwardToEmail(post, false);
						break;
					case 7:
						forwardToEmail(firstPost, true);
						break;
					case 8:
						intent = new Intent();
						intent.setClassName("com.athena.asm",
								"com.athena.asm.WritePostActivity");
						intent.putExtra(
								StringUtility.URL,
								"http://www.newsmth.net/bbsedit.php?board="
										+ post.getBoard() + "&id="
										+ post.getSubjectID() + "&ftype=");
						intent.putExtra(StringUtility.WRITE_TYPE, WritePostActivity.TYPE_POST_EDIT);
						intent.putExtra(StringUtility.TITLE, post.getTitle().replace("主题:", ""));
						startActivityForResult(intent, 0);
					default:
						break;
					}
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		return true;
	}

	private void forwardToEmail(final Post post, final boolean group)
	{
		String email = HomeActivity.m_application.getForwardEmailAddr();
		
		if (email == "") {
			final EditText input = new EditText(this);

			new AlertDialog.Builder(this)
			.setTitle("设置转寄邮箱")
			.setMessage("您还没有设置转寄邮箱，请先设置。如需更改，请至设置页面")
			.setView(input)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Editable value = input.getText(); 
					HomeActivity.m_application.updateForwardEmailAddr(value.toString());
					
					String msg = null;
					boolean result;

					if (group)
						result = m_smthSupport.forwardGroupPostToExternalMail(post, value.toString()) ;
					else 
						result = m_smthSupport.forwardPostToExternalMail(post, value.toString()) ;
					if ( result ) 
						msg = "已转寄往信箱"+value.toString();
					else
						msg = "可耻滴失败鸟";
					Toast.makeText(getApplicationContext(),
							msg, Toast.LENGTH_SHORT).show();
				}
			}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// Do nothing.
				}
			}).show();
		} else {
			String msg = null;
			boolean result;

			if (group)
				result = m_smthSupport.forwardGroupPostToExternalMail(post, email) ;
			else 
				result = m_smthSupport.forwardPostToExternalMail(post, email) ;
			if ( result ) 
				msg = "已转寄往信箱"+email;
			else
				msg = "可耻滴失败鸟";
			Toast.makeText(getApplicationContext(),
					msg, Toast.LENGTH_SHORT).show();

		}
		
		return ;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (HomeActivity.m_application.isTouchScroll()) {
			int touchY = (int) e.getRawY();
			float scale = (float) (m_screenHeight / 800.0);
			if (touchY > 60 * scale && touchY < 390 * scale) {
				setListOffset(-1);
			} else if (touchY > 410 * scale && touchY < 740 * scale) {
				setListOffset(1);
			}
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		final int flingMinXDistance = 100, flingMaxYDistance = 100;
		if (e1.getX() - e2.getX() > flingMinXDistance) {
				//&& Math.abs(e1.getY() - e2.getY()) < flingMaxYDistance) {
			// Fling left
			Toast.makeText(this, "下一页", Toast.LENGTH_SHORT).show();
			m_nextButton.performClick();
		} else if (e2.getX() - e1.getX() > flingMinXDistance) {
				//&& Math.abs(e1.getY() - e2.getY()) < flingMaxYDistance) {
			// Fling right
			Toast.makeText(this, "上一页", Toast.LENGTH_SHORT).show();
			m_preButton.performClick();
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		
		if (changedPropertyName.equals(PostListViewModel.POSTLIST_PROPERTY_NAME)) {
			reloadPostList();
		}
		
	}

}
