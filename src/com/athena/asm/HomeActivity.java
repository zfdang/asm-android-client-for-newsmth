package com.athena.asm;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.Adapter.TabsAdapter;
import com.athena.asm.data.Board;
import com.athena.asm.data.Preferences;
import com.athena.asm.fragment.CategoryFragment;
import com.athena.asm.fragment.FavoriteListFragment;
import com.athena.asm.fragment.GuidanceListFragment;
import com.athena.asm.fragment.MailFragment;
import com.athena.asm.fragment.ProfileFragment;
import com.athena.asm.listener.OnKeyDownListener;
import com.athena.asm.service.CheckMessageService;
import com.athena.asm.util.StringUtility;
import com.athena.asm.util.task.CheckUpdateAsyncTask;
import com.athena.asm.util.task.LoginTask;
import com.athena.asm.viewmodel.BaseViewModel;
import com.athena.asm.viewmodel.HomeViewModel;
import com.google.analytics.tracking.android.EasyTracker;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class HomeActivity extends SherlockFragmentActivity implements OnOpenActivityFragmentListener,
        BaseViewModel.OnViewModelChangObserver {

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        
        // stop EasyTracker methods
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        
        // Adding EasyTracker methods
        EasyTracker.getInstance().activityStart(this);
    }

    private HomeViewModel m_viewModel;

    public LayoutInflater m_inflater;

    private Handler m_handler = new Handler();

    // public static aSMApplication m_application;

    // public static int THEME = R.style.Theme_Sherlock;

    ViewPager m_viewPager;
    TabsAdapter m_tabsAdapter;

    private ProgressDialog m_pdialog;

    private boolean mDoubleBackToExit;
    private Handler mHandler = null;

    class PendingDoubleBackToExit implements Runnable {
        public void run() {
            mDoubleBackToExit = false;
        }
    }

    Boolean getBoolean(Object key){
        if(key == null)
            return false;
        return (Boolean)(key);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        aSMApplication application = aSMApplication.getCurrentApplication();

        setTheme(aSMApplication.THEME);

        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setContentView(R.layout.home);

        m_viewPager = new ViewPager(this);
        m_viewPager.setId(R.id.homePager);
        setContentView(m_viewPager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);

        boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;

        m_tabsAdapter = new TabsAdapter(this, m_viewPager);
        m_tabsAdapter.addTab(actionBar.newTab().setIcon(isLight ? R.drawable.home_inverse : R.drawable.home),
                GuidanceListFragment.class, null);
        m_tabsAdapter.addTab(actionBar.newTab().setIcon(isLight ? R.drawable.favorite_inverse : R.drawable.favorite),
                FavoriteListFragment.class, null);
        m_tabsAdapter.addTab(actionBar.newTab().setIcon(isLight ? R.drawable.category_inverse : R.drawable.category),
                CategoryFragment.class, null);
        m_tabsAdapter.addTab(actionBar.newTab().setIcon(isLight ? R.drawable.mail_inverse : R.drawable.mail),
                MailFragment.class, null);
        m_tabsAdapter.addTab(actionBar.newTab().setIcon(isLight ? R.drawable.profile_inverse : R.drawable.profile),
                ProfileFragment.class, null);
        m_tabsAdapter.finishInit();

        m_inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        m_viewModel = application.getHomeViewModel();
        m_viewModel.registerViewModelChangeObserver(this);
        m_viewModel.setCurrentTab(null); // since m_tabsAdapter.addTab will set
                                         // current tab

        boolean isAutoLogin = application.isAutoLogin();

        m_viewModel.updateLoginStatus();
        if (this.getIntent().getExtras() != null) {
            m_viewModel.setLoggedin(getBoolean(this.getIntent().getExtras().get(StringUtility.LOGINED)));
            m_viewModel.setGuestLogined(getBoolean(this.getIntent().getExtras().get(StringUtility.GUEST_LOGINED)));
        }

        // 如果已从login页面登录过来
        if (m_viewModel.isLogined()) {
            init();
        }
        // 如果是从login页面用guest登录过来
        else if (m_viewModel.isGuestLogined()) {
            init();
        }
        // 如果是第一次启动且保存了自动登录
        else if (isAutoLogin) {
            m_viewModel.restorSmthSupport();

            String userName = application.getAutoUserName();
            String password = application.getAutoPassword();

            showProgressDialog("自动登录中...");

            // LoginTask.doInBackground --> HomeViewModel.login -->
            // SmthSupport.login
            // loginTask.onPostExecute --> HomeViewModel.notifyLoginChanged
            // --> HomeViewModel.notifyViewModelChange
            // --> HomeActivity.onViewModelChange --> HomeActivity.loginTaskDone
            LoginTask loginTask = new LoginTask(m_viewModel, userName, password);
            loginTask.execute();
        }
        // 如果是第一次启动且没有自动登录
        else {
            Intent intent = new Intent();
            intent.setClassName("com.athena.asm", "com.athena.asm.LoginActivity");
            startActivity(intent);
            finish();
        }

        setRequestedOrientation(aSMApplication.ORIENTATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // @Override
    // public void onConfigurationChanged(Configuration newConfig) {
    // // do nothing to stop onCreated
    // super.onConfigurationChanged(newConfig);
    // }

    @Override
    public void onDestroy() {
        m_viewModel.unregisterViewModelChangeObserver(this);
        super.onDestroy();
    }

    public void showAuthenticationFailedToast() {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "用户名或密码错.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showConnectionFailedToast() {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "连接错误，请检查网络.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginTaskDone(int iLoginResult) {
        if (iLoginResult != 1) {
            if (iLoginResult == 0)
                showAuthenticationFailedToast();
            else if (iLoginResult == -1)
                showConnectionFailedToast();

            Intent intent = new Intent();
            intent.setClassName("com.athena.asm", "com.athena.asm.LoginActivity");
            startActivity(intent);
            finish();
        } else {
            m_viewModel.updateLoginStatus();
            init();
            if (m_viewModel.getCurrentTab().equals(StringUtility.TAB_GUIDANCE)) {
                m_viewModel.notifyGuidanceChanged();
            }

        }
    }

    // show information dialog, called by first run and about icon
    private void showInfoDialog() {
        // read version info from androidmanifest.xml
        String version = "unknown";
        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            version = pi.versionName;
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.d("HomeActivity: can't get version name", e.toString());
        }

        // generate about_content with version from manifest
        String content = getText(R.string.about_content).toString();
        String content_with_version = String.format(content, version);

        // linkify
        final SpannableString msg = new SpannableString(content_with_version);
        Linkify.addLinks(msg, Linkify.WEB_URLS);

        final AlertDialog dlg = new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(R.string.about_title)
                .setMessage(msg).create();

        dlg.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.about_close),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing here
                    }
                });
        dlg.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.about_upgrade),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CheckUpdateAsyncTask task = new CheckUpdateAsyncTask(HomeActivity.this);
                        task.execute();
                        dlg.dismiss();
                    }
                });

        dlg.show();

        // Make the textview clickable. Must be called after show()
        ((TextView) dlg.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void init() {
        // initTasks();
        aSMApplication application = aSMApplication.getCurrentApplication();
        if (application.isFirstLaunchApp() || application.isFirstLaunchAfterUpdate()) {
            application.markFirstLaunchApp();
            showInfoDialog();
        }

        // if external sdcard is not available, we can't show images
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "存储卡故障!附件图片将无法显示!", Toast.LENGTH_LONG).show();
        }

        m_viewModel.setChangeNotificationEnabled(false);
        String tab = m_viewModel.getCurrentTab() == null ? application.getDefaultTab() : m_viewModel.getCurrentTab();
        m_viewModel.setCurrentTab(tab);
        m_viewModel.setChangeNotificationEnabled(true);

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
        aSMApplication application = aSMApplication.getCurrentApplication();
        Boolean rememberUser = application.isRememberUser();
        if (!rememberUser) {
            application.updateAutoUserNameAndPassword("", "");
        }
        application.syncPreferences();

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
        // final Context context = this;
        Thread th = new Thread() {
            @Override
            public void run() {
                m_viewModel.logout();
                pdialog.cancel();
                if (!isToExit) {

                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(StringUtility.LOGOUT, true);
                    intent.setClassName("com.athena.asm", "com.athena.asm.LoginActivity");
                    startActivity(intent);
                    finish();
                } else {
                    // Intent intent = new Intent(context, LogoutService.class);
                    // PendingIntent pending = PendingIntent.getService(context,
                    // 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    // Calendar c = new GregorianCalendar();
                    // c.add(Calendar.SECOND, 2);
                    //
                    // AlarmManager alarm = (AlarmManager) context
                    // .getSystemService(Context.ALARM_SERVICE);
                    // alarm.cancel(pending);
                    // alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                    // pending);
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
                // check preference for logout_confirm
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                boolean b_logout_confirm = settings.getBoolean(Preferences.LOGOUT_CONFIRM, false);

                if (b_logout_confirm) {
                    // logout is required
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("确认要注销退出吗？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout(true);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    if (mDoubleBackToExit) {
                        // if mDoubleBackToExit is true, exit now
                        logout(true);
                    } else {
                        // set mDoubleBackToExit = true, and set delayed task to
                        // reset it to false
                        mDoubleBackToExit = true;
                        if (mHandler == null) {
                            mHandler = new Handler();
                        }
                        // reset will be run after 2000 ms
                        mHandler.postDelayed(new PendingDoubleBackToExit(), 2000);
                        Toast.makeText(this, "再按一次退出aSM", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                finishAndClean();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            int index = m_viewPager.getCurrentItem();
            // get OnKeyDownListener by this position;
            // if the fragment has implement the interface and not recycled, we
            // will get valid listener
            OnKeyDownListener listener = m_tabsAdapter.getOnKeyDownListener(index);
            if (listener != null) {
                return listener.onKeyDown(keyCode);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // http://stackoverflow.com/questions/4500354/control-volume-keys
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            // disable the beep sound when volume up/down is pressed
            int index = m_viewPager.getCurrentItem();
            OnKeyDownListener listener = m_tabsAdapter.getOnKeyDownListener(index);
            if (listener != null) {
                // only if we have valid listener
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public static final int SETTING = Menu.FIRST;
    public static final int REFRESH = Menu.FIRST + 1;
    public static final int CLEAN = Menu.FIRST + 2;
    public static final int ABOUT = Menu.FIRST + 3;
    public static final int LOGOUT = Menu.FIRST + 4;
    public static final int EXIT = Menu.FIRST + 5;
    public static final int NIGHT_THEME = Menu.FIRST + 6;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);
        boolean isLight = aSMApplication.THEME == R.style.Theme_Sherlock_Light;

        menu.add(0, CLEAN, Menu.NONE, "清除缓存").setIcon(isLight ? R.drawable.clean_inverse : R.drawable.clean)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, SETTING, Menu.NONE, "设置").setIcon(isLight ? R.drawable.setting_inverse : R.drawable.setting)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, REFRESH, Menu.NONE, "刷新").setIcon(isLight ? R.drawable.refresh_inverse : R.drawable.refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, NIGHT_THEME, Menu.NONE, "光线模式")
                .setIcon(isLight ? R.drawable.light_mode_inverse : R.drawable.light_mode)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, ABOUT, Menu.NONE, "关于").setIcon(isLight ? R.drawable.about_inverse : R.drawable.about)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(0, LOGOUT, Menu.NONE, "注销").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        menu.add(0, EXIT, Menu.NONE, "退出").setShowAsAction(
                MenuItem.SHOW_AS_ACTION_NEVER | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case SETTING:
            Intent intent = new Intent();
            intent.setClassName("com.athena.asm", "com.athena.asm.SettingActivity");
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
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDeleted = deleteFile("CategoryList");
                        if (isDeleted) {
                            m_viewModel.setCategoryList(null);
                            // tigger CategoryFragment: onViewModelChange ->
                            // reloadCategory
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
        case NIGHT_THEME:
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            boolean b_night_theme = settings.getBoolean(Preferences.NIGHT_THEME, true);
            SharedPreferences.Editor editor = settings.edit();

            // change night_theme settings
            if (b_night_theme) {
                // switch to day mode
                editor.putBoolean(Preferences.NIGHT_THEME, false);
                editor.commit();
            } else {
                // switch to night mode
                editor.putBoolean(Preferences.NIGHT_THEME, true);
                editor.commit();
            }
            aSMApplication.getCurrentApplication().initPreferences();

            // we destroy current activity, and create a new one
            // it's a very heavy process and should be improved later
            Intent tempIntent = getIntent();
            // finish();
            tempIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(tempIntent);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                // actionbar might disappear in old system, sleep to solve this
                // issue
                SystemClock.sleep(500);
            }

            // show toast for the switch
            if (b_night_theme) {
                Toast.makeText(getApplicationContext(), "已切换至日光模式", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "已切换至夜间模式", Toast.LENGTH_SHORT).show();
            }
            break;
        case CLEAN:
            UrlImageViewHelper.cleanup(this, 0);
            Toast.makeText(getApplicationContext(), "已清空图片缓存", Toast.LENGTH_SHORT).show();
            break;
        case ABOUT:
            showInfoDialog();
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

    private static final int REQUESTCODE_POST_LIST = 1;

    @Override
    public void onOpenActivityOrFragment(String target, Bundle bundle) {
        if (target.equals(ActivityFragmentTargets.POST_LIST)) {
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClassName("com.athena.asm", PostListActivity.class.getName());
            startActivityForResult(intent, REQUESTCODE_POST_LIST);
        } else if (target.equals(ActivityFragmentTargets.SUBJECT_LIST)) {
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClassName("com.athena.asm", SubjectListActivity.class.getName());
            startActivity(intent);
        } else if (target.equals(ActivityFragmentTargets.MAIL_LIST)) {
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClassName("com.athena.asm", MailListActivity.class.getName());
            startActivity(intent);
        } else if (target.equals(ActivityFragmentTargets.WRITE_POST)) {
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClassName("com.athena.asm", WritePostActivity.class.getName());
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUESTCODE_POST_LIST) {

            if (resultCode == RESULT_OK) {
                String boardEngName = intent.getStringExtra("board_eng_name");
                String boardChsName = intent.getStringExtra("board_chs_name");
                if (boardEngName != null) {
                    Board board = new Board();
                    board.setEngName(boardEngName);
                    board.setChsName(boardChsName);
                    board.setCurrentPageNo(0);
                    // this will cause issue when
                    // 导读页面->帖子详情->回到版面->搜索->搜索结果->点击帖子 的内容无法显示
                    board.setBoardID("fake");
                    Intent newIntent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(StringUtility.BOARD, board);
                    newIntent.putExtras(bundle);
                    newIntent.setClass(this, SubjectListActivity.class);
                    startActivity(newIntent);
                }
            }
        }
    }

    @Override
    public void onViewModelChange(BaseViewModel viewModel, String changedPropertyName, Object... params) {
        if (changedPropertyName.equals(HomeViewModel.LOGIN_PROPERTY_NAME)) {
            dismissProgressDialog();
            int iLoginResult = (Integer) params[0];
            loginTaskDone(iLoginResult);
        }
    }

    private void showProgressDialog(String message) {
        if (m_pdialog == null) {
            m_pdialog = new ProgressDialog(this);
            m_pdialog.setMessage(message);
            m_pdialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (m_pdialog != null) {
            m_pdialog.cancel();
            m_pdialog = null;
        }
    }
}
