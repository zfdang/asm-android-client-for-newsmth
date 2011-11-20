package com.athena.asm;

import java.util.ArrayList;

import com.athena.asm.data.Preferences;
import com.athena.asm.util.StringUtility;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class aSMApplication extends Application {
	private boolean isRememberUser = true;
	private boolean isAutoLogin = false;
	
	private String autoUserName = "";
	private String autoPassword = "";
	
	private String currentUserID = "";
	private String defaultTab = "001";
	private String defaultBoardType = "001";
	
	private int guidanceFontSize = 25;
	private int subjectFontSize = 15;
	private int postFontSize = 15;
	
	private ArrayList<String> blackList = new ArrayList<String>();
	
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
			editor.putString(Preferences.GUIDANCE_FONT_SIZE, "19");
		}
		else {
			String size = settings.getString(Preferences.GUIDANCE_FONT_SIZE, "25");
			guidanceFontSize = StringUtility.filterUnNumber(size);
			if (guidanceFontSize == 0) {
				guidanceFontSize = 25;
			}
		}
		
		if (!settings.contains(Preferences.SUBJECT_FONT_SIZE)) {
			editor.putString(Preferences.SUBJECT_FONT_SIZE, "15");
		}
		else {
			String size = settings.getString(Preferences.SUBJECT_FONT_SIZE, "15");
			subjectFontSize = StringUtility.filterUnNumber(size);
			if (subjectFontSize == 0) {
				subjectFontSize = 15;
			}
		}
		
		if (!settings.contains(Preferences.POST_FONT_SIZE)) {
			editor.putString(Preferences.POST_FONT_SIZE, "15");
		}
		else {
			String size = settings.getString(Preferences.POST_FONT_SIZE, "15");
			postFontSize = StringUtility.filterUnNumber(size);
			if (postFontSize == 0) {
				postFontSize = 15;
			}
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
		
		editor.commit();
		
		setAutoUserName(settings.getString(Preferences.USERNAME_KEY, ""));
		setAutoPassword(settings.getString(Preferences.PASSWORD_KEY, ""));
	}
	
	public void updateAutoUserNameAndPassword(String username, String password) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Preferences.USERNAME_KEY, username);
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
}
