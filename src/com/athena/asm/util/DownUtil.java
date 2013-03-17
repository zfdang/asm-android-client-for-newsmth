package com.athena.asm.util;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/***
 * DownUtil
 * 
 * @author zhangjia
 * 
 */
public class DownUtil {
	private static final int TIMEOUT = 10 * 1000;

	/***
	 * 下载文件
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public void downloadUpdateFile(String down_url, String file)
			throws Exception {
		int down_step = 5;// 提示step
		int totalSize;
		int downloadCount = 0;// 已经下载好的大小
		InputStream inputStream;
		OutputStream outputStream;

		URL url = new URL(down_url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// 获取下载文件的size
		totalSize = httpURLConnection.getContentLength();
		if (httpURLConnection.getResponseCode() == 404) {
			throw new Exception("fail!");
		}
		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// 文件存在则覆盖掉
		byte buffer[] = new byte[1024];
		int readsize = 0;
		while ((readsize = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, readsize);
			totalSize += readsize;// 时时获取下载到的大小

			/**
			 * 没
			 */
			if (downloadCount == 0
					|| (totalSize * 100 / totalSize - down_step) > downloadCount) {

			}

		}
		outputStream.close();
	}
}
