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
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.TemplateListModel;
import com.edgenius.wiki.gwt.client.model.TemplateModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.TemplateControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class TemplateListRenderWidget extends SimplePanel implements AsyncCallback<TemplateListModel>, RenderWidget{


	private FlowPanel contentPanel = new FlowPanel();
	private MessageWidget message = new MessageWidget();

	private RenderWidgetListener listener;
	private String componentKey;
	private String spaceUname;
	
	public TemplateListRenderWidget(final String spaceUname) {
		this.spaceUname = spaceUname;
		Button newTemplBtn = new Button(Msg.consts.create_template());
		newTemplBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				GwtClientUtils.refreshToken(GwtUtils.buildToken(PageMain.TOKEN_EDIT_TEMPLATE,spaceUname));
			}
		});
				
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(contentPanel);
		panel.add(new HTML("&nbsp;"));
		panel.add(newTemplBtn);
		panel.setCellHorizontalAlignment(newTemplBtn, HasHorizontalAlignment.ALIGN_CENTER);
		
		this.setWidget(panel);
		panel.setWidth("100%");

	}
	
	public void onFailure(Throwable caught) {
		GwtClientUtils.processError(caught);
		
		listener.onFailedLoad(componentKey, caught.getMessage());
		
	}
	public void onSuccess(TemplateListModel list) {
		if(!GwtClientUtils.preSuccessCheck(list,message)){
			listener.onFailedLoad(componentKey, ErrorCode.getMessageText(list.errorCode, list.errorMsg));
			return;
		}
		contentPanel.clear();
	
		if(list.templates != null && list.templates.size() > 0){
			final TemplateControllerAsync templateController = ControllerFactory.getTemplateController();
			final String spaceUname = list.spaceUname;
			
			ZebraTable listPanel = new ZebraTable(ZebraTable.STYLE_LIST,true);
			contentPanel.add(listPanel);
			int row = 0;
			
			listPanel.setWidget(row, 0, new Label(Msg.consts.name()));
			listPanel.setWidget(row, 1, new Label(Msg.consts.description()));
			listPanel.setWidget(row, 2, new Label(Msg.consts.creator()));
			listPanel.setWidget(row, 3, new Label(Msg.consts.shared()));
			listPanel.setWidget(row, 4, new Label(Msg.consts.actions()));
			listPanel.getColumnFormatter().setWidth(0, "15%");
			listPanel.getColumnFormatter().setWidth(1, "55%");
			listPanel.getCellFormatter().setAlignment(row, 4, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
			row++;
			for(final TemplateModel model : list.templates){
				listPanel.setWidget(row, 0, new Label(model.name));
				listPanel.setWidget(row, 1, new Label(model.desc));
				
				UserProfileLink creator = new UserProfileLink(model.author.getFullname(), spaceUname,model.author.getLoginname(),model.author.getPortrait());
				listPanel.setWidget(row, 2, creator);
				listPanel.setWidget(row, 3, new Label(model.shared?Msg.consts.yes():Msg.consts.no()));
				
				
				FlowPanel actionP = new FlowPanel();
				actionP.setStyleName(Css.NOWRAP);
				ClickLink delLink = new ClickLink(Msg.consts.delete());
				delLink.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						if(Window.confirm(Msg.consts.confirm_delete_templ())){
							templateController.deleteTemplate(spaceUname, model.id,TemplateListRenderWidget.this);
						}
					}
				});
				EventfulHyperLink editLink = new EventfulHyperLink(Msg.consts.edit(), GwtUtils.buildToken(PageMain.TOKEN_EDIT_TEMPLATE,spaceUname, String.valueOf(model.id)));
				actionP.add(delLink);
				actionP.add(new Label(" | "));
				actionP.add(editLink);
				listPanel.setWidget(row, 4, actionP);
			
				row++;
			}
		}else{
			Label no = new Label(Msg.consts.no_template());
			no.setStyleName(Css.BLANK_MSG);
			contentPanel.add(no);
		}
		listener.onSuccessLoad(componentKey, "templateListLoad");
		
	}
	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener){
		this.listener = listener;
		this.componentKey = widgetKey;
		
		listener.onLoading(componentKey);
		TemplateControllerAsync templateController = ControllerFactory.getTemplateController();
		templateController.getTemplates(spaceUname, false, true, this);

	}	
	public void onUserChanged(UserModel user) {
		
	}
	
	
}
