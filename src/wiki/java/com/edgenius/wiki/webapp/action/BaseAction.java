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
package com.edgenius.wiki.webapp.action;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.service.UserService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.webapp.filter.AjaxRedirectFilter.RedirectResponseWrapper;
import com.edgenius.wiki.ActivityType;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.search.service.IndexRebuildListener;
import com.edgenius.wiki.search.service.IndexServiceImpl;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ActivityLogService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Dapeng.Ni
 */
public class BaseAction extends ActionSupport{
	private static final long serialVersionUID = 893311807490739850L;

	protected transient final Logger log = LoggerFactory.getLogger(getClass());
	
	public static final String CANCEL = "cancel";
	
	protected UserReadingService userReadingService;
	protected SecurityService securityService;
	protected UserService userService;

	protected ActivityLogService activityLog;
	
	//********************************************************************
	//               Utilities methods
	//********************************************************************
	/**
	 * @param dFile
	 */
	protected void downloadFile(String name, File dFile) {
		HttpServletResponse response = getResponse();
		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", "attachment;filename=\"" + name + "\"");
		response.setHeader("Cache-control", "must-revalidate");
		response.addHeader("Content-Description", name);

		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(dFile));
			OutputStream out = response.getOutputStream();
			int count = 0;

			int len=0;
			byte[] ch = new byte[10240];
			while ((len = in.read(ch)) != -1) {
				out.write(ch,0,len);
				count +=len;
			}
			response.setContentLength(count);
		} catch (IOException e) {
			log.error("Exception occured writing out file:" + name,e);
		} finally {
			try {
				if (in != null)
					in.close(); // very important
			} catch (IOException e) {
				log.error("Error Closing file. File already written out - no exception being thrown.", e);
			}
		}
	}
	/**
	 * @param redir
	 */
	protected void sendAjaxFormRedir(String type) {
		try {
			String redir = ((RedirectResponseWrapper)getResponse()).getRedirect();
			if(redir == null){
				if(SharedConstants.FORM_RET_AUTH_EXP.equals(type))
					redir = WikiConstants.URL_LOGIN;
				else if(SharedConstants.FORM_RET_ACCESS_DENIED_EXP.equals(type))
					redir = WikiConstants.URL_ACCESS_DENIED;
			}
			getResponse().getOutputStream().write((SharedConstants.FORM_RET_HEADER+type+redir).getBytes());
		} catch (IOException e) {
			log.error(e.toString(),e);
		}
	}
    @SuppressWarnings("unchecked")
	protected void saveMessage(String msg) {
        List<String> messages = (List<String>) getRequest().getSession().getAttribute("messages");
        if (messages == null) {
            messages = new ArrayList<String>();
        }
        messages.add(msg);
        getRequest().getSession().setAttribute("messages", messages);
    }
	/**
	 * Sort by: in one sorted table, it may contain multiple fields to allow sort by. System try to support multiple sort by. 
	 * For example, if there s1,s2,s3 field to sort, if user click s2 first, then "order by s2", then user click "s1" 
	 * then the expected is "order by s1,s2", if user click s3, then "order by s3,s1,s2", if user click s2 again, 
	 * then put s2 at beginning again, "order by s2,s3,s1"
	 * 
	 * @param sortBy
	 */
	protected String getSortBySequence(String sessionName, int sortBy) {
		//separator by "|"
		String sortSeq = (String) getSession().getAttribute(sessionName);
		
		if(sortSeq == null)
			return ""+(sortBy==0?"":sortBy);
		
		String[] sortStr = sortSeq.split("\\|");
		
		//put sortBy as first! It is primary sort keyword
		StringBuffer sortBySb = new StringBuffer(sortBy).append("|");
		
		//then append all other sort keyword
		for (String str : sortStr) {
			int sort = NumberUtil.toInt(str, -1);
			if(sort == -1)
				continue;
			if(sort == sortBy)
				continue;
			
			sortBySb.append(sort).append("|"); 
		}
		
		//remove last |
		return sortBySb.toString().substring(0,sortBySb.length()-1); 
	}
    /**
     * Convenience method to get the request
     * @return current request
     */
    protected HttpServletRequest getRequest() {
        return ServletActionContext.getRequest();  
    }
    
    /**
     * Convenience method to get the response
     * @return current response
     */
    protected HttpServletResponse getResponse() {
        return ServletActionContext.getResponse();
    }
    
    /**
     * Convenience method to get the session
     */
    protected HttpSession getSession() {
    	return getRequest().getSession();
    }
    
    /**
     * Ugly, but 2 Action class need it....
     * @param listener
     * @param indexService
     */
    protected void rebuildIndexes(final IndexRebuildListener listener,final  IndexServiceImpl indexService) {
    	//this method only clean index files so far...
    	indexService.cleanIndexes(listener);
    	
    	final String uuid = UUID.randomUUID().toString();
		//I don't know how invoke HibernateInceptor inside IndexServiceImpl.rebuildIndex() threads, so I copy 
		//them into here, so that each rebuild*() thread will be surround  HibernateInceptor to avoid lazy loading problem
		//see http://forum.springframework.org/showthread.php?p=200379#post200379
		Thread t1 = new Thread(new Runnable(){
			public void run() {
				try {
					//Some page render need space read permission, such as PageIndex needs PageService.getPageTree() method.
					securityService.proxyLoginAsSystemAdmin();
					((IndexServiceImpl)indexService).rebuildPageIndex();		
				} catch (Exception e) {
					log.warn("Page index rebuid complete with error",e);
				} finally{
					securityService.proxyLogout();
				}
				listener.indexComplete(IndexRebuildListener.PAGE);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.PAGE);
			}

		});
		t1.setName("Rebuild page index");
		t1.setDaemon(true);
		t1.start();
		
		Thread t2 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildCommentIndex();		
				listener.indexComplete(IndexRebuildListener.COMMENT);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.COMMENT);
			}
		});
		t2.setDaemon(true);
		t2.setName("Rebuild comment index");
		t2.start();
		
		Thread t3 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildSpaceIndex();
				listener.indexComplete(IndexRebuildListener.SPACE);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.SPACE);
			}
		});
		t3.setDaemon(true);
		t3.setName("Rebuild space index");
		t3.start();

		Thread t4 =  new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildUserIndex();
				listener.indexComplete(IndexRebuildListener.USER);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.USER);
					
			}
		});
		t4.setDaemon(true);
		t4.setName("Rebuild user index");
		t4.start();
		
		Thread t5 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildRoleIndex();
				listener.indexComplete(IndexRebuildListener.ROLE);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.ROLE);
			}
		});
		t5.setDaemon(true);
		t5.setName("Rebuild role index");
		t5.start();
		
		Thread t6 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildPageTagIndex();
				listener.indexComplete(IndexRebuildListener.PTAG);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.PTAG);
			}
		});
		t6.setDaemon(true);
		t6.setName("Rebuild page tag index");
		t6.start();
		
		Thread t7 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildSpaceTagIndex();
				listener.indexComplete(IndexRebuildListener.STAG);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.STAG);
			}
		});
		t7.setDaemon(true);
		t7.setName("Rebuild space tag index");
		t7.start();
		
		Thread t8 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildAttachmentIndex();
				listener.indexComplete(IndexRebuildListener.ATTACHMENT);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.ATTACHMENT);
			}
		});
		t8.setDaemon(true);
		t8.setName("Rebuild attachment index");
		t8.start();
		
		Thread t9 = new Thread(new Runnable(){
			public void run() {
				((IndexServiceImpl)indexService).rebuildWidgetIndex();
				listener.indexComplete(IndexRebuildListener.WIDGET);
				
				//log activity
				logRebuildIndexEvent(uuid, IndexRebuildListener.WIDGET);
			}
		});
		t9.setDaemon(true);
		t9.setName("Rebuild widget index");
		t9.start();
	}
    
	private synchronized void logRebuildIndexEvent(String uuid, int type) {
		try {
			File idFile = new File(FileUtil.TEMP_DIR, "bi"+uuid);
			if(idFile.exists()){
				int sumType = NumberUtils.toInt(FileUtils.readFileToString(idFile),-1);
				if(type+sumType == IndexRebuildListener.INDEX_SIZE){
					//index completed
					ActivityLog activity = new ActivityLog();
					activity.setType(ActivityType.Type.SYSTEM_EVENT.getCode());
					activity.setSubType(ActivityType.SubType.REBUILD_INDEX.getCode());
					activity.setSrcResourceType(0);  //"0" resource type is partial indexed
					activity.setSrcResourceName(uuid);
					activity.setStatus(0);
					activity.setTgtResourceType(type);
					activity.setStatus(1);
					activity.setCreatedDate(new Date());
					activityLog.save(activity);
					
					FileUtils.deleteQuietly(idFile);
				}else{
					//waiting other index rebuild is done.
					FileUtils.writeStringToFile(idFile, String.valueOf(type+sumType));
				}
				if(sumType == -1){
					log.error("Index indicator file failed {} doesn't include valid value.", idFile.getName());
				}
			}else{
				//waiting other index rebuild is done.
				FileUtils.writeStringToFile(idFile, String.valueOf(type));
			}
		} catch (IOException e) {
			log.error("Write index indicator file failed",e);
		}
		
		
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	public void setActivityLog(ActivityLogService activityLog) {
		this.activityLog = activityLog;
	}
	/**
	 * @param securityService the securityService to set
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	
	//********************************************************************
	//               Implementation methods
	//********************************************************************
}
