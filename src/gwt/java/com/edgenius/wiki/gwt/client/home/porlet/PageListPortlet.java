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
package com.edgenius.wiki.gwt.client.home.porlet;

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.portal.Portlet;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public abstract class PageListPortlet extends Portlet{
	protected int FILL_TYPE_PAGE = 0;
	protected int FILL_TYPE_DRAFT = 1;
	protected int FILL_TYPE_MYPAGE = 3;
	
	protected int STYLE_SHOW_PORTRAIT = 1;
	protected int STYLE_SHOW_CREATED_BY = 1 << 1;
	protected int STYLE_SHOW_MODIFIED_BY = 1 << 2;
	
	/*
	 * Although this method only used by MyPageList draft type, but put it here from maintenance reason: it is all same 
	 * with fillPageList() method except URL.
	 */
	protected void fillList(List<PageItemModel> pages, int fillType, int style) {
		container.clear();
		//show page items one by one on fragment panel
		if(pages != null && pages.size() > 0){
			for(Iterator<PageItemModel> iter = pages.iterator();iter.hasNext();){
				final PageItemModel item = iter.next();
				FlowPanel itemPanel = new FlowPanel();
				
				if(fillType == FILL_TYPE_DRAFT){
					//append delete image to allow delete draft from portlet
					Image dot; 
					if(item.type == PageType.MANUAL_DRAFT){
						dot = new Image(IconBundle.I.get().bullet_green());
						dot.setTitle(Msg.consts.manual_draft());
					}else if(item.type == PageType.AUTO_DRAFT){
						dot = new Image(IconBundle.I.get().bullet_yellow());
						dot.setTitle(Msg.consts.auto_draft());
					}else{
						dot = new Image(IconBundle.I.get().bullet_red());
						dot.setTitle(Msg.consts.conflict_draft());
					}
					itemPanel.add(dot);
				}
				Hyperlink title;
				if(fillType == FILL_TYPE_DRAFT){
					title = getDraftLink(item);
				}else{
					//default Page Link
					title = getPageLink(item);
				}
				Label date = new Label(GwtClientUtils.toDisplayDate(item.modifiedDate));
				date.setStyleName(Css.PORTLET_WEAK_TEXT);
	
				//add key of this item
				itemPanel.add(new Hidden(item.uuid,item.uuid+item.type));

				if(item.pinTop){
					itemPanel.add(new Image(IconBundle.I.get().pin_small()));
				}
				
				itemPanel.add(title);
				if(fillType == FILL_TYPE_DRAFT || fillType == FILL_TYPE_MYPAGE){
					//append spaceUname
					Label spaceLabel = new Label("("+item.spaceUname+")");
					spaceLabel.setStyleName(Css.PORTLET_WEAK_TEXT);
					itemPanel.add(spaceLabel);
				}
				itemPanel.add(date);
				if(fillType == FILL_TYPE_DRAFT){
					//append delete image to allow delete draft from portlet
					Image bin = new Image(IconBundle.I.get().bin_close());
					bin.addClickHandler(new ClickHandler(){
						public void onClick(ClickEvent event) {
							if(Window.confirm(Msg.params.confirm_delete_draft(item.title))){
								//put enable busy before async call as offline call immediately return and busy icon won't disappear if 
								//enable after async call
								busy(true);
								PageControllerAsync pageController = ControllerFactory.getPageController();
								pageController.removeDraft(item.spaceUname, item.uuid, item.type, new RefreshDraftAsync());
							}
						}								
					});
					bin.setTitle(Msg.consts.delete_draft());
					itemPanel.add(bin);
				}
				
				if((style & STYLE_SHOW_CREATED_BY) > 0 || (style & STYLE_SHOW_MODIFIED_BY) > 0){
					//please be aware of, the PageItem has only modifier info - this will be filled to creator info when 
					//widget style is WIDGET_STYLE_ITEM_SHORT_BY_CREATE_DATE
					UserProfileLink modifier = new UserProfileLink(item.modifier, item.spaceUname,item.modifierUsername,item.modifierPortrait);
					Label mL;
					if((style & STYLE_SHOW_CREATED_BY) > 0)
						mL = new Label(Msg.consts.created_by());
					else
						mL = new Label(Msg.consts.modified_by());
					
					mL.setStyleName(Css.PORTLET_WEAK_TEXT);
					modifier.setStyleName(Css.WEAK_LINK);
					itemPanel.add(mL);
					itemPanel.add(modifier);
				}
				if((style & STYLE_SHOW_PORTRAIT) > 0){
					HorizontalPanel panel = new HorizontalPanel();
					Widget portrait = GwtClientUtils.createUserSmallPortrait(item.modifierPortrait, SharedConstants.PORTRAIT_SIZE_SMALL);
					panel.add(portrait);
					panel.setCellWidth(portrait, "50px");
					panel.add(itemPanel);
					container.add(panel);
					panel.setStyleName(Css.ITEM);
				}else{
					itemPanel.setStyleName(Css.ITEM);
					container.add(itemPanel);
				}
			}
		}else{
			empty();
		}
	}


//	/**
//	 * @param item
//	 * @return
//	 */
//	private Hyperlink getMessageLink(final PageItemModel item) {
//		String token = SharedConstants.CPAGE_MESSAGE_BOARD+ PageMain.TOKEN_CPAGE + item.uuid;
//		Hyperlink title = new Hyperlink(item.title,token);
//		return title;
//	}

	/**
	 * Display (none) in porlet.
	 */
	protected void empty(){
		container.clear();
		none.setStyleName(Css.PORTLET_WEAK_TEXT);
		container.add(none);
	}
	/**
	 * @param item
	 * @return
	 */
	private Hyperlink getPageLink(final PageItemModel item) {
		String token = GwtUtils.getSpacePageToken(item.spaceUname, item.title);
		Hyperlink title = new Hyperlink(item.title,token);
		return title;
	}
	/**
	 * @param item
	 * @return
	 */
	private Hyperlink getDraftLink(final PageItemModel item) {
		return new Hyperlink(item.title, GwtUtils.buildToken(PageMain.TOKEN_DRAFT, item.spaceUname,String.valueOf(item.type), String.valueOf(item.uid)));
	}
	
	private class RefreshDraftAsync implements AsyncCallback<PageModel>{

		public void onFailure(Throwable error) {
			busy(false);
			GwtClientUtils.processError(error);
		}

		public void onSuccess(PageModel model) {
			busy(false);
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				Window.alert(ErrorCode.getMessageText(model.errorCode, model.errorMsg));
				return;
			}
			
			//retrieve all items in draft portlet and delete corresponding one
			boolean removed = false;
			for(Iterator<Widget> iter = container.iterator();iter.hasNext();){
				Widget w = iter.next();
				if(w instanceof FlowPanel){
					FlowPanel panel= (FlowPanel) w;
					for(Iterator<Widget> flowIter = panel.iterator();flowIter.hasNext();){
						w = flowIter.next();
						if(w instanceof Hidden){
							if((model.pageUuid+model.type).equals(((Hidden)w).getValue())){
								//remove current item from container
								iter.remove();
								removed = true;
							}
							break;
						}
					}
					if(removed)
						break;
				}
			}
			if(container.getWidgetCount() == 0){
				empty();
			}
		}
		
	}
}
