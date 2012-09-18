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

import java.util.Iterator;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.constant.PageSaveMethod;
import com.edgenius.wiki.gwt.client.home.CreateSpaceDialogue;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.portal.Portal;
import com.edgenius.wiki.gwt.client.portal.PortletListDialogue;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ContextMenu;
import com.edgenius.wiki.gwt.client.widgets.ContextMenuItem;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.InviteDialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * @author Dapeng.Ni
 */
public class FunctionWidget extends SimplePanel {
	private ButtonBar panel = new ButtonBar(ButtonBar.NEGATIVE|ButtonBar.RIGHT);
	private ButtonBar verPanel = new ButtonBar(ButtonBar.NEGATIVE|ButtonBar.RIGHT);
	private PageMain main;
	//see bug:http://code.google.com/p/google-web-toolkit/issues/detail?id=727
	//on GWT1.4, set child width(),height() works but in GWT1.5M2, it does not work in IE, sucks, I have to give deckPanel
	private FlowPanel deck = new FlowPanel();
	private Button createButton;
	private Button editButton;
	private Button saveButton;
	private Button saveDraftButton;
	private Button forceSaveButton;
	private Button previewEditButton;
	public FunctionWidget(PageMain main){
		this.main = main;

		deck.insert(panel, 0);
		deck.insert(verPanel,1);
		panel.setVisible(true);
		verPanel.setVisible(false);
		
		
		this.setStyleName(Css.FUNCTION_BAR);
		this.setWidget(deck);
		
		//see bug:http://code.google.com/p/google-web-toolkit/issues/detail?id=727
		panel.setSize(null, null);
		verPanel.setSize(null, null);
	}
	public void loadDashboardFunc(final UserModel user, final Portal portal){
		//instance permission
		int[] perm = null;
		if(user != null)
			perm = user.getPermissions();
		
		panel.clear();
		
		if(perm != null && perm[ClientConstants.WRITE] == 1){
			Button createSpaceButton = new Button(Msg.consts.create_space(),ButtonIconBundle.createImage());
			createSpaceButton.setTitle(Msg.consts.create_space_title());
			createSpaceButton.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					CreateSpaceDialogue dialogue = new CreateSpaceDialogue();
					dialogue.addListDialogueListener(portal);
					dialogue.showbox();
				}
			});
			panel.add(createSpaceButton);
		}	
		
		if(!AbstractEntryPoint.isOffline() && !GwtUtils.isAnonymous(user)){
			Button addSpaceBtn = new Button(Msg.consts.more_spaces(),ButtonIconBundle.book_openImage());
			addSpaceBtn.setTitle(Msg.consts.more_spaces_title());
			addSpaceBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					PortletListDialogue dialogue = new PortletListDialogue(GwtUtils.isAnonymous(user));
					dialogue.addListDialogueListener(portal);
					dialogue.showbox();
				}
			});
			panel.add(addSpaceBtn);
		}
		
		//admin
		if(perm != null &&  perm[ClientConstants.ADMIN] == 1){
			Button adminButton = new Button(Msg.consts.system_admin(),ButtonIconBundle.adminImage());
			adminButton.setTitle(Msg.consts.system_admin_title());
			adminButton.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					GwtClientUtils.redirect(SharedConstants.URL_INSTANCE_ADMIN);
				}
			});
			panel.add(adminButton);
		}
		
	}
	public void loadHistoryFunc(int[] permission, String currPageTitle, int version) {
		panel.clear();
		
		if(!AbstractEntryPoint.isOffline()){
			//write
			if(permission != null && permission[ClientConstants.WRITE] == 1){
				//restore version
				Button restoreBtn = new Button(Msg.consts.restore());
				restoreBtn.setTitle(Msg.consts.restore_title());
				restoreBtn.setIcon(ButtonIconBundle.resore());
				restoreBtn.addClickHandler(new RestoreHistoryListener(currPageTitle,version));
				panel.add(restoreBtn);
				
			}
			
			//return current version
			Button diffBtn = new Button(Msg.consts.diff());
			diffBtn.setTitle(Msg.consts.diff_title());
			diffBtn.setIcon(ButtonIconBundle.diff());
			diffBtn.addClickHandler(new DiffHistoryListener(currPageTitle,version));
			panel.add(diffBtn);
		}
		
		Button resumeBtn = new Button(Msg.consts.return_last());
		resumeBtn.setTitle(Msg.consts.return_title());
		resumeBtn.setIcon(ButtonIconBundle.arrow_undoImage());
		resumeBtn.addClickHandler(new ReturnCurrentListener(currPageTitle));
		panel.add(resumeBtn);

		
	}

	public void loadViewFunc(PageModel page){
		panel.clear();
		
		if(page == null)
			return;
		
		int[] permission = page.permissions;
		int attribute = page.attribute;

		
		//write
		if(permission[ClientConstants.WRITE] == 1){
			//page not found or pure draft: not allow create children page
			if(!GwtUtils.contains(attribute, PageAttribute.NO_CREATE)){
				createButton = new Button(Msg.consts.create()); 
				//XXX:i18n
				createButton.setTitle(Msg.consts.create_page_title());
				createButton.setIcon(ButtonIconBundle.createImage());
				createButton.addClickHandler(new CreateListener());
				panel.add(createButton);
			}
			//page not found: not allow edit
			if(!GwtUtils.contains(attribute, PageAttribute.NO_EDIT)){
				editButton = new Button(Msg.consts.edit());
				editButton.setTitle(Msg.consts.edit_page_title());
				editButton.setIcon(ButtonIconBundle.editImage());
				editButton.addClickHandler(new EditListener());
				panel.add(editButton);
			}
		}
		// space invitation
		if(permission[ClientConstants.SPACE_BASE+ClientConstants.ADMIN] == 1){
	    	Button frdButton = new Button(Msg.consts.invite_friends()); 
	    	frdButton.setTitle(Msg.consts.invite_title());
	    	frdButton.setIcon(ButtonIconBundle.groupImage());
			frdButton.addClickHandler(new SpaceInviteListener());
			panel.add(frdButton);
	    }	
	    
		//so far for readonly page (such as "page not found", tagcloud etc), not necessary show menu item
		if(!GwtUtils.contains(attribute, PageAttribute.NO_MENU)){
			//for menu, if page is readonly, Menu  won't show as well. 
			//NOTE: if any functions add, please be aware which must not read-only function
			//at least now, I want to screen menu if page is read-only for page could have pretty layout. 
			boolean readonly = true;
			for(int idx=ClientConstants.WRITE;idx<permission.length;idx++){
				if(permission[idx] == 1){
					readonly = false;
					break;
				}
			}
			if(!readonly){
				Button menuBar = initActionMenu(page);
				//if nothing in menu, don't display "Function" button then.
				if(menuBar != null)
					panel.add(menuBar);
			}
		}

	}
	public void loadEditSidebarFunc() {
		panel.clear();
		saveButton = new Button(Msg.consts.save()); 
		saveButton.setTitle(Msg.consts.save_sidebar_title());
		saveButton.setIcon(ButtonIconBundle.diskImage());
		saveButton.addClickHandler(new SaveSidebarListener());
		panel.add(saveButton);
		
		Button previewEditButton = new Button(Msg.consts.preview());
		previewEditButton.setTitle(Msg.consts.preview_sidebar_title());
		previewEditButton.setIcon(ButtonIconBundle.eyeImage());
		previewEditButton.addClickHandler(new PreviewSidebarListener());
		panel.add(previewEditButton);
		
		Button cancelButton = new Button(Msg.consts.cancel());
		cancelButton.setTitle(Msg.consts.cancel_exit_title());
		cancelButton.setIcon(ButtonIconBundle.crossImage());
		cancelButton.addClickHandler(new CancelSidebarListener());
		panel.add(cancelButton);
	}
	/**
	 * 
	 */
	public void loadEditTemplateFunc() {
		panel.clear();
		saveButton = new Button(Msg.consts.save()); 
		saveButton.setTitle(Msg.consts.save());
		saveButton.setIcon(ButtonIconBundle.diskImage());
		saveButton.addClickHandler(new SaveTemplateListener());
		panel.add(saveButton);
		
		Button previewButton = new Button(Msg.consts.preview());
		previewButton.setTitle(Msg.consts.preview_title());
		previewButton.setIcon(ButtonIconBundle.eyeImage());
		previewButton.addClickHandler(new PreviewTemplateListener());
		panel.add(previewButton);
		
		Button cancelButton = new Button(Msg.consts.cancel());
		cancelButton.setTitle(Msg.consts.cancel_exit_title());
		cancelButton.setIcon(ButtonIconBundle.crossImage());
		cancelButton.addClickHandler(new CancelTemplateListener());
		panel.add(cancelButton);
		
	}
	/**
	 * 
	 */
	public void loadPreviewTemplateFunc() {
		panel.clear();

		saveButton = new Button(Msg.consts.save()); 
		saveButton.setTitle(Msg.consts.save_sidebar_title());
		saveButton.setIcon(ButtonIconBundle.diskImage());
		saveButton.addClickHandler(new SaveTemplateListener());
		panel.add(saveButton);
		
		Button resumeButton = new Button(Msg.consts.resume());
		resumeButton.setTitle(Msg.consts.resume_edit());
		resumeButton.setIcon(ButtonIconBundle.eyeImage());
		resumeButton.addClickHandler(new ResumeTemplateListener());
		panel.add(resumeButton);
		
		Button cancelButton = new Button(Msg.consts.cancel());
		cancelButton.setTitle(Msg.consts.cancel_exit_title());
		cancelButton.setIcon(ButtonIconBundle.crossImage());
		cancelButton.addClickHandler(new CancelTemplateListener());
		panel.add(cancelButton);
		
	}
	public void loadEditFunc(int pageAttribute){
		panel.clear();
		saveButton = new Button(Msg.consts.publish()); 
		saveButton.setTitle(Msg.consts.publish_title());
		saveButton.setIcon(ButtonIconBundle.diskImage());
		saveButton.addClickHandler(new SaveListener());
		panel.add(saveButton);
		
		if(!GwtUtils.contains(pageAttribute, PageAttribute.NO_CREATE_DRAFT)){
			saveDraftButton = new Button(Msg.consts.save_draft());
			saveDraftButton.setTitle(Msg.consts.save_draft_exit_title());
			saveDraftButton.setIcon(ButtonIconBundle.savedraftImage());
			saveDraftButton.addClickHandler(new SaveDraftListener());
			panel.add(saveDraftButton);
		}
		previewEditButton = new Button(Msg.consts.preview());
		previewEditButton.setTitle(Msg.consts.preview_title());
		previewEditButton.setIcon(ButtonIconBundle.eyeImage());
		previewEditButton.addClickHandler(new PreviewListener());
		
		Button cancelButton = new Button(Msg.consts.cancel());
		cancelButton.setTitle(Msg.consts.cancel_exit_title());
		cancelButton.setIcon(ButtonIconBundle.crossImage());
		cancelButton.addClickHandler(new CancelListener());
		
		panel.add(previewEditButton);
		panel.add(cancelButton);
	}
	
	/**
	 * Edit and Preview Panel will load this function bar in readonly false model, because it is saving status, and allow user
	 * choose force saving.<br>
	 * View Panel is history comparing result, so it is readonly model. Only resume is allowed
	 */
	public void loadVersionDiff(boolean readonly) {
		verPanel.clear();
		if(!readonly){
			forceSaveButton = new Button(Msg.consts.force_publish());
			forceSaveButton.setTitle(Msg.consts.force_publish_title());
			forceSaveButton.setIcon(ButtonIconBundle.diskImage());
			forceSaveButton.addClickHandler(new ForceSaveListener());
			verPanel.add(forceSaveButton);
		}		
		Button resumeButton = new Button(Msg.consts.resume());
		resumeButton.setTitle(Msg.consts.resume_title());
		resumeButton.setIcon(ButtonIconBundle.arrow_undoImage());
		resumeButton.addClickHandler(new DiffResumeListener());
		verPanel.add(resumeButton);
		panel.setVisible(false);
		verPanel.setVisible(true);
	}

	public void loadPreviewFunc(int pageAttribute){
		panel.clear();
		saveButton = new Button(Msg.consts.publish()); 
		saveButton.setTitle(Msg.consts.publish_title());
		saveButton.setIcon(ButtonIconBundle.diskImage());
		saveButton.addClickHandler(new SaveListener());
		panel.add(saveButton);
		
		if(!GwtUtils.contains(pageAttribute, PageAttribute.NO_CREATE_DRAFT)){
			saveDraftButton = new Button(Msg.consts.save_draft()); 
			saveDraftButton.setTitle(Msg.consts.save_draft_exit_title());
			saveDraftButton.setIcon(ButtonIconBundle.savedraftImage());
			saveDraftButton.addClickHandler(new SaveDraftListener());
			panel.add(saveDraftButton);
		}		
		Button resumeButton = new Button(Msg.consts.resume_edit());
		resumeButton.setTitle(Msg.consts.resume_edit_title());
		resumeButton.setIcon(ButtonIconBundle.arrow_undoImage());
		resumeButton.addClickHandler(new ResumeEditListener());
		
		panel.add(resumeButton);
	}
	
	/**
	 * Resume from version diff function bar.
	 */
	public void resume(){
		panel.setVisible(true);
		verPanel.setVisible(false);
	}

	
	public void createPage(){
		//this check will be same with permission check:
		if(createButton != null){
			//only page exist create button, allow shortcut to call this method
			for(Iterator<Widget> iter = panel.iterator();iter.hasNext();){
				Widget widget = iter.next();
				if(widget == createButton){
					History.newItem(GwtUtils.buildToken(PageMain.TOKEN_CREATE, main.getSpaceUname(),Msg.consts.new_page_default_title()));
					return;
				}
			}
		}
	}
	public void editPage(){
		//this check will be same with permission check:
		if(editButton != null){
			//only page exist edit button, allow shortcut to call this method
			for(Iterator<Widget> iter = panel.iterator();iter.hasNext();){
				Widget widget = iter.next();
				if(widget == editButton){
					
					History.newItem(GwtUtils.buildToken(PageMain.TOKEN_EDIT, main.getSpaceUname(),main.getPageUuid()));
					return;
				}
			}
		}
	}
	public void forceSave(){
		//do not do auto draft save
		main.editPanel.cancelAutoSave();
		
		//this check will be same with permission check:
		if(forceSaveButton != null){
			//only page exist forceSave button, allow shortcut to call this method
			for(Iterator<Widget> iter = verPanel.iterator();iter.hasNext();){
				Widget widget = iter.next();
				if(widget == forceSaveButton){
					forceSaveButton.setBusy(true);
					main.editPanel.save(true);
					return;	
				}
			}
		}
	}
	public void savePage() {
		//do not do auto draft save
		main.editPanel.cancelAutoSave();
		
		//does not force save: if there version conflict, it will return error message
		if(saveButton != null){
			saveButton.setBusy(true);
		}
		main.editPanel.save(false);
		
	}
	public void saveDraftPage(boolean exitToView){
		//do not do auto draft save
		main.editPanel.cancelAutoSave();
		
		//save draft then return to view page: this draft won't display on view page
		if(saveDraftButton != null){
			saveDraftButton.setBusy(true);
		}
		if(exitToView)
			main.editPanel.saveDraft(PageSaveMethod.SAVE_MANUAL_DRAFT_EXIT_TO_VIEW);
		else
			main.editPanel.saveDraft(PageSaveMethod.SAVE_MANUAL_DRAFT_STAY_IN_EDIT);
	}
	public void cancelPage(){
		//switch to loading panel - this invokes panelSwitch event, then will trigger exist confirm dialog
		if(!main.loading())
			return;
		
		//do not do auto draft save
		main.editPanel.cancelAutoSave();

		String pageUuid = main.getPageUuid();
		String spaceUname = main.getSpaceUname();
		//for create, it means already there are some draft exist  
		//for edit, it is possible there are some draft try to delete them.
		PageControllerAsync action = ControllerFactory.getPageController();
		if(pageUuid!= null){
			action.removeDraft(spaceUname,main.getPageUuid(),PageType.AUTO_DRAFT, new RemoveDraftAsync());
		}
		
		
		String token = GwtClientUtils.getToken();
		String actionID = GwtUtils.getToken(GwtUtils.parseToken(token),0).toUpperCase();
		
		if(PageMain.TOKEN_CREATE_HOME.equals(actionID)){
			//create new home, then just return space home then
			History.newItem(GwtUtils.getSpacePageToken(spaceUname, null));
		}else if(PageMain.TOKEN_EDIT.equals(actionID)){
			//edit exist page, then return to same page
			action.cancelEdit(main.getPageUuid(),null, new CancelAsync());
		}else if(PageMain.TOKEN_CREATE.equals(actionID)){
			//get back parent page title according to uuid, then jump to that page in AsyncCallback
			action.cancelEdit(null, PageMain.getParentPageUuid(),new CancelAsync());
		}else{
			//draft, try to return itself (if this draft already has page, otherwise, return it is parent)
			action.cancelEdit(main.getPageUuid(),PageMain.getParentPageUuid(),new CancelAsync());
		}

	}
	public void previewPage() {
		//do not do auto draft save
		main.editPanel.exitConfirm(false);
		main.editPanel.cancelAutoSave();

		main.switchTo(PageMain.PREVIEW_PANEL);
	}
	public void previewTemplate() {
		main.editTemplatePanel.preview();
	}
	public void saveTemplate(){
		main.editTemplatePanel.save();
	}
	public void cancelTemplate(){
		main.editTemplatePanel.cancel();
	}
	private void resumeTemplate() {
		main.editTemplatePanel.togglePreview();
	}
	public void previewSidebar() {
		main.editSidebarPanel.preview();
	}
	public void saveSidebar(){
		main.editSidebarPanel.save();
		
	}
	public void resumePreviewPage() {
		main.switchTo(PageMain.EDIT_PANEL);
	}

	public void restoreHistory(String spaceUname, String currPageTitle, int version) {
		
		main.editPanel.restoreHistory(spaceUname,currPageTitle,version);

	}

