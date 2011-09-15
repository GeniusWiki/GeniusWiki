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

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.KeyMap;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.CommentListModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.page.widgets.CaptchaDialog;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.CommentControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class CommentPanel extends PinPanel  implements AsyncCallback<CommentListModel> {
	 
	private CommentBox box;
	
	private Label noCommentLabel = new Label(Msg.consts.no_comment());
	private ClickLink addCommentBtn = new ClickLink(Msg.consts.post_comment());
	private CaptchaDialog captcha;

	private PageTabPanel parent;

	public CommentPanel(PageMain main,PageTabPanel parent){
		super(main);
		this.parent = parent;
		addCommentBtn.setStyleName(Css.LARGE_LINK_BTN);
		addCommentBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				//add new comment
				addBox(null);
			}
		});
		noCommentLabel.setStyleName(Css.BLANK_MSG);
		content.addStyleName(Css.COMMENT_PANEL);
	}

	/**
	 * Load page comments, pageUuid and spaceUname get from HTML hidden variable(PageMain)
	 */
	public void loadComment() {
		CommentControllerAsync action = ControllerFactory.getCommentController();
		action.getPageComments(main.getSpaceUname(),main.getPageUuid(), this);
		
	}
	/**
	 * Show page comment failure
	 */
	public void onFailure(Throwable error) {
		showBusy(false);
		GwtClientUtils.processError(error);
	}
	/**
	 * Show page comment success
	 */
	public void onSuccess(CommentListModel model) {
		showBusy(false);

		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		if(model.perms == null || model.perms.length != 2 || model.perms[ClientConstants.READ] == SharedConstants.FORBID){
			//not view permission comment
			//only clear content part, don't reset comment tab as it may has comment number... it allows user view even they have not
			//permission to view comment detail
			content.clear();
			message.info(Msg.consts.no_view_permission());
			//does not allow post
			header.remove(addCommentBtn);
			return;
		}
		if(model.perms[ClientConstants.WRITE] !=  SharedConstants.FORBID)
			header.add(addCommentBtn,DockPanel.EAST);
		refreshComments(model);
	}
	public void clear(){
		content.clear();
		parent.resetCommentCount("");
	}
	/**
	 * @param parentUid
	 * @param rootUid
	 */
	private void saveComment(String text, final Integer parentUid, final Integer rootUid){
		if(main.isAnonymousLogin()){
			//anonymous need catpcha
			//popup captcha enquire for anonymous user
			captcha = new CaptchaDialog(this,text,parentUid,rootUid);
			captcha.showbox();
		}else{
			saveCommentWithCaptcha(text,parentUid,rootUid,null);
		}
	}
	public void saveCommentWithCaptcha(String text, final Integer parentUid, final Integer rootUid, String captchaResponse) {
		CommentModel comment = new CommentModel();
		if(captchaResponse != null){
			comment.reqireCaptcha = true;
			comment.captchaCode = captchaResponse;
		}
		CommentControllerAsync commentController = ControllerFactory.getCommentController();
		comment.body = text;
		//if parent has not root, it means parent is root.
		comment.rootUid = rootUid == null? parentUid:rootUid;
		comment.parentUid = parentUid;
		commentController.createComment(main.getSpaceUname(), main.getPageUuid(),comment, new CreateAsync());
		
	}
	
	/**
	 * Add a commentBox under parent. Put it on top if parent null 
	 * @param parent
	 */
	public void addBox(CommentItem parent){
		//only allow on comment box exist, so always remove all, then show a box
		removeBox();
		
		if(parent == null){
			box = new CommentBox();
			content.insert(box,0);
		}else{
			box = new CommentBox(parent.getUid(),parent.getRootUid());
			int size = content.getWidgetCount();
			for(int idx=0;idx<size;idx++){
				Widget w = (Widget) content.getWidget(idx);
				if(w.equals(parent)){
					//append post comment textarea under current widget
					if(idx == size-1){
						//last
						content.add(box);
					}else
						content.insert(box, idx+1);
				}
			}
		}
		box.setFocus(true);
	}
	/**
	 * Resume post button from disable
	 */
	public void saveFunctionDone() {
		
		if(box != null){
			box.enablePostButton();
		}
	}


	//********************************************************************
	//               private method
	//********************************************************************
	private void refreshComments(CommentListModel model){
		//don't simply do content.clear(); try keep first Post box if it has
		for(Iterator<Widget> iter = content.iterator();iter.hasNext();){
			Widget w = iter.next();
			if(w instanceof CommentBox){
				continue;
			}
			iter.remove();
		}
		
		List<CommentModel> list = model.comments;
		if(list != null && list.size() > 0){
			for(Iterator<CommentModel> iter = list.iterator();iter.hasNext();){
				CommentModel comment = iter.next();
				CommentItem item = new CommentItem(comment,model.perms[1] ==0?true:false, model.isSpaceAdmin);
				content.add(item);
			}
		}else{
			content.add(noCommentLabel);
		}
	}
	private void insertComment(CommentModel model, boolean readonly, boolean isSpaceAdmin){
		//remove "No comments" label
		for(Iterator<Widget> iter = content.iterator();iter.hasNext();){
			Widget w = iter.next();
			if(w == noCommentLabel){
				iter.remove();
				break;
			}
		}
		
		//last post in root level.
		if(model.parentUid == null){
			CommentItem newitem = new CommentItem(model,readonly, isSpaceAdmin);
			content.add(newitem);
			return;
		}
		
		//replied post:
		int size = content.getWidgetCount();
		//append the last one in children list
		boolean foundParent = false;
		
		for(int idx=0;idx<size;idx++){
			Widget w = (Widget) content.getWidget(idx);
			if(w instanceof CommentItem){
				CommentItem item = (CommentItem) w;
				if(foundParent && item.getLevel() < model.level){
					//arrive last child, insert now
					CommentItem newitem = new CommentItem(model,readonly,isSpaceAdmin);
					content.insert(newitem,idx);
					return;
				}
				if(item.getUid().equals(model.parentUid)){
					//just mark, its parent is found, and continue to find last children of this parent
					foundParent = true;
				}
			}
		}
		//for last topic reply: it will find parent, but never find a level less than its. So no change to insert. Just append it to last one
		//it is last post
		CommentItem newitem = new CommentItem(model,readonly,isSpaceAdmin);
		content.add(newitem);
	}
	
	/**
	 * Remove all commentBox.
	 */
	private void removeBox(){
		for(Iterator<Widget> iter = content.iterator();iter.hasNext();){
			Widget w = iter.next();
			if(w instanceof CommentBox){
				CommentBox commentBox = ((CommentBox)w);
				//IE7: does not invoke onLostFocus() event when only call setFocus(false)!sucks IE!!!
				//so here directly call removeEventMethod
				commentBox.removeEventPreview();
				iter.remove();
				
				KeyCaptureListener.setCapture(true);
				box = null;
			}
		}
	}
	//********************************************************************
	//               private class
	//********************************************************************

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//	CommentBox class 
	private class CommentBox extends SimplePanel implements FocusHandler, BlurHandler, NativePreviewHandler {
		private VerticalPanel panel = new VerticalPanel();
		private TextArea textBox = new TextArea();
		Button postBtn = new Button(Msg.consts.post(),ButtonIconBundle.diskImage());
		
		private Integer rootUid;
		private Integer parentUid;
		private HandlerRegistration evtReg;
		public CommentBox(){
			this(null,null);
		}

		public CommentBox(Integer parentUid, Integer rootUid){
			this.parentUid = parentUid;
			this.rootUid = rootUid;
			
			textBox.addFocusHandler(this);
			textBox.addBlurHandler(this);
			textBox.addFocusHandler(KeyCaptureListener.instance());
			textBox.addBlurHandler(KeyCaptureListener.instance());
			
			ButtonBar btnBar = new ButtonBar();
			
			postBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					saveCommentInternal(CommentBox.this.parentUid, CommentBox.this.rootUid);
				}
			});
			Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
			cancelBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					cancel();
				}
			});
			
			btnBar.setStyleName(Css.SMALL_BUTTONS);
			btnBar.add(postBtn);
			btnBar.add(cancelBtn);
			panel.add(textBox);
			panel.add(btnBar);
			
			textBox.setStyleName(Css.COMM_TEXTAREA);
			this.setWidget(panel);
			
		}
		
		public void setFocus(boolean focus){
			textBox.setFocus(focus);
		}

		public void enablePostButton() {
			postBtn.setEnabled(true);
		}
		/**
		 * @param parentUid
		 * @param rootUid
		 */
		private void saveCommentInternal(final Integer parentUid, final Integer rootUid) {
			String text = textBox.getText();
			
			if(text == null || text.trim().length() == 0){
				Window.alert(Msg.consts.comment_no_empty());
				return;
			}
			saveComment(text, parentUid, rootUid);
			//to avoid second submit.
			postBtn.setEnabled(false);
		}

		public void onFocus(FocusEvent event) {
			//remove old one first
			removeEventPreview();
			evtReg = Event.addNativePreviewHandler(this);
		}

		public void onBlur(BlurEvent event) {
			removeEventPreview();
		}
	
		public void removeEventPreview() {
			if(evtReg != null){
				evtReg.removeHandler();
				evtReg = null;
			}
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Event preview class for CommentBox 
		//allow user short cut to save post:
		public void onPreviewNativeEvent(NativePreviewEvent event) {
			int type = event.getTypeInt();
			
			//IE only work for Event.ONKEYDOWN but not Event.ONKEYPRESS (FF is OK)
			if (!event.isCanceled() && type == Event.ONKEYDOWN) {
				NativeEvent nevt = event.getNativeEvent();
				int keyCode = nevt.getKeyCode();
				boolean ctrl = nevt.getCtrlKey();
				boolean alt = nevt.getAltKey();
				boolean shift = nevt.getShiftKey();
				boolean meta = nevt.getMetaKey();
				
				if(postBtn.isEnabled() && KeyMap.isKey(KeyMap.EDIT_SAVE, ctrl, alt, shift, meta, keyCode)){
					saveCommentInternal(parentUid, rootUid);
					event.cancel();
				}else if(keyCode == KeyCodes.KEY_ESCAPE
						||KeyMap.isKey(KeyMap.EDIT_CANCEL, ctrl, alt, shift, meta,keyCode)){
					cancel();
					event.cancel();
				}
			}
			
		}
		
		private void cancel(){
			String text = textBox.getText();
			if(text != null && text.trim().length() > 0){
				if(!Window.confirm(Msg.consts.discard_comment()))
					return;
			}
			removeBox();

		}


	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//           CommentItem    class 
	private class CommentItem extends SimplePanel implements ClickHandler{
		ClickLink replyLink = new ClickLink(Msg.consts.reply());
		ClickLink hideLink = new ClickLink();
		//TODO: need add this remove function
//		ClickLink remove = new ClickLink("remove");
		private Integer uid;
		private Integer rootUid;
		private int level;
		private FlowPanel body = new FlowPanel();
		public CommentItem(final CommentModel model, boolean readonly,boolean isSpaceAdmin){
			uid = model.uid;
			rootUid = model.rootUid;
			level = model.level;
	
			HorizontalPanel info = new HorizontalPanel();
			setBody(model.body,model.hide);
			
			String userPanelID = HTMLPanel.createUniqueId();
			HTMLPanel postBy = new HTMLPanel(Msg.params.post_by(userPanelID,GwtClientUtils.toDisplayDate(model.modifiedDate)));
			replyLink.addClickHandler(this);
//			remove.addClickHandler(this);
			info.add(new Image(IconBundle.I.get().comment()));
			info.add(postBy);
//			info.add(remove);
			if(!readonly){
				if( isSpaceAdmin || (main.getLoginUser() != null && !GwtUtils.isAnonymous(main.getLoginUser()) &&
					StringUtil.equalsIgnoreCase(main.getLoginUser().loginUsername,model.authorUsername))){
					hideLink.addClickHandler(this);
					info.add(hideLink);
				}
				info.add(replyLink);
			}
			
		
			UserProfileLink authorPop = new UserProfileLink(model.author, main.getSpaceUname(),model.authorUsername,model.authorPortrait);
			postBy.add(authorPop, userPanelID);
			
			
			Widget portrait = GwtClientUtils.createUserSmallPortrait(model.authorPortrait, 50);
			FlexTable panel = new FlexTable();
			panel.setWidget(0, 0, portrait);
			panel.setWidget(0, 1, body);
			panel.setWidget(1, 0, info);
			
			panel.getFlexCellFormatter().setWidth(0, 0, "1%");
			panel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_TOP);
			panel.getFlexCellFormatter().setRowSpan(0, 0, 2);
			panel.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_BOTTOM);
			
			this.setStyleName(Css.COMM_ITEM);
			body.setStyleName(Css.COMM_CONTENT);
			postBy.setStyleName(Css.COMM_POST_BY);
			DOM.setStyleAttribute(this.getElement(), "marginLeft", 3*model.level +"em");
			this.setWidget(panel);
		}

		public void onClick(ClickEvent event) {
			if(event.getSource() == replyLink){
				addBox(this);
				
			}else if(event.getSource() == hideLink){
				boolean hidden = hideLink.getObject().equals(Boolean.TRUE);
				String showMsg = Msg.consts.hide();
				if(hidden)
					showMsg = Msg.consts.show();
				
				if(Window.confirm(Msg.params.confirm_hide_comment(showMsg))){
					CommentControllerAsync commentController = ControllerFactory.getCommentController();
					commentController.hideComment(uid, !hidden, new HideAsync(CommentItem.this));
				}
			}
//			else if(sender == remove){
				//remove this comment
		}
		public Integer getUid() {
			return uid;
		}

		public Integer getRootUid() {
			return rootUid;
		}
		public int getLevel() {
			return level;
		}

		/**
		 * @param body2
		 */
		public void setBody(String text, boolean hidden) {
			//replace \n to <br>, but body won't display other tags, such as javascript, so use label to display
			if(text == null){
				text = "";
			}
			body.clear();
			StringBuilder buf = new StringBuilder();
			for(char ch : text.toCharArray()){
				if(ch == '\n'){
					if(buf.length() > 0){
						body.add(new Label(buf.toString()));
						buf = new StringBuilder();
					}
					body.add(new HTML("<br>"));
					continue;
				}
				buf.append(ch);
			}
			if(buf.length() > 0)
				body.add(new Label(buf.toString()));
			
			if(hidden){
				hideLink.setText(Msg.consts.show());
				hideLink.setObject(Boolean.TRUE);
			}else{
				hideLink.setText(Msg.consts.hide());
				hideLink.setObject(Boolean.FALSE);
			}
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//           CreateAsync    class 
	private class HideAsync implements AsyncCallback<CommentModel>{
		
		private CommentItem item;

		public HideAsync(CommentItem item){
			this.item = item;
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}

		public void onSuccess(CommentModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			item.setBody(model.body,model.hide);
		}
		
	}
	private class CreateAsync implements AsyncCallback<CommentModel>{
		/**
		 * Comment added failure
		 */
		public void onFailure(Throwable error) {
			//anyway enable the post button first
			if(captcha != null){
				captcha.enableSubmit();
			}
			saveFunctionDone();
			if(error instanceof CaptchaVerifiedException){
				captcha.refreshCaptch();
			}else{
				GwtClientUtils.processError(error);
			}
		}
		/**
		 * Comment added success
		 */
		public void onSuccess(CommentModel model) {
			removeBox();
			if(captcha != null){
				captcha.hidebox();
				captcha = null;
			}

			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			//must has write permission so far.
			//set isSpaceAdmin as false is harmless as this comment must be login use - then allow hide/show 
			insertComment(model,false, false);
			if(model.pageCommentCount > 0){
				//create must has at least one more comment, so must greater than 0
				parent.resetCommentCount(" (" + model.pageCommentCount+")");
			}
		}
	}


}
