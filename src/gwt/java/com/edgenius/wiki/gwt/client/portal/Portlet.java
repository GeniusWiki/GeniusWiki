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
package com.edgenius.wiki.gwt.client.portal;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.render.PageRender;
import com.edgenius.wiki.gwt.client.render.RenderPanel;
import com.edgenius.wiki.gwt.client.render.WikiRenderPanel;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.edgenius.wiki.gwt.client.widgets.PortletContext;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class Portlet  extends SimplePanel implements AsyncCallback<PortletModel>{
	public static final String ATTR_MODEL = "model";
	
	protected PortletContext context;
	//each portlet should have an unique ID: for my page, it has hardcode value (favourite,draft etc.)
	//for space, it use spaceUname.
	private String uid;
	
	protected HeaderDragHandler header = new HeaderDragHandler();
	protected FlowPanel footer = new FlowPanel();
	
	protected VerticalPanel container = new VerticalPanel();
	
	protected Vector<PortletListener> listeners = new Vector<PortletListener>();
	protected PortletModel portletModel;
	protected UserModel viewUser;
	private RenderPanel panel = new WikiRenderPanel();
	protected Label none = new Label("("+Msg.consts.none()+")");
	public Portlet(){
		
		VerticalPanel panel = new VerticalPanel();
		panel.setWidth("100%");
		
		panel.add(header);
		panel.add(container);
		panel.add(footer);
		header.setStyleName(Css.PORTLET_HEADER);
		footer.setStyleName(Css.PORTLET_FOOTER);
		container.setStyleName(Css.PORTLET_CONTAINER);
		this.setStyleName(Css.PORLET);
		this.setWidget(panel);
	}

	public void addPortletListener(PortletListener listener){
		listeners.add(listener);
	}
	public void init(PortletContext context) {
		this.context = context;
		portletModel = (PortletModel) context.getAttribute(ATTR_MODEL);
		setUid(portletModel.type+SharedConstants.PORTLET_SEP+EscapeUtil.escapeToken(portletModel.key));
	}
	public PortletContext getContext(){
		return this.context;
	}
	public Widget getDragHandler(){
		return header;
	}
	public void busy(boolean busy){
		header.busy(busy);
	}
	
	/**
	 * @param control: list on right upper side of portlet. should be a text link or 16*16 image
	 */
	protected void addControl(Widget control) {
		footer.add(control);
	}
	protected void clearControl(){
		footer.clear();
	}
	/**
	 * @param shortTitle, show on portlet title.
	 * @param title, show as hint text
	 * 
	 * @param token url possible be null
	 */
	protected void setPortletTitle(String shortTitle, String title, String token) {
		header.setTitle(shortTitle,title,token);
		
	}
	protected void setPortletLogo(String smallLogoUrl) {
		header.setLogo(smallLogoUrl);
	}
	
	protected void setPortletLogo(Image img) {
		header.setLogo(img);
	}
	
	/**
	 * Render portlet, executed after Portlet init() method, so that PortletContext is usable in this method.
	 * This method is only call once while Dashboard display. Normally, it only need to execute refresh();
	 */
	public void render(){
		if(ErrorCode.hasError(portletModel)){
			//this error normally is spaceWidget not exist error - the space has been deleted.
			container.clear();
			container.add(ErrorCode.getMessage(portletModel.errorCode, portletModel.errorMsg));
			return;
		}
		
		setPortletTitle(portletModel.title,portletModel.description,portletModel.titleURL);
		refresh();
	}
	/**
	 * Refresh this Portlet content. While user click "refresh" button on portlet, this method is executed.
	 */
	public void refresh() {
		
		busy(true);
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.invokePortlet(portletModel.type, portletModel.key, this);
		
	}
	
	/**
	 * Portlet is close method. While user click "close" button on portlet, this method is executed.
	 */
	public void close(){
		if(Window.confirm(Msg.params.confirm_close_portlet(portletModel.title))){
			for(Iterator<PortletListener> iter = listeners.iterator();iter.hasNext();){
				iter.next().close(this);
			}
		}
		
	};
	

	
	//invoke Portlet return
	public void onFailure(Throwable error) {
		busy(false);
		GwtClientUtils.processError(error);
	}
	
	//invoke Portlet return
	public void onSuccess(PortletModel portlet){
		busy(false);
		
		container.clear();
		if(!GwtClientUtils.preSuccessCheck(portlet,null)){
			//show error message on item part rather than general error message on HTML page above
			container.add(ErrorCode.getMessage(portlet.errorCode, portlet.errorMsg));
			return;
		}

		//put control initial before render - it is helpful if render content has issue - the delete button still valid for widget owner
		//so that he/she has chance to delete the bad widget.
		clearControl();
		
		if(portlet.perms[ClientConstants.WRITE] == SharedConstants.ALLOW){
			ClickLink editBtn = new ClickLink(Msg.consts.edit());
			editBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					PortletCreateDialog dia = new PortletCreateDialog(portletModel.key);
					dia.addListDialogueListener(new ListDialogueListener(){
						public void dialogClosed(DialogBox sender, List<? extends GeneralModel> modelList) {
							Portlet.this.refresh();
						}
					});
					dia.showbox();
				}
			});
			Image editImg = new Image(IconBundle.I.get().edit());
			editImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					viewFriends(portletModel.key);
				}
			});
			editImg.setStyleName(Css.PORTLET_FOOT_IMG);
			addControl(editImg);
			addControl(editBtn);
			
			Label sep1 = new Label(" | ");
			sep1.setStyleName(Css.PORTLET_WEAK_TEXT);
			addControl(sep1);
			
			//Temporarily disable widget change access control function as it is too complicated...???
