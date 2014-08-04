package com.athena.asm.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.text.Html;

import com.athena.asm.aSMApplication;
import com.athena.asm.data.Attachment;
import com.athena.asm.data.Board;
import com.athena.asm.data.Mail;
import com.athena.asm.data.MailBox;
import com.athena.asm.data.Post;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.fragment.SubjectListFragment;
import com.athena.asm.viewmodel.MailViewModel;

public class SmthSupport {
	public String userid;
	private String passwd;
	private boolean loginned;
	SmthCrawler crawler;

	private static class Holder {
		private static SmthSupport instance = new SmthSupport();
	}

	public static SmthSupport getInstance() {
		return Holder.instance;
	}

	private SmthSupport() {
		crawler = SmthCrawler.getIntance();
	}

	public void restore() {
		if (crawler.isDestroy()) {
			crawler.init();
		}
	}

	public boolean getLoginStatus() {
		return loginned;
	}

	/**
	 * 登录.
	 */
	public int login() {
		loginned = true;
		return crawler.login(userid, passwd);
	}

	/**
	 * 退出登录.
	 */
	public void logout() {
		if (loginned) {
			crawler.getUrlContent("http://www.newsmth.net/bbslogout.php");
			loginned = false;
		}
	}

	/**
	 * 退出登录，并关闭httpclient
	 */
	public void destory() {
		// skip the step to logout normally, to accelerate logout
		// logout();
		crawler.destroy();
	}

	public boolean uploadAttachFile(File file) {
		return crawler.uploadAttachFile(file);
	}

	public boolean sendMail(String mailUrl, String mailTitle, String userid, String num, String dir, String file,
			String signature, String mailContent) {
		return crawler.sendMail(mailUrl, mailTitle, userid, num, dir, file, signature, mailContent);
	}

	public boolean sendPost(String postUrl, String postTitle, String postContent, String signature, boolean isEdit) {
		return crawler.sendPost(postUrl, postTitle, postContent, signature, isEdit);
	}

	public String getUrlContent(String urlString) {
		return crawler.getUrlContent(urlString);
	}

	/**
	 * 获得首页导读.
	 * 
	 * @return
	 */
	public Object[] getGuidance() {
		String content = crawler.getUrlContent("http://www.newsmth.net/mainpage.html");
		Pattern hp = Pattern.compile("<table [^<>]+class=\"HotTable\"[^<>]+>(.*?)</table>", Pattern.DOTALL);
		if (content == null) {
			return new Object[] { Collections.emptyList(), Collections.emptyList() };
		}
		Matcher hm = hp.matcher(content);
		List<String> sectionList = new ArrayList<String>();
		List<List<Subject>> subjectList = new ArrayList<List<Subject>>();
		if (hm.find()) {
			sectionList.add("水木十大");
			String hc = hm.group(1);
			Pattern boardNamePattern = Pattern.compile("<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
			Matcher boardNameMatcher = boardNamePattern.matcher(hc);

			Pattern hip = Pattern.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
			Matcher him = hip.matcher(hc);
			Pattern hIdPattern = Pattern.compile("<a href=\"bbsqry.php\\?userid=(\\w+)\">");
			Matcher hIdMatcher = hIdPattern.matcher(hc);
			List<Subject> list = new ArrayList<Subject>();
			while (him.find() && hIdMatcher.find()) {
				Subject subject = new Subject();
				if (boardNameMatcher.find()) {
					subject.setBoardChsName(boardNameMatcher.group(1));
				}
				subject.setBoardEngName(him.group(1));
				subject.setSubjectID(him.group(2));
				subject.setTitle(him.group(3));
				subject.setAuthor(hIdMatcher.group(1));
				list.add(subject);
			}
			subjectList.add(list);
		}

		Pattern sp = Pattern.compile(
				"<span class=\"SectionName\"><a[^<>]+>([^<>]+)</a></span>(.*?)class=\"SecLine\"></td>", Pattern.DOTALL);
		Matcher sm = sp.matcher(content);
		while (sm.find()) {
			String sectionName = sm.group(1);
			sectionList.add(sectionName);
			String sc = sm.group(2);
			// System.out.println(sectionName);

			Pattern boardNamePattern = Pattern
					.compile("\"SectionItem\">.<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
			Matcher boardNameMatcher = boardNamePattern.matcher(sc);

			Pattern sip = Pattern.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
			Matcher sim = sip.matcher(sc);
			List<Subject> list = new ArrayList<Subject>();
			while (sim.find()) {
				Subject subject = new Subject();
				if (boardNameMatcher.find()) {
					subject.setBoardChsName(boardNameMatcher.group(1));
				}
				subject.setBoardEngName(sim.group(1));
				subject.setSubjectID(sim.group(2));
				subject.setTitle(sim.group(3));
				list.add(subject);
			}
			subjectList.add(list);
		}

		return new Object[] { sectionList, subjectList };
	}

	/**
	 * 获取版面收藏列表. type用来区别版面自己是不是目录
	 * 
	 * @return
	 */
	public void getFavorite(String id, List<Board> boardList, int type) {
		String url;
		if (type == 0) {
			url = "http://www.newsmth.net/bbsfav.php?select=" + id;
		} else {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + id;
		}

		String content = crawler.getUrlContent(url);
		if (content == null) {
			return;
		}

		// 先提取目录
		String patternStr = "o\\.f\\((\\d+),'([^']+)',\\d+,''\\);";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(content);
		List<String> list = new ArrayList<String>();
		while (matcher.find()) {
			list.add(matcher.group(1));
			Board board = new Board();
			board.setDirectory(true);
			board.setDirectoryName(matcher.group(2));
			board.setCategoryName("目录");
			boardList.add(board);
		}

		for (int i = 0; i < list.size(); i++) {
			getFavorite(list.get(i), boardList.get(i).getChildBoards(), 0);
		}

		// o.o(false,1,998,22156,'[站务]','Ask','新用户疑难解答','haning BJH',733,997,0);
		patternStr = "o\\.o\\(\\w+,\\d+,(\\d+),\\d+,'([^']+)','([^']+)','([^']+)','([^']*)',\\d+,\\d+,\\d+\\)";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			String boardID = matcher.group(1);
			String category = matcher.group(2);
			String engName = matcher.group(3);
			String chsName = matcher.group(4);
			String moderator = matcher.group(5);
			if (moderator.length() > 25) {
				moderator = moderator.substring(0, 21) + "...";
			}
			Board board = new Board();
			board.setDirectory(false);
			board.setBoardID(boardID);
			board.setCategoryName(category);
			board.setEngName(engName);
			board.setChsName(chsName);
			board.setModerator(moderator);
			if (moderator.contains("[目录]")) {
				board.setDirectory(true);
				board.setDirectoryName(chsName);
				getFavorite(engName, board.getChildBoards(), 1);
			}
			boardList.add(board);
		}

	}

    /**
     * 根据boardName来获取boardID
     */
    public String getBoardIDFromName(String boardName) {
        String url;
            url = "http://www.newsmth.net/bbsdoc.php?board=" + boardName;

        String content = crawler.getUrlContent(url);
        if (content == null) {
            return "";
        }

        // extrat board ID
        Pattern pattern = Pattern.compile("new docWriter\\('(\\w+)',(\\d+),");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            String name = matcher.group(1);
            String id = matcher.group(2);
//            Log.d("getBoardIDFromName", String.format("name=%s ==>id=%s", name, id));
            if(boardName.equals(name)){
                return id;
            }
        }
        return "";
    }

