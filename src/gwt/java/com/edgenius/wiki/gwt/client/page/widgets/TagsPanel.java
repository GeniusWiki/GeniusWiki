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
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyMap;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.edgenius.wiki.gwt.client.model.TagModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.render.SearchRenderWidget.TypeImageBundle;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.TagSuggestBox;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class TagsPanel extends Composite implements ClickHandler, FocusHandler, BlurHandler, NativePreviewHandler, AsyncCallback<TagListModel>{
	protected static final int TAG_TEXT = 0;
	protected static final int TAG_EDIT_BOX = 1;
	
	DeckPanel tagsDeck = new DeckPanel();
	private HandlerRegistration evtReg;

	private ClickLink doneTags = new ClickLink(Msg.consts.done());
	private ClickLink cancelTags = new ClickLink(Msg.consts.cancel());
	private ClickLink editTagsLink = new ClickLink(Msg.consts.edit());
	private TagSuggestBox tagsEditBox = new TagSuggestBox(TagSuggestBox.TYPE_PAGE_TAG);
	private FlowPanel tags = new FlowPanel();
	private CaptchaDialog captcha;
	
	public TagsPanel(){
		this.initWidget(tagsDeck);
		
		FlowPanel tagEditPanel = new FlowPanel();
		doneTags.addClickHandler(this);
		cancelTags.addClickHandler(this);
		tagEditPanel.add(tagsEditBox);
		tagEditPanel.add(new HTML("&nbsp;"));
		tagEditPanel.add(doneTags);
		tagEditPanel.add(new HTML("|"));
		tagEditPanel.add(cancelTags);
		tagsEditBox.setHint(Msg.consts.tags()+"...");
		
		tagsDeck.add(tags);
		tagsDeck.add(tagEditPanel);
		tagsDeck.showWidget(TAG_TEXT);
		
		editTagsLink.addClickHandler(this);
		
		tagsEditBox.addFocusHandler(this);
		tagsEditBox.addBlurHandler(this);

		//style
		DOM.setStyleAttribute(tagsEditBox.getElement(), "display", "inline");
		editTagsLink.setStyleName(Css.EDIT_TAGS_LINK);
		cancelTags.setStyleName(Css.EDIT_TAGS_LINK);
		doneTags.setStyleName(Css.EDIT_TAGS_LINK);
		tags.setStyleName(Css.RENDER_TAG);
		
	}


	public void editTag(){
		//To do edit, must 1. Tag show on text status but not on tagEditBox. 2. Allow edit, it means editTagsLink is available.
		if(tags.isVisible()){
			for(Iterator<Widget> iter = tags.iterator();iter.hasNext();){
				if(iter.next().equals(editTagsLink)){
					//allow edit
					tagsDeck.showWidget(TAG_EDIT_BOX);
					tagsEditBox.setFocus(true);
				}
			}
		}
	}

	public void hide() {
		tagsEditBox.setText("");
		tags.clear();
		tagsDeck.showWidget(TAG_TEXT);	
	}
	public void display(String spaceUname, String tagsStr, boolean allowEdit) {
		tagsEditBox.setSpaceUname(spaceUname);

		tagsEditBox.setText("");
		tags.clear();
		
		Image img = new Image(TypeImageBundle.I.get().ptag());
		img.setTitle(Msg.consts.tags());
		tags.add(img);
		
		if(tagsStr != null && tagsStr.trim().length() > 0){
			String[] tagTxts = tagsStr.split(",");
			for(int idx=0;idx<tagTxts.length;idx++){
				final String tagname = tagTxts[idx];
				if(tagname.trim().length() > 0){
					EventfulHyperLink tagLink = new EventfulHyperLink(tagname,
							GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_TAG_CLOUD) ,PageMain.getSpaceUname()));
					new TagPopup(tagLink, PageMain.getSpaceUname(),tagname);
					tagLink.setTitle(Msg.consts.goto_tagcloud());
					tagLink.setStyleName(Css.ITEM);
					tags.add(tagLink);
				}
			}
		}else{
			//show none
			tags.add(new HTML(" ("+Msg.consts.none()+")"));
		}
		
		//offline, disable edit tag
		if(allowEdit && !AbstractEntryPoint.isOffline()){
			//view
			tags.add(new HTML("&nbsp;"));
			tags.add(editTagsLink);
			//edit box
			tagsEditBox.setText(StringUtil.trimStartSpace(tagsStr));
		}
		
		tagsDeck.showWidget(TAG_TEXT);	
	}
	
	
	public void updateTagWithCaptha(String captchaResponse, CaptchaDialog dialog){
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
			TagControllerAsync tagControler = ControllerFactory.getTagController();
			this.captcha = dialog;
			tagControler.savePageTags(PageMain.getSpaceUname(),PageMain.getPageUuid(), tagsEditBox.getText(), model, this);
		}
		
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
	public void onSuccess(TagListModel model) {

		if(!GwtClientUtils.preSuccessCheck(model,null)){
			return;
		}
		
		if(captcha != null){
			captcha.hidebox();
			captcha = null;
		}
		
		tagsEditBox.doneEdit();
		tagsDeck.showWidget(TAG_TEXT);
		
		String tagString = "";
		if(model.tags != null){
			for(Iterator<TagModel> iter = model.tags.iterator();iter.hasNext();){
				TagModel tag = iter.next();
				tagString += tag.name + ",";
			}
		}
		if(tagString.trim().endsWith(",")){
			tagString = tagString.substring(0,tagString.length()-1);
		}
		//always allow edit if it return from edit status
		display(model.spaceUname, tagString, true);
	}
	
	

	public void onPreviewNativeEvent(NativePreviewEvent event){
		int type = event.getTypeInt();
		
		//IE only work for Event.ONKEYDOWN but not Event.ONKEYPRESS (FF is OK)
		if (!event.isCanceled() && type == Event.ONKEYDOWN) {
			NativeEvent evt = event.getNativeEvent();
			int keyCode = evt.getKeyCode();
			boolean ctrl = evt.getCtrlKey();
			boolean alt = evt.getAltKey();
			boolean shift = evt.getShiftKey();
			boolean meta = evt.getMetaKey();
			
			if(keyCode == KeyCodes.KEY_ESCAPE
				|| KeyMap.isKey(KeyMap.EDIT_CANCEL, ctrl, alt, shift, meta, keyCode)){
				cancel();
				event.cancel();
			}else if(KeyMap.isKey(KeyMap.EDIT_SAVE, ctrl, alt, shift, meta, keyCode)){
				done();
				event.cancel();
			}
		}
		
	}
	

	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		
		if(sender == editTagsLink){
			tagsDeck.showWidget(TAG_EDIT_BOX);
			tagsEditBox.setFocus(true);
		}else if(sender == doneTags){
			done();				
		}else if(sender == cancelTags){
			cancel();
		}
	}

	public void onFocus(FocusEvent event) {
		if(evtReg != null){
			evtReg.removeHandler();
			evtReg = null;
		}
		evtReg = Event.addNativePreviewHandler(this);
		
	}

	public void onBlur(BlurEvent event) {
		if(evtReg != null){
			evtReg.removeHandler();
			evtReg = null;
		}
	}
	
	private void cancel() {
		tagsEditBox.setFocus(false);
		tagsEditBox.cancelEdit();
		tagsDeck.showWidget(TAG_TEXT);
	}


	private void done() {
		if(!tagsEditBox.isValid()){
			return;
		}
		tagsEditBox.setFocus(false);
		if(PageMain.isAnonymousLogin()){
			//popup captcha enquire for anonymous user
			CaptchaDialog captcha = new CaptchaDialog(this);
			captcha.showbox();
		}else{
			TagControllerAsync tagControler = ControllerFactory.getTagController();
			this.captcha = null;
			tagControler.savePageTags(PageMain.getSpaceUname(),PageMain.getPageUuid(), tagsEditBox.getText(), null, this);
		}
	}

}
