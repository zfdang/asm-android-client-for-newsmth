package com.koushikdutta.urlimageviewhelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.http.NameValuePair;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

public final class UrlImageViewHelper {
    static void clog(String format, Object... args) {
        String log;
        if (args.length == 0)
            log = format;
        else
            log = String.format(format, args);
        if (Constants.LOG_ENABLED)
            Log.i(Constants.LOGTAG, log);
    }

    public static int copyStream(final InputStream input, final OutputStream output) throws IOException {
        final byte[] stuff = new byte[1024];
        int read = 0;
        int total = 0;
        while ((read = input.read(stuff)) != -1)
        {
            output.write(stuff, 0, read);
            total += read;
        }
        return total;
    }

    static Resources mResources;
    static DisplayMetrics mMetrics;
    private static void prepareResources(final Context context) {
        if (mMetrics != null) {
            return;
        }
        mMetrics = new DisplayMetrics();
        //final Activity act = (Activity)context;
        //act.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
        .getDefaultDisplay().getMetrics(mMetrics);
        final AssetManager mgr = context.getAssets();
        mResources = new Resources(mgr, mMetrics, context.getResources().getConfiguration());
    }


    /**
     * If bitmap can't be loaded successfully, errorResource will be draw to imageview
     * @param drawable resource
     */
    private static int mErrorResource = 0;
    public static void setErrorResource(int errorResource) {
        mErrorResource = errorResource;
    }


    private static boolean mUseZoomIn = true;
    private static boolean mUseZoomOut = true;
    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being smaller than the dimensions of the device itself.
     * @param useBitmapScaling Toggle for smart resizing.
     */
    public static void setUseZoomIn(boolean useZoomIn) {
        mUseZoomIn = useZoomIn;
    }
    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being smaller than the dimensions of the device itself.
     */
    public static boolean getUseZoomIn() {
        return mUseZoomIn;
    }

    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself.
     * Doing this will conserve memory.
     * @param useBitmapScaling Toggle for smart resizing.
     */
    public static void setUseZoomOut(boolean useZoomOut) {
        mUseZoomOut = useZoomOut;
    }
    /**
     * Bitmap scaling will use smart/sane values to limit the maximum
     * dimension of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself.
     * Doing this will conserve memory.
     */
    public static boolean getUseZoomOut() {
        return mUseZoomOut;
    }

    private static Drawable loadDrawableFromStream(final Context context, final String url, final String filename, final int targetWidth, final int targetHeight) {
        prepareResources(context);

        // clog(String.format("Target size: (%dx%d).", targetWidth, targetHeight));
        FileInputStream stream = null;
        // clog("Decoding: url=" + url + " filename=" + filename);
        try {
            BitmapFactory.Options o = null;

            Bitmap bitmap = null;
            if (mUseZoomOut || mUseZoomIn) {
                // decode image size (decode metadata only, not the whole image)
                o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                stream = new FileInputStream(filename);
                BitmapFactory.decodeStream(stream, null, o);
                stream.close();

                // get original image size
                int inWidth =  o.outWidth;
                int inHeight = o.outHeight;
                clog(String.format("Original bitmap size: (%dx%d).", inWidth, inHeight));

                // get size for pre-resized image
                o = new Options();
                o.inSampleSize = Math.max(inWidth/targetWidth, inHeight/targetHeight);
            }

            // decode pre-resized image
            stream = new FileInputStream(filename);
            // o.inPurgeable = true;
            bitmap = BitmapFactory.decodeStream(stream, null, o);
            stream.close();
            clog(String.format("Pre-sized bitmap size: (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));

            if (mUseZoomOut || mUseZoomIn) {
                // create bitmap which matches exactly with the target size
                float[] values = new float[9];
                // calc exact destination size
                // http://developer.android.com/reference/android/graphics/Matrix.ScaleToFit.html
                Matrix m = new Matrix();
                RectF inRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
                RectF outRect = new RectF(0, 0, targetWidth, targetHeight);
                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
                m.getValues(values);

                clog(String.format("Zoom: (%fx%f).", values[0], values[4]));
                if( mUseZoomOut && (values[0] < 1.0 || values[4] < 1.0) ){
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * values[0]),
                        (int) (bitmap.getHeight() * values[4]), true);
                    clog("Zoom out");
                }

                if( mUseZoomIn && (values[0] > 1.0 || values[4] > 1.0) ){
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * values[0]),
                            (int) (bitmap.getHeight() * values[4]), true);
                    clog("Zoom in");
                }
            }

