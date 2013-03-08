package com.athena.asm.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.athena.asm.ActivityFragmentTargets;
import com.athena.asm.OnOpenActivityFragmentListener;
import com.athena.asm.PostListActivity;
import com.athena.asm.ProgressDialogProvider;
import com.athena.asm.R;
import com.athena.asm.WritePostActivity;
import com.athena.asm.aSMApplication;
import com.athena.asm.Adapter.PostListAdapter;
import com.athena.asm.data.Mail;
import com.athena.asm.data.Post;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.DeletePostTask;
import com.athena.asm.util.task.ForwardPostToMailTask;
import com.athena.asm.util.task.LoadPostTask;
import com.athena.asm.util.task.RefreshEvent;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.PostListViewModel;

public class PostListFragment extends SherlockFragment implements
		OnClickListener, OnTouchListener, OnLongClickListener,
		OnGestureListener, BaseViewModel.OnViewModelChangObserver, RefreshEvent {

	private LayoutInflater m_inflater;

	private PostListViewModel m_viewModel;

	EditText m_pageNoEditText;
	Button m_firstButton;
	Button m_lastButton;
	Button m_preButton;
	Button m_goButton;
	Button m_nextButton;
	
	private boolean m_isPageNoEditTextTouched = false;

	private int m_screenHeight;
	private ListView m_listView;

	private GestureDetector m_GestureDetector;

	private boolean m_isNewInstance = false;
	private boolean m_isFromReplyOrAt = false;

	private boolean m_isNewTouchStart = false;
	private float m_touchStartX = 0;
	private float m_touchStartY = 0;

	private float m_touchCurrentX = 0;
	private float m_touchCurrentY = 0;

	private int m_startNumber = 0;

	private String m_url = null;

	private ShareActionProvider m_actionProvider;
	
	private ProgressDialogProvider m_progressDialogProvider;
	private OnOpenActivityFragmentListener m_onOpenActivityFragmentListener;

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

		m_pageNoEditText = (EditText) postListView
				.findViewById(R.id.edittext_page_no);
		m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
		m_pageNoEditText.setOnClickListener(this);
		m_pageNoEditText.setOnTouchListener(this);
		m_pageNoEditText.setTextColor(Color.GRAY);

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

		// hide all buttons and edittext
		m_firstButton.setVisibility(View.GONE);
		m_lastButton.setVisibility(View.GONE);
		m_preButton.setVisibility(View.GONE);
		m_nextButton.setVisibility(View.GONE);
		m_goButton.setVisibility(View.GONE);
		m_pageNoEditText.setVisibility(View.GONE);

		m_listView = (ListView) postListView.findViewById(R.id.post_list);

		m_viewModel.setBoardType(getActivity().getIntent().getIntExtra(
				StringUtility.BOARD_TYPE, 0));
		m_viewModel.setIsToRefreshBoard(false);

		m_GestureDetector = new GestureDetector(this);

		return postListView;
	}

	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		boolean isNewSubject = false;
		
		Activity parentActivity = getSherlockActivity();
		if (parentActivity instanceof ProgressDialogProvider) {
			m_progressDialogProvider = (ProgressDialogProvider) parentActivity;
		}
		if (parentActivity instanceof OnOpenActivityFragmentListener) {
			m_onOpenActivityFragmentListener = (OnOpenActivityFragmentListener) parentActivity;
		}

		if (m_isNewInstance) {
			Subject newSubject = (Subject) getActivity().getIntent()
					.getSerializableExtra(StringUtility.SUBJECT);
			if (newSubject != null) {
				isNewSubject = m_viewModel.updateSubject(newSubject);
			} else {
				m_isFromReplyOrAt = true;
				Mail mail = (Mail) getActivity().getIntent()
						.getSerializableExtra(StringUtility.MAIL);
				if (mail.getBoxType() == 4) {
					m_url = "http://m.newsmth.net/refer/at/read?index="
							+ mail.getNumber();
				} else {
					m_url = "http://m.newsmth.net/refer/reply/read?index="
							+ mail.getNumber();
				}
			}

			m_isNewInstance = false;
		}

		if (isNewSubject) {
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
					m_viewModel.getCurrentSubject(), 0, false, false,
					m_startNumber, null);
			loadPostTask.execute();
			if (m_progressDialogProvider != null) {
				m_progressDialogProvider.showProgressDialog();
			}
		} else if (m_isFromReplyOrAt) {
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
					m_viewModel.getCurrentSubject(), 0, false, false,
					m_startNumber, m_url);
			loadPostTask.execute();
			if (m_progressDialogProvider != null) {
				m_progressDialogProvider.showProgressDialog();
			}
		} else {
			reloadPostList();
		}
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);

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

        if (m_actionProvider != null) {
            m_actionProvider.setShareIntent(createShareIntent());
        }

		m_listView.setAdapter(new PostListAdapter(this, m_inflater, m_viewModel
				.getPostList()));

		m_viewModel.updateCurrentPageNumberFromSubject();
		m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
		m_listView.requestFocus();

		m_viewModel.setIsPreloadFinished(false);
		m_viewModel.updatePreloadSubjectFromCurrentSubject();

		if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			m_firstButton.setText(R.string.first_page);
			m_lastButton.setText(R.string.last_page);
			m_preButton.setText(R.string.pre_page);
			m_nextButton.setText(R.string.next_page);
			m_goButton.setText(R.string.go_and_last_page);
			// show buttons
			m_goButton.setVisibility(View.VISIBLE);
			m_pageNoEditText.setVisibility(View.VISIBLE);
			m_firstButton.setVisibility(View.VISIBLE);
			m_preButton.setVisibility(View.VISIBLE);
			m_nextButton.setVisibility(View.VISIBLE);
		} else if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_NORMAL
				&& !m_isFromReplyOrAt) {
			m_firstButton.setText(R.string.topic_first_page);
			m_lastButton.setText(R.string.topic_all_page);
			m_preButton.setText(R.string.topic_pre_page);
			m_nextButton.setText(R.string.topic_next_page);
			// show buttons
			m_firstButton.setVisibility(View.VISIBLE);
			m_lastButton.setVisibility(View.VISIBLE);
			m_preButton.setVisibility(View.VISIBLE);
			m_nextButton.setVisibility(View.VISIBLE);
		} else {
			// do not show any button in BOARD_TYPE_DIGEST & BOARD_TYPE_MARK
		}

		// getActivity might return null in Fragment
		// http://stackoverflow.com/questions/11631408/android-fragment-getactivity-sometime-returns-null
		Activity act = getActivity();
		if (act != null)
			act.setTitle(m_viewModel.getSubjectTitle());

		if (m_viewModel.getBoardType() == 0) {
			int nextPage = m_viewModel.getNextPageNumber();
			if (nextPage > 0) {
				m_viewModel.getPreloadSubject().setCurrentPageNo(nextPage);
				LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
						m_viewModel.getPreloadSubject(), 0, true, false,
						m_startNumber, null);
				loadPostTask.execute();
			}
		} else {
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
					m_viewModel.getPreloadSubject(), 3, true, false,
					m_startNumber, null);
			loadPostTask.execute();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.edittext_page_no) {
			changePageNoEditStatus();
			return;
		}
		
		boolean isNext = false;
		if (m_viewModel.getBoardType() == 0) { // 同主题导航

			if (view.getId() == R.id.btn_first_page) {
				m_viewModel.gotoFirstPage();
			} else if (view.getId() == R.id.btn_last_page) {
				m_viewModel.gotoLastPage();
			} else if (view.getId() == R.id.btn_pre_page) {
				m_viewModel.gotoPrevPage();
			} else if (view.getId() == R.id.btn_go_page) {
				// 如果未按过编辑框，GO的功能为末页。否则为GO
				if (m_isPageNoEditTextTouched) {
					int pageSet = Integer.parseInt(m_pageNoEditText.getText()
							.toString());
					m_viewModel.setCurrentPageNumber(pageSet);
				} else {
					m_viewModel.gotoLastPage();
				}
				
			} else if (view.getId() == R.id.btn_next_page) {
				m_viewModel.gotoNextPage();
				isNext = true;
			}

			m_viewModel.updateSubjectCurrentPageNumberFromCurrentPageNumber();
			m_pageNoEditText.setText(m_viewModel.getCurrentPageNumber() + "");
			if (view.getParent() != null) {
				((View) view.getParent()).requestFocus();
			}

			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
					m_viewModel.getCurrentSubject(), 0, false, isNext,
					m_startNumber, null);
			loadPostTask.execute();
			if (m_progressDialogProvider != null) {
				m_progressDialogProvider.showProgressDialog();
			}
		} else {
			int action = 0;
			if (view.getId() == R.id.btn_first_page) {
				action = 1;
			} else if (view.getId() == R.id.btn_pre_page) {
				action = 2;
			} else if (view.getId() == R.id.btn_next_page) {
				action = 3;
				isNext = true;
			} else if (view.getId() == R.id.btn_last_page) {
				m_viewModel.setSubjectExpand(true);
				m_viewModel.setBoardType(0);
				m_startNumber = Integer.parseInt(m_viewModel
						.getCurrentSubject().getSubjectID());
				m_viewModel.updateSubjectIDFromTopicSubjectID();
				m_viewModel.setSubjectCurrentPageNumber(1);
			}
			LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
					m_viewModel.getCurrentSubject(), action, false, isNext,
					m_startNumber, null);
			loadPostTask.execute();
			if (m_progressDialogProvider != null) {
				m_progressDialogProvider.showProgressDialog();
			}
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {

		if (changedPropertyName
				.equals(PostListViewModel.POSTLIST_PROPERTY_NAME)) {
			reloadPostList();
			if (m_progressDialogProvider != null) {
				m_progressDialogProvider.dismissProgressDialog();
			}
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
		if (aSMApplication.getCurrentApplication().isTouchScroll()) {
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
			final Post post = ((PostListAdapter.ViewHolder) relativeLayout
					.getTag()).post;
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
			if (post.getAuthor().equals(m_viewModel.getSmthSupport().userid)) {
				itemList.add(getString(R.string.post_edit_post));
				itemList.add(getString(R.string.post_delete_post));
			}
			final String[] items = new String[itemList.size()];
			itemList.toArray(items);
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.post_alert_title);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 0:
						if (m_onOpenActivityFragmentListener != null) {
							Bundle bundle = new Bundle();
							bundle.putSerializable(StringUtility.URL,
												   "http://www.newsmth.net/bbspst.php?board="
												   + post.getBoard() + "&reid="
												   + post.getSubjectID());
							bundle.putSerializable(StringUtility.WRITE_TYPE, WritePostActivity.TYPE_POST);
							bundle.putSerializable(StringUtility.IS_REPLY, true);
							m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.WRITE_POST,
									  bundle);
						}
						break;
					case 1:
						if (m_onOpenActivityFragmentListener != null) {
							Bundle bundle = new Bundle();
							bundle.putSerializable(StringUtility.URL,
												   "http://www.newsmth.net/bbspstmail.php?board="
												   + post.getBoard() + "&id="
												   + post.getSubjectID());
							bundle.putSerializable(StringUtility.WRITE_TYPE, WritePostActivity.TYPE_MAIL);
							bundle.putSerializable(StringUtility.IS_REPLY, true);
							m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.WRITE_POST,
																					  bundle);
						}
						
						break;
					case 2:
						if (m_onOpenActivityFragmentListener != null) {
							Bundle bundle = new Bundle();
							bundle.putSerializable(StringUtility.USERID, authorID);
							m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.VIEW_PROFILE,
																					  bundle);
						}
						break;
					case 3:
						ClipboardManager clip = (ClipboardManager) getActivity()
								.getSystemService(Context.CLIPBOARD_SERVICE);
						clip.setText(authorID);
						Toast.makeText(getActivity(),
								"ID ： " + authorID + "已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 4:
						ClipboardManager clip2 = (ClipboardManager) getActivity()
								.getSystemService(Context.CLIPBOARD_SERVICE);
						clip2.setText(post.getTextContent());
						Toast.makeText(getActivity(), "帖子内容已复制到剪贴板",
								Toast.LENGTH_SHORT).show();
						break;
					case 5:
						ForwardPostToMailTask task = new ForwardPostToMailTask(
								getActivity(), m_viewModel, post,
								ForwardPostToMailTask.FORWARD_TO_SELF, "");
						task.execute();
						break;
					case 6:
						forwardToEmail(post, false);
						break;
					case 7:
						forwardToEmail(firstPost, true);
						break;
					case 8:
						if (m_onOpenActivityFragmentListener != null) {
							Bundle bundle = new Bundle();
							// http://www.newsmth.net/bbsedit.php?board=PocketLife&id=1408697&ftype=0
							String url = "http://www.newsmth.net/bbsedit.php?board="
									   + post.getBoard() + "&id=" + post.getSubjectID() + "&ftype=0";
							bundle.putSerializable(StringUtility.URL, url);
							// Log.d("TYPE_POST_EDIT read URL = ", url);

							bundle.putSerializable(StringUtility.WRITE_TYPE, WritePostActivity.TYPE_POST_EDIT);
							bundle.putSerializable(StringUtility.TITLE, post.getTitle().replace("主题:", ""));
							m_onOpenActivityFragmentListener.onOpenActivityOrFragment(ActivityFragmentTargets.WRITE_POST,
									  												  bundle);
						}
						break;
					case 9:
						// delete the post
						DeletePostTask deleteTask = new DeletePostTask(
								getActivity(), m_viewModel, post.getBoard(), post.getSubjectID(), PostListFragment.this);
						deleteTask.execute();
						// expect callback RefreshEvent.refresh()
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
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.edittext_page_no) {
			changePageNoEditStatus();
			return false;
		}
		
		boolean isConsumed = m_GestureDetector.onTouchEvent(event);
		if (event.getAction() == MotionEvent.ACTION_CANCEL
				|| event.getAction() == MotionEvent.ACTION_UP) {
			if (m_isNewTouchStart) {
				m_isNewTouchStart = false;
				final int flingMinXDistance = 100, flingMaxYDistance = 100;
				if (m_touchCurrentX - m_touchStartX > flingMinXDistance
						&& Math.abs(m_touchCurrentY - m_touchStartY) < flingMaxYDistance) {
					// Fling left
					Toast.makeText(getActivity(), "下一页", Toast.LENGTH_SHORT)
							.show();
					m_nextButton.performClick();
				} else if (m_touchStartX - m_touchCurrentX > flingMinXDistance
						&& Math.abs(m_touchStartY - m_touchCurrentY) < flingMaxYDistance) {
					// Fling right
					Toast.makeText(getActivity(), "上一页", Toast.LENGTH_SHORT)
							.show();
					m_preButton.performClick();
				}
			}

		}
		return isConsumed;
	}

	private void refreshPostList() {
		LoadPostTask loadPostTask = new LoadPostTask(m_viewModel,
				m_viewModel.getCurrentSubject(), 0, false, false,
				m_startNumber, null);
		loadPostTask.execute();
		if (m_progressDialogProvider != null) {
			m_progressDialogProvider.showProgressDialog();
		}
	}

	public static final int REFRESH_SUBJECTLIST = Menu.FIRST;
	public static final int GO_TO_BOARD = Menu.FIRST +1;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;
		((SherlockFragmentActivity) getActivity()).getSupportMenuInflater()
				.inflate(R.menu.share_action_provider, menu);

		menu.add(0, REFRESH_SUBJECTLIST, Menu.NONE, "刷新")
				.setIcon(
						isLight ? R.drawable.refresh_inverse
								: R.drawable.refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, GO_TO_BOARD, Menu.NONE, "返回版面")
		.setIcon( isLight ? R.drawable.toboard_inverse
				: R.drawable.toboard)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		MenuItem actionItem = menu
				.findItem(R.id.menu_item_share_action_provider_action_bar);
		m_actionProvider = (ShareActionProvider) actionItem.getActionProvider();
		m_actionProvider
				.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// super.onOptionsItemSelected(item);
		if (!m_isFromReplyOrAt) {
			switch (item.getItemId()) {
			case REFRESH_SUBJECTLIST:
				refreshPostList();
				break;
			case GO_TO_BOARD:
				if (getActivity() instanceof PostListActivity ){
					String boardEngName = m_viewModel.getCurrentSubject().getBoardEngName();
					String boardChsName = m_viewModel.getCurrentSubject().getBoardChsName(); 
					if(boardEngName != null){
						((PostListActivity)getActivity()).doFinishBackToBoard(boardEngName, boardChsName);
					}
				}
				break;
			}
		}

		return true;
	}

	/**
	 * Creates a sharing {@link Intent}.
	 * 
	 * @return The sharing intent.
	 */
	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		if (m_viewModel.getCurrentSubject() != null) {
			Subject subject = m_viewModel.getCurrentSubject();
			shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject.getTitle());
			if (m_isFromReplyOrAt && m_url != null) {
				shareIntent.putExtra(Intent.EXTRA_TEXT, m_url);
			} else if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_SUBJECT) {
				shareIntent.putExtra(
						Intent.EXTRA_TEXT,
						subject.getTitle() + " http://m.newsmth.net/article/"
								+ subject.getBoardEngName() + "/"
								+ subject.getSubjectID());
			} else if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_NORMAL) {
				shareIntent.putExtra(
						Intent.EXTRA_TEXT,
						subject.getTitle()
								+ "http://www.newsmth.net/bbscon.php?bid="
								+ subject.getBoardID() + "&id="
								+ subject.getSubjectID());
			} else if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_DIGEST) {
				shareIntent.putExtra(Intent.EXTRA_TEXT,
						subject.getTitle() + " http://m.newsmth.net/article/"
								+ subject.getBoardEngName() + "/single/"
								+ subject.getSubjectID() + "/1");
			} else if (m_viewModel.getBoardType() == SubjectListFragment.BOARD_TYPE_MARK) {
				shareIntent.putExtra(Intent.EXTRA_TEXT,
						subject.getTitle() + " http://m.newsmth.net/article/"
								+ subject.getBoardEngName() + "/single/"
								+ subject.getSubjectID() + "/3");
			}

		}
		return shareIntent;
	}
	
	private void changePageNoEditStatus() {
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			m_pageNoEditText.setTextColor(Color.WHITE);
		} else {
			m_pageNoEditText.setTextColor(Color.BLACK);
		}
		
		m_isPageNoEditTextTouched = true;
	}

	private void forwardToEmail(final Post post, final boolean group) {
		String email = aSMApplication.getCurrentApplication()
				.getForwardEmailAddr();
		if (email == "") {
			final EditText input = new EditText(getActivity());

			new AlertDialog.Builder(getActivity())
					.setTitle("设置转寄邮箱")
					.setMessage("您还没有设置转寄邮箱，请先设置。如需更改，请至设置页面")
					.setView(input)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Editable value = input.getText();
									aSMApplication.getCurrentApplication()
											.updateForwardEmailAddr(
													value.toString());

									ForwardPostToMailTask task;
									if (group)
										task = new ForwardPostToMailTask(
												getActivity(),
												m_viewModel,
												post,
												ForwardPostToMailTask.FORWARD_TO_EMAIL_GROUP,
												value.toString());
									else
										task = new ForwardPostToMailTask(
												getActivity(),
												m_viewModel,
												post,
												ForwardPostToMailTask.FORWARD_TO_EMAIL,
												value.toString());
									task.execute();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();
		} else {
			ForwardPostToMailTask task;
			if (group)
				task = new ForwardPostToMailTask(getActivity(), m_viewModel,
						post, ForwardPostToMailTask.FORWARD_TO_EMAIL_GROUP,
						email);
			else
				task = new ForwardPostToMailTask(getActivity(), m_viewModel,
						post, ForwardPostToMailTask.FORWARD_TO_EMAIL, email);
			task.execute();
		}

		return;
	}

	@Override
	public void refresh() {
		refreshPostList();
	}

	public boolean onKeyDown(int keyCode) {
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			setListOffset(1);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			setListOffset(-1);
			return true;
		}
		return false;
	}

}
