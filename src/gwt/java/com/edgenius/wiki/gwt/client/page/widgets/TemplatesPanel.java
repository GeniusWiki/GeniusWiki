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
package com.edgenius.wiki.gwt.client.page.widgets;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.TemplateListModel;
import com.edgenius.wiki.gwt.client.model.TemplateModel;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.page.EditPanel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.TemplateControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.CloseButton;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class TemplatesPanel extends SimplePanel implements AsyncCallback<TemplateListModel>, ClickHandler{
	private FlowPanel content = new FlowPanel();
	private String loadedSpace;
	private Image busy = IconBundle.I.indicator();

	private MessageWidget message = new MessageWidget();
	private EditPanel editPanel;
	private CloseButton closeBtn = new CloseButton();
	public TemplatesPanel(PageMain main, EditPanel editPanel){
		this.editPanel = editPanel;

		closeBtn.addClickHandler(this);
		HorizontalPanel header = new HorizontalPanel();
		header.add(new HTML("<b>"+ Msg.consts.templates() + "</b>"));
		header.add(message);
		header.add(closeBtn);
		header.setCellHorizontalAlignment(closeBtn, HasHorizontalAlignment.ALIGN_RIGHT);
		
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		content.setWidth("100%");
		header.setWidth("100%");
		panel.add(header);
		panel.add(content);
		this.setWidget(panel);
		this.setStyleName(Css.ATTACHMNET_PANEL);
	}

	public void initPanel() {
		this.setVisible(false);
		loadedSpace = null;
	}
	public void toggle() {
		if(this.isVisible()){
			this.setVisible(false);
			return;
		}
		
		message.cleanMessage();
		load();
		this.setVisible(true);
		
	}
	private void load(){
		//for same space, don't load every time?
		if(StringUtil.equals(loadedSpace,PageMain.getSpaceUname()))
			return;
		
		content.clear();
		content.add(busy);
		TemplateControllerAsync templateController = ControllerFactory.getTemplateController();
		templateController.getTemplates(PageMain.getSpaceUname(), true, false, this);
	
	}
	public void onClick(ClickEvent event) {
		if(event.getSource() == closeBtn){
			toggle();
		}
	}

	public void onFailure(Throwable caught) {
		GwtClientUtils.processError(caught);
	}

	public void onSuccess(TemplateListModel result) {
		if(!GwtClientUtils.preSuccessCheck(result,message)){
			return;
		}
		
		content.clear();

        if(result.templates.size() > 0){
        	ZebraTable table = new ZebraTable();
			loadedSpace = PageMain.getSpaceUname();
			int row = 0;
			table.setWidget(row, 0, new Label(Msg.consts.name()));
			table.setWidget(row, 1, new Label(Msg.consts.description()));
			table.setWidget(row, 2, new Label(Msg.consts.from_space()));
			row++;
			
			for (final TemplateModel templ : result.templates) {
				ClickLink select = new ClickLink(templ.name);
				select.setTitle(Msg.consts.select_template());
				select.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						if(editPanel.isDirty()){
							if(!Window.confirm(Msg.consts.replace_content_by_templ()))
								return;
						}
						TemplateControllerAsync templateController = ControllerFactory.getTemplateController();
						templateController.getTemplate(PageMain.getSpaceUname(),templ.id, new AdoptTemplAsnyc());
						
					}
				});
				table.setWidget(row, 0, select);
				table.setWidget(row, 1, new Label(templ.desc));
				table.setWidget(row, 2,new Label(templ.fromSpace));
				row++;
			}
			content.add(table);
        }else{
        	Label msg = new Label(Msg.consts.no_template());
        	msg.setStyleName(Css.BLANK_MSG);
        	content.add(msg);
        }
	}
	
	private class AdoptTemplAsnyc implements AsyncCallback<TextModel>{

		public void onFailure(Throwable caught) {
			GwtClientUtils.processError(caught);
			
		}

		public void onSuccess(TextModel result) {
			if(!GwtClientUtils.preSuccessCheck(result,message)){
				return;
			}
			editPanel.setEditText(result.getText());
		}
		
	}

}
