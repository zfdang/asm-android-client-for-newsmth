package com.koushikdutta.urlimageviewhelper;

import android.content.Context;
import android.os.AsyncTask;

import com.athena.asm.util.SmthCrawler;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper.RequestPropertiesCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpUrlDownloader implements UrlDownloader {
    private RequestPropertiesCallback mRequestPropertiesCallback;
    private long maxSizeThreshold = 0;
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 1.7; .NET CLR 1.1.4322; CIBA; .NET CLR 2.0.50727)";
    private DefaultHttpClient mHttpClient;

    public HttpUrlDownloader(){
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 10);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
                schemeRegistry);

        mHttpClient = new DefaultHttpClient(cm, params);
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

                    HttpGet httpGet = new HttpGet(url);
                    httpGet.setHeader("User-Agent", USER_AGENT);

                    mHttpClient.setCookieStore(SmthCrawler.smthCookie);

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
//    public void download(final Context context, final String url, final String filename, final UrlDownloaderCallback callback, final Runnable completion) {
//        final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(final Void... params) {
//                try {
//                    UrlImageViewHelper.clog("Downloading URL " + url);
//                    InputStream is = null;
//
//                    String thisUrl = url;
//                    HttpURLConnection urlConnection;
//                    while (true) {
//                        final URL u = new URL(thisUrl);
//                        urlConnection = (HttpURLConnection)u.openConnection();
//                        urlConnection.setInstanceFollowRedirects(true);
//
//                        if (mRequestPropertiesCallback != null) {
//                            final ArrayList<NameValuePair> props = mRequestPropertiesCallback.getHeadersForRequest(context, url);
//                            if (props != null) {
//                                for (final NameValuePair pair: props) {
//                                    urlConnection.addRequestProperty(pair.getName(), pair.getValue());
//                                }
//                            }
//                        }
//
//                        /// yhl start
//                        // 加入cookie
//                        Log.v(this.toString(), "======================================SmthCrawler.smthCookieString=" + SmthCrawler.smthCookieString);
//                        urlConnection.setRequestProperty("Cookie", SmthCrawler.smthCookie.getCookies().toString());
//
//                        //// yhl end
//
//                        /////// yhl start
//                        // 打印请求header
//                        Log.v(this.toString(), "======================================HttpUrlDownloader, print request header:");
//                        for(String header: urlConnection.getRequestProperties().keySet()){
//                            if(header != null){
//                                for(String value:urlConnection.getRequestProperties().get(header)){
//                                    Log.v(this.toString(), "======" + header + ": " + value);
//                                }
//                            }
//                        }
//                        ////// yhl end
//
//                        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM)
//                            break;
//                        thisUrl = urlConnection.getHeaderField("Location");
//                    }
//
//
//                    if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                        UrlImageViewHelper.clog("Response Code: " + urlConnection.getResponseCode());
//                        return null;
//                    }
//
//                    //// yhl start
//                    // print response headers
//                    UrlImageViewHelper.clog("=============================HttpUrlDownloader, print response headers:");
//                    for(String header: urlConnection.getHeaderFields().keySet()){
//                        if(header != null){
//                            for(String value: urlConnection.getHeaderFields().get(header)){
//                                Log.v(this.toString(), "=========" + header + ": " + value);
//                            }
//                        }
//                    }
//
////                    for (Map.Entry<String, List<String>> k : urlConnection.getHeaderFields().entrySet()) {
////                        for (String v : k.getValue()){
////                            UrlImageViewHelper.clog(k.getKey() + ":" + v);
////                        }
////                    }
//                    //// yhl end
//
//                    // check response size
//                    int contentSize = urlConnection.getContentLength();
//                    if( maxSizeThreshold != 0 &&  contentSize > maxSizeThreshold ){
//                        UrlImageViewHelper.clog(String.format("Download abort, size %d > %d, %s", contentSize, maxSizeThreshold, url));
//                        return null;
//                    } else {
//                        UrlImageViewHelper.clog(String.format("Image size %d < threshold %d, %s", contentSize, maxSizeThreshold, url));
//                    }
//                    is = urlConnection.getInputStream();
//                    callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
//                    return null;
//                }
//                catch (final Throwable e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            @Override
//            protected void onPostExecute(final Void result) {
//                UrlImageViewHelper.clog("Finish download URL " + url);
//                completion.run();
//            }
//        };
//
//        UrlImageViewHelper.executeTask(downloader);
//    }

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
