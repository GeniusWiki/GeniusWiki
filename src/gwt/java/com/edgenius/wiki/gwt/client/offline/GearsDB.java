/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.wiki.gwt.client.offline;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.model.OfflineModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PageThemeModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.gears.client.Factory;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.gears.client.database.Database;
import com.google.gwt.gears.client.database.DatabaseException;
import com.google.gwt.gears.client.database.ResultSet;

/**
 * Please note: all date time format is long (string) format which is from Date.getTime();  
 * @author Dapeng.Ni
 */
public class GearsDB {
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Following table will be DEFAUT Database
	private static final String TABLE_DROP_USER = "drop table if exists edgenius_users";

	private static final String TABLE_CREATE_USER = " create table if not exists edgenius_users ("
			+ " uid INTEGER PRIMARY KEY AUTOINCREMENT, " 
			+ " db_version integer, " 
			+ " user_uid integer, "
			+ " username text, " 
			+ " fullname text, " 
			+ " portrait text, " 
			+ " home_layout text " + ") ";

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Following table will be User owned Database
	private static final String TABLE_DROP_SPACE = "drop table if exists  edgenius_spaces";

	private static final String TABLE_DROP_PAGE = "drop table if exists edgenius_pages";

	private static final String TABLE_DROP_ATTACHMENT = "drop table if exists edgenius_attachments";

	private static final String TABLE_DROP_COMMENT = "drop table if exists edgenius_comments";
	
	private static final String TABLE_DROP_TAG = "drop table if exists edgenius_tags";
	
	private static final String TABLE_DROP_PAGE_TAG = "drop table if exists edgenius_page_tags";
	
	private static final String TABLE_DROP_THEME = "drop table if exists edgenius_themes";

	private static final String TABLE_CREATE_SPACE = " create table if not exists edgenius_spaces ("
			+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
			+ " description text, " 
			+ " tag_string text, " 
			+ " logo_large text, "
			+ " logo_small text, " 
			+ " name text unique, " 
			+ " removed integer not null, " 
			+ " type integer not null, "
			+ " unix_name text unique, "
			+ " homepage_uuid text," 
			+ " permissions text,"
			+ " sync_options integer not null,"
			+ " sync_date long not null" + ") ";

	// This table mixed auto/manual draft, history and current page:
	// draft: type > 0, refer to SharedConstants.AUTO_DRAFT or
	// SharedConstants.MANUAL_DRAFT
	// history: isHistory is true
	// otherwise it is current page: type==0 and isHistory=false
	private static final String TABLE_CREATE_PAGE = " create table if not exists edgenius_pages ( "
		//!!!CANNOT use page_uid from server side, as this page also save history,draft, which may has same page_uid
			+ " uid INTEGER PRIMARY KEY AUTOINCREMENT, "  
			// **This field is from Page_Content table and modify type to "text"
			+ " content text, "
			// **This field is identify if page has been edit in offline mode
			+ " unix_name text, " 
			+ " title text, " 
			+ " page_uuid text, " 
			+ " version integer not null, "
			+ " parent_page_uuid text, " 
			+ " type integer not null, " 
			+ " level integer not null, "
			+ " tagString text, " 
			+ " favorite integer, " 
			+ " watched integer, " 
			+ " creator text, "
			+ " creatorUsername text, " + " creatorPortrait text, " + " createDate long, " 
			+ " modifier text, "
			+ " modifierUsername text, " + " modifierPortrait text, " + " modifiedDate long, "
			+ " isHistory integer  not null, "
			+ " permissions text," 
			+ " offline_updated integer  not null" 
			+ " )";
	
	private static final String TABLE_CREATE_ATTATCHMNET = " create table if not exists edgenius_attachments ("
			+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
			+ " spaceUname text, " 
			+ " pageUuid text, " 
			+ " nodeUuid text, "
			+ " version text, " 
			+ " filename text, " 
			+ " creator text, " 
			+ " date long, " 
			+ " size long, "
			+ " desc text, " 
			+ " status integer, " 
			+ " submissionDate long, "
			+ " submissionRetry integer, "
			+ " offline_updated integer  not null" 
			+ ") ";

	private static final String TABLE_CREATE_COMMENT = " create table if not exists edgenius_comments ("
			+ " uid INTEGER, " 
			+ " spaceUname text, " 
			+ " pageUuid text, " 
			+ " author text, " 
			+ " date long, " 
			+ " body text, "
			+ " level integer, " 
			+ " rootUid integer, " 
			+ " parentUid integer, " 
			+ " hide integer " 
			+ ") ";
	
	private static final String TABLE_CREATE_TAG = " create table if not exists edgenius_tags ("
		+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
		+ " type int,"
		+ " tag text,"
		+ " unix_name text"
		+ ") ";
	
	private static final String TABLE_THEME = " create table if not exists edgenius_themes ("
		+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
		+ " spaceUname text,"
		//could be PageUuid, SharedConstant.THEME_PAGE_TYPE_HOME or THEME_PAGE_TYPE_DEFAULT
		+ " pageType text,"
		+ " welcome text,"
		+ " body text,"
		+ " sidebar text"
		+ ") ";
	
	private static final String TABLE_CREATE_PAGE_TAG = " create table if not exists edgenius_page_tags ("
		+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
		+ " tag_uid INTEGER,"
		+ " page_uuid text"
		+ ") ";

	private static final String INDEX_CREATE_PAGES_1 = "create index if not exists index_page_1 on edgenius_pages (page_uuid)";

	private static final String INDEX_CREATE_PAGES_2 = "create index if not exists index_page_2 on edgenius_pages (unix_name,title)";
	
	private static final String INDEX_CREATE_ATTACHMENT_1 = "create index if not exists index_att_1 on edgenius_attachments (pageUuid)";
	
	private static final String INDEX_CREATE_COMMENT_1 = "create index if not exists index_comment_1 on edgenius_comments (pageUuid)";
	
	private static final String INDEX_CREATE_TAG_1 = "create index if not exists index_tag_1 on edgenius_tags (tag)";
	
	private static final String INDEX_CREATE_PTAG_1 = "create index if not exists index_ptag_1 on edgenius_page_tags (tag_uid,page_uuid)";

	private static final String INDEX_CREATE_THEME = "create index if not exists index_theme_1 on edgenius_themes (spaceUname,pageType)";
	private static final int PAGE_TAG_TYPE = 0;

	//user double SharedConstants.PORTLET_SEP as separator
	private static String ITME_SEP = SharedConstants.PORTLET_SEP+SharedConstants.PORTLET_SEP;

	Database database;

	private GearsDB(Integer userUid) throws GearsException {
		database = initUserDBInstance(userUid);
	}

	/**
	 * @param userUid: This is server side user_uid from edgenius_users table. Anonymous will be -1.
	 * @return
	 * @throws GearsException 
	 */
	public static GearsDB getUserDB(Integer userUid) throws GearsException {
		return new GearsDB(userUid);
	}
	//********************************************************************
	//               Default DB method: could be static
	//********************************************************************

	/**
	 * @param username
	 * @return
	 */
	public static UserModel getUser(String username) {
		UserModel model = new UserModel();
		// anonymous
		model.setUid(-1);
//		model.setLoginname("anonymous");
		
		if(username == null)
			return model;
		
		ResultSet rs = null;
		Database defaultDB = null;
		try {
			defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select * from edgenius_users where username=?", new String[]{str(username)});
			if (rs.isValidRow()) {
				model.setUid(rs.getFieldAsInt(2));
				model.setLoginname(rs.getFieldAsString(3));
				model.setFullname(rs.getFieldAsString(4));
				model.setPortrait(rs.getFieldAsString(5));
			}
		} catch (Exception e) {
			Log.error("Get user by name with error " + username, e);
		} finally{
			try {
				if(rs != null)rs.close();
				if(defaultDB != null) defaultDB.close();
			} catch (Exception e) {
			}
		}
	
		return model;
	}

