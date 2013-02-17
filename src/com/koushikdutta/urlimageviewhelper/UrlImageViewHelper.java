package com.koushikdutta.urlimageviewhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.athena.asm.HomeActivity;
import com.athena.asm.R;
import com.athena.asm.aSMApplication;
import com.athena.asm.util.SmthCrawler;

public final class UrlImageViewHelper {
	private static final String LOGTAG = "UrlImageViewHelper";

	public static int copyStream(InputStream input, OutputStream output)
			throws IOException {
		byte[] stuff = new byte[1024];
		int read = 0;
		int total = 0;
		while ((read = input.read(stuff)) != -1) {
			output.write(stuff, 0, read);
			output.flush();
			total += read;
		}
		return total;
	}

	static Resources mResources;
	static DisplayMetrics mMetrics;

	private static void prepareResources(Context context) {
		if (mMetrics != null)
			return;
		mMetrics = new DisplayMetrics();
		Activity act = (Activity) context;
		act.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
		AssetManager mgr = context.getAssets();
		mResources = new Resources(mgr, mMetrics, null);
	}

	private static int calculateSampleNumber(InputStream stream) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, opts);
		opts.inSampleSize = computeSampleSize(opts, -1, 240 * 400);
		opts.inJustDecodeBounds = false;

		return opts.inSampleSize;
	}

	private static BitmapDrawable loadDrawableFromStream(Context context,
			InputStream stream, int sampleSize) {
		prepareResources(context);

		BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
		sampleOptions.inSampleSize = sampleSize;

		final Bitmap bitmap = BitmapFactory.decodeStream(stream, null,
				sampleOptions);
		// Log.i(LOGTAG, String.format("Loaded bitmap (%dx%d).",
		// bitmap.getWidth(), bitmap.getHeight()));
		return new BitmapDrawable(mResources, bitmap);
	}

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
	public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
	public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
	public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
	public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
	public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
	public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
	public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource, final boolean isToScale) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				CACHE_DURATION_THREE_DAYS, isToScale);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, final boolean isToScale) {
		setUrlDrawable(imageView.getContext(), imageView, url, null,
				CACHE_DURATION_THREE_DAYS, isToScale);
	}

	public static void loadUrlDrawable(final Context context, final String url, final boolean isToScale) {
		setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, isToScale);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable, final boolean isToScale) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				CACHE_DURATION_THREE_DAYS, isToScale);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, int defaultResource, long cacheDurationMs, final boolean isToScale) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
				cacheDurationMs, isToScale);
	}

	public static void loadUrlDrawable(final Context context, final String url,
			long cacheDurationMs, final boolean isToScale) {
		setUrlDrawable(context, null, url, null, cacheDurationMs, isToScale);
	}

	public static void setUrlDrawable(final ImageView imageView,
			final String url, Drawable defaultDrawable, long cacheDurationMs, final boolean isToScale) {
		setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
				cacheDurationMs, isToScale);
	}

	private static void setUrlDrawable(final Context context,
			final ImageView imageView, final String url, int defaultResource,
			long cacheDurationMs, final boolean isToScale) {
		Drawable d = null;
		if (defaultResource != 0)
			d = imageView.getResources().getDrawable(defaultResource);
		setUrlDrawable(context, imageView, url, d, cacheDurationMs, isToScale);
	}

	private static boolean isNullOrEmpty(CharSequence s) {
		return (s == null || s.equals("") || s.equals("null") || s
				.equals("NULL"));
	}

	private static boolean mHasCleaned = false;

	public static String getFilenameForUrl(String url) {
		return "" + url.hashCode() + ".urlimage";
	}

	public static void cleanup(Context context) {
		if (mHasCleaned)
			return;
		mHasCleaned = true;
		try {
			// purge any *.urlimage files over a week old
			String[] files = context.getFilesDir().list();
			if (files == null)
				return;
			for (String file : files) {
				if (!file.endsWith(".urlimage"))
					continue;

				File f = context.getFileStreamPath(file);//new File(file);
				if (System.currentTimeMillis() > f.lastModified()
						+ CACHE_DURATION_TWO_DAYS) {
					f.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void setUrlDrawable(final Context context,
			final ImageView imageView, final String url,
			final Drawable defaultDrawable, long cacheDurationMs, final boolean isToScale) {
		cleanup(context);
		// disassociate this ImageView from any pending downloads
		if (imageView != null)
			mPendingViews.remove(imageView);

		if (isNullOrEmpty(url)) {
			if (imageView != null)
				imageView.setImageDrawable(defaultDrawable);
			return;
		}

		final UrlImageCache cache = UrlImageCache.getInstance();
		Drawable d = cache.get(url);
		if (d != null && isToScale) {
			// Log.i(LOGTAG, "Cache hit on: " + url);
			if (imageView != null)
				imageView.setImageDrawable(d);
			return;
		}

		final String filename = getFilenameForUrl(url);

		File file = context.getFileStreamPath(filename);
		if (file.exists()) {
			try {
				if (cacheDurationMs == CACHE_DURATION_INFINITE
						|| System.currentTimeMillis() < file.lastModified()
								+ cacheDurationMs) {
					// Log.i(LOGTAG, "File Cache hit on: " + url + ". " +
					// (System.currentTimeMillis() - file.lastModified()) +
					// "ms old.");
					FileInputStream fis = context.openFileInput(filename);
					int sampleSize = 1;
					if (isToScale) {
						sampleSize = calculateSampleNumber(fis);
					}
					fis.close();
					fis = context.openFileInput(filename);
					BitmapDrawable drawable = loadDrawableFromStream(context,
							fis, sampleSize);
					fis.close();
					if (imageView != null)
						imageView.setImageDrawable(drawable);
					cache.put(url, drawable);
					return;
				} else {
					// Log.i(LOGTAG, "File cache has expired. Refreshing.");
				}
			} catch (Exception ex) {
			}
		}

		// null it while it is downloading
		if (imageView != null)
			imageView.setImageDrawable(defaultDrawable);

		// since listviews reuse their views, we need to
		// take note of which url this view is waiting for.
		// This may change rapidly as the list scrolls or is filtered, etc.
		// Log.i(LOGTAG, "Waiting for " + url);
		if (imageView != null)
			mPendingViews.put(imageView, url);

		ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
		if (currentDownload != null) {
			// Also, multiple vies may be waiting for this url.
			// So, let's maintain a list of these views.
			// When the url is downloaded, it sets the imagedrawable for
			// every view in the list. It needs to also validate that
			// the imageview is still waiting for this url.
			if (imageView != null)
				currentDownload.add(imageView);
			return;
		}

		final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
		if (imageView != null)
			downloads.add(imageView);
		mPendingDownloads.put(url, downloads);

		AsyncTask<Void, Void, Drawable> downloader = new AsyncTask<Void, Void, Drawable>() {
			@Override
			protected Drawable doInBackground(Void... params) {
				try {
					DefaultHttpClient client = new DefaultHttpClient();
					HttpGet get = new HttpGet(url);
					get.setHeader("User-Agent", SmthCrawler.userAgent);
					get.addHeader("Accept-Encoding", "gzip, deflate");
					HttpResponse resp = client.execute(get);
					int status = resp.getStatusLine().getStatusCode();
					if (status != HttpURLConnection.HTTP_OK) {
						// Log.i(LOGTAG, "Couldn't download image from Server: "
						// + url + " Reason: " +
						// resp.getStatusLine().getReasonPhrase() + " / " +
						// status);
						return null;
					}
					HttpEntity entity = resp.getEntity();
					Header header = entity.getContentType();
					Log.d(LOGTAG, header.getValue());
					if (header.getValue().contains("image")) {
						float size = entity.getContentLength();

						boolean isToLoad = checkIsToLoadImage(context, size);
						if (isToLoad) {
							// Log.i(LOGTAG, url + " Image Content Length: " +
							// size);
							InputStream is = entity.getContent();
							FileOutputStream fos = context.openFileOutput(
									filename, Context.MODE_PRIVATE);
							copyStream(is, fos);
							fos.close();
							is.close();
							FileInputStream fis = context
									.openFileInput(filename);
							int sampleSize = 1;
							if (isToScale) {
								sampleSize = calculateSampleNumber(fis);
							}
							fis.close();
							fis = context.openFileInput(filename);
							return loadDrawableFromStream(context, fis,
									sampleSize);
						}
					} /*
					 * else { String content = EntityUtils.toString(entity);
					 * Log.d(LOGTAG, content); }
					 */

					return null;
				} catch (Exception ex) {
					ex.printStackTrace();
					Log.e(LOGTAG, "Exception during Image download of " + url,
							ex);
					return null;
				}
			}

			protected void onPostExecute(Drawable result) {
				if (result == null)
					result = imageView.getResources().getDrawable(
							R.drawable.defalutimage);
				mPendingDownloads.remove(url);
				cache.put(url, result);
				for (ImageView iv : downloads) {
					// validate the url it is waiting for
					String pendingUrl = mPendingViews.get(iv);
					if (!url.equals(pendingUrl)) {
						// Log.i(LOGTAG,
						// "Ignoring out of date request to update view for " +
						// url);
						continue;
					}
					mPendingViews.remove(iv);
					if (result != null) {
						final Drawable newImage = result;
						final ImageView imageView = iv;
						imageView.setImageDrawable(newImage);
					}
				}
			}
		};
		downloader.execute();
	}

	private static boolean checkIsToLoadImage(Context context, float imageSize) {
		boolean isAutoOptimize = aSMApplication.getCurrentApplication().isAutoOptimize();
		// 自动优化
		if (isAutoOptimize) {
			ConnectivityManager connectionManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
			int netType = networkInfo.getType();
			// WIFI下全部下载
			if (netType == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		float threshold = aSMApplication.getCurrentApplication().getImageSizeThreshold();
		// 非自动优化或者自动优化但在移动网络中，需阈值判断
		if (threshold == 0 || imageSize < threshold * 1024) {
			return true;
		}

		return false;
	}

	private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
	private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
