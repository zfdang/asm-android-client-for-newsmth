package com.athena.asm.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/*
 * With API-Level 14 (ICS) the View method canScrollHorizontally() has been introduced, 
 * so we can implement canScrollHorizontally for views in ViewPager to allow / disallow horizontal scroll.
 * but if you want this before ICS, it's necessary to extend ViewPager, and override canScroll
 * for more information:
 * http://stackoverflow.com/questions/7774642/scroll-webview-horizontally-inside-a-viewpager
 */
public class MyViewPager extends ViewPager {

    public MyViewPager(Context context) {
        super(context);
    }

    @Override
    protected boolean canScroll(View arg0, boolean arg1, int arg2, int arg3, int arg4) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH 
                && arg0 instanceof TouchImageView) {
            TouchImageView view = (TouchImageView) arg0;
            return view.canScrollHorizontally(-arg2);
        } else {
            return super.canScroll(arg0, arg1, arg2, arg3, arg4);
        }
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
