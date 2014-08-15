package com.athena.asm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.athena.asm.aSMApplication;
import com.athena.asm.data.Profile;

public class StringUtility {

    public static final String LOGINED = "logined";
    public static final String GUEST_LOGINED = "guest_logined";
    public static final String LOGINED_ID = "loginedID";

    public static final String LOGOUT = "logout";

    public static final String URL = "url";

    public static final String USERID = "userid";
    public static final String BOARD = "board";
    public static final String BID = "bid";
    public static final String SUBJECT_ID = "subjectID";
    public static final String AUTHOR = "author";
    public static final String TITLE = "title";
    public static final String SUBJECT = "subject";
    public static final String POST = "post";
    public static final String BOARD_TYPE = "boardType";
    public static final String SUBJECT_LIST = "subjectList";
    public static final String PROFILE = "profile";
    public static final String MAIL_BOX_TYPE = "boxType";
    public static final String MAIL = "mail";
    public static final String WRITE_TYPE = "write_type";
    public static final String IS_REPLY = "is_reply";
    public static final String REFRESH_BOARD = "refreshBoard";
    public static final String SELECTED_FILE = "selectedFile";
    public static final String IMAGE_URL = "imageUrl";
    public static final String IMAGE_NAME = "imageName";
    public static final String IMAGE_INDEX = "imageIndex";
    
    public static final String STATUS_OK = "statusOK";
    
    public static final String TAB_GUIDANCE = "001";
    public static final String TAB_FAVORITE = "002";
    public static final String TAB_CATEGORY = "003";
    public static final String TAB_MAIL = "004";
    public static final String TAB_PROFILE = "005";
    
    public static final String IS_NEW_MESSAGE = "isNewMessage";

    private static SimpleDateFormat dateformat = null;