	public String checkNewReplyOrAt() {
		String result = null;
		String content = crawler.getUrlContentFromMobile("http://m.newsmth.net/");
		if (content != null) {
			if (content.contains(">@我(")) {
				result = "新@";
			} else if (content.contains(">回我(")) {
				result = "新回复";
			}
		}

		return result;
	}

	public boolean checkNewMail() {
		String content = crawler.getUrlContentFromMobile("http://m.newsmth.net/");
		if (content == null) {
			return false;
		}
		if (content.contains("邮箱(新)")) {
			return true;
		} else {
			return false;
		}
	}

	public void markAllMessageRead(int type) {
		String url = "";
		if (type == 0) {
			url = "http://m.newsmth.net/refer/at/read?index=all";
		} else {
			url = "http://m.newsmth.net/refer/reply/read?index=all";
		}
		crawler.getUrlContentFromMobile(url);
	}

	/**
	 * 获取邮箱信息
	 * 
	 * @return
	 */
	public MailBox getMailBoxInfo() {
		MailBox mailBox = new MailBox();
		String content = crawler.getUrlContent("http://www.newsmth.net/bbsmail.php");
		if (content == null) {
			return null;
		}
		if (content.contains("您有未读邮件")) {
			mailBox.setHavingNewMail(true);
		} else {
			mailBox.setHavingNewMail(false);
		}

		Pattern inboxPattern = Pattern
				.compile("<td><a href=\"bbsmailbox.php\\?path=\\.DIR&title=%CA%D5%BC%FE%CF%E4\" class=\"ts2\">[^<>]+</a></td>\\s<td>(\\d+)</td>");
		Matcher inboxMatcher = inboxPattern.matcher(content);
		while (inboxMatcher.find()) {
			mailBox.setInboxNumber(Integer.parseInt(inboxMatcher.group(1)));
		}
		Pattern outboxPattern = Pattern
				.compile("<td><a href=\"bbsmailbox.php\\?path=\\.SENT&title=%B7%A2%BC%FE%CF%E4\" class=\"ts2\">[^<>]+</a></td>\\s<td>(\\d+)</td>");
		Matcher outboxMatcher = outboxPattern.matcher(content);
		while (outboxMatcher.find()) {
			mailBox.setOutboxNumber(Integer.parseInt(outboxMatcher.group(1)));
		}
		Pattern trashboxPattern = Pattern
				.compile("<td><a href=\"bbsmailbox.php\\?path=\\.DELETED&title=%C0%AC%BB%F8%CF%E4\" class=\"ts2\">[^<>]+</a></td>\\s<td>(\\d+)</td>");
		Matcher trashboxMatcher = trashboxPattern.matcher(content);
		while (trashboxMatcher.find()) {
			mailBox.setTrashboxNumber(Integer.parseInt(trashboxMatcher.group(1)));
		}

		content = crawler.getUrlContentFromMobile("http://m.newsmth.net/refer/at");
		if (content != null) {
			if (content.contains(">@我(")) {
				mailBox.setHavingNewAt(true);
			} else if (content.contains(">回我(")) {
				mailBox.setHavingNewReply(true);
			}
		}

		return mailBox;
	}

	/**
	 * 取得邮件标题列表
	 * 
	 * @param boxType
	 * @param startNumber
	 * @return
	 */
	public List<Mail> getMailList(int boxType, int startNumber) {
		String urlString = "http://www.newsmth.net/bbsmailbox.php?";
		String boxString = "";
		String boxDirString = "";
		String startString = "";
		if (startNumber != -1) {
			startString += "&start=" + startNumber;
		}
		switch (boxType) {
		case 0:
			urlString += "path=.DIR" + startString + "&title=%CA%D5%BC%FE%CF%E4";
			boxString = "%CA%D5%BC%FE%CF%E4";
			boxDirString = ".DIR";
			break;
		case 1:
			urlString += "path=.SENT" + startString + "&title=%B7%A2%BC%FE%CF%E4";
			boxString = "%B7%A2%BC%FE%CF%E4";
			boxDirString = ".SENT";
			break;
		case 2:
			urlString += "path=.DELETED" + startString + "&title=%C0%AC%BB%F8%CF%E4";
			boxString = "%C0%AC%BB%F8%CF%E4";
			boxDirString = ".DELETED";
			break;
		default:
			break;
		}

		List<Mail> mailList = new ArrayList<Mail>();
		String result = crawler.getUrlContent(urlString);
		if (result == null) {
			return Collections.emptyList();
		}
		int counter = 0;
		String matchString = "";

		String numberAndDatePatternString = "<td class=\"mt3\">([^<>]+)</td>";
		Pattern numberAndDatePattern = Pattern.compile(numberAndDatePatternString);
		Matcher numberAndDateMatcher = numberAndDatePattern.matcher(result);
		while (numberAndDateMatcher.find()) {
			matchString = numberAndDateMatcher.group(1);
			int number = Integer.parseInt(matchString) - 1;
			numberAndDateMatcher.find();
			matchString = numberAndDateMatcher.group(1);

			Mail mail = new Mail();
			mail.setBoxString(boxString);
			mail.setBoxType(boxType);
			mail.setBoxDirString(boxDirString);
			mail.setNumber(number);
			mail.setDateString(matchString.replaceAll("&nbsp;", " ").trim());
			mailList.add(mail);
		}

		String isUnreadPatternString = "<img src='images/(\\w+).gif'[^<>]+>";
		Pattern isUnreadPattern = Pattern.compile(isUnreadPatternString);
		Matcher isUnreadMatcher = isUnreadPattern.matcher(result);
		while (isUnreadMatcher.find()) {
			matchString = isUnreadMatcher.group(1);
			Mail mail = mailList.get(counter);
			if (matchString.contains("omail")) {
				mail.setUnread(false);
			} else {
				mail.setUnread(true);
			}
			counter++;
		}

		counter = 0;
		String valuePatternString = "<input type=\"checkbox\"[^<>]+value=\"([^<>]+)\">";
		Pattern valuePattern = Pattern.compile(valuePatternString);
		Matcher valueMatcher = valuePattern.matcher(result);
		while (valueMatcher.find()) {
			matchString = valueMatcher.group(1);
			Mail mail = mailList.get(counter);
			mail.setValueString(matchString);
			counter++;
		}

		counter = 0;
		String statusPatternString = "<td class=\"mt4\"><nobr>([^<>]+)</nobr></td>";
		Pattern statusPattern = Pattern.compile(statusPatternString);
		Matcher statusMatcher = statusPattern.matcher(result);
		while (statusMatcher.find()) {
			matchString = statusMatcher.group(1);
			Mail mail = mailList.get(counter);
			mail.setStatus(matchString.replaceAll("&nbsp;", "").trim());
			counter++;
		}

		counter = 0;
		String senderPatternString = "<td class=\"mt3\"><a href=\"bbsqry.php\\?userid=([^<>]+)\">[^<>]+</a></td>";
		Pattern senderPattern = Pattern.compile(senderPatternString);
		Matcher senderMatcher = senderPattern.matcher(result);
		while (senderMatcher.find()) {
			matchString = senderMatcher.group(1);
			Mail mail = mailList.get(counter);
			mail.setSenderID(matchString);
			counter++;
		}

		counter = 0;
		String titlePatternString = "<td class=\"mt5\">&nbsp;<a[^<>]+>([^<>]+)</a></td>";
		Pattern titlePattern = Pattern.compile(titlePatternString);
		Matcher titleMatcher = titlePattern.matcher(result);
		while (titleMatcher.find()) {
			matchString = titleMatcher.group(1);
			Mail mail = mailList.get(counter);
			mail.setTitle(matchString.trim());
			counter++;
		}

		counter = 0;
		String sizePatternString = "<td class=\"mt3\" style=[^<>]+>(\\d+)</td>";
		Pattern sizePattern = Pattern.compile(sizePatternString);
		Matcher sizeMatcher = sizePattern.matcher(result);
		while (sizeMatcher.find()) {
			matchString = sizeMatcher.group(1);
			Mail mail = mailList.get(counter);
			mail.setSizeString(matchString);
			counter++;
		}

		return mailList;
	}

