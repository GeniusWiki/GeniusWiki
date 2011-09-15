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

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.editor.Editor;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.FunctionWidget;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class EditSidebarPanel extends SimplePanel implements AsyncCallback<PageModel>, PanelSwitchListener {
	private Label title = new Label(Msg.consts.edit_sidebar());
	private Editor contentArea;
	private MessageWidget message = new MessageWidget();
	private PageMain main;
	private boolean dirty = false;
	private boolean exitConfirm = true;
	private String currentToken;
	private RadioButton radio1 = new RadioButton("sticky",Msg.consts.homepage_only());
	private RadioButton radio3 = new RadioButton("sticky",Msg.consts.default_sidebar());
	private RadioButton radio2 = new RadioButton("sticky",Msg.consts.current_only());
//	private RadioButton radio4 = new RadioButton("sticky","all pages whatever it has special sidebar or not");
	

	protected FunctionWidget functionBtnBar;
	
	public EditSidebarPanel(PageMain main) {
		this.main = main;
		main.addPanelSwitchListener(this);
		
		contentArea = new Editor(main,false);
		main.editorRegistry.register(contentArea);
		ContentChangeListener changeListener = new ContentChangeListener();
		contentArea.addKeyPressHandler(changeListener);
		contentArea.addChangeHandler(changeListener);
		
		functionBtnBar = new FunctionWidget(main);
		
		VerticalPanel optionPanel = new VerticalPanel(); 
		optionPanel.add(new Label(Msg.consts.stick_sidebar_editing()));
		optionPanel.add(radio1);
		optionPanel.add(radio2);
		optionPanel.add(radio3);
		
		optionPanel.add(new HTML(Msg.consts.sidebar_tip()));
		
		FlexTable mainPanel = new FlexTable();
		mainPanel.setWidget(0, 0, contentArea);
		mainPanel.setWidget(0, 1, optionPanel);
		
		mainPanel.getColumnFormatter().setStyleName(1,Css.SIDEBAR_OPTIONS);
		
	    FlexTable headerPanel = new FlexTable();
	    headerPanel.setWidget(0,0,title);
	    headerPanel.setWidget(0,1,functionBtnBar);
	    
	    title.setStyleName(Css.RENDER_TITLE);
	    mainPanel.getCellFormatter().setAlignment(0, 0,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.getCellFormatter().setAlignment(0, 1,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    
	    //main
		VerticalPanel panel = new VerticalPanel();
		headerPanel.setWidth("100%");
		mainPanel.setWidth("100%");
		optionPanel.setSize("100%","100%");
		panel.setWidth("100%");
		
		panel.add(message);
		panel.add(headerPanel);
		panel.add(mainPanel);
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
		functionBtnBar.loadEditSidebarFunc();
		
		//must turn on sidebar - for preview use
		main.setSidebarButtonVisible(ClientConstants.RIGHT, true);
		main.setSidebarVisible(ClientConstants.RIGHT, true);
		
		//fill for preview
		main.sidebar.fillPanel(model);
		
		contentArea.setFocus(true);
		//must call before setText(), so that content could point to correct editor 
		contentArea.enableRich(model.isRichContent);
		contentArea.setText(model.content == null?"":model.content);

		main.fillEditNavBar(model);
		main.setSpaceUname(model.spaceUname);
		main.setPageUuid(model.pageUuid);
		
		setOption(model.pageThemeType);
		main.switchTo(PageMain.EDIT_SIDEBAR_PANEL);
	}


	public void preview() {
		//only render, no draft saving
		PageControllerAsync action = ControllerFactory.getPageController();
		action.previewSidebar(main.getSpaceUname(),main.getPageUuid(),contentArea.getText(), 
				contentArea.isRichEnabled(), new PreviewAsync());
	}

	public void save() {
		String type = getOption();
		//persist to theme object
		PageControllerAsync action = ControllerFactory.getPageController();
		action.saveSidebar(main.getSpaceUname(),main.getPageUuid(), type,
				contentArea.getText(), contentArea.isRichEnabled(), new SaveAsync());
	}
	
	private void setOption(String pageThemeType) {
		
		if(SharedConstants.SIDEBAR_TYPE_HOME.equals(pageThemeType)){
			radio1.setValue(true);
		}else if(SharedConstants.SIDEBAR_TYPE_DEFAULT.equals(pageThemeType)){
			radio3.setValue(true);
		}else if(SharedConstants.SIDEBAR_TYPE_CURRENT.equals(pageThemeType)){
			radio2.setValue(true);
		}else{
			//default
			radio3.setValue(true);
		}
	}
	private String getOption() {
		String type = SharedConstants.SIDEBAR_TYPE_DEFAULT;
		if(radio1.getValue()){
			type = SharedConstants.SIDEBAR_TYPE_HOME;
		}else if(radio2.getValue()){
			type = SharedConstants.SIDEBAR_TYPE_CURRENT;
		}else if(radio3.getValue()){
			type = SharedConstants.SIDEBAR_TYPE_DEFAULT;
		}
		return type;
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
	private class PreviewAsync implements AsyncCallback<PageModel>{

		public void onFailure(Throwable caught) {
			
		}
		public void onSuccess(PageModel model) {
			//fill for preview
			main.sidebar.fillPanel(model);
			
		}
		
	}
	private class SaveAsync implements AsyncCallback<PageModel>{
		
		public void onFailure(Throwable caught) {
			
		}
		public void onSuccess(PageModel model) {
			exitConfirm = false; 
			dirty = false;
			//fill viewPanel then resetToken to view model (only spaceUname-$SP-PageTtitle)
			main.viewPanel.showPanel(model);
			main.viewPanel.resetToken();
		}
		
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