//			ClickLink friendsBtn = new ClickLink("Friends");
//			friendsBtn.addClickHandler(new ClickHandler(){
//				public void onClick(ClickEvent event) {
//					viewFriends(portletModel.key);
//				}
//
//			});
//			Image friendsImg = IconBundle.I.get().group());
//			friendsImg.addClickHandler(new ClickHandler(){
//				public void onClick(ClickEvent event) {
//					viewFriends(portletModel.key);
//				}
//			});
//			addControl(friendsImg);
//			addControl(friendsBtn);
			
//			Label sep2 = new Label(" | ");
//			sep2.setStyleName(Css.PORTLET_WEAK_TEXT);
//			addControl(sep2);
			
			ClickLink deleteBtn = new ClickLink(Msg.consts.delete());
			deleteBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					deleteWidget(portletModel.key);
				}
			});
		
			Image deleteImg = new Image(IconBundle.I.get().bin_close());
			deleteImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					deleteWidget(portletModel.key);
				}
			});
			deleteImg.setStyleName(Css.PORTLET_FOOT_IMG);
			addControl(deleteImg);
			addControl(deleteBtn);

		}
		
		container.add(panel);

		final PageModel model = (PageModel) portlet.renderContent;
		PageRender render = new PageRender(panel);
		render.renderContent(model.spaceUname, model, model.renderContent, false);
		
	}


	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public UserModel getViewUser() {
		return viewUser;
	}

	public void setViewUser(UserModel viewUser) {
		this.viewUser = viewUser;
	}
	//********************************************************************
	//               Private methods
	//********************************************************************
	private void viewFriends(String key) {
		// TODO Auto-generated method stub
		
	}

	private void deleteWidget(String key) {
		if(Window.confirm(Msg.consts.confirm_delete_widget())){
			busy(true);
			PortalControllerAsync portalController = ControllerFactory.getPortalController();
			portalController.removeWidget(key, new DeleteWidgetAsync());
		}
	}
	//********************************************************************
	//               Private class
	//********************************************************************
	private class DeleteWidgetAsync implements AsyncCallback<PortletModel>{
		public void onFailure(Throwable error) {
			// TODO Auto-generated method stub
			busy(false);
			GwtClientUtils.processError(error);
		}

		public void onSuccess(PortletModel portlet) {
			busy(false);
			
			container.clear();
			if(!GwtClientUtils.preSuccessCheck(portlet,null)){
				//show error message on item part rather than general error message on HTML page above
				container.add(ErrorCode.getMessage(portlet.errorCode, portlet.errorMsg));
				return;
			}
			
			for(Iterator<PortletListener> iter = listeners.iterator();iter.hasNext();){
				iter.next().close(Portlet.this);
			}
		}
		
	}
	protected class HeaderDragHandler extends FocusPanel {
		private HorizontalPanel controlPanel = new HorizontalPanel();
		private HorizontalPanel logoPanel = new HorizontalPanel();
		private HorizontalPanel titlePanel = new HorizontalPanel();
		private Image closeImg = new Image(IconBundle.I.get().close());
		private Image refreshImg = new Image(IconBundle.I.get().refresh());
		private Image busyImg = IconBundle.I.barIndicator();
		private HorizontalPanel panel = new HorizontalPanel();
		
		public HeaderDragHandler(){

			HorizontalPanel left  = new HorizontalPanel();
			left.add(busyImg);
			left.add(logoPanel);
			left.add(titlePanel);
			panel.add(left);
			panel.add(controlPanel);
			
			left.setCellVerticalAlignment(logoPanel, HasVerticalAlignment.ALIGN_MIDDLE);
			left.setCellVerticalAlignment(busyImg, HasVerticalAlignment.ALIGN_MIDDLE);
			left.setCellVerticalAlignment(titlePanel, HasVerticalAlignment.ALIGN_MIDDLE);
			
			panel.setCellHorizontalAlignment(left, HasHorizontalAlignment.ALIGN_LEFT);
			panel.setCellVerticalAlignment(left, HasVerticalAlignment.ALIGN_MIDDLE);
			panel.setCellHorizontalAlignment(controlPanel, HasHorizontalAlignment.ALIGN_RIGHT);
			panel.setCellVerticalAlignment(controlPanel, HasVerticalAlignment.ALIGN_MIDDLE);
			
			refreshImg.setTitle(Msg.consts.refresh());
			controlPanel.add(refreshImg);
			refreshImg.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					refresh();
				}
			});
			
			if(!AbstractEntryPoint.isOffline()){
				closeImg.setTitle(Msg.consts.close());
				controlPanel.add(closeImg);
				closeImg.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						close();
					}
				});
				closeImg.setVisible(false);
			}
			
			busyImg.setVisible(false);
			refreshImg.setVisible(false);
			
			
			this.addMouseOverHandler(new MouseOverHandler(){
				public void onMouseOver(MouseOverEvent event) {
					refreshImg.setVisible(true);
					if(!AbstractEntryPoint.isOffline()){
						closeImg.setVisible(true);
					}
				}
			});
			this.addMouseOutHandler(new MouseOutHandler(){
				public void onMouseOut(MouseOutEvent event) {
					refreshImg.setVisible(false);
			    	if(!AbstractEntryPoint.isOffline()){
			    		closeImg.setVisible(false);
			    	}
				}
			});
			this.add(panel);
			
			DOM.setStyleAttribute(refreshImg.getElement(), "cursor", "pointer");
			DOM.setStyleAttribute(refreshImg.getElement(), "cursor", "hand");
			if(!AbstractEntryPoint.isOffline()){
				DOM.setStyleAttribute(closeImg.getElement(), "cursor", "pointer");
				DOM.setStyleAttribute(closeImg.getElement(), "cursor", "hand");
			}
			DOM.setStyleAttribute(panel.getElement(), "width", "100%");
			DOM.setStyleAttribute(panel.getElement(), "height", "100%");
		 
			sinkEvents(Event.MOUSEEVENTS);
			
		}

		/**
		 * @param shortTitle, show on portlet title.
		 * @param title, show as hint text
		 * @param token
		 */
		private void setTitle(String shortTitle,String title, final String token) {
			titlePanel.clear();
			if(token != null && token.trim().length() > 0){
				EventfulHyperLink titleLink = new EventfulHyperLink(shortTitle,token);
				if(title != null && title.trim().length() > 0){
					titleLink.setTitle(title);
				}
				titlePanel.add(titleLink);
			}else{
				Label nameL = new Label(shortTitle);
				if(title != null && title.trim().length() > 0){
					nameL.setTitle(title);
				}
				titlePanel.add(nameL);
			}
		}
		private void setLogo(String logoUrl){
			Widget img = GwtClientUtils.createSpaceLogo(logoUrl);
			setLogo(img);
		}
		/**
		 * The widget could be any GWT widget, such as Image, or HTML etc.
		 * @param logo
		 */
		private void setLogo(Widget logo){
			logoPanel.clear();
			logo.setStyleName(Css.PORTLET_LOGO);
			logoPanel.add(logo);
		}
		
		private void busy(boolean busy){
			busyImg.setVisible(busy);
		}
	}

	

}
