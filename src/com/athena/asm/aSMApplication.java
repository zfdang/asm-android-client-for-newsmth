package com.athena.asm;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.athena.asm.data.Board;
import com.athena.asm.data.Preferences;
import com.athena.asm.util.CrashHandler;
import com.athena.asm.util.MyDatabase;
import com.athena.asm.util.SimpleCrypto;
import com.athena.asm.util.StringUtility;
import com.athena.asm.viewmodel.HomeViewModel;
import com.athena.asm.viewmodel.MailViewModel;
import com.athena.asm.viewmodel.PostListViewModel;
import com.athena.asm.viewmodel.SubjectListViewModel;

public class aSMApplication extends Application {

    private static aSMApplication m_application;
    public static int THEME = R.style.Theme_Sherlock;
    public static int ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    public static int IMAGE_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

    private boolean m_isFirstLaunch = false;
    private boolean m_isLoadDefaultCategoryFile = false;

    /*
     * settings: to change the default value of one setting: 1. change
     * android:defaultValue of the element in preference.xml 2. change default
     * value for the variable 3. change default value when editor.putBoolean /
     * settings.getBoolean
     */
    private boolean m_isRememberUser = true;
    private boolean m_isAutoLogin = false;

    private String m_autoUserName = "";
    private String m_autoPassword = "";

    private String currentUserID = "";
    private String defaultTab = StringUtility.TAB_GUIDANCE;
    private String defaultBoardType = "001";

    private boolean isShowCheck = true;
    private boolean isUseVibrate = true;
    private String checkInterval = "3";

    private int lastLaunchVersionCode = 4;
    private int currentVersionCode = 5;

    private boolean isWeiboStyle = false;
    private boolean isShowIp = true;
    private int guidanceFontSize = 25;
    private int guidanceSecondFontSize = 20;
    private int subjectFontSize = 18;
    private int postFontSize = 17;

    private LinkedList<Board> recentBoards = null;
    private Set<String> recentBoardNameSet = null;

    private boolean isAutoOptimize = true;
    private float imageSizeThreshold = 50;

    private boolean isTouchScroll = true;
    private boolean isTouchSwipe = true;
    private boolean isTouchSwipeBack = true;
    private boolean isTouchHint = true;
    private boolean isHidePinSubject = false;
    private boolean isNightTheme = true;

    private int defaultOrientation = 0;
    private int defaultImageOrientation = 0;
    private boolean isPromotionShow = true;
    private String promotionContent = "";

    private ArrayList<String> blackList = new ArrayList<String>();

    private String forwardEmailAddr = "";

    // View models for Activities
    private HomeViewModel m_homeViewModel = new HomeViewModel();
    private SubjectListViewModel m_subjectListViewModel = new SubjectListViewModel();
    private PostListViewModel m_postListViewModel = new PostListViewModel();
    private MailViewModel m_mailViewModel = new MailViewModel();

    // IP database
    public static MyDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

        db = new MyDatabase(this);

