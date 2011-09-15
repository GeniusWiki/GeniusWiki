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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class HelpDialog  extends DialogBox implements ClickHandler{
	ClickLink sPop = new ClickLink(Msg.consts.open_new_window());
	ClickLink kPop = new ClickLink(Msg.consts.open_new_window());
	Image sImg = new Image(IconBundle.I.get().new_window());
	Image kImg = new Image(IconBundle.I.get().new_window());
	
	public HelpDialog(){
		this.setText(Msg.consts.help() + " - " + Msg.consts.press() +" F1");
		this.setIcon(new Image(IconBundle.I.get().help()));
		Frame syntax = new Frame(showHtmlPage("static/syntax.html"));
		Frame key = new Frame(showHtmlPage("static/keys.html"));
		
		DOM.setElementAttribute(syntax.getElement(), "frameborder", "0");
		DOM.setElementAttribute(key.getElement(), "frameborder", "0");
		
		syntax.setSize("100%", "100%");
		key.setSize("100%", "100%");
		
		FlowPanel sbar = new FlowPanel();
		Image sImg1 = new Image(IconBundle.I.get().home_link());
		sbar.add(sImg1);
		HTML sHome = new HTML("<a href='http://geniuswiki.com/page/GeniusWiki+document/GeniusWiki+document' target='_blank'> " + Msg.consts.online_help() +"</a>");
		sbar.add(sHome);
		//the latter one display first!
		sbar.add(sPop);
		sbar.add(sImg);
		
		sbar.setWidth("100%");
		sImg1.setStyleName(Css.LEFT);
		sHome.setStyleName(Css.LEFT);
		sImg.setStyleName(Css.RIGHT);
		sPop.setStyleName(Css.RIGHT);
		sImg.addClickHandler(this);
		sPop.addClickHandler(this);
		
		FlowPanel kbar = new FlowPanel();
		Image kImg1 = new Image(IconBundle.I.get().home_link());
		HTML kHome = new HTML("<a href='http://geniuswiki.com/page/GeniusWiki+document/GeniusWiki+document' target='_blank'> " + Msg.consts.online_help() +"</a>");
		kbar.add(kImg1);
		kbar.add(kHome);
		
		//the latter one display first!
		kbar.add(kPop);
		kbar.add(kImg);
		kbar.setWidth("100%");
		
		kImg1.setStyleName(Css.LEFT);
		kHome.setStyleName(Css.LEFT);
		kImg.setStyleName(Css.RIGHT);
		kPop.setStyleName(Css.RIGHT);
		kImg.addClickHandler(this);
		kPop.addClickHandler(this);
		
		VerticalPanel syntaxPanel = new VerticalPanel();
		syntaxPanel.add(sbar);
		syntaxPanel.add(syntax);
		syntaxPanel.setCellHeight(sbar, "30px");
		syntaxPanel.setCellHeight(syntax, "100%");
		
		VerticalPanel keyPanel = new VerticalPanel();
		keyPanel.add(kbar);
		keyPanel.add(key);
		keyPanel.setCellHeight(kbar, "30px");
		keyPanel.setCellHeight(key, "100%");
		
		DecoratedTabPanel deck = new DecoratedTabPanel();
		deck.add(syntaxPanel,Msg.consts.markup());
		deck.add(keyPanel,Msg.consts.keyboard());
		
		//show markup panel
		deck.selectTab(0);
		this.setWidget(deck); 
		
		syntax.setSize("100%", "100%");
		key.setSize("100%", "100%");
		deck.addStyleName(Css.DECK);
		this.addStyleName(Css.HELP_DIALOG_BOX);
		
	}

	/**
	 * @return
	 */
	private String showHtmlPage(String src) {
		return GwtClientUtils.getBaseUrl() + src;
	}

	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		this.hidebox();
		if(sender == sPop || sender == sImg){
			Window.open(GwtClientUtils.getBaseUrl()+"static/syntax.html",SharedConstants.APP_NAME+"SyntaxHelp","");
		}else if(sender == kPop){
			Window.open(GwtClientUtils.getBaseUrl()+"static/keys.html",SharedConstants.APP_NAME+"KeysHelp","");
		}
	}
	
}
