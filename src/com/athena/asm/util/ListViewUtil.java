package com.athena.asm.util;

import android.view.KeyEvent;
import android.widget.ListView;

public class ListViewUtil {

    public ListViewUtil() {
        // TODO Auto-generated constructor stub
    }

    /*
     * Scroll the ListView by VOLUME DOWN/UP
     * return true if listview was scrolled
     * otherwise return false
     */
    public static boolean ScrollListViewByKey(ListView lv, int keyCode){
        if(lv != null){
            int offset = (int) (lv.getHeight() * 0.95);
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                lv.smoothScrollBy(offset, 500);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                lv.smoothScrollBy(-1 * offset, 500);
                return true;
            }
        }
        return false;
    }
}
