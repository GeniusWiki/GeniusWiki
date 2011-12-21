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
package com.edgenius.wiki.service.impl;

import static com.edgenius.wiki.model.AbstractPage.PAGE_TYPE.DRAFT;
import static com.edgenius.wiki.model.AbstractPage.PAGE_TYPE.HISTORY;
import static com.edgenius.wiki.model.AbstractPage.PAGE_TYPE.PAGE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Hibernate;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.internal.PersistentMap;
import org.hibernate.collection.internal.PersistentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.Version;
import com.edgenius.core.dao.ConfigurationDAO;
import com.edgenius.core.dao.CrFileNodeDAO;
import com.edgenius.core.dao.CrWorkspaceDAO;
import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.dao.ResourceDAO;
import com.edgenius.core.dao.RoleDAO;
import com.edgenius.core.dao.UserDAO;
import com.edgenius.core.dao.hibernate.HibernateUtil;
import com.edgenius.core.model.Configuration;
import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.model.CrWorkspace;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.FileUtilException;
import com.edgenius.core.util.ZipFileUtil;
import com.edgenius.license.InvalidLicenseException;
import com.edgenius.wiki.dao.ActivityLogDAO;
import com.edgenius.wiki.dao.CommentDAO;
import com.edgenius.wiki.dao.DraftDAO;
import com.edgenius.wiki.dao.FriendDAO;
import com.edgenius.wiki.dao.HistoryDAO;
import com.edgenius.wiki.dao.InvitationDAO;
import com.edgenius.wiki.dao.NotificationDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.PageLinkDAO;
import com.edgenius.wiki.dao.PageProgressDAO;
import com.edgenius.wiki.dao.PageTagDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.dao.SpaceTagDAO;
import com.edgenius.wiki.dao.TemplateDAO;
import com.edgenius.wiki.dao.UserPageDAO;
import com.edgenius.wiki.dao.WidgetDAO;
import com.edgenius.wiki.dao.hibernate.JDBCTemplateDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.installation.UpgradeService;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.AbstractPage.PAGE_TYPE;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Draft;
import com.edgenius.wiki.model.DraftContent;
import com.edgenius.wiki.model.Friend;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.HistoryContent;
import com.edgenius.wiki.model.Invitation;
import com.edgenius.wiki.model.Notification;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageLink;
import com.edgenius.wiki.model.PageTag;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.model.Template;
import com.edgenius.wiki.model.UserPageMark;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.quartz.ExportableJob;
import com.edgenius.wiki.quartz.ExportedJob;
import com.edgenius.wiki.service.BackupException;
import com.edgenius.wiki.service.BackupService;
import com.edgenius.wiki.service.DataBinder;
import com.edgenius.wiki.service.DataBinder.ContentBodyMap;
import com.edgenius.wiki.util.CommentComparator;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * @author Dapeng.Ni
 */