	public List<Mail> getReplyOrAtList(MailViewModel mailViewModel, int boxType, int startNumber) {
		String urlString = "";
		switch (boxType) {
		case 4:
			urlString = "http://m.newsmth.net/refer/at";
			break;
		case 5:
			urlString = "http://m.newsmth.net/refer/reply";
			break;
		default:
			break;
		}

		if (startNumber != -1) {
			urlString += "?p=" + startNumber;
		}

		List<Mail> mailList = new ArrayList<Mail>();
		String result = crawler.getUrlContentFromMobile(urlString);
		if (result == null) {
			return Collections.emptyList();
		}

		// <div><a href="/refer/reply/read?index=
		Pattern itemPattern;
		if (boxType == 4) {
			itemPattern = Pattern.compile("<div><a href=\"/refer/at/read\\?index=(\\d+)\"([^<>]*)>([^<>]+)");
		} else {
			itemPattern = Pattern.compile("<div><a href=\"/refer/reply/read\\?index=(\\d+)\"([^<>]*)>([^<>]+)");
		}
		Matcher itemMatcher = itemPattern.matcher(result);
		while (itemMatcher.find()) {
			Mail mail = new Mail();
			mail.setBoxType(boxType);
			int number = Integer.parseInt(itemMatcher.group(1));
			mail.setNumber(number);
			if (itemMatcher.groupCount() == 2) {
				mail.setUnread(false);
				mail.setTitle(itemMatcher.group(2));
			} else {
				String type = itemMatcher.group(2);
				if (type.contains("top")) {
					mail.setUnread(true);
				} else {
					mail.setUnread(false);
				}

				mail.setTitle(itemMatcher.group(3));
			}

			mailList.add(mail);
		}

		// 2012-05-28<a href="/user/query/
		Pattern userIDPattern = Pattern.compile("([^<>]+)<a href=\"/user/query/([^<>]+)\"");
		Matcher userIDMatcher = userIDPattern.matcher(result);
		int index = 0;
		while (userIDMatcher.find()) {
			String dateString = userIDMatcher.group(1).trim();
			mailList.get(index).setDateString(dateString.replace("&nbsp;", ""));
			mailList.get(index).setSenderID(userIDMatcher.group(2));
			index++;
		}

		// / <a class="plant">1/1272</a>
		Pattern pagePattern = Pattern.compile("<a class=\"plant\">(\\d+)/(\\d+)");
		Matcher pageMatcher = pagePattern.matcher(result);
		if (pageMatcher.find()) {
			mailViewModel.setCurrentPageNo(Integer.parseInt(pageMatcher.group(1)));
			mailViewModel.setTotalPageNo(Integer.parseInt(pageMatcher.group(2)));
		}

		return mailList;
	}

	public void getMailContent(Mail mail) {
		String boxTypeString = "";
		switch (mail.getBoxType()) {
		case 0:
			boxTypeString = ".DIR";
			break;
		case 1:
			boxTypeString = ".SENT";
			break;
		case 2:
			boxTypeString = ".DELETED";
			break;
		default:
			break;
		}
		String url = "http://www.newsmth.net/bbsmailcon.php?dir=" + boxTypeString + "&num=" + mail.getNumber()
				+ "&title=" + mail.getBoxString();
		String result = crawler.getUrlContent(url);
		if (result == null) {
			mail.setContent("加载失败");
			mail.setDate(new Date());
		} else {
			Pattern contentPattern = Pattern.compile("prints\\('(.*?)'\\);", Pattern.DOTALL);
			Matcher contentMatcher = contentPattern.matcher(result);
			if (contentMatcher.find()) {
				String contentString = contentMatcher.group(1);
				Object[] objects = StringUtility.parsePostContent(contentString);
				mail.setContent((String) objects[0]);
				mail.setDate((java.util.Date) objects[1]);
			}
		}
	}

	/**
	 * 获取分类讨论区列表
	 * 
	 * @return
	 */
	public void getCategory(String id, List<Board> boardList, boolean isFolder) {
		// http://www.newsmth.net/bbsfav.php?x
		// there are three kinds of items in category: folder, group, board

		// 1. folder
		// o.f(1,'系统　　　 水木社区系统版面 ',0,'NewSMTH.net');
		// http://www.newsmth.net/bbsfav.php?select=1&x

		// 2. group
		// o.o(true,1,502,356446,'[站务]','BBSData','社区系统数据','[目录]',10,501,0);
		// http://www.newsmth.net/bbsboa.php?group=0&group2=502

		// 3. board
		// o.o(false,1,104,27745,'[出国]','AdvancedEdu','飞跃重洋','LSAT madonion
		// Mperson',22553,103,25);
		// http://www.newsmth.net/bbsdoc.php?board=AdvancedEdu

		String url;
		if (id.equals("TOP")) {
			url = "http://www.newsmth.net/bbsfav.php?x";
		} else if (isFolder) {
			// 1. folder
			url = "http://www.newsmth.net/bbsfav.php?select=" + id + "&x";
		} else {
			// 2. group
			url = "http://www.newsmth.net/bbsboa.php?group=0&group2=" + id;
		}

		String content = crawler.getUrlContent(url);
		if (content == null) {
			return;
		}

		// 先提取folder
		String patternStr = "o\\.f\\((\\d+),'([^']+)',\\d+,'([^']+)'\\);";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			// Log.d("Find folder", matcher.group(2));
			getCategory(matcher.group(1), boardList, true);
		}

