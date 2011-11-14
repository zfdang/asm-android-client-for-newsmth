package com.athena.asm.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.athena.asm.data.Board;
import com.athena.asm.data.Post;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;

public class SmthSupport {
	private String userid;
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
	public boolean login() {
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
		logout();
		crawler.destroy();
	}

	public boolean sendPost(String postUrl, String postTitle, String postContent) {
		return crawler.sendPost(postUrl, postTitle, postContent);
	}

	public String getUrlContent(String urlString) {
		return crawler.getUrlContent(urlString);
	}

	/**
	 * 获取版面收藏列表.
	 * 
	 * @return
	 */
	public void getFavorite(String id, List<Board> boardList) {
		String url = "http://www.newsmth.net/bbsfav.php?select=" + id;

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
			getFavorite(list.get(i), boardList.get(i).getChildBoards());
		}

		patternStr = "o\\.o\\(\\w+,\\d+,(\\d+),\\d+,'([^']+)','([^']+)','([^']+)','([^']+)',\\d+,\\d+,\\d+\\)";
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
			boardList.add(board);
		}

	}

	/**
	 * 获取分类讨论区列表
	 * 
	 * @return
	 */
	public void getCategory(String id, List<Board> boardList, boolean isTypeTwo) {
		String url;
		if (id.equals("TOP")) {
			url = "http://www.newsmth.net/bbsfav.php?x";
		} else if (!isTypeTwo) {
			url = "http://www.newsmth.net/bbsfav.php?select=" + id + "&x";
		} else {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + id;
		}

		String content = crawler.getUrlContent(url);
		if (content == null) {
			return;
		}

		// 先提取目录
		String patternStr = "o\\.f\\((\\d+),'([^']+)',\\d+,'([^']+)'\\);";
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
		
		/*patternStr = "o\\.o\\(true,\\d+,(\\d+),\\d+,'([^']+)','([^']+)','([^']+)','([^']+)',\\d+,\\d+,\\d+\\)";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		while (matcher.find()) {
			//list.add(matcher.group(1));
			Board board = new Board();
			board.setDirectory(true);
			board.setDirectoryName(matcher.group(2));
			board.setCategoryName("目录");
			//boardList.add(board);
		}*/

		for (int i = 0; i < list.size(); i++) {
			getCategory(list.get(i), boardList.get(i).getChildBoards(), false);
		}

		patternStr = "o\\.o\\((\\w+),\\d+,(\\d+),\\d+,'([^']+)','([^']+)','([^']+)','([^']+)',\\d+,\\d+,\\d+\\)";
		pattern = Pattern.compile(patternStr);
		matcher = pattern.matcher(content);
		List<Board> dirList = new ArrayList<Board>();
		while (matcher.find()) {
			String isDirString = matcher.group(1);
			String boardID = matcher.group(2);
			String category = matcher.group(3);
			String engName = matcher.group(4);
			String chsName = matcher.group(5);
			String moderator = matcher.group(6);
			if (moderator.length() > 25) {
				moderator = moderator.substring(0, 21) + "...";
			}
			Board board = new Board();
			board.setBoardID(boardID);
			board.setCategoryName(category);
			board.setEngName(engName);
			board.setChsName(chsName);
			board.setModerator(moderator);
			
			if (isDirString.equals("true")) {
				board.setDirectory(true);
				board.setDirectoryName(category + "  " + chsName);
				dirList.add(board);
			}
			else {
				board.setDirectory(false);
			}
			
			boardList.add(board);
		}
		
		for (Iterator<Board> iterator = dirList.iterator(); iterator.hasNext();) {
			Board board = (Board) iterator.next();
			getCategory(board.getEngName(), board.getChildBoards(), true);
		}

	}

	/**
	 * 获取版面主题列表.
	 * 
	 * @return
	 */
	public List<Subject> getSubjectList(Board board, int boardType, boolean isReloadPageNo) {
		String boardname = board.getEngName();
		int pageno = board.getCurrentPageNo();
		if (isReloadPageNo) {
			pageno = 0;
		}
		String result = getMainSubjectList(boardname, pageno, boardType);
		if (result == null) {
			return Collections.emptyList();
		}
		Pattern p = Pattern
				.compile("docWriter\\('[^']+',(\\d+),\\d+,\\d+,\\d+,(\\d+),\\d+,'[^']+',\\d+,\\d+\\)");
		Matcher m = p.matcher(result);
		if (m.find()) {
			board.setBoardID(m.group(1));
			board.setCurrentPageNo(Integer.parseInt(m.group(2)));
			if (board.getCurrentPageNo() > board.getTotalPageNo() || isReloadPageNo) {
				board.setTotalPageNo(board.getCurrentPageNo());
			}
		}

		String patternStr = "c\\.o\\((\\d+),\\d+,'([^']+)','([^']+)',(\\d+),'([^']+)',\\d+,\\d+,\\d+\\);";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(result);
		List<Subject> subjectList = new ArrayList<Subject>();
		while (matcher.find()) {
			String subjectid = matcher.group(1);
			String author = matcher.group(2);
			String type = matcher.group(3).trim();
			String dateStr = matcher.group(4);
			Date date = StringUtility.toDate(dateStr);
			String title = matcher.group(5);
			Subject subject = new Subject();
			subject.setAuthor(author);
			subject.setBoardID(board.getBoardID());
			subject.setBoardEngName(boardname);
			subject.setSubjectID(subjectid);
			subject.setTitle(title);
			subject.setType(type);
			subject.setDate(date);
			subjectList.add(subject);
		}

		return subjectList;
	}
	
	/**
	 * 获取单贴内容
	 * 
	 * @param subject
	 * @return
	 */
	public List<Post> getSinglePostList(Subject subject) {
		//String result = getPostContent(subject.getBoardID(), subject.getSubjectID());
		List<Post> postList = new ArrayList<Post>();
		Post post = new Post();
		post.setAuthor(subject.getAuthor());
		post.setSubjectID(subject.getSubjectID());
		post.setBoardID(subject.getBoardID());
		post.setBoard(subject.getBoardEngName());
		postList.add(post);
		crawler.getPostList(postList);
		
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
	public List<Post> getPostList(Subject subject) {
		String result = getPostListContent(subject.getBoardEngName(),
				subject.getSubjectID(), subject.getCurrentPageNo());
		if (result == null) {
			return Collections.emptyList();
		}
		Matcher bidMatcher = Pattern
				.compile(
						"tconWriter\\('[^']+',(\\d+),\\d+,\\d+,(\\d+),(\\d+),\\d+,\\d+,\\d+")
				.matcher(result);
		String boardid = "";
		if (bidMatcher.find()) {
			boardid = bidMatcher.group(1);
			int totalPage = Integer.parseInt(bidMatcher.group(2));
			subject.setTotalPageNo(totalPage);
			Log.d("asm : totalpageno", totalPage + "");
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

			if (flag) {
				flag = false;
				if (!StringUtility.isEmpty(author)
						&& StringUtility.isEmpty(subject.getAuthor())) {
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
	 * 获得首页导读.
	 * 
	 * @return
	 */
	public Object[] getGuidance() {
		String content = crawler
				.getUrlContent("http://www.newsmth.net/mainpage.html");
		Pattern hp = Pattern.compile(
				"<table [^<>]+class=\"HotTable\"[^<>]+>(.*?)</table>",
				Pattern.DOTALL);
		if (content == null) {
			return new Object[] { Collections.emptyList(),
					Collections.emptyList() };
		}
		Matcher hm = hp.matcher(content);
		List<String> sectionList = new ArrayList<String>();
		List<List<Subject>> subjectList = new ArrayList<List<Subject>>();
		if (hm.find()) {
			sectionList.add("水木十大");
			String hc = hm.group(1);
			Pattern boardNamePattern = Pattern
					.compile("<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
			Matcher boardNameMatcher = boardNamePattern.matcher(hc);

			Pattern hip = Pattern
					.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
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

		Pattern sp = Pattern
				.compile(
						"<span class=\"SectionName\"><a[^<>]+>([^<>]+)</a></span>(.*?)class=\"SecLine\"></td>",
						Pattern.DOTALL);
		Matcher sm = sp.matcher(content);
		while (sm.find()) {
			String sectionName = sm.group(1);
			sectionList.add(sectionName);
			String sc = sm.group(2);
			System.out.println(sectionName);

			Pattern boardNamePattern = Pattern
					.compile("\"SectionItem\">.<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
			Matcher boardNameMatcher = boardNamePattern.matcher(sc);

			Pattern sip = Pattern
					.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
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
	 * 获得个人信息
	 * 
	 * @param userID
	 *            要查询的用户ID
	 * @return
	 */
	public Profile getProfile(String userID) {
		String url = "http://www.newsmth.net/bbsqry.php?userid=" + userID;
		String content = crawler.getUrlContent(url);
		Pattern profilePattern = Pattern.compile("<pre>(.*?)</pre>",
				Pattern.DOTALL);

		Profile profile = new Profile();

		Matcher profileMatcher = profilePattern.matcher(content);
		if (profileMatcher.find()) {
			String detailString = profileMatcher.group(1);
			profile = StringUtility.parseProfile(detailString);
		}

		Pattern desPattern = Pattern.compile("prints\\('(.*?)'\\);",
				Pattern.DOTALL);
		Matcher desMatcher = desPattern.matcher(content);
		if (desMatcher.find()) {
			String descriptionString = desMatcher.group(1);
			descriptionString = descriptionString.replaceAll("[\\\\n]", "<br/>");
			profile.setDescription(descriptionString);
		} else {
			profile.setDescription("这家伙很懒，啥也没留下");
		}

		return profile;
	}

	public String getPostContent(String boardid, String subjectid) {
		String url = "http://www.newsmth.net/bbscon.php?bid=" + boardid
				+ "&id=" + subjectid;
		return crawler.getUrlContent(url);
	}

	private String getPostListContent(String board, String subjectid, int pageno) {
		String url = "http://www.newsmth.net/bbstcon.php?board=" + board
				+ "&gid=" + subjectid;
		if (pageno > 0) {
			url += "&pno=" + pageno;
		}
		return crawler.getUrlContent(url);
	}

	public String getMainSubjectList(String board, int pageno, int type) {
		String url = "";
		if (type == 0) {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=6";
		}
		else {
			url = "http://www.newsmth.net/bbsdoc.php?board=" + board + "&ftype=0";
		}
		if (pageno > 0) {
			url = url + "&page=" + pageno;
		}
		return crawler.getUrlContent(url);
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
