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

import com.athena.asm.Adapter.CategoryListAdapter;
import com.athena.asm.Adapter.FavoriteListAdapter;
import com.athena.asm.Adapter.GuidanceListAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadCategoryTask;
import com.athena.asm.util.task.LoadFavoriteTask;
import com.athena.asm.util.task.LoadGuidanceTask;
//import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoadProfileTask;

public class HomeActivity extends Activity implements OnClickListener {

	public List<String> guidanceSectionNames = null;
	public List<List<Subject>> guidanceSectionDetails = null;
	public List<Board> favList = null;
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

	private LoadGuidanceTask loadGuidanceTask;
	private LoadFavoriteTask loadFavoriteTask;
	//private LoadMailTask loadMailTask;
	private LoadCategoryTask loadCategoryTask;
	private LoadProfileTask loadProfileTask;

	private ArrayList<View> cacheViewStack = new ArrayList<View>();
	private double currentTabIndex = 0;

	public String loginUserID = "guest";
	private boolean isLogined = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);

		this.isLogined = (Boolean) this.getIntent().getExtras()
				.get(StringUtility.LOGINED);
		if (isLogined) {
			loginUserID = (String) this.getIntent().getExtras()
					.get(StringUtility.LOGINED_ID);
		}

		inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		smthSupport = SmthSupport.getInstance();

		bodyContainer = (LinearLayout) findViewById(R.id.bodyContainer);
		titleTextView = (TextView) findViewById(R.id.title);

		initTabListeners();
		initTasks();

		aSMApplication application = (aSMApplication)getApplication();
		String defaultTab = application.getDefaultTab();
		if (defaultTab.equals("001")) {
			reloadGuidanceList();
		} else if (defaultTab.equals("002")) {
			reloadFavorite(favList, 20);
		} else if (defaultTab.equals("003")) {
			reloadCategory(categoryList, 30);
		}else {
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

		btnMail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadMail();
			}
		});

		btnCategory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reloadCategory(categoryList, 40);
			}
		});

		btnProfile.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				reloadProfile(currentProfile, 50);
			}
		});
	}

	private void initTasks() {
		loadGuidanceTask = new LoadGuidanceTask(this);
		loadFavoriteTask = new LoadFavoriteTask(this);
		//loadMailTask = new LoadMailTask(this);
		loadCategoryTask = new LoadCategoryTask(this);
		loadProfileTask = new LoadProfileTask(this, loginUserID, 50);
	}

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
			loadGuidanceTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.guidance, null);
			ListView listView = (ListView) layout
					.findViewById(R.id.guidance_list);
			listView.setAdapter(new GuidanceListAdapter(this, 0, 0,
					guidanceSectionNames, guidanceSectionDetails));
			listView.setOnItemClickListener(new OnItemClickListener() {
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

	public void reloadMail() {

	}

	public void reloadCategory(List<Board> boardList, int step) {
		if (categoryList == null) {
			loadCategoryTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.category, null);
			ListView listView = (ListView) layout.findViewById(R.id.category_list);
			listView.setAdapter(new CategoryListAdapter(this, boardList, step));
			
			RelativeLayout relativeLayout = (RelativeLayout) layout.findViewById(R.id.board_relative_layout);
			
			Button goButton = (Button) relativeLayout.findViewById(R.id.btn_go_board);
			goButton.setOnClickListener(this);
			
			AutoCompleteTextView textView = (AutoCompleteTextView) relativeLayout.findViewById(R.id.search_board);
			textView.setCompletionHint("请输入版面英文名");
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  
		                android.R.layout.simple_dropdown_item_1line, boardFullStrings); 
			textView.setAdapter(adapter);

			titleTextView.setText(R.string.title_category);
			switchToView(layout, step);
		}
	}

	public void reloadProfile(Profile profile, final int step) {
		if (profile == null) {
			loadProfileTask.execute();
		} else {
			View layout = inflater.inflate(R.layout.profile, null);

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

	private void logout() {
		final ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setMessage("正在退出...");
		pdialog.show();
		clearData();
		Thread th = new Thread() {
			public void run() {
				smthSupport.destory();
				pdialog.cancel();

				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(StringUtility.LOGOUT, true);
				intent.setClassName("com.athena.asm",
						"com.athena.asm.LoginActivity");
				startActivity(intent);
			}
		};
		th.start();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (cacheViewStack.size() == 0) {
				if (isLogined) {
					logout();
				} else {
					return super.onKeyDown(keyCode, event);
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
	public static final int ABOUT = Menu.FIRST + 1;
	public static final int EXIT = Menu.FIRST + 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SETTING, Menu.NONE, "设置");
		menu.add(0, ABOUT, Menu.NONE, "关于");
		menu.add(0, EXIT, Menu.NONE, "注销");

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
		case EXIT:
			logout();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn_go_board) {
			AutoCompleteTextView textView = (AutoCompleteTextView) ((RelativeLayout)view.getParent()).findViewById(R.id.search_board);
			
			InputMethodManager inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(textView.getWindowToken(), 0);
			
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable(StringUtility.BOARD, boardHashMap.get(textView.getText().toString()));
			intent.putExtras(bundle);
			intent.setClassName("com.athena.asm",
					"com.athena.asm.SubjectListActivity");
			this.startActivity(intent);
		}
	}
}
