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

import java.util.Date;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.BaseEntryPoint;
import com.edgenius.wiki.gwt.client.Callback;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.constant.PageSaveMethod;
import com.edgenius.wiki.gwt.client.editor.Editor;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentButton;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.page.widgets.CaptchaDialog;
import com.edgenius.wiki.gwt.client.page.widgets.SpacePagesDialog;
import com.edgenius.wiki.gwt.client.page.widgets.TemplatesPanel;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MessageDialog;
import com.edgenius.wiki.gwt.client.widgets.MessageDialogListener;
import com.edgenius.wiki.gwt.client.widgets.TagSuggestBox;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class EditPanel  extends DiffPanel implements AsyncCallback<PageModel>, PanelSwitchListener{

	//title cannot over 250 char
	private static final int TITLE_MAX_LEN = SharedConstants.TITLE_MAX_LEN;
	//30s to save draft
	private static final int SAVE_DRAFT_TIMEOUT = 30000;
	
	private TextBox titleBox = new TextBox();
	private TagSuggestBox tagBox = new TagSuggestBox(TagSuggestBox.TYPE_PAGE_TAG);
	private Label draftStatusBar = new Label();
	
	private Editor contentArea;
	
    //attachment panel

	private AttachmentPanel attPanel;
	private TemplatesPanel templatePanel;
	private AttachmentButton attachmentBtn;
	private ClickLink templateBtn = new ClickLink(Msg.consts.templates());
	private CheckBox noticeCheckbox = new CheckBox(Msg.consts.send_notify());
	
	private Timer saveDraftTimer = new SaveDraftTimer();
	//auto save draft time if set or not
	private boolean saveDraftTimerSet = false;
	private String autoSaveMsgUuid = null;
	private boolean dirty = false;
	private boolean exitConfirm = true;
	private String currentToken;
	
	public EditPanel(final PageMain main){
		super(main);
		
		main.addPanelSwitchListener(this);
		
		contentArea = new Editor(main,false);
		main.editorRegistry.register(contentArea);
		
		titleBox.setTabIndex(1);
		if(!AbstractEntryPoint.isOffline()){
			tagBox.setTabIndex(2);
		}
		contentArea.setTabIndex(3);
		tagBox.setHint(Msg.consts.tags()+"...");
		
	    titleBox.addBlurHandler(new BlurHandler(){
			public void onBlur(BlurEvent event) {
				//display the final saved title
				titleBox.setText(StringUtil.shrinkSpaces(titleBox.getText()));

				validSave();
			}
	    });

	    
	    if(!AbstractEntryPoint.isOffline()){
	    	templatePanel = new TemplatesPanel(main,this);
	    	templatePanel.setVisible(false);
	    	templateBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					//toggle template panel
					templatePanel.toggle();
				}
	    	});
	    }

//	    Label titleLabel = new Label(Msg.consts.title());

		//attachment
		attPanel = new AttachmentPanel(main,false);
		attachmentBtn = new AttachmentButton(attPanel);
		contentArea.bindAttachmentPanel(attPanel);
		
		//must put below above 2 sentences: attPanel is created 
		main.registerAttachmentPanel(attPanel);
		
	    //tag, att, location, notification 
