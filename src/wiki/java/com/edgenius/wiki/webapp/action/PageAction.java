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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.model.User;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.webapp.taglib.PageInfo;
import com.edgenius.wiki.InstanceSetting;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */

@SuppressWarnings("serial")
public class PageAction extends BaseAction {

	private static final String SE_DASHBOARD = "sehome";
	private static final String SE_SPACE = "sespace";
	private static final String SE_PAGE = "sepage";
	private static final int ITEM_COUNT_PER_PAGE = 30;
	
	//parameters
	//pageTitle
	private String p;
	//spaceUname
	private String s;
	//anchor
	private String a;
	//space uid - if spaceUname has invalid characters
	private String suid;
	//pageUuid - if pageTitle has invalid characters, 
	private String u;
	
	//read-only flag
	private String r;
	//page number for view-only pagination list of space/page
	private int page;
	
	private SpaceService spaceService; 
	private PageService pageService;
	private RenderService renderService;  
	private SecurityService securityService;
	private SettingService settingService;
	//********************************************************************
	//               Action methods
	//********************************************************************
	/**
	 * This method cooperate with URLRewrite.xml to handle system page URLs:
	 * There are 4 types URLs, and each type has full and tiny URL, then total 8 different URL for page:
	 * 
	 *  /page/xxx/xxx - This URL uses for public view, for example, link in send out mail, meaning URL in PrettyURL widget etc.
	 *  /p/ddd - This is above URL tiny style, as spaceUname or pageTitle has invalid URL characters, then we will use this one.
	 *  All above 2 URLs will redirect to /page#/xxx format for GWT ajax token base usage.
	 *  Please note, xxx - is encoded by Escpace.escapeToken(), rather than URLEncoder.encode() as xxx will redirect to GWT token directly. 
	 *  
	 *  /s/xxx - Tiny format to list pages in space which has invalid character in spaceUname. if read-only model, normally page list is /page/spaceUname format 
	 *  		 If the request doesn't come from SE, it will be redirect to /page#/xxx format.
	 *  
	 *  /view/xxx/xxx - This URL uses for read-only format page display.  This URL will keep page plain view, aka, read-only model 
	 *  /v/ddd -  Tiny format of above URL.  
	 * 
	 *  /sv/ddd -  Tiny format to list pages in space which has invalid character in spaceUname. if read-only model, normally page list is /page/spaceUname format.
	 *  			This URL will keep page in plain view, aka, read-only model.
	 * 
	 *  There is another token base URL request, which is invoke GWT ajax request. 
	 *  /page#/spaceUname/pageTitle
	 *  
	 *  TODO:This URL will be redirect to /page/spaceUname/pageTitle ONLY in robot model.
	 *  Please be careful page with anchor: #/spaceUname/pageTitle%23anchor -> /spaceUname/pageTitle#anchor
	 */
	public String doView(){
		boolean viewOnly = false;
		
		Boolean isRobot = (Boolean) getRequest().getAttribute(Constants.SE_ROBOT);
		boolean readonly = "true".equalsIgnoreCase(r);
		if((isRobot != null && isRobot) || readonly){
			viewOnly = true;
			log.info("View only render on flags isRobot={}; readonly={}", isRobot, readonly);
		}
//		viewOnly = true;

		if(!viewOnly){
			if(!StringUtils.isBlank(u)){
				//tinyURL - get back
				Page pg = pageService.getCurrentPageByUuid(u, false);
				if(pg != null){
					s = pg.getSpace().getUnixName();
					p = pg.getTitle();
				}
			}else if(!StringUtils.isBlank(suid) && NumberUtils.toInt(suid,-1) != -1){
				//home  page
				//spaceUname has invalid characters, then using suid...
				Space space = spaceService.getSpace(NumberUtils.toInt(suid));
				if(space != null)
					s = space.getUnixName();
				p = null;
			}	
			if(!StringUtils.isBlank(s)){
				try {
					getResponse().sendRedirect(WikiUtil.getPageRelativeTokenURL(s,p,a));
				} catch (IOException e) {
					//return to default page
					log.error("Unable send redirect",e);
					return SUCCESS;
				}
			}
			return SUCCESS;
		}else{
			//Search Engine Robot!!! Return pure HTML page
			log.info("Handle Read only view on agent {}", getRequest().getHeader("User-Agent"));
			getRequest().setAttribute("readonly", readonly);
			return handleViewonlyRequest();
		}
	}

	
	/**
	 * 
	 */
	private String handleViewonlyRequest() {
		User viewer = WikiUtil.getUser();

		if(!StringUtils.isBlank(s)){
			if(StringUtils.isBlank(p)){
				//pageTitle must not be blank for any page, even home, otherwise, here will be list pages on space
				//security check: is space allow read?
				if(securityService.isAllowSpaceReading(s, viewer)){
					//space home page: page list
					List<Page> list = pageService.getPageTree(s);
					Space space = spaceService.getSpaceByUname(s);
					getRequest().setAttribute(WikiConstants.ATTR_SPACE,space);
					getRequest().setAttribute(WikiConstants.ATTR_LIST, list);
				}
				return SE_SPACE;
			}else{
				//page content
				try {
					if("page.do".equals(p)){
						//This is for compatible old format URL page/page.do? format. Redirect to space home page
						getResponse().setStatus(301); //permanent redirect
						getResponse().sendRedirect(WikiUtil.getPageRedirFullURL(s, null, null));
					}
					
					Page contentPage = pageService.getCurrentPageByTitleWithoutSecurity(s, p, false);
					//TODO: even set URLEncoding="UTF8" and LocalFilter setRequestEncoding() , they won't fix problem if URL parameter  has non-ascii code.
					//so this may cause page can not return correct... (But above solve problem while in Ajax call in Spring MVC, need investigation, note,
					//this may cause  by HTTP get or post, post works for encoding parameters, but why URLEncoding not set, Spring MVC not working also?)
					viewPage(viewer, contentPage, s);
				} catch (Exception e) {
					log.error("Unable to render page " + s + ":" +p,e);
				}
				return SE_PAGE;
			}
		}
		

		if(!StringUtils.isBlank(u)){
			//pageUuid - tinyURL
			//page content
			try {
				Page contentPage = pageService.getCurrentPageByUuid(u);
				String suname = contentPage != null?contentPage.getSpace().getUnixName():null;
				viewPage(viewer, contentPage, suname);
			} catch (Exception e) {
				log.error("Unable to render page UUID: " + u,e);
			}
			return SE_PAGE;
		}
		
		//spaceUname has invalid characters, then using suid...
		if(!StringUtils.isBlank(suid) && NumberUtils.toInt(suid,-1) != -1){
			Space space = spaceService.getSpace(NumberUtils.toInt(suid));
			if(securityService.isAllowSpaceReading(space.getUnixName(), viewer)){
				//space home page: page list
				List<Page> list = pageService.getPageTree(space.getUnixName());
				getRequest().setAttribute(WikiConstants.ATTR_SPACE,space);
				getRequest().setAttribute(WikiConstants.ATTR_LIST, list);
			}
			return SE_SPACE;
		}
		
		if(page == 0){
			//This is chance to display default home page.It will include pagination list, in next page(1st), it will display 
			//space list and allow further crawl. 
			
			///TODO:However, there is one possibility, if URL is /page#/spaceUname/pageTitle format,
			//display home page is not expected. So in client side, it will check URL token, if it has, then it will try to 
			//redirect to /page/spaceUname/pageTitle format(without "#" token). 
			
			
			//first enter, page number is unset, value is 0. display default dashboard content
			InstanceSetting instance = settingService.getInstanceSetting();
			
			String markup; 
			if(instance == null || StringUtil.isBlank(instance.getDashboardMarkup())){
				markup = SharedConstants.DEFAULT_DAHSBOARD_MARKUP;
			}else{
				markup = instance.getDashboardMarkup();
			}
			List<RenderPiece> pieces = renderService.renderHTML(markup);
			String content = renderService.renderNativeHTML(null, null, pieces);
			getRequest().setAttribute(WikiConstants.ATTR_CONTENT, content);
		}else{
			//dashboard: space list
			List<Space> list = spaceService.getSpaces(viewer, (page-1)*ITEM_COUNT_PER_PAGE, ITEM_COUNT_PER_PAGE,null,null,false);
			
			//return result won't include private as viewer should be anonymous, but here just for top security - double check!
			for(Iterator<Space> iter = list.iterator();iter.hasNext();){
				Space space = iter.next();
				if(space.isPrivate() || StringUtils.equalsIgnoreCase(SharedConstants.SYSTEM_SPACEUNAME, space.getUnixName()))
					iter.remove();
			}
			getRequest().setAttribute(WikiConstants.ATTR_LIST, list);
			
		}
		
		int total = spaceService.getSpaceCount(null);
		PageInfo pinfo = new PageInfo();
		pinfo.setTotalPage(total/ITEM_COUNT_PER_PAGE + (total%ITEM_COUNT_PER_PAGE>0?1:0));
		
		pinfo.setCurrentPage(page);
		getRequest().setAttribute(WikiConstants.ATTR_PAGINATION_INFO, pinfo);
		
		return SE_DASHBOARD;
		
	}

