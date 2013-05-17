package com.athena.asm.Adapter;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Hashtable;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.athena.asm.aSMApplication;
import com.athena.asm.listener.OnKeyDownListener;
import com.athena.asm.viewmodel.HomeViewModel;

public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
    private final SherlockFragmentActivity m_activity;
    private final ActionBar m_actionBar;
    private final ViewPager m_viewPager;
    private final ArrayList<TabInfo> m_tabs = new ArrayList<TabInfo>();
    private HomeViewModel m_viewModel;
    private boolean m_isInited = false;

    // save cached fragments which implement OnKeyDownListener
    private Hashtable<Integer, SoftReference<OnKeyDownListener>> m_keyListeners = new Hashtable<Integer, SoftReference<OnKeyDownListener>>();

    static final class TabInfo {
        private final Class<?> clss;
        private final Bundle args;

        TabInfo(Class<?> _class, Bundle _args) {
            clss = _class;
            args = _args;
        }
    }

    public void finishInit() {
        m_isInited = true;
    }

    public TabsAdapter(SherlockFragmentActivity activity, ViewPager pager) {
        super(activity.getSupportFragmentManager());
        m_activity = activity;
        m_viewModel = ((aSMApplication) activity.getApplication()).getHomeViewModel();
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
        Fragment m = Fragment.instantiate(m_activity, info.clss.getName(), info.args);
        // cache the fragment as SoftReference object
        if (m instanceof OnKeyDownListener) {
            // only if the fragment has implemented OnKeyDownListener interface
            OnKeyDownListener listener = (OnKeyDownListener) m;
            m_keyListeners.put(position, new SoftReference<OnKeyDownListener>(listener));
        }
        return m;
    }

    // get keydown listener by position, null might be returned
    public OnKeyDownListener getOnKeyDownListener(int position) {
        try {
            SoftReference<OnKeyDownListener> sf = m_keyListeners.get(position);
            OnKeyDownListener listener = sf.get();
            return listener;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return null;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (m_isInited) {
            m_actionBar.setSelectedNavigationItem(position);
            int tab = position + 1;
            m_viewModel.setCurrentTab("00" + tab);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        if (m_isInited) {
            Object tag = tab.getTag();
            for (int i = 0; i < m_tabs.size(); i++) {
                if (m_tabs.get(i) == tag) {
                    m_viewPager.setCurrentItem(i);
                    int index = i + 1;
                    m_viewModel.setCurrentTab("00" + index);
                    break;
                }
            }
        }
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
}
