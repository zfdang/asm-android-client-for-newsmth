package com.athena.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.Adapter.CategoryListAdapter;
import com.athena.asm.Adapter.FavoriteListAdapter;
import com.athena.asm.Adapter.GuidanceListAdapter;
import com.athena.asm.Adapter.MailAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Mail;
import com.athena.asm.data.MailBox;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadCategoryTask;
import com.athena.asm.util.task.LoadFavoriteTask;
import com.athena.asm.util.task.LoadGuidanceTask;
import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoginTask;
//import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoadProfileTask;


public class HomeActivity extends Activity implements OnClickListener {

	public List<String> guidanceSectionNames = null;
	public List<List<Subject>> guidanceSectionDetails = null;
	public List<Board> favList = null;
	public MailBox mailBox = null;
	public List<Mail> mailList = null;
	public List<Board> categoryList = null;
	public List<String> boardFullStrings = null;
	public HashMap<String, Board> boardHashMap = null;

	public Profile currentProfile = null;

	public SmthSupport smthSupport;

	public LayoutInflater inflater;
	private LinearLayout bodyContainer;
	private TextView titleTextView;

	private TextView btnGuidance;
	private TextView btnFavorite;
	private TextView btnMail;
	private TextView btnCategory;
	private TextView btnProfile;

	/*
	 * private LoadGuidanceTask loadGuidanceTask; private LoadFavoriteTask
	 * loadFavoriteTask; private LoadMailTask loadMailTask; private
	 * LoadCategoryTask loadCategoryTask; private LoadProfileTask
	 * loadProfileTask;
	 */

	private ArrayList<View> cacheViewStack = new ArrayList<View>();
	private double currentTabIndex = 0;