	/**
	 * @param viewer
	 * @param contentPage
	 * @param spaceUname
	 */
	private void viewPage(User viewer, Page contentPage, String spaceUname) {
		if(contentPage != null && securityService.isAllowPageReading(spaceUname, contentPage.getPageUuid(), viewer)){
			contentPage.setRenderPieces(renderService.renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, contentPage));
			String content = renderService.renderNativeHTML(contentPage.getSpace().getUnixName(), contentPage.getPageUuid(), contentPage.getRenderPieces());
			contentPage.getContent().setContent(content);
			getRequest().setAttribute(WikiConstants.ATTR_PAGE, contentPage);
			
			if(Global.ADSENSE){
				String root = DataRoot.getDataRoot();
				if(FileUtil.exist(root+"ad.html")){
					//This is a trick, allow user put external HTML to display AD
					try {
						File adfile = FileUtil.getFile(root+"ad.html");
						String adtext = FileUtils.readFileToString(adfile);
						getRequest().setAttribute("adtext", adtext);
					} catch (IOException e) {
						log.error("Unable to read ad.html", e);
					}
				}else{
					getRequest().setAttribute("adsense", Global.ADSENSE);
				}
			}
			getRequest().setAttribute("origLink", WikiUtil.getPageRedirFullURL(s, p, contentPage.getPageUuid()));
			if(Shell.enabled){
				getRequest().setAttribute("shellLink", Shell.getPageShellURL(s, p));
			}
		}
	}

	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setP(String pageUname) {
		this.p = pageUname;
	}
	public void setS(String spaceUname) {
		this.s = spaceUname;
	}


	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public String getU() {
		return u;
	}

	public void setU(String u) {
		this.u = u;
	}

	public String getR() {
		return r;
	}

	public void setR(String r) {
		this.r = r;
	}

	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSettingService(SettingService settingService) {
		this.settingService = settingService;
	}

}