            clog(String.format("Final bitmap size: (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));
            final BitmapDrawable bd = new BitmapDrawable(mResources, bitmap);
            return bd;
        } catch (final IOException e) {
            clog(e.toString());
            return null;
        } catch (final OutOfMemoryError e) {
            clog(e.toString());
            return null;
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Log.w(Constants.LOGTAG, "Failed to close FileInputStream", e);
                }
            }
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

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView} once it finishes loading.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    public static void loadUrlDrawable(final Context context, final String url) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs);
    }

    public static void loadUrlDrawable(final Context context, final String url, final long cacheDurationMs) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, CACHE_DURATION_THREE_DAYS, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, cacheDurationMs, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that
     *            should be displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final int defaultResource, final long cacheDurationMs, final UrlImageViewCallback callback) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, callback);
    }

    private static boolean isNullOrEmpty(final CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s.equals("NULL"));
    }

    public static String getFilenameForUrl(final String url) {
        return url.hashCode() + ".urlimage";
    }

    /**
     * Clear out cached images.
     * @param context
     * @param age The max age of a file. Files older than this age
     *              will be removed.
     */
    public static void cleanup(final Context context, long age) {
        clog(String.format("Cleanup, age=%d", age));
        try {
            // purge any *.urlimage files over "age" old
            final String[] files = context.getFilesDir().list();
            if (files == null) {
                clog("Cleanup, no cached files found");
                return;
            }
            for (final String file : files) {
                if (!file.endsWith(".urlimage")) {
                    continue;
                }

                final File f = new File(context.getFilesDir().getAbsolutePath() + '/' + file);
                if (System.currentTimeMillis() > f.lastModified() + age) {
                    f.delete();
                    clog(String.format("Cached file %s removed", file));
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear out all cached images older than a day
     * it will call cleanup(context, CACHE_DURATION_ONE_DAY);
     * @param context
     */
    private static boolean mHasCleaned = false;
    public static void cleanup(final Context context) {
        // only cleanup for the first time
        if(mHasCleaned)
            return;
        mHasCleaned = true;

        // purge any *.urlimage files over a week old
        cleanup(context, CACHE_DURATION_ONE_DAY);
    }
    
    private static boolean checkCacheDuration(File file, long cacheDurationMs) {
        return cacheDurationMs == CACHE_DURATION_INFINITE || System.currentTimeMillis() < file.lastModified() + cacheDurationMs;
    }
    

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context A {@link Context} to allow setUrlDrawable to load and save
     *            files.
     * @param imageView The {@link ImageView} to display the image to after it
     *            is loaded.
     * @param url The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *            {@code imageView} while the image has not been loaded. This
     *            image will also be displayed if the image fails to load. This
     *            can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this
     *            image should be cached locally.
     * @param callback An instance of {@link UrlImageViewCallback} that is
     *            called when the image successfully finishes loading. This
     *            value can be null.
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url, final Drawable defaultDrawable, final long cacheDurationMs, final UrlImageViewCallback callback) {
        Assert.assertTrue("setUrlDrawable and loadUrlDrawable should only be called from the main thread.", Looper.getMainLooper().getThread() == Thread.currentThread());
        cleanup(context);
        // disassociate this ImageView from any pending downloads
        if (isNullOrEmpty(url)) {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                imageView.setImageDrawable(defaultDrawable);
            }
            return;
        }

        final int tw;
        final int th;
        if (mMetrics == null)
            prepareResources(context);
        tw = mMetrics.widthPixels;
        th = mMetrics.heightPixels;

        final String filename = context.getFileStreamPath(getFilenameForUrl(url)).getAbsolutePath();
        final File file = new File(filename);
        clog(String.format("Target file cache = %s", filename));

        // mLiveCache
        Drawable drawable = mLiveCache.get(url);
        if( drawable != null){
            clog("loaded from live cache: " + url);
        }

        if (drawable != null) {
            clog("Cache hit on: " + url);
            // if the file age is older than the cache duration, force a refresh.
            // note that the file must exist, otherwise it is using a default.
            // not checking for file existence would do a network call on every
            // 404 or failed load.
            if (file.exists() && !checkCacheDuration(file, cacheDurationMs)) {
                clog("Cache hit, but file is stale. Forcing reload: " + url);
                drawable = null;
            }
            else {
                clog("Using cached image: " + url);
            }
        }

        if (drawable != null) {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                imageView.setImageDrawable(drawable);
            }
            if (callback != null) {
                callback.onLoaded(imageView, drawable, url, true);
            }
            return;
        }

        // oh noes, at this point we definitely do not have the file available in memory
        // let's prepare for an asynchronous load of the image.

        // null it while it is downloading
        // since listviews reuse their views, we need to
        // take note of which url this view is waiting for.
        // This may change rapidly as the list scrolls or is filtered, etc.
        clog("Waiting for " + url + " " + imageView);
        if (imageView != null) {
            imageView.setImageDrawable(defaultDrawable);
            mPendingViews.put(imageView, url);
        }

        final ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
        if (currentDownload != null) {
            // Also, multiple vies may be waiting for this url.
            // So, let's maintain a list of these views.
            // When the url is downloaded, it sets the imagedrawable for
            // every view in the list. It needs to also validate that
            // the imageview is still waiting for this url.
            if (imageView != null) {
                currentDownload.add(imageView);
            }
            return;
        }

        final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
        if (imageView != null) {
            downloads.add(imageView);
        }
        mPendingDownloads.put(url, downloads);

        final int targetWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
        final int targetHeight = th <= 0 ? Integer.MAX_VALUE : th;
        final Loader loader = new Loader() {
            @Override
            public void onDownloadComplete(UrlDownloader downloader, InputStream in, String existingFilename) {
                try {
                    Assert.assertTrue(in == null || existingFilename == null);
                    if (in == null && existingFilename == null)
                        return;
                    String targetFilename = filename;
                    if (in != null) {
                        FileOutputStream fout = new FileOutputStream(filename);
                        copyStream(in, fout);
                        fout.close();
                        clog("DownloadComplete: stream saved to cached file " + filename);
                    }
                    else {
                        targetFilename = existingFilename;
                        clog("DownloadComplete: use cached file " + existingFilename);
                    }
                    result = loadDrawableFromStream(context, url, targetFilename, targetWidth, targetHeight);
                }
                catch (final Exception ex) {
                    // always delete busted files when we throw.
                    new File(filename).delete();
                    if (Constants.LOG_ENABLED)
                        Log.e(Constants.LOGTAG, "Error loading " + url, ex);
                }
                finally {
                    // if we're not supposed to cache this thing, delete the temp file.
                    if (downloader != null && !downloader.allowCache())
                        new File(filename).delete();
                }
            }
        };

        final Runnable completion = new Runnable() {
            @Override
            public void run() {
                Assert.assertEquals(Looper.myLooper(), Looper.getMainLooper());
                Drawable usableResult = loader.result;
                if (usableResult == null) {
                    clog("No usable result: " + url);
                    if (mErrorResource != 0 && imageView != null){
                        usableResult = imageView.getResources().getDrawable(mErrorResource);;
                        clog("fallback to error resource");
                    } else {
                        usableResult = defaultDrawable;
                        clog("fallback to default resource");
                    }
                    // never cache fallback drawable
                    // mLiveCache.put(url, usableResult);
                } else {
                    // cache the result
                    // mLiveCache.put(url, usableResult);
                }
                mPendingDownloads.remove(url);
                if (callback != null && imageView == null)
                    callback.onLoaded(null, loader.result, url, false);
                int waitingCount = 0;
                for (final ImageView iv: downloads) {
                    // validate the url it is waiting for
                    final String pendingUrl = mPendingViews.get(iv);
                    if (!url.equals(pendingUrl)) {
                        clog("Ignoring out of date request to update view for " + url + " " + pendingUrl + " " + iv);
                        continue;
                    }
                    waitingCount++;
                    mPendingViews.remove(iv);
                    if (usableResult != null) {
//                        System.out.println(String.format("imageView: %dx%d, %dx%d", imageView.getMeasuredWidth(), imageView.getMeasuredHeight(), imageView.getWidth(), imageView.getHeight()));
                        iv.setImageDrawable(usableResult);
//                        System.out.println(String.format("imageView: %dx%d, %dx%d", imageView.getMeasuredWidth(), imageView.getMeasuredHeight(), imageView.getWidth(), imageView.getHeight()));
                        // onLoaded is called with the loader's result (not what is actually used). null indicates failure.
                    }
                    if (callback != null && iv == imageView)
                        callback.onLoaded(iv, loader.result, url, false);
                }
                clog("Populated: " + url + waitingCount);
            }
        };


        if (file.exists()) {
            try {
                if (checkCacheDuration(file, cacheDurationMs)) {
                    clog("File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");

                    final AsyncTask<Void, Void, Void> fileloader = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            loader.onDownloadComplete(null, null, filename);
                            return null;
                        }
                        @Override
                        protected void onPostExecute(final Void result) {
                            completion.run();
                        }
                    };
                    executeTask(fileloader);
                    return;
                }
                else {
                    clog("File cache has expired. Refreshing.");
                }
            }
            catch (final Exception ex) {
                clog(ex.toString());
            }
        }
        
        for (UrlDownloader downloader: mDownloaders) {
            if (downloader.canDownloadUrl(url)) {

                downloader.download(context, url, filename, loader, completion);
                return;
            }
        }
        
        imageView.setImageDrawable(defaultDrawable);
    }

    private static abstract class Loader implements UrlDownloader.UrlDownloaderCallback {
        Drawable result;
    }
    
    /*
    * max image size to be downloaded, in bytes
    *
    */
    private static long maxImageSizeThreshold = 0;
    public static void setMaxImageSize(long imageSize)
    {
        clog(String.format("Max Image Size: %d", imageSize));
        maxImageSizeThreshold = imageSize;
        mHttpDownloader.setMaxsizeToDownload(maxImageSizeThreshold);
    }
    public static long getMaxImageSize()
    {
        return maxImageSizeThreshold;
    }

    private static HttpUrlDownloader mHttpDownloader = new HttpUrlDownloader();
    private static ContentUrlDownloader mContentDownloader = new ContentUrlDownloader();
    private static ContactContentUrlDownloader mContactDownloader = new ContactContentUrlDownloader();
    private static FileUrlDownloader mFileDownloader = new FileUrlDownloader();
    private static ArrayList<UrlDownloader> mDownloaders = new ArrayList<UrlDownloader>();
    public static ArrayList<UrlDownloader> getDownloaders() {
        return mDownloaders;
    }
    
    static {
        mDownloaders.add(mHttpDownloader);
        mDownloaders.add(mContactDownloader);
        mDownloaders.add(mContentDownloader);
        mDownloaders.add(mFileDownloader);
    }
    
    public static interface RequestPropertiesCallback {
        public ArrayList<NameValuePair> getHeadersForRequest(Context context, String url);
    }

    private static RequestPropertiesCallback mRequestPropertiesCallback;

    public static RequestPropertiesCallback getRequestPropertiesCallback() {
        return mRequestPropertiesCallback;
    }

    public static void setRequestPropertiesCallback(final RequestPropertiesCallback callback) {
        mRequestPropertiesCallback = callback;
    }

    private static UrlImageCache mLiveCache = UrlImageCache.getInstance();

    static void executeTask(final AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT < Constants.HONEYCOMB) {
            task.execute();
        } else {
            executeTaskHoneycomb(task);
        }
    }

    @TargetApi(Constants.HONEYCOMB)
    private static void executeTaskHoneycomb(final AsyncTask<Void, Void, Void> task) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
}
