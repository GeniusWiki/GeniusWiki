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
package com.edgenius.wiki.gwt.client.page;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * @author Dapeng.Ni
 */
public class SideBar extends PinPanel{

	private RenderPanel sidebarRender = new WikiRenderPanel();
	private FlowPanel sidebarFuncPanel = new FlowPanel();
	
	public SideBar(){
		super(false, null);
		sidebarFuncPanel.setStyleName(Css.ACTION);
		
	}
	
	public void fillPanel(final PageModel model){
		content.clear();
		
		if(!GwtUtils.contains(model.attribute,PageAttribute.NO_SIDE_BAR)){
			//must enable first
			sidebarRender.clear();
			content.add(sidebarRender);
			main.setSidebarButtonVisible(ClientConstants.RIGHT, true);
			//although server side is not possible pass model.pinPanel here as minus value, here just for double confirm.
			main.setSidebarVisible(ClientConstants.RIGHT, (model.pinPanel < 0 || (model.pinPanel & SharedConstants.TAB_TYPE_RIGHT_SIDEBAR) > 0));
			
			//at moment, author and modifier info in right side bar
			//construct page author / modifier information
			PageRender sideRender = new PageRender(sidebarRender);
			sideRender.renderContent(model.spaceUname, model,model.sidebarRenderContent, false);
			
			//allow user edit this page side bar, to make thing simple, only space admin have permission to edit side bar
			if(!AbstractEntryPoint.isOffline() && model.permissions[SharedConstants.PERM_SPACE_BASE + ClientConstants.ADMIN] == 1){
				sidebarFuncPanel.clear();
				content.add(sidebarFuncPanel);
				
				EventfulHyperLink editSidebar = new EventfulHyperLink(Msg.consts.edit_sidebar(),
						GwtUtils.buildToken(PageMain.TOKEN_EDIT_SIDEBAR, model.spaceUname,model.pageUuid));
				
				//add a separator
				sidebarFuncPanel.add(editSidebar);
			}
		}else{
			//just disable the whole right sidebar
			main.setSidebarButtonVisible(ClientConstants.RIGHT, false);
			main.setSidebarVisible(ClientConstants.RIGHT, false);
		}
	}


}
