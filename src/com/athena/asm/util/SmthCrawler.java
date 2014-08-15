package com.athena.asm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.athena.asm.data.Attachment;
import com.athena.asm.data.Post;

public class SmthCrawler {
	public static String smthEncoding = "GBK";
	public static String mobileSMTHEncoding = "UTF-8";
	public static String userAgent = HttpClientHelper.USER_AGENT;
	public static CookieStore smthCookie;

	private int threadNum;
	private ExecutorService execService;

	private boolean destroy;

	private static class Holder {
		static SmthCrawler instance = new SmthCrawler();
	}

	private DefaultHttpClient httpClient;

	public static SmthCrawler getIntance() {
		return Holder.instance;
	}

	private SmthCrawler() {
		init();
	}

    public static CookieStore getCookieStore(){
        return smthCookie;
    }

	public void init() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 10);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params,
				schemeRegistry);
		httpClient = new DefaultHttpClient(cm, params);
		// 重试
		// httpClient.setHttpRequestRetryHandler(new
		// DefaultHttpRequestRetryHandler(3, false));
		// 超时设置
		// httpClient.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
		// 10000);
		// httpClient.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT,
		// 10000);
		httpClient.getParams()
				.setParameter(ClientPNames.HANDLE_REDIRECTS, true);

		threadNum = 10;
		execService = Executors.newFixedThreadPool(threadNum);
		destroy = false;
	}

	// return values:
	// 1: success; 0: authentication failed; -1: connection failed
	public int login(String userid, String passwd) {
		String url = "http://www.newsmth.net/bbslogin.php";
		HttpPost httpPost = new HttpPost(url);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("id", userid));
		formparams.add(new BasicNameValuePair("passwd", passwd));
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "GBK");
		} catch (UnsupportedEncodingException e1) {
			return -1;
		}
		httpPost.setEntity(entity);
		httpPost.setHeader("User-Agent", userAgent);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity e = response.getEntity();
			String content = EntityUtils.toString(e, smthEncoding);
			if (content.contains("你登录的窗口过多")) {
				formparams.add(new BasicNameValuePair("kick_multi", "1"));
				UrlEncodedFormEntity entity2;
				entity2 = new UrlEncodedFormEntity(formparams, "GBK");
				httpPost = new HttpPost(
						"http://www.newsmth.net/bbslogin.php?mainurl=");
				httpPost.setHeader("User-Agent", userAgent);
				httpPost.setEntity(entity2);
				httpClient.execute(httpPost);
			} else if (content.contains("您的用户名并不存在，或者您的密码错误")) {
				return 0;
			} else if (content.contains("用户密码错误")) {
				return 0;
			}
            // 保存cookie
            smthCookie = httpClient.getCookieStore();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

	public boolean uploadAttachFile(File file) {
		HttpPost httpPost = new HttpPost(
				"http://www.newsmth.net/bbsupload.php?act=add");
		MultipartEntity entity = new MultipartEntity();

		// entity.addPart(file.getName(), new FileBody(file));
		try {
			entity.addPart("attachfile0", new FileBody(file));
			entity.addPart("counter", new StringBody("1"));
			entity.addPart("MAX_FILE_SIZE", new StringBody("5242880"));
		} catch (UnsupportedEncodingException e1) {
			return false;
		}
		httpPost.setEntity(entity);
		httpPost.setHeader("User-Agent", userAgent);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity e = response.getEntity();
			String content = EntityUtils.toString(e, smthEncoding);
			if (!content.contains("上传成功")) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean sendPost(String postUrl, String postTitle,
			String postContent, String signature, boolean isEdit) {
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("title", postTitle));
		formparams.add(new BasicNameValuePair("text", postContent));
		if (isEdit) {
			postUrl += "&do";
		}
		else {
			formparams.add(new BasicNameValuePair("signature", signature));
		}
		
		HttpPost httpPost = new HttpPost(postUrl);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "GBK");
		} catch (UnsupportedEncodingException e1) {
			return false;
		}
		httpPost.setEntity(entity);
		httpPost.setHeader("User-Agent", userAgent);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity e = response.getEntity();
			String content = EntityUtils.toString(e, smthEncoding);
			if (!content.contains("成功")) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean sendMail(String mailUrl, String mailTitle, String userid,
			String num, String dir, String file, String signature,
			String mailContent) {
		HttpPost httpPost = new HttpPost(mailUrl);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("title", mailTitle));
		formparams.add(new BasicNameValuePair("userid", userid));
		formparams.add(new BasicNameValuePair("num", num));
		formparams.add(new BasicNameValuePair("dir", dir));
		formparams.add(new BasicNameValuePair("file", file));
		formparams.add(new BasicNameValuePair("signature", signature));
		formparams.add(new BasicNameValuePair("backup", "1"));
		formparams.add(new BasicNameValuePair("text", mailContent));

		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "GBK");
		} catch (UnsupportedEncodingException e1) {
			return false;
		}
		httpPost.setEntity(entity);
		httpPost.setHeader("User-Agent", userAgent);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity e = response.getEntity();
			String content = EntityUtils.toString(e, smthEncoding);
			if (!content.contains("发送成功")) {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/*
	 * public String getRedirectUrl(String url) {
	 * httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,false);
	 * HttpGet httpget = new HttpGet(url); httpget.setHeader("User-Agent",
	 * userAgent); httpget.addHeader("Accept-Encoding", "gzip, deflate"); String
	 * newUrl; try { HttpResponse response = httpClient.execute(httpget); Header
	 * locationHeader = response.getLastHeader("Location"); newUrl =
	 * locationHeader.getValue(); } catch (IOException e) {
	 * Log.d("com.athena.asm", "get url failed,", e); newUrl = null; }
	 * httpClient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,true);
	 * return newUrl; }
	 */

	public String getPostRequestResult(String url, List<NameValuePair> params) {
		HttpPost httpPost = new HttpPost(url);
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(params, "GBK");
		} catch (UnsupportedEncodingException e1) {
			return null;
		}
		httpPost.setEntity(entity);
		httpPost.setHeader("User-Agent", userAgent);
		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity e = response.getEntity();
			String content = EntityUtils.toString(e, smthEncoding);
			return content;
		} catch (IOException e) {
			return null;
		}
	}

	public String fetchContent(String url, String encoding) {
		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("User-Agent", userAgent);
		httpget.addHeader("Accept-Encoding", "gzip, deflate");
        if(smthCookie != null){
            httpClient.setCookieStore(smthCookie);
        }
		String content;
		try {
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			Header[] headers = response.getHeaders("Content-Encoding");
			boolean isgzip = false;
			if (headers != null && headers.length != 0) {
				for (Header header : headers) {
					String s = header.getValue();
					if (s.contains("gzip")) {
						isgzip = true;
					}
				}
			}
			if (isgzip) {
				InputStream is = entity.getContent();
				BufferedReader br = new java.io.BufferedReader(
						new InputStreamReader(new GZIPInputStream(is), encoding));
				String line;
				StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append("\n");
				}
				br.close();
				content = sb.toString();
			} else {
				content = EntityUtils.toString(entity, encoding);
			}
		} catch (Exception e) {
			Log.e("Crawler:fetchContent", "get url failed,", e);
			content = null;
		}
		return content;
	}

    public String fetchAttachmentFilename(String url) {
        HttpHead httphead = new HttpHead(url);
        httphead.setHeader("User-Agent", userAgent);
        String filename = "file.unknown";
        try {
            HttpResponse response = httpClient.execute(httphead);
            Header[] headers = response.getHeaders("Content-Disposition");
            if (headers != null && headers.length != 0) {
                for (Header header : headers) {
                    String s = header.getValue();
                    if (s.contains("filename")) {
						// how to decode filename from http header:
						// http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
                        String rawValue = s.substring(s.lastIndexOf("=") + 1);
                        filename = rawValue;
                        // Log.d("fetchAttachmentFilename", url + filename);
                        return filename;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Crawler:fetchContent", "get url failed,", e);
        }
        return filename;
    }

	public String getUrlContentFromMobile(String url) {
		return fetchContent(url, mobileSMTHEncoding);
	}

	public String getUrlContent(String url) {
		return fetchContent(url, smthEncoding);
	}

	public void getPostList(List<Post> postList) {
		if (postList == null)
			return;
		Pattern contentPattern = Pattern.compile("prints\\('(.*?)'\\);",
				Pattern.DOTALL);
		Pattern infoPattern = Pattern
				.compile("conWriter\\(\\d+, '[^']+', \\d+, (\\d+), (\\d+), (\\d+), '[^']+', (\\d+), \\d+,'([^']+)'\\);");
		List<Future<?>> futureList = new ArrayList<Future<?>>(postList.size());
		for (Post post : postList) {
			Future<?> future = execService.submit(new PostContentCrawler(post,
					contentPattern, infoPattern));
			futureList.add(future);
		}
		for (Future<?> future : futureList) {
			try {
				future.get();
			} catch (InterruptedException e) {
				Log.e("com.athena.asm", "excute error", e);
			} catch (ExecutionException e) {
				Log.e("com.athena.asm", "excute error", e);
			}
		}
	}

	public void excuteMethod() {

	}

	public void destroy() {
		httpClient.getConnectionManager().shutdown();
		execService.shutdown();
		destroy = true;
	}

	public boolean isDestroy() {
		return destroy;
	}

	class PostContentCrawler implements Runnable {

		private Post post;
		private Pattern contentPattern;
		private Pattern infoPattern;

		public PostContentCrawler(Post post, Pattern contentPattern,
				Pattern infoPattern) {
			this.post = post;
			this.contentPattern = contentPattern;
			this.infoPattern = infoPattern;
		}

		@Override
		public void run() {
			String url = "http://www.newsmth.net/bbscon.php?bid="
					+ post.getBoardID() + "&id=" + post.getSubjectID();
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader("User-Agent", SmthCrawler.userAgent);
			httpget.addHeader("Accept-Encoding", "gzip, deflate");
			String content;
			try {
				HttpResponse response = httpClient.execute(httpget);
				HttpEntity entity = response.getEntity();
				Header[] headers = response.getHeaders("Content-Encoding");
				boolean isgzip = false;
				if (headers != null && headers.length != 0) {
					for (Header header : headers) {
						String s = header.getValue();
						if (s.contains("gzip")) {
							isgzip = true;
						}
					}
				}
				if (isgzip) {
					InputStream is = entity.getContent();
					BufferedReader br = new java.io.BufferedReader(
							new InputStreamReader(new GZIPInputStream(is),
									SmthCrawler.smthEncoding));
					String line;
					StringBuilder sb = new StringBuilder();
					while ((line = br.readLine()) != null) {
						sb.append(line);
						sb.append("\n");
					}
					br.close();
					content = sb.toString();
				} else {
					content = EntityUtils.toString(entity,
							SmthCrawler.smthEncoding);
				}
			} catch (IOException e) {
				Log.d("com.athena.asm", "get url failed,", e);
				return;
			}
			Matcher contentMatcher = contentPattern.matcher(content);
			if (contentMatcher.find()) {
				String contentString = contentMatcher.group(1);
				Object[] objects = StringUtility
						.parsePostContent(contentString);
				post.setContent((String) objects[0]);
				post.setDate((java.util.Date) objects[1]);
			}

			if (content == null) {
				return;
			}

			Matcher infoMatcher = infoPattern.matcher(content);
			if (infoMatcher.find()) {
				post.setSubjectID(infoMatcher.group(1));
				post.setTopicSubjectID(infoMatcher.group(2));
				post.setTitle(infoMatcher.group(5));
			}

			String bid = null, id = null, ftype = null, num = null, cacheable = null;
			Matcher attachPartOneMatcher = Pattern.compile(
					"attWriter\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").matcher(
					content);
			if (attachPartOneMatcher.find()) {
				bid = attachPartOneMatcher.group(1);
				id = attachPartOneMatcher.group(2);
				ftype = attachPartOneMatcher.group(3);
				num = attachPartOneMatcher.group(4);
				cacheable = attachPartOneMatcher.group(5);
			}

			ArrayList<Attachment> attachFiles = new ArrayList<Attachment>();
			Matcher attachPartTwoMatcher = Pattern.compile(
					"attach\\('([^']+)', (\\d+), (\\d+)\\)").matcher(content);
			while (attachPartTwoMatcher.find()) {
				Attachment innerAtt = new Attachment();
				innerAtt.setBid(bid);
				innerAtt.setId(id);
				innerAtt.setFtype(ftype);
				innerAtt.setNum(num);
				innerAtt.setCacheable(cacheable);
				innerAtt.setMobileType(false);
				String name = attachPartTwoMatcher.group(1);
				String len = attachPartTwoMatcher.group(2);
				String pos = attachPartTwoMatcher.group(3);
				innerAtt.setName(name);
				innerAtt.setLen(len);
				innerAtt.setPos(pos);
				attachFiles.add(innerAtt);
			}
			post.setAttachFiles(attachFiles);
		}

	}
}
