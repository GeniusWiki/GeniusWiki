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
package com.edgenius.wiki.gwt.client.render;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.BaseEntryPoint;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.JavascriptExposer;
import com.edgenius.wiki.gwt.client.home.HomePortalVisitor;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.login.LoginDialog;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.portal.Portal;
import com.edgenius.wiki.gwt.client.portal.PortalVisitor;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.FeedbackDialog;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.EventfulHyperLink;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class PageRender implements RenderWidgetListener{
	private RenderPanel panel;
	private List<RenderContentListener> listeners;
	private List<RenderWidget> widgetContainer = new ArrayList<RenderWidget>();
	private List<String> widgetCounter = new ArrayList<String>();
	private StringBuffer widgetRenderContent = new StringBuffer();
	private UserModel currentUser;
	private PortalVisitor visitor;
	
	public PageRender(RenderPanel container){
		this.panel = container;
	}
	
	public void addRenderContentListener(RenderContentListener listener){
		if(listeners == null)
			listeners = new ArrayList<RenderContentListener>();
		
		listeners.add(listener);
	}

	/**
	 * @param spaceUname 
	 * @param panel
	 * @param model
	 * @param pieces: could be pageContent, or side bar content 
	 * @param isPreivew: if this render is for preview window
	 */
	public void renderContent(final String spaceUname, PageModel page, List<RenderPiece> pieces, boolean isPreivew) {
		//most stuff only need renderPiece, except PageAuthorPanel at moment
		widgetRenderContent = new StringBuffer();
		widgetCounter.clear();
		widgetContainer.clear();
		
		panel.clear();
		
		if(pieces != null){
			int size = pieces.size();
			for(int idx=0;idx<size;idx++){
				Object piece = pieces.get(idx);
				if(piece instanceof LinkModel){
					LinkModel ln = (LinkModel) piece;
					if(listeners != null){
						fireRenderEvent(ln.getView());
					}
					final String anchorTxt = StringUtil.isBlank(ln.getAnchor())?"":(EscapeUtil.escapeToken(ln.getAnchor().trim()));
					if(anchorTxt.length() != 0 && StringUtil.isBlank(ln.getLink())){
						//[view>#anchor] style: will only redirect to anchor in same page
						ClickLink link = new ClickLink(ln.getView(),true);
						link.addClickHandler(new ClickHandler(){
							public void onClick(ClickEvent event) {
								GwtClientUtils.gotoAnchor(anchorTxt);
							}
						});
						panel.add(link);
					}else if (ln.getType() == LinkModel.LINK_TO_CLIENT_CLICK_LINK){
						final String[] token = LinkUtil.parseCLinkParamters(ln.getLink());
						if(token != null && token.length > 0){
							//If it has anchor field, it will be treated as hyper link.Otherwise, ClickLink
							if(!StringUtil.isEmpty(ln.getAnchor())){
								//now it only support user popup - so need to consolidate if want to more functions 
								EventfulHyperLink link = new EventfulHyperLink(ln.getView(), ln.getAnchor());
								String method = token[0];
								String[] params = null;
								if(token.length > 1){
									params = new String[token.length -1];
									for (int idx1=1;idx1<token.length;idx1++) {
										params[idx1-1] = token[idx1];
									}
								}
								JavascriptExposer.run(method,params,link);
								panel.add(link);
							}else{
								//click link
								ClickLink link = new ClickLink(ln.getView(),true);
								link.addClickHandler(new ClickHandler(){
									public void onClick(ClickEvent event) {
										String method = token[0];
										String[] params = null;
										if(token.length > 1){
											params = new String[token.length -1];
											for (int idx=1;idx<token.length;idx++) {
												params[idx-1] = token[idx];
											}
										}
										JavascriptExposer.run(method,params,(Widget)event.getSource());
									}
								});
								panel.add(link);
							}
						}else{
							panel.add(new HTML(ln.getView()));
							Log.error("Unable pasre link with type 'client click link':" + ln.getLink());
						}
					}else if (ln.getType() == LinkModel.LINK_TO_SERVER_CLICK_LINK){
						
					}else{
						if(isPreivew){
							ClickLink link = new ClickLink(ln.getView(),true);
							link.addClickHandler(new ClickHandler(){
								public void onClick(ClickEvent event) {
									Window.alert(Msg.consts.preview_disable_link());
								}
							});
							panel.add(link);
						}else{
							String tokenName = ln.getLink();
							
							String linkSpaceUname = ln.getSpaceUname();
							//extspace or current space,  if null(failure tolerance) use current spaceUname as value
							linkSpaceUname = (linkSpaceUname == null || linkSpaceUname.trim().length() == 0)?spaceUname:linkSpaceUname;
							if(ln.getType() == LinkModel.LINK_TO_CREATE_FLAG){
								//$CREATE ,for link to non-exist page, or for "PAGE_NOT_FOUND" case
								tokenName = GwtUtils.buildToken(PageMain.TOKEN_CREATE,linkSpaceUname,tokenName);
							}else if(ln.getType() == LinkModel.LINK_TO_CREATE_HOME_FLAG){
								tokenName = GwtUtils.buildToken(PageMain.TOKEN_CREATE_HOME,linkSpaceUname,tokenName);
							}else if (ln.getType() == LinkModel.LINK_TO_OPEN_NEW_WIN){
								//no use so far
								tokenName = GwtUtils.buildToken(PageMain.TOKEN_OPEN_NEW_WINDOW, linkSpaceUname, tokenName);
							}else if (ln.getType() == LinkModel.LINK_TO_HYPER_TOKEN){
								//keep token unchanged - nothing to do here.
							}else
								tokenName = GwtUtils.getSpacePageToken(linkSpaceUname, tokenName);
							
							tokenName += (anchorTxt==""?"":"#"+anchorTxt);
							Hyperlink link = new Hyperlink(ln.getView(), true, tokenName);
							panel.add(link);
						}
					}
				}else if(piece instanceof MacroModel){
					MacroModel rs = (MacroModel) piece;
					if(rs != null){
						
						if(rs.macroName.equalsIgnoreCase(SharedConstants.MACRO_SPACE_ADMIN)){
							SpaceAdminRenderWidget adminPanel = new SpaceAdminRenderWidget(spaceUname);
							panel.add(adminPanel);
							widgetContainer.add(adminPanel);
							adminPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
							
						}else if(rs.macroName.equalsIgnoreCase(SharedConstants.MACRO_NOTIFY_SYSADMIN)){
							SendingNotifyRenderWidget adminPanel = new SendingNotifyRenderWidget();
							panel.add(adminPanel);
							widgetContainer.add(adminPanel);
							adminPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
						}else if(rs.macroName.equals(SharedConstants.MACRO_TAG_CLOUD)){
							TagCloudRenderWidget macroPanel = new TagCloudRenderWidget(spaceUname);
							panel.add(macroPanel);
							widgetContainer.add(macroPanel);
							macroPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
						}else if(rs.macroName.equals(SharedConstants.MACRO_TEMPLATE_LIST)){
							TemplateListRenderWidget macroPanel = new TemplateListRenderWidget(spaceUname);
							panel.add(macroPanel);
							widgetContainer.add(macroPanel);
							macroPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
						}else if(rs.macroName.equals(SharedConstants.MACRO_SEARCH)){
							String keyword = (String) rs.values.get(NameConstants.KEYWORD);
							SearchRenderWidget macroPanel = new SearchRenderWidget(keyword);
							panel.add(macroPanel);
							widgetContainer.add(macroPanel);
							macroPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
						}else if(rs.macroName.equals(SharedConstants.MACRO_USER_PROFILE)){
							String username = rs.values.get(NameConstants.USERNAME);
							UserProfileRenderWidget macroPanel = new UserProfileRenderWidget(username);
							panel.add(macroPanel);
							widgetContainer.add(macroPanel);
							macroPanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
							
						}else if(rs.macroName.equals(SharedConstants.MACRO_INCLUDE)){
							String src = rs.values.get(NameConstants.SRC);
							
							LinkModel link = LinkUtil.parseMarkup(src);
							if(StringUtil.isBlank(link.getSpaceUname())){
								//if @space is blank, treat as current space 
								link.setSpaceUname(spaceUname);
							}
							IncludeRenderWidget includePanel = new IncludeRenderWidget(link);
							panel.add(includePanel);
							widgetContainer.add(includePanel);
							includePanel.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
							
						}else if(rs.macroName.equals(SharedConstants.MACRO_COMMENT)){
							String title = (String) rs.values.get(NameConstants.TITLE);
							ClickLink comment = new ClickLink(title);
							comment.addClickHandler(new ClickHandler(){
								public void onClick(ClickEvent event) {
									if(BaseEntryPoint.I != null && BaseEntryPoint.I instanceof PageMain){
										PageMain main = ((PageMain)BaseEntryPoint.I);
										if(main.getVisiblePanelIndex() == PageMain.VIEW_PANEL){
											main.viewPanel.tabPanel.newComment();
											return;
										}
									}
									Window.alert(Msg.consts.not_available_mode());
								}
							});
							widgetRenderContent.append(title);
							panel.add(comment);
						}else if(rs.macroName.equals(SharedConstants.MACRO_PORTAL)){
							boolean show = BooleanUtil.toBooleanTrue(rs.values.get(NameConstants.SHOWLOGO));
							int column = NumberUtil.toInt(rs.values.get(NameConstants.COLUMNS), -1);
							if(column == -1){
								//this is just for makes this macro can tolerance spell error - colunm or columns
								column = NumberUtil.toInt(rs.values.get(NameConstants.COLUMN), -1);
							}
							if(column == -1){
								column = SharedConstants.DEFAULT_PORTAL_COLUMNS;
							}
							if(visitor == null){
								//default portal visitor - don't use InstancePortalVisitor as it is dangerous to change default dashboard
								visitor = new HomePortalVisitor();
							}
							Portal portal = new Portal(visitor, column, show);
							panel.add(portal);
							widgetContainer.add(portal);
							portal.onLoad(HTMLPanel.createUniqueId(), currentUser, this);
						}else if(rs.macroName.equals(SharedConstants.MACRO_FEEDBACK)){
							String imgOn = rs.values.get(NameConstants.IMAGE);
							HasClickHandlers btn;
							if(StringUtil.isBlank(imgOn)){
								//text base link
								String title = (String) rs.values.get(NameConstants.TITLE);
								btn = new ClickLink(title);
							}else{
								btn = new Image(imgOn);
							}
							btn.addClickHandler(new ClickHandler(){
								public void onClick(ClickEvent event) {
									FeedbackDialog dialog = new FeedbackDialog();
									dialog.showbox();
								}
							});
							
							//for hide welcome message
							widgetRenderContent.append("feedback");//NON-I18N
							panel.add((Widget)btn);
						}else if(rs.macroName.equals(SharedConstants.MACRO_SIGNUP)){
							String imgOn = rs.values.get(NameConstants.IMAGE);
							HasClickHandlers btn;
							if(StringUtil.isBlank(imgOn)){
								//text base link
								String title = (String) rs.values.get(NameConstants.TITLE);
								btn = new ClickLink(title);
							}else{
								btn = new Image(imgOn);
							}
							btn.addClickHandler(new ClickHandler(){
								public void onClick(ClickEvent event) {
									LoginDialog dialogue = new LoginDialog(LoginDialog.SINGUP);
									dialogue.showbox();
								}
							});
							
							//for hide welcome message
							widgetRenderContent.append("signup");//NON-I18N
							panel.add((Widget)btn);
						}
					}
				}else if(piece instanceof TextModel){
					String text = ((TextModel) piece).toString();
					panel.add(text);
					if(listeners != null){
						fireRenderEvent(text);
					}
				}
			}
		}
		
		panel.submit();

		if(listeners != null){
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					attemptFireRenderEnd();
				}

			});
		}
	}

	/**
	 * @param view
	 */
	private void fireRenderEvent(String content) {
		if(listeners != null){
			//fire renderContent event
			for (RenderContentListener listener : listeners) {
				listener.render(content==null?"":content);
			}
		}
	}
	private void attemptFireRenderEnd() {
		if(widgetCounter.size() == 0){
			//all ajax RenderWidget completed render, fire renderEnd event
			if(listeners != null){
				//fire renderContent event
				for (RenderContentListener listener : listeners) {
					listener.renderEnd(widgetRenderContent.toString());
				}
			}
		}
	}

	public void onLoading(String componentKey) {
		widgetCounter.add(componentKey);
		Log.info("Component " + componentKey + " loaded to Render. New size " + widgetCounter.size());
	}

	public void onFailedLoad(String componentKey, String errorMsg) {
		widgetCounter.remove(componentKey);
		widgetRenderContent.append(errorMsg);
		
		Log.info("Component " + componentKey + " is removed for failed loaded; Left widget " + widgetCounter.size() );
		attemptFireRenderEnd();
	}
	public void onSuccessLoad(String componentKey, String content) {
		widgetCounter.remove(componentKey);
		widgetRenderContent.append(content);
		
		Log.info("Component " + componentKey + " is removed for success loaded; Left widget " + widgetCounter.size());
		attemptFireRenderEnd();
	}
	
	public void registerPortalVistor(PortalVisitor visitor){
		this.visitor = visitor;
	}
	/**
	 * @param user
	 */
	public void login(UserModel user) {
		this.currentUser = user;
		Log.info("WidgetContainer start fire user change event:" + user + " on " + widgetContainer.size());
		for (RenderWidget widget : widgetContainer) {
			widget.onUserChanged(user);
		}
	}

}