    public static String getFormattedString(Date date){
        if(dateformat == null)
            dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date != null)
            return dateformat.format(date);
        return "";
    }

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
                if (values.length > 1) {
                    paramMap.put(values[0], values[1]);
                }
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

    //elito (CMCC) 共上站 1072 次，发表过 396 篇文章
    //上次在  [Fri Mar 29 07:35:05 2013] 从 [117.136.0.*] 到本站一游。
    //离线时间[Fri Mar 29 07:35:36 2013] 信箱: [信] 生命力: [180] 身份: [用户]。
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

        if (content.contains("积分")) {
            begin = content.indexOf("积分: [") + 5;
            String subScoreString = content.substring(begin);
            end = subScoreString.indexOf("]") + begin;
            profile.setScore(getIntFromSubString(begin, end, content));
        }

        if (content.contains("目前在站上")) {
            profile.setOnlineStatus(2);
        } else if (content.contains("因在线上或非常断线不详")) {
            profile.setOnlineStatus(1);
        } else {
            profile.setOnlineStatus(0);
        }

        // extract IP address
        Pattern myipPattern = Pattern.compile("\\[(\\d{1,3}.\\d{1,3}.\\d{1,3}.[\\d\\*]+)\\]");
        Matcher myipMatcher = myipPattern.matcher(content);
        if (myipMatcher.find()) {
            String ip = myipMatcher.group(1);
            if (aSMApplication.getCurrentApplication().isShowIp()) {
                String _ip = ip.replace('*', '1');
                ip = ip + "(" + aSMApplication.db.getLocation(_ip) + ")";
            }
            profile.setIp(ip);
        }

        return profile;
    }
    
    // parse content from m.newsmth.net
    // URL like: http://m.newsmth.net/article/Children/930419181?p=1
    public static Object[] parseMobilePostContent(String content) {
        if (content == null) {
            return new Object[] { "", null };
        }

        if (aSMApplication.getCurrentApplication().isWeiboStyle()) {
            content = content.replaceAll("(\\<br\\/\\>)+【 在 (\\S+?) .*?的大作中提到: 】<br\\/>:(.{1,20}).*?FROM",
                    "//<font color=\"#6A5ACD\">@$2<\\/font>: <font color=\"#708090\">$3<\\/font> <br \\/>FROM");
            content = content.replaceAll("--\\<br \\/\\>FROM", "<br \\/>FROM");
            content = content.replaceAll("FROM: (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\*)\\]", "<br \\/>");
        }

        if (aSMApplication.getCurrentApplication().isShowIp()) {
            Pattern myipPattern = Pattern.compile("FROM[: ]*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
            Matcher myipMatcher = myipPattern.matcher(content);
            while (myipMatcher.find()) {
                String ipl = myipMatcher.group(1);
                if (ipl.length() > 5) {
                    ipl = "<font color=\"#c0c0c0\">@ $1\\*("
                            + aSMApplication.db.getLocation(ipl + "1") + ")<\\/font>";
                } else {
                    ipl = "<font color=\"#c0c0c0\">@ $1\\*<\\/font>";
                }
                content = myipMatcher.replaceAll(ipl);
            }
        }

        content = content.replace("<br />", "<br/>");
        String[] lines = content.split("<br/>");
        StringBuilder sb = new StringBuilder();
        int linebreak = 0;
        int linequote = 0;
        int seperator = 0;
        boolean isMainbodyEnd = false;
        ArrayList<String> attachList = new ArrayList<String>();
        for (String line : lines) {
            Pattern urlPattern = Pattern.compile("<a target=\"_blank\" href=\"([^<>]+)\"><img");
            Matcher urlMatcher = urlPattern.matcher(line);
            if (urlMatcher.find()) {
                attachList.add(line);
                continue;
            }
            if (isMainbodyEnd) {
                Pattern aPattern = Pattern.compile("<a href=\"([^<>]+)\">([^<>]+)</a>");
                Matcher aMatcher = aPattern.matcher(line);
                if (aMatcher.find()) {
                    attachList.add(line);
                }
                continue;
            }
            if (line.equals("--")) {
                if (seperator > 1) {
                    break;
                }

                seperator++;
            } else {
                if (seperator > 0) {
                    if (line.length() > 0) {
                        line = "<font color=#33CC66>" + line + "</font>";
                    } else {
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
            
            if (seperator > 0 && !line.contains("修改") && line.contains("FROM ")) {
                isMainbodyEnd = true;
            }

            if (line.equals("")) {
                linebreak++;
                if (linebreak > 1) {
                    continue;
                }
            } else {
                linebreak = 0;
            }

            // ※ 修改:·mozilla 于 Mar 18 13:42:45 2013 修改本文·[FROM: 220.249.41.*]
            // ※ 来源:·水木社区 newsmth.net·[FROM: 220.249.41.*]
            // 修改:mozilla FROM 220.249.41.*
            // FROM 220.249.41.*
            if(line.startsWith("※ 修改:") || line.startsWith("※ 来源:")){
                // we don't extract these lines from mobile content, duplicated information
                continue;
            }
            sb.append(line).append("<br />");
        }

        String result = sb.toString().trim();
        // remove last <br />
        result = result.replaceAll("<br />$", "");
        return new Object[] {result, attachList};
    }

    // parse content from www2
    // URL like: http://www.newsmth.net/bbscon.php?bid=647&id=930420184
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
            if (line.startsWith("发信人:") || line.startsWith("寄信人:")) {
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
                if (seperator > 1) {
                    break;
                }

                seperator++;
            } else {
                if (seperator > 0) {
                    if (line.length() > 0) {
                        line = "<font color=#33CC66>" + line + "</font>";
                    } else {
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

            // [36m※ 修改:・mozilla 于 Mar 18 13:42:45 2013 修改本文・[FROM: 220.249.41.*]\r[m\n\r
            // [m\r[1;31m※ 来源:・水木社区 newsmth.net・[FROM: 220.249.41.*]\r[m\n
            // ※ 来源:·水木社区 newsmth.net·[FROM: 119.6.200.*]
            if (line.contains("※ 来源:·") || line.contains("※ 修改:·")) {
                // remove ASCII control first
                Pattern cPattern = Pattern.compile("※[^\\]]*\\]");
                Matcher cMatcher = cPattern.matcher(line);
                if(cMatcher.find()){
                    line = cMatcher.group(0);
                }

                if (aSMApplication.getCurrentApplication().isShowIp()) {
                    Pattern myipPattern = Pattern.compile("FROM[: ]*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
                    Matcher myipMatcher = myipPattern.matcher(line);
                    while (myipMatcher.find()) {
                        String ipl = myipMatcher.group(1);
                        if (ipl.length() > 5) {
                            ipl = "<font color=\"#c0c0c0\">FROM $1\\*("
                                    + aSMApplication.db.getLocation(ipl + "1") + ")<\\/font>";
                        } else {
                            ipl = "<font color=\"#c0c0c0\">FROM $1\\*<\\/font>";
                        }
                        line = myipMatcher.replaceAll(ipl);
                    }
                }
            }
            sb.append(line).append("<br />");
        }

        String result = sb.toString().trim();
        return new Object[] { result, date };
    }

    public static int parseInt(String text) {
        if (text.trim().length() > 0) {
            return Integer.parseInt(text.trim());
        } else {
            return 0;
        }
    }

    public static int filterUnNumber(String str) {
        String regExpression = "[^0-9]";
        Pattern pattern = Pattern.compile(regExpression);
        Matcher matcher = pattern.matcher(str);
        String temp = matcher.replaceAll("").trim();
        if (temp.length() > 0) {
            return parseInt(temp);
        } else {
            return 0;
        }
    }

    public static int getOccur(String src, String find) {
        int number = 0;
        int index = -1;
        while ((index = src.indexOf(find, index)) > -1) {
            ++index;
            ++number;
        }
        return number;
    }
}