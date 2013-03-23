package com.athena.asm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.athena.asm.Adapter.ViewPagerAdapter;
import com.athena.asm.util.StringUtility;
import com.athena.asm.view.MyViewPager;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FullImageActivity extends SherlockActivity
	implements OnLongClickListener, OnPageChangeListener {

	private MyViewPager vp;
    private ViewPagerAdapter vpAdapter;

    // pagination navigator current position
	private TextView m_tv;
	private int m_imageIdx;

    private ArrayList<String> m_imageNames;
	private ArrayList<String> m_imageUrls;
	private String m_imageName;
	private String m_imageUrl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setTheme(aSMApplication.THEME);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.full_image);

        // get page indicator textview
        m_tv = (TextView) findViewById(R.id.full_image_indicator);

		m_imageNames = getIntent().getStringArrayListExtra (StringUtility.IMAGE_NAME);
		m_imageUrls = getIntent().getStringArrayListExtra (StringUtility.IMAGE_URL);
		m_imageIdx = getIntent().getIntExtra(StringUtility.IMAGE_INDEX, 0);

        vp = (MyViewPager) findViewById(R.id.myviewpager);
        vpAdapter = new ViewPagerAdapter(m_imageUrls, this);
        vp.setAdapter(vpAdapter);
        vp.setOnPageChangeListener(this);
        vp.setCurrentItem(m_imageIdx);
        setCurIndicator(m_imageIdx);

