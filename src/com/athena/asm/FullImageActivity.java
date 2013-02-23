package com.athena.asm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.athena.asm.util.StringUtility;
import com.athena.asm.view.TouchImageView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FullImageActivity extends SherlockActivity
	implements OnLongClickListener {

	private TouchImageView m_image;
	private String m_imageName;
	private String m_imageUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_image);

		m_imageName = getIntent().getStringExtra(StringUtility.IMAGE_NAME);
		if (m_imageName.trim().length() == 0) {
			m_imageName = "未命名";
		}
		m_imageUrl = getIntent().getStringExtra(StringUtility.IMAGE_URL);

		m_image = (TouchImageView) findViewById(R.id.image_view);
		m_image.setOnLongClickListener(this);

		UrlImageViewHelper.setUseZoomIn(true); // enable zoom in
		UrlImageViewHelper.setUseZoomOut(false); // don't zoom out
		UrlImageViewHelper.setMaxImageSize(0); // load all size
		if (aSMApplication.getCurrentApplication().isNightTheme()) {
			UrlImageViewHelper.setUrlDrawable(m_image, m_imageUrl, R.drawable.loading_night);
		} else {
			UrlImageViewHelper.setUrlDrawable(m_image, m_imageUrl, R.drawable.loading_day);
		}

		Toast.makeText(this, "长按出菜单,返回键退出", Toast.LENGTH_SHORT).show();

		setRequestedOrientation(aSMApplication.ORIENTATION);
	}


	@Override
	public boolean onLongClick(View arg0) {
			List<String> itemList = new ArrayList<String>();
			itemList.add(getString(R.string.full_image_save));
			itemList.add(getString(R.string.full_image_back));
			final String[] items = new String[itemList.size()];
			itemList.toArray(items);

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(String.format("图片: %s", m_imageName));
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 0:
						try {
							String fileName;
							if (m_imageName.equals("未命名")) {
								DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
								String time = formatter.format(new Date());
								fileName = time + m_imageName + ".png";
							} else {
								fileName = m_imageName;
							}

							if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
								String path = Environment.getExternalStorageDirectory().getPath() + "/aSM/images/";
								File dir = new File(path);
								if (!dir.exists()) {
									dir.mkdirs();
								}

								FileInputStream fis = openFileInput(UrlImageViewHelper.getFilenameForUrl(m_imageUrl));
								FileOutputStream fos = new FileOutputStream(path + fileName);

								BufferedInputStream bufr = new BufferedInputStream(fis);
								BufferedOutputStream bufw = new BufferedOutputStream(fos);

								int len = 0;
								byte[] buf = new byte[1024];
								while ((len = bufr.read(buf)) != -1) {
									bufw.write(buf, 0, len);
									bufw.flush();
								}
								bufw.close();
								bufr.close();

								Toast.makeText(FullImageActivity.this, "图片已存为SD卡下aSM/images/" + fileName,
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							Toast.makeText(FullImageActivity.this, "保存时出现未知错误.", Toast.LENGTH_SHORT).show();
						}
						break;
					case 1:
						onBackPressed();
						break;
					default:
						break;
					}
					dialog.dismiss();
				}
			});

			AlertDialog menuDialog = builder.create();
			menuDialog.show();

			return true;
		}

	@Override
	public void onBackPressed() {
		// expect post refresh
		// setResult(PostListActivity.RETURN_FROM_FULL_IMAGE, getIntent());

		// TODO Auto-generated method stub
		super.onBackPressed();
	}
}
