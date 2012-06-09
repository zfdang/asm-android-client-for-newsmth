package com.athena.asm;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.Adapter.TabsAdapter;
import com.athena.asm.fragment.CategoryFragment;
import com.athena.asm.fragment.FavoriteListFragment;
import com.athena.asm.fragment.GuidanceListFragment;
import com.athena.asm.fragment.MailFragment;
import com.athena.asm.fragment.ProfileFragment;
import com.athena.asm.service.CheckMessageService;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoginTask;
import com.athena.asm.viewmodel.HomeViewModel;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HomeActivity extends SherlockFragmentActivity {

	private HomeViewModel m_viewModel;

	public LayoutInflater m_inflater;

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
								: R.drawable.category), CategoryFragment.class,
				null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.mail_inverse : R.drawable.mail),
				MailFragment.class, null);
		m_tabsAdapter.addTab(
				actionBar.newTab().setIcon(
						isLight ? R.drawable.profile_inverse
								: R.drawable.profile), ProfileFragment.class,
				null);
		m_tabsAdapter.finishInit();
		
		m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		m_viewModel = m_application.getHomeViewModel();
		// m_viewModel.registerViewModelChangeObserver(this);
		m_viewModel.setCurrentTab(null); // since m_tabsAdapter.addTab will set
											// current tab

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
			finishAndClean();
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
		// m_viewModel.unregisterViewModelChangeObserver(this);
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
			finishAndClean();
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
		if (tab.equals(StringUtility.TAB_GUIDANCE)) {
			item = 0;
		} else if (tab.equals(StringUtility.TAB_FAVORITE)) {
			item = 1;
		} else if (tab.equals(StringUtility.TAB_CATEGORY)) {
			item = 2;
		} else if (tab.equals(StringUtility.TAB_MAIL)) {
			item = 3;
		} else {
			item = 4;
		}

		m_viewPager.setCurrentItem(item);

		CheckMessageService.schedule(this);
	}

	private void exit() {
		Boolean rememberUser = m_application.isRememberUser();
		if (!rememberUser) {
			m_application.updateAutoUserNameAndPassword("", "");
		}
		m_application.syncPreferences();

		finishAndClean();
	}
	
	private void finishAndClean() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(CheckMessageService.MESSAGE_NOTIFICATION_ID);
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private void logout(final boolean isToExit) {
		final ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setMessage("正在注销...");
		pdialog.show();
		CheckMessageService.unschedule(this);
		m_viewModel.clear();
//		final Context context = this;
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
					finishAndClean();
				} else {
//					Intent intent = new Intent(context, LogoutService.class);
//					PendingIntent pending = PendingIntent.getService(context,
//							0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//					Calendar c = new GregorianCalendar();
//					c.add(Calendar.SECOND, 2);
//
//					AlarmManager alarm = (AlarmManager) context
//							.getSystemService(Context.ALARM_SERVICE);
//					alarm.cancel(pending);
//					alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
//							pending);
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
				finishAndClean();
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
			int index = m_viewPager.getCurrentItem();
			switch (index) {
			case 0:
				m_viewModel.setGuidanceSectionNames(null);
				m_viewModel.setGuidanceSectionDetails(null);
				m_viewModel.notifyGuidanceChanged();
				break;
			case 1:
				boolean isDeleted = deleteFile("FavList");
				if (isDeleted) {
					m_viewModel.setFavList(null);
					m_viewModel.notifyFavListChanged();
				}
				break;
			case 2:
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
									m_viewModel.notifyCategoryChanged();
								}

							}
						});
				builder.setNegativeButton("取消", null);
				builder.create().show();
				break;
			case 3:
				m_viewModel.setMailbox(null);
				m_viewModel.notifyMailboxChanged();
				break;
			case 4:
				m_viewModel.setCurrentProfile(null);
				m_viewModel.notifyProfileChanged(null);
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
}