		// 再寻找board和group
		patternStr = "o\\.o\\((\\w+),\\d+,(\\d+),\\d+,'([^']+)','([^']+)','([^']+)','([^']*)',\\d+,\\d+,\\d+\\)";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			String isGroup = matcher.group(1);
			String boardID = matcher.group(2);
			String category = matcher.group(3);
			String engName = matcher.group(4);
			String chsName = matcher.group(5);
			String moderator = matcher.group(6);
			if (moderator.length() > 25) {
				moderator = moderator.substring(0, 21) + "...";
			}

			if (isGroup.equals("true")) {
				// find group, add its child boards recursively
				// Log.d("find Group", engName);
				getCategory(boardID, boardList, false);
			} else {
				// Log.d("find Board", engName);
				Board board = new Board();
				board.setBoardID(boardID);
				board.setCategoryName(category);
				board.setEngName(engName);
				board.setChsName(chsName);
				board.setModerator(moderator);
				boardList.add(board);
			}
		}
	}

	/**
	 * 获取经过过滤的主题列表.
	 * 
	 * @return
	 */
	public List<Subject> getSearchSubjectList(String boardName, String boardID, String queryString) {
		String url = "http://www.newsmth.net/bbsbfind.php?q=1&" + queryString;
		String result = crawler.getUrlContent(url);
		if (result == null) {
			return Collections.emptyList();
		}
		String patternStr = "ta\\.r\\('[^']+','([^']+)','<a href=\"bbsqry.php\\?userid=(\\w+)\">\\w+</a>','([^']+)','<a href=\"bbscon.php\\?bid=(\\d+)&id=(\\d+)\">([^<>]+)</a>'\\);";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(result);
		List<Subject> subjectList = new ArrayList<Subject>();
		while (matcher.find()) {
			String type = matcher.group(1).trim();
			String author = matcher.group(2);
			String dateStr = matcher.group(3).replace("&nbsp;", " ");
			// SimpleDateFormat formatter = new SimpleDateFormat("MMM dd",
			// Locale.US);
			// Date date;
			// try {
			// date = formatter.parse(dateStr);
			// } catch (ParseException e) {
			// date = new Date();
			// }
			String boardid = matcher.group(4);
			String subjectid = matcher.group(5);
			String title = matcher.group(6);

			Subject subject = new Subject();
			subject.setAuthor(author);
			subject.setBoardID(boardID);
			subject.setBoardEngName(boardName);
			subject.setBoardID(boardid);
			subject.setSubjectID(subjectid);
			subject.setTitle(title);
			subject.setType(type);
			subject.setDateString(dateStr);
			subjectList.add(subject);
		}

		return subjectList;
	}

	/**
	 * 获取版面主题列表.
	 * 
	 * @return
	 */
	public List<Subject> getSubjectListFromMobile(Board board, int boardType, boolean isReloadPageNo,
			ArrayList<String> blackList) {
		String boardname = board.getEngName();
		int pageno = board.getCurrentPageNo();
		if (isReloadPageNo) {
			pageno = 0;
		}
		String result = getMainSubjectListFromMobile(boardname, pageno, boardType);
		if (result == null) {
			return Collections.emptyList();
		}

		List<Subject> subjectList = new ArrayList<Subject>();

		// <a class="plant">1/1272</a> 当前页/总共页
		Pattern pagePattern = Pattern.compile("<a class=\"plant\">(\\d+)/(\\d+)");
		Matcher pageMatcher = pagePattern.matcher(result);
		if (pageMatcher.find()) {
			board.setCurrentPageNo(Integer.parseInt(pageMatcher.group(1)));
			board.setTotalPageNo(Integer.parseInt(pageMatcher.group(2)));
		}

		// 同主题模式, 主题后面有(NNN), 两个作者
		// <div><a href="/article/DC/423562"
		// class="m">如了个3D相机，FUJI-REAL3D-W3</a>(1)</div><div>2013-02-06&nbsp;<a
		// href="/user/query/penwall">penwall</a>|2013-02-06&nbsp;<a
		// href="/user/query/DRAGON9">DRAGON9</a></div>

		// 其他模式
		// <div><a
		// href="/article/DC/single/2515/1">●&nbsp;如了个3D相机，FUJI-REAL3D-W3</a></div><div>2515&nbsp;2013-02-06&nbsp;<a
		// href="/user/query/penwall">penwall</a></div>

		// 置顶的帖子， class="top"
		// <div><a href="/article/DC/419129"
		// class="top">审核通过DC版治版方针</a>(0)</div><div>2012-12-22&nbsp;<a
		// href="/user/query/SYSOP">SYSOP</a>|2012-12-22&nbsp;<a
		// href="/user/query/SYSOP">SYSOP</a></div>

		// 2013-02-06&nbsp;<a href="/user/query/penwall">penwall</a>
		Pattern userIDPattern = Pattern.compile("([^<>]+)<a href=\"/user/query/([^<>]+)\"");
		Matcher userIDMatcher = userIDPattern.matcher(result);
		int index = 1;
		while (userIDMatcher.find()) {
			// subject mode has two author info per subject
			index = 1 - index;
			if (boardType == SubjectListFragment.BOARD_TYPE_SUBJECT && index == 1)
				continue;

			Subject subject = new Subject();
			String dateString = userIDMatcher.group(1).trim();
			String[] dates = dateString.split("&nbsp;");
			if (dates.length < 2) {
				subject.setDateString(dates[0]);
			} else {
				subject.setDateString(dates[1]);
			}
			// subject.setDateString(dateString.replace("&nbsp;", ""));
			subject.setAuthor(userIDMatcher.group(2));
			subject.setBoardID(board.getBoardID());
			subject.setBoardEngName(boardname);
			subject.setCurrentPageNo(1);
			subject.setType(" ");
			subjectList.add(subject);
		}

		// <div><a href="/article/DC/423562"
		// class="m">如了个3D相机，FUJI-REAL3D-W3</a>(1)</div>
		// <div><a
		// href="/article/DC/single/2515/1">●&nbsp;如了个3D相机，FUJI-REAL3D-W3</a></div>
		String subPattern1 = "";
		String subPattern2 = "";
		if (boardType != SubjectListFragment.BOARD_TYPE_SUBJECT) {
			boardname = boardname + "/single";
		}
		if (boardType == SubjectListFragment.BOARD_TYPE_NORMAL) {
			subPattern1 = "/0";
		} else if (boardType == SubjectListFragment.BOARD_TYPE_DIGEST) {
			subPattern1 = "/1";
		} else if (boardType == SubjectListFragment.BOARD_TYPE_MARK) {
			subPattern1 = "/3";
		}
		if (boardType == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			subPattern2 = "\\((\\d+)\\)";
		}

		Pattern subjectPattern = Pattern.compile("<div><a href=\"/article/" + boardname + "/(\\d+)" + subPattern1
				+ "\"([^<>]*)>([^<>]+)</a>" + subPattern2);
		// Log.d("getSubjectListFromMobile RE", subjectPattern.pattern());
		Matcher subjectMatcher = subjectPattern.matcher(result);
		index = 0;
		while (subjectMatcher.find()) {
			// Log.d("getSubjectListFromMobile result",
			// subjectMatcher.group(0));
			if (subjectMatcher.groupCount() == 2) {
				subjectList.get(index).setSubjectID(subjectMatcher.group(1));
				subjectList.get(index).setTitle(subjectMatcher.group(2));
			} else {
				String type = subjectMatcher.group(2);
				if (type.contains("top")) {
					// 置顶的帖子
					subjectList.get(index).setType(Subject.TYPE_BOTTOM);
				}
				subjectList.get(index).setSubjectID(subjectMatcher.group(1));
				// add replied number after subject title in SUBJECT mode
				String subjectTitle = "null";
				if (boardType == SubjectListFragment.BOARD_TYPE_SUBJECT)
					subjectTitle = subjectMatcher.group(3) + " (" + subjectMatcher.group(4) + ")";
				else
					subjectTitle = subjectMatcher.group(3);
				subjectList.get(index).setTitle(subjectTitle);
			}
			index++;
			if (index > subjectList.size()) {
				break;
			}
		}

		if (aSMApplication.getCurrentApplication().isHidePinSubject()) {
			for (Iterator<Subject> iterator = subjectList.iterator(); iterator.hasNext();) {
				Subject subject = (Subject) iterator.next();
				if (subject.getType().equals(Subject.TYPE_BOTTOM)) {
					iterator.remove();
				}
			}
		}

		return subjectList;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public List<Post> getSinglePostListFromMobileUrl(Subject subject, String url) {
		String result = crawler.getUrlContentFromMobile(url);
		if (result == null || result.contains("指定的文章不存在或链接错误") || result.contains("您无权阅读此版面")) {
			return null;
		}

		List<Post> postList = new ArrayList<Post>();
		Post post = new Post();

		// <a href="/article/NewSoftware/single/68557">楼主
		Pattern infoPattern = Pattern.compile("<a href=\"/article/([^<>]+)/single/(\\d+)\">楼主");
		Matcher infoMatcher = infoPattern.matcher(result);
		if (infoMatcher.find()) {
			subject.setBoardEngName(infoMatcher.group(1));
			subject.setTopicSubjectID(infoMatcher.group(2));
			subject.setCurrentPageNo(1);
			subject.setType(" ");
		}
		// subject.setBoardID(board.getBoardID());

		// <a userid href="/user/query/
		Pattern userIDPattern = Pattern.compile("<a href=\"/user/query/([^<>]+)\"");
		Matcher userIDMatcher = userIDPattern.matcher(result);

		if (userIDMatcher.find()) {
			post.setTopicSubjectID(subject.getTopicSubjectID());
			post.setBoardID(subject.getBoardID());
			post.setBoard(subject.getBoardEngName());
			String author = userIDMatcher.group(1);
			post.setAuthor(author);
			subject.setAuthor(author);
		}

		// <a href="/article/NewExpress/post/11111">回复
		Pattern subjectPattern = Pattern.compile("<a href=\"/article/" + subject.getBoardEngName()
				+ "/post/(\\d+)\\?s=1\"");
		Matcher subjectMatcher = subjectPattern.matcher(result);
		if (subjectMatcher.find()) {
			post.setSubjectID(subjectMatcher.group(1));
			subject.setSubjectID(post.getSubjectID());
		}

		// <a class="plant">2012-02-23 00:16:41</a>
		Pattern datePattern = Pattern.compile("<a class=\"plant\">(\\d)([^<>]+)</a>");
		Matcher dateMatcher = datePattern.matcher(result);
		if (dateMatcher.find()) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				post.setDate((java.util.Date) sdf.parse(dateMatcher.group(1) + dateMatcher.group(2)));
				subject.setDateString(dateMatcher.group(1) + dateMatcher.group(2));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		// <li class="f">title</li>
		Pattern titlePattern = Pattern.compile("<li class=\"f\">([^<>]+)</li>");
		Matcher titleMatcher = titlePattern.matcher(result);
		String titleString = "";
		if (titleMatcher.find()) {
			titleString = Html.fromHtml(titleMatcher.group(1)).toString();
			post.setTitle(titleString);
			subject.setTitle(titleString);
		}

		// post content
		Pattern contentPattern = Pattern.compile("<div class=\"sp\">(.*?)</div>");
		Matcher contentMatcher = contentPattern.matcher(result);
		if (contentMatcher.find()) {
			String contentString = contentMatcher.group(1);
			Object[] objects = StringUtility.parseMobilePostContent(contentString);
			post.setContent((String) objects[0]);
			ArrayList<Attachment> attachFiles = new ArrayList<Attachment>();
			@SuppressWarnings("unchecked")
			ArrayList<String> attachList = (ArrayList<String>) objects[1];
			for (Iterator<String> iterator = attachList.iterator(); iterator.hasNext();) {
				String attach = (String) iterator.next();
				Attachment innerAtt = new Attachment();

				if (attach.contains("<img")) {
				    // this attachment is an image
					Pattern urlPattern = Pattern.compile("<a target=\"_blank\" href=\"([^<>]+)\"");
					// there are two kinds of URL here, we should add http if necessary
					// <a target="_blank" href="/att/NewExpress/3749711/258">
					// <a target="_blank" href="http://att.newsmth.net/nForum/att/Picture/568422/225">
					Matcher urlMatcher = urlPattern.matcher(attach);
					if (urlMatcher.find()) {
						String urlString = urlMatcher.group(1);
						// append prefix if necessary
						if (!urlString.startsWith("http://")){
							urlString = "http://att.newsmth.net/nForum" + urlString;
						}
						innerAtt.setMobileUrlString(urlString);
						// find image content type from HTTP stream
						String filename = crawler.fetchAttachmentFilename(urlString);
						innerAtt.setName(filename);
					}
				} else {
				    // other attachment, shown as downloadable link
					Pattern urlPattern = Pattern.compile("<a href=\"([^<>]+)\">([^<>]+)</a>");
					Matcher urlMatcher = urlPattern.matcher(attach);
					if (urlMatcher.find()) {
						innerAtt.setMobileUrlString(urlMatcher.group(1));
						innerAtt.setName(urlMatcher.group(2));
					}
				}

				innerAtt.setMobileType(true);

				attachFiles.add(innerAtt);
			}
			post.setAttachFiles(attachFiles);
		}

		postList.add(post);

		return postList;
	}

	/**
	 * 获取单贴内容
	 * 
	 * @param subject
	 * @return
	 */
	public List<Post> getSinglePostList(Subject subject) {
		// String result = getPostContent(subject.getBoardID(),
		// subject.getSubjectID());
		List<Post> postList = new ArrayList<Post>();
		Post post = new Post();
		post.setAuthor(subject.getAuthor());
		post.setSubjectID(subject.getSubjectID());
		post.setBoardID(subject.getBoardID());
		post.setBoard(subject.getBoardEngName());
		postList.add(post);
		crawler.getPostList(postList);

		subject.setTopicSubjectID(post.getTopicSubjectID());

		return postList;
	}

	public List<Post> getTopicPostList(Subject subject, int action) {
	    List<Post> postList = new ArrayList<Post>();
		String url = "http://www.newsmth.net/bbscon.php?bid=" + subject.getBoardID() + "&id=";
		if (action == Post.ACTION_FIRST_POST_IN_SUBJECT) {
		    // http://www.newsmth.net/bbscon.php?bid=15&id=2956478
			url += subject.getTopicSubjectID();
		} else {
			url += subject.getSubjectID();
			if (action == Post.ACTION_PREVIOUS_POST_IN_SUBJECT) {
			    // http://www.newsmth.net/bbscon.php?bid=15&id=2956806&p=tp
				url += "&p=tp";
			} else {
			    // http://www.newsmth.net/bbscon.php?bid=15&id=2956806&p=tn
			    // Post.ACTION_NEXT_POST_IN_SUBJECT
				url += "&p=tn";
			}
		}
		String content = crawler.getUrlContent(url);
		if (content == null) {
			return null;
		}

		Post post = new Post();

		post.setBoardID(subject.getBoardID());
		post.setBoard(subject.getBoardEngName());

		Pattern contentPattern = Pattern.compile("prints\\('(.*?)'\\);", Pattern.DOTALL);
		Pattern infoPattern = Pattern
				.compile("conWriter\\(\\d+, '[^']+', \\d+, (\\d+), (\\d+), (\\d+), '[^']+', (\\d+), \\d+,'([^']+)'\\);");
		Matcher contentMatcher = contentPattern.matcher(content);
		if (contentMatcher.find()) {
			String contentString = contentMatcher.group(1);
			Object[] objects = StringUtility.parsePostContent(contentString);
			post.setContent((String) objects[0]);
			post.setDate((java.util.Date) objects[1]);
			int index1 = contentString.indexOf("发信人:");
			int index2 = contentString.indexOf("(");
			String authorString = contentString.substring(index1 + 4, index2 - 1).trim();
			post.setAuthor(authorString);
		}

		Matcher infoMatcher = infoPattern.matcher(content);
		if (infoMatcher.find()) {
			post.setSubjectID(infoMatcher.group(1));
			post.setTopicSubjectID(infoMatcher.group(2));
			post.setTitle(infoMatcher.group(5));
		}

		String bid = null, id = null, ftype = null, num = null, cacheable = null;
		Matcher attachPartOneMatcher = Pattern.compile("attWriter\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+)").matcher(
				content);
		if (attachPartOneMatcher.find()) {
			bid = attachPartOneMatcher.group(1);
			id = attachPartOneMatcher.group(2);
			ftype = attachPartOneMatcher.group(3);
			num = attachPartOneMatcher.group(4);
			cacheable = attachPartOneMatcher.group(5);
		}

		ArrayList<Attachment> attachFiles = new ArrayList<Attachment>();
		Matcher attachPartTwoMatcher = Pattern.compile("attach\\('([^']+)', (\\d+), (\\d+)\\)").matcher(content);
		while (attachPartTwoMatcher.find()) {
			Attachment innerAtt = new Attachment();
			innerAtt.setBid(bid);
			innerAtt.setId(id);
			innerAtt.setFtype(ftype);
			innerAtt.setNum(num);
			innerAtt.setCacheable(cacheable);
			String name = attachPartTwoMatcher.group(1);
			String len = attachPartTwoMatcher.group(2);
			String pos = attachPartTwoMatcher.group(3);
			innerAtt.setName(name);
			innerAtt.setLen(len);
			innerAtt.setPos(pos);
			attachFiles.add(innerAtt);
		}
		post.setAttachFiles(attachFiles);
		postList.add(post);

		subject.setSubjectID(post.getSubjectID());
		subject.setTopicSubjectID(post.getTopicSubjectID());
		subject.setAuthor(post.getAuthor());
		subject.setDateString(StringUtility.getFormattedString(post.getDate()));

		return postList;
	}

	/**
	 * 获取同主题帖子列表.
	 * 
	 * @param board
	 * @param mainSubjectid
	 * @param pageno
	 * @return
	 */
	public List<Post> getPostList(Subject subject, ArrayList<String> blackList, int startNumber) {
		// String result = getPostListContent(subject.getBoardEngName(),
		// subject.getSubjectID(), subject.getCurrentPageNo(), startNumber);
		String url = "http://www.newsmth.net/bbstcon.php?board=" + subject.getBoardEngName() + "&gid="
				+ subject.getSubjectID();
		if (subject.getCurrentPageNo() > 0) {
			url += "&pno=" + subject.getCurrentPageNo();
		}
		if (startNumber > 0) { // 对应web的"从此处展开"
			url += "&start=" + startNumber;
		}
		String result = crawler.getUrlContent(url);
		if (result == null) {
			return Collections.emptyList();
		}
		Matcher bidMatcher = Pattern.compile("tconWriter\\('[^']+',(\\d+),\\d+,\\d+,(\\d+),(\\d+),\\d+,\\d+,\\d+")
				.matcher(result);
		String boardid = "";
		if (bidMatcher.find()) {
			boardid = bidMatcher.group(1);
			int totalPage = Integer.parseInt(bidMatcher.group(2));
			subject.setTotalPageNo(totalPage);
			// Log.d("asm : totalpageno", totalPage + "");
			int page = Integer.parseInt(bidMatcher.group(3));
			subject.setCurrentPageNo(page);
		}

		String patternStr = "\\[(\\d+),'([^']+)'\\]";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(result);
		List<Post> postList = new ArrayList<Post>();

		boolean flag = true;
		while (matcher.find()) {
			String subjectid = matcher.group(1);
			String author = matcher.group(2);
			if (blackList.contains(author)) {
				continue;
			}

			if (flag) {
				flag = false;
				if (!StringUtility.isEmpty(author) && StringUtility.isEmpty(subject.getAuthor())) {
					subject.setAuthor(author);
				}
			}

			Post post = new Post();
			post.setAuthor(author);
			post.setSubjectID(subjectid);
			post.setBoardID(boardid);
			post.setBoard(subject.getBoardEngName());
			postList.add(post);
		}

		crawler.getPostList(postList);
		return postList;
	}

	/**
	 * 获取移动版水木帖子列表.
	 * 
	 * @param board
	 * @param mainSubjectid
	 * @param pageno
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Post> getPostListFromMobile(Subject subject, ArrayList<String> blackList, int boardType) {
		String url = "";
		int currentPageNo = subject.getCurrentPageNo();
		boolean isInSubject = false;
		if (boardType == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			// http://m.newsmth.net/article/Children/930419181?p=1
			url = "http://m.newsmth.net/article/" + subject.getBoardEngName() + "/" + subject.getSubjectID();

			if (currentPageNo > 0) {
				url += "?p=" + currentPageNo;
			}
			isInSubject = true;
		} else if (boardType == SubjectListFragment.BOARD_TYPE_DIGEST) {
			url = "http://m.newsmth.net/article/" + subject.getBoardEngName() + "/single/" + subject.getSubjectID()
					+ "/1";
		} else if (boardType == SubjectListFragment.BOARD_TYPE_MARK) {
			url = "http://m.newsmth.net/article/" + subject.getBoardEngName() + "/single/" + subject.getSubjectID()
					+ "/3";
		}

		String result = crawler.getUrlContentFromMobile(url);
		if (result == null || result.contains("指定的文章不存在或链接错误") || result.contains("您无权阅读此版面")) {
			return null;
		}

		List<Post> postList = new ArrayList<Post>();

		boolean flag = true;
		// <a class="plant">10楼</a>|<a href="/user/query/xiaojunxiao">xiaojunxiao</a>
		// <a class="plant">楼主</a>|<a href="/user/query/vivianv">vivianv</a>
		Pattern userIDPattern = Pattern.compile("<a class=\"plant\">((\\d+)楼|楼主)</a>\\|<a href=\"/user/query/([^<>]+)\"");
		Matcher userIDMatcher = userIDPattern.matcher(result);

		while (userIDMatcher.find()) {
			Post post = new Post();
			post.setTopicSubjectID(subject.getTopicSubjectID());
			post.setBoardID(subject.getBoardID());
			post.setBoard(subject.getBoardEngName());
			String post_index = userIDMatcher.group(1);
			post.setPostIndex(post_index);
			String post_author = userIDMatcher.group(3);
			post.setAuthor(post_author);
			post.setTopicSubjectID(subject.getSubjectID());

			if (flag) {
				flag = false;
				if (!StringUtility.isEmpty(post_author) && StringUtility.isEmpty(subject.getAuthor())) {
					subject.setAuthor(post_author);
				}
			}

			postList.add(post);
		}

		// <a href="/article/NewExpress/post/11111">回复
		// only logined user has this option. guest user will have subjectID = null
		Pattern subjectPattern = Pattern.compile("<a href=\"/article/" + subject.getBoardEngName() + "/post/(\\d+)\"");
		Matcher subjectMatcher = subjectPattern.matcher(result);
		int index = 0;
		while (subjectMatcher.find()) {
			postList.get(index).setSubjectID(subjectMatcher.group(1));

			++index;
		}

		// <a class="plant">2012-02-23 00:16:41</a>
		Pattern datePattern = Pattern.compile("<a class=\"plant\">(\\d)([^<>]+)</a>");
		Matcher dateMatcher = datePattern.matcher(result);
		index = 0;
		boolean isOdd = !isInSubject;
		flag = true;
		while (dateMatcher.find()) {
			if (isOdd) {
				if (currentPageNo > 1) {
					dateMatcher.group(1);
					currentPageNo = 0;
					continue;
				}

				isOdd = false;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					postList.get(index)
							.setDate((java.util.Date) sdf.parse(dateMatcher.group(1) + dateMatcher.group(2)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				if (!isInSubject) {
					break;
				}
				++index;
			} else {
				if (flag) {
					flag = false;
					String pageString = dateMatcher.group(1) + dateMatcher.group(2);
					int splitIndex = pageString.indexOf("/");
					int totalPage = Integer.parseInt(pageString.substring(splitIndex + 1));
					subject.setTotalPageNo(totalPage);
					int page = Integer.parseInt(pageString.substring(0, splitIndex));
					subject.setCurrentPageNo(page);
				}

				isOdd = true;
				continue;
			}
		}

		// <li class="f">title</li>
		index = 0;
		Pattern titlePattern = Pattern.compile("<li class=\"f\">([^<>]+)</li>");
		Matcher titleMatcher = titlePattern.matcher(result);
		String titleString = "";
		if (titleMatcher.find()) {
			titleString = Html.fromHtml(titleMatcher.group(1)).toString();
			postList.get(index).setTitle(titleString);
		}
		titleString = "Re: " + titleString;

		for (int i = 1; i < postList.size(); i++) {
			postList.get(i).setTitle(titleString);
		}

		// post content
		index = 0;
		Pattern contentPattern = Pattern.compile("<div class=\"sp\">(.*?)</div>");
		Matcher contentMatcher = contentPattern.matcher(result);
		while (contentMatcher.find()) {
			String contentString = contentMatcher.group(1);


			Object[] objects = StringUtility.parseMobilePostContent(contentString);
			postList.get(index).setContent((String) objects[0]);
			ArrayList<Attachment> attachFiles = new ArrayList<Attachment>();
			ArrayList<String> attachList = (ArrayList<String>) objects[1];
			for (Iterator<String> iterator = attachList.iterator(); iterator.hasNext();) {
				String attach = (String) iterator.next();
				Attachment innerAtt = new Attachment();

				if (attach.contains("<img")) {
				    // this attachment is an image
					Pattern urlPattern = Pattern.compile("<a target=\"_blank\" href=\"([^<>]+)\"");
					Matcher urlMatcher = urlPattern.matcher(attach);
					if (urlMatcher.find()) {
						String urlString = urlMatcher.group(1);
						// append prefix if necessary
						if (!urlString.startsWith("http://")){
							urlString = "http://att.newsmth.net/nForum" + urlString;
						}
						innerAtt.setMobileUrlString(urlString);
                        // find image conten·t type from HTTP stream
                        String filename = crawler.fetchAttachmentFilename(urlString);
						innerAtt.setName(filename);
					}
				} else {
				    // other attachment, shown as downloadable link
					Pattern urlPattern = Pattern.compile("<a href=\"([^<>]+)\">([^<>]+)</a>");
					Matcher urlMatcher = urlPattern.matcher(attach);
					if (urlMatcher.find()) {
						innerAtt.setMobileUrlString(urlMatcher.group(1));
						innerAtt.setName(urlMatcher.group(2));
					}
				}

				innerAtt.setMobileType(true);

				attachFiles.add(innerAtt);
			}
			postList.get(index).setAttachFiles(attachFiles);
			++index;
		}

		return postList;
	}

	private Boolean forwardGroupPostTo(Post post, String to) {
		String url = "http://www.newsmth.net/bbstfwd.php?do";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("board", post.getBoard()));
		params.add(new BasicNameValuePair("gid", post.getSubjectID()));
		params.add(new BasicNameValuePair("start", post.getSubjectID()));
		params.add(new BasicNameValuePair("noansi", "1"));
		params.add(new BasicNameValuePair("target", to));
		String content = crawler.getPostRequestResult(url, params);
		if (content != null && content.contains("操作成功")) {
			return true;
		} else {
			return false;
		}
	}

	private Boolean forwardPostTo(Post post, String to) {
		String url = "http://www.newsmth.net/bbsfwd.php?do";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("board", post.getBoard()));
		params.add(new BasicNameValuePair("id", post.getSubjectID()));
		params.add(new BasicNameValuePair("noansi", "1"));
		params.add(new BasicNameValuePair("target", to));
		String content = crawler.getPostRequestResult(url, params);
		if (content != null && content.contains("操作成功")) {
			return true;
		} else {
			return false;
		}
	}

	public Boolean forwardGroupPostToExternalMail(Post post, String emailAddress) {
		return forwardGroupPostTo(post, emailAddress);
	}

	public Boolean forwardGroupPostToMailBox(Post post) {
		return forwardPostTo(post, userid);
	}

	public Boolean forwardPostToExternalMail(Post post, String emailAddress) {
		return forwardPostTo(post, emailAddress);
	}

	public Boolean forwardPostToMailBox(Post post) {
		return forwardPostTo(post, userid);
	}

	public Boolean deletePost(String boardname, String postid) {
		// http://www.newsmth.net/bbsdel.php?board=PocketLife&id=1418719
		// "操作成功: 删除成功.
		String url = "http://www.newsmth.net/bbsdel.php?board=" + boardname + "&id=" + postid;
		String content = crawler.getUrlContent(url);
		if (content == null) {
			return false;
		}
		if (content.contains("删除成功")) {
			return true;
		} else {
			return false;
		}
	}

    public Boolean addBoardToFavorite(String groupid, String boardname) {
        // http://www.newsmth.net/bbsfav.php?bname=<boardname>&select=<groupid>
        // return new favorite list: if the boardname is returned, operation success
        String url = "http://www.newsmth.net/bbsfav.php?select=" + groupid + "&bname=" + boardname;
        String content = crawler.getUrlContent(url);
        if (content == null) {
            return false;
        }
        if (content.contains(boardname)) {
            return true;
        } else {
            return false;
        }
    }

    public Boolean removeBoardFromFavorite(String groupid, String boardname, String boardid) {
        // http://www.newsmth.net/bbsfav.php?select=<groupid>&delete=<boardid-1>
        // return new favorite list: if the boardname is not returned, operation success
        if(boardid == null && boardname != null) {
            boardid = getBoardIDFromName(boardname);
        }
        // o.o(false,1,1029,1441174,'[数码]','PocketLife','掌上智能','leavesnow earthmouse wedf',372363,1028,644);
        // it's strange the value of delete parameter is not boardid, but boardid-1
        int bid = Integer.parseInt(boardid) - 1;
        String url = "http://www.newsmth.net/bbsfav.php?select=" + groupid + "&delete=" + bid;
        String content = crawler.getUrlContent(url);
        if (content == null) {
            return false;
        }
        if (! content.contains(boardname)) {
            return true;
        } else {
            return false;
        }
    }

    /**
	 * 获得个人信息
	 * 
	 * @param userID
	 *            要查询的用户ID
	 * @return
	 */
	public Profile getProfile(String userID) {
		String url = "http://www.newsmth.net/bbsqry.php?userid=" + userID;
		String content = crawler.getUrlContent(url);
		if (content == null) {
			return null;
		}
		Pattern profilePattern = Pattern.compile("<pre>(.*?)</pre>", Pattern.DOTALL);

		Profile profile = new Profile();

		Matcher profileMatcher = profilePattern.matcher(content);
		if (profileMatcher.find()) {
			String detailString = profileMatcher.group(1);
			profile = StringUtility.parseProfile(detailString);
		}

		Pattern desPattern = Pattern.compile("prints\\('(.*?)'\\);", Pattern.DOTALL);
		Matcher desMatcher = desPattern.matcher(content);
		if (desMatcher.find()) {
			String descriptionString = desMatcher.group(1);
			descriptionString = descriptionString.replaceAll("\\\\/", "/"); // \/ ==> /
			descriptionString = descriptionString.replaceAll("\\\\\\\\", "\\\\"); // \\ ==> \
			descriptionString = descriptionString.replaceAll("\\\\\"", "\""); // \" ==> "
			descriptionString = descriptionString.replaceAll("\\\\'", "'"); // \' ==> '
			descriptionString = descriptionString.replaceAll("\\\\n", "<br/>"); // \n ==> <br/>
			profile.setDescription(descriptionString);
		} else {
			profile.setDescription("这家伙很懒，啥也没留下");
		}

		return profile;
	}

	public String getPostContent(String boardid, String subjectid) {
		String url = "http://www.newsmth.net/bbscon.php?bid=" + boardid + "&id=" + subjectid;
		return crawler.getUrlContent(url);
	}

	// private String getPostListContent(String board, String subjectid, int
	// pageno, int startNumber) {
	// String url = "http://www.newsmth.net/bbstcon.php?board=" + board
	// + "&gid=" + subjectid;
	// if (pageno > 0) {
	// url += "&pno=" + pageno;
	// }
	// if (startNumber > 0) {
	// url += "&start=" + startNumber;
	// }
	// return crawler.getUrlContent(url);
	// }

	public String getMainSubjectList(String board, int pageno, int type) {
		String url = "";
		if (type == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=6";
		} else if (type == SubjectListFragment.BOARD_TYPE_NORMAL) {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=0";
		} else if (type == SubjectListFragment.BOARD_TYPE_DIGEST) {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=1";
		} else {// mark
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=3";
		}
		if (pageno > 0) {
			url = url + "&page=" + pageno;
		}
		return crawler.getUrlContent(url);
	}

	public String getMainSubjectListFromMobile(String board, int pageno, int type) {
		String url = "";
		if (type == SubjectListFragment.BOARD_TYPE_SUBJECT) {
			url = "http://m.newsmth.net/board/" + board;
		} else if (type == SubjectListFragment.BOARD_TYPE_NORMAL) {
			url = "http://m.newsmth.net/board/" + board + "/0";
		} else if (type == SubjectListFragment.BOARD_TYPE_DIGEST) {
			url = "http://m.newsmth.net/board/" + board + "/1";
		} else {// mark
			url = "http://m.newsmth.net/board/" + board + "/3";
		}
		if (pageno > 0) {
			url = url + "?p=" + pageno;
		}
		return crawler.getUrlContentFromMobile(url);
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

}