	/**
	 * @param userUid
	 * @return
	 */
	public static boolean isExistUser(Integer userUid) {
		if(userUid == null)
			userUid = -1;
		
		ResultSet rs = null;
		Database defaultDB = null;
		try {
			defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select user_uid from edgenius_users where user_uid=?", new String[]{str(userUid)});
			if (rs.isValidRow()) {
				return true;
			}
		} catch (GearsException e) {
			Log.info("Check user exist or not failed. Return false.");
		} finally{
			try {
				if(rs != null)rs.close();
				if(defaultDB != null) defaultDB.close();
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * Update user portal by new list of portlets
	 * @param porlets
	 * @throws GearsException 
	 */
	public static void updatePortal(Integer userUid, List<String> portlets) throws GearsException{
		if(portlets == null)
			return;
		
		StringBuffer layoutSb = new StringBuffer();
		for (String portal : portlets) {
			layoutSb.append(portal).append(ITME_SEP);
		}
		String layout = "";
		if(layoutSb.length() > ITME_SEP.length()){
			//remove last sep
			layout = layoutSb.substring(0, layoutSb.length() -  ITME_SEP.length());
		}
		
		userUid = userUid == null?-1:userUid; 
		Database defaultDB = Factory.getInstance().createDatabase();
		defaultDB.open(OfflineConstants.DEFAULT_DB);
		defaultDB.execute("update edgenius_users set home_layout=? where user_uid=?",new String[]{str(layout),str(userUid)});
		Log.info("Layout update:" + layout);
		
	}
	/**
	 * If space is not exist in portal, then put it to portal. Otherwise, do nothing
	 * @param userUid
	 * @param layoutStr
	 */
	public static void addSpaceToPortal(Integer userUid, String spaceUname) {
	
		updateLayoutWithSpace(userUid, spaceUname, true);
		
	}
	/**
	 * @param userUid
	 * @param spaceUname
	 */
	public static void removeLayout(Integer userUid, String spaceUname) {

		updateLayoutWithSpace(userUid, spaceUname, false);
	}


	/**
	 * @return
	 */
	public static List<String> getHomeLayout(Integer userUid) {
		if(userUid == null) 
			userUid =  -1;
		
		ResultSet rs = null;
		Database defaultDB = null;
		List<String> list = new ArrayList<String>();
		try {
			defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select home_layout from edgenius_users where user_uid=?", new String[]{str(userUid)});
			if (rs.isValidRow()) {
				String layout = rs.getFieldAsString(0);
				//user double SharedConstants.PORTLET_SEP as separator
				String[] ls = layout.split("\\"+SharedConstants.PORTLET_SEP+"\\"+SharedConstants.PORTLET_SEP);
				if(ls != null){
					for(int idx=0;idx<ls.length;idx++){
						list.add(ls[idx]);
					}
				}
			}
		} catch (DatabaseException e) {
			Log.error("Get user error ", e);
		} finally{
			try {
				if(rs != null)rs.close();
				if(defaultDB != null) defaultDB.close();
			} catch (Exception e) {
			}
		}
		return list;
	}
	/**
	 *  default DB may not exist or version is obsolete
	 */
	public static boolean isNeedResetDefaultDB(int mainDBVersion) {
		ResultSet rs = null;
		int existVer = -1;
		try {
			Database defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select db_version from edgenius_users where user_uid=?", new String[]{"-2"});
			if (rs.isValidRow()) {
				existVer = rs.getFieldAsInt(0);
			}
		} catch (Exception e) {
			Log.info("System Database read error, may cause it is first time to version check" + e);
		} finally {
			if (rs != null) {
				try {rs.close();} catch (DatabaseException e) {}
			}
		}
		return existVer < mainDBVersion;
		
	}
	/**
	 * User DB not exist or version obsolete
	 */
	public static boolean isNeedResetUserDB(int userUid, int userDBVer){

		ResultSet rs = null;
		// check version to confirm tables need re-created
		int existVer = -1;
		try {
			Database defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			Log.info("Check database update for user " + userUid );
			rs = defaultDB.execute("select db_version from edgenius_users where user_uid=?", new String[]{str(userUid)});
			if (rs.isValidRow()) {
				existVer = rs.getFieldAsInt(0);
			}
		} catch (Exception e) {
			Log.info("User Database read error, may cause it is first time to check user DB version, DB not exist yet." + e);
		} finally {
			if (rs != null) {
				try {rs.close();} catch (DatabaseException e) {}
			}
		}
		
		return existVer < userDBVer;
	}
	/**
	 * @return
	 */
	public static Map<Integer, UserEssential> getUsersEssential() {
		ResultSet rs = null;
		Map<Integer,UserEssential> backup = new HashMap<Integer, UserEssential>();
		try {
			Database defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select db_version,user_uid,home_layout from edgenius_users where user_uid !=?"
					, new String[]{str(OfflineConstants.DEFAULT_USER_UID)});
			while (rs.isValidRow()) {
				UserEssential user = new UserEssential();
				user.dbVer = rs.getFieldAsInt(1); //db_version
				user.uid = rs.getFieldAsInt(2);//userUid
				user.layout = rs.getFieldAsString(3); //home layout
				backup.put(user.uid,user);
				rs.next();
			}
		} catch (Exception e) {
			Log.info("Get user essential failed. This may cause by Default DB not exist.",e);
		} finally {
			if (rs != null) {
				try {rs.close();} catch (DatabaseException e) {}
			}
		}
		
		return backup;
	}
	/**
	 * @return
	 */
	public Map<String, SpaceEssential> getSpacesEnssential() {
		ResultSet rs = null;
		Map<String, SpaceEssential> backup = new HashMap<String, SpaceEssential>(); 
		try{
			rs = database.execute("select unix_name,sync_options from edgenius_spaces");
			while(rs.isValidRow()){
				SpaceEssential space = new SpaceEssential();
				space.spaceUname = rs.getFieldAsString(0);
				space.options = rs.getFieldAsInt(1);
				backup.put(space.spaceUname, space);
				
				rs.next();
			}

		}catch (Exception e) {
			Log.info("Failed get spaces essentail, this may cause by user DB not exist",e);
		} finally {
			if (rs != null) {
				try {rs.close();} catch (DatabaseException e) {}
			}
		}
		return backup;
	}
	/**
	 * Drop default DB and create it, then save all user essential information into DB.
	 * @param existUsers
	 */
	public static void resetDefaultDB(Collection<UserEssential> existUsers) {
		try {
			Database defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			// init Default DB
			defaultDB.execute(TABLE_DROP_USER);
			defaultDB.execute(TABLE_CREATE_USER);
			Log.info("Default database created.");
			
			//insert all exist users
			//insert backup value to new table
			for (UserEssential user : existUsers) {
				defaultDB.execute("insert into edgenius_users(db_version,user_uid,home_layout) values(?,?,?)",
						new String[] {str(user.dbVer),str(user.uid),  str(user.layout)});
			}
		} catch (Exception e) {
			Log.error("Unable to initialize default DB  and rollback users",e);
		}
	}

	/**
	 * @param spaces
	 */
	public void resetUserDB(Collection<SpaceEssential> spaces) {
		try {
			//drop
			database.execute(TABLE_DROP_SPACE);
			database.execute(TABLE_DROP_PAGE);
			database.execute(TABLE_DROP_ATTACHMENT);
			database.execute(TABLE_DROP_COMMENT);
			database.execute(TABLE_DROP_TAG);
			database.execute(TABLE_DROP_PAGE_TAG);
			database.execute(TABLE_DROP_THEME);
			
			//create
			database.execute(TABLE_CREATE_SPACE);
			database.execute(TABLE_CREATE_PAGE);
			database.execute(TABLE_CREATE_ATTATCHMNET);
			database.execute(TABLE_CREATE_COMMENT);
			database.execute(TABLE_CREATE_TAG);
			database.execute(TABLE_CREATE_PAGE_TAG);
			database.execute(TABLE_THEME);
			
			//index
			database.execute(INDEX_CREATE_PAGES_1);
			database.execute(INDEX_CREATE_PAGES_2);
			database.execute(INDEX_CREATE_ATTACHMENT_1);
			database.execute(INDEX_CREATE_COMMENT_1);
			database.execute(INDEX_CREATE_TAG_1);
			database.execute(INDEX_CREATE_PTAG_1);
			database.execute(INDEX_CREATE_THEME);
			Log.info("User DB is created.");
		} catch (DatabaseException e) {
			Log.error("Unexception to reset Offline database with exception.", e);
		}
		
		for (SpaceEssential space : spaces) {
			int options = space.options <= 0?OfflineUtil.getDefaultSyncOptions():space.options;
			try {

				database.execute("insert into edgenius_spaces (sync_options,unix_name,removed,type,sync_date) values(?,?,?,?,?)"
						, new String[] {
						str(options), 
						str(space.spaceUname),"0","0","0"});
			} catch (DatabaseException e) {
				Log.error("Can not initialize space " + space.spaceUname,e);
			}
		}

	}
	/**
	 * @param userUid
	 * @param offlineDBVersion
	 */
	public static void updateUserDBVersion(int userUid, int offlineDBVersion) {
		try {
			Database defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			defaultDB.execute("update edgenius_users set db_version=? where user_uid=?", 
					new String[]{str(offlineDBVersion),str(userUid)});
		} catch (Exception e) {
			Log.error("Failed upgrade user db version to " + offlineDBVersion + "; In next sycn cicle, user DB have to reset again.",e);
		}
		
		
	}
	//********************************************************************
	//               Method for user database
	//********************************************************************
	/**
	 * @param database
	 * @param page
	 * @return status of update: false means this page does not update as its offline updated version does not upload success
	 * @throws DatabaseException
	 */
	public boolean saveOrUpdatePage(PageModel page, boolean offlineEditing) throws DatabaseException {
		boolean update = false;
		// use uuid, version, and type(for auto/manual draft)check: if page is duplicated.
		// isHistory won't as key: as download page, it may update same version to history 
		ResultSet rs = null;
		try{
			rs = database.execute("select page_uuid from edgenius_pages where page_uuid=? and version=? and type=?"
					, new String[] {str(page.pageUuid), str(page.pageVersion), str(page.type)});

			if (rs.isValidRow()) {
				update = true;
			}
	
			StringBuffer richContent = new StringBuffer();
			// serialise renderPieces to Rich Tag string
			if (page.renderContent != null) {
				for (RenderPiece piece : page.renderContent) {
					richContent.append(piece.toRichAjaxTag());
				}
			}
	
			if (update) {
				database.execute("update edgenius_pages set content=?,unix_name=?,title=?,parent_page_uuid=?"
						+ ",level=?,tagString=?,favorite=?,watched=?"
						+ ",creator=?,creatorUsername=?,creatorPortrait=?,createDate=?"
						+ ",modifier=?,modifierUsername=?,modifierPortrait=?,modifiedDate=?"
						+ ",isHistory=?,permissions=?,offline_updated=? " + " where page_uuid=? and version=? and type=?"
						, new String[] {
						str(richContent.toString()), 
						str(page.spaceUname), 
						str(page.title), 
						str(page.parentPageUuid), str(page.level), str(page.tagString), str(page.favorite),
						str(page.watched), str(page.creator), str(page.creatorUsername), str(page.creatorPortrait),
						str(page.createDate), 
						str(page.modifier), 
						str(page.modifierUsername), 
						str(page.modifierPortrait),
						str(page.modifiedDate), 
						str(page.isHistory),
						str(page.permissions),
						str(offlineEditing?SharedConstants.OFFLINE_EDITED:SharedConstants.OFFLINE_DOWNLOAD_FROM_SERVER),
						// last is pageUuid for WHERE statement
						str(page.pageUuid),
						str(page.pageVersion),
						str(page.type)});
				Log.info("Page update for uuid: " + page.pageUuid + "; version: " + page.pageVersion + "; type:"+page.type + "; isHistory: "
						+ page.isHistory);
			} else {
				database.execute("insert into edgenius_pages (content" +
								",unix_name" +
								",title" +
								",page_uuid" +
								",version" +
								",parent_page_uuid"
									+ ",type,level,tagString,favorite,watched"
									+ ",creator,creatorUsername,creatorPortrait,createDate,modifier,modifierUsername" 
									+ ",modifierPortrait,modifiedDate"
									+ ",isHistory,permissions,offline_updated) "
									+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", new String[] {
									str(richContent.toString()),
									str(page.spaceUname), 
									str(page.title),
									str(page.pageUuid),
									str(page.pageVersion),
									str(page.parentPageUuid),
									str(page.type),str(page.level), str(page.tagString), str(page.favorite),
									str(page.watched), str(page.creator), str(page.creatorUsername),
									str(page.creatorPortrait), str(page.createDate), str(page.modifier),
									str(page.modifierUsername), str(page.modifierPortrait), str(page.modifiedDate),
									str(page.isHistory), 
									str(page.permissions), 
									str(offlineEditing?SharedConstants.OFFLINE_EDITED:SharedConstants.OFFLINE_DOWNLOAD_FROM_SERVER)});
				
				Log.info("Page saved for uuid: " + page.pageUuid + "; version: " + page.pageVersion+ "; type:"+page.type + "; isHistory: "
						+ page.isHistory);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return true;
	}

	/**
	 * @param tagString
	 * @throws DatabaseException 
	 */
	public void saveOrUpdatePageTags(String spaceUname,String pageUuid, String tagString) throws DatabaseException {
		//parse tag string to tag list
		String[] tags = tagString.split(",");
		for (String tag : tags) {
			saveOrUpdatePageTag(spaceUname,pageUuid, tag.trim());
		}
	}


	/**
	 * @param space
	 * @param database
	 * @throws DatabaseException
	 */
	public int saveOrUpdateSpace(SpaceModel space) throws DatabaseException {
		boolean update = false;
		ResultSet rs = null,rs1 = null;
		int uid = -1;
		try{
			rs =  database.execute("select uid  from edgenius_spaces where unix_name=?", new String[] {str(space.unixName)});
			if (rs != null && rs.isValidRow()) {
				uid = rs.getFieldAsInt(0);
				update = true;
			}
			if (update) {
				database.execute("update edgenius_spaces set description=?," +
						"tag_string=?,name=?," +
						"removed=?,type=?," +
						"homepage_uuid=?" + 
						",logo_large=?,logo_small=?,permissions=?" +
						",sync_date=? " 
						+ " where unix_name=?", 
						new String[] {
						str(space.description), 
						str(space.tags), 
						str(space.name), 
						str(space.isRemoved), 
						str(space.type),
						str(space.homepageUuid), 
						str(space.largeLogoUrl), 
						str(space.smallLogoUrl),
						str(space.permissions), 
						str(space.syncDate), 
						// last is pageUuid for WHERE statement
						str(space.unixName) });
			
			} else {
				database.execute("insert into edgenius_spaces (unix_name,description,tag_string,name,removed,type"
						+ ",homepage_uuid,logo_large,logo_small,permissions,sync_date) " 
						+ " values(?,?,?,?,?,?,?,?,?,?,?)",
						new String[] { 
								str(space.unixName), 
								str(space.description), 
								str(space.tags), 
								str(space.name), 
								str(space.isRemoved),
								str(space.type), str(space.homepageUuid), str(space.largeLogoUrl), 
								str(space.smallLogoUrl),
								str(space.permissions),
								str(space.syncDate)});
				//get latest uid
				rs1 = database.execute("select uid from edgenius_spaces where unix_name=?",
						new String[] { str(space.unixName) });
				if (rs1 != null && rs1.isValidRow()) {
					uid = rs1.getFieldAsInt(0);
				}
			}
			return uid;
		}finally{
			try{
				if(rs != null)rs.close();
				if(rs1 != null)rs1.close();
			}catch(Exception e){}

		}
	}


	public void saveOrUpdateTheme(String spaceUname, PageThemeModel theme) throws DatabaseException {
		boolean update = false;
		ResultSet rs = null;
		try{
			rs =  database.execute("select uid  from edgenius_themes where spaceUname=? and pageType=? "
					, new String[] {str(spaceUname),str(theme.type)});
			if (rs != null && rs.isValidRow()) {
				update = true;
			}
			if (update) {
				database.execute("update edgenius_themes set " +
						"welcome=?," +
						"body=?," +
						"sidebar=?" 
						+ " where spaceUname=? and pageType=? ", 
						new String[] {
						str(theme.welcome), 
						str(theme.bodyMarkup), 
						str(theme.sidebarMarkup), 
						// last is pageUuid for WHERE statement
						str(spaceUname), 
						str(theme.type)});
			
			} else {
				database.execute("insert into edgenius_themes (spaceUname,pageType,welcome,body,sidebar) " 
						+ " values(?,?,?,?,?)",
						new String[] { 
								str(spaceUname), 
								str(theme.type),
								str(theme.welcome), 
								str(theme.bodyMarkup), 
								str(theme.sidebarMarkup)});
				
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		
	}


	/**
	 * @param database
	 * @param attachmentNodes
	 * @throws DatabaseException
	 */
	public AttachmentModel saveOrUpdateAttachment(String spaceUname, String pageUuid, AttachmentModel attachment, int offlineUpdated) throws DatabaseException {
		boolean update = false;
		ResultSet rs = null;
		try{
			rs =  database.execute("select offline_updated from edgenius_attachments where nodeUuid=? and version=?"
					, new String[] {attachment.nodeUuid, attachment.version });
			if (rs.isValidRow()) {
				update = true;
				int offlineU = rs.getFieldAsInt(0);
				if(offlineUpdated == 0&& offlineU > 0){
					//saving from server side download, but this record has not been upload successfully (sync -> upload first)
					//then the update won't continue, it means, this same does not sync success!
					return attachment;
				}
			}
			attachment.offlineEdited = offlineUpdated;
			if(offlineUpdated > 0){
				//need get nodeUuid and new max version if the file exist, otherwise, create a nodeUuid
				//get attachment by filename:
				ResultSet rs1 = null;
				try {
					rs1 = database.execute("select * from edgenius_attachments where filename=? and spaceUname=? and pageUuid=? order by version desc", 
							new String[]{str(attachment.filename),str(spaceUname),str(pageUuid)});
					if(rs1.isValidRow()){
						AttachmentModel att = retrieveAttachment(rs1);
						attachment.nodeUuid = att.nodeUuid;
						attachment.version = Integer.valueOf(NumberUtil.toInt(att.version,0) + 1).toString();
					}else{
						//ok, can not find this file exist yet then create new node uuid
						attachment.nodeUuid = OfflineUtil.createAttachmentUuid(this);
						attachment.version = 1+"";
					}
				}finally{
					try{rs1.close();}catch(Exception e){}
				}
			}
			if (update) {
				database.execute("update edgenius_attachments set spaceUname=?,pageUuid=?,filename=?,creator=?"
						+ ",date=?,size=?,desc=?,status=?,submissionDate=?,submissionRetry=?,offline_updated=? " 
						+ " where nodeUuid=? and version=?", new String[] {
						str(spaceUname),
						str(pageUuid),
						str(attachment.filename), str(attachment.creator), str(attachment.date),
						str(attachment.size), 
						str(attachment.desc), 
						str(attachment.status), 
						str(attachment.submissionDate), 
						str(attachment.submissionRetry), 
						str(offlineUpdated), 
						// last is nodeUuid and version
						str(attachment.nodeUuid), str(attachment.version) });
				Log.info("Attachment update for uuid: " + attachment.nodeUuid + "; version: " + attachment.version);
			} else {
				database.execute("insert into edgenius_attachments (spaceUname,pageUuid,nodeUuid,version,filename,creator"
						+ ",date,size,desc,status,submissionDate,submissionRetry, offline_updated) " 
						+ " values(?,?,?,?,?,?,?,?,?,?,?,?,?)", 
						new String[] {
						str(spaceUname), 
						str(pageUuid), 
						str(attachment.nodeUuid),  
						str(attachment.version), 
						str(attachment.filename),
						str(attachment.creator), str(attachment.date), str(attachment.size), 
						str(attachment.desc),
						str(attachment.status), 
						str(attachment.submissionDate), 
						str(attachment.submissionRetry), 
						str(offlineUpdated)});
				Log.info("Attachment saved for uuid: " + attachment.nodeUuid + "; version: " + attachment.version);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return attachment;

	}
	public static void saveOrUpdateUser(UserModel user) throws GearsException{
		ResultSet rs = null;
		
		Database defaultDB = Factory.getInstance().createDatabase();
		defaultDB.open(OfflineConstants.DEFAULT_DB);
		try{
			boolean update = false;
			rs =  defaultDB.execute("select user_uid from edgenius_users where user_uid=?"
					, new String[] {str(user.getUid())});
			if (rs.isValidRow()) {
				update = true;
			}
			if (update) {
				defaultDB.execute("update edgenius_users set db_version=?,username=?,fullname=?,portrait=? " 
						+ " where user_uid=? ", new String[] {
								str(user.getOfflineDBVersion()), 
								str(user.getLoginname()),
								str(user.getFullname()),
								str(user.getPortrait()),
								str(user.getUid())}); 
				Log.info("User update for : " + user.getLoginname());
			} else {
				defaultDB.execute("insert into edgenius_users(db_version,user_uid,username,fullname,portrait) values(?,?,?,?,?)",
						new String[] { 
						str(user.getOfflineDBVersion()), 
						str(user.getUid()),
						str(user.getLoginname()), 
						str(user.getFullname()),
						str(user.getPortrait())});
		
				Log.info("User saved for : " + user.getLoginname());
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
	}


	/**
	 * @param spaceUname 
	 * @param database
	 * @param comment
	 * @throws DatabaseException
	 */
	public void saveOrUpdateComment(String spaceUname, CommentModel comment) throws DatabaseException {
		boolean update = false;
		ResultSet rs = null;
		try{
			rs =  database.execute("select uid from edgenius_comments where uid=?", new String[] { str(comment.uid) });
			if (rs != null && rs.isValidRow()) {
				update = true;
			}
			if (update) {
				database.execute("update edgenius_comments set spaceUname=?, pageUuid=?,author=?,date=?,body=?"
						+ ",level=?,rootUid=?,parentUid=?, hide=? " + " where uid=?", 
						new String[] {
						str(spaceUname),
						str(comment.pageUuid), str(comment.author), str(comment.modifiedDate), str(comment.body),
						str(comment.level), str(comment.rootUid), str(comment.parentUid), str(comment.hide),
						// last as key
						str(comment.uid) });
				Log.info("Comment update for uid: " + comment.uid);
			} else {
				database.execute("insert into edgenius_comments (uid,spaceUname,pageUuid,author,date,body"
						+ ",level,rootUid,parentUid,hide) " + " values(?,?,?,?,?,?,?,?,?,?)", 
						new String[] {
						str(comment.uid), 
						str(spaceUname),
						str(comment.pageUuid), str(comment.author), str(comment.modifiedDate), str(comment.body),
						str(comment.level), str(comment.rootUid), str(comment.parentUid),str(comment.hide),});
				Log.info("Comment save for uid: " + comment.uid);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
	}

	/**
	 * @param nodeUuid
	 * @param version
	 * @param offlineSubmissioning
	 * @throws DatabaseException 
	 */
	public void updateAttachmentStatus(String nodeUuid, String version, int offlineSubmissioning) throws DatabaseException {
		database.execute("update edgenius_attachments set offline_updated=? where nodeUuid=? and version=?"
				,new String[]{str(offlineSubmissioning),str(nodeUuid),str(version)});
		
	}
	/**
	 * @param key
	 * @param value
	 * @throws DatabaseException 
	 */
	public void removeOfflineEditedPage(String oldUuid, int type) throws DatabaseException {
		
		database.execute("delete from edgenius_pages where page_uuid=? and type=? and offline_updated>0"
			, new String[]{str(oldUuid),str(type)});

	}

	/**
	 * if version is null, delete all version for this node
	 */
	public List<AttachmentModel> removeAttachment(String pageUuid, String nodeUuid,String version, boolean permenant) throws DatabaseException{
		//get all attachments in pages
		List<AttachmentModel> list = new ArrayList<AttachmentModel>();
		ResultSet rs = null;
		String verQ = version==null?"":" and version=?";
		try{
			//get all attachment except marked as removed
			rs =  database.execute("select * from edgenius_attachments where pageUuid=? and  nodeUuid=?" + verQ
				 , version == null? new String[]{str(pageUuid),str(nodeUuid)}: new String[]{str(pageUuid),str(nodeUuid), str(version)});
			while(rs.isValidRow()){
				AttachmentModel model = retrieveAttachment(rs);
				list.add(model);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		
		//delete
		if(permenant){
			//delete downloaded from serer side also
			database.execute("delete from edgenius_attachments where pageUuid=? and nodeUuid=?" + verQ
					, version == null? new String[]{str(pageUuid),str(nodeUuid)}: new String[]{str(pageUuid),str(nodeUuid), str(version)});
		}else{
			//delete all offline edit version, they don't need sync with server side anymore
			database.execute("delete from edgenius_attachments where pageUuid=? and nodeUuid=? and offline_updated>0" + verQ
					, version == null? new String[]{str(pageUuid),str(nodeUuid)}: new String[]{str(pageUuid),str(nodeUuid), str(version)});
			//mark all online version offlineUpdated to delete status
			database.execute("update edgenius_attachments set offline_updated="+SharedConstants.OFFLINE_DELETED
					+" where pageUuid=? and nodeUuid=?" + verQ
					, version == null? new String[]{str(pageUuid),str(nodeUuid)}: new String[]{str(pageUuid),str(nodeUuid), str(version)});
		}
				
		return list;
	}
	/**
	 * Get page status according to attachment type(manual, auto, or current page's) and these attachment must not be marked 
	 * removed in offline edit.
	 * @param pageUuid
	 * @return
	 * @throws DatabaseException
	 */
	public List<AttachmentModel> getPageAttachments(String pageUuid,int type) throws DatabaseException {
		List<AttachmentModel> list = new ArrayList<AttachmentModel>();
		ResultSet rs = null;
		try{
			//get all attachment except marked as removed
			rs =  database.execute("select * from edgenius_attachments where pageUuid=? and status=? and offline_updated!="+SharedConstants.OFFLINE_DELETED
				, new String[]{str(pageUuid),str(type)});
			while(rs.isValidRow()){
				AttachmentModel model = retrieveAttachment(rs);
				list.add(model);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}

	/**
	 * Check if this nodeUuid is exist in table, whatever it is status (deleted or offline edited)
	 * @param uuid
	 * @return
	 * @throws DatabaseException 
	 */
	public boolean hasAttachmentByNodeUuid(String uuid) throws DatabaseException {
		ResultSet rs = null;
		try{
			rs =  database.execute("select uid from edgenius_attachments where nodeUuid=?"
				, new String[]{str(uuid)});
			return rs.isValidRow();
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
	}
	public SpaceModel getSpace(String spaceUname) throws DatabaseException {
		ResultSet rs = null;
		try{
			rs = database.execute("select * from edgenius_spaces where unix_name=? "
				, new String[]{str(spaceUname)});
			if(rs.isValidRow()){
				return retrieveSpace(rs);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return null;
	}


	public List<SpaceModel> getAllSpaces() {
		ResultSet rs = null;
		try {
			List<SpaceModel> spaces = new ArrayList<SpaceModel>();
			rs = database.execute("select * from edgenius_spaces");
			while(rs.isValidRow()){
				spaces.add(retrieveSpace(rs));
				rs.next();
			}
			return spaces;
		} catch (DatabaseException e) {
			Log.warn("Can not get spaces list");
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return null;
	}


	/**
	 * @param spaceUname
	 * @param pageTitle
	 * @return
	 * @throws DatabaseException 
	 */
	public PageModel getPageByTitle(String spaceUname, String pageTitle) throws DatabaseException {
		
		ResultSet rs = null;
		try{
			rs =  database.execute("select * from edgenius_pages where unix_name=? and title=? and isHistory=0 and type=0"
				, new String[]{str(spaceUname),str(pageTitle)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return null;
	}
	/**
	 * @param spaceUname
	 * @return
	 * @throws DatabaseException 
	 */
	public PageModel getHomepage(String spaceUname) throws DatabaseException {
		ResultSet rs = null;
		try{
			rs =  database.execute("select p.* from edgenius_pages as p, edgenius_spaces as s " +
					" where s.unix_name=? and s.homepage_uuid=p.page_uuid and p.isHistory=0 and p.type=0"
				, new String[]{str(spaceUname)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return null;
	}
	/**
	 * @param pageUuid
	 * @return
	 * @throws DatabaseException 
	 */
	public PageModel getCurrentPageByUuid(String pageUuid) throws DatabaseException {
		ResultSet rs = null;
		try{
			rs =  database.execute("select * from edgenius_pages where page_uuid=?  and isHistory=0 and type=0"
				, new String[]{str(pageUuid)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return null;
	}
	public PageModel getHistoryByUid(Integer uid) throws DatabaseException {
		ResultSet rs = null;
		try{
			//input is page_uid rather than record uid.
			rs =  database.execute("select * from edgenius_pages where uid=?  and isHistory=1 and type=0"
				, new String[]{str(uid)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return null;
	}


	public ArrayList<PageItemModel> getPagesFromSpace(String spaceUname, int returnNum) throws DatabaseException {

		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try{
			rs =  database.execute("select * from edgenius_pages where unix_name=? and isHistory=0 and type=0 order by modifiedDate desc" 
				, new String[]{str(spaceUname)});
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}


	public ArrayList<PageItemModel> getMyDrafts() throws DatabaseException {
		ResultSet rs = null;
		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		try{
			rs = database.execute("select * from edgenius_pages where type>0 order by type asc, modifiedDate desc");
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}
	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 * @throws DatabaseException 
	 */
	public int getCommentCount(String pageUuid) throws DatabaseException {
		int ret = 0;
		ResultSet rs = null;
		try{
			rs = database.execute("select count(*) from edgenius_comments where pageUuid=?" , new String[]{str(pageUuid)});
			if(rs.isValidRow()){
				ret = rs.getFieldAsInt(0);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return ret;
	}

	/**
	 * @param spaceUname
	 * @param pageUuid
	 * @return
	 */
	public List<CommentModel> getComments(String pageUuid) throws DatabaseException{
		List<CommentModel> list = new ArrayList<CommentModel>();
		ResultSet rs = null;
		try{
			rs = database.execute("select * from edgenius_comments where pageUuid=?" , new String[]{str(pageUuid)});
			while(rs.isValidRow()){
				CommentModel model = new CommentModel();
				int idx=0;
				model.uid = rs.getFieldAsInt(idx++);
				//skip spaceUname
				idx++;
				model.pageUuid = rs.getFieldAsString(idx++); 
				model.author = rs.getFieldAsString(idx++); 
				model.modifiedDate = rs.getFieldAsLong(idx++); 
				model.body = rs.getFieldAsString(idx++); 
				model.level = rs.getFieldAsInt(idx++); 
				model.rootUid = rs.getFieldAsInt(idx++); 
				model.parentUid = rs.getFieldAsInt(idx++); 
				model.hide = rs.getFieldAsInt(idx++)>0?true:false;
				list.add(model);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
	
		}
		return list;
	}


	public ArrayList<PageItemModel> getPageHistory(String pageUuid) throws DatabaseException {
		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try{
			rs = database.execute("select * from edgenius_pages where page_uuid=? and isHistory=1 and type=0 order by version desc" 
				, new String[]{str(pageUuid)});
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
	
		}
		return list;
	}



	public ArrayList<PageItemModel> getTagedPages(String spaceUname, String tag, int count){
		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try {
			String sql = "select p.* from edgenius_pages as p, edgenius_tags as t,edgenius_page_tags as pt where " +
			" p.page_uuid=pt.page_uuid and pt.tag_uid=t.uid and t.tag=? and t.unix_name=? and p.isHistory=0 and p.type=0 " +
			" order by p.modifiedDate ";
			if(count > 0)
				sql += " limit " + count;
			
			rs = database.execute(sql, new String[]{str(tag),str(spaceUname)});
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.error("Can not get pages with tag " + tag,e);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}

		return list;
		
	}

	/**
	 * @param spaceUname
	 * @return
	 */
	public List<String> getPageTagsNameList(String spaceUname) {
		List<String> list = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = database.execute("select * from edgenius_tags where unix_name=?", new String[]{str(spaceUname)});
			while(rs.isValidRow()){
				list.add(rs.getFieldAsString(0));
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.error("Can not get page tag list from space " + spaceUname,e);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}

		return list;
	}
	/**
	 * Get all spaces for this user which need do sync. It means the space "sync_date" is before needSyncDate
	 * @param needSyncDate
	 * @return
	 */
	public ArrayList<OfflineModel> getNeedSyncSpaces(long needSyncDate) {
		ArrayList<OfflineModel> ret = new ArrayList<OfflineModel>();
		ResultSet rs = null;
		try {
			rs = database.execute("select unix_name,sync_options,sync_date from edgenius_spaces where sync_date<?"
					, new String[]{str(needSyncDate)});
			while(rs.isValidRow()){
				OfflineModel model = new OfflineModel();
				model.spaceUname = rs.getFieldAsString(0);
				model.options = rs.getFieldAsInt(1);
				model.syncDate = rs.getFieldAsLong(2);
				ret.add(model);
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get need sync spaces " );
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return ret;
	}


	public ArrayList<PageItemModel> getAuthoredPagesInSpace(String spaceUname, String username, int count) {
		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try {
			String sql = "select * from edgenius_pages where " +
			" unix_name=? and creatorUsername=? and isHistory=0 and type=0 " +
			" order by modifiedDate ";
			if(count > 0)
				sql += " limit " + count;
			
			rs = database.execute(sql, new String[]{str(spaceUname),str(username)});
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.error("Can not get user "+username+" authored pages in space " + spaceUname,e);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}

		return list;
	}
	
	public Long getSyncDate(String spaceUname) {
		ResultSet rs = null;
		try {
			rs = database.execute("select sync_date from edgenius_spaces where unix_name=?"
					, new String[]{str(spaceUname)});
			if(rs.isValidRow()){
				return rs.getFieldAsLong(0);
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get space " + spaceUname + " latest touch date");
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		

		return null;
	}
	
	public void saveOptions(String spaceUname, int options) {
		try {
			database.execute("update edgenius_spaces set sync_options=? " + " where unix_name=?", new String[] {
					str( options), 
					// last is pageUuid for WHERE statement
					str(spaceUname) });
		} catch (DatabaseException e) {
			Log.warn("Can not save space " + spaceUname + " options");
		}
	}
	
	public int getOptions(String spaceUname) {
		ResultSet rs = null;
		try {
			rs = database.execute("select sync_options from edgenius_spaces where unix_name=?"
					, new String[]{str(spaceUname)});
			if(rs.isValidRow()){
				int opt = rs.getFieldAsInt(0);
				if(opt == SharedConstants.OPTION_ALL){
					opt = OfflineUtil.getDefaultSyncOptions();
				}
				return opt;
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get space " + spaceUname + " options");
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		

		//return default value
		return  OfflineUtil.getDefaultSyncOptions();


	}
	/**
	 * @param existPage
	 * @throws DatabaseException 
	 * @Deprecated as offline does not support version, this method is useless now
	 */
	public void updateToHistory(PageModel existPage) throws DatabaseException {
		ResultSet rs = null;
		try{
			rs =  database.execute("select uid from edgenius_pages where page_uuid=?  and isHistory=0 and type=0",
				 new String[]{str(existPage.pageUuid)});
			if(rs.isValidRow()){
				int uid = rs.getFieldAsInt(0);
				database.execute("update edgenius_pages set isHistory=1  where uid =?",new String[]{str(uid)});		
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		
	}
	/**
	 * @param unixName
	 * @return
	 */
	public List<String> getPagesUuidInSpace(String spaceUname) {
		
		List<String> uuids = new ArrayList<String>();
		ResultSet rs = null;
		try {
			rs = database.execute("select page_uuid from edgenius_pages where unix_name=?"
					, new String[]{str(spaceUname)});
			while(rs.isValidRow()){
				uuids.add(rs.getFieldAsString(0));
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get pages uuid for space " + spaceUname);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return uuids;
	}

	/**
	 * Delete page by uuid, this may include page histories, drafts etc.
	 * @param uuid
	 */
	public void removePages(String uuid) {
		try {
			database.execute("delete from edgenius_pages where page_uuid=?",new String[]{str(uuid)});
		} catch (DatabaseException e) {
			Log.error("delete page " + uuid + " is failed",e);
		}
	}
	
	/**
	 * Delete draft by uuid and type. 
	 * @param uid
	 */
	public void removeDraft(String uuid, int type) {
		if(type == 0)
			//failure tolerance: if type == 0, this may delete current page and its history
			return;
		
		//delete draft and put it into delete queue
		try {
			database.execute("delete from edgenius_pages where page_uuid=? and type=?",new String[]{str(uuid),str(type)});
			
		} catch (DatabaseException e) {
			Log.error("delete page " + uuid + " with type " + type + " is failed",e);
		}
		
	}

	/**
	 * @param spaceUname
	 * @return
	 */
	public int removeSpace(String spaceUname) {
		int uid  = -1;
		ResultSet rs = null;
		try {
			rs = database.execute("select uid from edgenius_spaces where unix_name=?"
					, new String[]{str(spaceUname)});
			if(rs.isValidRow()){
				uid = rs.getFieldAsInt(0);
				database.execute("delete from edgenius_spaces where uid=?",new String[]{str(uid)});
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get space " + spaceUname);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		
		return uid;
	}

	/**
	 * This method also remove all page comments, attachment from database
	 * @param spaceUname
	 */
	public void removePagesInSpace(String spaceUname) {
		try {
			database.execute("delete from edgenius_pages where unix_name=?",new String[]{str(spaceUname)});
			database.execute("delete from edgenius_attachments where spaceUname=?",new String[]{str(spaceUname)});
			database.execute("delete from edgenius_comments where spaceUname=?",new String[]{str(spaceUname)});
		} catch (DatabaseException e) {
			Log.error("delete pages/attachments/comments from spaces " + spaceUname + " is failed",e);
		}
		
	}
	/**
	 * Is there any draft version for this page from this user? If has return draft page list
	 * @param spaceUname
	 * @param title
	 * @param user
	 * @return
	 */
	public List<PageItemModel> hasDraft(String spaceUname, String title, UserModel user) {
		List<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try {
			rs = database.execute("select uid, modifiedDate, type from edgenius_pages where unix_name=? and title=? and type > 0"
					, new String[]{str(spaceUname),str(title)});
			while(rs.isValidRow()){
				PageItemModel draft = new PageItemModel();
				draft.uid = rs.getFieldAsInt(0);
				draft.modifiedDate = rs.getFieldAsLong(1);
				draft.type = rs.getFieldAsInt(2);
				list.add(draft);
				rs.next();
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get space " + spaceUname + " options");
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		Log.info("page " + title + " has draft " + list.size());
		return list;
	}



	/**
	 * @param spaceUname
	 * @param title
	 * @param type
	 * @return
	 */
	public PageModel getUserDraft(String spaceUname, String title, int type, UserModel user) {
		ResultSet rs = null;
		try {
			rs = database.execute("select * from edgenius_pages where unix_name=? and title=? and type=? and creatorUsername=?"
					, new String[]{str(spaceUname),str(title),str(type),str(user.loginUsername)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get draft " + spaceUname + " with type " + type + " for user" + user);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return null;
	}
	public PageModel getDraft(Integer uid, int draftType) {
		ResultSet rs = null;
		try {
			rs = database.execute("select * from edgenius_pages where uid=? and type=?"
					, new String[]{str(uid),str(draftType)});
			if(rs.isValidRow()){
				return retrievePage(rs);
			}
		} catch (DatabaseException e) {
			Log.warn("Can not get draft " + uid + " with type " + draftType);
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
		}
		return null;
	}


	/**
	 * @return any offline edited pages, includes draft as well
	 * @throws DatabaseException 
	 */
	public ArrayList<PageModel> getOfflineUpdatedPages() throws DatabaseException {
		ArrayList<PageModel> list = new ArrayList<PageModel>();
		ResultSet rs = null;
		try{
			//get current page and all drafts
			rs =  database.execute("select * from edgenius_pages where isHistory=0 and offline_updated="+SharedConstants.OFFLINE_EDITED);
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				//this page will save to server side, so it need send back page version of its last time download  
				//but if version is 1, then means this is new created page
				//!!! this is comment as offline page does not increase page version.
//				if(page.pageVersion > 1 && page.type == 0){
//					page.pageVersion = getPageBaseVersion(page.pageUuid);
//				}
				list.add(page);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}


	/**
	 * @return any offlined updated attachments: removed or new uploaded
	 * @throws DatabaseException 
	 */
	public List<AttachmentModel> getOfflineUpdatedAttachments() throws DatabaseException {
		List<AttachmentModel> list = new ArrayList<AttachmentModel>();
		ResultSet rs = null;
		try{
			//get current page and all drafts
			rs =  database.execute("select * from edgenius_attachments where offline_updated>0");
			while(rs.isValidRow()){
				AttachmentModel att = retrieveAttachment(rs);
				list.add(att);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}
	

	/**
	 * @param spaceUname
	 * @param pageType
	 * @param pageUuid
	 * @return
	 * @throws DatabaseException 
	 */
	public PageThemeModel getPageTheme(String spaceUname, String pageType, String pageUuid) throws DatabaseException {
		PageThemeModel pTheme = new PageThemeModel(); 
		ResultSet rs = null,rs1 = null,rs2 = null;
		try{
			//get current page and all drafts
			rs =  database.execute("select * from edgenius_themes where spaceUname=? and pageType=?", 
					new String[]{str(spaceUname), str(pageUuid)});
			if(rs.isValidRow()){
				pTheme = retrieveTheme(rs);
			}else{
				//if pageUuid is not exist for theme, then try pageType
				rs1 =  database.execute("select * from edgenius_themes where spaceUname=? and  pageType=?", 
						new String[]{str(spaceUname), str(pageType)});
				if(rs1.isValidRow()){
					pTheme = retrieveTheme(rs1);
				}else{
					// if even page type(HOME page) not exist, then try to get default one
					rs2 =  database.execute("select * from edgenius_themes where spaceUname=? and  pageType=?", 
							new String[]{str(spaceUname), str(SharedConstants.THEME_PAGE_SCOPE_DEFAULT)});
					if(rs2.isValidRow()){
						pTheme = retrieveTheme(rs2);
					}
				}
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}
			try{
				if(rs1 != null)rs1.close();
			}catch(Exception e){}
			try{
				if(rs2 != null)rs2.close();
			}catch(Exception e){}

		}
		return pTheme;
	}
	
	public ArrayList<PageItemModel> getPageChildren(String pageUuid) throws DatabaseException {
		ArrayList<PageItemModel> list = new ArrayList<PageItemModel>();
		ResultSet rs = null;
		try{
			rs = database.execute("select * from edgenius_pages where parent_page_uuid=? and isHistory=0 and type=0"
				, new String[]{str(pageUuid)});
			while(rs.isValidRow()){
				PageModel page = retrievePage(rs);
				PageItemModel item = OfflineUtil.extractToItem(page);
				list.add(item);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return list;
	}
	public int getPageChildrenCount(String pageUuid) throws DatabaseException {
		int ret = 0;
		ResultSet rs = null;
		try{
			rs = database.execute("select count(*) from edgenius_pages where parent_page_uuid=? and isHistory=0 and type=0" , 
					new String[]{str(pageUuid)});
			if(rs.isValidRow()){
				ret = rs.getFieldAsInt(0);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return ret;
	}
	/**
	 * Please note, this method does not sort the returned list. Please do it outside this method.
	 * @param spaceUname
	 * @return
	 * @throws DatabaseException
	 */
	public List<PageModel> getPageTree(String spaceUname) throws DatabaseException {
		
		List<PageModel> list = new ArrayList<PageModel>();
		ResultSet rs = null;
		try{
			rs = database.execute("select * from edgenius_pages where unix_name=? and isHistory=0 and type=0" , new String[]{str(spaceUname)});
			while(rs.isValidRow()){
				//TODO - get all content? at least page content is not necessary
				PageModel page = retrievePage(rs);
				page.content = null;
				list.add(page);
				rs.next();
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		
		return list;
	}

	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * Add or delete space from home portal 
	 * @param userUid
	 * @param spaceUname
	 * @param add 
	 */
	private static void updateLayoutWithSpace(Integer userUid, String spaceUname, boolean add) {
		ResultSet rs = null;
		Database defaultDB = null;
		userUid = userUid == null?-1:userUid; 
		try {
			defaultDB = Factory.getInstance().createDatabase();
			defaultDB.open(OfflineConstants.DEFAULT_DB);
			rs = defaultDB.execute("select home_layout from edgenius_users where user_uid=?", new String[]{str(userUid)});
			if (rs.isValidRow()) {
				String layout = rs.getFieldAsString(0);
				if(layout == null)
					layout = "";
				layout = getSpaceLayoutString(layout,spaceUname, add);
				
				if(layout != null){
					defaultDB.execute("update edgenius_users set home_layout=? where user_uid=?",
							new String[]{str(layout),str(userUid)});
					Log.info("Space layout update:" + spaceUname);
				}else{
					Log.info("Space layout does not need update:" + spaceUname);
				}
				
			}
		} catch (DatabaseException e) {
			Log.error("Get user error ", e);
		} finally{
			try {
				if(rs != null)rs.close();
				if(defaultDB != null) defaultDB.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * @param layout
	 * @param add true add or false to delete
	 * @return
	 */
	private static String getSpaceLayoutString(String layout,String spaceUname, boolean add) {
		
		String checkStr = PortletModel.SPACE + SharedConstants.PORTLET_SEP + EscapeUtil.escapeToken(spaceUname) + SharedConstants.PORTLET_SEP;
		int idx = layout.indexOf(checkStr);
		if(add){
			final String layoutStr = PortletModel.SPACE + SharedConstants.PORTLET_SEP + EscapeUtil.escapeToken(spaceUname) 
				+ SharedConstants.PORTLET_SEP + "0" + SharedConstants.PORTLET_SEP  +"0";
			//check if this space exist in or not in current layout
			if(idx == -1){ //not exist, then append layout
				layout += ((layout==null||layout.trim().length() == 0)?"":ITME_SEP)  + layoutStr;
				return layout;
			}else{
				return null;
			}
		}else{
			if(idx != -1){ //exist , then delete from layout
				//remove layout string
				StringBuffer sb = new StringBuffer(layout.substring(0,idx));
				int lastSep = layout.indexOf(ITME_SEP, idx);
				if(lastSep != -1){
					sb.append(layout.substring(lastSep+2));
				}
				String ret = sb.toString();
				if(ret.endsWith(ITME_SEP))
					ret = sb.substring(0, sb.length()-ITME_SEP.length());
				return ret;
			}else{
				return null;
			}
		}
		
	}
	/**
	 * @param pageUuid
	 * @return
	 * @throws DatabaseException 
	 */
	private int getPageBaseVersion(String pageUuid) throws DatabaseException {
		ResultSet rs = null;
		try{
			//history and 
			rs =  database.execute("select max(version) from edgenius_pages " +
					"where isHistory>0 and offline_updated=0 and type=0 and page_uuid=?",
					new String[]{str(pageUuid)});
			if(rs.isValidRow()){
				return rs.getFieldAsInt(0);
			}
		}finally{
			try{
				if(rs != null)rs.close();
			}catch(Exception e){}

		}
		return 1;
	}


	/**
	 * @param pageUuid
	 * @param tag
	 * @throws DatabaseException
	 */
	private void saveOrUpdatePageTag(String spaceUname, String pageUuid, String tag) throws DatabaseException {
		int uid = -1;
		ResultSet rs = null,rs1 = null,rs2 = null;
		try{
			
			//get tag uid, create new tag if it does not exist
			rs = database.execute("select uid from edgenius_tags where tag=? and type=? and unix_name=?", 
						new String[] {str(tag), str(PAGE_TAG_TYPE),str(spaceUname)});

			if (rs != null && rs.isValidRow()) {
				uid = rs.getFieldAsInt(0);
			}else{
				//create new tag 
				database.execute("insert into edgenius_tags(tag,type,unix_name) values(?,?,?)"
							,new String[]{str(tag),str(PAGE_TAG_TYPE),str(spaceUname)});
				rs1 = database.execute("select uid from edgenius_tags where tag=? and  type=? and unix_name=?", 
						new String[] {str(tag), str(PAGE_TAG_TYPE),str(spaceUname)});
				if(rs1.isValidRow())
					uid = rs1.getFieldAsInt(0);
			}
			if(uid == -1){
				Log.error("Unable to get tag " + tag + " uid. Page tag update failed.");
				return;
			}
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// save or update new tag<->page relationship table:try to find page_tag uid, 
			rs2 = database.execute("select uid from edgenius_page_tags where tag_uid=? and page_uuid=?", 
					new String[] {str(uid), str(pageUuid)});
			if(rs2.isValidRow()){
				Log.info("Tag page relation already exist in DB. Do nothing.");
				return;
			}
			database.execute("insert into edgenius_page_tags(tag_uid,page_uuid) values(?,?)",
					new String[] {str(uid), str(pageUuid)});
		}finally{
			try{
				if(rs != null)rs.close();
				if(rs1 != null)rs1.close();
				if(rs2 != null)rs2.close();
			}catch(Exception e){}
		}
	}


	/**
	 * @param rs
	 * @return
	 * @throws DatabaseException
	 */
	private PageModel retrievePage(ResultSet rs) throws DatabaseException {
		PageModel model = new PageModel();
		int idx=0;
		model.uid =  rs.getFieldAsInt(idx);
		model.content = rs.getFieldAsString(++idx);
		model.spaceUname = rs.getFieldAsString(++idx);
		model.title = rs.getFieldAsString(++idx);
		model.pageUuid= rs.getFieldAsString(++idx);
		model.pageVersion = rs.getFieldAsInt(++idx);
		model.parentPageUuid = rs.getFieldAsString(++idx);
		model.type = rs.getFieldAsShort(++idx);
		model.level= rs.getFieldAsInt(++idx);
		model.tagString= rs.getFieldAsString(++idx);
		model.favorite= rs.getFieldAsShort(++idx);
		model.watched= rs.getFieldAsShort(++idx);
		model.creator= rs.getFieldAsString(++idx);
		model.creatorUsername= rs.getFieldAsString(++idx);
		model.creatorPortrait= rs.getFieldAsString(++idx);
		//to keep create data value, it may use in update page to save back to new version page
		model.createDate= rs.getFieldAsLong(++idx);
		model.modifier= rs.getFieldAsString(++idx);
		model.modifierUsername= rs.getFieldAsString(++idx);
		model.modifierPortrait= rs.getFieldAsString(++idx);
		model.modifiedDate= rs.getFieldAsLong(++idx);
		model.isHistory = rs.getFieldAsShort(++idx)>0?true:false;
		model.permissions = parsePerms(rs.getFieldAsString(++idx));
		//skip offlineUpdated
		idx++;
		return model;
		

	}
	/**
	 * @param rs
	 * @return
	 * @throws DatabaseException 
	 */
	private PageThemeModel retrieveTheme(ResultSet rs) throws DatabaseException {
		PageThemeModel model = new PageThemeModel();
		int idx=3; //skip following fields
//		+ " uid INTEGER  PRIMARY KEY AUTOINCREMENT, " 
//		+ " spaceUname text,"
//		+ " pageType text"
		
		model.welcome = rs.getFieldAsString(idx);
		model.bodyMarkup = rs.getFieldAsString(++idx);
		model.sidebarMarkup = rs.getFieldAsString(++idx);
		return model;
	}

	/**
	 * @param rs
	 * @return
	 * @throws DatabaseException
	 */
	private SpaceModel retrieveSpace(ResultSet rs) throws DatabaseException {
		SpaceModel model = new SpaceModel();
		int idx=0;
		//0: uid
		model.uid = rs.getFieldAsInt(idx);
		model.description = rs.getFieldAsString(++idx);
		model.tags = rs.getFieldAsString(++idx);
		model.largeLogoUrl = rs.getFieldAsString(++idx);
		model.smallLogoUrl = rs.getFieldAsString(++idx);
		model.name = rs.getFieldAsString(++idx);
		model.isRemoved = rs.getFieldAsShort(++idx)>0?true:false;
		model.type = rs.getFieldAsShort(++idx);
		model.unixName =rs.getFieldAsString(++idx);
		model.homepageUuid =rs.getFieldAsString(++idx);
		model.permissions = parsePerms(rs.getFieldAsString(++idx));
		return model;
	}
	
	/**
	 * @param rs
	 * @return
	 * @throws DatabaseException
	 */
	private AttachmentModel retrieveAttachment(ResultSet rs) throws DatabaseException {
		AttachmentModel model = new AttachmentModel();
		//0: uid, skipped
		int idx=0;
		model.spaceUname = rs.getFieldAsString(++idx);
		//2: pageUuid
		model.pageUuid = rs.getFieldAsString(++idx);
		model.nodeUuid = rs.getFieldAsString(++idx);
		model.version = rs.getFieldAsString(++idx);
		model.filename = rs.getFieldAsString(++idx);
		model.creator = rs.getFieldAsString(++idx);
		model.date = rs.getFieldAsLong(++idx);
		model.size = rs.getFieldAsLong(++idx);
		model.desc = rs.getFieldAsString(++idx);
		model.status = rs.getFieldAsInt(++idx);
		model.submissionDate = rs.getFieldAsLong(++idx);
		model.submissionRetry = rs.getFieldAsInt(++idx);
		model.offlineEdited = rs.getFieldAsInt(++idx);
		return model;
	}
	/**
	 * @param fieldAsString
	 * @return
	 */
	private int[] parsePerms(String str) {
		if(str == null)
			return new int[20];
		
		int[] ret = new int[20];
		String[] list = str.split(",");
		if(list != null){
			int idx=0;
			for (String intStr:list) {
				if(intStr == null || intStr.trim().length() == 0)
					continue;
				int perm = new Integer(intStr.trim()).intValue();
				ret[idx++]= perm;
			}
		}
		return ret;
	}


	/**
	 * @param userUid
	 * @return 
	 * @throws GearsException
	 */
	private static Database initUserDBInstance(Integer userUid) throws GearsException {
		// Use login user Uid as offline database name: This avoid special
		// character if use username
		String dbName = userUid == null ? (OfflineConstants.DATABASE_NAME_PREFIX + "-1")
				: (OfflineConstants.DATABASE_NAME_PREFIX + userUid);

		Database db = Factory.getInstance().createDatabase();
		db.open(dbName);
		return db;

	}
	private String str(int[] permissions) {
		if(permissions == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (int i : permissions) {
			sb.append(i).append(",");
		}
		return sb.toString();
	}
	private static String str(String str) {
		if (str == null)
			return "";
		return str;
	}

	private static String str(Boolean num) {
		if (num == null)
			return "0";

		return num ? "1" : "0";
	}

	private static String str(Long num) {
		if (num == null)
			return "0";

		return num.toString();
	}


	private static String str(Integer num) {
		if (num == null)
			return "0";

		return num.toString();
	}

	private static String str(Short num) {
		if (num == null)
			return "0";

		return num.toString();
	}

}
