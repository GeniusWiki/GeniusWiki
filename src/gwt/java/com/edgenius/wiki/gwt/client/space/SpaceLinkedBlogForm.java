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

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogMetaList;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.FormListBox;
import com.edgenius.wiki.gwt.client.widgets.FormPasswordTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class SpaceLinkedBlogForm extends Composite implements ClickHandler, AsyncCallback<BlogMetaList>, ChangeHandler{
	
	@UiField Label blogReadonlyUrl;
	@UiField FormTextBox blogUrl;
	@UiField FormTextBox blogUser;
	@UiField FormPasswordTextBox blogPassword;
	@UiField FlowPanel bloglist;
	@UiField DeckPanel bloglistDeck;
	@UiField Label currentBlog;
	@UiField Label currentBlogLabel;
	@UiField FormListBox blogs;
	@UiField ClickLink disconnect;
	@UiField ClickLink getBlogs;
	
	private List<BlogMeta> blogMetas;
	private MessageWidget message;
	private BlogMeta currentBlogMeta;
	interface PanelUiBinder extends UiBinder<Widget, SpaceLinkedBlogForm> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	private SpaceCreateForm spaceCreateForm;
	
	public SpaceLinkedBlogForm(){
		
		this.initWidget(uiBinder.createAndBindUi(this));
		
		blogs.addChangeHandler(this);
		blogs.addItem("Wordpress", "1");  //hardcode  
//		blogs.addItem("Blogger", "2");  
		blogs.setSelectedIndex(0);
		blogUrl.valid(Msg.consts.url(), true, 0, 0, null);
		blogUser.valid(Msg.consts.user_name(), true, 0, 0, null);
		blogPassword.valid(Msg.consts.password(), true, 0, 0, null);
		
		getBlogs.addClickHandler(this);
		disconnect.addClickHandler(this);
		
		currentBlogLabel.setVisible(false);
		blogReadonlyUrl.setVisible(false);

	}
	public void setMessage(MessageWidget message) {
		this.message = message;
	}

	public void setCurrentBlog(BlogMeta model) {
		this.currentBlogMeta = model;
		if(model != null){
			currentBlogLabel.setVisible(true);
			currentBlog.setText(model.getName());
			if(model.getType() == 2){
				setBloggerURL();
			}else{
				blogUrl.setText(model.getUrl());
			}
			blogUser.setText(model.getUsername());
			blogPassword.setText(model.getPassword());
			disconnect.setObject(model.getKey());
			
			//!!! here assume blog type is index value of list
			blogs.setSelectedIndex(model.getType());
		}else{
			currentBlogLabel.setVisible(false);
			currentBlog.setText("");
			blogUrl.setText("");
			blogUser.setText("");
			blogPassword.setText("");
			blogs.setSelectedIndex(0);
			disconnect.setObject(null);
		}
	}
	public void onChange(ChangeEvent event) {
		if(event.getSource() == blogs.getEventSource()){
			//change blog type
			//clean the possible error message
			int type = NumberUtil.toInt(blogs.getSelectedValue(),0) ;
			if(type > 0){
				blogs.setError(null);
				if(type == 2){
					setBloggerURL();
				}else{
					blogReadonlyUrl.setText("");
					blogUrl.setVisible(true);
					blogReadonlyUrl.setVisible(false);
				}
			}
			
		}
	}

	public void onClick(ClickEvent event) {
		if( event.getSource() == getBlogs){
			//get blog list
			int blogType = NumberUtil.toInt(blogs.getSelectedValue(),0);
			if(blogType == 0){
				blogs.setError(Msg.consts.must_select_blogtype());
				return;
			}
			if(!blogUrl.isValidForSubmit() || !blogUser.isValidForSubmit() || !blogPassword.isValidForSubmit())
				return;
			
			bloglistDeck.showWidget(1);
			SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
			spaceController.getBlogs(blogType, StringUtil.trim(blogUrl.getText()), 
					StringUtil.trim(blogUser.getText()), StringUtil.trim(blogPassword.getText()),this);
		}else if(event.getSource() == disconnect){
			if(Window.confirm(Msg.consts.confirm_disconnect_blog())){
				disconnect();
			}
		}

	}
	

	private void disconnect() {
		this.setVisible(false);

		this.removeFromParent();
		
		if(spaceCreateForm != null){
			spaceCreateForm.blogformRemoved();
		}
	}


	public void onFailure(Throwable error) {
		bloglistDeck.showWidget(0);
	}
	
	public void onSuccess(BlogMetaList blogs) {
		bloglist.clear();
		bloglistDeck.showWidget(0);
		if(!GwtClientUtils.preSuccessCheck(blogs, message)){
			return;
		}
		if(blogs.blogList == null || blogs.blogList.size() == 0){
			bloglist.add(new Label(Msg.consts.none()));
			return;
		}
		this.blogMetas = blogs.blogList;
		
		for (BlogMeta meta : blogs.blogList) {
			RadioButton radio = new RadioButton("blog", meta.getName(), false);
			radio.setFormValue(meta.getId());
			bloglist.add(radio);
		}
		
	}

	public BlogMeta getSelectedBlog() {
		BlogMeta selectedBlog  = null;
		//first, try to check if user select a blog from blog list checkbox 
		
		if(blogMetas != null && blogMetas.size() > 0){
			//following code looks ugly: for get linked blogMeta
			for(Iterator<Widget> iter = bloglist.iterator();iter.hasNext();){
				Widget widget = iter.next();
				if(widget instanceof RadioButton){
					RadioButton radio = ((RadioButton)widget);
					if(radio.getValue()){
						String blogID = radio.getFormValue();
						for (Iterator<BlogMeta> it = blogMetas.iterator();it.hasNext();){
							BlogMeta meta = it.next();
							if(StringUtil.equals(meta.getId(),blogID)){
								selectedBlog = meta;
								break;
							}
						}
					}
				}
				if(selectedBlog != null);
					break;
			}
		}
		
		//if user not select from checkbox, then check if we already have a "currentBlog"
		if(selectedBlog == null){
			selectedBlog = currentBlogMeta;
		}
		
		//all input(URL,user, password) will overwrite existed value, this is true if user want to update - 
		//for example, only update password then save
		//the bad is, we need do further validate in link blog to space as the inputs maybe wrong even they pick up
		//from drop list.
		if(selectedBlog != null){
			int blogType = NumberUtil.toInt(blogs.getSelectedValue(),0);
			if(blogType == 0 || !blogUser.isValidForSubmit()
					|| !blogPassword.isValidForSubmit() || !blogUrl.isValidForSubmit()){
				//if any input are wrong value, return null,means invalid input
				selectedBlog = null;
			}else{
				selectedBlog.setType(blogType);
				selectedBlog.setUsername(blogUser.getText());
				selectedBlog.setPassword(blogPassword.getText());
				selectedBlog.setUrl(blogUrl.getText());
			}
		}
		return selectedBlog;
	}
	private void setBloggerURL() {
		blogReadonlyUrl.setText("www.blogger.com");
		blogUrl.setVisible(false);
		blogReadonlyUrl.setVisible(true);
	}
	public void setSpaceCreateForm(SpaceCreateForm spaceCreateForm) {
		this.spaceCreateForm = spaceCreateForm;
	}

}
