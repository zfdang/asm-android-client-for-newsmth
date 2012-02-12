package com.athena.asm;

import java.io.File;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.athena.asm.util.StringUtility;

public class FileChooserActivity extends
        com.ipaulpro.afilechooser.FileChooserActivity {
    private static final String TAG = "FileSelectorTestActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the file chooser with all file types
        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            showFileChooser();
        }
        //showFileChooser("选择要上传的文件", "*/*");
        //showFileChooser("选择要上传的文件", null);
    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to stop onCreated
		super.onConfigurationChanged(newConfig);
	}

    @Override
    protected void onFileSelect(File file) {
        if (file != null) {
            //final Context context = getApplicationContext();

            // Get the path of the Selected File.
            //final String path = file.getAbsolutePath();
            //Log.d(TAG, "File path: " + path);

            // Get the MIME type of the Selected File.          
            //String mimeType = FileUtils.getMimeType(context, file);
            //Log.d(TAG, "File MIME type: " + mimeType);

            // Get the Uri of the Selected File
            // final Uri uri = Uri.fromFile(file);

            // Get the thumbnail of the Selected File, if image/video
            // final Bitmap bm = FileUtils.getThumbnail(context, uri, mimeType);

            // Here you can return any data from above to the calling Activity  
            Intent intent = new Intent();
            intent.putExtra(StringUtility.SELECTED_FILE, file);
            //intent.putExtra(StringUtility.SELECTED_FILE, path);
            setResult(RESULT_OK, intent);
            
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
