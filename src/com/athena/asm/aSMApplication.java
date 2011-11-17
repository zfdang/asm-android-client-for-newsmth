package com.athena.asm;

import com.athena.asm.data.Preferences;

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
}
