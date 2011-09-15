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
package com.edgenius.wiki.gwt.client.instance;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.RenderMarkupModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderContentListener;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.Hr;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.LazyLoadingPanel;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class InstanceDashboardPanel extends SimplePanel implements RenderContentListener, AsyncCallback<RenderMarkupModel>, ClickHandler, LazyLoadingPanel{
	
	private static final int DECK_RENDER = 0;
	private static final int DECK_BUSY = 1;
	
	private RenderPanel renderPanel = new WikiRenderPanel();
	private PageRender render = new PageRender(renderPanel);
	private MessageWidget message = new MessageWidget();
	private TextArea editor = new TextArea();
	private DeckPanel deck = new DeckPanel();
	
	private Button previewBtn = new Button(Msg.consts.preview());
	private Button publishBtn = new Button(Msg.consts.publish());
	private Button cancelBtn = new Button(Msg.consts.cancel());
	private Button defaultBtn = new Button(Msg.consts.defaultK());
	private ClickLink editLink = new ClickLink(Msg.consts.edit());

	private boolean fillText = true;
	
	private HorizontalPanel editPanel = new HorizontalPanel();
	
	public InstanceDashboardPanel(){
		render.addRenderContentListener(this);
		render.registerPortalVistor(new InstancePortalVisitor());

		previewBtn.addClickHandler(this);
		publishBtn.addClickHandler(this);
		cancelBtn.addClickHandler(this);
		defaultBtn.addClickHandler(this);
		editLink.addClickHandler(this);
		
		VerticalPanel barPanel = new VerticalPanel();
		barPanel.setSpacing(5);
		barPanel.add(previewBtn);
		barPanel.add(publishBtn);
		barPanel.add(cancelBtn);
		barPanel.add(defaultBtn);
	
		editPanel.add(editor);
		editPanel.add(barPanel);
		
		//This panel is just because deck make its children to 100% width, which enlarge the indicator icon
		FlowPanel busyPanel = new FlowPanel();
		busyPanel.add(IconBundle.I.indicator());
		
		deck.insert(renderPanel,DECK_RENDER);
		deck.insert(busyPanel,DECK_BUSY);

		FlowPanel titleBar = new FlowPanel();
		HTML l2 = new HTML("<span class='"+ Css.HEADING2 +"'>" + Msg.consts.dashboard_preview() + "</span>");
		
		titleBar.add(l2);
		titleBar.add(new HTML(" - "));
		titleBar.add(editLink);
		
		VerticalPanel main = new VerticalPanel();
		main.setSpacing(10);
		main.add(message);
		main.add(editPanel);
		main.add(titleBar);
		main.add(new Hr());
		main.add(deck);
		this.setWidget(main);
		
		barPanel.setStyleName(Css.BUTTONS);
		barPanel.addStyleName(Css.EQUALS_WIDTH_BUTTON);
		
		renderPanel.removeStyleName(Css.RENDER_CONTENT);
		DOM.setElementAttribute(main.getElement(), "width", "98%");
		editor.setStyleName(Css.DASHMARKUP_TEXTBOX);
		
		
		//get dashboard markup to render service side
		editPanel.setVisible(false);
		deck.showWidget(DECK_BUSY);
	}

	public void load() {
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.getDashboard(true, this);
	}


	public void renderEnd(String text) {
		message.cleanMessage();
		deck.showWidget(DECK_RENDER);
	}

	public void renderStart() {
		
	}

	public void render(String text) {
		
	}
	
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		
	}

	public void onSuccess(RenderMarkupModel result) {
		if(ErrorCode.hasError(result)){
			message.error(result.errorCode);
			return;
		}
		if(fillText){
			//first time enter or cancel - fill textArea with service side code
			fillText = false;
			editor.setText(result.markup);
		}
		//As user don't have permission, so only display "more spaces" button but not "create space" and "system admin"
		//this is desired results...
		UserModel user = new UserModel();
		user.setUid(result.loginUserUid);
		render.login(user);
		
		render.renderContent(null, null, result.renderContent, false);
		
	}



	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		if(sender == previewBtn){
			deck.showWidget(DECK_BUSY);
			portalController.previewDashboard(editor.getText(), this);
		}else if(sender == cancelBtn){
			if(Window.confirm(Msg.consts.confirm_cancel())){
				fillText = true;
				deck.showWidget(DECK_BUSY);
				portalController.getDashboard(true, this);
				editPanel.setVisible(false);
			}
		}else if(sender == defaultBtn){
			if(Window.confirm(Msg.consts.return_default())){
				editor.setText(SharedConstants.DEFAULT_DAHSBOARD_MARKUP);
				deck.showWidget(DECK_BUSY);
				portalController.saveDashboard(editor.getText(), this);
				editPanel.setVisible(false);
			}
		}else if(sender == publishBtn){
			if(Window.confirm(Msg.consts.confirm_publish_dashboard())){
				deck.showWidget(DECK_BUSY);
				portalController.saveDashboard(editor.getText(), this);
				editPanel.setVisible(false);
			}
		}else if(sender == editLink){
			editPanel.setVisible(true);
		}
	}


}
