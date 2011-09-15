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
package com.edgenius.wiki.gwt.client.render;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class IncludeRenderWidget extends SimplePanel implements RenderWidget, AsyncCallback<PageModel>{

	private RenderPanel renderPanel = new WikiRenderPanel();
	private PageRender render = new PageRender(renderPanel);
	private RenderWidgetListener listener;
	private String componentKey;
	private LinkModel link;
	
	/**
	 * @param link
	 */
	public IncludeRenderWidget(LinkModel link) {
		this.link = link;
		this.setWidget(renderPanel);
	}

	public void onLoad(String widgetKey, UserModel currentUser, RenderWidgetListener listener) {
		this.listener = listener;
		this.componentKey = widgetKey;
		
		listener.onLoading(componentKey);
		
		PageControllerAsync pageController =  ControllerFactory.getPageController();
		//pageTitle@spaceUname#phaseName == link@spaceUname#anchor
		pageController.renderPagePiecePhase(link.getSpaceUname(),link.getLink(),link.getAnchor(),this);
	}

	public void onUserChanged(UserModel user) {
		

	}

	public void onFailure(Throwable error) {
		String err = ErrorCode.getMessageText(ErrorCode.PAGE_PHASE_RENDER_FAILED, null);
		renderPanel.add(GwtUtils.renderErrorText(err));
		renderPanel.submit();
		listener.onFailedLoad(componentKey, err);

	}

	public void onSuccess(PageModel page) {
		if(!GwtClientUtils.preSuccessCheck(page,null)){
			String err = ErrorCode.getMessageText(page.errorCode, page.errorMsg);
			renderPanel.add(GwtUtils.renderErrorText(err));
			renderPanel.submit();
			listener.onFailedLoad(componentKey, err);
			return;
		}
		
		//at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content 
		listener.onSuccessLoad(componentKey, "Included content");
		render.renderContent(page.spaceUname, page, page.renderContent, false);
	}

}
