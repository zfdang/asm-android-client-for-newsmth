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
    private static final String USER_AGENT = "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; QQDownload 1.7; .NET CLR 1.1.4322; CIBA; .NET CLR 2.0.50727)";
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