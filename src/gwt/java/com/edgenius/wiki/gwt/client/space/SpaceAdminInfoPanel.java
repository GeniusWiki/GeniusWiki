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
package com.edgenius.wiki.gwt.client.space;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class SpaceAdminInfoPanel  extends Composite implements SpaceUpdateListener ,AsyncCallback<SpaceModel>, ClickHandler {

	private static final int TOKEN_SPACE_BLOG_FORM = 3;
	private static final int TOKEN_SPACE_LOGO_FORM = 2;
	private static final int TOKEN_SPACE_UPDATE_FORM = 1;
	private static final int TOKEN_SPACE_FUNC_LIST = 0;
	
	interface PanelUiBinder extends UiBinder<Widget, SpaceAdminInfoPanel> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	@UiField Label spacenameLabel;
	@UiField Label unixname;
	@UiField Label descLabel;
	@UiField Label quotaLabel;
//	@UiField FlowPanel linkBlogLabel;
	@UiField Label privateSpaceLabel;
	@UiField DeckPanel deck;
	@UiField MessageWidget message;
	@UiField SimplePanel logo;
	
	//panel in deck
	@UiField VerticalPanel funcPanel;
	@UiField SpaceUpdateForm detailForm;
	@UiField SpaceLogoForm logoForm;
//	@UiField VerticalPanel blogsForm;
//	@UiField Button addBlog; 
//	@UiField Button updateBlog;
//	@UiField Button cancelBlog;
	
	private String spaceUname;
//	private Collection<BlogMeta> currentBlogMeta;
	
	public SpaceAdminInfoPanel(String spaceUname){
		this.spaceUname = spaceUname;
		this.initWidget(uiBinder.createAndBindUi(this));;
		
		logoForm.addListener(this);
		detailForm.addListener(this);
		
//		updateBlog.addClickHandler(this);
//		cancelBlog.addClickHandler(this);
//
//		if(SharedConstants.DISABLE_BLOG_FUNC){
//			linkBlogLabel.setVisible(false);
//		}else{		
//			addBlog.addClickHandler(this);
//			DOM.setStyleAttribute(addBlog.getElement(), "width", "130px");
//		}
		//refresh page
		SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
		spaceController.getSpace(spaceUname,this);

	}

	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
	}
	/*
	 * Get space success return
	 */
	public void onSuccess(SpaceModel model) {
		
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		message.cleanMessage();
		refreshInfo(model);
		
	}

	public void spaceUpdateCancelled() {
		deck.showWidget(TOKEN_SPACE_FUNC_LIST);
	}

	public void spaceUpdated(SpaceModel model) {
		//update infopanel then show the function on left side
		refreshInfo(model);
		deck.showWidget(TOKEN_SPACE_FUNC_LIST);
		
	}	
	public void logoUpdated(String smallLogoUrl, String largeLogoUrl) {
		logo.clear();
		logo.setWidget(GwtClientUtils.createSpaceLogo(largeLogoUrl));
		
		deck.showWidget(TOKEN_SPACE_FUNC_LIST);
	}
	private void refreshInfo(SpaceModel model){
		
		funcPanel.clear();
		
		//admin permission
		if(model.permissions[ClientConstants.ADMIN] == 1 && !model.isRemoved){
			ClickLink detailBtn = new ClickLink(Msg.consts.edit_detail());
			detailBtn.setObject(TOKEN_SPACE_UPDATE_FORM);
			detailBtn.addClickHandler(this);
			funcPanel.add(detailBtn);
			
			ClickLink updateBtn = new ClickLink(Msg.consts.update_logo());
			updateBtn.setObject(TOKEN_SPACE_LOGO_FORM);
			updateBtn.addClickHandler(this);
			funcPanel.add(updateBtn);
			
			if(!SharedConstants.DISABLE_BLOG_FUNC){
				ClickLink editBlog = new ClickLink(Msg.consts.edit_blog());
				editBlog.setObject(TOKEN_SPACE_BLOG_FORM);
				editBlog.addClickHandler(this);
				funcPanel.add(editBlog);
			}
		}
		
		//remove also need check admin permission
		if(model.permissions[ClientConstants.ADMIN] == 1){
			funcPanel.add(getRemoveActionLink(model.isRemoved,model.delayRemoveHours));
		}

		deck.showWidget(TOKEN_SPACE_FUNC_LIST);
		detailForm.refresh(model);
		unixname.setText(model.unixName);
		spacenameLabel.setText(model.name);
		descLabel.setText(model.description);
		if(model.quota == null || model.quota.length != 2)
			quotaLabel.setText(Msg.consts.unknown());
		else{
			if(model.quota[1] <= 0)
				quotaLabel.setText(Msg.params.attach_quota(model.quota[0]+"", Msg.consts.unlimited()));
			else
				quotaLabel.setText(Msg.params.attach_quota(GwtUtils.convertHumanSize(model.quota[1] - model.quota[0]), GwtUtils.convertHumanSize(model.quota[1])));
		}
		
		if(model.type == SharedConstants.PRIVATE_SPACE){
			privateSpaceLabel.setText(Msg.consts.yes());
		}else{
			privateSpaceLabel.setText(Msg.consts.no());
		}
		
//		displayBlogLink(model.linkBlogMetas);
		logoForm.fillFields(model);
		logo.clear();
		logo.setWidget(GwtClientUtils.createSpaceLogo(model.largeLogoUrl));
	}

	
	public void onClick(ClickEvent event) {
		Object src = event.getSource();
//		if(src == updateBlog){
//			
//			SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
//			spaceController.updateLinkedBlog(spaceUname,getSelectedBlog() ,new UpdateLinkedBlogSync());
//		}else if(src == cancelBlog){
//			
//			//user may click disconnect - here restore them.
//			displayBlogLink(this.currentBlogMeta);
//			deck.showWidget(TOKEN_SPACE_FUNC_LIST);
//		}else if(src == addBlog){
//			
//			addBlogForm(null);
//		}else 
//			
		if(src instanceof ClickLink){
			
			//function link to switch deckPanel
			deck.showWidget((Integer)(((ClickLink)event.getSource()).getObject()));
		}
	}
