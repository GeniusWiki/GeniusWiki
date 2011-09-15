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
package com.edgenius.wiki.gwt.client.editor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.ElementRequester;
import com.edgenius.wiki.gwt.client.ElementRequesterCallback;
import com.edgenius.wiki.gwt.client.html.HTMLNode;
import com.edgenius.wiki.gwt.client.html.HtmlNodeListenerImpl;
import com.edgenius.wiki.gwt.client.html.HtmlParser;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.server.utils.EscapeUtil;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.Hr;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.PageSuggestBox;
import com.edgenius.wiki.gwt.client.widgets.SpaceSuggestBox;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class MCELinkDialog extends MCEDialog implements ElementRequesterCallback {
	private HintTextBox extLinkBox = new HintTextBox(Msg.consts.http_prefix());
	private SpaceSuggestBox spaceBox = new SpaceSuggestBox(Msg.consts.current_space());
	private PageSuggestBox pageBox = new PageSuggestBox();
	
	private MessageWidget message = new MessageWidget();
	private ElementRequester request = new ElementRequester(message);
	private boolean update = false;
	private String currSpaceUname;
	public MCELinkDialog(final TinyMCE tiny){
		super(tiny);
		request.addCallback(this);
		
		this.setText(Msg.consts.insert_link());
		Label l1 = new Label(Msg.consts.link_url());
		Label l2 = new Label(Msg.consts.space());
		Label l3 = new Label(Msg.consts.page());
		
		FlexTable layout = new FlexTable();
		layout.setWidget(0,0,l2);
		layout.setWidget(0,1,spaceBox);
		
		layout.setWidget(1,0,l3);
		layout.setWidget(1,1,pageBox);
		
		Hr sep = new Hr();
		layout.setWidget(2, 0, sep);
		layout.getFlexCellFormatter().setColSpan(2, 0, 2);
		
		layout.setWidget(3,0,l1);
		layout.setWidget(3,1,extLinkBox);
		
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(layout);
		
		panel.setSize("100%", "100%");
		layout.setSize("100%", "100%");
		l1.setStyleName(Css.FORM_LABEL);
		l2.setStyleName(Css.FORM_LABEL);
		l3.setStyleName(Css.FORM_LABEL);
		sep.setStyleName(Css.SEPARATOR);
		extLinkBox.setStyleName(Css.FORM_INPUT);
		spaceBox.setStyleName(Css.FORM_INPUT);
		pageBox.setStyleName(Css.FORM_INPUT);
		this.setWidget(panel);
		
		//can not use spaceBox.onLostFocus() to decide when get PageList, as spaceBox is suggest box, a popup suggest box
		//will trigger the focus event, but at the moment, the space input not finish yet...
		pageBox.addFocusHandler(new FocusHandler(){
			public void onFocus(FocusEvent event) {
				String spaceUname = StringUtil.trim(spaceBox.getText());
				
				if(!StringUtil.equalsIgnoreCase(spaceUname,currSpaceUname)){
					pageBox.request(spaceUname);
					currSpaceUname = spaceUname;
				}
			}
			
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				pageBox.setFocus(true);
			}
		});
		
		//initial request for current space
		//just put it as blank so that comparing with spaceBox.getText()
		currSpaceUname = "";
		pageBox.request(StringUtil.trim(tiny.getSpaceUname()));
	}
	@Override
	protected void okEvent() {
		String src = StringUtil.trim(extLinkBox.getText());
		String sUname = StringUtil.trim(spaceBox.getText());
		String pTitle = StringUtil.trim(pageBox.getText());
		String tagline = null;
		
		tiny.restoreEditorBookmark();
		
		if(!StringUtil.isBlank(src)){
			//external link input
			if(LinkUtil.isExtLink(src)){
				LinkModel link = new LinkModel();
				link.setView(tiny.getSelectedText());
				link.setLink(src);
				tagline = link.toRichAjaxTag();
			}else{
				message.warning(Msg.consts.error_invalid_url());
			}
		}else if(!StringUtil.isBlank(pTitle)){
			//page link input
			LinkModel link = new LinkModel();
			link.setView(tiny.getSelectedText());
			int sep;
			String anchor="";
			if ((sep = StringUtil.indexSeparatorWithoutEscaped(pTitle, "#")) != -1) {
				anchor = pTitle.substring(sep + 1);
				pTitle = pTitle.substring(0, sep);
			}
			link.setSpaceUname(EscapeUtil.unescapeMarkupLink(sUname));
			link.setLink(EscapeUtil.unescapeMarkupLink(pTitle));
			link.setAnchor(EscapeUtil.unescapeMarkupLink(anchor));
			tagline = link.toRichAjaxTag();
		}else{
			message.warning(Msg.consts.error_invalid_url_or_page());
		}
		if(tagline != null){
			
			if(update){
				Element ele = tiny.getParentElement("A");
				if(ele == null || !(ele instanceof  LinkElement)){
					Log.error("Unable to find updated link tag A");
				}else{
					Element linkEle = ele;
					HtmlParser parser = new HtmlParser();
					HtmlNodeListenerImpl nodeListener = new HtmlNodeListenerImpl();
					parser.scan(tagline, nodeListener);
					HTMLNode atag = null; 
					for (HTMLNode node: nodeListener.getHtmlNode()) {
						if(!node.isCloseTag() && node.getTagName().equalsIgnoreCase("A")){
							atag = node;
							break;
						}
					}
					if(atag != null){
						Map<String,String> attributes = atag.getAttributes();
						if(attributes != null && attributes.size() > 0){
							for (Entry<String,String> entry : attributes.entrySet()) {
								Log.info("Update link with new attributes:" + entry.getKey()+":" + entry.getValue());
								linkEle.setAttribute(entry.getKey(), entry.getValue());
							}
						}
					}
				}
			}else{
				tiny.insertContent(tagline);
			}
			this.close();
		}


	}
	
	public void initField(String href, String wajax){
		if(!StringUtil.isBlank(href) &&( href.startsWith("http://") || href.startsWith("https://"))){
			update = true;
			extLinkBox.setText(href);
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					extLinkBox.setFocus(true);
				}
			});
		}else if(!StringUtil.isBlank(wajax)){
			update = true;
			Map<String,String> wmap = RichTagUtil.parseWajaxAttribute(wajax);
			String sUname = wmap.get(NameConstants.SPACEUNAME);
			if(!StringUtil.isBlank(sUname) && !StringUtil.equalsIgnoreCase(tiny.getSpaceUname(),sUname)){
				spaceBox.setText(sUname);
			}else{
				//default space, will display hint text
				spaceBox.setText("");
			}
			String link = wmap.get(NameConstants.LINK);
			if(!StringUtil.isBlank(link)){
				pageBox.setText(link);
			}
		}

	}

	
	public void pageTitleList(String spaceUname, List<String> titles) {
	}
	public void pageTitleListRequestFailed(String errorCode) {
	}
	public void spaceUnameList(List<String> spaces) {
	}
	public void spaceUnameListRequestFailed(String errorCode) {
	}
	public void pageTree(TreeItemListModel model) {
	}
	public void pageTreeRequestFailed(String errorCode) {
	}

}
