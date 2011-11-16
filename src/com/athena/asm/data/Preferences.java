package com.athena.asm.data;

import android.content.Context;

public class Preferences {
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "password";

	public static final String REMEMBER_USER = "remember_user";
	public static final String AUTO_LOGIN = "auto_login";
	public static final String DEFAULT_TAB = "default_tab";
	public static final String DEFAULT_BOARD_TYPE = "default_board_type";
	
	
	private static class Holder {
		private static Preferences instance = new Preferences();
	}
	
	public static Preferences getInstance() {
		return Holder.instance;
	}
	
	private Preferences() {
		init();
	}
	
	public void init() {
		
	}
	
	public void loadPreferences(Context context) {
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		// 准备统一读取preferences
	}
}
