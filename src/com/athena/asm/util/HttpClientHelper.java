package com.athena.asm.util;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpClientHelper {
    private static HttpClientHelper mClientHelper;
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.143 Safari/537.36";
    private static final String DEFAULT_CHARSET = HTTP.UTF_8;
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private DefaultHttpClient mHttpClient;

    private HttpClientHelper(){

    }

    public static HttpClientHelper getInstance(){
        if(mClientHelper == null){
            synchronized (HttpClientHelper.class) {
                if(mClientHelper == null){
                    mClientHelper = new HttpClientHelper();
                }
            }
        }
        return mClientHelper;
    }

    public synchronized DefaultHttpClient getHttpClient(){
        if(mHttpClient == null){
            synchronized (HttpClientHelper.class) {
                if(mHttpClient == null){
                    SchemeRegistry sr = new SchemeRegistry();
                    Scheme http = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
                    sr.register(http);

                    HttpParams params = new BasicHttpParams();
                    HttpProtocolParams.setUserAgent(params, USER_AGENT);
                    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                    HttpProtocolParams.setContentCharset(params, DEFAULT_CHARSET);

                    ConnManagerParams.setMaxTotalConnections(params, 10);
                    ConnManagerParams.setTimeout(params, 1000);

                    HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT);
                    HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT);

                    // 支持多线程
                    ClientConnectionManager cm = new ThreadSafeClientConnManager(params, sr);
                    mHttpClient = new DefaultHttpClient(cm, params);
                }
            }
        }
        return mHttpClient;
    }
}