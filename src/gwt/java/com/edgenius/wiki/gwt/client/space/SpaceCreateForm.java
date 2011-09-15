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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextArea;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBoxValidCallback;
import com.edgenius.wiki.gwt.client.widgets.HelpPopup;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.TagSuggestBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Create new space form.
 * 
 * @author Dapeng.Ni
 */
public class SpaceCreateForm extends Composite implements SubmitHandler,ClickHandler,
		AsyncCallback<SpaceModel>,  FormTextBoxValidCallback, ValueChangeHandler<Boolean>{
	
	
	protected static final int TITLE_MAX_LEN = 100;
	protected static final int SHORTNAME_MAX_LEN = 25;
	protected static final int DESC_MAX_LEN = 255;
	
	//private static char[] INVALID_UNIX_NAME_CHARS = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~".toCharArray();
	
	@UiField MessageWidget message;
	@UiField FormPanel form;
	@UiField FormTextBox name;
	@UiField FormTextBox unixname;
	@UiField FormTextArea desc;
	@UiField TagSuggestBox tagsEditBox;
	@UiField CheckBox privateBox;
	@UiField Image helpOnPrivate;
	@UiField ThemeListPanel themeListPanel;
	@UiField CheckBox linkBlogs;
	@UiField Image helpOnLinkBlog;
	@UiField FlowPanel blogsDeck; 
	@UiField VerticalPanel blogsForm; 
	@UiField com.google.gwt.user.client.ui.Button addBlog; 
	
	interface PanelUiBinder extends UiBinder<Widget, SpaceCreateForm> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	private Vector<SpaceUpdateListener> listener = new Vector<SpaceUpdateListener>();
	
	private DialogBox parent;
	private HelpPopup popOnPrivateSpace;
	private HelpPopup popOnHelpBlog;
	
	private Button createBtn = new Button(Msg.consts.create(),ButtonIconBundle.createImage());
	private Button cancel = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());

	public SpaceCreateForm(DialogBox parent){
		this.parent = parent;
		this.initWidget(uiBinder.createAndBindUi(this));
		
		themeListPanel.load(null);
		themeListPanel.setTarget(parent);
		unixname.valid(Msg.consts.spacekey(), true, 0, SHORTNAME_MAX_LEN, this);

		popOnHelpBlog = new HelpPopup(helpOnLinkBlog, HelpPopup.LINK_BLOG);
		helpOnPrivate.addClickHandler(this);
	
		popOnPrivateSpace = new HelpPopup(helpOnPrivate, HelpPopup.SPACE_PRIVATE);
		
		name.valid(Msg.consts.name(), true, 0, TITLE_MAX_LEN, this);
		desc.valid(Msg.consts.description(), true, 0, DESC_MAX_LEN, null);
		
		createBtn.addClickHandler(this);
		cancel.addClickHandler(this);
		parent.getButtonBar().add(cancel);
		parent.getButtonBar().add(createBtn);

		if(SharedConstants.DISABLE_BLOG_FUNC){
			linkBlogs.setVisible(false);//need remove id enable
			helpOnLinkBlog.setVisible(false);
		}else{
			linkBlogs.addValueChangeHandler(this);
			helpOnLinkBlog.addClickHandler(this);
		}
		blogsDeck.setVisible(false);
		
		addBlog.addClickHandler(this);
		form.addSubmitHandler(this);
	}


	public void onValueChange(ValueChangeEvent<Boolean> event) {
		if(event.getSource() == linkBlogs){
			if(blogsForm.getWidgetCount() == 0){
				addBlankBlogForm();
			}
			blogsDeck.setVisible(event.getValue());
			parent.center();
		}
		
	}

	public void onClick(ClickEvent event) {
		if(event.getSource() == helpOnPrivate)
			popOnPrivateSpace.pop();
		if(event.getSource() == helpOnLinkBlog){
			popOnHelpBlog.pop();
		}else if(event.getSource() == createBtn){
			createBtn.setBusy(true);
			form.submit();
		}else if(event.getSource() == cancel){
			for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
				iter.next().spaceUpdateCancelled();
			}
		}else if(event.getSource() == addBlog){
			addBlankBlogForm();
		}
	}
	public void onSubmit(SubmitEvent event) {
		//validate form
		boolean errors = false;
		if(!name.isValidForSubmit())
			errors = true;
		if(!unixname.isValidForSubmit())
			errors = true;
		if(!desc.isValidForSubmit())
			errors = true;

		
		if(linkBlogs.getValue()){
			//must choose a blog
			if(getSelectedBlog() == null){
				errors = true;
				message.error(Msg.consts.err_need_blog_selected());
			}
		}
		//make RPC call
		if(!errors){
			SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
			SpaceModel space = new SpaceModel();
			space.name = name.getText();
			space.description = desc.getText();
			space.tags = tagsEditBox.getText();
			//space type bring private/public
			space.type = privateBox.getValue()?SharedConstants.PRIVATE_SPACE:SharedConstants.PUBLIC_SPACE;
			
			space.linkBlogMetas = getSelectedBlog();
			space.themeName = themeListPanel.getSelected();
			space.unixName = StringUtil.trimToEmpty(unixname.getText());
			spaceController.createSpace(space, this);
	
		}else{
			//allow second time submit.
			createBtn.setBusy(false);
		}
		
		//always cancel, RPC call could take over
		event.cancel();
	}
	
	/**
	 * @return
	 */
	private Collection<BlogMeta> getSelectedBlog() {
		Collection<BlogMeta>  blogs = new ArrayList<BlogMeta>();
		for(Iterator<Widget> iter = blogsForm.iterator();iter.hasNext();){
			Widget w = iter.next();
			if(w instanceof SpaceLinkedBlogForm){
				SpaceLinkedBlogForm blogForm  = (SpaceLinkedBlogForm) w;
				BlogMeta blog = blogForm.getSelectedBlog();
				if(blog != null){
					blogs.add(blog);
				}
			}
		}
		
		return blogs.size()==0?null:blogs;
	}

	public void blogformRemoved() {
		if(blogsForm.getWidgetCount() == 0){
			//automatically hide blog deck
			linkBlogs.setValue(false);
			blogsDeck.setVisible(false);
			parent.center();
		}
	}
	
	
	public void onFailure(Throwable error) {
		createBtn.setBusy(false);
		GwtClientUtils.processError(error);
	}
	/**
	 * Space created.
	 */
	public void onSuccess(SpaceModel space) {
		//must enable here: for update space, The user could update again, in this case, signin must be enable
		createBtn.setBusy(false);

		if(!GwtClientUtils.preSuccessCheck(space,message)){
			return;
		}
		for(Iterator<SpaceUpdateListener> iter = listener.iterator();iter.hasNext();){
			iter.next().spaceUpdated(space);
		}
		
	}
	public void addListener(SpaceUpdateListener lis){
		listener.add(lis);
	}
	
	public String onBlurValid(Object source) {
		SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
		
		if(source == unixname){
			String err = validateUnixname();
			if(err == null && !StringUtil.isEmpty(unixname.getText())){
				spaceController.isDuplicatedSpace(StringUtil.trim(unixname.getText()), false, new DuplicateSpaceAsyncCallback(false));
			}
			return err;
			
		}else if(source == name){
			if(!StringUtil.isEmpty(name.getText())){
				spaceController.isDuplicatedSpace(StringUtil.trim(name.getText()), true, new DuplicateSpaceAsyncCallback(true));
			}
		}
		return null;
	}

	public String onKeyUpValid(Object source) {
		if(source == unixname){
			return validateUnixname();
		}
		return null;
	}

	private void addBlankBlogForm() {
		SpaceLinkedBlogForm blogForm = new SpaceLinkedBlogForm();
		blogForm.setMessage(message);
		blogForm.setSpaceCreateForm(this);
		blogForm.setCurrentBlog(null);
		blogsForm.add(blogForm);
	}

	private String validateUnixname(){
		String es = null;
		
		String text = StringUtil.trimToEmpty(unixname.getText());
		if(text.length() > 0 && (text.matches("[-]+") || text.startsWith("-") || text.endsWith("-"))){ 
			//start with hyphen is invalid or only contains hyphens
			es = Msg.consts.space_uname_invalid_start();
		}
		
		if(es == null){
			if((text.length() > 0  && !text.matches("[a-zA-Z0-9-]+"))){
				es = Msg.consts.space_uname_invalid_chars();
			}
		}
		
		return es;
	}
	
	private class DuplicateSpaceAsyncCallback implements AsyncCallback<Boolean>{
		boolean isName;
		public DuplicateSpaceAsyncCallback(boolean isName) {
			this.isName = isName;
		}

		public void onFailure(Throwable error) {
		}

		public void onSuccess(Boolean isDuplicated) {
			if(isDuplicated){
				if(isName) name.setError(ErrorCode.getMessageText(ErrorCode.DUPLICATE_SPACE_TITLE_ERR, "Space title is used."));
				if(!isName) unixname.setError(ErrorCode.getMessageText(ErrorCode.DUPLICATE_SPACE_KEY_ERR, "Space key is used."));
			}
		}
		
	}
}
