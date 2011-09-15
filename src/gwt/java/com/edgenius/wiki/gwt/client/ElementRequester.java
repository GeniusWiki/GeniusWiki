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
package com.edgenius.wiki.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.model.TreeItemModel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * Helper class to send Ajax request to get some values(such as Page list, Space list etc). 
 * @author Dapeng.Ni
 */
public class ElementRequester{
	private PageTitleListAsync pageTitleListAsync = new PageTitleListAsync();
	private PageTreeAsync pageTreeAsync = new PageTreeAsync();
	private SpaceUnameListAsync spaceUnameListAsync = new SpaceUnameListAsync();
	private MessageWidget message;
	
	private Vector<ElementRequesterCallback> collects = new Vector<ElementRequesterCallback>();
	
	public ElementRequester(){
		this(null);
	}
	
	public void addCallback(ElementRequesterCallback callback){
		collects.add(callback);
	}
	
	public ElementRequester(MessageWidget message){
		this.message = message;
	}
	
	public void needPageTitleList(String spaceUname){
		PageControllerAsync action = ControllerFactory.getPageController();
		action.getPageTree(spaceUname,pageTitleListAsync);
	}
	
	public void needSpaceUnameList(String filterText){
		
		SpaceControllerAsync action = ControllerFactory.getSpaceController();
		//get all space by given filter
		action.getSpacesInfo(filterText, 0, String.valueOf(SharedConstants.SORT_BY_SPACEKEY), spaceUnameListAsync);
	}
	public void needPageTree(String spaceUname) {
		PageControllerAsync action = ControllerFactory.getPageController();
		action.getPageTree(spaceUname,pageTreeAsync);
	}

	public void needUserList() {
		//TODO
	}
	//********************************************************************
	//               AsyncCallback classes
	//********************************************************************
	class PageTreeAsync implements AsyncCallback<TreeItemListModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			for (ElementRequesterCallback callback : collects) {
				callback.pageTreeRequestFailed(null);
			}
		}
		
		public void onSuccess(TreeItemListModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				for (ElementRequesterCallback callback : collects) {
					callback.pageTreeRequestFailed(model.errorCode);
				}
				return;
			}
			

			for (ElementRequesterCallback callback : collects) {
				callback.pageTree(model);
			}
		}
		
	}
	class PageTitleListAsync implements AsyncCallback<TreeItemListModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			for (ElementRequesterCallback callback : collects) {
				callback.pageTitleListRequestFailed(null);
			}
		}
		
		public void onSuccess(TreeItemListModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,null)){
				for (ElementRequesterCallback callback : collects) {
					callback.pageTitleListRequestFailed(model.errorCode);
				}
				return;
			}
			
			List<String> titles = new ArrayList<String>();
			for(TreeItemModel item : model.list){
				titles.add(item.title);
			}
			for (ElementRequesterCallback callback : collects) {
				callback.pageTitleList(model.spaceUname,titles);
			}
		}

	}
	class SpaceUnameListAsync implements AsyncCallback<SpaceListModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			for (ElementRequesterCallback callback : collects) {
				callback.spaceUnameListRequestFailed(null);
			}
		}
		
		public void onSuccess(SpaceListModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				for (ElementRequesterCallback callback : collects) {
					callback.spaceUnameListRequestFailed(model.errorCode);
				}
				return;
			}
			List<String> titles = new ArrayList<String>();
			for(SpaceModel item: model.spaceList){
				titles.add(item.unixName);
			}
			for (ElementRequesterCallback callback : collects) {
				callback.spaceUnameList(titles);
			}
		}
		
	}


}
