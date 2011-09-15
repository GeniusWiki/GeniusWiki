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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class MoreLessButtonBar extends Composite implements ClickHandler, MouseOverHandler, MouseOutHandler{
	private boolean hasPre;
	private boolean hasNxt;
	private int currentPage = 1;
	
	private Image busy = IconBundle.I.indicator(); 
	private FocusPanel moreBtn = new FocusPanel();
	private FocusPanel lessBtn = new FocusPanel();
	private FlowPanel main = new FlowPanel();
	private List<MoreLessButtonBarListener> listeners = new ArrayList<MoreLessButtonBarListener>();
	
	//default - first page
	public MoreLessButtonBar(){
		this(false,true,1);
	}
	public MoreLessButtonBar(boolean hasPre, boolean hasNxt, int currentPage){
		lessBtn.add(new Label(Msg.consts.less()));
		moreBtn.add(new Label(Msg.consts.more()));
		lessBtn.addClickHandler(this);
		moreBtn.addClickHandler(this);
		lessBtn.addMouseOverHandler(this);
		moreBtn.addMouseOverHandler(this);
		lessBtn.addMouseOutHandler(this);
		moreBtn.addMouseOutHandler(this);

		main.add(lessBtn);
		main.add(moreBtn);
		
		main.setStyleName("morelessbar");
		lessBtn.setStyleName("lessbtn");
		moreBtn.setStyleName("morebtn");
		
		this.initWidget(main);
		
		this.setPaginationInfo(hasPre, hasNxt, currentPage);
	
	}
	public void busy(boolean showBusy){
		if(showBusy){
			lessBtn.setVisible(false);
			moreBtn.setVisible(false);
			main.add(busy);
			busy.setVisible(true);
		}else{
			lessBtn.setVisible(hasPre);
			moreBtn.setVisible(hasNxt);
			busy.setVisible(false);
			main.remove(busy);
		}
	}
	public void addMoreLessButtonBarListener(MoreLessButtonBarListener listener){
		listeners.add(listener);
	}
	public void removeMoreLessButtonBarListener(MoreLessButtonBarListener listener){
		listeners.remove(listener);
	}
	public void setPaginationInfo(boolean hasPre, boolean hasNxt, int currentPage) {
		lessBtn.setVisible(hasPre);
		moreBtn.setVisible(hasNxt);
		this.hasPre = hasPre;
		this.hasNxt = hasNxt;
		this.currentPage = currentPage;
		
		this.setVisible(true);
		lessBtn.removeStyleName("separator");
		if(!hasPre && !hasNxt){
			//no button
			this.setVisible(false);
		}else if(hasPre && !hasNxt){
			//only previous
			lessBtn.setWidth("100%");
		}else  if(!hasPre && hasNxt){
			//only next
			moreBtn.setWidth("100%");
		}else{
			lessBtn.addStyleName("separator");
			lessBtn.setWidth("49%");
			moreBtn.setWidth("49%");
		}
	}

	public void onClick(ClickEvent evt) {
		if(evt.getSource() == lessBtn){
			--currentPage;
		}else if(evt.getSource() == moreBtn){
			++currentPage;
		}
		for (MoreLessButtonBarListener listener:listeners) {
			listener.pageChange(currentPage);
		}
	}
	public void onMouseOver(MouseOverEvent evt) {
		if(evt.getSource() == lessBtn){
			lessBtn.addStyleName("hover");
		}else if(evt.getSource() == moreBtn){
			moreBtn.addStyleName("hover");
		}
		
	}
	public void onMouseOut(MouseOutEvent evt) {
		if(evt.getSource() == lessBtn){
			lessBtn.removeStyleName("hover");
		}else if(evt.getSource() == moreBtn){
			moreBtn.removeStyleName("hover");
		}
		
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public boolean isHasPre() {
		return hasPre;
	}
	public boolean isHasNxt() {
		return hasNxt;
	}
	public int getCurrentPage() {
		return currentPage;
	}


	
	
}
