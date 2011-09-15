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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class PreviewPanel  extends DiffPanel implements AsyncCallback<PageModel>{
	private Label previewTitle = new Label();
	private Label previewTag = new Label();
	
	private WikiRenderPanel previewContent = new WikiRenderPanel();

	private FlexTable mainPanel = new FlexTable();
	
	private PageRender render = new PageRender(previewContent);

	private MessageWidget previewMessage = new MessageWidget();
	public PreviewPanel(final PageMain main){
		super(main);
		
	    // Title -- Status
		FlowPanel titlePanel = new FlowPanel();
	    //function buttons
	    titlePanel.add(previewTitle);
	    //draft status bar and function
	    titlePanel.add(functionBtnBar);
	    
	    previewTitle.setStyleName(Css.RENDER_TITLE);
	    previewTag.setStyleName(Css.RENDER_TAG);
	    
	    VerticalPanel panel = new VerticalPanel();
	    panel.add(previewMessage);
	    panel.add(message);
	    panel.add(diffMessage);
		diffMessage.setVisible(false);
		
	    panel.add(titlePanel);
	    panel.add(previewTag);
	    
	    
	    mainPanel.setWidget(0, 0, previewContent);
	    mainPanel.setWidget(0, 1, diffContent);
	    panel.add(mainPanel);

	    mainPanel.getCellFormatter().setAlignment(0, 0,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.getCellFormatter().setAlignment(0, 1,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.setWidth("100%");
	    
	    DOM.setElementAttribute(panel.getElement(), "width", "100%");
	    this.setWidget(panel);
	}
	/*
	 * Also assign title, tagString separate from model.title and model.tagString. 
	 * The reason model.title and model.tagString may may from saved page model. It is better
	 * does not refresh from EditPanel textbox. Otherwise, you must be very careful the method sequence for 
	 * FillPanel() and fillPreview(). FillPanel must call before fillPreview(), otherwise, panel value maybe obsolete 
	 * value 
	 * 
	 */
	public void fillPreview(String title, String tagString, PageModel model) {
		functionBtnBar.loadPreviewFunc(model==null?0:model.attribute);
		if(model == null){
			previewTitle.setText("");
			previewTag.setText("");
			previewContent.clear();
			return;
		}
		previewTitle.setText(title);
		if(tagString == null || tagString.trim().length() == 0)
			tagString = "("+Msg.consts.none()+")";
		
		previewTag.setText(Msg.consts.tags() + ": "+ tagString);
		//render text to  
		render.renderContent(main.getSpaceUname(), model,model.renderContent, true);
		
		previewMessage.cleanMessage();
		previewMessage.warning(Msg.consts.in_preview_mode(), false);
		
		//for Editing exit confirm usage:
		main.editPanel.exitConfirm(true);
		main.editPanel.setCurrentToken(GwtClientUtils.getToken());

		main.sidebar.fillPanel(model);
		
	}


	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}
	/**
	 * SaveDraft success, then load preview...
	 * review is going to load, but found no preview text exist, save draft as preview text then
	 */
	public void onSuccess(PageModel model) {

		if(ErrorCode.hasError(model)){
			main.errorOnLoadingPanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
			return;
		}

		main.setPreviewReady(true, model);
		main.switchTo(PageMain.PREVIEW_PANEL);
	}
	/*
	 * saving version conflict diff: text maybe modified by user choose context menu (accept/deny)
	 */
	protected void diffRendered() {
		super.diffRendered();
		
		functionBtnBar.loadVersionDiff(false);
		diffContent.setVisible(true);
		previewContent.setVisible(false);

	}
	/*
	 * saving version conflict diff: text maybe modified by user choose context menu (accept/deny)
	 */
	protected void diffResume() {
		super.diffResume();
		
		functionBtnBar.resume();
		String diffRs = getDiffMergeResult();
		diffContent.setVisible(false);
		previewContent.setVisible(true);
		if(diffRs != null){
			//user modified diff by context menu, need reset edit area
			main.editPanel.setEditText(diffRs);
			main.setPreviewReady(false,null);
			main.switchTo(PageMain.PREVIEW_PANEL);
		}
	}
}
