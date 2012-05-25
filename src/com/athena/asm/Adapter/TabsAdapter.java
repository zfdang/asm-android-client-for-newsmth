package com.athena.asm.Adapter;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.athena.asm.aSMApplication;

public class TabsAdapter extends FragmentPagerAdapter implements
		ActionBar.TabListener, ViewPager.OnPageChangeListener {
	private final SherlockFragmentActivity m_activity;
	private final ActionBar m_actionBar;
	private final ViewPager m_viewPager;
	private final ArrayList<TabInfo> m_tabs = new ArrayList<TabInfo>();

	static final class TabInfo {
		private final Class<?> clss;
		private final Bundle args;

		TabInfo(Class<?> _class, Bundle _args) {
			clss = _class;
			args = _args;
		}
	}

	public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
		super(activity.getSupportFragmentManager());
		m_activity = activity;
		m_actionBar = activity.getSupportActionBar();
		m_viewPager = pager;
		m_viewPager.setAdapter(this);
		m_viewPager.setOnPageChangeListener(this);
	}

	public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
		TabInfo info = new TabInfo(clss, args);
		tab.setTag(info);
		tab.setTabListener(this);
		m_tabs.add(info);
		m_actionBar.addTab(tab);
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return m_tabs.size();
	}

	@Override
	public Fragment getItem(int position) {
		TabInfo info = m_tabs.get(position);
		return Fragment.instantiate(m_activity, info.clss.getName(), info.args);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		m_actionBar.setSelectedNavigationItem(position);
		notifyDataChanged(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Object tag = tab.getTag();
		for (int i = 0; i < m_tabs.size(); i++) {
			if (m_tabs.get(i) == tag) {
				m_viewPager.setCurrentItem(i);
				notifyDataChanged(i);
				break;
			}
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}
	
	public void notifyDataChanged(int position) {
		aSMApplication application = (aSMApplication)m_activity.getApplication();
		switch (position) {
		case 0:
			application.getGuidanceListViewModel().notifyGuidanceChanged();
			break;
		case 1:
			application.getFavListViewModel().notifyFavListChanged();
		default:
			break;
		}
	}
}
