package com.athena.asm.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.athena.asm.data.Board;
import com.athena.asm.data.MailBox;
import com.athena.asm.data.Profile;
import com.athena.asm.data.Subject;
import com.athena.asm.util.SmthSupport;

public class HomeViewModel extends BaseViewModel {

	private List<String> m_guidanceSectionNames = null;
	private List<List<Subject>> m_guidanceSectionDetails = null;
	private List<Board> m_favList = null;
	private MailBox m_mailBox = null;
	private List<Board> m_categoryList = null;
	private List<String> m_boardFullStrings = null;
	private HashMap<String, Board> m_boardHashMap = null;

	private Profile m_currentProfile = null;
	
	private boolean m_isLogined = false;
    private boolean m_isGuestLoggedin = false;
    private String m_loginUserID = "guest";
    
    private String m_currentTab;

	private SmthSupport m_smthSupport;
	
	public static final String CURRENTTAB_PROPERTY_NAME = "CurrentTab";
	public static final String GUIDANCE_PROPERTY_NAME = "Guidance";
	public static final String CATEGORYLIST_PROPERTY_NAME = "CategoryList";
	public static final String FAVLIST_PROPERTY_NAME = "FavList";
	public static final String MAILBOX_PROPERTY_NAME = "Mailbox";
	public static final String PROFILE_PROPERTY_NAME = "Profile";
	
	public HomeViewModel() {
		m_smthSupport = SmthSupport.getInstance();
	}
	
	public boolean isLogined() {
		return m_isLogined;
	}
	
	public void setLoggedin(boolean isUserLoggedin) {
		m_isLogined = isUserLoggedin;
	}
	
	public boolean isGuestLogined() {
		return m_isGuestLoggedin;
	}
	
	public void setGuestLogined(boolean isGuestLoggedin) {
		m_isGuestLoggedin = isGuestLoggedin;
	}
	
	public String loginUserID() {
		return m_loginUserID;
	}
	
	public void updateLoginStatus() {
		m_isLogined = m_smthSupport.getLoginStatus();
		if (m_isLogined) {
			m_loginUserID = m_smthSupport.getUserid();
		}
	}
	
	public List<String> guidanceSectionNames() {
		return m_guidanceSectionNames;
	}
	
	public void setGuidanceSectionNames(List<String> guidanceSectionNames) {
		m_guidanceSectionNames = guidanceSectionNames;
	}
	
	public List<List<Subject>> guidanceSectionDetails() {
		return m_guidanceSectionDetails;
	}
	
	public void setGuidanceSectionDetails(List<List<Subject>> guidanceSectionDetails) {
		m_guidanceSectionDetails = guidanceSectionDetails;
	}
	
	public List<Board> favList() {
		return m_favList;
	}
	
	public void setFavList(List<Board> favList) {
		m_favList = favList;
	}
	
	public MailBox mailBox() {
		return m_mailBox;
	}
	
	public void setMailbox(MailBox mailbox) {
		m_mailBox = mailbox;
	}
	
	public List<Board> categoryList() {
		return m_categoryList;
	}
	
	public void setCategoryList(List<Board> categoryList) {
		m_categoryList = categoryList;
	}
	
	public List<String> boardFullStrings() {
		return m_boardFullStrings;
	}
	
	public HashMap<String, Board> boardHashMap() {
		return m_boardHashMap;
	}
	
	public Profile currentProfile() {
		return m_currentProfile;
	}
	
	public void setCurrentProfile(Profile currentProfile) {
		m_currentProfile = currentProfile;
	}
	
	public String currentTab() {
		return m_currentTab;
	}
	
	public void setCurrentTab(String currentTab) {
		m_currentTab = currentTab;
		
		m_changeObserver.OnViewModelChange(this, CURRENTTAB_PROPERTY_NAME);
	}
	
	public boolean login(String userName, String password) {
		m_smthSupport.setUserid(userName);
		m_smthSupport.setPasswd(password);
		return m_smthSupport.login();
	}
	
	public void logout() {
		m_smthSupport.destory();
	}
	
	public void restorSmthSupport() {
		m_smthSupport.restore();
	}
	
	@SuppressWarnings("unchecked")
	public void updateGuidance() {
		Object[] guidance = m_smthSupport.getGuidance();
		setGuidanceSectionNames((List<String>) guidance[0]);
		setGuidanceSectionDetails((List<List<Subject>>) guidance[1]);
	}
	
	
	public ArrayList<Board> updateFavList(ArrayList<Board> realFavList) {
		if (realFavList == null) {
			realFavList = new ArrayList<Board>();
			m_smthSupport.getFavorite("0", realFavList, 0);
		}
		
		ArrayList<Board> favList = new ArrayList<Board>();
		Board board = new Board();
		board.setDirectory(true);
		board.setDirectoryName("最近访问版面");
		board.setCategoryName("目录");
		//board.setChildBoards(new ArrayList<Board>(application.getRecentBoards()));
		favList.add(board);
		favList.addAll(realFavList);
		setFavList(favList);
		
		return realFavList;
	}
	
	public void updateCategoryList() {
		ArrayList<Board> categoryList = new ArrayList<Board>();
		m_smthSupport.getCategory("TOP", categoryList, false);
		setCategoryList(categoryList);
	}
	
	public void updateMailbox() {
		setMailbox(m_smthSupport.getMailBoxInfo());
	}
	
	public Profile getProfile(String userID) {
		Profile profile = m_smthSupport.getProfile(userID);
		if (userID.equals(m_loginUserID)) {
			setCurrentProfile(profile);
		}
		
		return profile;
	}
	
	public void updateBoardInfo() {
		m_boardFullStrings = new ArrayList<String>();
		m_boardHashMap = new HashMap<String, Board>();
		readBoadInfo(m_categoryList);
	}
	
	private void readBoadInfo(List<Board> boards) {
		for (Iterator<Board> iterator = boards.iterator(); iterator.hasNext();) {
			Board board = (Board) iterator.next();
			if (board != null && board.getEngName() != null) {
				if (!m_boardFullStrings.contains(board.getEngName())) {
					m_boardFullStrings.add(board.getEngName());
				}
				m_boardHashMap.put(board.getEngName().toLowerCase(), board);
			}
			readBoadInfo(board.getChildBoards());
		}
	}
	
	public void NotifyGuidanceChanged() {
		m_changeObserver.OnViewModelChange(this, GUIDANCE_PROPERTY_NAME);
	}
	
	public void NotifyFavListChanged() {
		m_changeObserver.OnViewModelChange(this, FAVLIST_PROPERTY_NAME);
	}
	
	public void NotifyCategoryChanged() {
		m_changeObserver.OnViewModelChange(this, CATEGORYLIST_PROPERTY_NAME);
	}
	
	public void NotifyMailboxChanged() {
		m_changeObserver.OnViewModelChange(this, MAILBOX_PROPERTY_NAME);
	}
	
	public void NotifyProfileChanged(Profile profile, int step) {
		m_changeObserver.OnViewModelChange(this, PROFILE_PROPERTY_NAME, profile, step);
	}
	
}