//		setRequestedOrientation(aSMApplication.ORIENTATION);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public static final int MENU_EXIF = Menu.FIRST;
	public static final int MENU_SAVE = Menu.FIRST + 1;
	public static final int MENU_BACK = Menu.FIRST + 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_EXIF, Menu.NONE, getString(R.string.full_image_information));
		menu.add(0, MENU_SAVE, Menu.NONE, getString(R.string.full_image_save));
		menu.add(0, MENU_BACK, Menu.NONE, getString(R.string.full_image_back));

		return super.onCreateOptionsMenu(menu);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		updateImageInfoByIndex();

		switch (item.getItemId()) {
		case MENU_EXIF:
			showExifDialog();
			break;
		case MENU_SAVE:
			saveImage();
			break;
		case MENU_BACK:
			onBackPressed();
			break;
		}
		return true;
	}

    /**
     * set current page indicator "N/M"
     */
    private void setCurIndicator(int position)
    {
		m_imageIdx = position;
		String pos = String.format("%s/%s", position+1, m_imageUrls.size());
		m_tv.setText(pos);
    }

	// get image attribute from exif
	private void setImageAttributeFromExif(View layout, int tv_id, ExifInterface exif, String attr){
		if (layout == null || exif == null)
			return;
		TextView tv = (TextView) layout.findViewById(tv_id);
		if (tv == null){
			Log.d("setImageAttributeFromExif", "Invalid resource ID: " + tv_id);
			return;
		}
		String attribute = exif.getAttribute(attr);
		if (attribute != null){
			// there are some special treatment
			// http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html
			if(attr.equals(ExifInterface.TAG_APERTURE)){
				attribute = "F/" + attribute;
			} else if(attr.equals(ExifInterface.TAG_EXPOSURE_TIME)){
				try{
					float f = Float.parseFloat(attribute);
					if(f >= 1.0){
						attribute = attribute + " s";
					} else if ( f >= 0.1 ){
						f = 1/f;
						BigDecimal exposure = new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP);
						attribute = "1/" + exposure.toString() + " s";
					} else {
						f = 1/f/10;
						BigDecimal exposure = new BigDecimal(f).setScale(0, BigDecimal.ROUND_HALF_UP);
						exposure = exposure.multiply(new BigDecimal(10));
						attribute = "1/" + exposure.toString() + " s";
					}
				} catch(NumberFormatException e){
					Log.d("Can't convert exposure:", attribute);
				}
			} else if(attr.equals(ExifInterface.TAG_FLASH)){
				int flash = Integer.parseInt(attribute);
				switch(flash){
				case 0x0:
					attribute += " (No Flash)";
					break;
				case 0x1:
					attribute += " (Fired)";
					break;
				case 0x5:
					attribute += " (Fired, Return not detected)";
					break;
				case 0x7:
					attribute += " (Fired, Return detected)";
					break;
				case 0x8:
					attribute += " (On, Did not fire)";
					break;
				case 0x9:
					attribute += " (On, Fired)";
					break;
				case 0xd:
					attribute += " (On, Return not detected)";
					break;
				case 0xf:
					attribute += " (On, Return detected)";
					break;
				case 0x10:
					attribute += " (Off, Did not fire)";
					break;
				case 0x14:
					attribute += " (Off, Did not fire, Return not detected)";
					break;
				case 0x18:
					attribute += " (Auto, Did not fire)";
					break;
				case 0x19:
					attribute += " (Auto, Fired)";
					break;
				case 0x1d:
					attribute += " (Auto, Fired, Return not detected)";
					break;
				case 0x1f:
					attribute += " (Auto, Fired, Return detected)";
					break;
				case 0x20:
					attribute += " (No flash function)";
					break;
				case 0x30:
					attribute += " (Off, No flash function)";
					break;
				case 0x41:
					attribute += " (Fired, Red-eye reduction)";
					break;
				case 0x45:
					attribute += " (Fired, Red-eye reduction, Return not detected)";
					break;
				case 0x47:
					attribute += " (Fired, Red-eye reduction, Return detected)";
					break;
				case 0x49:
					attribute += " (On, Red-eye reduction)";
					break;
				case 0x4d:
					attribute += " (On, Red-eye reduction, Return not detected)";
					break;
				case 0x4f:
					attribute += " (On, Red-eye reduction, Return detected)";
					break;
				case 0x50:
					attribute += " (Off, Red-eye reduction)";
					break;
				case 0x58:
					attribute += " (Auto, Did not fire, Red-eye reduction)";
					break;
				case 0x59:
					attribute += " (Auto, Fired, Red-eye reduction)";
					break;
				case 0x5d:
					attribute += " (Auto, Fired, Red-eye reduction, Return not detected)";
					break;
				case 0x5f:
					attribute += " (Auto, Fired, Red-eye reduction, Return detected)";
					break;
				default:
					break;
				}
			} else if(attr.equals(ExifInterface.TAG_WHITE_BALANCE)){
				int wb = Integer.parseInt(attribute);
				switch(wb){
				case 0:
					attribute += " (Auto)";
					break;
				case 1:
					attribute += " (Manual)";
					break;
				}
			}
			tv.setText(attribute);
		}
	}

	void showExifDialog(){
		// show exif information dialog
		LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.image_info, (ViewGroup)findViewById(R.id.image_info_layout));
        try {
			String filename = UrlImageViewHelper.getFilenameForUrl(m_imageUrl);
	        File file = new File(getExternalFilesDir(null), filename);
	        String sFileName =  file.getAbsolutePath();

			ExifInterface exif = new ExifInterface(sFileName);
			// basic information
			setImageAttributeFromExif(layout, R.id.ii_datetime, exif, ExifInterface.TAG_DATETIME);
			setImageAttributeFromExif(layout, R.id.ii_width, exif, ExifInterface.TAG_IMAGE_WIDTH);
			setImageAttributeFromExif(layout, R.id.ii_height, exif, ExifInterface.TAG_IMAGE_LENGTH);

			// get filesize
			FileInputStream fis = new FileInputStream(file);
			int filesize = fis.available();
			fis.close();
			TextView tv = (TextView) layout.findViewById(R.id.ii_size);
			if(tv != null) {
				tv.setText(FileUtils.byteCountToDisplaySize(filesize));
			}

			// capture information
			setImageAttributeFromExif(layout, R.id.ii_make, exif, ExifInterface.TAG_MAKE);
			setImageAttributeFromExif(layout, R.id.ii_model, exif, ExifInterface.TAG_MODEL);
			setImageAttributeFromExif(layout, R.id.ii_focal_length, exif, ExifInterface.TAG_FOCAL_LENGTH);
			setImageAttributeFromExif(layout, R.id.ii_aperture, exif, ExifInterface.TAG_APERTURE);
			setImageAttributeFromExif(layout, R.id.ii_exposure_time, exif, ExifInterface.TAG_EXPOSURE_TIME);
			setImageAttributeFromExif(layout, R.id.ii_flash, exif, ExifInterface.TAG_FLASH);
			setImageAttributeFromExif(layout, R.id.ii_iso, exif, ExifInterface.TAG_ISO);
			setImageAttributeFromExif(layout, R.id.ii_white_balance, exif, ExifInterface.TAG_WHITE_BALANCE);
       } catch (IOException e){
           Log.d("read ExifInfo", "can't read Exif information");
       }

       new AlertDialog.Builder(FullImageActivity.this).setTitle("图片信息").setView(layout)
         .setPositiveButton("确定", null)
         .show();
	}

	void saveImage(){
		// save image to sdcard
		try {
			String fileName;
			if (m_imageName.equals("未命名")) {
				String time = StringUtility.getFormattedString(new Date());
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

				File file = new File(getExternalFilesDir(null),
					UrlImageViewHelper.getFilenameForUrl(m_imageUrl));
				FileInputStream fis = new FileInputStream(file);
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
	}

	void updateImageInfoByIndex(){
		// update imageName and imageURL
		m_imageName = m_imageNames.get(m_imageIdx);
        if (m_imageName.trim().length() == 0) {
            m_imageName = "未命名";
        }
        m_imageUrl = m_imageUrls.get(m_imageIdx);
	}

	@Override
	public boolean onLongClick(View arg0) {
			updateImageInfoByIndex();

            // build menu for long click
			List<String> itemList = new ArrayList<String>();
			itemList.add(getString(R.string.full_image_information));
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
						showExifDialog();
			           break;
					case 1:
						saveImage();
						break;
					case 2:
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

	/*
	 * three methods from OnPageChangeListener
	 */
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		setCurIndicator(arg0);
	}
}