//	    Label tagLabel = new Label(Msg.consts.tags());
	    noticeCheckbox.setValue(true);
	    
	    
	    //content
	    ContentChangeListener changeListener = new ContentChangeListener();
	    contentArea.addKeyPressHandler(changeListener);
	    contentArea.addChangeHandler(changeListener);
	    
	  //hide first, show when fillPanel() according to PageAttribute.NO_ATTACHMENT
		attachmentBtn.setVisible(false);
	    //hide attachment initial
	    attPanel.setVisible(false);

	    titleBox.setStyleName(Css.TITLE_BOX);
	    
	    draftStatusBar.setStyleName(Css.DRAFT_STATUS_MSG); 
	    
	    //main content panel, contains contentPanel, diffContent and SideBar panel.
		FlexTable mainPanel = new FlexTable();
		mainPanel.setCellPadding(0);
		mainPanel.setCellSpacing(0);
	    mainPanel.setWidget(0, 0, contentArea);
	    mainPanel.setWidget(0, 1, diffContent);
	    
	    mainPanel.getCellFormatter().setAlignment(0, 0,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.getCellFormatter().setAlignment(0, 1,HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
	    mainPanel.setWidth("100%");
	    
	    diffMessage.setVisible(false);
	    //build panel
		HTMLPanel hpanel = new HTMLPanel(readEditLayout());
		hpanel.add(message, "_message");
		hpanel.add(diffMessage, "_diff_message");
		hpanel.add(titleBox, "_title");
		hpanel.add(draftStatusBar, "_draft_message");
		if(!AbstractEntryPoint.isOffline()){
			hpanel.add(tagBox, "_tags");
			hpanel.add(noticeCheckbox, "_notification");
		}
		hpanel.add(functionBtnBar, "_actions");
		hpanel.add(attachmentBtn, "_attchments");
		hpanel.add(attPanel, "_attchments_panel");
		if(!AbstractEntryPoint.isOffline()){
			templateBtn.setStyleName(Css.ATT_BTN);
			hpanel.add(templateBtn, "_templates");
			hpanel.add(templatePanel, "_templates_panel");
		}
		hpanel.add(mainPanel, "_body");
		
	    this.setWidget(hpanel);
	}
	
	public void login(UserModel user) {
		//so far, nothing to do
	}
	/**
	 * set new context in text-area box for editing 
	 * @param text
	 */
	public void setEditText(String text){
		contentArea.setText(text);
	}
	public boolean isRichEditor(){
		return contentArea.isRichEnabled();
	}
	/**
	 * Set panel as visible.
	 * @param model
	 */
	public void showPanel(PageModel model){
		message.cleanMessage();
		//user maybe save from Version Conflict panel, so, try to resume from it so that ShowPanel always can show edit ContentArea.
		diffResume();
		//model never expect is null: create new page will bring title and edit content loaded
		fillPanel(model);
		main.switchTo(PageMain.EDIT_PANEL);
		
		if((model.newPageType == PageAttribute.NEW_PAGE || model.newPageType == PageAttribute.NEW_HOMEPAGE)
				&& titleBox.getText() != null && titleBox.getText().length() > 1){
			//mark text as selected for user easily update title
			titleBox.setFocus(true);
			titleBox.setSelectionRange(0, titleBox.getText().length());
		}else{
			contentArea.setFocus(true);
		}
		
	}
    public void saveDraft(PageSaveMethod saveDraftType) {
        saveDraft(saveDraftType, null);
    }
	/**
	 * Following case will invoke saveDraft() method:
	 * 
	 * <li>Preview is going to load, but found no preview text exist, save draft as preview text then - SAVE_DRAFT_LOAD_PREVIEW</li>
	 * <li>Attachment is going to upload, and found no draft exist - SAVE_DRAFT_STAY_IN_EDIT</li>
	 * <li>save draft timer invoke - SAVE_DRAFT_STAY_IN_EDIT</li>
	 * <li>User choose "save draft" button - SAVE_DRAFT_EXIT_TO_VIEW</li>
	 * 
	 * Save draft and go to page view: only user click "Save draft" button.
	 * Save draft will load preview panel if first case.
	 * 
	 * Callback is optional and will return pageUuid
	 */
	public void saveDraft(PageSaveMethod saveDraftType, Callback<String> callback) {
		if(!validSave()){
			saveFunctionDone();
			if(saveDraftType == PageSaveMethod.SAVE_DRAFT_LOAD_PREVIEW){
				main.switchTo(PageMain.EDIT_PANEL);
			}
			return;
		}
		cancelAutoSave();
		//clean any the possible message of resuming auto-saved, because another auto save will replace old one 
		if(autoSaveMsgUuid != null){
			message.removeMessage(autoSaveMsgUuid);
			autoSaveMsgUuid = null;
		}

		PageControllerAsync action = ControllerFactory.getPageController();
		PageModel draft = extactPageValue();
		if(saveDraftType == PageSaveMethod.SAVE_MANUAL_DRAFT_EXIT_TO_VIEW && !main.isAnonymousLogin()) {
			//save draft and exist, if it is new created page, it need return its last page, but for existed page, it just need
			//return its original page. so it need give title of page as first parameter
			action.saveManualDraft(draft, true, new SaveExitAsync(PageType.MANUAL_DRAFT,null));
		} else if(saveDraftType == PageSaveMethod.SAVE_MANUAL_DRAFT_STAY_IN_EDIT) {
			draft.visibleAttachments = main.getAttachmentNodeUuidList();
			//return title keep current one, because it won't jump to any other page
			action.saveManualDraft(draft,false, new SaveStayAsync(PageSaveMethod.SAVE_MANUAL_DRAFT_STAY_IN_EDIT, callback));
		} else if(saveDraftType == PageSaveMethod.SAVE_DRAFT_LOAD_PREVIEW) {
			draft.visibleAttachments = main.getAttachmentNodeUuidList();
			//review is going to load, but found no preview text exist, save draft as preview text then
			//anonymous could preview, so here will "save" to draft, but in server side, it will check if it is anonymous,  
			//it only do renderHTML() without save!!!
			action.saveAutoDraft(draft, main.previewPanel);
		} else if(saveDraftType == PageSaveMethod.SAVE_AUTO_DRAFT_STAY_IN_EDIT){
			if(!PageMain.isAnonymousLogin()){
				draft.visibleAttachments = main.getAttachmentNodeUuidList();
				action.saveAutoDraft(draft, new SaveStayAsync(PageSaveMethod.SAVE_AUTO_DRAFT_STAY_IN_EDIT, callback));
			}
		}
	}
	
	/**
	 * <li>User click "save" button on edit/create/preview panel. So far, forceSave should be false.</li>
	 * <li>In diff message or diff panel function bar, user choose "force save"</li>
	 * @param forceSave
	 */
	public void save(boolean forceSave){
		if(!validSave()){
			//no valid content, then stop save, but restore the function buttons status to enable status
			saveFunctionDone();
			return;
		}
		
		if(main.isAnonymousLogin()){
			//popup captcha enquire for anonymous user
			CaptchaDialog captcha = new CaptchaDialog(this,forceSave);
			captcha.showbox();
		}else{
			saveWithCaptcha(forceSave,null, null);
		}
		
	}
	/**
	 * This method only be called in CaptchDialog
	 * @param forceSave
	 */
	public void saveWithCaptcha(boolean forceSave, String captchaResponse, CaptchaDialog captcha){
		if(!validSave()){
			//no valid content, then stop save, but restore the function buttons status to enable status			
			saveFunctionDone();
			return;
		}
		
		PageModel model = extactPageValue();
		if(captchaResponse != null){
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		cancelAutoSave();
		PageControllerAsync action = ControllerFactory.getPageController();
		action.savePage(model,forceSave,new SaveExitAsync(PageType.NONE_DRAFT, captcha));
	}

	/**
	 * @param spaceUname
	 * @param currPageTitle
	 * @param version
	 */
	public void restoreHistory(String spaceUname, String currPageTitle, int version) {
		if(Window.confirm(Msg.params.overwrite_latest(version+""))){
			if(main.isAnonymousLogin()){
				//popup captcha enquire for anonymous user
				CaptchaDialog captcha = new CaptchaDialog(this,spaceUname,currPageTitle,version);
				captcha.showbox();
			}else{
				PageControllerAsync pageController = ControllerFactory.getPageController();
				pageController.restoreHistory(null, spaceUname, currPageTitle, version, new RestoryHistoryCallback(null));
			}
		}
	}
	public void restoreHistoryWithCaptcha(String spaceUname, String currPageTitle, int version, String captchaResponse, CaptchaDialog captcha) {
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		PageControllerAsync pageController = ControllerFactory.getPageController();
		pageController.restoreHistory(model, spaceUname, currPageTitle, version, new RestoryHistoryCallback(captcha));
	}
	/**
	 * 
	 */
	public void removePage() {
		//XXX:i18n
		MessageDialog.confirm(Msg.consts.confirm_trash_page(), new MessageDialogListener() {
			
			public void confirmed() {
				if(main.isAnonymousLogin()){
					//popup captcha enquire for anonymous user
					CaptchaDialog captcha = new CaptchaDialog(EditPanel.this);
					captcha.showbox();
				}else{
					PageControllerAsync action = ControllerFactory.getPageController();
					action.removePage(null, main.getSpaceUname(),main.getPageUuid(),false, new RemovePageAsync(null));
				}
			}
			public void cancelled() {
				
			}
		});

	}

	public void removePageWithCaptcha(String captchaResponse, CaptchaDialog captchaDialog) {
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		PageControllerAsync action = ControllerFactory.getPageController();
		action.removePage(model, main.getSpaceUname(),main.getPageUuid(),false, new RemovePageAsync(captchaDialog));
		
	}
	
	//TODO: move/copy capture is not very safe as they only decide if popup that move/copy dialog. if hack out that move/copy in 
	// client side, then users can skip captcha and move/copy. Although move/copy have server side security valid for taget space
	//but it give a chance to break the space which allows anonymous to move or copy. It means hacker may duplicate pages in same space 
	//or delete page by move to otherspace 
	public void movePage() {
		if(main.isAnonymousLogin()){
			//popup captcha enquire for anonymous user
			CaptchaDialog captcha = new CaptchaDialog(CaptchaDialog.TYPE_MOVE_PAGE,this);
			captcha.showbox();
		}else{
			SpacePagesDialog dialogue = new SpacePagesDialog(Msg.consts.move_to(), main,SpacePagesDialog.MOVE);
			dialogue.showbox();
		}
	}
	public void movePageWithCaptcha(String captchaResponse, CaptchaDialog captchaDialog) {
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		SecurityControllerAsync action = ControllerFactory.getSecurityController();
		action.captchaValid(model, new MoveOrCopyCaptchaAsync(CaptchaDialog.TYPE_MOVE_PAGE, captchaDialog));
	
	}
	public void copyPage() {
		if(main.isAnonymousLogin()){
			//popup captcha enquire for anonymous user
			CaptchaDialog captcha = new CaptchaDialog(CaptchaDialog.TYPE_COPY_PAGE,this);
			captcha.showbox();
		}else{
			SpacePagesDialog dialogue = new SpacePagesDialog(Msg.consts.copy_to(), main,SpacePagesDialog.COPY);
			dialogue.showbox();
		}
	}
	public void copyPageWithCaptcha(String captchaResponse, CaptchaDialog captchaDialog) {
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		SecurityControllerAsync action = ControllerFactory.getSecurityController();
		action.captchaValid(model, new MoveOrCopyCaptchaAsync(CaptchaDialog.TYPE_MOVE_PAGE, captchaDialog));
	}
	public void cancelAutoSave(){
		//cancel save draft first
		saveDraftTimer.cancel();
		saveDraftTimerSet = false;
	}
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		message.error(Msg.consts.loading_edit_panel_failed());
	}

	/**
	 * Edit content success loaded:
	 * <li>user click "Edit" button</li> 
	 * <li>Aysnc call editPage() to get Page content</li> 
	 * <li>Show edit panel with returned content</li> 
	 */
	public void onSuccess(PageModel model) {
		
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			main.errorOnLoadingPanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
			return;
		}

		showPanel(model);
		main.setPreviewReady(true,model);
	}

	public String getEditTagString() {
		String str = tagBox.getText();
		//build comma separated string according to input.
		if(str != null){
			str = str.trim();
			String[] tags = str.split("[\\s,]+");
			//clear string first
			str = "";
			for(int idx=0;idx<tags.length;idx++){
				str += tags[idx] + ", ";
			}
			str = str.trim();
			if(str.endsWith(","))
				str = str.substring(0,str.length()-1);
		}else
			str = "";
		return str;
		
	}

	public String getEditTitle() {
		return titleBox.getText();
	}
	public void setAttachmentCount(int attsCount){
		attachmentBtn.setCount(attsCount);
	}

	/**
	 * Resume save/save draft button from disable
	 */
	public void saveFunctionDone(){
		functionBtnBar.actionDone();
		main.previewPanel.functionBtnBar.actionDone();
	}


	public boolean isDirty() {
		return dirty;
	}
	
	//In some cases, the exit confirm needn't be popup, for example, click save button, or switch to preview etc.
	public void exitConfirm(boolean confirm) {
		exitConfirm = confirm;
	}
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void setCurrentToken(String currentToken) {
		this.currentToken = currentToken;
	}
	public void onPanelSwitched(Widget src, int toIndex) {
		if(src != this)
			return;
		
		if(toIndex != PageMain.EDIT_PANEL){
			//some dialog's parent UI is edit area rather than whole screen, user can switch to other panel even the dialog is opened. 
			//so if panel switched, hide this dialog as well.
			contentArea.closeChildDialog();
		}
	}
	public boolean onPanelSwitching(Widget src, int toIndex) {
		if(src != this && src != main.previewPanel)
			return true;
		
		//This part code is complex and confused -  
		if(toIndex != PageMain.EDIT_PANEL){
			if(currentToken != null && isDirty() && exitConfirm){
				if(!Window.confirm(Msg.consts.confirm_exit())){
					//restore current editing token...
					History.newItem(currentToken, false);
					//stop switch panel
					return false;
				}else{
					//??TODO if user clicks cancel, action.cancelEdit() should be executed twice - it is not good
					//but scenarios like this: user clicks navbar to switch to view panel, this may happens in EditPanel or PreviewPanel
					//so have to do that again, but this won't do any redirect like CancelAsync() in FunctionWidget.
					//call cancel page to clean editing marker - or something may be need to do in cancel action.
					main.editPanel.cancelAutoSave();
					PageControllerAsync action = ControllerFactory.getPageController();
					action.cancelEdit(main.getPageUuid(),null, new AsyncCallback<String>() {
						//dummy, nothing need to do.
						public void onFailure(Throwable arg0) {}
						public void onSuccess(String arg0) {}
					});
				}
			}
			//restore to true - it maybe false
			exitConfirm(true);
		}
		return true;
	}
	
	//********************************************************************
	//               Private methods
	//********************************************************************
	/**
	 * @return
	 */
	private boolean validSave() {
		//also call by titleBox.keyboardListener(), so if more valid, please change there.
		if(StringUtil.isBlank(titleBox.getText())){
			draftStatusBar.setText(Msg.consts.err_empty_title());
			if(draftStatusBar.getStyleName().indexOf(Css.ERROR) == -1){
				draftStatusBar.addStyleName(Css.ERROR);
				titleBox.addStyleName(Css.ERROR);
			}
			return false;
		}
		if(titleBox.getText().length() > TITLE_MAX_LEN){
			draftStatusBar.setText(Msg.params.title_length(String.valueOf(TITLE_MAX_LEN)));
			if(draftStatusBar.getStyleName().indexOf(Css.ERROR) == -1){
				draftStatusBar.addStyleName(Css.ERROR);
				titleBox.addStyleName(Css.ERROR);
			}
			return false;
		}
		if(!tagBox.isValid()){
			return false;
		}
		
		if(draftStatusBar.getStyleName().indexOf(Css.ERROR) != -1){
			draftStatusBar.removeStyleName(Css.ERROR);
			titleBox.removeStyleName(Css.ERROR);
		}
		
		return true;
	}
	/**
	 * Only put value to panel, does not change visible attribute of this panel.
	 * @param model
	 */
	private void fillPanel(final PageModel model) {
	    setCurrentToken(GwtClientUtils.getToken());

		functionBtnBar.loadEditFunc(model==null?0:model.attribute);
		draftStatusBar.setText("");
		draftStatusBar.removeStyleName(Css.ERROR);
		titleBox.removeStyleName(Css.ERROR);
		dirty = false;
		
		//always hide print button as it maybe a new page 
		main.setPrintBtnVisible(false);
		
		if(model == null){
			//clear all fields
			titleBox.setText("");
			tagBox.setText("");
			contentArea.setText("");
		}else{
			if(model.autoSaveUid != null){
				HorizontalPanel messagePanel = new HorizontalPanel();
				HTML m1 = new HTML(Msg.params.resume_auto_draft(GwtClientUtils.toDisplayDate(model.autoSaveDate)) + " &nbsp;");
				ClickLink resumeDraftButton = new ClickLink(Msg.consts.resume_auto_draft());
				messagePanel.add(m1);
				messagePanel.add(resumeDraftButton);
				messagePanel.add(new HTML("?"));
				autoSaveMsgUuid = message.info(messagePanel, -1,true);
				resumeDraftButton.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						autoSaveMsgUuid = null;
						message.cleanMessage();
						PageControllerAsync action = ControllerFactory.getPageController();
						//get this user's draft and its attachments
						action.editDraft(model.autoSaveUid, PageType.AUTO_DRAFT,true, new LoadDraftAsync(PageType.AUTO_DRAFT));
					}
				});
			}
			if(model.editingUserFullname != null){
				HorizontalPanel messagePanel = new HorizontalPanel();
				HTML m1 = new HTML(Msg.params.page_editing_warn(model.editingTime<=1?Msg.consts.just_now():Msg.params.in_minutes_ago(model.editingTime+"")));
				UserProfileLink userButton = new UserProfileLink(model.editingUserFullname, main.getSpaceUname(),model.editingUsername,model.editingUserPortrait);
				messagePanel.add(userButton);
				messagePanel.add(m1);
				messagePanel.add(new HTML("."));
				message.warning(messagePanel, 100000,true);
			}
			
			//update page info : if user refresh page during edit, those info be catch back.
			main.setCurrentPageTitle(model.title);
			PageMain.setParentPageUuid(model.parentPageUuid);
			main.setPageAttribute(model.attribute);
			main.setNewPageType(model.newPageType);
			main.setSpaceUname(model.spaceUname);
			main.setPageVersion(model.pageVersion);
			main.setPageUuid(model.pageUuid == null?"":model.pageUuid.toString());
			
			//put fields into edit box
			titleBox.setText(model.title == null?"":(model.title.length() > TITLE_MAX_LEN?model.title.substring(0,TITLE_MAX_LEN):model.title));
			tagBox.setText(model.tagString == null?"":model.tagString);
			tagBox.setSpaceUname(model.spaceUname);
			
			//must call before setText(), so that content could point to correct editor 
			contentArea.enableRich(model.isRichContent);
			contentArea.setText(model.content == null?"":model.content);
            contentArea.resize();
            
			if(templatePanel != null){
				templatePanel.initPanel();
			}
			if(!GwtUtils.contains(model.attribute,PageAttribute.NO_ATTACHMENT)){
				attachmentBtn.setVisible(true);
				attPanel.mergeAttachments(model.attachmentJson);
				if(GwtUtils.isAnonymous(main.getLoginUser()))
					attPanel.setReadonly(true);
				else
					attPanel.setReadonly(false);
			}else{
				//hide all attachment and panel...
				setAttachmentCount(0);
				attachmentBtn.setVisible(false);
				attPanel.setVisible(false);
				attPanel.setReadonly(false);
				attPanel.reset();
			}
			
			
			main.fillEditNavBar(model);
			
			
			if(model.draftUid != null){
				HorizontalPanel messagePanel = new HorizontalPanel();
				HTML m1 = new HTML(Msg.params.resume_draft(GwtClientUtils.toDisplayDate(model.draftDate)) + " &nbsp;");
				ClickLink resumeDraftButton = new ClickLink(Msg.consts.resume_draft());
				messagePanel.add(m1);
				messagePanel.add(resumeDraftButton);
				messagePanel.add(new HTML("?"));
				
				message.info(messagePanel, -1,true);
				resumeDraftButton.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						autoSaveMsgUuid = null;
						message.cleanMessage();
						PageControllerAsync action = ControllerFactory.getPageController();
						//get this user's draft and its attachments
						action.editDraft(model.draftUid, PageType.MANUAL_DRAFT,true, new LoadDraftAsync(PageType.MANUAL_DRAFT));
					}
				});
				
				final MessageDialog confirmDraftDlg  = new MessageDialog(MessageDialog.TYPE_CONFIRM, 
						Msg.consts.confirm() + " " + Msg.consts.resume_draft(),
						Msg.params.resume_manual_draft(GwtClientUtils.toDisplayDate(model.draftDate)));
				confirmDraftDlg.addMessageListener(new MessageDialogListener(){
					public void cancelled() {}
					public void confirmed() {
						autoSaveMsgUuid = null;
						message.cleanMessage();
						PageControllerAsync action = ControllerFactory.getPageController();
						//get this user's draft and its attachments
						action.editDraft(model.draftUid, PageType.MANUAL_DRAFT,true, new LoadDraftAsync(PageType.MANUAL_DRAFT));
					}
				});
				confirmDraftDlg.showbox();
				
			}
		}
	}
	

	private PageModel extactPageValue() {
		
		PageModel model = new PageModel();
		
		//so far, only create/createHome has valid ParentPageUuid. Otherwise it is null.
		model.parentPageUuid = PageMain.getParentPageUuid();

		model.spaceUname = PageMain.getSpaceUname();
		model.attribute = main.getPageAttribute();
		model.newPageType = main.getNewPageType();
		
		model.pageUuid = PageMain.getPageUuid();
		model.pageVersion = main.getPageVersion();
		//Title doesn't allow over one spaces inside. This is because multiple spaces display as one space in HTML
		//if user input multiple spaces in title, it is very confuse when 2 title looks same, however, content is different.
		model.title = StringUtil.shrinkSpaces(titleBox.getText());
		model.tagString = tagBox.getText();
		model.content = contentArea.getText();
		model.isRichContent = contentArea.isRichEnabled();
		
		//notify value comes from WikiConstants.NOTIFY_*
		model.requireNotified = (noticeCheckbox.getValue()?1:0);
		model.attachmentList = attPanel.getUploadedItemsUuid();

		return model;
	}


	/*
	 * show saving version conflict diff panel  
	 */
	protected void diffRendered() {
		super.diffRendered();
		
		functionBtnBar.loadVersionDiff(false);
		diffContent.setVisible(true);
		contentArea.setVisible(false);
	}
	/*
	 * saving version conflict diff: text maybe modified by user choose context menu (accept/deny)
	 */
	protected void diffResume() {
		super.diffResume();
		
		functionBtnBar.resume();
		String diffRs = getDiffMergeResult();
		if(diffRs != null){
			//user modified diff by context menu, need reset edit area
			contentArea.setText(diffRs);
			main.setPreviewReady(false,null);
		}
		diffContent.setVisible(false);
		contentArea.setVisible(true);
	}

	/**
	 * @param model
	 */
	private void versionConflictMessage() {
	    
	    //duplicated this message for both edit and preview panel:
		message.warning(buildVersionConflictMessage(this),false);
		main.previewPanel.message.warning(buildVersionConflictMessage(main.previewPanel), false);
		
	}
	private HorizontalPanel buildVersionConflictMessage(final DiffPanel diffPanel) {
		ClickLink diffLink = new ClickLink(Msg.consts.compare());
		diffLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				//this compare won't be in TOKEN way to invoke: when user refresh page, it will return to create/edit page
				PageControllerAsync action = ControllerFactory.getPageController();
				PageModel draft = extactPageValue();
				action.diffConflict(draft,diffPanel.versionAsync);
			}

		});
		ClickLink forcsSaveLink = new ClickLink(Msg.consts.force_save());
		forcsSaveLink.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				//save anyway
				save(true);
			}
		});
		return ErrorCode.getMessage(ErrorCode.PAGE_VERSION_CONFLICT, null, new Widget[]{diffLink,forcsSaveLink});	
	}

	
	/**
	 * @return View layout from javascript.
	 */
	private native String readEditLayout()/*-{
	   return $wnd._edit_layout;
	}-*/;
	//********************************************************************
	//                       Private Listener inner class
	//********************************************************************

	private class ContentChangeListener  implements KeyPressHandler, ChangeHandler{

		private void touch() {
			//user type words in WikiEditPanel, this causes content change event:
			//need server side render wiki page again.
			//anonymous user does not support auto-save function!
			if(!saveDraftTimerSet && !main.isAnonymousLogin()){
				saveDraftTimer.schedule(SAVE_DRAFT_TIMEOUT);
				saveDraftTimerSet = true;
			}
			//if preview is ready status, need set it to false, because there are some new input coming in.
			if(main.isPreviewReady())
				main.setPreviewReady(false,null);
			
			if(!dirty){
				//onBeforeUnload() to need user confirm before close browser window
				GwtClientUtils.onBeforeUnload(Msg.consts.confirm_exit_editing());
			}
			
			//mark user edit this page flag, so that give a chance to ask user exit during saving
			dirty = true;
			
		}

		public void onKeyPress(KeyPressEvent event) {
			touch();
		}

		//rich editor cannot detect keyPress correctly - menu bar evnet won't trigger onKeyPress(), so already dirty on rich editor..  
		public void onChange(ChangeEvent event) {
			touch();
		}

	}

	private class SaveDraftTimer extends Timer{
		public void run() {
			saveDraft(PageSaveMethod.SAVE_AUTO_DRAFT_STAY_IN_EDIT);
			saveDraftTimerSet = false;
		}
	}
	
	private class SaveExitAsync implements  AsyncCallback<PageModel>{
		
		private PageType draftStatus;
		private CaptchaDialog captcha;
		public SaveExitAsync(PageType draftStatus, CaptchaDialog captcha){
			this.draftStatus = draftStatus;
			this.captcha = captcha;
			
		}
		public void onFailure(Throwable error) {
			if(captcha != null){
				captcha.enableSubmit();
			}
			if(error instanceof CaptchaVerifiedException && captcha != null){
				captcha.refreshCaptch();
			}else{
				//restore button status to enable.
				saveFunctionDone();
				
				GwtClientUtils.processError(error);
				//bind message with preview: preview has publish/save draft button
				message.error(Msg.consts.save_error());
				main.previewPanel.message.error(Msg.consts.save_error());
			}
		}
		public void onSuccess(PageModel model) {
			//restore button status to enable.
			saveFunctionDone();
			
			if(captcha != null){
				captcha.hidebox();
			}
			dirty = false;
			
			//bind message with preview: preview has publish/save draft button
			message.cleanMessage();
			main.previewPanel.message.cleanMessage();
			
			//!!! This method use special handling in error so that showing version conflict message 
			if(BaseEntryPoint.I != null)
				//LoginMain is not extend from BaseEntryPoint, then it should be null.
				BaseEntryPoint.I.isSessionExpired(model.loginUsername);
			
			if(ErrorCode.hasError(model)){
				//special handle for version conflict
				if(ErrorCode.PAGE_VERSION_CONFLICT.equals(model.errorCode)){
					versionConflictMessage();
				}else{ //just show error message then
					//bind message with preview: preview has publish/save draft button
					message.error(ErrorCode.getMessage(model.errorCode,model.errorMsg));
					main.previewPanel.message.error(ErrorCode.getMessage(model.errorCode, model.errorMsg));
				}
				return;
			}
			

			//fill viewPanel then resetToken: remove $EDIT or $CREATE and don't invoke RPC call as well.
			exitConfirm(false);
			dirty = false;
			main.viewPanel.showPanel(model);
			main.viewPanel.resetToken();
		}

	}

	private class SaveStayAsync implements  AsyncCallback<PageModel>{
		private PageSaveMethod saveType;
        private Callback<String> callback;
		/**
		 * @param callback 
		 * @param saveAutoDraftStayInEdit
		 */
		public SaveStayAsync(PageSaveMethod saveType, Callback<String> callback) {
			this.saveType = saveType;
			this.callback = callback;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
			//restore button status to enable.
			saveFunctionDone();
			
//			it is better silence here
//			draftStatusBar.setText("Failed on auto saving at " + new SimpleDateFormat("HH:mm").format(new Date()));
		}
		public void onSuccess(PageModel model) {
			//restore button status to enable.
			saveFunctionDone();
			

			if(!GwtClientUtils.preSuccessCheck(model,message)){
				//bind message with preview: preview has publish/save draft button
				main.previewPanel.message.error(ErrorCode.getMessage(model.errorCode, model.errorMsg));
				return;
			}
			//bind message with preview: preview has publish/save draft button
			message.cleanMessage();
			main.previewPanel.message.cleanMessage();
			
			if(this.saveType == PageSaveMethod.SAVE_AUTO_DRAFT_STAY_IN_EDIT){
				draftStatusBar.setText(Msg.params.auto_save_at(DateTimeFormat.getFormat("HH:mm").format(new Date())));
				//msg from yellow, fade out, normal color then fade in 
				String fcolor = DOM.getStyleAttribute(draftStatusBar.getElement(),"color");
				String bcolor = DOM.getStyleAttribute(draftStatusBar.getElement(),"backgroundColor");
				
				DOM.setStyleAttribute(draftStatusBar.getElement(),"color","#9F6000");
				DOM.setStyleAttribute(draftStatusBar.getElement(),"backgroundColor","#FEEFB3");
				
				fadeMessage(Css.DRAFT_STATUS_MSG, fcolor,bcolor);
			}else if(this.saveType == PageSaveMethod.SAVE_MANUAL_DRAFT_STAY_IN_EDIT){
				dirty = false;
				draftStatusBar.setText(Msg.params.draft_save_at(DateTimeFormat.getFormat("HH:mm").format(new Date())));
				//msg from red, fade out, normal color then fade in 
				String fcolor = DOM.getStyleAttribute(draftStatusBar.getElement(),"color");
				String bcolor = DOM.getStyleAttribute(draftStatusBar.getElement(),"backgroundColor");
				
				DOM.setStyleAttribute(draftStatusBar.getElement(),"color","#D8000C");
				DOM.setStyleAttribute(draftStatusBar.getElement(),"backgroundColor","#FFBABA");
				
				fadeMessage(Css.DRAFT_STATUS_MSG, fcolor,bcolor);
			}
				
			//update page info
			//don't update pageUid and pageVersion: keep it only non-draft page can update pageUid
			//does not update title: if user refresh browser, original title will be used to load page
//			main.setPageTitle(model.title);
			main.setSpaceUname(model.spaceUname);
			PageMain.setPageUuid(model.pageUuid == null?"":model.pageUuid.toString());
			
			main.setPreviewReady(true,model);
			
			if(callback != null){
			    callback.callback(model.pageUuid);
			}
		}
	}
	private static native void fadeMessage(String clz, String fcolor, String bcolor)/*-{
		var msg = $wnd.$("."+clz)
        msg.fadeOut(2000, function(){
        	msg.css("color",fcolor);
        	msg.css("background-color",bcolor);
			msg.fadeIn("fast");
		});
    }-*/;
	
	private class LoadDraftAsync implements  AsyncCallback<PageModel>{
		private PageType draftStatus;
		public LoadDraftAsync(PageType draftStatus){
			this.draftStatus = draftStatus;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(PageModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}

			//only update Edit Textarea and attachment fields: draft don't save tags info.
			contentArea.setText(model.content == null?"":model.content);
			attPanel.mergeAttachments(model.attachmentJson, draftStatus);
			main.setPreviewReady(true,model);
		}
	}
	
	
	private class RestoryHistoryCallback implements AsyncCallback<PageModel>{
		private CaptchaDialog captcha;
		public RestoryHistoryCallback(CaptchaDialog captcha){
			this.captcha = captcha;
		}
		public void onFailure(Throwable error) {
			if(captcha != null){
				captcha.enableSubmit();
			}
			
			if(error instanceof CaptchaVerifiedException && captcha != null){
				captcha.refreshCaptch();
			}else{
				GwtClientUtils.processError(error);
			}
		}
		public void onSuccess(PageModel model) {
			if(captcha != null){
				captcha.hidebox();
			}
			
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				main.errorOnVisiblePanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
				return;
			}

			main.viewPanel.showPanel(model);
			main.viewPanel.showInfo(Msg.consts.version_restore());
			
		}
	}

	private class MoveOrCopyCaptchaAsync implements AsyncCallback<Integer>{
		private CaptchaDialog captcha;
		int type;
		public MoveOrCopyCaptchaAsync(int type, CaptchaDialog captcha) {
			this.captcha = captcha;
			this.type = type;
		}
		public void onFailure(Throwable error) {
			if(captcha != null){
				captcha.enableSubmit();
			}
			if(error instanceof CaptchaVerifiedException && captcha != null){
				captcha.refreshCaptch();
			}else{
				GwtClientUtils.processError(error);
			}
		}

		public void onSuccess(Integer result) {
			if(captcha != null){
				captcha.hidebox();
			}
			if(result == null || result == -1)
				return;
			
			if(type == CaptchaDialog.TYPE_MOVE_PAGE){
				SpacePagesDialog dialogue = new SpacePagesDialog(Msg.consts.move_to(), main,SpacePagesDialog.MOVE);
				dialogue.showbox();
			}
			if(type == CaptchaDialog.TYPE_COPY_PAGE){
				SpacePagesDialog dialogue = new SpacePagesDialog(Msg.consts.copy_to(), main,SpacePagesDialog.COPY);
				dialogue.showbox();
			}
		}
		
	}
	private class RemovePageAsync implements AsyncCallback<PageModel>{
		private CaptchaDialog captcha;
		public RemovePageAsync(CaptchaDialog captcha) {
			this.captcha = captcha;
		}
		public void onFailure(Throwable error) {
			if(captcha != null){
				captcha.enableSubmit();
			}
			if(error instanceof CaptchaVerifiedException && captcha != null){
				captcha.refreshCaptch();
			}else{
				GwtClientUtils.processError(error);
			}
		}
		public void onSuccess(PageModel model) {
			if(captcha != null){
				captcha.hidebox();
			}
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				main.errorOnVisiblePanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
				return;
			}

			//go to home page
			GwtClientUtils.refreshToken(GwtUtils.getSpacePageToken(main.getSpaceUname(),null));
			
		}
	}
}
