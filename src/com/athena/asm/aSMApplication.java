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

import com.athena.asm.data.Board;
import com.athena.asm.data.Preferences;
import com.athena.asm.util.SimpleCrypto;
import com.athena.asm.util.StringUtility;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

public class aSMApplication extends Application {
	private boolean isRememberUser = true;
	private boolean isAutoLogin = false;
	
	private String autoUserName = "";
	private String autoPassword = "";
	
	private String currentUserID = "";
	private String defaultTab = "001";
	private String defaultBoardType = "001";
	
	private int lastLaunchVersionCode = 4;
	private int currentVersionCode = 5;
	
	private int guidanceFontSize = 25;
	private int guidanceSecondFontSize = 20;
	private int subjectFontSize = 18;
	private int postFontSize = 17;
	
	private LinkedList<Board> recentBoards = null;
	private Set<String> recentBoardNameSet = null;
	
	
	
	private boolean isTouchScroll = true;
	private ArrayList<String> blackList = new ArrayList<String>();
	
	public void syncPreferences() {
		try {
			FileOutputStream fos = openFileOutput("RecentFavList",
					Context.MODE_PRIVATE);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(recentBoards);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void initPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		
		if (!settings.contains(Preferences.REMEMBER_USER)) {
			editor.putBoolean(Preferences.REMEMBER_USER, true);
		}
		else {
			isRememberUser = settings.getBoolean(Preferences.REMEMBER_USER, true);
		}
		
		if (!settings.contains(Preferences.AUTO_LOGIN)) {
			editor.putBoolean(Preferences.AUTO_LOGIN, false);
		}
		else {
			isAutoLogin = settings.getBoolean(Preferences.AUTO_LOGIN, false);;
		}
		
		if (!settings.contains(Preferences.DEFAULT_TAB)) {
			editor.putString(Preferences.DEFAULT_TAB, "001");
		}
		else {
			defaultTab = settings.getString(Preferences.DEFAULT_TAB, "001");
		}
		
		if (!settings.contains(Preferences.DEFAULT_BOARD_TYPE)) {
			editor.putString(Preferences.DEFAULT_BOARD_TYPE, "001");
		}
		else {
			defaultBoardType = settings.getString(Preferences.DEFAULT_BOARD_TYPE, "001");
		}
		
		if (!settings.contains(Preferences.GUIDANCE_FONT_SIZE)) {
			editor.putString(Preferences.GUIDANCE_FONT_SIZE, "25");
		}
		else {
			String size = settings.getString(Preferences.GUIDANCE_FONT_SIZE, "25");
			guidanceFontSize = StringUtility.filterUnNumber(size);
			if (guidanceFontSize == 0) {
				guidanceFontSize = 25;
			}
		}
		
		if (!settings.contains(Preferences.GUIDANCE_SECOND_FONT_SIZE)) {
			editor.putString(Preferences.GUIDANCE_SECOND_FONT_SIZE, "20");
		}
		else {
			String size = settings.getString(Preferences.GUIDANCE_SECOND_FONT_SIZE, "20");
			guidanceSecondFontSize = StringUtility.filterUnNumber(size);
			if (guidanceSecondFontSize == 0) {
				guidanceSecondFontSize = 20;
			}
		}
		
		if (!settings.contains(Preferences.SUBJECT_FONT_SIZE)) {
			editor.putString(Preferences.SUBJECT_FONT_SIZE, "18");
		}
		else {
			String size = settings.getString(Preferences.SUBJECT_FONT_SIZE, "18");
			subjectFontSize = StringUtility.filterUnNumber(size);
			if (subjectFontSize == 0) {
				subjectFontSize = 18;
			}
		}
		
		if (!settings.contains(Preferences.POST_FONT_SIZE)) {
			editor.putString(Preferences.POST_FONT_SIZE, "17");
		}
		else {
			String size = settings.getString(Preferences.POST_FONT_SIZE, "17");
			postFontSize = StringUtility.filterUnNumber(size);
			if (postFontSize == 0) {
				postFontSize = 17;
			}
		}
		
		if (!settings.contains(Preferences.TOUCH_SCROLL)) {
			editor.putBoolean(Preferences.TOUCH_SCROLL, true);
		}
		else {
			setTouchScroll(settings.getBoolean(Preferences.TOUCH_SCROLL, true));
		}
		
		if (!settings.contains(Preferences.BLACK_LIST)) {
			editor.putString(Preferences.BLACK_LIST, "");
		}
		else {
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
		}
		editor.putString(Preferences.LAST_LAUNCH_VERSION, currentVersionCode + "");
		
		setAutoUserName(settings.getString(Preferences.USERNAME_KEY, ""));
		setAutoPassword(settings.getString(Preferences.PASSWORD_KEY, ""));
		
		if (lastLaunchVersionCode == 4) {
			try {
				autoPassword = SimpleCrypto.encrypt("comathenaasm", autoPassword);
				editor.putString(Preferences.PASSWORD_KEY, autoPassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		editor.commit();
		
		try {
			autoPassword = SimpleCrypto.decrypt("comathenaasm", autoPassword);
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
				for (Iterator<Board> iterator = recentBoards.iterator(); iterator
						.hasNext();) {
					Board board = (Board) iterator.next();
					if (recentBoardNameSet.contains(board.getEngName())) {
						toDeleteBoards.add(board);
					}
					else {
						recentBoardNameSet.add(board.getEngName());
					}
				}
				Log.d("com.athena.asm", "loading from file");
				fis.close();
				for (Iterator<Board> iterator = toDeleteBoards.iterator(); iterator
						.hasNext();) {
					Board board = (Board) iterator.next();
					recentBoards.remove(board);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		this.isRememberUser = isRememberUser;
	}
	public boolean isRememberUser() {
		return isRememberUser;
	}
	public void setAutoLogin(boolean isAutoLogin) {
		this.isAutoLogin = isAutoLogin;
	}
	public boolean isAutoLogin() {
		return isAutoLogin;
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
		this.autoUserName = autoUserName;
	}

	public String getAutoUserName() {
		return autoUserName;
	}

	public void setAutoPassword(String autoPassword) {
		this.autoPassword = autoPassword;
	}

	public String getAutoPassword() {
		return autoPassword;
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

	public void setTouchScroll(boolean isTouchScroll) {
		this.isTouchScroll = isTouchScroll;
	}
}
