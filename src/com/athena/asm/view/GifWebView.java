package com.athena.asm.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.os.Environment;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;
import android.widget.Toast;

import com.athena.asm.R;
import com.athena.asm.util.task.DownloadFileTask;

public class GifWebView extends WebView {

    private String m_url;
    private String m_imageName;

    public GifWebView(Context context, String url, String imageName) {
        super(context);
        m_url = url;
        m_imageName = imageName;

        String content = String.format("<html><body><img src=\"%s\" width=100%%></body></html>", m_url);
        loadData(content, "text/html", null);
    }

    void saveImage() {
        // save image to sdcard
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/aSM/images/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                DownloadFileTask task = new DownloadFileTask(getContext(), m_url, m_imageName, path);
                task.execute();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "保存时出现未知错误.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onLongClickEvent() {
        this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

        // build menu for long click
        List<String> itemList = new ArrayList<String>();
        itemList.add(getContext().getString(R.string.full_image_save));
        itemList.add(getContext().getString(R.string.full_image_back));
        final String[] items = new String[itemList.size()];
        itemList.toArray(items);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(String.format("图片: %s", m_imageName));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                case 0:
                    saveImage();
                    break;
                case 1:
                    dialog.dismiss();
                    break;
                default:
                    break;
                }
                dialog.dismiss();
            }
        });

        AlertDialog menuDialog = builder.create();
        menuDialog.show();
        return;
    }

    /**
     * flag to wait long click event
     */
    private Handler mHandler = null;
    private PendingCheckForLongClick mPendingCheckForLongClick = null;
    private boolean mWaitingForLongClick;
    static final int LONG_CLICK_TOLERANCE = 20;
    PointF start = new PointF();
    PointF last = new PointF();

    class PendingCheckForLongClick implements Runnable {
        public void run() {
            // Log.d("PendingCheckForLongClick", "run");
            if (mWaitingForLongClick) {
                mWaitingForLongClick = false;
                onLongClickEvent();
            }
        }
    }

    private void checkForLongClick(int delayOffset) {
        // Log.d("checkForLongClick", "check");
        mWaitingForLongClick = true;

        if (mHandler == null) {
            mHandler = new Handler();
        }
        if (mPendingCheckForLongClick != null) {
            mHandler.removeCallbacks(mPendingCheckForLongClick);
        } else {
            mPendingCheckForLongClick = new PendingCheckForLongClick();
        }
        mHandler.postDelayed(mPendingCheckForLongClick, ViewConfiguration.getLongPressTimeout() - delayOffset);
    }

    private void clearCheckForLongClick() {
        // Log.d("ClearCheckForLongClick", "clear");
        mWaitingForLongClick = false;
        if (mHandler != null && mPendingCheckForLongClick != null) {
            mHandler.removeCallbacks(mPendingCheckForLongClick);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF curr = new PointF(event.getX(), event.getY());

        int xDiff = (int) Math.abs(curr.x - start.x);
        int yDiff = (int) Math.abs(curr.y - start.y);
        // Log.d("OnTouch", String.format("%d, %d, action=%d", xDiff, yDiff,
        // event.getAction()));
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            last.set(curr);
            start.set(last);
            checkForLongClick(0);
            break;
        case MotionEvent.ACTION_MOVE:
            if (xDiff > LONG_CLICK_TOLERANCE || yDiff > LONG_CLICK_TOLERANCE) {
                clearCheckForLongClick();
            }
            break;
        default:
            clearCheckForLongClick();
            break;
        }
        return true; // indicate event was handled
    }
}
