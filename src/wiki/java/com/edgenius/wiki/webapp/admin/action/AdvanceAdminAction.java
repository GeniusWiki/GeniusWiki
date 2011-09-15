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
package com.edgenius.wiki.webapp.admin.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;

import com.edgenius.core.Installation;
import com.edgenius.core.Version;
import com.edgenius.core.model.User;
import com.edgenius.core.service.CacheService;
import com.edgenius.core.service.MessageService;
import com.edgenius.license.InvalidLicenseException;
import com.edgenius.license.LicenseManager;
import com.edgenius.wiki.ActivityType;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.ActivityLog;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.search.service.IndexRebuildListener;
import com.edgenius.wiki.search.service.IndexService;
import com.edgenius.wiki.search.service.IndexServiceImpl;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.webapp.action.BaseAction;
import com.edgenius.wiki.webapp.context.ApplicationContextUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class AdvanceAdminAction extends BaseAction{

	private static final String MESSAGE = "message";
	protected static final int QUOTA_RESET_COUNT = 3000;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// parameters
	private String license;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// service
	private CacheService cacheService;
	private IndexService indexService;
	private MessageService messageService;
	private SettingService settingService;
	private SpaceDAO spaceDAO;
	private PageDAO pageDAO;
	//********************************************************************
	//               function methods
	//********************************************************************
	public String  execute(){
		//load page
		viewLicense();
		getRequest().setAttribute("logDir", System.getProperty("geniuswiki.log.dir"));
		return SUCCESS;
	}

	public String updateLicense(){
		if (Installation.INSTANCE_HOSTING.equalsIgnoreCase(Installation.INSTANCE_TYPE)){
			//hosting version does not allow update current license
			viewLicense();
			return SUCCESS;
		}
		
		int status = LicenseManager.verfiyLicense(license, userReadingService.getUserTotalCount(null));
		if( status == 0){
			Installation install = Installation.refreshInstallation();
			install.setLicense(license);
			settingService.saveInstallation(install);
			
			//update Version class information
			ApplicationContextUtil.licenseCheck(ServletActionContext.getServletContext(), license);
			getRequest().setAttribute("error",null);
		}else{
			getRequest().setAttribute("error", messageService.getMessage("update.failed") + getLicenseStatus(status));
		}
		
		viewLicense();
		return SUCCESS;
	}
	public String resetCache(){
		
		cacheService.reset(CacheService.CACHE_ALL);
		getRequest().setAttribute("message", messageService.getMessage("cache.cleaned"));
		return MESSAGE;
	}
	public String printCache() {

		cacheService.printPolicyCache();
		getRequest().setAttribute("message", messageService.getMessage("cache.printed"));
		return MESSAGE;
	}
	public String redeployShell(){
		User anonymous = userReadingService.getUserByName(null);
		
		//in shell side, page request also verify if space exists or not, if not, it will do space request
		//so here won't do space request
		final LinkedBlockingDeque<String[]> pageQ = new LinkedBlockingDeque<String[]>();
		
		//This will make GW request Shell.key again - so if Shell site cleans the data, re-deploy still can work out.
		//If shell data is still there, it will return same key as GW instanceID and address won't changed.
		Shell.key = null;
		
		new Thread(new Runnable(){
			public void run() {
				int pageCount = 0;
				do{
					try {
						String[] str = pageQ.take();
						
						if(StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, str[0]))
							break;
						
						Shell.notifyPageCreate(str[0], str[1], true);
						pageCount++;
						
						//don't explode the too many concurrent request to Shell!
						Thread.sleep(1000);
						if((pageCount % QUOTA_RESET_COUNT) == 0){
							log.warn("Maximumn page shell request count arrived {}, sleep another 24 hours ", QUOTA_RESET_COUNT);
							//google app engine has quota limitation. Here will sleep 24 hours to wait next quota reset.
							Thread.sleep(24*3600*1000);
							log.warn("Maximumn page shell request sleep is end, restart page request process");
						}
					} catch (InterruptedException e) {
						log.error("Thread interrupted for shell request", e);
					}
				}while(true);
				
				ActivityLog activity = new ActivityLog();
				activity.setType(ActivityType.Type.SYSTEM_EVENT.getCode());
				activity.setSubType(ActivityType.SubType.REDEPLOY_SHELL.getCode());
				activity.setTgtResourceName("SHELL-DEPLOYED");//hardcode
				activity.setCreatedDate(new Date());
				activityLog.save(activity);

				
				log.info("Shell page request is done for {} pages", pageCount);
			}
		}).start();
		
		
		int pageCount = 0;
		
		long start = System.currentTimeMillis();
		log.info("Shell redeploy request starting...");
		
		List<Space> spaces = spaceDAO.getObjects();
		if(spaces != null){
			for (Space space : spaces) {
				if(space.isPrivate() || space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) 
					|| StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, space.getUnixName()))
					continue;
				
				String spaceUname = space.getUnixName();
			
				List<String> pages = pageDAO.getPagesUuidInSpace(spaceUname);
				if(pages != null){
					for (String puuid : pages) {
						if(!securityService.isAllowPageReading(spaceUname, puuid, anonymous))
							continue;
						try {
							pageQ.put(new String[]{spaceUname, puuid});
							pageCount++;
						} catch (InterruptedException e) {
							log.error("Thread interrupted for shell Page Queue", e);
						}
						
					}
				}
			}
			
			log.info("All shell request put into queue. Pages{}; Takes {}s", 
					new Object[]{pageCount, (System.currentTimeMillis() - start)/1000});
		}

		try {
			pageQ.put(new String[]{SharedConstants.SYSTEM_SPACEUNAME, ""});
		} catch (InterruptedException e) {
			log.error("Thread interrupted for shell Page Queue - end sign", e);
		}
		
		getRequest().setAttribute("message", messageService.getMessage("redeploy.shell.invoked"));
		return MESSAGE;
	}
	public String rebuildIndex() {
		try {
			IndexRebuildListener listener = new IndexRebuildListener(){
				public void indexComplete(int type) {
					//TODO: process of rebuild index....
				}
			};
			rebuildIndexes(listener, (IndexServiceImpl) indexService);
		} catch (Exception e) {
			getRequest().setAttribute("message",  messageService.getMessage("indexing.failed"));
		}
		getRequest().setAttribute("message", messageService.getMessage("indexing.started"));
		return MESSAGE;
	}

	//********************************************************************
	//               private
	//********************************************************************
	private void viewLicense() {
		Installation install = Installation.refreshInstallation();
		getRequest().setAttribute("isHosting", Installation.INSTANCE_HOSTING.equalsIgnoreCase(Installation.INSTANCE_TYPE));
		
		try {
			LicenseManager mgr = new LicenseManager(install.getLicense());
			getRequest().setAttribute("licCompany", mgr.getCompany());
			
			String expired;
			if(mgr.getExpiredDate() == null)
				expired = messageService.getMessage("not.available");
			else
				expired = SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG).format(mgr.getExpiredDate());
			getRequest().setAttribute("licExpired", expired);
			
			String users;
			if(mgr.getUserLimit() <=0)
				users = messageService.getMessage("unlimited");
			else
				users = mgr.getUserLimit()+"";
			getRequest().setAttribute("licLimit", users);
			
			if(Version.LICENSE_STATUS > 0)
				getRequest().setAttribute("licstatus", "<span color='#D8000C'>"+getLicenseStatus(Version.LICENSE_STATUS)+"</span>");
			else
				getRequest().setAttribute("licstatus", getLicenseStatus(Version.LICENSE_STATUS));
			
		} catch (InvalidLicenseException e) {
			getRequest().setAttribute("error", messageService.getMessage("invalid.license"));
			log.error("Unable to get license ",e);
		}
	}
	/**
	 * @return
	 */
	private String getLicenseStatus(int status) {
		if(status == 1) 
			return messageService.getMessage("invalid.license.reason.1");
		else if(status == 2){
			return messageService.getMessage("invalid.license.reason.2");
		}else if(status > 0){
			return messageService.getMessage("invalid.license");
		}else{
			return messageService.getMessage("valid");
		}
		
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}
	public String getLicense() {
		return license;
	}
	public void setLicense(String license) {
		this.license = license;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}

	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}


}
