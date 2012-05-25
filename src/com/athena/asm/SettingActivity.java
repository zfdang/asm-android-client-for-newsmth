package com.athena.asm;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;

import com.athena.asm.data.Preferences;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {
	private CheckBoxPreference m_rememberUser;
	private CheckBoxPreference m_autoLogin;
	private ListPreference m_defaultTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		m_rememberUser = (CheckBoxPreference) findPreference(Preferences.REMEMBER_USER);
		m_autoLogin = (CheckBoxPreference) findPreference(Preferences.AUTO_LOGIN);
		m_defaultTab = (ListPreference) findPreference(Preferences.DEFAULT_TAB);
		m_rememberUser.setOnPreferenceChangeListener(this);
		m_rememberUser.setOnPreferenceClickListener(this);
		m_defaultTab.setOnPreferenceChangeListener(this);
		m_defaultTab.setOnPreferenceClickListener(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals(Preferences.REMEMBER_USER)) {
			if (!(Boolean)newValue) {
				m_autoLogin.setChecked(false);
			}
		} else if (preference.getKey().equals(Preferences.NIGHT_THEME)) {
			if ((Boolean)newValue) {
				HomeActivity.THEME = R.style.Theme_Sherlock;
			} else {
				HomeActivity.THEME = R.style.Theme_Sherlock_Light;
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
			HomeActivity.m_application.initPreferences();
			Intent intent = new Intent(this, HomeActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;//super.onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
