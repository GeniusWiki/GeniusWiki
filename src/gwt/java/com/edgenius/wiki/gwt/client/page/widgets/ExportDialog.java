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
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class ExportDialog extends DialogBox implements ClickHandler, AsyncCallback<String>{
	private static final int MAIN_DECK = 0;
	private static final int BUSY_DECK = 1;
	private MessageWidget message = new MessageWidget();
	private Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
	private Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	private DeckPanel deck = new DeckPanel();
	private PageMain pageMain;
	private RadioButton pdfF = new RadioButton("format");
	private RadioButton htmlF= new RadioButton("format");
	private RadioButton scopeS = new RadioButton("scope");
	private RadioButton scopeP = new RadioButton("scope");
	
	public ExportDialog(PageMain pageMain){
		this.pageMain = pageMain;
		this.setIcon(new Image(IconBundle.I.get().export()));
		this.setText(Msg.consts.export());
		
		ButtonBar btnPanel = getButtonBar();
		btnPanel.add(cancelBtn);
		btnPanel.add(okBtn);
		okBtn.addClickHandler(this);
		cancelBtn.addClickHandler(this);
		
		Label l1 = new Label(Msg.consts.format());
		Label l2 = new Label(Msg.consts.scope());

		pdfF.setText("PDF"); //NON-i18n
		htmlF.setText("HTML");  //NON-i18n
		scopeS.setText(Msg.consts.whole_space());
		scopeP.setText(Msg.consts.current_page());
		
		
		l1.setStyleName(Css.FORM_LABEL);
		l2.setStyleName(Css.FORM_LABEL);
		
		FlexTable main = new FlexTable();
		main.setWidget(0, 0, l1);
		main.setWidget(0, 1, htmlF);
		//TODO: temporary comment as PDF is not ready...
//		main.setWidget(0, 2, pdfF);
		
		main.setWidget(1, 0, l2);
		main.setWidget(1, 1, scopeS);
		main.setWidget(1, 2, scopeP);
		
		main.getFlexCellFormatter().setWidth(0, 0, "130px");
		main.getFlexCellFormatter().setWidth(0, 1, "130px");
		
		htmlF.setValue(true);
		scopeS.setValue(true);
		
		HorizontalPanel busy = new HorizontalPanel();
		Image busyImg = new Image(IconBundle.I.get().loadingBar());
		busy.add(busyImg);
		busy.setCellHorizontalAlignment(busyImg, HasHorizontalAlignment.ALIGN_CENTER);
		busy.setCellVerticalAlignment(busyImg, HasVerticalAlignment.ALIGN_BOTTOM);
		busy.setHeight("200px");
		
		deck.insert(main, MAIN_DECK);
		deck.insert(busy, BUSY_DECK);
		deck.setSize("98%", "100%");
		deck.showWidget(MAIN_DECK);
		
		VerticalPanel panel = new VerticalPanel();
		panel.setCellHorizontalAlignment(message, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(deck, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setWidth("100%");
		panel.add(message);
		panel.add(deck);
		
		this.setWidget(panel);
	}

	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if(sender == cancelBtn){
			this.hidebox();
		}else if(sender == okBtn){
			okBtn.setBusy(true);
			deck.showWidget(BUSY_DECK);
			int type = SharedConstants.EXPORT_TYPE_HTML;
			if(pdfF.getValue()){
				type = SharedConstants.EXPORT_TYPE_PDF;
			}else if(htmlF.getValue()){
				type = SharedConstants.EXPORT_TYPE_HTML;
			}
			
			String pageUuid = null;
			if(scopeP.getValue()){
				pageUuid = pageMain.getPageUuid();
			}
			//call server side
			PageControllerAsync action = ControllerFactory.getPageController();
			action.export(pageMain.getSpaceUname(), pageUuid, type, this);
		}
	}

	public void onFailure(Throwable error) {
		okBtn.setBusy(false);
		deck.showWidget(MAIN_DECK);
		GwtClientUtils.processError(error);
		message.error(Msg.consts.fail_export());
	}

	public void onSuccess(String result) {
		if(result != null){
			pageMain.download(result);
			this.hidebox();
		}else{
			//failed
			okBtn.setBusy(false);
			deck.showWidget(MAIN_DECK);
			message.error(Msg.consts.error_request());
		}
		
	}
}