        m_application = this;
        m_application.initPreferences();
    }

    protected void onDestroy() {
        db.close();
    }

    public static aSMApplication getCurrentApplication() {
        return m_application;
    }

    public void syncPreferences() {
        try {
            FileOutputStream fos = openFileOutput("RecentFavList", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(recentBoards);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFirstLaunchApp() {
        return m_isFirstLaunch;
    }

    public void markFirstLaunchApp() {
        m_isFirstLaunch = false;
        lastLaunchVersionCode = currentVersionCode;
    }

    public boolean isFirstLaunchAfterUpdate() {
        if (lastLaunchVersionCode < currentVersionCode) {
            return true;
        } else {
            return false;
        }
    }

    public void updateDefaultCategoryLoadStatus(boolean isLoaded) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Preferences.DEFAULT_CATEGORY_LOADED, isLoaded);
        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public void initPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        if (!settings.contains(Preferences.DEFAULT_CATEGORY_LOADED)) {
            editor.putBoolean(Preferences.DEFAULT_CATEGORY_LOADED, false);
        } else {
            setLoadDefaultCategoryFile(settings.getBoolean(Preferences.DEFAULT_CATEGORY_LOADED, true));
        }

        if (!settings.contains(Preferences.REMEMBER_USER)) {
            editor.putBoolean(Preferences.REMEMBER_USER, true);
        } else {
            m_isRememberUser = settings.getBoolean(Preferences.REMEMBER_USER, true);
        }

        if (!settings.contains(Preferences.AUTO_LOGIN)) {
            editor.putBoolean(Preferences.AUTO_LOGIN, false);
        } else {
            m_isAutoLogin = settings.getBoolean(Preferences.AUTO_LOGIN, false);
            ;
        }

        if (!settings.contains(Preferences.DEFAULT_TAB)) {
            editor.putString(Preferences.DEFAULT_TAB, StringUtility.TAB_GUIDANCE);
        } else {
            defaultTab = settings.getString(Preferences.DEFAULT_TAB, StringUtility.TAB_GUIDANCE);
        }

        if (!settings.contains(Preferences.DEFAULT_BOARD_TYPE)) {
            editor.putString(Preferences.DEFAULT_BOARD_TYPE, "001");
        } else {
            defaultBoardType = settings.getString(Preferences.DEFAULT_BOARD_TYPE, "001");
        }

        if (!settings.contains(Preferences.SHOW_CHECK)) {
            editor.putBoolean(Preferences.SHOW_CHECK, true);
        } else {
            setShowCheck(settings.getBoolean(Preferences.SHOW_CHECK, true));
        }

        if (!settings.contains(Preferences.USE_VIBRATE)) {
            editor.putBoolean(Preferences.USE_VIBRATE, true);
        } else {
            setUseVibrate(settings.getBoolean(Preferences.USE_VIBRATE, true));
        }

        if (!settings.contains(Preferences.CHECK_INTERVAL)) {
            editor.putString(Preferences.CHECK_INTERVAL, "3");
        } else {
            setCheckInterval(settings.getString(Preferences.CHECK_INTERVAL, "3"));
        }

        if (!settings.contains(Preferences.WEIBO_STYLE)) {
            editor.putBoolean(Preferences.WEIBO_STYLE, false);
        } else {
            setWeiboStyle(settings.getBoolean(Preferences.WEIBO_STYLE, false));
        }

        if (!settings.contains(Preferences.SHOW_IP)) {
            editor.putBoolean(Preferences.SHOW_IP, true);
        } else {
            setShowIp(settings.getBoolean(Preferences.SHOW_IP, true));
        }

        if (!settings.contains(Preferences.GUIDANCE_FONT_SIZE)) {
            editor.putString(Preferences.GUIDANCE_FONT_SIZE, "25");
        } else {
            String size = settings.getString(Preferences.GUIDANCE_FONT_SIZE, "25");
            guidanceFontSize = StringUtility.filterUnNumber(size);
            if (guidanceFontSize == 0) {
                guidanceFontSize = 25;
            }
        }

        if (!settings.contains(Preferences.GUIDANCE_SECOND_FONT_SIZE)) {
            editor.putString(Preferences.GUIDANCE_SECOND_FONT_SIZE, "20");
        } else {
            String size = settings.getString(Preferences.GUIDANCE_SECOND_FONT_SIZE, "20");
            guidanceSecondFontSize = StringUtility.filterUnNumber(size);
            if (guidanceSecondFontSize == 0) {
                guidanceSecondFontSize = 20;
            }
        }

        if (!settings.contains(Preferences.SUBJECT_FONT_SIZE)) {
            editor.putString(Preferences.SUBJECT_FONT_SIZE, "18");
        } else {
            String size = settings.getString(Preferences.SUBJECT_FONT_SIZE, "18");
            subjectFontSize = StringUtility.filterUnNumber(size);
            if (subjectFontSize == 0) {
                subjectFontSize = 18;
            }
        }

        if (!settings.contains(Preferences.POST_FONT_SIZE)) {
            editor.putString(Preferences.POST_FONT_SIZE, "17");
        } else {
            String size = settings.getString(Preferences.POST_FONT_SIZE, "17");
            postFontSize = StringUtility.filterUnNumber(size);
            if (postFontSize == 0) {
                postFontSize = 17;
            }
        }

        if (!settings.contains(Preferences.TOUCH_SCROLL)) {
            editor.putBoolean(Preferences.TOUCH_SCROLL, true);
        } else {
            isTouchScroll = settings.getBoolean(Preferences.TOUCH_SCROLL, true);
        }

        if (!settings.contains(Preferences.TOUCH_SWIPE)) {
            editor.putBoolean(Preferences.TOUCH_SWIPE, true);
        } else {
            isTouchSwipe = settings.getBoolean(Preferences.TOUCH_SWIPE, true);
        }

        if (!settings.contains(Preferences.TOUCH_SWIPE_BACK)) {
            editor.putBoolean(Preferences.TOUCH_SWIPE_BACK, true);
        } else {
            isTouchSwipeBack = settings.getBoolean(Preferences.TOUCH_SWIPE_BACK, true);
        }

        if (!settings.contains(Preferences.TOUCH_HINT)) {
            editor.putBoolean(Preferences.TOUCH_HINT, true);
        } else {
            isTouchHint = settings.getBoolean(Preferences.TOUCH_HINT, true);
        }

        if (!settings.contains(Preferences.HIDE_PIN_SUBJECT)) {
            editor.putBoolean(Preferences.HIDE_PIN_SUBJECT, false);
        } else {
            setHidePinSubject(settings.getBoolean(Preferences.HIDE_PIN_SUBJECT, false));
        }

        if (!settings.contains(Preferences.NIGHT_THEME)) {
            editor.putBoolean(Preferences.NIGHT_THEME, true);
        } else {
            setNightTheme(settings.getBoolean(Preferences.NIGHT_THEME, true));
        }

        if (!settings.contains(Preferences.DEFAULT_ORIENTATION)) {
            editor.putString(Preferences.DEFAULT_ORIENTATION, "0");
        } else {
            setDefaultOrientation(StringUtility
                    .filterUnNumber(settings.getString(Preferences.DEFAULT_ORIENTATION, "0")));
        }

        if (!settings.contains(Preferences.DEFAULT_IMAGE_ORIENTATION)) {
            editor.putString(Preferences.DEFAULT_IMAGE_ORIENTATION, "0");
        } else {
            setDefaultImageOrientation(StringUtility
                    .filterUnNumber(settings.getString(Preferences.DEFAULT_IMAGE_ORIENTATION, "0")));
        }

        if (!settings.contains(Preferences.PROMOTION_SHOW)) {
            editor.putBoolean(Preferences.PROMOTION_SHOW, true);
        } else {
            setPromotionShow(settings.getBoolean(Preferences.PROMOTION_SHOW, true));
        }

        if (!settings.contains(Preferences.PROMOTION_CONTENT)) {
            editor.putString(Preferences.PROMOTION_CONTENT, "");
        } else {
            setPromotionContent(settings.getString(Preferences.PROMOTION_CONTENT, ""));
        }

        if (!settings.contains(Preferences.AUTO_OPTIMIZE)) {
            editor.putBoolean(Preferences.AUTO_OPTIMIZE, true);
        } else {
            setAutoOptimize(settings.getBoolean(Preferences.AUTO_OPTIMIZE, true));
        }

        if (!settings.contains(Preferences.IMAGE_SIZE_THRESHOLD)) {
            editor.putString(Preferences.IMAGE_SIZE_THRESHOLD, "50");
        } else {
            String size = settings.getString(Preferences.IMAGE_SIZE_THRESHOLD, "50");
            setImageSizeThreshold(StringUtility.filterUnNumber(size));
        }

        if (!settings.contains(Preferences.BLACK_LIST)) {
            editor.putString(Preferences.BLACK_LIST, "");
        } else {
            blackList.clear();
            String blackListString = settings.getString(Preferences.BLACK_LIST, "");
            blackListString = blackListString.replaceAll("　", " ");
            String[] ids = blackListString.split(" ");
            for (int i = 0; i < ids.length; i++) {
                String idString = ids[i].trim();
                if (idString.length() > 0) {
                    blackList.add(ids[i]);
                }
            }
        }
        if (!settings.contains(Preferences.FORWARD_EMAIL)) {
            editor.putString(Preferences.FORWARD_EMAIL, "");
        } else {
            setForwardEmailAddr(settings.getString(Preferences.FORWARD_EMAIL, ""));
            ;
        }

        PackageManager pm = getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(getPackageName(), 0);
            currentVersionCode = pi.versionCode;
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }

        if (settings.contains(Preferences.LAST_LAUNCH_VERSION)) {
            String versionCode = settings.getString(Preferences.LAST_LAUNCH_VERSION, "4");
            lastLaunchVersionCode = StringUtility.filterUnNumber(versionCode);
        } else {
            m_isFirstLaunch = true;
        }
        editor.putString(Preferences.LAST_LAUNCH_VERSION, currentVersionCode + "");

        setAutoUserName(settings.getString(Preferences.USERNAME_KEY, ""));
        setAutoPassword(settings.getString(Preferences.PASSWORD_KEY, ""));

        if (lastLaunchVersionCode == 4) {
            try {
                m_autoPassword = SimpleCrypto.encrypt("comathenaasm", m_autoPassword);
                editor.putString(Preferences.PASSWORD_KEY, m_autoPassword);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        editor.commit();

        try {
            m_autoPassword = SimpleCrypto.decrypt("comathenaasm", m_autoPassword);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (recentBoards == null) {
            try {
                FileInputStream fis = openFileInput("RecentFavList");
                ObjectInputStream ois = new ObjectInputStream(fis);
                recentBoards = (LinkedList<Board>) ois.readObject();
                recentBoardNameSet = new HashSet<String>();
                ArrayList<Board> toDeleteBoards = new ArrayList<Board>();
                for (Iterator<Board> iterator = recentBoards.iterator(); iterator.hasNext();) {
                    Board board = (Board) iterator.next();
                    if (recentBoardNameSet.contains(board.getEngName())) {
                        toDeleteBoards.add(board);
                    } else {
                        recentBoardNameSet.add(board.getEngName());
                    }
                }
                Log.d("aSMApplication.initPreferences", "loading from file");
                fis.close();
                for (Iterator<Board> iterator = toDeleteBoards.iterator(); iterator.hasNext();) {
                    Board board = (Board) iterator.next();
                    recentBoards.remove(board);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isNightTheme()) {
            THEME = R.style.Theme_Sherlock;
        } else {
            THEME = R.style.Theme_Sherlock_Light;
        }

        switch (defaultOrientation) {
        case 0:
            // http://developer.android.com/reference/android/R.attr.html#screenOrientation
            ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            break;
        case 1:
            ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            break;
        case 2:
            ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            break;
        default:
            break;
        }

        switch (defaultImageOrientation) {
        case 0:
            // http://developer.android.com/reference/android/R.attr.html#screenOrientation
            IMAGE_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            break;
        case 1:
            IMAGE_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
            break;
        default:
            break;
        }
    }

    public void updateAutoUserNameAndPassword(String username, String password) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Preferences.USERNAME_KEY, username);
        try {
            password = SimpleCrypto.encrypt("comathenaasm", password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(Preferences.PASSWORD_KEY, password);
        editor.commit();
    }

    public void setRememberUser(boolean isRememberUser) {
        this.m_isRememberUser = isRememberUser;
    }

    public boolean isRememberUser() {
        return m_isRememberUser;
    }

    public void setAutoLogin(boolean isAutoLogin) {
        this.m_isAutoLogin = isAutoLogin;
    }

    public boolean isAutoLogin() {
        return m_isAutoLogin;
    }

    public void setCurrentUserID(String currentUserID) {
        this.currentUserID = currentUserID;
    }

    public String getCurrentUserID() {
        return currentUserID;
    }

    public void setDefaultTab(String defaultTab) {
        this.defaultTab = defaultTab;
    }

    public String getDefaultTab() {
        return defaultTab;
    }

    public void setDefaultBoardType(String defaultBoardType) {
        this.defaultBoardType = defaultBoardType;
    }

    public String getDefaultBoardType() {
        return defaultBoardType;
    }

    public void setAutoUserName(String autoUserName) {
        this.m_autoUserName = autoUserName;
    }

    public String getAutoUserName() {
        return m_autoUserName;
    }

    public void setAutoPassword(String autoPassword) {
        this.m_autoPassword = autoPassword;
    }

    public String getAutoPassword() {
        return m_autoPassword;
    }

    public void setGuidanceFontSize(int guidanceFontSize) {
        this.guidanceFontSize = guidanceFontSize;
    }

    public int getGuidanceFontSize() {
        return guidanceFontSize;
    }

    public void setGuidanceSecondFontSize(int guidanceFontSize) {
        this.guidanceSecondFontSize = guidanceFontSize;
    }

    public int getGuidanceSecondFontSize() {
        return guidanceSecondFontSize;
    }

    public void setSubjectFontSize(int subjectFontSize) {
        this.subjectFontSize = subjectFontSize;
    }

    public int getSubjectFontSize() {
        return subjectFontSize;
    }

    public void setPostFontSize(int postFontSize) {
        this.postFontSize = postFontSize;
    }

    public int getPostFontSize() {
        return postFontSize;
    }

    public ArrayList<String> getBlackList() {
        return blackList;
    }

    public void addRecentBoard(Board board) {
        if (recentBoards == null) {
            recentBoards = new LinkedList<Board>();
        }
        if (recentBoardNameSet == null) {
            recentBoardNameSet = new HashSet<String>();
        }
        if (recentBoardNameSet.contains(board.getEngName())) {
            for (Iterator<Board> iterator = recentBoards.iterator(); iterator.hasNext();) {
                Board board2 = (Board) iterator.next();
                if (board2.getEngName().equals(board.getEngName())) {
                    recentBoards.remove(board2);
                    break;
                }
            }
        }
        recentBoards.addFirst(board);
        recentBoardNameSet.add(board.getEngName());
        if (recentBoards.size() > 10) {
            recentBoards.removeLast();
        }
    }

    public void setRecentBoards(LinkedList<Board> recentBoards) {
        this.recentBoards = recentBoards;
    }

    public Queue<Board> getRecentBoards() {
        if (recentBoards == null) {
            recentBoards = new LinkedList<Board>();
        }
        return recentBoards;
    }

    public boolean isTouchScroll() {
        return isTouchScroll;
    }

    public boolean isTouchSwipe() {
        return isTouchSwipe;
    }

    public boolean isTouchSwipeBack() {
        return isTouchSwipeBack;
    }

    public boolean isTouchHint() {
        return isTouchHint;
    }

    public void setTouchScroll(boolean isTouchScroll) {
        this.isTouchScroll = isTouchScroll;
    }

    public boolean isHidePinSubject() {
        return isHidePinSubject;
    }

    public void setHidePinSubject(boolean isHidePinSubject) {
        this.isHidePinSubject = isHidePinSubject;
    }

    public void switchHidePinSubject() {
        this.isHidePinSubject = !this.isHidePinSubject;
    }

    public boolean isPromotionShow() {
        return isPromotionShow;
    }

    public void setPromotionShow(boolean isPromotionShow) {
        this.isPromotionShow = isPromotionShow;
    }

    public boolean isAutoOptimize() {
        return isAutoOptimize;
    }

    public void setAutoOptimize(boolean isAutoOptimize) {
        this.isAutoOptimize = isAutoOptimize;
    }

    public float getImageSizeThreshold() {
        return imageSizeThreshold;
    }

    public void setImageSizeThreshold(float imageSizeThreshold) {
        this.imageSizeThreshold = imageSizeThreshold;
    }

    public boolean isNightTheme() {
        return isNightTheme;
    }

    public void setNightTheme(boolean isNightTheme) {
        this.isNightTheme = isNightTheme;
    }

    public HomeViewModel getHomeViewModel() {
        return m_homeViewModel;
    }

    public SubjectListViewModel getSubjectListViewModel() {
        return m_subjectListViewModel;
    }

    public PostListViewModel getPostListViewModel() {
        return m_postListViewModel;
    }

    public MailViewModel getMailViewModel() {
        return m_mailViewModel;
    }

    public String getForwardEmailAddr() {
        return forwardEmailAddr;
    }

    public void setForwardEmailAddr(String forwardEmailAddr) {
        this.forwardEmailAddr = forwardEmailAddr;
    }

    public void updateForwardEmailAddr(String email) {
        this.forwardEmailAddr = email;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Preferences.FORWARD_EMAIL, email);
        editor.commit();

    }

    public String getPromotionContent() {
        return promotionContent;
    }

    public void setPromotionContent(String promotionContent) {
        this.promotionContent = promotionContent;
    }

    public boolean isShowCheck() {
        return isShowCheck;
    }

    public void setShowCheck(boolean isShowCheck) {
        this.isShowCheck = isShowCheck;
    }

    public boolean isWeiboStyle() {
        return isWeiboStyle;
    }

    public void setWeiboStyle(boolean isWeiboStyle) {
        this.isWeiboStyle = isWeiboStyle;
    }

    public boolean isShowIp() {
        return isShowIp;
    }

    public void setShowIp(boolean isShowIp) {
        this.isShowIp = isShowIp;
    }

    public String getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(String checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isUseVibrate() {
        return isUseVibrate;
    }

    public void setUseVibrate(boolean isUseVibrate) {
        this.isUseVibrate = isUseVibrate;
    }

    public boolean isLoadDefaultCategoryFile() {
        return m_isLoadDefaultCategoryFile;
    }

    public void setLoadDefaultCategoryFile(boolean isLoadDefaultCategoryFile) {
        this.m_isLoadDefaultCategoryFile = isLoadDefaultCategoryFile;
    }

    public int getDefaultOrientation() {
        return defaultOrientation;
    }

    public void setDefaultOrientation(int defaultOrientation) {
        this.defaultOrientation = defaultOrientation;
    }

    public int getDefaultImageOrientation() {
        return defaultImageOrientation;
    }

    public void setDefaultImageOrientation(int defaultImageOrientation) {
        this.defaultImageOrientation = defaultImageOrientation;
    }

}
