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

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.editor.Editor;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.TemplateListModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.FunctionWidget;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.TemplateControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class EditTemplatePanel extends SimplePanel implements AsyncCallback<PageModel>, PanelSwitchListener {
	
	protected static final int TITLE_MAX_LEN = 80;
	private MessageWidget message = new MessageWidget();
	private PageMain main;
	private HintTextBox title = new HintTextBox(Msg.consts.title());
	private HintTextBox desc = new HintTextBox(Msg.consts.description());
	private CheckBox shared = new CheckBox(Msg.consts.sharing_all_spaces());
	private Editor contentArea;
	private Label status = new Label();
	private Label previewTitle = new Label();
//	private Label previewDesc = new Label();
	private WikiRenderPanel previewContent = new WikiRenderPanel();
	private FunctionWidget functionBtnBar;
	private FunctionWidget functionBtnBarPreview;
	private DeckPanel deck = new DeckPanel();
	private boolean dirty = false;
	private boolean exitConfirm = true;
	private String currentToken;
	private int templID;
	
	public EditTemplatePanel(PageMain main) {
		this.main = main;
		main.addPanelSwitchListener(this);
		contentArea = new Editor(main,false);
		main.editorRegistry.register(contentArea);
		
		ContentChangeListener changeListener = new ContentChangeListener();
		contentArea.addKeyPressHandler(changeListener);
		contentArea.addChangeHandler(changeListener);
		
		functionBtnBar = new FunctionWidget(main);
		functionBtnBarPreview = new FunctionWidget(main);
		title.addKeyPressHandler(new KeyPressHandler(){
			public void onKeyPress(KeyPressEvent event) {
				String text = title.getText();
				if(text.length() > TITLE_MAX_LEN){
					title.setText(text.substring(0,TITLE_MAX_LEN));
					status.setText(Msg.params.title_length(""+TITLE_MAX_LEN));
				}
				if(validSave()){
					status.removeStyleName(Css.ERROR);
					title.removeStyleName(Css.ERROR);
					status.setText("");
				}
			}
	    });
		FlexTable headerPanel = new FlexTable();
		FlowPanel f1 = new FlowPanel();
		f1.add(title);
		f1.add(status);
		headerPanel.setWidget(0,0,f1);
	    headerPanel.setWidget(0,1,functionBtnBar);
	    title.setStyleName(Css.TITLE_BOX);
	    
	    FlowPanel f2 = new FlowPanel();
		f2.add(desc);
		f2.add(shared);
	    headerPanel.setWidget(1,0,f2);
	    headerPanel.getFlexCellFormatter().setColSpan(1, 0, 2);
	    desc.setStyleName(Css.DESC);
	    
	    
	    headerPanel.setStyleName(Css.TEMPLATE_HEADER);
	    headerPanel.getRowFormatter().setStyleName(0,Css.EDIT_TITLE_PANEL);
	    headerPanel.getRowFormatter().setStyleName(1,Css.EDIT_TAG_PANEL);

	    //main
		VerticalPanel panel = new VerticalPanel();
		headerPanel.setWidth("100%");
		panel.setWidth("100%");
		
		VerticalPanel editPanel = new VerticalPanel();
		editPanel.add(headerPanel);
		editPanel.add(contentArea);
		editPanel.setWidth("100%");
		
		VerticalPanel previewPanel = new VerticalPanel();
		FlexTable previewHeaderPanel = new FlexTable();
		previewHeaderPanel.setWidget(0, 0, previewTitle);
		previewHeaderPanel.setWidget(0, 1, functionBtnBarPreview);
//		previewHeaderPanel.setWidget(1, 0, previewDesc);
//		previewHeaderPanel.getFlexCellFormatter().setColSpan(1, 0, 2);
		previewHeaderPanel.setWidth("100%");
		previewPanel.setWidth("100%");
		
		previewPanel.add(previewHeaderPanel);
		previewPanel.add(previewContent);
		
		previewPanel.setStyleName(Css.PREVIEW);
		previewTitle.setStyleName(Css.RENDER_TITLE);
//		previewDesc.setStyleName(Css.HEADING3);
		
		deck.insert(editPanel,0);
		deck.insert(previewPanel,1);
		deck.showWidget(0);
		
		panel.add(message);
		panel.add(deck);
		this.setWidget(panel);
		
		
	}

	public void login(UserModel user) {
		//nothing to do
	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		message.error(Msg.consts.loading_edit_panel_failed());
	}

	public void onSuccess(PageModel model) {
		
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			main.errorOnLoadingPanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
			return;
		}

		dirty = false;
		currentToken = GwtClientUtils.getToken();

		showPanel(model);
		
	}
	private void showPanel(PageModel model) {
		functionBtnBar.loadEditTemplateFunc();
		//although make focus is good practice, but current hint text box doesn't support highlight first...
//		title.setFocus(true);
		
		if(model != null){
			//must call before setText(), so that content could point to correct editor 
			contentArea.enableRich(model.isRichContent);
			contentArea.setText(model.content == null?"":model.content);
			title.setText(model.title);
			desc.setText(model.tagString);
			//damn hack: user history to identify if this template shared 
			shared.setValue(model.isHistory);
			main.fillEditNavBar(model);
			this.templID = model.uid;
		}
		
		main.setSpaceUname(model.spaceUname);
		main.setPageUuid(model.pageUuid);
		deck.showWidget(0);
		main.switchTo(PageMain.EDIT_TEMPLATE_PANEL);
	}


	public void preview() {
		//render to preview
		TemplateControllerAsync action = ControllerFactory.getTemplateController();
		action.previewTemplate(main.getSpaceUname(), contentArea.getText(), contentArea.isRichEnabled(),new PreviewAsync());
	}

	public void save() {
		if(!validSave()){
			return;
		}
		//persist to theme object
		TemplateControllerAsync action = ControllerFactory.getTemplateController();
		action.saveTemplate(main.getSpaceUname(),this.templID, title.getText(),desc.getText(), contentArea.getText(), 
				contentArea.isRichEnabled(), shared.getValue(), new SaveAsync());
	}
	
	public void cancel() {
		// return to template list
		GwtClientUtils.refreshToken(FunctionWidget.viewTemplateListToken(main.getSpaceUname()));
		
	}
	private boolean validSave() {
		if(StringUtil.isBlank(title.getText())){
			status.setText(Msg.consts.err_empty_title());
			status.addStyleName(Css.ERROR);
			title.addStyleName(Css.ERROR);
			return false;
		}
		return true;
	}
	public void togglePreview(){
		int idx = deck.getVisibleWidget();
		idx = idx==0?1:0;
		deck.showWidget(idx);
	}
	private class PreviewAsync implements AsyncCallback<PageModel>{

		public void onFailure(Throwable caught) {
			GwtClientUtils.processError(caught);
		}
		public void onSuccess(PageModel model) {
			PageRender render = new PageRender(previewContent);
			//returned pageModel only has PageModel.sidebarRenderContent
			render.renderContent(main.getSpaceUname(), model, model.renderContent,true);
			
			previewTitle.setText(title.getText());
//			previewDesc.setText(desc.getText());
			
			functionBtnBarPreview.loadPreviewTemplateFunc();
			togglePreview();
		}
		
	}
	private class SaveAsync implements AsyncCallback<TemplateListModel>{
		
		public void onFailure(Throwable caught) {
			GwtClientUtils.processError(caught);
		}
		public void onSuccess(TemplateListModel model) {
			exitConfirm = false;
			dirty = false;
			//display templates page
			GwtClientUtils.refreshToken(GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TEMPLATE_LIST) ,model.spaceUname));
			
		}
		
	}


	public void onPanelSwitched(Widget src, int toIndex) {
	}

	public boolean onPanelSwitching(Widget src, int toIndex) {
		if(src != this)
			return true;
		
		if(currentToken != null && dirty && exitConfirm){
			if(!Window.confirm(Msg.consts.confirm_exit())){
				//restore current editing token...
				History.newItem(currentToken, false);
				//stop switch panel
				return false;
			}
		}
		//restore to true - it maybe false
		exitConfirm = true;
		
		return true;
	}

	private class ContentChangeListener  implements KeyPressHandler, ChangeHandler{

		private void touch() {
			if(!dirty){
				//onBeforeUnload() to need user confirm before close browser window
				GwtClientUtils.onBeforeUnload(Msg.consts.confirm_exit_editing());
			}
			
			//mark user edit this page flag, so that give a chance to ask user exit during saving
			dirty = true;
			
		}


		//rich editor cannot detect keyPress correctly - menu bar evnet won't trigger onKeyPress(), so alreay dirty on rich editor..  
		public void onChange(ChangeEvent event) {
			touch();
		}

		public void onKeyPress(KeyPressEvent event) {
			touch();
		}

	}



}
