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
package com.edgenius.wiki.gwt.client.home.porlet;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.portal.Portlet;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBar;
import com.edgenius.wiki.gwt.client.widgets.MoreLessButtonBarListener;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class ActivityPortlet extends Portlet implements MoreLessButtonBarListener {
	private VerticalPanel messagePanel = new VerticalPanel();
	private RenderPanel contentPanel = new WikiRenderPanel();
	private MoreLessButtonBar controlBar = new MoreLessButtonBar();
	private PaginationAsync paginationAsync = new PaginationAsync();
	
	public void render() {
		controlBar.addMoreLessButtonBarListener(this);
		
		this.header.addStyleDependentName(Css.DEP_WIDGET);
		this.addStyleDependentName(Css.DEP_WIDGET);
		
		setPortletTitle(getPortletTitle(),getPortletTitle(), null);
		setPortletLogo(new Image(IconBundle.I.get().message()));
		
		contentPanel.setStyleName(Css.ACTIVITIES);

		messagePanel.setWidth("100%");
		container.add(contentPanel);
		container.add(messagePanel);
		//try to get page
		refresh();
	}
	/**
	 * @return
	 */
	public static String getPortletTitle() {
		return Msg.consts.activity_log();
	}

	public void onSuccess(PortletModel model) {
		RenderMarkupModel content = (RenderMarkupModel) model.renderContent;
		callback(model, content);
		
	}
	/**
	 * @param model
	 * @param content
	 */
	private void callback(PortletModel model, RenderMarkupModel content) {
		busy(false);
		messagePanel.clear();
		clearControl();
		
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			//show error message on item part rather than general error message on HTML page above
			messagePanel.add(ErrorCode.getMessage(model.errorCode, model.errorMsg));
			return;
		}
		if(content.renderContent == null || content.renderContent.size() == 0){
			contentPanel.clear();
			Label none = new Label("("+Msg.consts.none()+")");
			none.setStyleName(Css.PORTLET_WEAK_TEXT);
			contentPanel.add(none);
			contentPanel.submit();
		}else{
			PageRender render = new PageRender(contentPanel);
			render.renderContent(null, null,content.renderContent, false);
			
			controlBar.setPaginationInfo(model.hasPre,model.hasNxt,model.currentPage);
			addControl(controlBar);
		}
	}
	public void pageChange(int currentPageNum) {
		controlBar.busy(true);
		HelperControllerAsync helperController = (HelperControllerAsync) ControllerFactory.getHelperController();
		helperController.getActivityLogs(currentPageNum, paginationAsync);
	}
	
	private class PaginationAsync implements AsyncCallback<PortletModel>{
		public void onFailure(Throwable error) {
			controlBar.busy(false);
			GwtClientUtils.processError(error);
		}

		public void onSuccess(PortletModel model) {
			controlBar.busy(false);
			ActivityPortlet.this.callback(model, (RenderMarkupModel)model.renderContent);
		}
		
	}
}
