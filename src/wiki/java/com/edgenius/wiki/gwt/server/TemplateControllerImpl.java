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
package com.edgenius.wiki.gwt.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.TemplateListModel;
import com.edgenius.wiki.gwt.client.model.TemplateModel;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.TemplateController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.Template;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SecurityDummy;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.TemplateService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class TemplateControllerImpl extends GWTSpringController implements TemplateController{

	private TemplateService templateService;
	private SpaceService spaceService;
	private MessageService messageService;
	private RenderService renderService;
	private SecurityDummy securityDummy;


	public TemplateListModel deleteTemplate(String spaceUname, Integer templID) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		templateService.remove(templID);
		return getTemplates(spaceUname, false, true);
	}


	public TemplateListModel getTemplates(String spaceUname, boolean withOtherSpaces, boolean adminOnly) {
		User viewer = WikiUtil.getUser();
		
		if( adminOnly ){
			if(StringUtils.isBlank(spaceUname) ||SharedConstants.SYSTEM_SPACEUNAME.equals(spaceUname)){
				securityDummy.checkInstanceAdmin();
			}else{
				securityDummy.checkSpaceAdmin(spaceUname);
			}
		}else{
			if(StringUtils.isBlank(spaceUname)){
				TemplateListModel model = new TemplateListModel();
				model.errorCode = ErrorCode.TMEPL_LOAD_FAILED; 
			}else{
				//this method is call from user editing page and get template list, so check spaceWrite permission
				securityDummy.checkSpaceWrite(spaceUname);
			}
		}
		
		List<Template> templs = templateService.getSpaceTemplates(spaceUname, viewer, withOtherSpaces);
		return copyTemplates(templs, spaceUname, viewer);
	}
	
	public PageModel createTemplate(String spaceUname){
		securityDummy.checkSpaceAdmin(spaceUname);
		
		//navbar and detect user preferred editor
		PageModel page = new PageModel();
		String spaceTitle = getSpaceTitle(spaceUname);
		page.spaceUname = spaceUname;
		page.spaceTitle = spaceTitle;
		
		List<PageModel> navList = new ArrayList<PageModel>();
		PageModel nav = new PageModel();
		nav.title = messageService.getMessage("template.list");
		nav.spaceTitle = spaceTitle;
		nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TEMPLATE_LIST),spaceUname);
		navList.add(nav);
		
		nav = new PageModel();
		nav.title = messageService.getMessage("create.template");
		nav.spaceTitle = spaceTitle;
		nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.TOKEN_EDIT_TEMPLATE));
		navList.add(nav);
		
		page.ancenstorList = new ArrayList<PageModel>();
		page.ancenstorList.addAll(navList);
		
		//user preferred editor
		User viewer = WikiUtil.getUser();
		page.isRichContent = viewer.getSetting().isUsingRichEditor();
		
		return page;
	}


	public PageModel editTemplate(String spaceUname, Integer templID) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		Template templ = templateService.getTemplate(templID);
		User viewer = WikiUtil.getUser();
		PageModel page = new PageModel();
		page.uid = templ.getUid();
		page.title = templ.getName();
		page.tagString = templ.getDescription();
		//damn hacker: user history to identify if this template shared
		page.isHistory = templ.isShared();
		
		page.isRichContent = viewer.getSetting().isUsingRichEditor();
		
		if(page.isRichContent){
			page.sidebarRenderContent = renderService.renderHTML(RenderContext.RENDER_TARGET_RICH_EDITOR, templ.getContent());
			page.content =  renderService.renderRichHTML(spaceUname, null, page.sidebarRenderContent);
		}else{
			page.sidebarRenderContent = renderService.renderHTML(templ.getContent());
			page.content = templ.getContent();
		}
		
		//navbar
		String spaceTitle = getSpaceTitle(spaceUname);
		page.spaceUname = spaceUname;
		page.spaceTitle = spaceTitle;
		List<PageModel> navList = new ArrayList<PageModel>();
		PageModel nav = new PageModel();
		nav.title = messageService.getMessage("template.list");
		nav.spaceTitle = spaceTitle;
		nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TEMPLATE_LIST));
		navList.add(nav);
		nav = new PageModel();
		
		nav.title = messageService.getMessage("edit.template");
		nav.spaceTitle = spaceTitle;
		nav.navToken = GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.TOKEN_EDIT_TEMPLATE),String.valueOf(templID));
		navList.add(nav);
		
		page.ancenstorList = new ArrayList<PageModel>();
		page.ancenstorList.addAll(navList);
		
		return page;
	}

	public PageModel previewTemplate(String spaceUname, String content, boolean richEnabled) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		PageModel model = new PageModel();
		if(richEnabled){
			content = renderService.renderHTMLtoMarkup(spaceUname,content);
		}
		model.renderContent = renderService.renderHTML(content);
		
		return model;
	}

	public TemplateListModel saveTemplate(String spaceUname, Integer templID, String title, String desc, String content, boolean richEnabled,
			boolean shared) {
		securityDummy.checkSpaceAdmin(spaceUname);
		
		Template templ = new Template();
		
		if(templID != null && templID != 0)
			//update
			templ = templateService.getTemplate(templID);
		
		WikiUtil.setTouchedInfo(userReadingService, templ);
		templ.setName(title);
		templ.setDescription(desc);
		if(richEnabled){
			content = renderService.renderHTMLtoMarkup(spaceUname, content);
		}
		templ.setContent(content);
		templ.setShared(shared);
		Space space = spaceService.getSpaceByUname(spaceUname);
		templ.setSpace(space);
		templateService.saveOrUpdateTemplates(templ);
		
		//it is not necessary to return full list as client side will do refresh token again - then go to template list page
		TemplateListModel model = new TemplateListModel();
		model.spaceUname = spaceUname;
		return model;
	}
	public TextModel getTemplate(String spaceUname, Integer templID){
		Template templ = templateService.getTemplate(templID);
		TextModel model = new TextModel();
		if(templ == null){
			model.errorCode = ErrorCode.TMEPL_LOAD_FAILED;
			return model;
		}
		
		if(!templ.isShared())
			securityDummy.checkSpaceRead(spaceUname);
		
		model.setText(templ.getContent());
		return model;
		
	}
	//********************************************************************
	//               private methods
	//********************************************************************

	/**
	 * @param spaceUname
	 * @return
	 */
	private String getSpaceTitle(String spaceUname) {
		//???if space not exist, return spaceUname???
		String spaceTitle = spaceUname;
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space != null)
			spaceTitle = space.getName();
		return spaceTitle;
	}
	/**
	 * @param templs
	 * @param spaceUname 
	 * @param viewer 
	 * @return
	 */
	private TemplateListModel copyTemplates(List<Template> templs, String spaceUname, User viewer) {
		TemplateListModel model = new TemplateListModel();
		model.spaceUname = spaceUname;
		model.templates = new ArrayList<TemplateModel>();
		for (Template template : templs) {
			model.templates.add(copyTemplate(viewer, template)); 
		}
		return model;
	}


	/**
	 * @param viewer
	 * @param template
	 * @return 
	 */
	private TemplateModel copyTemplate(User viewer, Template template) {
		TemplateModel tModel = new TemplateModel();
		tModel.id = template.getUid();
		tModel.name = template.getName();
		tModel.desc = template.getDescription();
		tModel.shared = template.isShared();
		tModel.fromSpace = template.getSpace().getUnixName();
		tModel.author = UserUtil.copyUserToModel(viewer, template.getCreator());
		
		return tModel;
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setTemplateService(TemplateService templateService) {
		this.templateService = templateService;
	}


	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}


	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}


	public void setRenderService(RenderService renderService) {
		this.renderService = renderService;
	}
	public void setSecurityDummy(SecurityDummy securityDummy) {
		this.securityDummy = securityDummy;
	}

}
