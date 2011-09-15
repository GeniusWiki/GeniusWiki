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
package com.edgenius.wiki.gwt.client.offline.controller;

import java.util.List;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.PageItemListModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.TagListModel;
import com.edgenius.wiki.gwt.client.model.TagModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.offline.GearsDB;
import com.edgenius.wiki.gwt.client.offline.OfflineUtil;
import com.edgenius.wiki.gwt.client.server.TagControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class TagOfflineControllerImpl extends AbstractOfflineControllerImpl implements TagControllerAsync {

	public void getTags(String spaceUname, AsyncCallback<TagListModel> callback) {
		if(StringUtil.isBlank(spaceUname)){
			getSpaceScopeTags(callback);
		}else{
			getPageScopeTags(spaceUname, callback);
		}
		
	}
	
	public void getPageScopeTags(String spaceUname, AsyncCallback<TagListModel> callback) {
		UserModel user = OfflineUtil.getUser();
		
		TagListModel model = new TagListModel();
		try {
			List<String> tags = GearsDB.getUserDB(user.getUid()).getPageTagsNameList(spaceUname);
			model.spaceUname = spaceUname;
			if(tags != null){
				for (String name: tags) {
					TagModel tagModel = new TagModel();
					tagModel.name = name;
					model.tags.add(tagModel); 
				}
				Log.info("Get " + tags.size() + " tags for space " + spaceUname);
			}
			
		} catch (GearsException e) {
			Log.info("Exception get space " + spaceUname + " all page tags");
		}
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);
	}

	public void getTagPages(String spaceUname, String tagname, int count, AsyncCallback<PageItemListModel> callback) {
		
		PageItemListModel model = new PageItemListModel();
		if(tagname != null){
			tagname = tagname.trim();
			UserModel user = OfflineUtil.getUser();
			try {
				model.itemList = GearsDB.getUserDB(user.getUid()).getTagedPages(spaceUname,tagname,count);
				Log.debug("get page list for tag " + tagname + "; return size " + model.itemList.size());
			} catch (GearsException e) {
				Log.info("Exception while get tagged pages " + tagname);
			}
		}		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);

	}
	
	public void getSpaceScopeTags(AsyncCallback<TagListModel> callback) {
		

	}


	
	public void getTagSpaces(String name, int count, AsyncCallback<SpaceListModel> callback) {
		// TODO Auto-generated method stub

	}


	public void savePageTags(String spaceUname, String pageUuid, String tagString,CaptchaCodeModel captcha,  AsyncCallback<TagListModel> callback) {
		// TODO Auto-generated method stub
		
	}



}
