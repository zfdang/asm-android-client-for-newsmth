package com.koushikdutta.urlimageviewhelper;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.apache.http.NameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper.RequestPropertiesCallback;

public class HttpUrlDownloader implements UrlDownloader {
    private RequestPropertiesCallback mRequestPropertiesCallback;
    private long maxSizeThreshold = 0;

    public RequestPropertiesCallback getRequestPropertiesCallback() {
        return mRequestPropertiesCallback;
    }

    public void setRequestPropertiesCallback(final RequestPropertiesCallback callback) {
        mRequestPropertiesCallback = callback;
    }


    @Override
    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final Runnable completion) {
        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                try {
                    UrlImageViewHelper.clog("Downloading URL " + url);
                    InputStream is = null;

                    String thisUrl = url;
                    HttpURLConnection urlConnection;
                    while (true) {
                        final URL u = new URL(thisUrl);
                        urlConnection = (HttpURLConnection)u.openConnection();
                        urlConnection.setInstanceFollowRedirects(true);

                        if (mRequestPropertiesCallback != null) {
                            final ArrayList<NameValuePair> props = mRequestPropertiesCallback.getHeadersForRequest(context, url);
                            if (props != null) {
                                for (final NameValuePair pair: props) {
                                    urlConnection.addRequestProperty(pair.getName(), pair.getValue());
                                }
                            }
                        }

                        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM)
                            break;
                        thisUrl = urlConnection.getHeaderField("Location");
                    }

                    if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        UrlImageViewHelper.clog("Response Code: " + urlConnection.getResponseCode());
                        return null;
                    }
                    // check response size
                    int contentSize = urlConnection.getContentLength();
                    if( maxSizeThreshold != 0 &&  contentSize > maxSizeThreshold ){
                        UrlImageViewHelper.clog(String.format("Download abort, size %d > %d, %s", contentSize, maxSizeThreshold, url));
                        return null;
                    } else {
                        UrlImageViewHelper.clog(String.format("Image size %d < threshold %d, %s", contentSize, maxSizeThreshold, url));
                    }
                    is = urlConnection.getInputStream();
                    callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
                    return null;
                }
                catch (final Throwable e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final Void result) {
                UrlImageViewHelper.clog("Finish download URL " + url);
                completion.run();
            }
        };

        UrlImageViewHelper.executeTask(downloader);
    }

    @Override
    public boolean allowCache() {
        return true;
    }
    
    @Override
    public boolean canDownloadUrl(String url) {
        return url.startsWith("http");
    }

    @Override
    public void setMaxsizeToDownload(long size){
        maxSizeThreshold = size;
    }

}
