package com.athena.asm;

import com.athena.asm.data.Preferences;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {
	private CheckBoxPreference rememberUser;
	private CheckBoxPreference autoLogin;
	private ListPreference defaultTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		rememberUser = (CheckBoxPreference) findPreference(Preferences.REMEMBER_USER);
		autoLogin = (CheckBoxPreference) findPreference(Preferences.AUTO_LOGIN);
		defaultTab = (ListPreference) findPreference(Preferences.DEFAULT_TAB);
		rememberUser.setOnPreferenceChangeListener(this);
		rememberUser.setOnPreferenceClickListener(this);
		defaultTab.setOnPreferenceChangeListener(this);
		defaultTab.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(Preferences.REMEMBER_USER)) {
			if (!(Boolean)newValue) {
				autoLogin.setChecked(false);
			}
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		return true;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			HomeActivity.application.initPreferences();
			return super.onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
