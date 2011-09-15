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
package com.edgenius.wiki.gwt.client.home;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.MessagePanel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderContentListener;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dashboard Panel
 * @author Dapeng.Ni
 *  Merge SearchMain from GWT Module into SimplePanel and render as a one of Deck panel in PageMain. (Oct. 25th, 2007). 
 */
public class HomeMain extends MessagePanel implements RenderContentListener, AsyncCallback<RenderMarkupModel> {

	private PageMain main;
	private RenderPanel renderPanel = new WikiRenderPanel();
	private PageRender render = new PageRender(renderPanel);
	
	public HomeMain(PageMain main){
		this.main = main;
		render.registerPortalVistor(new HomePortalVisitor());
		render.addRenderContentListener(this);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(renderPanel);
		
		//!!!DON'T user default render style - just remove it to use body level css
		renderPanel.removeStyleName(Css.RENDER_CONTENT);
		DOM.setElementAttribute(panel.getElement(), "width", "100%");
		this.setWidget(panel);
		
	}

	public void login(UserModel user) {
		Log.info("Home main get login message : "  + user);
		render.login(user);
	}

	/**
	 * 
	 */
	public void showPanel() {
		//hide right sidebar - i.e., page information bar 
		main.setSidebarButtonVisible(ClientConstants.RIGHT, false);
		main.setSidebarVisible(ClientConstants.RIGHT, false);
		
		//get dashboard markup to render service side 
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.getDashboard(false, this);
		
	}
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}

	public void onSuccess(RenderMarkupModel result) {
		if(ErrorCode.hasError(result)){
			main.errorOnLoadingPanel(ErrorCode.getMessageText(result.errorCode,result.errorMsg));
			return;
		}
		
		render.renderContent(null, null, result.renderContent, false);
		
	}

	public void renderEnd(String text) {
		//clean navbar, only left DashBoard
		main.resetNavbar(null);
		main.setPageUuid("");
		main.setPageVersion(0);
		
		main.setPageAttribute(0);
		main.setCurrentPageTitle("");
		main.setSpaceUname("");
		
		main.setPrintBtnVisible(false);
		
		main.switchTo(PageMain.DASHBOARD_PANEL);

	}
	
	public void renderStart() {
		main.loading();
	}

	public void render(String text) {
		
	}

}
