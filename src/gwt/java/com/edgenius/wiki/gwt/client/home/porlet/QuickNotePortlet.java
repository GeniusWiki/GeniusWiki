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
import com.edgenius.wiki.gwt.client.model.QuickNoteModel;
import com.edgenius.wiki.gwt.client.portal.Portlet;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.AutoResizeTextArea;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class QuickNotePortlet extends Portlet implements ClickHandler, BlurHandler{
	
	private DeckPanel deck = new DeckPanel();
	private FocusPanel panel = new FocusPanel();
	private FlowPanel textPanel = new FlowPanel();
	private AutoResizeTextArea textArea = new AutoResizeTextArea();
	private String noteContent;
	private HandlerRegistration handlerReg;
	
	public void render() {

		this.header.addStyleDependentName(Css.DEP_WIDGET);
		this.addStyleDependentName(Css.DEP_WIDGET);
		
		setPortletTitle(getPortletTitle(),getPortletTitle(), null);
		setPortletLogo(new Image(IconBundle.I.get().note()));
		
		FlowPanel areaPanel = new FlowPanel();
		textArea.setStyleName(Css.QUICK_NOTE_TEXTAREA);
		textArea.addBlurHandler(this);
		//deckPanel auto set width/height 100% to its direct children,  
		//so here is turn around put textArea into FlowPanel, then to deckPanel 
		areaPanel.add(textArea);
		
		deck.insert(textPanel,0);
		deck.insert(areaPanel,1);
		deck.showWidget(0);
		
		panel.setWidget(deck);
		container.add(panel);
		
		refresh();
	}

	public void onSuccess(PortletModel model) {
		callback((QuickNoteModel) model.renderContent);
		
	}

	/**
	 * @return
	 */
	public static String getPortletTitle() {
		return Msg.consts.quick_note();
	}

	

	public void onClick(ClickEvent event) {
		//show edit area
		setPortletLogo(new Image(IconBundle.I.get().note_edit()));
		deck.showWidget(1);

		//FocusPanel conflict with this textarea when mouse click, so remove it first
		if(handlerReg != null){
			handlerReg.removeHandler();
			handlerReg = null;
		}
		textArea.setText(noteContent);
		textArea.setFocus(true);
	}

	public void onBlur(BlurEvent event) {
		busy(true);
		//save 
		SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
		securityController.saveUserQuickNote(textArea.getText(),new SaveNoteAsync());
	}
	
	//********************************************************************
	//               private methods
	//********************************************************************
	private class SaveNoteAsync implements AsyncCallback<QuickNoteModel>{

		public void onFailure(Throwable error) {
			busy(false);
			GwtClientUtils.processError(error);
		}
		public void onSuccess(QuickNoteModel result) {
			callback(result);
		}
		
	}
	private void callback(QuickNoteModel note) {
		
		busy(false);
		setPortletLogo(new Image(IconBundle.I.get().note()));
		if(handlerReg == null){
			handlerReg = panel.addClickHandler(this);
		}
		//show view text
		deck.showWidget(0);
		renderText(note);
		
	}

	/**
	 * @param note
	 */
	private void renderText(QuickNoteModel note) {
		textPanel.clear();
		if(note == null || StringUtil.isBlank(note.content)){
			Label lb = new Label(Msg.consts.quick_note_default_msg());
			textPanel.add(lb);
			
			noteContent = "";
		}else{
			RenderPanel renderPanel = new WikiRenderPanel();
			textPanel.add(renderPanel);
			
			PageRender render = new PageRender(renderPanel);
			render.renderContent(null, null, note.renderContent, false);
			
			noteContent = note.content;
		}
	}
}
