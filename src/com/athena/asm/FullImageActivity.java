package com.athena.asm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.athena.asm.util.StringUtility;
import com.athena.asm.view.TouchImageView;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FullImageActivity extends Activity implements OnClickListener {

	private TouchImageView m_image;
	private String m_imageName;
	private String m_imageUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.full_image);

		m_imageName = getIntent().getStringExtra(StringUtility.IMAGE_NAME);
		if (m_imageName.trim().length() == 0) {
			m_imageName = "未命名";
		}
		m_imageUrl = getIntent().getStringExtra(StringUtility.IMAGE_URL);

		TextView titleTextView = (TextView) findViewById(R.id.image_title);
		titleTextView.setText(m_imageName);

		ImageButton saveImageButton = (ImageButton) findViewById(R.id.save_image);
		saveImageButton.setOnClickListener(this);

		m_image = (TouchImageView) findViewById(R.id.image_view);
		
		UrlImageViewHelper.setUrlDrawable(m_image, m_imageUrl,
				R.drawable.loading, 60000, false);

	}

	@Override
	public void onClick(View v) {
		ProgressDialog pdialog = new ProgressDialog(this);
		pdialog.setMessage("正在保存中...");
		pdialog.show();
		try {
			String fileName;
			if (m_imageName.equals("未命名")) {
				DateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd-HH-mm-ss");
				String time = formatter.format(new Date());
				fileName = time + m_imageName + ".png";
			} else {
				fileName = m_imageName;
			}

			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String path = "/sdcard/aSM/images/";
				File dir = new File(path);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				FileInputStream fis = this.openFileInput(UrlImageViewHelper
						.getFilenameForUrl(m_imageUrl));
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

				Toast.makeText(this, "已保存到SD卡的aSM/images目录下.",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			Toast.makeText(this, "保存时出现未知错误.", Toast.LENGTH_SHORT).show();
		}
		pdialog.cancel();
	}
}