public class BackupServiceImpl implements InitializingBean, BackupService {
	public static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);
	private static final String BACKUP_PREFIX = "geniuswiki-backup-";
	private static final String BACKUP_SUFFIX = ".zip";
	private static final String OBJS_BINDER_NAME = "binder.xml";
	private static final String TMP_BACKUP = "_backup";
	private static final String TMP_RESTORE = "_restore";
	private static final String COMMENT_FILE_NAME = "README";
	
	
	private Resource rootLocation;
	private Resource backupLocation;
	private Resource restoreLocation;
	private Resource rssLocation;
	private Resource indexLocation;
	private Resource repositoryLocation;
	private Resource themeLocation;
	private Resource skinLocation;
	private HibernateUtil hibernateUtil;
	private RoleDAO roleDAO;
	private UserDAO userDAO;
	
	private UpgradeService upgradeService;
	
	private ConfigurationDAO configurationDAO;
	
	
	private CrWorkspaceDAO crWorkspaceDAO;
	private CrFileNodeDAO crFileNodeDAO;
	
	
	private FriendDAO friendDAO;
	private InvitationDAO invitationDAO;
	private NotificationDAO notificationDAO;
	
	private TemplateDAO templateDAO;
	private WidgetDAO widgetDAO;
	
	private SpaceDAO spaceDAO;
	//page comment, page link, page progress,
	private PageDAO pageDAO;
	private HistoryDAO historyDAO;
	private DraftDAO draftDAO;
	private PageLinkDAO pageLinkDAO;
	private PageProgressDAO pageProgressDAO;
	private CommentDAO commentDAO;
	private SpaceTagDAO spaceTagDAO;
	private PageTagDAO pageTagDAO;
	private UserPageDAO userPageDAO;
	
	
	private PermissionDAO permissionDAO;
	private ResourceDAO resourceDAO;
	
	@Autowired	private ActivityLogDAO activityLogDAO;
	
	private JDBCTemplateDAO jdbcTemplateDAO;
	
	//quartz job
	private ExportableJob removeSpaceJobInvoker;
	private ExportableJob backupJobInvoker;
	
	private PluginService pluginService; 
	//JDK1.6 @Override
	@Transactional(readOnly=true)
	public String backup(int options, String comment) throws BackupException{

		try {
			log.info("Backup starting.... on options {}", options);
			String dir = FileUtil.createTempDirectory(TMP_BACKUP);
			String zipFileName = getBackupFilename();
			ZipFileUtil.createZipFile(zipFileName, getSources(dir, options, comment),true);;
			
			//clean temp directory - not critical task
			try {
				FileUtil.deleteDir(dir);
			} catch (Exception e) {
				log.warn("Backup tempoaraily directory can not deleted: " + dir);
			}
			log.info("Backup success complete");
			return zipFileName;
		} catch (Exception e) {
			log.error("Backup exception", e);
			throw new BackupException(e);
		} finally{
			
			//!! This method must be executed after XML exported -- lazy loading is done. 
			//exportData() method has side-effect on Hibernate session objects as it changes values of PO. This causes Hibernate query after
			//this method may return wrong value. For example, getUser() may return a user with Null permission.
			//Here will clean hibernate session to clean all POs.
			hibernateUtil.clearSession();
		}

	}
	public String getFileComment(File zipFile){
		String comment = "";
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			ZipEntry entry = zip.getEntry(COMMENT_FILE_NAME);
			if(entry != null){
				comment = IOUtils.toString(zip.getInputStream(entry));
			}
		} catch (Exception e) {
			log.info("backup/restore file comment not available:" + zipFile.getAbsolutePath());
		} finally{
			if(zip != null)
				try {
					zip.close();
				} catch (Exception e) {}
		}
		
		return comment;
	}


	
	//JDK1.6 @Override
	@Transactional(readOnly=false, propagation = Propagation.REQUIRED)
	public void restore(File file) throws BackupException, InvalidLicenseException{
		
		log.info("Restore starting....");
		long time = System.currentTimeMillis();
		FileInputStream zipis = null;
		try {
			//unzip to temporary directory first
			String dir = FileUtil.createTempDirectory(TMP_RESTORE);
			zipis = new FileInputStream(file);
			ZipFileUtil.expandZipToFolder(zipis, dir);
			
			log.info("Restore file unzipped to {}. Took {}s", dir, (System.currentTimeMillis() - time)/1000);
			
			//get databinder, to check options
			File binderFile = new File(FileUtil.getFullPath(dir,OBJS_BINDER_NAME));
			//version check - it may has different fields name etc to cause need do migrate
			int binderVersion = versionCheck(binderFile);
			log.info("Data binder version is {}",binderVersion);
			
			time = System.currentTimeMillis();
			FileInputStream bis = new FileInputStream(binderFile);
			InputStreamReader reader = new InputStreamReader(bis, Constants.UTF8);
			
			XStream xstream = createXStreamInstance();
			DataBinder binder = (DataBinder) xstream.fromXML(reader);
			reader.close();
			bis.close();
			log.info("Parse binder XML took {}s", (System.currentTimeMillis() - time)/1000);
			
			int leftUserCount = licenseCheck(binder);
			
			//As HL Robert's export, Page has list has duplicated: such as page id=93 is enclosed DataObject normally,
			//but, unexpected, same page appear again in page list, this time it is as a referenece object, but this cause same page
			//has duplicate ojbect in return list
			//<com.edgenius.wiki.model.Page reference="93"/>
			//Here is just temporary fix only for HL. But the fix is Hibernate.loadAll() see BaseDAOHibernate.getAll()
			List<Page> pages = (List<Page>) binder.get(Page.class.getName());
			Set<Integer> dup = new HashSet<Integer>();
			for(Iterator<Page> iter = pages.iterator();iter.hasNext();){
				Integer uid = iter.next().getUid();
				if(dup.contains(uid)){
					log.error("There are duplciated pages while import data, UID:" + uid);
					iter.remove();
					continue;
				}
				dup.add(uid);
			}
			
			Map<Integer,String> spaceMap = null;
			if(binderVersion <= 2180){
				//a bug fix - for older 2.18 version as customized theme use spaceUid as key, which changed after import...
				//Since 2.19, customized theme XML is removed, this problem doesn't exist any more. I found this bug when 2.19:(
				List<Space> spaces= (List<Space>) binder.get(Space.class.getName());
				//save old version spaceUname and spaceUid into a map 
				spaceMap = new HashMap<Integer,String>();
				for(Iterator<Space> iter = spaces.iterator();iter.hasNext();){
					Space space = iter.next();
					spaceMap.put(space.getUid(), space.getUnixName());
				}
			}
			int options = binder.getOptions();
			
			if((options & BACKUP_DATA) >0){
				time = System.currentTimeMillis();
				importData(binder, dir, binderVersion);
				log.info("Restore database table took {}s", (System.currentTimeMillis() - time)/1000);
				
				//delete binder file after import success
				if(!binderFile.delete())
					binderFile.deleteOnExit();
			}
			
			time = System.currentTimeMillis();
			if((options & BACKUP_ATTACHMENT) >0){
				FileUtils.deleteDirectory(repositoryLocation.getFile());
				FileUtils.moveDirectory(new File(FileUtil.getFullPath(dir,binder.getDir(BACKUP_ATTACHMENT))), repositoryLocation.getFile());
			}
			if((options & BACKUP_RSS) >0){
				FileUtils.deleteDirectory(rssLocation.getFile());
				FileUtils.moveDirectory(new File(FileUtil.getFullPath(dir,binder.getDir(BACKUP_RSS))), rssLocation.getFile());
			}
			if((options & BACKUP_INDEX) >0){
				FileUtils.deleteDirectory(indexLocation.getFile());
				FileUtils.moveDirectory(new File(FileUtil.getFullPath(dir,binder.getDir(BACKUP_INDEX))), indexLocation.getFile());
			}
			if((options & BACKUP_SKIN) >0){
				FileUtils.deleteDirectory(skinLocation.getFile());
				FileUtils.moveDirectory(new File(FileUtil.getFullPath(dir,binder.getDir(BACKUP_SKIN))), skinLocation.getFile());
			}
			if((options & BACKUP_THEME) >0){
				FileUtils.deleteDirectory(themeLocation.getFile());
				FileUtils.moveDirectory(new File(FileUtil.getFullPath(dir,binder.getDir(BACKUP_THEME))), themeLocation.getFile());
				if(binderVersion <= 2180){
					//rename customized theme to new spaceUid
					File customizedDir = new File(themeLocation.getFile(),"customized");
					File customizedSubDir = new File(themeLocation.getFile(),"customizedTemp");
					
					String[] files = customizedDir.list(FileFilterUtils.suffixFileFilter(".xml"));
					if(files.length > 0){
						customizedSubDir.mkdirs();
					}
					for (String name : files) {
						int uid = NumberUtils.toInt(name.substring(0, name.length()-4),-1);
						if(uid == -1){
							log.info("Unable to get correct space UID from theme file name {}",name);
							continue;
						}
						String uname = spaceMap.get(uid);
						if(uname == null){
							log.warn("Unable to get old spaceUname by UID {}", uid);
							continue;
						}
						
						Space space = spaceDAO.getByUname(uname);
						if(space == null){
							log.warn("Unable to get space by Uname {}", uname);
							continue;
						}
						uid = space.getUid();
						FileUtils.moveFile(new File(customizedDir, name), new File(customizedSubDir, uid+".xml"));
					}
					
					if(customizedSubDir.exists()){
						//replace old by renamed themes
						FileUtils.deleteDirectory(customizedDir);
						FileUtils.moveDirectory(customizedSubDir, customizedDir);
					}
				}
			}
			
			//upgrade data file under DataRoot -- Here assume theme, index, rss etc. All use default name from DataRoot!!!
			try {
				upgradeService.doBackupPackageUpgardeForDataFiles(String.valueOf((float)binderVersion/1000));
			} catch (Exception e) {
				log.error("Unexpected erorr while upgrade backup export package from " + binderVersion + " to " + Version.VERSION,e);
			}
			
			log.info("Restore data root files tooks {}s", (System.currentTimeMillis() - time)/1000);
			try {
				FileUtil.deleteDir(dir);
			} catch (IOException e) {
				log.error("Unable to delete restore temp directory " + dir);
			}
			
			Version.LEFT_USERS = leftUserCount;
			log.info("Restore success complete. Database transaction will submit.");
		} catch (InvalidLicenseException e) {
			log.error("Restore failed",e);
			throw e;
		} catch (Exception e) {
			log.error("Restore failed",e);
			throw new BackupException(e);
		} finally{
			if(zipis != null){
				try {
					zipis.close();
				} catch (Exception e) {}
			}
		}

	}


	@SuppressWarnings("unchecked")
//JDK1.6 @Override
	public List<File> getBackupFileList() {
		try {
			//TODO: need order by modified data?
			Collection list = FileUtils.listFiles(backupLocation.getFile(), FileFilterUtils.suffixFileFilter(".zip"), null);
			if(list != null){
				List<File> sList = new ArrayList<File>(list);
				Collections.sort(sList,new Comparator<File>(){
					public int compare(File o1, File o2) {
						return (o2.lastModified() - o1.lastModified())>0?1:-1;
					}
					
				});
				return sList;
			}else 
				//just avoid nullpoint
				return new ArrayList<File>();
		} catch (IOException e) {
			log.error("Failed retrieve backup directory",e);
			return new ArrayList<File>();
		}
	}

	@SuppressWarnings("unchecked")
