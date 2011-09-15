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
package com.edgenius.wiki.gwt.client.portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.PageSuggestBox;
import com.edgenius.wiki.gwt.client.widgets.SpaceSuggestBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class PortletCreateDialog extends DialogBox implements AsyncCallback<PortletModel>{

	private static final int PAGE_LINER_INDEX = 0;
	private static final int MARKUP_RENDER_INDEX = 1;
	private static final int DECK_MAIN = 0;
	private static final int DECK_BUSY = 1;
	
	private TextBox title = new TextBox();
	private TextBox desc = new TextBox();
	private RadioButton typePageLinker = new RadioButton("widgetType");
	private RadioButton typeMarkup = new RadioButton("widgetType");
	private CheckBox keepPrivate = new CheckBox();
	
	private SpaceSuggestBox pSpace = new SpaceSuggestBox(null);
	private PageSuggestBox pTitle = new PageSuggestBox();
	private String currSpaceUname = null;
	private TextArea markup = new TextArea();
	
	private MessageWidget message = new MessageWidget();
	private MessageWidget busyMessage = new MessageWidget();
	private DeckPanel deck = new DeckPanel();
	private DeckPanel mainDeck = new DeckPanel();
	
	private ClickHandler typeChangeListener = new TypeChangeListener();
	private Vector<ListDialogueListener> listeners = new Vector<ListDialogueListener>();
	private Image busyImg = IconBundle.I.loading();
	private Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
	
	public PortletCreateDialog(final String key){
		if(key == null){
			this.setText(Msg.consts.create_widget());
		}else{
			this.setText(Msg.consts.edit_widget());
		}
		this.setIcon(new Image(IconBundle.I.get().star()));
		
		FlexTable plinkerPanel = new FlexTable();
		
		pTitle.addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event) {
				String spaceUname = StringUtil.trim(pSpace.getText());
				if(!StringUtil.isBlank(spaceUname) && !StringUtil.equalsIgnoreCase(currSpaceUname, spaceUname)){
					pTitle.request(spaceUname);
					currSpaceUname = spaceUname;
				}
			}
			
		});
		Label l1 = new Label(Msg.consts.space_uname());
		plinkerPanel.setWidget(0, 0, l1);
		plinkerPanel.setWidget(0, 1, pSpace);
		Label l2 = new Label(Msg.consts.page_title());
		plinkerPanel.setWidget(1, 0, l2);
		plinkerPanel.setWidget(1, 1, pTitle);
		
		FlexTable markupPanel = new FlexTable();
		Label l3 = new Label(Msg.consts.title());
		markupPanel.setWidget(0, 0, l3);
		markupPanel.setWidget(0, 1, title);
		title.setStyleName(Css.LONG_INPUT);
		
		Label l4 = new Label(Msg.consts.content());
		markupPanel.setWidget(1, 0, l4);
		markupPanel.setWidget(1, 1, markup);
		markup.setStyleName(Css.LONG_TEXTBOX);
		
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
		
				WidgetModel widget = new WidgetModel();
				widget.key = key;
				widget.description = desc.getText();
				widget.shared = !keepPrivate.getValue();
				
				String error = null;
				String spaceUname = null;
				if(typeMarkup.getValue()){
					widget.type = WidgetModel.TYPE_MARKUP_RENDER;
					widget.title = title.getText();
					widget.content = markup.getText();
					if(StringUtil.isBlank(widget.title)){
						error = "Title ";
					}
					if(StringUtil.isBlank(widget.content)){
						if(error != null)
							error += " or Content";
						else
							error = "Content ";
					}
				}else{
					//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
					//page link widget
					widget.type = WidgetModel.TYPE_PAGE_LINKER;
					spaceUname = StringUtil.trimToEmpty(pSpace.getText());
					widget.title =  StringUtil.trimToEmpty(pTitle.getText());
					//build content - whatever spaceUname or title is correct as this widget object is saved into ValidPageAsync() and save immediately if no errors. 
					widget.content = "{include:src="+EscapeUtil.escapeMacroParam(widget.title)+ "@"+EscapeUtil.escapeMacroParam(spaceUname)+"}";
					//it is bad idea if title is blank, then point to home page: it need lots hack to get back Home title etc. and in exception case, widget even can be blank title
					//this cause user have no chance to delete it anymore --- left rubbish widget on DB. 
					if(StringUtil.isBlank(widget.title)){
						error = "Title ";
					}
					if(StringUtil.isBlank(spaceUname)){
						if(error != null)
							error += " or Space";
						else
							error = "Space ";
					}
				}
				
				if(error != null){
					error += " can not be blank.";
					message.error(error);
					return;
				}
				
				okBtn.setEnabled(false);
				if(typeMarkup.getValue()){
					//for markup content widget, create immediately
					PortalControllerAsync portalController = ControllerFactory.getPortalController();
					portalController.saveOrUpdateWidget(widget, PortletCreateDialog.this);
				}else{
					//for pagelink widget, valid from server side first, then save
					//valid if page exist or not? Valid from server side rather than just use suggest box, this is more exactly
					PageControllerAsync pageController = ControllerFactory.getPageController();
					pageController.exist(spaceUname, widget.title, new ValidPageAsync(widget));
				}
				
			}
		});
		Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				hidebox();
			}
		});
		typePageLinker.addClickHandler(typeChangeListener);
		typeMarkup.addClickHandler(typeChangeListener);
		
		getButtonBar().add(cancelBtn);
		getButtonBar().add(okBtn);
		

		deck.insert(plinkerPanel,PAGE_LINER_INDEX);
		deck.insert(markupPanel,MARKUP_RENDER_INDEX);
		
		fillPanel();
		
		int row = 0;
		FlexTable main = new FlexTable();
		main.getColumnFormatter().setWidth(0, "120px");
		
		main.setWidget(row, 0, message);
		main.getFlexCellFormatter().setColSpan(row, 0, 4);
		row++;
		
		Label l6 = new Label(Msg.consts.introduction());
		main.setWidget(row, 0, l6);
		main.setWidget(row, 1, desc);
		desc.setMaxLength(250);
		desc.setStyleName(Css.LONG_INPUT);
		Label l5 = new Label(Msg.consts.keep_private());
		main.setWidget(row, 2, l5);
		main.getFlexCellFormatter().setAlignment(row, 2, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
		main.setWidget(row, 3, keepPrivate);
		row++;

		HorizontalPanel chooser = new HorizontalPanel();
		Label l7 = new Label(Msg.consts.page_linker());
		Label l8 = new Label(Msg.consts.markup_render());
		chooser.add(typePageLinker);
		chooser.add(l7);
		chooser.add(new HTML("&nbsp;&nbsp;&nbsp;"));
		chooser.add(typeMarkup);
		chooser.add(l8);
		chooser.setCellWidth(typePageLinker, "20px");
		chooser.setCellWidth(typeMarkup, "20px");
		main.setWidget(row,0,chooser);
		main.getFlexCellFormatter().setColSpan(row, 0, 4);
		main.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		row++;
		main.getFlexCellFormatter().setColSpan(row, 0, 4);
		main.getFlexCellFormatter().setHeight(row, 0, "3px");
		row++;
		
		main.setWidget(row, 0, deck);
		main.getFlexCellFormatter().setColSpan(row, 0, 4);
		row++;

		main.setStyleName(Css.FORM);
		title.setStyleName(Css.FORM_INPUT);
		desc.setStyleName(Css.FORM_INPUT);
		pTitle.setStyleName(Css.FORM_INPUT);
		pSpace.setStyleName(Css.FORM_INPUT);
		
		l1.setStyleName(Css.FORM_LABEL);
		l2.setStyleName(Css.FORM_LABEL);
		l3.setStyleName(Css.FORM_LABEL);
		l4.setStyleName(Css.FORM_LABEL);
//		l5.setStyleName(Css.FORM_LABEL);

		l6.setStyleName(Css.FORM_LABEL);
		l7.setStyleName(Css.FORM_LABEL);
		DOM.setElementAttribute(l7.getElement(), "style", "text-align:left");
		l8.setStyleName(Css.FORM_LABEL);
		DOM.setElementAttribute(l8.getElement(), "style", "text-align:left");
		
		main.setSize("100%", "100%");
		deck.setSize("100%", "100%");

		VerticalPanel busyPanel = new VerticalPanel();
		busyPanel.add(busyMessage);
		busyPanel.add(busyImg);
		
		
		mainDeck.insert(main, DECK_MAIN);
		mainDeck.insert(busyPanel, DECK_BUSY);
		if(key == null)
			mainDeck.showWidget(DECK_MAIN);
		else{
			//edit, loading widget info
			mainDeck.showWidget(DECK_BUSY);
			PortalControllerAsync portalController = ControllerFactory.getPortalController();
			portalController.getWidget(key, new LoadingAsync());
		}
		
		this.setWidget(mainDeck);
		
	}
	public void addListDialogueListener(ListDialogueListener listener){
		listeners.add(listener);
	}
	/**
	 * @param deck
	 */
	private void fillPanel() {
		deck.showWidget(PAGE_LINER_INDEX);
		typePageLinker.setChecked(true);
		typeMarkup.setChecked(false);
	}

	public void onFailure(Throwable caught) {
		okBtn.setEnabled(true);
		message.error(Msg.consts.unknown_error());
		
	}

	public void onSuccess(PortletModel portlet) {
		okBtn.setEnabled(true);
		hidebox();
		List<PortletModel> portlets = new ArrayList<PortletModel>();
		portlets.add(portlet);
		
		//fire event, tell observer to update portlet
		for(Iterator<ListDialogueListener> iter = listeners.iterator();iter.hasNext();){
			ListDialogueListener lis =  iter.next();
			lis.dialogClosed(this, portlets);
		}

	}
	
	private class TypeChangeListener implements ClickHandler{
		public void onClick(ClickEvent evt) {
			if(evt.getSource() == typeMarkup){
				deck.showWidget(MARKUP_RENDER_INDEX);
			}else if(evt.getSource()  == typePageLinker){
				deck.showWidget(PAGE_LINER_INDEX);
			}
		}
		
	}
	
	private class LoadingAsync implements AsyncCallback<WidgetModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			busyMessage.error(Msg.consts.load_widget_fail());
			
		}
		public void onSuccess(WidgetModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,busyMessage)){
				return;
			}
			//fill info
			desc.setText(model.description);
			if(model.type == WidgetModel.TYPE_MARKUP_RENDER){
				markup.setText(model.content);
				title.setText(model.title);
				typePageLinker.setChecked(false);
				typeMarkup.setChecked(true);
				deck.showWidget(MARKUP_RENDER_INDEX);
				
			}else if(model.type == WidgetModel.TYPE_PAGE_LINKER){
				pSpace.setText(model.description);
				pTitle.setText(model.title);
				typePageLinker.setChecked(true);
				typeMarkup.setChecked(false);
				deck.showWidget(PAGE_LINER_INDEX);
			}
			
			//go back edit panel
			mainDeck.showWidget(DECK_MAIN);
			
		}
	}
	
	private class ValidPageAsync implements AsyncCallback<Boolean>{
		
		private WidgetModel widget;
		public ValidPageAsync(WidgetModel widget) {
			this.widget = widget;
		}
		public void onFailure(Throwable error) {
			okBtn.setEnabled(true);
			GwtClientUtils.processError(error);
			message.error(Msg.consts.verfy_page_exist_fail());
			
		}
		public void onSuccess(Boolean exist) {
			
			if(exist){
				//create widget
				PortalControllerAsync portalController = ControllerFactory.getPortalController();
				portalController.saveOrUpdateWidget(widget, PortletCreateDialog.this);
			}else{
				okBtn.setEnabled(true);
				message.error(Msg.consts.page_not_exist_fail());
			}
		}
	}
}