//	private ArrayList<BlogMeta> getSelectedBlog() {
//		ArrayList<BlogMeta>  blogs = new ArrayList<BlogMeta>();
//		for(Iterator<Widget> iter = blogsForm.iterator();iter.hasNext();){
//			Widget w = iter.next();
//			if(w instanceof SpaceLinkedBlogForm){
//				SpaceLinkedBlogForm blogForm  = (SpaceLinkedBlogForm) w;
//				BlogMeta blog = blogForm.getSelectedBlog();
//				if(blog != null){
//					blogs.add(blog);
//				}
//			}
//		}
//		
//		return blogs.size()==0?null:blogs;
//	}
//	/**
//	 * @param blog
//	 */
//	private void displayBlogLink(Collection<BlogMeta> model) {
//		this.currentBlogMeta = model;
//		linkBlogLabel.clear();
//		blogsForm.clear();
//		boolean hasLinked = false;
//		if(model != null){
//			int idx= 0;
//			int len = model.size()-1;
//			for (BlogMeta blog:model) {
//				if(!StringUtil.isBlank(blog.getUrl())){
//					Anchor blogLink = new Anchor();
//					blogLink.setHref(blog.getUrl());
//					blogLink.setText(blog.getName());
//					blogLink.setTarget("_blank");
//					linkBlogLabel.add(blogLink);
//					if(idx != len)
//						linkBlogLabel.add(new Label(", "));
//					addBlogForm(blog);
//					hasLinked = true;
//				}
//				idx++;
//			}
//		}
//		if(!hasLinked){
//			//display a blank blog form
//			addBlogForm(null);
//			linkBlogLabel.add(new HTML(Msg.consts.no()));
//		}
//	}
	//********************************************************************
	//               private methods
	//********************************************************************

	/**
	 * @param blog
	 */
//	private void addBlogForm(BlogMeta blog) {
//		SpaceLinkedBlogForm blogForm = new SpaceLinkedBlogForm();
//		blogForm.setMessage(message);
//		blogForm.setCurrentBlog(blog);
//		blogsForm.add(blogForm);
//	}

	private ClickLink getRemoveActionLink(boolean removed, String delayHours){
		ClickLink removeBtn;
		if(removed){
			removeBtn = new ClickLink(Msg.consts.cancel_removed_space());
			removeBtn.setStyleName(Css.WARNING_LINK);
			removeBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					SpaceControllerAsync action = ControllerFactory.getSpaceController();
					action.restoreSpace(spaceUname, new RestoreSpaceAsync());
				}
			});
			message.warning(Msg.params.remove_delay(delayHours));
		}else{
			removeBtn = new ClickLink(Msg.consts.remove_space());
			removeBtn.setStyleName(Css.WARNING_LINK);
			removeBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					if(Window.confirm(Msg.consts.confirm_remove_space())){
						SpaceControllerAsync action = ControllerFactory.getSpaceController();
						action.removeSpace(spaceUname, new RemoveSpaceAsync());
					}
				}
			});
		}
		return removeBtn;
	}
	//********************************************************************
	//               private class
	//********************************************************************

	private class RemoveSpaceAsync implements AsyncCallback<SpaceModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(SpaceModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			if(model.delayRemoveHours == null || model.delayRemoveHours.trim().length() == 0){
				//delete immediately happen: goto dashboard:
				GwtClientUtils.refreshToken("");
			}else{
				funcPanel.clear();
				funcPanel.add(getRemoveActionLink(model.isRemoved,model.delayRemoveHours));
				message.warning(Msg.params.remove_delay(model.delayRemoveHours));
			}
		}
		
	}
//	private class UpdateLinkedBlogSync implements AsyncCallback<BlogMetaList>{
//		public void onFailure(Throwable error) {
//			GwtClientUtils.processError(error);
//		}
//		public void onSuccess(BlogMetaList model) {
//			if(!GwtClientUtils.preSuccessCheck(model,message)){
//				return;
//			}
//			displayBlogLink(model.blogList);
//			deck.showWidget(TOKEN_SPACE_FUNC_LIST);
//		}
//		
//	}
	private class RestoreSpaceAsync implements AsyncCallback<SpaceModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		public void onSuccess(SpaceModel model) {
	
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			message.cleanMessage();
			refreshInfo(model);
		}
		
	}
	

}
