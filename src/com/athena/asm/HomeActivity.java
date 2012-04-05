package com.athena.asm;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;

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
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.LoadCategoryTask;
import com.athena.asm.util.task.LoadFavoriteTask;
import com.athena.asm.util.task.LoadGuidanceTask;
import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoginTask;
//import com.athena.asm.util.task.LoadMailTask;
import com.athena.asm.util.task.LoadProfileTask;
import com.athena.asm.viewmodel.HomeViewModel;
import com.athena.asm.viewmodel.BaseViewModel;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HomeActivity extends Activity implements OnClickListener, BaseViewModel.OnViewModelChangObserver {

    private HomeViewModel m_viewModel;

    public LayoutInflater m_inflater;
    private LinearLayout m_bodyContainer;
    private TextView m_titleTextView;

    private TextView m_btnGuidance;
    private TextView m_btnFavorite;
    private TextView m_btnMail;
    private TextView m_btnCategory;
    private TextView m_btnProfile;

    private ArrayList<View> m_cacheViewStack = new ArrayList<View>();
    private double m_currentTabIndex = 0;

    private boolean m_isIntoSettings = false;

    private Handler m_handler = new Handler();

    public static aSMApplication m_application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.home);

        m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        m_bodyContainer = (LinearLayout) findViewById(R.id.bodyContainer);
        m_titleTextView = (TextView) findViewById(R.id.title);

        m_application = (aSMApplication) getApplication();
        m_application.initPreferences();
        
        m_viewModel = m_application.getHomeViewModel();
        m_viewModel.registerViewModelChangeObserver(this);
        
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

            LoginTask loginTask = new LoginTask(this, m_viewModel, userName, password);
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
        if (HomeActivity.m_application.isNightTheme()) {
            ((LinearLayout) m_titleTextView.getParent().getParent())
                    .setBackgroundColor(getResources().getColor(
                            R.color.body_background_night));
        } else {
            ((LinearLayout) m_titleTextView.getParent().getParent())
                    .setBackgroundColor(getResources().getColor(
                            R.color.body_background));
        }

        if (m_isIntoSettings) {
            m_isIntoSettings = false;
            int index = (int) (m_currentTabIndex / 10);
            if (index == 1) {
                reloadFavorite(m_viewModel.getFavList(), 20);
            } else {
                reloadGuidanceList();
            }
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
        initTabListeners();
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

        String tab = m_viewModel.getCurrentTab() == null ? m_application.getDefaultTab() : m_viewModel.getCurrentTab();
        m_viewModel.setCurrentTab(tab);
    }

    private void initTabListeners() {
        m_btnGuidance = (TextView) findViewById(R.id.footer_btn_guidance);
        m_btnFavorite = (TextView) findViewById(R.id.footer_btn_favorite);
        m_btnMail = (TextView) findViewById(R.id.footer_btn_mail);
        m_btnCategory = (TextView) findViewById(R.id.footer_btn_category);
        m_btnProfile = (TextView) findViewById(R.id.footer_btn_profile);

        m_btnGuidance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	m_viewModel.setCurrentTab("001");
            }
        });

        m_btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	m_viewModel.setCurrentTab("002");
            }
        });

        m_btnCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	m_viewModel.setCurrentTab("003");
            }
        });

        m_btnMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	m_viewModel.setCurrentTab("004");
            }
        });

        m_btnProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	m_viewModel.setCurrentTab("005");
            }
        });
    }

    private void switchToView(View targetView, double targetIndex) {
        if (m_currentTabIndex == targetIndex) {
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
        int currentTabNumber = (int) (m_currentTabIndex / 10);
        int targetTabNumber = (int) (targetIndex / 10);
        if (currentTabNumber == targetTabNumber) {
            if (targetIndex > m_currentTabIndex
                    && m_bodyContainer.getChildCount() > 0) {
                m_cacheViewStack.add(m_bodyContainer.getChildAt(0));
            }
        } else {
            m_cacheViewStack.clear();
        }

        m_currentTabIndex = targetIndex;

        m_bodyContainer.removeAllViews();
        m_bodyContainer.addView(targetView);
    }

    public void reloadGuidanceList() {
        if (m_viewModel.getGuidanceSectionNames() == null || m_viewModel.getGuidanceSectionDetails() == null) {
            LoadGuidanceTask loadGuidanceTask = new LoadGuidanceTask(this, m_viewModel);
            loadGuidanceTask.execute();
        } else {
            View layout = m_inflater.inflate(R.layout.guidance, null);
            ListView listView = (ListView) layout
                    .findViewById(R.id.guidance_list);
            listView.setAdapter(new GuidanceListAdapter(this, 0, 0,
            		m_viewModel.getGuidanceSectionNames(), m_viewModel.getGuidanceSectionDetails()));
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
                            m_viewModel.getGuidanceSectionNames(), m_viewModel.getGuidanceSectionDetails()));
                    listView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent,
                                View view, final int position, long id) {
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(StringUtility.SUBJECT,
                                    (Subject) view.getTag());
                            intent.putExtras(bundle);
                            intent.setClassName("com.athena.asm",
                                    "com.athena.asm.PostListActivity");
                            startActivity(intent);
                        }
                    });
                    switchToView(listView, 11);
                }
            });
            m_titleTextView.setText(R.string.title_guidance);
            switchToView(listView, 10);
        }
    }

    public void reloadFavorite(final List<Board> boardList, int step) {
        if (boardList == null) {
            LoadFavoriteTask loadFavoriteTask = new LoadFavoriteTask(this, m_viewModel);
            loadFavoriteTask.execute();
        } else {
            View layout = m_inflater.inflate(R.layout.favorite, null);
            ListView listView = (ListView) layout
                    .findViewById(R.id.favorite_list);
            final FavoriteListAdapter favoriteListAdapter = new FavoriteListAdapter(
                    this, boardList, step);
            listView.setAdapter(favoriteListAdapter);

            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        final int position, long id) {
                    Board outBoard = boardList.get(position);
                    if (outBoard.isDirectory()) {
                        Board board = (Board) view.getTag();
                        if (board.getDirectoryName().equals("最近访问版面")) {
                            reloadFavorite(
                                    new ArrayList<Board>(
                                            HomeActivity.m_application
                                                    .getRecentBoards()),
                                    ++favoriteListAdapter.step);
                        } else {
                            reloadFavorite(board.getChildBoards(),
                                    ++favoriteListAdapter.step);
                        }
                    } else {
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(StringUtility.BOARD,
                                (Board) view.getTag());
                        HomeActivity.m_application.addRecentBoard((Board) view
                                .getTag());
                        intent.putExtras(bundle);
                        intent.setClassName("com.athena.asm",
                                "com.athena.asm.SubjectListActivity");
                        startActivity(intent);
                    }
                }
            });

            m_titleTextView.setText(R.string.title_favorite);
            switchToView(listView, step);
        }
    }

    public void reloadCategory(final List<Board> boardList, int step) {
        if (boardList == null) {
            LoadCategoryTask loadCategoryTask = new LoadCategoryTask(this, m_viewModel);
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

            m_titleTextView.setText(R.string.title_category);
            switchToView(layout, step);
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

        m_titleTextView.setText(R.string.title_mail);
        switchToView(listView, 40);
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
            LoadProfileTask loadProfileTask = new LoadProfileTask(this, m_viewModel,
                    m_viewModel.getLoginUserID(), 50);
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

            m_titleTextView.setText(R.string.title_profile);
            switchToView(layout, step);
        }
    }

    private void clearData() {
    	m_viewModel.setGuidanceSectionNames(null);
    	m_viewModel.setGuidanceSectionDetails(null);
    	m_viewModel.setFavList(null);
    	m_viewModel.setCategoryList(null);
        m_viewModel.setCurrentProfile(null);
        m_inflater = null;
        m_cacheViewStack.clear();
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
            if (m_cacheViewStack.size() == 0) {
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
                    // return super.onKeyDown(keyCode, event);
                }
            } else if (m_cacheViewStack.size() > 0) {
                View lastView = m_cacheViewStack.get(m_cacheViewStack.size() - 1);
                m_cacheViewStack.remove(m_cacheViewStack.size() - 1);
                switchToView(lastView, m_currentTabIndex - 1);
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
        super.onCreateOptionsMenu(menu);
        menu.add(0, SETTING, Menu.NONE, "设置");
        menu.add(0, REFRESH, Menu.NONE, "刷新");
        menu.add(0, CLEAN, Menu.NONE, "清除缓存");
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
            m_isIntoSettings = true;
            Intent intent = new Intent();
            intent.setClassName("com.athena.asm",
                    "com.athena.asm.SettingActivity");
            startActivity(intent);
            break;
        case REFRESH:
            int index = (int) (m_currentTabIndex / 10);
            switch (index) {
            case 1:
            	m_viewModel.setGuidanceSectionNames(null);
                reloadGuidanceList();
                break;
            case 2:
                boolean isDeleted = deleteFile("FavList");
                if (isDeleted) {
                    m_viewModel.setFavList(null);
                    reloadFavorite(m_viewModel.getFavList(), 20);
                }
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
                                    m_viewModel.setCategoryList(null);
                                    reloadCategory(m_viewModel.getCategoryList(), 30);
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

            Board board = m_viewModel.getBoardHashMap().get(textView.getText().toString()
                    .toLowerCase());

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
	public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object ... params) {
		
		if (changedPropertyName.equals(HomeViewModel.GUIDANCE_PROPERTY_NAME)) {
			reloadGuidanceList();
		}
		else if (changedPropertyName.equals(HomeViewModel.CATEGORYLIST_PROPERTY_NAME)) {
			reloadCategory(m_viewModel.getCategoryList(), 30);
		}
		else if (changedPropertyName.equals(HomeViewModel.FAVLIST_PROPERTY_NAME)) {
			reloadFavorite(m_viewModel.getFavList(), 20);
		}
		else if (changedPropertyName.equals(HomeViewModel.MAILBOX_PROPERTY_NAME)) {
			loadMail();
		}
		else if (changedPropertyName.equals(HomeViewModel.PROFILE_PROPERTY_NAME)) {
			Profile profile = (Profile)params[0];
			int step = (Integer)params[1];
			
			reloadProfile(profile, step);
		}
		else if (changedPropertyName.equals(HomeViewModel.CURRENTTAB_PROPERTY_NAME)) {
			String tab = m_viewModel.getCurrentTab();
			
			//TODO: find a better place to do this...
			if (!tab.equals("004")) {
				m_application.getMailViewModel().clear();
			}
			
			if (tab.equals("001")) {
	            reloadGuidanceList();
	        } else if (tab.equals("002")) {
	            reloadFavorite(m_viewModel.getFavList(), 20);
	        } else if (tab.equals("003")) {
	            reloadCategory(m_viewModel.getCategoryList(), 30);
	        } else if (tab.equals("004")) {
	            reloadMail();
	        } else {
	            reloadProfile(m_viewModel.getCurrentProfile(), 50);
	        }
		}
		
	}
}
