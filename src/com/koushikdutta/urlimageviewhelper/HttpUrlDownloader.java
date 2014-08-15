package com.koushikdutta.urlimageviewhelper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.util.HttpClientHelper;
import com.athena.asm.util.SmthCrawler;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper.RequestPropertiesCallback;

public class HttpUrlDownloader implements UrlDownloader {
    private RequestPropertiesCallback mRequestPropertiesCallback;
    private long maxSizeThreshold = 0;
    private DefaultHttpClient mHttpClient;

    public HttpUrlDownloader(){
        mHttpClient = HttpClientHelper.getInstance().getHttpClient();
        mHttpClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
    }

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
                    if(!ensureUrlValid(url)){
                        UrlImageViewHelper.clog("url " + url +" is NOT valid! ");
                        return null;
                    }

                    mHttpClient.setCookieStore(SmthCrawler.smthCookie);

                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse response = mHttpClient.execute(httpGet);
                    int statusCode = response.getStatusLine().getStatusCode();
                    if(statusCode != HttpURLConnection.HTTP_OK){
                        UrlImageViewHelper.clog("Response Code: " + statusCode);
                        return null;
                    }

                    // check response size
                    long contentSize = response.getEntity().getContentLength();
                    if( maxSizeThreshold != 0 &&  contentSize > maxSizeThreshold ){
                        UrlImageViewHelper.clog(String.format("Download abort, size %d > %d, %s", contentSize, maxSizeThreshold, url));
                        return null;
                    } else {
                        UrlImageViewHelper.clog(String.format("Image size %d < threshold %d, %s", contentSize, maxSizeThreshold, url));
                    }

                    InputStream is = response.getEntity().getContent();
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

    private boolean ensureUrlValid(String url){
        try {
            URI uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}