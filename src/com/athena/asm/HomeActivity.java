package com.athena.asm;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.Adapter.CategoryListAdapter;
import com.athena.asm.Adapter.MailAdapter;
import com.athena.asm.Adapter.TabsAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Profile;
import com.athena.asm.fragment.FavoriteListFragment;
import com.athena.asm.fragment.GuidanceListFragment;
import com.athena.asm.fragment.TestListFragment;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadCategoryTask;
import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoadProfileTask;
import com.athena.asm.util.task.LoginTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HomeActivity extends SherlockFragmentActivity implements
		OnClickListener, BaseViewModel.OnViewModelChangObserver {

	private HomeViewModel m_viewModel;

	public LayoutInflater m_inflater;

	private double m_currentTabIndex = 0;

	private Handler m_handler = new Handler();

	public static aSMApplication m_application;

	public static int THEME = R.style.Theme_Sherlock;

	ViewPager m_viewPager;
	TabsAdapter m_tabsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		m_application = (aSMApplication) getApplication();
		m_application.initPreferences();

		if (HomeActivity.m_application.isNightTheme()) {
			THEME = R.style.Theme_Sherlock;
		} else {
			THEME = R.style.Theme_Sherlock_Light;
		}
		setTheme(THEME);

		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		// setContentView(R.layout.home);

		m_viewPager = new ViewPager(this);
		m_viewPager.setId(R.id.homePager);
		setContentView(m_viewPager);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		boolean isLight = THEME == R.style.Theme_Sherlock_Light;

		m_tabsAdapter = new TabsAdapter(this, m_viewPager);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.home_inverse : R.drawable.home),
				GuidanceListFragment.class, null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.favorite_inverse
								: R.drawable.favorite),
				FavoriteListFragment.class, null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.category_inverse
								: R.drawable.category), TestListFragment.class,
				null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.mail_inverse : R.drawable.mail),
				TestListFragment.class, null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.profile_inverse
								: R.drawable.profile), TestListFragment.class,
				null);

		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_viewModel = m_application.getHomeViewModel();
		m_viewModel.registerViewModelChangeObserver(this);
		m_viewModel.setCurrentTab(null); // since m_tabsAdapter.addTab will set current tab

		boolean isAutoLogin = m_application.isAutoLogin();

		m_viewModel.updateLoginStatus();
		if (this.getIntent().getExtras() != null) {
			m_viewModel.setLoggedin((Boolean) this.getIntent().getExtras()
					.get(StringUtility.LOGINED));
			m_viewModel.setGuestLogined((Boolean) this.getIntent().getExtras()
					.get(StringUtility.GUEST_LOGINED));
		}

		// 如果已从login页面登陆过来
		if (m_viewModel.isLogined()) {
			init();
		}
		// 如果是从login页面用guest登陆过来
		else if (m_viewModel.isGuestLogined()) {
			init();
		}
		// 如果是第一次启动且保存了自动登陆
		else if (isAutoLogin) {
			m_viewModel.restorSmthSupport();

			String userName = m_application.getAutoUserName();
			String password = m_application.getAutoPassword();

			LoginTask loginTask = new LoginTask(this, m_viewModel, userName,
					password);
			loginTask.execute();
		}
		// 如果是第一次启动且没有自动登陆
		else {
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.LoginActivity");
			startActivity(intent);
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onDestroy() {
		m_viewModel.unregisterViewModelChangeObserver(this);

		super.onDestroy();
	}

	public void showFailedToast() {
		m_handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), "用户名或密码错.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void loginTaskDone(boolean result) {
		if (!result) {
			showFailedToast();

			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.LoginActivity");
			startActivity(intent);
			finish();
		} else {
			m_viewModel.updateLoginStatus();
			init();
		}
	}

	private void init() {
		// initTasks();
		if (m_application.isFirstLaunchAfterUpdate()) {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setTitle(R.string.update_title);
			alertBuilder.setMessage(R.string.update_info);
			alertBuilder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i) {
							dialog.dismiss();
						}
					});
			alertBuilder.show();
		}

		String tab = m_viewModel.getCurrentTab() == null ? m_application
				.getDefaultTab() : m_viewModel.getCurrentTab();
		m_viewModel.setCurrentTab(tab);
		
		int item = 0;
		if (tab.equals("001")) {
			item = 0;
		} else if (tab.equals("002")) {
			item = 1;
		} else if (tab.equals("003")) {
			item = 2;
		} else if (tab.equals("004")) {
			item = 3;
		} else {
			item = 4;
		}

		m_viewPager.setCurrentItem(item);
	}

	public void reloadCategory(final List<Board> boardList, int step) {
		if (boardList == null) {
			LoadCategoryTask loadCategoryTask = new LoadCategoryTask(this,
					m_viewModel);
			loadCategoryTask.execute();
		} else {
			View layout = m_inflater.inflate(R.layout.category, null);
			ListView listView = (ListView) layout
					.findViewById(R.id.category_list);
			final CategoryListAdapter categoryListAdapter = new CategoryListAdapter(
					this, boardList, step);
			listView.setAdapter(categoryListAdapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					Board board = boardList.get(position);
					if (board.isDirectory()) {
						categoryListAdapter.step++;
						reloadCategory(
								((Board) view.getTag()).getChildBoards(),
								categoryListAdapter.step);
					} else {
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putSerializable(StringUtility.BOARD,
								(Board) view.getTag());
						intent.putExtras(bundle);
						intent.setClassName("com.athena.asm",
								"com.athena.asm.SubjectListActivity");
						startActivity(intent);
					}
				}
			});

			RelativeLayout relativeLayout = (RelativeLayout) layout
					.findViewById(R.id.board_relative_layout);

			Button goButton = (Button) relativeLayout
					.findViewById(R.id.btn_go_board);
			goButton.setOnClickListener(this);

			AutoCompleteTextView textView = (AutoCompleteTextView) relativeLayout
					.findViewById(R.id.search_board);
			textView.setCompletionHint("请输入版面英文名");
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line,
					m_viewModel.getBoardFullStrings());
			textView.setAdapter(adapter);
		}
	}

	public void loadMail() {
		View layout = m_inflater.inflate(R.layout.mail, null);
		ListView listView = (ListView) layout.findViewById(R.id.mail_list);
		listView.setAdapter(new MailAdapter(this, m_viewModel.getMailBox()));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					final int position, long id) {
				if (position == 0 || position == 1 || position == 2) {
					final int boxType = position;
					Intent intent = new Intent();
					intent.putExtra(StringUtility.MAIL_BOX_TYPE, boxType);
					intent.setClassName("com.athena.asm",
							"com.athena.asm.MailListActivity");
					startActivity(intent);
				} else if (position == 3) {
					Intent intent = new Intent();
					intent.setClassName("com.athena.asm",
							"com.athena.asm.WritePostActivity");
					intent.putExtra(StringUtility.URL,
							"http://www.newsmth.net/bbspstmail.php");
					intent.putExtra(StringUtility.WRITE_TYPE, 1);
					intent.putExtra(StringUtility.IS_REPLY, false);
					startActivity(intent);
				}
			}
		});

	}

	public void reloadMail() {
		if (m_viewModel.isLogined()) {
			LoadMailTask loadMailTask = new LoadMailTask(this, m_viewModel);
			loadMailTask.execute();
		} else {
			Toast.makeText(getApplicationContext(), "请登陆后再使用.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void reloadProfile(Profile profile, final int step) {
		if (profile == null) {
			LoadProfileTask loadProfileTask = new LoadProfileTask(this,
					m_viewModel, m_viewModel.getLoginUserID(), 50);
			loadProfileTask.execute();
		} else {
			View layout = m_inflater.inflate(R.layout.profile, null);

			RelativeLayout relativeLayout = (RelativeLayout) layout
					.findViewById(R.id.headerLinearLayout);
			relativeLayout.setVisibility(View.GONE);

			ImageButton searchButton = (ImageButton) layout
					.findViewById(R.id.btn_search);
			searchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					RelativeLayout relativeLayout = (RelativeLayout) v
							.getParent();
					EditText searchEditText = (EditText) relativeLayout
							.findViewById(R.id.search_edit);
					((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(
									searchEditText.getWindowToken(), 0);
					String idString = searchEditText.getText().toString()
							.trim();
					if (idString.length() > 0) {
						LoadProfileTask profileTask = new LoadProfileTask(
								HomeActivity.this, m_viewModel, idString, step);
						profileTask.execute();
					}

				}
			});

			TextView userIDTextView = (TextView) layout
					.findViewById(R.id.profile_userid);
			userIDTextView.setText(profile.getUserID());

			TextView userScoreTextView = (TextView) layout
					.findViewById(R.id.profile_user_score);
			if (profile.getScore() != 0) {
				userScoreTextView.setText("积分：" + profile.getScore());
			} else {
				userScoreTextView.setVisibility(View.GONE);
			}

			TextView userNicknameTextView = (TextView) layout
					.findViewById(R.id.profile_user_nickname);
			userNicknameTextView.setText(profile.getNickName());

			TextView descTextView = (TextView) layout
					.findViewById(R.id.profile_user_desc);
			descTextView.setText(Html.fromHtml(profile.getDescription()));

			TextView aliveTextView = (TextView) layout
					.findViewById(R.id.profile_aliveness);
			aliveTextView.setText(profile.getAliveness() + "");
			TextView loginedTimeTextView = (TextView) layout
					.findViewById(R.id.profile_login_times);
			loginedTimeTextView.setText(profile.getLoginTime() + "");
			TextView postNoTextView = (TextView) layout
					.findViewById(R.id.profile_post_number);
			postNoTextView.setText(profile.getPostNumber() + "");
			TextView onlineTextView = (TextView) layout
					.findViewById(R.id.profile_online_status);
			switch (profile.getOnlineStatus()) {
			case 0:
				onlineTextView.setText("离线");
				break;
			case 1:
				onlineTextView.setText("不明");
				break;
			case 2:
				onlineTextView.setText("在线");
				break;

			default:
				break;
			}

			if (HomeActivity.m_application.isNightTheme()) {
				userIDTextView.setTextColor(layout.getResources().getColor(
						R.color.blue_text_night));
				userScoreTextView.setTextColor(layout.getResources().getColor(
						R.color.blue_text_night));
				userNicknameTextView.setTextColor(layout.getResources()
						.getColor(R.color.blue_text_night));
			}

		}
	}

	private void clearData() {
		m_viewModel.clear();
		m_inflater = null;
	}

	private void exit() {
		Boolean rememberUser = m_application.isRememberUser();
		if (!rememberUser) {
			m_application.updateAutoUserNameAndPassword("", "");
		}
		m_application.syncPreferences();

		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void logout(final boolean isToExit) {
		final ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setMessage("正在注销...");
		pdialog.show();
		clearData();
		Thread th = new Thread() {
			@Override
			public void run() {
				m_viewModel.logout();
				pdialog.cancel();
				if (!isToExit) {
					Intent intent = new Intent();
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(StringUtility.LOGOUT, true);
					intent.setClassName("com.athena.asm",
							"com.athena.asm.LoginActivity");
					startActivity(intent);
					finish();
				} else {
					exit();
				}
			}
		};
		th.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (m_viewModel.isLogined()) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("确认要注销退出吗？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								logout(true);
							}
						});
				builder.setNegativeButton("取消", null);
				builder.create().show();
				// logout();
			} else {
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public static final int SETTING = Menu.FIRST;
	public static final int REFRESH = Menu.FIRST + 1;
	public static final int CLEAN = Menu.FIRST + 2;
	public static final int ABOUT = Menu.FIRST + 3;
	public static final int LOGOUT = Menu.FIRST + 4;
	public static final int EXIT = Menu.FIRST + 5;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// super.onCreateOptionsMenu(menu);
		boolean isLight = THEME == R.style.Theme_Sherlock_Light;

		menu.add(0, CLEAN, Menu.NONE, "清除缓存")
				.setIcon(isLight ? R.drawable.clean_inverse : R.drawable.clean)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, SETTING, Menu.NONE, "设置")
				.setIcon(
						isLight ? R.drawable.setting_inverse
								: R.drawable.setting)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, REFRESH, Menu.NONE, "刷新")
				.setIcon(
						isLight ? R.drawable.refresh_inverse
								: R.drawable.refresh)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, ABOUT, Menu.NONE, "关于")
				.setIcon(isLight ? R.drawable.about_inverse : R.drawable.about)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(0, LOGOUT, Menu.NONE, "注销").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		menu.add(0, EXIT, Menu.NONE, "退出").setShowAsAction(
				MenuItem.SHOW_AS_ACTION_NEVER
						| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case SETTING:
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
					"com.athena.asm.SettingActivity");
			startActivity(intent);
			break;
		case REFRESH:
			int index = (int) (m_currentTabIndex / 10);
			switch (index) {
			case 3:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("耗时较长，确定吗？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								boolean isDeleted = deleteFile("CategoryList");
								if (isDeleted) {
									m_viewModel.setCategoryList(null);
									reloadCategory(
											m_viewModel.getCategoryList(), 30);
								}

							}
						});
				builder.setNegativeButton("取消", null);
				builder.create().show();
				break;
			case 4:
				reloadMail();
				break;
			case 5:
				m_viewModel.setCurrentProfile(null);
				reloadProfile(m_viewModel.getCurrentProfile(), 50);
				break;
			default:
				break;
			}
			break;
		case CLEAN:
			UrlImageViewHelper.cleanup(this);
			Toast.makeText(getApplicationContext(), "已清空图片缓存",
					Toast.LENGTH_SHORT).show();
			break;
		case ABOUT:
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
			alertBuilder.setTitle(R.string.about_title);
			// TextView message = new TextView(this);
			// message.setText(Html.fromHtml(getString(R.string.about_content)));
			// alertBuilder.setView(message);
			alertBuilder.setMessage(R.string.about_content);
			alertBuilder.setPositiveButton("确认",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int i) {
							dialog.dismiss();
						}
					});
			alertBuilder.show();
			break;
		case LOGOUT:
			logout(false);
			break;
		case EXIT:
			if (m_viewModel.isLogined()) {
				logout(true);
			} else {
				m_viewModel.logout();
				exit();
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_go_board) {
			AutoCompleteTextView textView = (AutoCompleteTextView) ((RelativeLayout) view
					.getParent()).findViewById(R.id.search_board);

			Board board = m_viewModel.getBoardHashMap().get(
					textView.getText().toString().toLowerCase());

			if (board == null) {
				m_handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(), "版面不存在.",
								Toast.LENGTH_SHORT).show();
					}
				});
				return;
			}

			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);

			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.BOARD, board);
			intent.putExtras(bundle);
			intent.setClassName("com.athena.asm",
					"com.athena.asm.SubjectListActivity");
			this.startActivity(intent);
		}
	}

	@Override
	public void onViewModelChange(BaseViewModel viewModel,
			String changedPropertyName, Object... params) {

		if (changedPropertyName
				.equals(HomeViewModel.CATEGORYLIST_PROPERTY_NAME)) {
			reloadCategory(m_viewModel.getCategoryList(), 30);
		} else if (changedPropertyName
				.equals(HomeViewModel.MAILBOX_PROPERTY_NAME)) {
			loadMail();
		} else if (changedPropertyName
				.equals(HomeViewModel.PROFILE_PROPERTY_NAME)) {
			Profile profile = (Profile) params[0];
			int step = (Integer) params[1];

			reloadProfile(profile, step);
		}
//		} else if (changedPropertyName
//				.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
//			String tab = m_viewModel.getCurrentTab();
//
//			// TODO: find a better place to do this...
//			if (!tab.equals("004")) {
//				m_application.getMailViewModel().clear();
//			}
//
//			if (tab.equals("001")) {
//				// reloadGuidanceList();
//			} else if (tab.equals("002")) {
//				// reloadFavorite(m_viewModel.getFavList(), 20);
//			} else if (tab.equals("003")) {
//				reloadCategory(m_viewModel.getCategoryList(), 30);
//			} else if (tab.equals("004")) {
//				reloadMail();
//			} else {
//				reloadProfile(m_viewModel.getCurrentProfile(), 50);
//			}
//		}

	}
}
