package com.athena.asm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.athena.asm.data.Profile;

public class StringUtility {

	public static final String LOGINED = "logined";
	public static final String LOGINED_ID = "loginedID";
	
	public static final String LOGOUT = "logout";

	public static final String URL = "url";

	public static final String BOARD = "board";
	public static final String BID = "bid";
	public static final String SUBJECT_ID = "subjectID";
	public static final String AUTHOR = "author";
	public static final String TITLE = "title";
	public static final String SUBJECT = "subject";
	public static final String POST = "post";
	public static final String BOARD_TYPE = "boardType";

	/**
	 * 从链接中提取相关参数
	 */
	public static Map<String, String> getUrlParams(String url) {
		Map<String, String> paramMap = new HashMap<String, String>();
		if (!url.equals("")) {
			url = url.substring(url.indexOf('?') + 1);
			String paramaters[] = url.split("&");
			for (String param : paramaters) {
				String values[] = param.split("=");
				paramMap.put(values[0], values[1]);
			}
		}
		return paramMap;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.equals("");
	}

	public static Date toDate(String dateStr) {
		try {
			return new Date(Long.valueOf(dateStr) * 1000);
		} catch (Exception e) {
			return new Date();
		}
	}

	private static String subStringBetween(String line, String str1, String str2) {
		int idx1 = line.indexOf(str1);
		int idx2 = line.indexOf(str2);
		return line.substring(idx1 + str1.length(), idx2);
	}

	private static String getSubString(int start, int end, String content) {
		return content.substring(start, end).trim();
	}

	private static int getIntFromSubString(int start, int end, String content) {
		return Integer.parseInt(content.substring(start, end).trim());
	}

	public static Profile parseProfile(String content) {
		Profile profile = new Profile();
		int begin = content.indexOf("(") - 1;
		profile.setUserID(getSubString(0, begin, content));

		int end = content.indexOf(")");
		profile.setNickName(getSubString(begin + 2, end, content));

		begin = content.indexOf("共上站 ") + 4;
		end = content.indexOf(" 次，发表过");
		profile.setLoginTime(getIntFromSubString(begin, end, content));

		begin = end + 6;
		end = content.indexOf(" 篇文章");
		profile.setPostNumber(getIntFromSubString(begin, end, content));

		begin = content.indexOf("生命力: [") + 6;
		end = content.indexOf("] 身份");
		profile.setAliveness(getIntFromSubString(begin, end, content));

		if (content.contains("目前在站上")) {
			profile.setOnlineStatus(2);
		} else if (content.contains("因在线上或非常断线不详")) {
			profile.setOnlineStatus(1);
		} else {
			profile.setOnlineStatus(0);
		}

		return profile;
	}

	public static Object[] parsePostContent(String content) {
		Date date = new Date();
		if (content == null) {
			return new Object[] { "", date };
		}
		content = content.replace("\\n", "\n").replace("\\r", "\r")
				.replace("\\/", "/").replace("\\\"", "\"").replace("\\'", "'");
		String[] lines = content.split("\n");
		StringBuilder sb = new StringBuilder();
		int linebreak = 0;
		int linequote = 0;
		int seperator = 0;
		for (String line : lines) {
			if (line.startsWith("发信人:")) {
				/*
				 * line = "<font color=#6699FF>" +
				 * MyUtils.subStringBetween(line, "发信人: ", ", 信区:") + "</font>";
				 * sb.append(line);
				 */
				continue;
			} else if (line.startsWith("标  题:")) {
				continue;
			} else if (line.startsWith("发信站:")) {
				line = subStringBetween(line, "(", ")");
				SimpleDateFormat sdf = new SimpleDateFormat(
						"EEE MMM d HH:mm:ss yyyy", Locale.US);
				try {
					date = sdf.parse(line);
					continue;
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
			}
			if (line.equals("--")) {
				if (seperator > 0) {
					break;
				}

				seperator++;
			} else {
				if (seperator > 0) {
					if (line.length() > 0) {
						line = "<font color=#33CC66>" + line + "</font>";
					}
					else {
						continue;
					}
				}
			}

			if (line.startsWith(":")) {
				linequote++;
				if (linequote > 5) {
					continue;
				} else {
					line = "<font color=#006699>" + line + "</font>";
				}
			} else {
				linequote = 0;
			}

			if (line.equals("")) {
				linebreak++;
				if (linebreak > 1) {
					continue;
				}
			} else {
				linebreak = 0;
			}

			if (line.contains("※ 来源:·水木社区")) {
				break;
			}
			sb.append(line).append("<br />");
		}

		String result = sb.toString().trim();
		return new Object[] { result, date };
	}
}