//JDK1.6 @Override
	public List<File> getRestoreFileList() {
		try {
			Collection list = FileUtils.listFiles(restoreLocation.getFile(), FileFilterUtils.suffixFileFilter(".zip"), null);
			if(list != null){
				List<File> sList = new ArrayList<File>(list);
				Collections.sort(sList,new Comparator<File>(){
					public int compare(File o1, File o2) {
						return (o2.lastModified() - o1.lastModified())>0?1:-1;
					}
					
				});
				return sList;
			}else 
				//just avoid nullpoint
				return new ArrayList<File>();
		} catch (IOException e) {
			log.error("Failed retrieve restore directory",e);
			return new ArrayList<File>();
		}
	}
	public void moveBackupFileToRestoreList(File srcFile) {
		try {
			FileUtils.moveToDirectory(srcFile, restoreLocation.getFile(), false);
		} catch (IOException e) {
			log.error("Failed move backup file to restore directory",e);
		}
	}

	public void addFileToRestoreList(File restoreFile, String filename) {
		try {
			FileUtils.copyFile(restoreFile, new File(restoreLocation.getFile(), filename));
			
			log.info("Copy file " + restoreFile.getAbsolutePath() + " to restore directory");
		} catch (IOException e) {
			log.error("Unable to put file " + restoreFile.getAbsolutePath() + " to restore directory",e);
		}
		
	}

	//JDK1.6 @Override
	public void afterPropertiesSet() throws Exception {
		if(backupLocation == null)
			throw new BeanInitializationException("Must declare backup location property.");
		
		if(backupLocation.exists() && !backupLocation.getFile().isDirectory())
			throw new BeanInitializationException("Backup location must be directory.");
		
		if(!backupLocation.exists() && !backupLocation.getFile().mkdirs()){
			throw new BeanInitializationException("Failed creating backup location.");
		}
		
		if(restoreLocation == null)
			throw new BeanInitializationException("Must declare restore location property.");
		
		if(restoreLocation.exists() && !restoreLocation.getFile().isDirectory())
			throw new BeanInitializationException("Restore location must be directory.");
		
		if(!restoreLocation.exists() && !restoreLocation.getFile().mkdirs()){
			throw new BeanInitializationException("Failed creating restore location.");
		}
		
		if(rootLocation == null)
			throw new BeanInitializationException("Must declare data root location property.");
			
	}
	
	//********************************************************************
	//               private 
	//********************************************************************

	/**
	 * @param binderFile
	 */
	private int versionCheck(File binderFile) {
		//it does not worth to use any XML technique to get version number...
		String verStr = null;
		LineIterator iter = null;
		try {
			for(iter = FileUtils.lineIterator(binderFile);iter.hasNext();){
				String line = iter.nextLine();
				if(verStr != null){
					int eIdx = line.indexOf("</version>");
					if(eIdx != -1){
						verStr += line.substring(0,eIdx);
						break;
					}else{
						AuditLogger.error("Version in binder can not find close tag in next line, failed~");
					}
					//I don't bear version tag in more than 2 lines!
					break;
				}
				
				int sIdx = line.indexOf("<version>");
				if(sIdx != -1){
					int eIdx = line.indexOf("</version>");
					if(eIdx != -1){
						verStr = line.substring(sIdx+ "<version>".length(),eIdx);
						break;
					}else{
						verStr = line.substring(sIdx+ "<version>".length());
						AuditLogger.error("Version in binder even not insdie same line!?");
					}
				}

			}
			
		} catch (IOException e) {
			log.error("Unable to read binder file to get version ", e);
		} finally{
			if(iter != null)
				iter.close();
		}
		
		if(verStr == null || NumberUtils.toFloat(verStr.trim(),-1f) == -1f){
			log.error("version parse failed.");
			return 0;
		}
		
		//upgrade binder file
		try {
			upgradeService.doBackupPackageUpgardeForBinder(verStr.trim(), binderFile);
		} catch (Exception e) {
			log.error("Unexpected erorr while upgrade backup binder file from " + verStr.trim() + " to " + Version.VERSION, e);
		}
		
		return (int) (NumberUtils.toFloat(verStr.trim(), 0) * 1000);
	}

	/**
	 * @return
	 */
	private XStream createXStreamInstance() {
		XStream xstream = new XStream();
		xstream.registerConverter(new HibernatePersistentSetConverter(xstream.getMapper()));
		xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
		xstream.registerConverter(new HibernatePersistentListConverter(xstream.getMapper()));
		xstream.registerConverter(new HibernatePersistentBagConverter(xstream.getMapper()));
		xstream.setMode(XStream.ID_REFERENCES);
		return xstream;
	}
	/**
	 * @return
	 * @throws IOException 
	 */
	private String getBackupFilename() throws IOException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String stamp = format.format(new Date());
		String mid = stamp;
		int failRetry = 500,retry=0;
		String name = null;
		do{
			name = FileUtil.getFullPath(backupLocation.getFile().getCanonicalPath(), BACKUP_PREFIX+mid+BACKUP_SUFFIX);
			retry++;
			mid = stamp + "-"+retry;
		}while(new File(name).exists() && retry < failRetry);
		
		return new File(name).getCanonicalPath();
	}
	
	
	private Map<File, String> getSources(String dir, int options, String comment) throws IOException, FileUtilException, ParserConfigurationException, XmlPullParserException{
		
		String binderName = FileUtil.getFullPath(dir, OBJS_BINDER_NAME);
		
		DataBinder binder = new DataBinder();
		binder.setVersion(Version.VERSION);
		binder.setOptions(options);
		
		long start = System.currentTimeMillis();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// backup database
		Map<File, String> list = new HashMap<File, String>();
		if((options & BACKUP_DATA) > 0){
			exportData(binder, list);
		}
		
		log.info("Backup successfully export data from database in {}ms", (System.currentTimeMillis() - start) );
		start = System.currentTimeMillis();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// backup data directory
		String root = rootLocation.getFile().getCanonicalPath();
		if(!root.endsWith(File.separator))
			root += File.separator;
		
		int rootLen = root.length();
		if((options & BACKUP_ATTACHMENT) > 0){
			String path = repositoryLocation.getFile().getCanonicalPath();
			if(path.length() > rootLen){
				list.put(repositoryLocation.getFile(),root);
				binder.addDir(BACKUP_ATTACHMENT, createRelativeDir(path, root));
			}else{
				AuditLogger.warn("Repository location is not inside root:" + path + "; Root is " + root);
			}
		}
		if((options & BACKUP_SKIN) > 0){
			String path = skinLocation.getFile().getCanonicalPath();
			if(path.length() > rootLen){
				list.put(skinLocation.getFile(),root);
				binder.addDir(BACKUP_SKIN, createRelativeDir(path, root));
			}else{
				AuditLogger.warn("Skin location is not inside root:" + path + "; Root is " + root);
			}
		}
		if((options & BACKUP_THEME) > 0){
			String path = themeLocation.getFile().getCanonicalPath();
			if(path.length() > rootLen){
				list.put(themeLocation.getFile(),root);
				binder.addDir(BACKUP_THEME, createRelativeDir(path, root));
			}else{
				AuditLogger.warn("Theme location is not inside root:" + path + "; Root is " + root);
			}
		}
		if((options & BACKUP_INDEX) > 0){
			String path = indexLocation.getFile().getCanonicalPath();
			if(path.length() > rootLen){
				list.put(indexLocation.getFile(),root);
				binder.addDir(BACKUP_INDEX, createRelativeDir(path, root));
			}else{
				AuditLogger.warn("Index location is not inside root:" + path + "; Root is " + root);
			}

		}
		if((options & BACKUP_RSS) > 0){
			String path = rssLocation.getFile().getCanonicalPath();
			if(path.length() > rootLen){
				list.put(rssLocation.getFile(),root);
				binder.addDir(BACKUP_RSS, createRelativeDir(path, root));
			}else{
				AuditLogger.warn("RSS location is not inside root:" + path + "; Root is " + root);
			}
		}
		
		if((options & BACKUP_CONF) > 0){
			list.put(new File(FileUtil.getFullPath(root,Global.FILE)),root);
			list.put(new File(FileUtil.getFullPath(root,Server.FILE)),root);
			list.put(new File(FileUtil.getFullPath(root,Installation.FILE)),root);
		}
		
		log.info("Databinder successed adds external files info in {}ms", (System.currentTimeMillis() - start) );
		start = System.currentTimeMillis();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Save Data binder XML
		//output to XML file - rather than directly using String as next SAXParser InputSource() as this way save memory!
		
		String binderTmp = FileUtil.getFullPath(dir, OBJS_BINDER_NAME+"_tmp");
		log.info("Databinder is going to export to file {} ", binderTmp);
		
		OutputStream os = FileUtil.getFileOutputStream(binderTmp);
		Writer writer = new OutputStreamWriter(os, Constants.UTF8);
		XStream xstream = createXStreamInstance();
		xstream.toXML(binder,writer);
		
		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(os);
		
		log.info("Databinder export to {} successfully in {}ms. Next will do proxy object removing", binderTmp, (System.currentTimeMillis() - start) );
		start = System.currentTimeMillis();
		
		//TODO: above XML has resolves-to attribute, following code will remove them, this part code may need remove 
		//if XStream supports suppress "resolves-to"
		//see: http://old.nabble.com/resolves-to-is-bad-idea--td20733702.html
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// remove "resolve-to" attribute to real class tag, discard useless proxy class name
		os = FileUtil.getFileOutputStream(binderName);
		writer = new OutputStreamWriter(os, Constants.UTF8);
		FileReader reader = new FileReader(binderTmp);

		//Don't use SAX parser as it can not process ASCII control characters, such as "&#x3;"(text-end). 
		//See: http://xstream.codehaus.org/faq.html#XML_control_char
		
		ResolveRemoveHandler handler = this.new ResolveRemoveHandler(writer);
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(false);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput(reader);
		int eventType = xpp.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				handler.startElement(xpp);
			} else if (eventType == XmlPullParser.END_TAG) {
				handler.endElement(xpp.getName());
			} else if (eventType == XmlPullParser.TEXT) {
				handler.characters(xpp.getText());
			}
			eventType = xpp.next();
		}
		
		
