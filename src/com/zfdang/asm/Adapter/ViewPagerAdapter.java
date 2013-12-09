package com.zfdang.asm.Adapter;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.zfdang.asm.R;
import com.zfdang.asm.aSMApplication;
import com.zfdang.asm.view.TouchImageView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class ViewPagerAdapter extends PagerAdapter {

//	private TouchImageView m_currentView;
	private Context m_context;
	ArrayList<String> m_imageUrls;

	public ViewPagerAdapter(ArrayList<String> imgUrls, Context context) {
		m_imageUrls = imgUrls;
		m_context = context;
	}

	@Override
	public int getCount() {
		if(m_imageUrls != null)
			return m_imageUrls.size();
		return 0;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		TouchImageView view = (TouchImageView)object;
		((ViewPager) container).removeView(view);
		view = null;
	}

	@Override
	public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        // set parameters for UrlImageViewHelper
		UrlImageViewHelper.setUseZoomIn(false); // enable zoom in
		UrlImageViewHelper.setUseZoomOut(false); // don't zoom out
		UrlImageViewHelper.setMaxImageSize(0); // load all size

		// initialize TouchImageView
		String imageUrl = m_imageUrls.get(position);
		TouchImageView iv = new TouchImageView(m_context);
		iv.setLayoutParams(mParams);
		iv.setOnLongClickListener((OnLongClickListener)m_context);
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			UrlImageViewHelper.setErrorResource(R.drawable.failure_night);
			UrlImageViewHelper.setUrlDrawable(iv, imageUrl, R.drawable.loading_night);
		} else {
			UrlImageViewHelper.setErrorResource(R.drawable.failure_day);
			UrlImageViewHelper.setUrlDrawable(iv, imageUrl, R.drawable.loading_day);
		}
		((ViewPager) container).addView(iv, 0);
        return iv;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return (arg0 == arg1);
	}

}