//	public static String viewFriendsToken(String spaceUname) {
//		return GwtUtils.getSpacePageToken(spaceUname,null)+ SharedConstants.CPAGE_FRIENDS+ PageMain.TOKEN_CPAGE;
//	}
	public static String viewSpaceAdminToken(String spaceUname) {
		return GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_SPACEADMIN), spaceUname);
	}
	public static String viewTemplateListToken(String spaceUname) {
		return GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TEMPLATE_LIST), spaceUname);
	}
	
	//********************************************************************
	//               private methods
	//********************************************************************
	private void inviteFriends() {
		InviteDialog dialog = new InviteDialog();
		dialog.showbox();
		
	}
	//********************************************************************
	//               private class
	//********************************************************************
	private class CreateListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			createPage();
		}
	}
	private class EditListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			editPage();
		}
	}
	private class SaveListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			savePage();
		}
	}
	private class SaveTemplateListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			saveTemplate();
		}
	}
	private class PreviewTemplateListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			previewTemplate();
		}
	}
	private class ResumeTemplateListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			resumeTemplate();
		}

	}
	
	private class CancelTemplateListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			cancelTemplate();
		}
	}
	
	private class SaveSidebarListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			saveSidebar();
		}
	}
	private class ForceSaveListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			forceSave();
		}
	}
	private class DiffResumeListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			main.resumeFromDiff();
		}
	}
	private class SaveDraftListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			//OK, My wife said it is not good experience, if click "Save draft" button then page exit.
			//so I change true -> false, means it won't exist only a message show manual draft saved - exactly 
			//same with shortcut key (Ctrl-s)
			saveDraftPage(false);
		}
	}

	private class CancelListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			cancelPage();
		}
	}
	private class CancelSidebarListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			PageControllerAsync action = ControllerFactory.getPageController();
			//edit exist page sidebar, then return to same page
			action.cancelEdit(main.getPageUuid(),null, new CancelAsync());

		}
	}
	private class SpaceInviteListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			inviteFriends();
		}


	}


	private class RemoveDraftAsync implements AsyncCallback<PageModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(PageModel  model) {
			//clean edit page error message
			main.cleanMessage();
			
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				main.errorOnVisiblePanel(ErrorCode.getMessageText(model.errorCode,model.errorMsg));
				return;
			}
		}
		
	}
	private class PinPageAsync implements AsyncCallback<Boolean>{
		private ContextMenuItem menuItem;
		private boolean pined;
		/**
		 * @param pinTopPageItem
		 * @param b
		 */
		public PinPageAsync(ContextMenuItem pinTopPageItem, boolean pined) {
			this.menuItem = pinTopPageItem;
			this.pined = pined;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(Boolean success) {
			
			if(success){
				//success return will shift to reversed value
				this.pined = !this.pined;
				
				menuItem.setTextImage(this.pined?Msg.consts.remove_pin():Msg.consts.pin_to_top()
				,this.pined?new Image(IconBundle.I.get().pin()):new Image(IconBundle.I.get().pin_dis()));

				GwtClientUtils.hidePagePin(!this.pined);
				
				//switch status code
				this.menuItem.setObject(pined?1:0);
			}
		}
	}
	private class CancelAsync implements AsyncCallback<String>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(String title) {
			//view page by page title (and current spaceUname)
			History.newItem(GwtUtils.getSpacePageToken(PageMain.getSpaceUname(), title));
		}
	}
	
	private class RestoreHistoryListener implements ClickHandler{
		private int version;
		private String currPageTitle;
		public RestoreHistoryListener(String currPageTitle, int version) {
			this.version = version;
			this.currPageTitle = currPageTitle;
		}

		public void onClick(ClickEvent event) {
			restoreHistory(main.getSpaceUname(), currPageTitle, version);
		}


	}
	private class DiffHistoryListener implements ClickHandler{

		private int version;
		private String currPageTitle;
		
		public DiffHistoryListener(String currPageTitle,int version) {
			this.version = version;
			this.currPageTitle = currPageTitle;
		}

		public void onClick(ClickEvent event) {
			PageControllerAsync action = ControllerFactory.getPageController();
			action.diff(main.getSpaceUname(),currPageTitle, Integer.valueOf(version), main.viewPanel.versionAsync);
		}
	}
	private class ReturnCurrentListener implements ClickHandler{

		private String currPageTitle;

		public ReturnCurrentListener(String currPageTitle) {
			this.currPageTitle = currPageTitle;
		}

		public void onClick(ClickEvent event) {
			
			History.newItem(GwtUtils.getSpacePageToken(main.getSpaceUname(),currPageTitle));
		}
	}

	private class PreviewListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			previewPage();
		}

	}
	private class PreviewSidebarListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			previewSidebar();
		}
		
	}
	private class ResumeEditListener implements ClickHandler{
		public void onClick(ClickEvent event) {
			resumePreviewPage();
		}
	}
	
	private Button initActionMenu(final PageModel page) {
		int[] permission = page.permissions;
		int attribute = page.attribute;
		
		Button function = new Button(Msg.consts.actions(),ButtonIconBundle.menuImage());
		final ContextMenu menu = new ContextMenu(function);
		
		//write, and page is not "page not found" or "pure draft page" etc
		if(permission[ClientConstants.REMOVE] == 1 && !GwtUtils.contains(attribute, PageAttribute.NO_MOVE)){
			ContextMenuItem moveItem = new ContextMenuItem(Msg.consts.move_to(),new Image(IconBundle.I.get().page_go()), new Command(){
				public void execute() {
					main.editPanel.movePage();
					
				}
			});
			menu.addItem(moveItem);
		}
		
		if(permission[ClientConstants.WRITE] == 1 && !GwtUtils.contains(attribute, PageAttribute.NO_COPY)){
			ContextMenuItem copyItem = new ContextMenuItem(Msg.consts.copy_to(),new Image(IconBundle.I.get().page_copy()), new Command(){
				public void execute() {
					main.editPanel.copyPage();
				}
			});
			menu.addItem(copyItem);
		}
		//remove,and page is not "page not found"
		if(permission[ClientConstants.REMOVE] == 1  && !GwtUtils.contains(attribute, PageAttribute.NO_REMOVE)){
			ContextMenuItem trashItem = new ContextMenuItem(Msg.consts.trash_page(),new Image(IconBundle.I.get().bin_close()), new Command(){
				public void execute() {
					main.editPanel.removePage();
				}
			});
			menu.addItem(trashItem);
		}
		
		
		
		//page permission
		if(permission[ClientConstants.RESTRICT] == 1 && !GwtUtils.contains(attribute, PageAttribute.NO_PERMISSION)){
			ContextMenuItem pagePermItem = new ContextMenuItem(Msg.consts.page_restrict(),new Image(IconBundle.I.get().lock_add()), new Command(){
				public void execute() {
					PageSecurityDialogue dialogue = new PageSecurityDialogue(main);
					dialogue.showbox();
				}
			});
			menu.addItem(pagePermItem);
		}
		
		
		//space admin
		if(permission[ClientConstants.SPACE_BASE+ClientConstants.ADMIN] == 1){
			if(menu.getItemSize() > 0){
				menu.addSeparator();
			}

			//space template
			final ContextMenuItem pinTopPageItem = new ContextMenuItem((page.pintop>0?Msg.consts.remove_pin():Msg.consts.pin_to_top())
					,(page.pintop>0?new Image(IconBundle.I.get().pin()):new Image(IconBundle.I.get().pin_dis())));
			
			//cache status into this menu item, so when user click multiple times, it always can switch correctly.
			pinTopPageItem.setObject(Integer.valueOf(page.pintop));
			pinTopPageItem.setCommand(new Command(){
				public void execute() {
					boolean pined = ((Integer)pinTopPageItem.getObject()>0);
					PageControllerAsync action = ControllerFactory.getPageController();
					action.markPage(page.pageUuid,SharedConstants.USER_PAGE_TYPE_PINTOP,!pined, 
							new PinPageAsync(pinTopPageItem,pined));
					
				}
			});
			menu.addItem(pinTopPageItem);
			
			//space template
			ContextMenuItem spaceTemplateListItem = new ContextMenuItem(Msg.consts.page_templates(),new Command(){
				public void execute() {
					String token = viewTemplateListToken(main.getSpaceUname());
					GwtClientUtils.refreshToken(token);
					
				}
			});
			menu.addItem(spaceTemplateListItem);

			//space admin
			ContextMenuItem spaceAdminItem = new ContextMenuItem(Msg.consts.space_admin(),new Command(){
				public void execute() {
					//goto space admin page
					String token = viewSpaceAdminToken(main.getSpaceUname());
					GwtClientUtils.refreshToken(token);	
				}
			});
			menu.addItem(spaceAdminItem);
		}	
		
		if(permission[SharedConstants.PERM_INSTNACE_MGM] == 1){
			menu.addSeparator();
			ContextMenuItem sysAdminItem = new ContextMenuItem(Msg.consts.system_admin(),ButtonIconBundle.adminImage(), new Command(){
				public void execute() {
					//goto space admin page
					GwtClientUtils.redirect(SharedConstants.URL_INSTANCE_ADMIN);
				}
			});
			menu.addItem(sysAdminItem);
		}
//	    TODO: Comments temporarily
//	    if(permission[16] == 1){
//		    ContextMenuItem expPDFItem = new ContextMenuItem("export PDF",new Command(){
//				public void execute() {
//					Window.alert("working on...");
//				}
//		    });
//		    menu0.addItem(expPDFItem);
//	    }
		
		if(menu.getItemSize() == 0){
			return null;
		}else{

			function.setTitle(Msg.consts.function_title());
			function.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					menu.showMenu();
//					GwtClientUtils.showMenu(function, item, false);
				}
				
			});
			return function;
		}
	}
	
	public void actionDone(){
		if(saveButton != null && !saveButton.isEnabled()){
			saveButton.setBusy(false);
		}
		if(saveDraftButton != null && !saveDraftButton.isEnabled()){
			saveDraftButton.setBusy(false);
		}
		if(forceSaveButton != null && !forceSaveButton.isEnabled()){
			forceSaveButton.setBusy(false);
		}
		if(previewEditButton != null && !previewEditButton.isEnabled()){
			previewEditButton.setBusy(false);
		}
	}


}