//		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
//		XMLReader reader = parser.getXMLReader();
//		reader.setContentHandler(this.new ResolveRemoveHandler(writer));
//		reader.parse(new InputSource(fis));

		//close
		IOUtils.closeQuietly(reader);
		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(os);
		log.info("Databinder XML polished in {}ms", (System.currentTimeMillis() - start) );
		start = System.currentTimeMillis();
		
		//this will make c:\\doc~1\\ expand to c:\\my document\\...
		String canonicalPath = new File(dir).getCanonicalPath();
		if(!canonicalPath.endsWith(File.separator))
			canonicalPath += File.separator;
				
				
		File tmp = new File(binderTmp);
		if(!tmp.delete())
			tmp.deleteOnExit();
		
		//binder XML always put into root directory in backup zip
		list.put(new File(binderName),canonicalPath);

		//Comment file
		if(!StringUtils.isBlank(comment)){
			File commentFile= new File(FileUtil.getFullPath(dir, COMMENT_FILE_NAME));
			FileUtils.writeStringToFile(commentFile, comment);
			list.put(commentFile,canonicalPath);
		}
		
		log.info("System starting zip files...");
		return list;
	}

	private static String createRelativeDir(String canonicalPath, String parentPath) {
		if(parentPath == null)
			return canonicalPath;
		
		int len = parentPath.length();
		return canonicalPath.substring(len);
	}
	@Transactional(readOnly=true, propagation=Propagation.REQUIRED)
	private void exportData(DataBinder binder, Map<File, String> zipFileList) throws IOException, FileUtilException {
		//I don't use clone, as it has side effect: XStream will use ID to reference to same object, this save lots size on XML
		//for example, if user A popup in top all XML with ID 123, then all refer same user object, XStream will use line to refer to ID 123. 
		//If clone(), the all object won't be same (not equals(), here is object equals) and, all refer this user will expand 
		//to entire user information XML. 

		//Hibernate javassist API generates different Proxy class name in some unknown reason(I don't investigate) It causes ClassNotFound exception.
		// <content class="com.edgenius.wiki.model.HistoryContent_$$_javassist_4" id="1677" resolves-to="com.edgenius.wiki.model.HistoryContent">
		//I got an unknown reason issue: above _$$_javassist_4 could be explain in geniuswiki 1.1 version. But XStream say class not found 
		//exception while in geniuswiki 1.2 version.  It also looks not serializable problem as it looks I recompile 1.1, it also still only know 
		//javassist_4.  In 1.2, it generate something like javasisst_17 etc...  I don't know what is reason and no more time to investigate...
		// But anyway, delete ugly _$$_javassist_4 in XML file is extra benefit...
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// conf
		List<Configuration> confs = configurationDAO.getObjects();
		binder.addAll(Configuration.class.getName(), confs);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// user / role / user following
		List<User> users = userDAO.getObjects();
		//username:List<following username>
		Map<String,List<String>> followingMap = new HashMap<String, List<String>>();
		for (User user : users) {
			//although user clone is not quite necessary for proxy, but it may cause in this OpenSessionInView, user get null value of permission.
			user.setRoles(null);
			user.setPermissions(null);
			List<User> followings = user.getFollowings();
			if(followings != null && followings.size() > 0){
				List<String> fList = new ArrayList<String>();
				for (User following : followings) {
					fList.add(following.getUsername());
				}
				followingMap.put(user.getUsername(),fList);
			}
			//backup separately
			user.setFollowers(null);
			user.setFollowings(null);
		}
		binder.addAll(User.class.getName(), users);
		//bind user following into separated bind map
		binder.add(DataBinder.USER_FOLLOWING_BINDER_NAME, followingMap);
		
		
		List<Role> roles = roleDAO.getObjects();
		for (Role role : roles) {
			role.setPermissions(null);
			Hibernate.initialize(role.getUsers());
		}
		binder.addAll(Role.class.getName(),roles);
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// workspace /nodes
		List<CrWorkspace> ws = crWorkspaceDAO.getObjects();
		binder.addAll(CrWorkspace.class.getName(),ws);
		
		List<CrFileNode> fnodes = crFileNodeDAO.getObjects();
		binder.addAll(CrFileNode.class.getName(), fnodes);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// widgets, friends, invitation, notification, responses
		List<Widget> widgets = widgetDAO.getObjects();
		binder.addAll(Widget.class.getName(), widgets);
		
		List<Friend> frds = friendDAO.getObjects();
		binder.addAll(Friend.class.getName(),frds);
		
		List<Invitation> ivs = invitationDAO.getObjects();
		binder.addAll(Invitation.class.getName(),ivs);
		
		List<Notification> notfs = notificationDAO.getObjects();
		binder.addAll(Notification.class.getName(), notfs);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space, pages, links, progress, comments, tags
		Map<String,String> homeMap = new HashMap<String, String>();
		
		List<Space> spaces = spaceDAO.getObjects();
		for (Space space : spaces) {
			//here, space may bring home page embedded. 
			if(space.getHomepage() != null){
				homeMap.put(space.getUnixName(), space.getHomepage().getPageUuid());
			}
			space.setHomepage(null);
			space.setTags(null);
		}
		binder.addAll(Space.class.getName(),spaces);
		
		ContentBodyMap contentBodyMap = ContentBodyMap.initialForBackup();
		List<Page> sortedPages = new ArrayList<Page>();
		List<Page> pages = pageDAO.getObjects();
		for (Page page : pages) {
			//its is better put parent page before its child, 
			//but also need avoid infinite looping, e.g, A parent is B, B parent is A, although this is unexpected, but need take care 
			if(page.getParent() != null){
				List<Page> parents = new ArrayList<Page>();
				processParentPage(sortedPages, page, parents, contentBodyMap);
			}
			initPage(page);
			pushContent(page, contentBodyMap);
			sortedPages.add(page);
		}
		binder.addAll(Page.class.getName(),sortedPages);
		
		//put draft/history after page, as their parent is from page
		List<History> histories = historyDAO.getObjects();
		for (History history : histories) {
			Hibernate.initialize(history.getContent());
			pushContent(history, contentBodyMap);
		}
		binder.addAll(History.class.getName(),histories);
		
		List<Draft> drafts= draftDAO.getObjects();
		for (Draft draft : drafts) {
			Hibernate.initialize(draft.getPageProgress());
			Hibernate.initialize(draft.getContent());
			pushContent(draft, contentBodyMap);
		}
		binder.addAll(Draft.class.getName(),drafts);
		
		//put space/homepage relation after page and space
		binder.add(DataBinder.HOME_PAGE_BINDER_NAME,homeMap);
		
		//after PAGE, DRAFT, HISTORY complete, finialise the ContentBody map and put all contentbody file into zip list
		contentBodyMap.finalise();
		zipFileList.putAll(contentBodyMap.getZipMap());
		
		List<PageComment> sortedComments = CommentComparator.getParentBeforeSortedList(commentDAO.getObjects());
		binder.addAll(PageComment.class.getName(), sortedComments);
		
		
		List<SpaceTag> stags = spaceTagDAO.getObjects();
		binder.addAll(SpaceTag.class.getName(),stags);
		
		List<PageTag> ptags = pageTagDAO.getObjects();
		binder.addAll(PageTag.class.getName(),ptags);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// user favourite, watched, templates
		List<UserPageMark> ups = userPageDAO.getObjects();
		binder.addAll(UserPageMark.class.getName(),ups);

		List<Template> templs = templateDAO.getObjects();
		binder.addAll(Template.class.getName(), templs);
		

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// resource, permission
		List<Permission> perms = permissionDAO.getObjects();
		for (Permission perm : perms) {
			perm.setResource(null);
		}
		binder.addAll(Permission.class.getName(), perms);
		
		List<com.edgenius.core.model.Resource> resrcs = resourceDAO.getObjects();
		for (com.edgenius.core.model.Resource res : resrcs) {
			Hibernate.initialize(res.getPermissions());
		}
		binder.addAll(com.edgenius.core.model.Resource.class.getName(), resrcs);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// QUARTZ jobs
		ExportedJob rsJob = removeSpaceJobInvoker.exportJob();
		if(rsJob != null)
			binder.addAll(rsJob.getJobType(), rsJob.getJobs());
		
		ExportedJob bkJob = backupJobInvoker.exportJob();
		if(bkJob != null)
			binder.addAll(bkJob.getJobType(), bkJob.getJobs());
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//activityLog
		List<ActivityLog> al = activityLogDAO.getObjects();
		binder.addAll(ActivityLog.class.getName(),al);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// plugin services
		pluginService.backup(binder);
		
		log.info("Export data success with object {} types ", binder.getObjectsSize());
		
	}
	
	/**
	 * @param page
	 * @param contentBodys 
	 * @param contentBodyMap 
	 * @throws IOException 
	 */
	private void pushContent(AbstractPage page, ContentBodyMap bodyMap) throws IOException {
		int contentId = 0;
		PAGE_TYPE type = null;
		String body = null;
		if(page instanceof Page){
			type = PAGE;
			PageContent pcontent = ((Page) page).getContent();
			contentId =pcontent.getUid();
			body = pcontent.getContent();
			pcontent.setContent(null);
		}else if(page instanceof Draft){
			type = DRAFT;
			DraftContent dcontent = ((Draft) page).getContent();
			contentId = dcontent.getUid();
			body = dcontent.getContent();
			dcontent.setContent(null);
		}else if(page instanceof History){
			type = HISTORY;
			HistoryContent hcontent = ((History) page).getContent();
			contentId = hcontent.getUid();
			body = hcontent.getContent();
			hcontent.setContent(null);
		}
		
		bodyMap.add(contentId, type, body);
	}
	
	/**
	 * @param page
	 * @param parent
	 * @param parents
	 * @param contentBodyMap 
	 * @throws IOException 
	 */
	private void processParentPage(List<Page> container, Page page, List<Page> parents, ContentBodyMap contentBodyMap) throws IOException {
		Page parent = page.getParent();
		if(parent != null){
			if(parents.indexOf(parent) == -1){
				if(container.indexOf(parent) == -1){
					//this parent is not put into container yet
					parents.add(parent);
					processParentPage(container, parent, parents, contentBodyMap);
					
					initPage(parent);
					pushContent(page, contentBodyMap);
					container.add(parent);
				}
			}else{
				//there are infinite looping in parents
				AuditLogger.error("There are infinit parents looping:" + Arrays.toString(parents.toArray()) + "; Page parent is set to null" + page);
				//just make this page's parent as null and stop looping...
				page.setParent(null);
			}
		}
		//top of page tree: parent == null
	}

	/**
	 * @param page
	 */
	private void initPage(Page page) {
		Hibernate.initialize(page.getPageProgress());
		Hibernate.initialize(page.getLinks());
		Hibernate.initialize(page.getContent());
		page.setTags(null);
	}
	@Transactional(readOnly=false, propagation=Propagation.REQUIRES_NEW)
	private void cleanDatabase(){
		
		//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		//!!!!!! DON'T disturb run sequence as Database relationship dependencies!!!! 
		pluginService.resorePreClean();
		
		jdbcTemplateDAO.cleanTableRelations();
		
		permissionDAO.cleanTable();
		resourceDAO.cleanTable();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// page relative
		commentDAO.cleanTable();
		pageLinkDAO.cleanTable();
		userPageDAO.cleanTable();
		pageDAO.cleanTable();
		historyDAO.cleanTable();
		draftDAO.cleanTable();
		pageTagDAO.cleanTable();
		pageProgressDAO.cleanTable();
		jdbcTemplateDAO.cleanNonDAOTables();

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space relative
		templateDAO.cleanTable();
		spaceTagDAO.cleanTable();
		spaceDAO.cleanTable();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// others
		friendDAO.cleanTable();
		invitationDAO.cleanTable();
		notificationDAO.cleanTable();
		widgetDAO.cleanTable();
		crFileNodeDAO.cleanTable();
		crWorkspaceDAO.cleanTable();
		widgetDAO.cleanTable();
		activityLogDAO.cleanTable();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// user, role
		roleDAO.cleanTable();
		userDAO.cleanTable();
		
		configurationDAO.cleanTable();
		
	}

	private int licenseCheck(DataBinder binder) throws InvalidLicenseException  {
		List<User> users = (List<User>) binder.get(User.class.getName());
		if(Version.USER_LIMITED != -1){
			if(users.size() > Version.USER_LIMITED)
				throw new InvalidLicenseException("Restore user amount exceeds license limitation.");
			
			return Version.USER_LIMITED - users.size();
		}
		
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	private void importData(DataBinder binder, String rootDir, int binderVersion) throws IOException {
		
		log.info("Cleaning database....");
		cleanDatabase();
		log.info("Database cleaned and starting restore data.");
		
		//clean all noise for this clean import
		hibernateUtil.clearSession();
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// conf
		List<Configuration> confs= (List<Configuration>) binder.get(Configuration.class.getName());
		for (Configuration conf: confs) {
			conf.setUid(null);
			configurationDAO.saveOrUpdate(conf);
		}

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// user, role, resource, permission
		List<User> users = (List<User>) binder.get(User.class.getName());
		for (User user: users) {
			user.setUid(null);
			
			//as index is immediately execute after import. hibernate still get role from cache
			//it may aovid nullpointException in 
			//UserDAOHibnerate.refreshInstancePermissionCache()=>role.getPermissions()=>getInstancePermission(wikiPermissions, perms);
			if(user.getPermissions()  == null)
				user.setPermissions(new HashSet<Permission>());
			
			userDAO.saveOrUpdate(user);
		}
		//restore user network until all users created
		List<Map<String, List<String>>> flist = (List<Map<String, List<String>>>) binder.get(DataBinder.USER_FOLLOWING_BINDER_NAME);
		Map<String, List<String>> followingMap = (flist == null || flist.size() == 0 )?null:flist.get(0);
		if(followingMap != null){
			User user;
			for(Entry<String,List<String>> entry: followingMap.entrySet()){
				user = userDAO.getUserByName(entry.getKey());
				if(user != null){
					List<User> followings = new ArrayList<User>();
					user.setFollowings(followings);
					
					for(String followingName :entry.getValue()){
						User following = userDAO.getUserByName(followingName);
						if(following != null){
							followings.add(following);
						}else{
							log.warn("Unable to find user {} for user {} following", followingName, entry.getKey());
						}
					}
					
					userDAO.saveOrUpdate(user);
				}else{
					log.warn("Unable to restore user network data for user {}", entry.getKey());
				}
			}
		}
		List<Role> roles = (List<Role>) binder.get(Role.class.getName());
		for (Role role: roles) {
			role.setUid(null);
			if(role.getUsers() != null){
				for (User user : role.getUsers()) {
					Set<Role> ur = user.getRoles();
					if(ur == null){
						ur = new HashSet<Role>();
						user.setRoles(ur);
					}
					ur.add(role);
				}
			}
			
			//as index is immediately execute after import. hibernate still get role from cache
			//it may aovid nullpointException in 
			//UserDAOHibnerate.refreshInstancePermissionCache()=>role.getPermissions()=>getInstancePermission(wikiPermissions, perms);
			if(role.getPermissions()  == null)
				role.setPermissions(new HashSet<Permission>());
			
			roleDAO.saveOrUpdate(role);
		}
		
		List<Permission> perms = (List<Permission>) binder.get(Permission.class.getName());
		for (Permission perm: perms) {
			perm.setUid(null);
			if(perm.getUsers() != null){
				for (User user : perm.getUsers()) {
					//put this permission to user permission list
					Set<Permission> pms = user.getPermissions();
					if(pms == null){
						pms = new HashSet<Permission>();
						user.setPermissions(pms);
					}
					pms.add(perm);
				}
			}
			if(perm.getRoles() != null){
				for (Role role : perm.getRoles()) {
					//put this permission to role permission list
					Set<Permission> pms = role.getPermissions();
					if(pms == null){
						pms = new HashSet<Permission>();
						role.setPermissions(pms);
					}
					pms.add(perm);
				}
			}
//			resourceDAO will cascade save permissions
//			permissionDAO.saveOrUpdate(perm);
		}
		
		List<com.edgenius.core.model.Resource> resrcs = (List<com.edgenius.core.model.Resource>) binder.get(com.edgenius.core.model.Resource.class.getName());
		for (com.edgenius.core.model.Resource res: resrcs) {
			res.setUid(null);
			if(res.getPermissions() != null){
				for(Permission perm : res.getPermissions()){
					perm.setResource(res);
				}
			}
			resourceDAO.saveOrUpdate(res);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// widgets
		List<Widget> widgets = (List<Widget>) binder.get(Widget.class.getName());
		for (Widget widget: widgets) {
			widget.setUid(null);
			widgetDAO.saveOrUpdate(widget);
		}
		
		List<Friend> frds = (List<Friend>) binder.get(Friend.class.getName());
		for (Friend frd: frds) {
			frd.setUid(null);
			friendDAO.saveOrUpdate(frd);
		}
			
		List<Invitation> ivs = (List<Invitation>) binder.get(Invitation.class.getName());
		for (Invitation iv: ivs) {
			iv.setUid(null);
			invitationDAO.saveOrUpdate(iv);
		}
		
		List<Notification> notfs = (List<Notification>) binder.get(Notification.class.getName());
		for (Notification notf: notfs) {
			notf.setUid(null);
			notificationDAO.saveOrUpdate(notf);
		}

		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// workspace /nodes
		List<CrWorkspace> ws = (List<CrWorkspace>) binder.get(CrWorkspace.class.getName());
		for (CrWorkspace w: ws) {
			w.setUid(null);
			crWorkspaceDAO.saveOrUpdate(w);
		}
		List<CrFileNode> fnodes = (List<CrFileNode>) binder.get(CrFileNode.class.getName());
		for (CrFileNode node: fnodes) {
			node.setUid(null);
			crFileNodeDAO.saveOrUpdate(node);
		}
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// space, pages, links, progress, comments, tags
		List<Map<String, String>> hlist = (List<Map<String,String>>) binder.get(DataBinder.HOME_PAGE_BINDER_NAME);
		Map<String,String> homeMap = (hlist == null || hlist.size() == 0 )?null:hlist.get(0);
		Collection<String> homepageUuids = homeMap != null? homeMap.values():null;
		Collection<String> spaceUnames = homeMap != null? homeMap.keySet():null;
		Map<String,Space> spaceMap = new HashMap<String, Space>();
		Map<String,Page> pageMap = new HashMap<String, Page>();

		List<Space> spaces = (List<Space>) binder.get(Space.class.getName());
		for (Space space: spaces) {
			space.setUid(null);
			//build space map, so that save performance while build home page relations
			if(spaceUnames != null){
				if(SharedConstants.SYSTEM_SPACEUNAME.equalsIgnoreCase(space.getUnixName())){
					//instance space(where UNIX_NAME='$SYSTEM$') won't be remove during clean space table 
					//reason is this record is verify object in DBCP connection pool validationQuery property. 
					//If removed, DBCP will broken and DB connect can not get successfully.   
					//so here will get it back from current DB and put it back in following
					Space sysSpace = spaceDAO.getByUname(SharedConstants.SYSTEM_SPACEUNAME);
					//this is just in case if system space is also delete in some case...
					if(sysSpace != null)
						space = sysSpace;
				}
				if(spaceUnames.contains(space.getUnixName())){
					spaceMap.put(space.getUnixName(), space);
				}
			}
			spaceDAO.saveOrUpdate(space);
		}

		
		ContentBodyMap contentBodyMap = ContentBodyMap.initialForRestore(rootDir);
		List<Page> pages = (List<Page>) binder.get(Page.class.getName());
		for (Page page: pages) {
			page.setUid(null);
			if(page.getContent() != null){
				//load from contentBodys file - after VERSION 3.0
				if(binderVersion > 3000){
					page.getContent().setContent(contentBodyMap.getContent(page.getContent().getUid(), PAGE));
				}
				page.getContent().setUid(null);
			}
			if(page.getPageProgress() != null){
				page.getPageProgress().setUid(null);
				pageProgressDAO.saveOrUpdate(page.getPageProgress());
			}
			if(page.getLinks() != null){
				for (PageLink link : page.getLinks()) {
					link.setUid(null);
				}
			}
			//build space map, so that save performance while build home page relations
			if(homepageUuids != null){
				if(homepageUuids.contains(page.getPageUuid())){
					pageMap.put(page.getPageUuid(), page);
				}
			}
			pageDAO.saveOrUpdate(page);
		}
		
		//must after page saved as page_parent_uid need from Page table
		List<History> histories = (List<History>) binder.get(History.class.getName());
		for (History history: histories) {
			history.setUid(null);
			if(history.getContent() != null){
				if(binderVersion > 3000){
					history.getContent().setContent(contentBodyMap.getContent(history.getContent().getUid(), HISTORY));
				}
				history.getContent().setUid(null);
			}
			historyDAO.saveOrUpdate(history);
		}
	
		List<Draft> drafts = (List<Draft>) binder.get(Draft.class.getName());
		for (Draft draft: drafts) {
			draft.setUid(null);
			if(draft.getContent() != null){
				if(binderVersion > 3000){
					draft.getContent().setContent(contentBodyMap.getContent(draft.getContent().getUid(), DRAFT));
				}
				draft.getContent().setUid(null);
			}
			if(draft.getPageProgress() != null){
				draft.getPageProgress().setUid(null);
				pageProgressDAO.saveOrUpdate(draft.getPageProgress());
			}			
			draftDAO.saveOrUpdate(draft);
		}
		
		
		//put space/homepage relation after page and space
//		binder.add(DataBinder.HOME_PAGE_BINDER_NAME,homeMap);
		
		List<PageComment> comments = (List<PageComment>) binder.get(PageComment.class.getName());
		for (PageComment comment: comments) {
			comment.setUid(null);
			commentDAO.saveOrUpdate(comment);
		}
		
		
		List<SpaceTag> stags = (List<SpaceTag>) binder.get(SpaceTag.class.getName());
		for (SpaceTag tag: stags) {
			tag.setUid(null);
			if(tag.getSpaces() != null){
				for(Space space : tag.getSpaces()){
					List<SpaceTag> st = space.getTags();
					if(st == null){
						st = new ArrayList<SpaceTag>();
						space.setTags(st);
					}
					st.add(tag);
				}
			}
			spaceTagDAO.saveOrUpdate(tag);
		}
		
		List<PageTag> ptags = (List<PageTag>) binder.get(PageTag.class.getName());
		for (PageTag tag: ptags) {
			tag.setUid(null);
			if(tag.getPages() != null){
				for(Page page: tag.getPages()){
					List<PageTag> st = page.getTags();
					if(st == null){
						st = new ArrayList<PageTag>();
						page.setTags(st);
					}
					st.add(tag);
				}
			}
			pageTagDAO.saveOrUpdate(tag);
		}
		
		for(Entry<String,String> entry:homeMap.entrySet()){
			Space space = spaceMap.get(entry.getKey());
			Page page = pageMap.get(entry.getValue());
			if(space != null && page != null){
				space.setHomepage(page);
				spaceDAO.saveOrUpdate(space);
			}
		}
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// user favourite, watched, templates
		List<UserPageMark> ups = (List<UserPageMark>) binder.get(UserPageMark.class.getName());
		for (UserPageMark up: ups) {
			up.setUid(null);
			userPageDAO.saveOrUpdate(up);
		}
		List<Template> templs = (List<Template>) binder.get(Template.class.getName());
		for (Template templ: templs) {
			templ.setUid(null);
			templateDAO.saveOrUpdate(templ);
		}

		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// quartz jobs
		List<Map<String,Object>> rsJob = (List<Map<String, Object>>) binder.get(removeSpaceJobInvoker.getClass().getName());
		if(rsJob != null){
			removeSpaceJobInvoker.importJob(rsJob);
		}
		
		List<Map<String,Object>> bkJob = (List<Map<String, Object>>) binder.get(backupJobInvoker.getClass().getName());
		if(bkJob != null){
			backupJobInvoker.importJob(bkJob);
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//activityLog
		List<ActivityLog> al = (List<ActivityLog>) binder.get(ActivityLog.class.getName());
		for (ActivityLog a: al) {
			a.setUid(null);
			activityLogDAO.saveOrUpdate(a);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// plugin services;
		pluginService.restore(binder);
		
		log.info("Database restore success");
		
	}


	//********************************************************************
	//               set /  get 
	//********************************************************************
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}
	public void setBackupLocation(Resource location) {
		this.backupLocation = location;
	}


	public void setRssLocation(Resource rssLocation) {
		this.rssLocation = rssLocation;
	}


	public void setIndexLocation(Resource indexLocation) {
		this.indexLocation = indexLocation;
	}


	public void setRepositoryLocation(Resource repositoryLocation) {
		this.repositoryLocation = repositoryLocation;
	}


	public void setThemeLocation(Resource themeLocation) {
		this.themeLocation = themeLocation;
	}


	public void setSkinLocation(Resource skinLocation) {
		this.skinLocation = skinLocation;
	}
	public void setRootLocation(Resource rootLocation) {
		this.rootLocation = rootLocation;
	}

	public void setRoleDAO(RoleDAO roleDAO) {
		this.roleDAO = roleDAO;
	}

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}

	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}

	public void setCrWorkspaceDAO(CrWorkspaceDAO crWorkspaceDAO) {
		this.crWorkspaceDAO = crWorkspaceDAO;
	}

	public void setConfigurationDAO(ConfigurationDAO configurationDAO) {
		this.configurationDAO = configurationDAO;
	}

	public void setFriendDAO(FriendDAO frienDAO) {
		this.friendDAO = frienDAO;
	}

	public void setNotificationDAO(NotificationDAO notificationDAO) {
		this.notificationDAO = notificationDAO;
	}

	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}

	public void setCommentDAO(CommentDAO commentDAO) {
		this.commentDAO = commentDAO;
	}

	public void setSpaceTagDAO(SpaceTagDAO spaceTagDAO) {
		this.spaceTagDAO = spaceTagDAO;
	}

	public void setPageTagDAO(PageTagDAO pageTagDAO) {
		this.pageTagDAO = pageTagDAO;
	}

	public void setUserPageDAO(UserPageDAO userPageDAO) {
		this.userPageDAO = userPageDAO;
	}

	public void setWidgetDAO(WidgetDAO widgetDAO) {
		this.widgetDAO = widgetDAO;
	}

	public void setCrFileNodeDAO(CrFileNodeDAO crFileNodeDAO) {
		this.crFileNodeDAO = crFileNodeDAO;
	}

	public void setPermissionDAO(PermissionDAO permissionDAO) {
		this.permissionDAO = permissionDAO;
	}

	public void setHistoryDAO(HistoryDAO historyDAO) {
		this.historyDAO = historyDAO;
	}

	public void setDraftDAO(DraftDAO draftDAO) {
		this.draftDAO = draftDAO;
	}

	public void setJdbcTemplateDAO(JDBCTemplateDAO jdbcTemplateDAO) {
		this.jdbcTemplateDAO = jdbcTemplateDAO;
	}

	public void setPageLinkDAO(PageLinkDAO pageLinkDAO) {
		this.pageLinkDAO = pageLinkDAO;
	}

	public void setPageProgressDAO(PageProgressDAO pageProgressDAO) {
		this.pageProgressDAO = pageProgressDAO;
	}

	public void setInvitationDAO(InvitationDAO invitationDAO) {
		this.invitationDAO = invitationDAO;
	}

	public void setHibernateUtil(HibernateUtil hibernateUtil) {
		this.hibernateUtil = hibernateUtil;
	}

	public void setTemplateDAO(TemplateDAO templateDAO) {
		this.templateDAO = templateDAO;
	}

	public void setUpgradeService(UpgradeService upgradeService) {
		this.upgradeService = upgradeService;
	}

	public void setRemoveSpaceJobInvoker(ExportableJob removeSpaceJobInvoker) {
		this.removeSpaceJobInvoker = removeSpaceJobInvoker;
	}

	/**
	 * @param backupJobInvoker the backupJobInvoker to set
	 */
	public void setBackupJobInvoker(ExportableJob backupJobInvoker) {
		this.backupJobInvoker = backupJobInvoker;
	}

	public void setRestoreLocation(Resource restoreLocation) {
		this.restoreLocation = restoreLocation;
	}


	//********************************************************************
	//               private classes
	//********************************************************************
	@SuppressWarnings("unchecked")
	private class HibernatePersistentSetConverter extends CollectionConverter{
		/**
		 * @param mapper
		 */
		public HibernatePersistentSetConverter(Mapper mapper) {
			super(mapper);
			
		}

		
		@Override
		public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
			Set set = new HashSet();
			for(Object o: ((PersistentSet)obj)){
				set.add(o);
			}
			super.marshal(set, writer, context);
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	        Collection collection = new HashSet();
	        populateCollection(reader, context, collection);
	        return collection;
		}
		
		@Override
		public boolean canConvert(Class clz) {
			if(PersistentSet.class.equals(clz))
				return true;
			
			return false;
		}
		

	}
	@SuppressWarnings("unchecked")
	private class HibernatePersistentBagConverter extends CollectionConverter{
		/**
		 * @param mapper
		 */
		public HibernatePersistentBagConverter(Mapper mapper) {
			super(mapper);
			
		}
		
		@Override
		public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
			List set = new ArrayList();
			for(Object o: ((PersistentBag)obj)){
				set.add(o);
			}
			super.marshal(set, writer, context);
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	        Collection collection = new ArrayList();
	        populateCollection(reader, context, collection);
	        return collection;
		}
		
		@Override
		public boolean canConvert(Class clz) {
			if(PersistentBag.class.equals(clz))
				return true;
			
			return false;
		}
		
		
	}
	@SuppressWarnings("unchecked")
	private class HibernatePersistentListConverter extends CollectionConverter{
		/**
		 * @param mapper
		 */
		public HibernatePersistentListConverter(Mapper mapper) {
			super(mapper);
			
		}
		
		@Override
		public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
			List set = new ArrayList();
			for(Object o: ((PersistentList)obj)){
				set.add(o);
			}
			super.marshal(set, writer, context);
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	        Collection collection = new ArrayList();
	        populateCollection(reader, context, collection);
	        return collection;
		}
		
		@Override
		public boolean canConvert(Class clz) {
			if(PersistentList.class.equals(clz))
				return true;
			
			return false;
		}
	}
	@SuppressWarnings("unchecked")
	private class HibernatePersistentMapConverter extends MapConverter{
		/**
		 * @param mapper
		 */
		public HibernatePersistentMapConverter(Mapper mapper) {
			super(mapper);
			
		}
		
		@Override
		public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) {
			Map map = new HashMap();
			
			for(Entry o : (Set<Entry>)((PersistentMap)obj).entrySet()){
				map.put(o.getKey(),o.getValue());
			}
			
			super.marshal(map, writer, context);
			
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
	        Map map = new HashMap();
	        populateMap(reader, context, map);
	        return map;
		}
		
		@Override
		public boolean canConvert(Class clz) {
			if(PersistentMap.class.equals(clz))
				return true;
			
			return false;
		}
	}
	private class ResolveRemoveHandler {

		private Writer writer;
		private Map<String,String> referenceClassMap = new HashMap<String, String>();
		private LinkedList<TagNameChangeElement> startTagStack = new LinkedList<TagNameChangeElement>();

		public ResolveRemoveHandler(Writer writer) {
			this.writer = writer;
		}

		public void characters(String content) throws IOException {
			writer.write(StringEscapeUtils.escapeXml(content));
		}

		public void endElement(String name) throws IOException {
			
			String tagName = name;
			if(startTagStack.size() > 0  && StringUtils.equals(name, startTagStack.getFirst().getName())){
				TagNameChangeElement ele = startTagStack.removeFirst();
				if(ele.getNewName() != null){
					tagName = ele.getNewName();
				}
			}
			StringBuffer line = new StringBuffer("</");
			line.append(tagName);
			line.append(">");
			
			writer.write(line.toString());
		
		}



		public void startElement(XmlPullParser xpp) throws IOException {

			final String name = xpp.getName();
			String tagName = name;
			
			boolean pushed = false;
			Map<String,String> attributes = new HashMap<String, String>();
			for(int idx =0;idx<xpp.getAttributeCount();idx++){
				attributes.put(xpp.getAttributeName(idx),xpp.getAttributeValue(idx));
			}
			
			String realClz = attributes.get("resolves-to");
			if(!StringUtils.isBlank(realClz)){
				if(name.startsWith("com.edgenius")){
					if(attributes.get("id") != null)
						referenceClassMap.put(attributes.get("id"), realClz);
					//this tag name is proxy class, replace to normal one
					//example: <com.edgenius.wiki.model.Page___-_-__javassist__9 id="48" resolves-to="com.edgenius.wiki.model.Page">
					tagName = realClz;
					startTagStack.addFirst(new TagNameChangeElement(name,tagName));
					pushed = true;
					attributes.remove("class");
				}else{
					//This tag is field of some tag, then need keep it is tag name but reset class to non-proxy class name
					//example:<content class="com.edgenius.wiki.model.HistoryContent_$$_javassist_4" id="1653" resolves-to="com.edgenius.wiki.model.HistoryContent">
					attributes.put("class", realClz);
				}
				//always remove "resolves-to"
				attributes.remove("resolves-to");
				
			}
			
			//replace reference class, this is assume reference must after real class, ie.
			// <page_javassist_xxx resolves-to="page" id=100> must before <parent class="page_javassist_xxx" reference="100">
			String ref = attributes.get("reference");
			if(ref != null){
				String newClz = referenceClassMap.get(ref);
				if(newClz != null){
					String clz = attributes.get("class");
					if(clz != null){
						//try to check if this reference tag name has been changed
						//example:<parent class="com.edgenius.wiki.model.Page_$$_javassist_9" reference="1657"/>
						attributes.put("class",newClz);
					}else{
						//example:<com.edgenius.wiki.model.Page___-_-__javassist__9 reference="1921" />
						//This tag name must be proxy class name, replace
						tagName = newClz;
						startTagStack.addFirst(new TagNameChangeElement(name,tagName));
						pushed = true;
					}
				}
			}
			
			if(!pushed && startTagStack.size() > 0 
				&& StringUtils.equals(startTagStack.getFirst().getName(),name)){
				//example <page resolves-to="xxx"><page></page></page>, this will avoid embedded page closed tag won't be treat 
				//mapping with first tag, ie. wrong result <xxx><page></xxx></page>
				startTagStack.addFirst(new TagNameChangeElement(name,null));
			}
			
			StringBuffer line = new StringBuffer("<");
			line.append(tagName);
			
			for(Entry<String,String> entry :attributes.entrySet()){
				line.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
			}
			line.append(">");
			writer.write(line.toString());
			
//			System.out.println("URI:" + uri);
//			System.out.println("localName" + localName  );
//			System.out.println("name:" + name  );
//			StringBuffer att = new StringBuffer(); 
//			if(atts != null){
//				for(int idx =0;idx<atts.getLength();idx++){
//					att.append(atts.getQName(idx)).append("=").append(atts.getValue(idx));
//				}
//			}
//			System.out.println("atts:" +   att.toString());
		}
	
	}
	private static class TagNameChangeElement{

		private String name;
	
		private String newName;

		public TagNameChangeElement(String name, String newName) {
			this.name = name;
			this.newName = newName;
		}
		
		public boolean equals(Object obj){
			if(!(obj instanceof TagNameChangeElement))
				return false;
			
			return ((TagNameChangeElement) obj).name.equals(this.name);
		}
		public int hashCode(){
			return new HashCodeBuilder().append(this.name).toHashCode();
		}
		public String getName() {
			return name;
		}

		public String getNewName() {
			return newName;
		}

		
	}
	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}
	public static void main(String[] args) throws FileUtilException, IOException {
		//TODO: this part code may move to UnitTest in future
		List<Page> container = new ArrayList<Page>();
		List<Page> parents = new ArrayList<Page>();
		
		Page a = new Page();
		a.setPageUuid("a");
		Page b = new Page();
		b.setPageUuid("b");
		Page c = new Page();
		c.setPageUuid("c");
		Page d = new Page();
		d.setPageUuid("d");
		
		d.setParent(c);
		c.setParent(b);
		b.setParent(a);
//		a.setParent(d);
		
		BackupServiceImpl im = new BackupServiceImpl();
		im.processParentPage(container, d, parents, new ContentBodyMap());
		
		System.out.println(Arrays.toString(container.toArray()));
	}
}
