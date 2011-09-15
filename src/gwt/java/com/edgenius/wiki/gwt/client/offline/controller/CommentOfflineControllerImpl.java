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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.model.CommentListModel;
import com.edgenius.wiki.gwt.client.model.CommentModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.offline.GearsDB;
import com.edgenius.wiki.gwt.client.offline.OfflineUtil;
import com.edgenius.wiki.gwt.client.server.CommentControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.CascadeComparator;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.google.gwt.gears.client.GearsException;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public class CommentOfflineControllerImpl extends AbstractOfflineControllerImpl implements CommentControllerAsync {

	
	public void getPageComments(String spaceUname, String pageUuid, AsyncCallback<CommentListModel> callback) {
		CommentListModel model = new CommentListModel();
		
		try {
			UserModel user = OfflineUtil.getUser();
			
			List<CommentModel> list = GearsDB.getUserDB(user.getUid()).getComments(pageUuid);
			//retrieve all comment object to manually set comment's parent
			if(list != null){
				for (CommentModel comment: list) {
					if(comment.parentUid != null){
						comment.setParent(findParent(list, comment.parentUid));
					}
				}
				//sort comment
				Set<CommentModel> sort = new TreeSet<CommentModel>(new CascadeComparator());
				sort.addAll(list);
				model.comments = new ArrayList<CommentModel>(sort);
			}
			
			//check page permission, then decide Comment Permission
			PageModel page = GearsDB.getUserDB(user.getUid()).getCurrentPageByUuid(pageUuid);
			model.perms = new int[2];
			model.perms[ClientConstants.READ] = page.permissions[ClientConstants.COMMENT_READ];
			//not necessary, since offline comment is read only
			//model.perms[ClientConstants.WRITE] = page.permissions[ClientConstants.COMMENT_WRITE];
			model.perms[ClientConstants.WRITE] = 0;
				
		} catch (GearsException e) {
			Log.error("unable to get offline comment count",e);
			model.errorCode = ErrorCode.COMMENT_GET_FAILED;
		}
		
		OfflineUtil.setLoginInfo(model);
		callback.onSuccess(model);

	}
	


	public void createComment(String spaceUname, String pageUuid, CommentModel msg, AsyncCallback<CommentModel> callback) {
		// TODO Auto-generated method stub

	}

	public void hideComment(Integer uid, boolean hide, AsyncCallback<CommentModel> callback) {
		// TODO Auto-generated method stub
		
	}
	
	//********************************************************************
	//               private methods
	//********************************************************************
	/**
	 * @param parentUid
	 * @return
	 */
	private CommentModel findParent(List<CommentModel> list, Integer parentUid) {
		for (CommentModel comment: list) {
			if(parentUid.equals(comment.uid))
				return comment;
		}
		return null;
	}



}