	public String loginUserID = "guest";
	private boolean isLogined = false;
	private boolean isGuestLogined = false;

	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);

		smthSupport = SmthSupport.getInstance();

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		bodyContainer = (LinearLayout) findViewById(R.id.bodyContainer);
		titleTextView = (TextView) findViewById(R.id.title);
		
		aSMApplication application = (aSMApplication) getApplication();
		application.initPreferences();
		boolean isAutoLogin = application.isAutoLogin();
		
		if (this.getIntent().getExtras() != null) {
			this.isLogined = (Boolean) this.getIntent().getExtras().get(StringUtility.LOGINED);
			this.isGuestLogined = (Boolean) this.getIntent().getExtras().get(StringUtility.GUEST_LOGINED);
		}
		
		// 如果已从login页面登陆过来
		if (isLogined) {
			loginUserID = (String) this.getIntent().getExtras().get(StringUtility.LOGINED_ID);
			init();
		}
		// 如果是从login页面用guest登陆过来
		else if (isGuestLogined) {
			init();
		}
		// 如果是第一次启动且保存了自动登陆
		else if (isAutoLogin) {
			smthSupport.restore();
			
			String userName = application.getAutoUserName();
			String password = application.getAutoPassword();
			
			LoginTask loginTask = new LoginTask(this, userName, password);
			loginTask.execute();
		}
		// 如果是第一次启动且没有自动登陆
		else {
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm","com.athena.asm.LoginActivity");
			startActivity(intent);
			finish();
		}
	}
	
	public void showFailedToast() {
		handler.post(new Runnable() {
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
			intent.setClassName("com.athena.asm","com.athena.asm.LoginActivity");
			startActivity(intent);
			finish();
		} else {
			isLogined = true;
			aSMApplication application = (aSMApplication) getApplication();
			loginUserID = application.getAutoUserName();
			init();
		}
	}
	
	private void init() {
		initTabListeners();
		// initTasks();

		aSMApplication application = (aSMApplication) getApplication();
		String defaultTab = application.getDefaultTab();
		if (defaultTab.equals("001")) {
			reloadGuidanceList();
		} else if (defaultTab.equals("002")) {
			reloadFavorite(favList, 20);
		} else if (defaultTab.equals("003")) {
			reloadCategory(categoryList, 30);
		} else if (defaultTab.equals("004")) {
			reloadMail();
		} else {
			reloadProfile(currentProfile, 50);
		}
	}

	private void initTabListeners() {
		btnGuidance = (TextView) findViewById(R.id.footer_btn_guidance);
		btnFavorite = (TextView) findViewById(R.id.footer_btn_favorite);
		btnMail = (TextView) findViewById(R.id.footer_btn_mail);
		btnCategory = (TextView) findViewById(R.id.footer_btn_category);
		btnProfile = (TextView) findViewById(R.id.footer_btn_profile);

		btnGuidance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadGuidanceList();
			}
		});

		btnFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadFavorite(favList, 20);
			}
		});

		btnCategory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadCategory(categoryList, 30);
			}
		});

		btnMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadMail();
			}
		});

		btnProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				reloadProfile(currentProfile, 50);
			}
		});
	}

	/*
	 * private void initTasks() { loadGuidanceTask = new LoadGuidanceTask(this);
	 * loadFavoriteTask = new LoadFavoriteTask(this); loadMailTask = new
	 * LoadMailTask(this); loadCategoryTask = new LoadCategoryTask(this);
	 * loadProfileTask = new LoadProfileTask(this, loginUserID, 50); }
	 */

	private void switchToView(View targetView, double targetIndex) {
		if (currentTabIndex == targetIndex) {
			return;
		}
		/*
		 * else if (currentTabIndex > targetIndex) { if
		 * (bodyContainer.getChildCount() > 0) {
		 * bodyContainer.getChildAt(0).setAnimation
		 * (AnimationUtils.loadAnimation(HomeActivity.this,
		 * R.anim.push_right_out)); }
		 * targetView.setAnimation(AnimationUtils.loadAnimation
		 * (HomeActivity.this, R.anim.push_left_in)); } else { if
		 * (bodyContainer.getChildCount() > 0) {
		 * bodyContainer.getChildAt(0).setAnimation
		 * (AnimationUtils.loadAnimation(HomeActivity.this,
		 * R.anim.push_left_out)); }
		 * targetView.setAnimation(AnimationUtils.loadAnimation
		 * (HomeActivity.this, R.anim.push_right_in)); }
		 */

		// cache标准：同tab下层级cache，切tab清空cache重置为首页
		int currentTabNumber = (int) (currentTabIndex / 10);
		int targetTabNumber = (int) (targetIndex / 10);
		if (currentTabNumber == targetTabNumber) {
			if (targetIndex > currentTabIndex
					&& bodyContainer.getChildCount() > 0) {
				cacheViewStack.add(bodyContainer.getChildAt(0));
			}
		} else {
			cacheViewStack.clear();
		}

		currentTabIndex = targetIndex;

		bodyContainer.removeAllViews();
		bodyContainer.addView(targetView);
	}

	public void reloadGuidanceList() {
		if (guidanceSectionNames == null || guidanceSectionDetails == null) {
			LoadGuidanceTask loadGuidanceTask = new LoadGuidanceTask(this);
			loadGuidanceTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.guidance, null);
			ListView listView = (ListView) layout
			.findViewById(R.id.guidance_list);
			listView.setAdapter(new GuidanceListAdapter(this, 0, 0,
					guidanceSectionNames, guidanceSectionDetails));
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.guidance, null);
					ListView listView = (ListView) layout
					.findViewById(R.id.guidance_list);
					listView.setAdapter(new GuidanceListAdapter(
							HomeActivity.this, 1, position,
							guidanceSectionNames, guidanceSectionDetails));
					switchToView(listView, 11);
				}
			});
			// ExpandableListView listView = (ExpandableListView)
			// layout.findViewById(R.id.guidance_list);
			// listView.setAdapter(new GuidanceListAdapter(this,
			// guidanceSectionNames, guidanceSectionDetails));
			titleTextView.setText(R.string.title_guidance);
			switchToView(listView, 10);
		}
	}

	public void reloadFavorite(List<Board> boardList, int step) {
		if (favList == null) {
			LoadFavoriteTask loadFavoriteTask = new LoadFavoriteTask(this);
			loadFavoriteTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.favorite, null);
			ListView listView = (ListView) layout
			.findViewById(R.id.favorite_list);
			listView.setAdapter(new FavoriteListAdapter(this, boardList, step));

			titleTextView.setText(R.string.title_favorite);
			switchToView(listView, step);
		}
	}

	public void reloadCategory(List<Board> boardList, int step) {
		if (categoryList == null) {
			LoadCategoryTask loadCategoryTask = new LoadCategoryTask(this);
			loadCategoryTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.category, null);
			ListView listView = (ListView) layout
			.findViewById(R.id.category_list);
			listView.setAdapter(new CategoryListAdapter(this, boardList, step));

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
					boardFullStrings);
			textView.setAdapter(adapter);

			titleTextView.setText(R.string.title_category);
			switchToView(layout, step);
		}
	}

	public void loadMail() {
		View layout = inflater.inflate(R.layout.mail, null);
		ListView listView = (ListView) layout.findViewById(R.id.mail_list);
		listView.setAdapter(new MailAdapter(this, mailBox));

		titleTextView.setText(R.string.title_mail);
		switchToView(listView, 40);
	}

	public void reloadMail() {
		if (isLogined) {
			LoadMailTask loadMailTask = new LoadMailTask(this);
			loadMailTask.execute();
		} else {
			Toast.makeText(getApplicationContext(), "请登陆后再使用.",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void reloadProfile(Profile profile, final int step) {
		if (profile == null) {
			LoadProfileTask loadProfileTask = new LoadProfileTask(this,
					loginUserID, 50);
			loadProfileTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.profile, null);
			
			RelativeLayout relativeLayout = (RelativeLayout) layout.findViewById(R.id.headerLinearLayout);
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
								HomeActivity.this, idString, step);
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
			}
			else {
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

			titleTextView.setText(R.string.title_profile);
			switchToView(layout, step);
		}
	}

	private void clearData() {
		guidanceSectionDetails = null;
		guidanceSectionNames = null;
		favList = null;
		categoryList = null;
		currentProfile = null;
		inflater = null;
		cacheViewStack.clear();
	}
	
	private void exit() {
		aSMApplication application = (aSMApplication) getApplication();
		Boolean rememberUser = application.isRememberUser();
		if (!rememberUser) {
			application.updateAutoUserNameAndPassword("", "");
		}
		application.syncPreferences();

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
				smthSupport.destory();
				pdialog.cancel();
				if (!isToExit) {
					Intent intent = new Intent();
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra(StringUtility.LOGOUT, true);
					intent.setClassName("com.athena.asm",
					"com.athena.asm.LoginActivity");
					startActivity(intent);
					finish();
				}
				else {
					exit();
				}
			}
		};
		th.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (cacheViewStack.size() == 0) {
				if (isLogined) {
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
					//return super.onKeyDown(keyCode, event);
				}
			} else if (cacheViewStack.size() > 0) {
				View lastView = cacheViewStack.get(cacheViewStack.size() - 1);
				cacheViewStack.remove(cacheViewStack.size() - 1);
				switchToView(lastView, currentTabIndex - 1);
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public static final int SETTING = Menu.FIRST;
	public static final int REFRESH = Menu.FIRST + 1;
	public static final int ABOUT = Menu.FIRST + 2;
	public static final int LOGOUT = Menu.FIRST + 3;
	public static final int EXIT = Menu.FIRST + 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SETTING, Menu.NONE, "设置");
		menu.add(0, REFRESH, Menu.NONE, "刷新");
		menu.add(0, ABOUT, Menu.NONE, "关于");
		menu.add(0, LOGOUT, Menu.NONE, "注销");
		menu.add(0, EXIT, Menu.NONE, "退出");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case SETTING:
			Intent intent = new Intent();
			intent.setClassName("com.athena.asm",
			"com.athena.asm.SettingActivity");
			startActivity(intent);
			break;
		case REFRESH:
			int index = (int) (currentTabIndex / 10);
			switch (index) {
			case 1:
				guidanceSectionNames = null;
				reloadGuidanceList();
				break;
			case 2:
				favList = null;
				reloadFavorite(favList, 20);
				break;
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
									categoryList = null;
									reloadCategory(categoryList, 30);
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
				currentProfile = null;
				reloadProfile(currentProfile, 50);
				break;
			default:
				break;
			}
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
			if (isLogined) {
				logout(true);
			}
			else {
				smthSupport.destory();
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

			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);

			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.BOARD,
					boardHashMap.get(textView.getText().toString()));
			intent.putExtras(bundle);
			intent.setClassName("com.athena.asm",
			"com.athena.asm.SubjectListActivity");
			this.startActivity(intent);
		}
	}
}
