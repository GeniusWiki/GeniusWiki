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

import java.util.Iterator;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class Pagination extends SimplePanel{
	//how many items display on one page
	public static final int PAGE_SIZE = SharedConstants.PAGE_SIZE;
	//how many display on bar. such as < << 2,3,4 >>  > , only show 3 items on bar, others hide
	private static final int BAR_SIZE = 10;
	//if achieve first page, hide previous and first page button or not? same with last page.
	private boolean hideButton = true;
	private int totalPage;
	private int currentPage;
	private int barSize;
	private int pageSize;
	private int totalItem;
	
	HorizontalPanel panel = new HorizontalPanel();
	HorizontalPanel leftBtnPanel = new HorizontalPanel();
	HorizontalPanel listPanel = new HorizontalPanel();
	HorizontalPanel rightBtnPanel = new HorizontalPanel();
	
	private Vector listeners = new Vector();
	
	public Pagination(){
		this(0,PAGE_SIZE,BAR_SIZE, false);
	}
	public Pagination(int totalItem){
		this(totalItem,PAGE_SIZE,BAR_SIZE, false);
	}
	
	public Pagination(int totalItem, int pageSize,int barSize,boolean hideButton){
		this.pageSize = pageSize;
		this.totalItem = totalItem;
		totalPage = (int) Math.ceil((double)totalItem/(double)pageSize);
		this.barSize = barSize;
		this.currentPage = 0;
		this.hideButton = hideButton;
		
		
		panel.add(leftBtnPanel);
		panel.add(listPanel);
		panel.add(rightBtnPanel);
		init();
		this.setStyleName(Css.PAGINATION);
		this.setWidget(panel);
	}
	private void init(){
		leftBtnPanel.clear();
		listPanel.clear();
		rightBtnPanel.clear();
		if(totalPage < 2){
			return;
		}
		
		if(currentPage == 1){
			if(!hideButton){
				//show disable first page button 
				leftBtnPanel.add(new Image(ResultImageBundle.I.get().firstDisable()));
			}
		}else{
			Image first = new Image(ResultImageBundle.I.get().first());
			leftBtnPanel.add(first);
			first.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					firePageEvent(1);
				}
			});
		}
//			already in first page
		if(currentPage == 1){
			if(!hideButton){
				//show disable first page button 
				leftBtnPanel.add(new Image(ResultImageBundle.I.get().prevDisable()));
			}
		}else{
			Image prev =new Image( ResultImageBundle.I.get().prev());
			leftBtnPanel.add(prev);
			prev.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					firePageEvent(currentPage-1);
				}
			});
		}
		
		//page list
		int right = barSize / 2;
		int left = barSize - right;
		//index from 0
		int start = currentPage - left;
		int end = currentPage + right;
		if(start < 0){
			start = 0;
			end = barSize;
		}
		if(end > totalPage){
			end = totalPage;
			start = totalPage - barSize < 0?0:totalPage - barSize;
		}
		for(int idx = start;idx < end; idx++){
			if(idx+1==currentPage){
				Label current = new Label(Integer.valueOf(idx+1).toString());
				current.setStyleName(Css.CURRENT);
				listPanel.add(current);
				continue;
			}
			final int page = idx+1;
			ClickLink link = new ClickLink(Integer.valueOf(page).toString());
			link.setStyleName(Css.LABEL);
			link.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					firePageEvent(page);
				}
			});
			listPanel.add(link);
		}
			
//			last page
		if(currentPage == totalPage){
			if(!hideButton){
				//show disable first page button 
				rightBtnPanel.add(new Image(ResultImageBundle.I.get().nextDisable()));
			}
		}else{
			Image next = new Image(ResultImageBundle.I.get().next());
			rightBtnPanel.add(next);
			next.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					firePageEvent(currentPage+1);
				}
			});
		}
//			last page
		if(currentPage == totalPage){
			if(!hideButton){
				//show disable first page button 
				rightBtnPanel.add(new Image(ResultImageBundle.I.get().lastDisable()));
			}
		}else{
			Image last = new Image(ResultImageBundle.I.get().last());
			rightBtnPanel.add(last);
			last.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					firePageEvent(totalPage);
				}
			});
		}
	}
	

	public int setCurrentPage(int currentPage) {
		if(currentPage < 1)
			currentPage = 1;
		if(currentPage > totalPage)
			currentPage = totalPage;
		
		this.currentPage = currentPage;
		//need optimize to don't rebuild whole bar
		init();
		return currentPage;
	}
	
	public void addPaginationListener(PaginationListener listener){
		listeners.add(listener);
	}
	private void firePageEvent(int pageNum) {
		for (Iterator iter = listeners.iterator();iter.hasNext();) {
			((PaginationListener)iter.next()).pageChanging(pageNum);
		}
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public int getTotalPage(){
		return totalPage;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalItem() {
		return totalItem;
	}
	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
		totalPage = totalItem/pageSize;
		int left = totalItem%pageSize;
		if(left != 0)
			++totalPage;
	}
}
