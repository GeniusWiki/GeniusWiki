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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.ScaleImage;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.ShellTheme;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.SpaceUtil;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.rss.RSSService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * Space admin on Space admin page
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class SpaceAction  extends BaseAction {
	protected static final int QUOTA_RESET_COUNT = 1000;
	private static final String RET_SHELL = "shell";
	
	private String spaceUname;
	private File file;
    private String fileContentType;
    private String fileFileName;
    private int widgetStyle;
    private boolean showPortrait;
    private int[] commentNTo;
    private int commentNTtype;
    private int commentMaxPerDay;
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // service
    private SettingService settingService;
    private PageService pageService;
    private SpaceService spaceService;
    private ThemeService themeService;
    private SecurityDummy securityDummy;
    
    private RSSService rssService;
    
    public String setting(){
    	securityDummy.checkSpaceAdmin(spaceUname);
    	
    	Space space = spaceService.getSpaceByUname(spaceUname);
    	if(space == null)
    		return ERROR;
    	
    	SpaceSetting setting = space.getSetting();
    	widgetStyle = setting.getWidgetStyle();
    	showPortrait = !setting.isHidePortrait();
    	
    	int nt = setting.getCommentNotifyType();
    	resetCommentNTo(nt);
    	
    	if((nt & SpaceSetting.COMMENT_NOTIFY_FEQ_DAILY)>0){
    		commentNTtype = SpaceSetting.COMMENT_NOTIFY_FEQ_DAILY;
    	}
    	if((nt & SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST)>0){
    		commentNTtype = SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST;
    	}
    	
    	commentMaxPerDay = setting.getCommentNotifyMaxPerDay();
    	return "setting";
    }

	
    public String updateSetting(){
    	Space space = spaceService.getSpaceByUname(spaceUname);
    	if(space == null)
    		return ERROR;
    	
    	SpaceSetting setting = space.getSetting();
    	int nTo = 0;
    	if(commentNTo != null && commentNTo.length > 0){
    		for (int to : commentNTo) {
				nTo |= to;
			}
    		setting.setCommentNotifyType( nTo | commentNTtype);
    	}else{
    		setting.setCommentNotifyType(0);
    	}
    	resetCommentNTo(nTo);
    	
    	if((commentNTtype & SpaceSetting.COMMENT_NOTIFY_FEQ_EVERY_POST)>0){
    		setting.setCommentNotifyMaxPerDay(commentMaxPerDay);
    	}else{
    		//although disable but show value on spaceSetting 
    		commentMaxPerDay = setting.getCommentNotifyMaxPerDay();
    	}
    	
    	setting.setHidePortrait(!showPortrait);
    	
    	int origStyle = setting.getWidgetStyle();
    	setting.setWidgetStyle(widgetStyle);
//    	setting.setRssItemsCount(rssItemsCount)
//    	setting.setRssContentLen(rssContentLen)
//    	setting.setForbidReaderViewHistory(forbidReaderViewHistory)
    	try {
    		settingService.saveOrUpdateSpaceSetting(space, setting);
		} catch (Exception e) {
			log.error("Unable save space setting " + spaceUname,e);
			getRequest().setAttribute("message", "Save failed, please try again");
			return "setting";
		}
		
		try {
	    	//Check widget style if it is only for sort order change, if so, need rebuild space RSS feed to reflect change
	    	if(origStyle != widgetStyle
	    		&& (origStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE || origStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE)
	    		&& (widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE || widgetStyle == SpaceSetting.WIDGET_STYLE_ITEM_SHORT_BY_MODIFIED_DATE)){
	    		rssService.createFeed(spaceUname);
	    	}
		} catch (Exception e) {
			log.error("Rebuild space RSS feed failed, however ignore this error." + spaceUname,e);
		}
		
		getRequest().setAttribute("message", "Save success");
    	return "setting";
    }
    
    /**
     * Upload Logo
     * @return
     */
	public String doLogo(){
		if(StringUtils.isBlank(spaceUname)){
			try {
				getResponse().getWriter().write(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR);
			} catch (IOException e) {
				log.error("IO Error " , e);
			}
			return null;
		}
		
		//TODO: need to file is image checking.
		try{
			Space space = spaceService.getSpaceByUname(spaceUname); 
			//So far, the file is temporary file, it has ".tmp" extension, so don't use it.
			File smallOut = ScaleImage.scale(file,FileUtil.getFileExtension(fileFileName), WikiConstants.LOGO_SMALL_WIDTH);
			//don't cut original logo
			File largeout = file;
			
			//give different file name, so that it won't be version file, 
			FileNode largeLogo = getFileNode(largeout,"");
			
			FileNode smallLogo = getFileNode(smallOut,"small");

			spaceService.uploadLogo(space, smallLogo, largeLogo);
			
			String smallUrl = SpaceUtil.getSpaceLogoUrl(space,themeService,space.getLogoSmall(),false);
			String largeUrl = SpaceUtil.getSpaceLogoUrl(space,themeService,space.getLogoLarge(),true);
			
			getResponse().getWriter().write(smallUrl + SharedConstants.LOGO_SEP + largeUrl);
		} catch (AuthenticationException e) {
			sendAjaxFormRedir(SharedConstants.FORM_RET_AUTH_EXP);
			return null;
		} catch (AccessDeniedException e) {
			sendAjaxFormRedir(SharedConstants.FORM_RET_ACCESS_DENIED_EXP);
			return null;
		} catch (Exception e) {
			log.error(e.toString(),e);
			//any error, maybe user upload incorrect format image
			try {
				getResponse().getWriter().write(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR);
			} catch (IOException e1) {
				log.error("Unable to response" ,e);
			}
		}
		return null;
	}

	//********************************************************************
	//               Shell functions
	//********************************************************************
	/**
	 * Shell admin page - display shell name, image etc.
	 * @throws UnsupportedEncodingException 
	 */
	public String shell(){
		if(!Shell.enabled){
			getRequest().setAttribute("shellServiceEnabled", false);
			getRequest().setAttribute("warning", "Shell service doesn't enabled, please contact your system adminitrator.");
			return RET_SHELL;
		}
		
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			getRequest().setAttribute("shellServiceEnabled", false);
			getRequest().setAttribute("error", "Space doesn't exist.");
			return RET_SHELL;
		}
		
		if(space.isPrivate()){
			getRequest().setAttribute("shellServiceEnabled", false);
			getRequest().setAttribute("warning", "Private space can not be pushed to publich Shell service.");
			return RET_SHELL;
		}
		
		try {
			getRequest().setAttribute("shellServiceEnabled", true);
			Map<String, String> shellNames = space.getSetting().getShellNames();
			String shellTheme = null;
			if(shellNames != null)
				shellTheme = shellNames.get(spaceUname);
			if(shellTheme == null) shellTheme = "simple"; //default name
			
			ShellTheme theme = new ShellTheme();
			theme.setName(shellTheme);
			theme.setUrl(Shell.url + "page/" +  URLEncoder.encode(spaceUname, Constants.UTF8));
			theme.setImageUrl(Shell.rootUrl + "themes/" + theme.getName() + "/preview.png");
			theme.setImageSmallUrl(Shell.rootUrl + "themes/" + theme.getName() + "/preview-small.png");
			
			
			theme.setEnabled(!space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) && !space.isPrivate());
			getRequest().setAttribute("theme",theme);
		} catch (Exception e) {
			getRequest().setAttribute("error", "Failed to get space shell information.");
			log.error("Get theme failed on space " + spaceUname, e);
		}
		
		
		return RET_SHELL;
	}
	public String shellEnable(){
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			getRequest().setAttribute("error", "Space doesn't exist.");
			return RET_SHELL;
		}
		if(space.isPrivate()){
			getRequest().setAttribute("error", "Private space can not be deploy to shell.");
			return RET_SHELL;
		}
		
		if(space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED)){
			space.removeExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
			spaceService.updateSpace(space, false);
		}
		getRequest().setAttribute("message", "Space shell service is enabled, please choose redeploy to push your space to shell.");
		
		return shell();
	}
	
	public String shellDisable(){
		//disable and delete
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null){
			getRequest().setAttribute("error", "Space doesn't exist.");
			return RET_SHELL;
		}
		
		if(!space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED)){
			space.addExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
			spaceService.updateSpace(space, false);
			
			//must after set space extLink type to shell_disabled as ShellValidatorServlet will verify against this value
			Shell.notifySpaceRemove(spaceUname);
		}
		
		getRequest().setAttribute("message", "Space shell service is disabled.");
		
		return RET_SHELL;
	}
	/**
     * Redeploy shell - request spaceUname as input parameter
     * @return
     */
    public String shellDeploy(){
    	securityDummy.checkSpaceAdmin(spaceUname);
    	
    	Space space = spaceService.getSpaceByUname(spaceUname);
		if(space.isPrivate() || space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED) 
			|| StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, space.getUnixName())){
			getRequest().setAttribute("error", "Deploy failed: Space is private or shell service is disabled.");
	    	return RET_SHELL;
		}
		
    	User anonymous = userReadingService.getUserByName(null);
		
		final LinkedBlockingDeque<String[]> pageQ = new LinkedBlockingDeque<String[]>();
		//This will make GW request Shell.key again - so if Shell site cleans the data, re-deploy still can work out.
		//If shell data is still there, it will return same key as GW instanceID and address won't changed.
		Shell.key = null;
		
		new Thread(new Runnable(){
			public void run() {
				int pageCount = 0;
				
				//in shell side, page request also verify if space exists or not, if not, it will do space request
				//however, if space already exist, it won't refresh space - if space has menu, it won't refresh again.
				//so, we also refresh space 
				Shell.notifySpaceCreate(spaceUname);
				
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
							log.warn("Maximumn page shell request count arrived {}, sleep another 24 hours on space {}.", QUOTA_RESET_COUNT, spaceUname);
							//google app engine has quota limitation. Here will sleep 24 hours to wait next quota reset.
							Thread.sleep(24*3600*1000);
							log.warn("Maximumn page shell request sleep is end, restart page request process on space{}.", spaceUname);
						}
					} catch (InterruptedException e) {
						log.error("Thread interrupted for shell request", e);
					}
				}while(true);
				
				log.info("Shell page request is done for {} pages on space {}.", pageCount, spaceUname);
			}
		}).start();
		
		
		int pageCount = 0;
		
		long start = System.currentTimeMillis();
		log.info("Shell on space {} redeploy request starting...", spaceUname);
		
		
		List<String> pages = pageService.getPagesUuidInSpace(spaceUname, anonymous);
		if(pages != null){
			for (String puuid : pages) {
				try {
					pageQ.put(new String[]{spaceUname, puuid});
					pageCount++;
				} catch (InterruptedException e) {
					log.error("Thread interrupted for shell Page Queue on space " + spaceUname, e);
				}
			}
		}
		
		log.info("All shell on space {} request put into queue. Pages{}; Takes {}s", 
				new Object[]{spaceUname, pageCount, (System.currentTimeMillis() - start)/1000});

		try {
			pageQ.put(new String[]{SharedConstants.SYSTEM_SPACEUNAME, ""});
		} catch (InterruptedException e) {
			log.error("Thread interrupted for shell Page Queue - end sign", e);
		}
		
		getRequest().setAttribute("message", "Shell is redeploying, this may take a couple of minutes to complete.");
		
		return shell();
    }
	//********************************************************************
	//               private methods
	//********************************************************************
	/**
	 * @param nt
	 */
	private void resetCommentNTo(int nt) {
		commentNTo = new int[3];
    	if((nt & SpaceSetting.COMMENT_NOTIFY_TO_AUTHOR)>0){
    		commentNTo[0] = 1;
    	}
    	if((nt & SpaceSetting.COMMENT_NOTIFY_TO_ALL_CONTRIBUTOR)>0){
    		commentNTo[1] = 1;
    	}
    	if((nt & SpaceSetting.COMMENT_NOTIFY_TO_SPACE_OWNEER)>0){
    		commentNTo[2] = 1;
    	}
	}
    
	/**
	 * @param largeout
	 * @return 
	 * @throws FileNotFoundException
	 */
	private FileNode getFileNode(File largeout, String namePrefix) throws FileNotFoundException {
		FileNode largeAtt = new FileNode();
		largeAtt.setShared(false);
		largeAtt.setFile(new FileInputStream(largeout));
		largeAtt.setFilename(namePrefix+fileFileName);
		largeAtt.setContentType(fileContentType);
		largeAtt.setType(RepositoryService.TYPE_SPACE);
		largeAtt.setIdentifier(spaceUname);
		largeAtt.setCreateor(WikiUtil.getUserName());
		largeAtt.setStatus(PageType.NONE_DRAFT.value());
		largeAtt.setSize(0);
		
		return largeAtt;
	}
	
	//********************************************************************
	//               set  /  get methods
	//********************************************************************
	public String getSpaceUname() {
		return spaceUname;
	}
	public void setSpaceUname(String spaceUname) {
		this.spaceUname = spaceUname;
	}
	public void setFile(File file) {
		this.file = file;
	}
	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}
	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public int[] getCommentNTo() {
		return commentNTo;
	}

	public void setCommentNTo(int[] commentNTo) {
		this.commentNTo = commentNTo;
	}

	public int getCommentNTtype() {
		return commentNTtype;
	}

	public void setCommentNTtype(int commentNTtype) {
		this.commentNTtype = commentNTtype;
	}

	public boolean isShowPortrait() {
		return showPortrait;
	}


	public void setShowPortrait(boolean showPortrait) {
		this.showPortrait = showPortrait;
	}


	public int getWidgetStyle() {
		return widgetStyle;
	}

	public void setWidgetStyle(int widgetStyle) {
		this.widgetStyle = widgetStyle;
	}


	public int getCommentMaxPerDay() {
		return commentMaxPerDay;
	}

	public void setCommentMaxPerDay(int commentMaxPerDay) {
		this.commentMaxPerDay = commentMaxPerDay;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setThemeService(ThemeService themeService) {
		this.themeService = themeService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}


	public void setRssService(RSSService rssService) {
		this.rssService = rssService;
	}


	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}


	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}
}
