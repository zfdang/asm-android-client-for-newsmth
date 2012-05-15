package com.athena.asm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadPostTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.PostListViewModel;

public class PostListFragment extends Fragment
							  implements OnClickListener,
							  			 OnTouchListener,
										 OnLongClickListener, OnGestureListener,
										 BaseViewModel.OnViewModelChangObserver {

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
	
	private boolean m_isNewInstance = false;
	
	private boolean m_isNewTouchStart = false;
	private float m_touchStartX = 0;
	private float m_touchStartY = 0;
	
	private float m_touchCurrentX = 0;
	private float m_touchCurrentY = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		setRetainInstance(true);
		m_isNewInstance = true;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		m_inflater = inflater;
		View postListView = inflater.inflate(R.layout.post_list, null);

		aSMApplication application = (aSMApplication) getActivity()
				.getApplication();
		m_viewModel = application.getPostListViewModel();
		m_viewModel.registerViewModelChangeObserver(this);

		this.m_screenHeight = getActivity().getWindowManager()
				.getDefaultDisplay().getHeight();
		
		m_titleTextView = (TextView) postListView.findViewById(R.id.title);

		if (HomeActivity.m_application.isNightTheme()) {
			 ((LinearLayout) m_titleTextView.getParent().getParent())
			 .setBackgroundColor(getResources().getColor(R.color.body_background_night));
		}

		m_pageNoEditText = (EditText) postListView
				.findViewById(R.id.edittext_page_no);
		m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");

		m_firstButton = (Button) postListView.findViewById(R.id.btn_first_page);
		m_firstButton.setOnClickListener(this);
		m_lastButton = (Button) postListView.findViewById(R.id.btn_last_page);
		m_lastButton.setOnClickListener(this);
		m_preButton = (Button) postListView.findViewById(R.id.btn_pre_page);
		m_preButton.setOnClickListener(this);
		m_goButton = (Button) postListView.findViewById(R.id.btn_go_page);
		m_goButton.setOnClickListener(this);
		m_nextButton = (Button) postListView.findViewById(R.id.btn_next_page);
		m_nextButton.setOnClickListener(this);

		m_listView = (ListView) postListView.findViewById(R.id.post_list);

		m_viewModel.setBoardType(getActivity().getIntent().getIntExtra(StringUtility.BOARD_TYPE, 0));
		m_viewModel.setIsToRefreshBoard(false);

		m_GestureDetector = new GestureDetector(this);

		return postListView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		boolean isNewSubject = false;
		if (m_isNewInstance) {
			Subject newSubject = (Subject) getActivity().getIntent()
					.getSerializableExtra(StringUtility.SUBJECT);
			isNewSubject = m_viewModel.updateSubject(newSubject);
			m_isNewInstance = false;
		}
		
		if (isNewSubject) {
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel, m_viewModel.getCurrentSubject(),
					0, false, false);
			loadPostTask.execute();
			((PostListActivity)getActivity()).showProgressDialog();
		}
		else {
			reloadPostList();
		}
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
				LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
						m_viewModel.getPreloadSubject(), 0, true, false);
				loadPostTask.execute();
			}
		} else {
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel, m_viewModel.getPreloadSubject(),
					3, true, false);
			loadPostTask.execute();
		}
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

			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel, m_viewModel.getCurrentSubject(),
					0, false, isNext);
			loadPostTask.execute();
			((PostListActivity)getActivity()).showProgressDialog();
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
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel, m_viewModel.getCurrentSubject(),
					action, false, isNext);
			loadPostTask.execute();
			((PostListActivity)getActivity()).showProgressDialog();
		}
	}
	
	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {
		
		if (changedPropertyName.equals(PostListViewModel.POSTLIST_PROPERTY_NAME)) {
			reloadPostList();
			((PostListActivity)getActivity()).dismissProgressDialog();
		}
		
	}

	@Override
	public boolean onDown(MotionEvent e) {
		m_isNewTouchStart = true;
		m_touchStartX = e.getX();
		m_touchStartY = e.getY();
		m_touchCurrentX = m_touchStartX;
		m_touchCurrentY = m_touchStartY;
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		m_touchCurrentX += distanceX;
		m_touchCurrentY += distanceY;
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
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
	public boolean onLongClick(View v) {
		if (m_viewModel.getSmthSupport().getLoginStatus()) {
			RelativeLayout relativeLayout = null;
			if (v.getId() == R.id.PostContent) {
				relativeLayout = (RelativeLayout) v.getParent();
			} else {
				relativeLayout = (RelativeLayout) v;
			}
			final String authorID = (String) ((TextView) relativeLayout
					.findViewById(R.id.AuthorID)).getText();
			final Post post = ((PostListAdapter.ViewHolder)relativeLayout.getTag()).post;
			List<String> itemList = new ArrayList<String>();
			itemList.add(getString(R.string.post_reply_post));
			itemList.add(getString(R.string.post_reply_mail));
			itemList.add(getString(R.string.post_query_author));
			itemList.add(getString(R.string.post_copy_author));
			itemList.add(getString(R.string.post_copy_content));
			itemList.add(getString(R.string.post_foward_self));
			if (post.getAuthor().equals(m_viewModel.getSmthSupport().userid)) {
				itemList.add(getString(R.string.post_edit_post));
			}
			final String[] items = new String[itemList.size()];
			itemList.toArray(items);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
						ClipboardManager clip = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						clip.setText(authorID);
						Toast.makeText(getActivity(),
								"ID ： " + authorID + "已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						ClipboardManager clip2 = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						clip2.setText(post.getTextContent());
						Toast.makeText(getActivity(), "帖子内容已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 5:
						boolean result = m_viewModel.getSmthSupport().forwardPostToMailBox(post);
						if (result) {
							Toast.makeText(getActivity(),
									"已转寄到自己信箱中", Toast.LENGTH_SHORT).show();
						}
						break;
					case 6:
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		boolean isConsumed = m_GestureDetector.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_CANCEL ||
				event.getAction() == MotionEvent.ACTION_UP) {
			if (m_isNewTouchStart) {
				m_isNewTouchStart = false;
				final int flingMinXDistance = 100, flingMaxYDistance = 100;
				if (m_touchCurrentX - m_touchStartX > flingMinXDistance 
						&& Math.abs(m_touchCurrentY - m_touchStartY) < flingMaxYDistance) {
					// Fling left
					Toast.makeText(getActivity(), "下一页", Toast.LENGTH_SHORT).show();
					m_nextButton.performClick();
				} else if (m_touchStartX - m_touchCurrentX > flingMinXDistance 
						&& Math.abs(m_touchStartY - m_touchCurrentY) < flingMaxYDistance) {
					// Fling right
					Toast.makeText(getActivity(), "上一页", Toast.LENGTH_SHORT).show();
					m_preButton.performClick();
				}
			}
			
		}
		return isConsumed;
	}
	
	private void refreshPostList() {
		LoadPostTask loadPostTask = new LoadPostTask(m_viewModel, m_viewModel.getCurrentSubject(),
				0, false, false);
		loadPostTask.execute();
		((PostListActivity)getActivity()).showProgressDialog();
	}

	
}
