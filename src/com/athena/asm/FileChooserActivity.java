package com.athena.asm;

import java.io.File;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class FileChooserActivity extends
        com.ipaulpro.afilechooser.FileChooserActivity {
    private static final String TAG = "FileSelectorTestActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the file chooser with all file types
        showFileChooser("选择要上传的文件", "*/*");
    }

    @Override
    protected void onFileSelect(File file) {
        if (file != null) {
            final Context context = getApplicationContext();

            // Get the path of the Selected File.
            final String path = file.getAbsolutePath();
            Log.d(TAG, "File path: " + path);

            // Get the MIME type of the Selected File.          
            String mimeType = FileUtils.getMimeType(context, file);
            Log.d(TAG, "File MIME type: " + mimeType);

            // Get the Uri of the Selected File
            // final Uri uri = Uri.fromFile(file);

            // Get the thumbnail of the Selected File, if image/video
            // final Bitmap bm = FileUtils.getThumbnail(context, uri, mimeType);

            // Here you can return any data from above to the calling Activity  
            finish();
        }
    }

    @Override
    protected void onFileError(Exception e) {
        Log.e(TAG, "File select error", e);
        finish();
    }

    @Override
    protected void onFileSelectCancel() {
        Log.d(TAG, "File selections canceled");
        finish();
    }

    @Override
    protected void onFileDisconnect() {
        Log.d(TAG, "External storage disconneted");
        finish();
    }
}
