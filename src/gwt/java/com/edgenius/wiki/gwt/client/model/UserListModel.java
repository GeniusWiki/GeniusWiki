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
package com.edgenius.wiki.gwt.client.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.Window;

/**
 * @author Dapeng.Ni
 */
public class UserListModel extends GeneralModel{

	public int total;
	private ArrayList<UserModel> userList = new ArrayList<UserModel>();
	
	public List<UserModel> getUserModelList(){
		return userList;
	}

	public void setUserModeList(ArrayList<UserModel> userList) {
		this.userList = userList;
	}
	public UserModel getUserModel(int idx){
		return (UserModel) userList.get(idx);
	}
	public void remove(int idx){
		userList.remove(idx);
	}
	public int getSize(){
		return userList.size();
	}
	public void addAll(List<UserModel> users){
		if(users == null)
			return;
		
		for(Iterator<UserModel> iter = users.iterator();iter.hasNext();)
			add(iter.next());
	}
	public boolean update(UserModel model){
		for (Iterator<UserModel>  iter = userList.iterator();iter.hasNext();) {
			UserModel exi = iter.next();
			if(exi.equals(model)){
				//show current page
				return true;
			}
		}
		return false;
	}
	public int  add(UserModel model){
		//sorted by user loginname now
		//possible extends to sorted by Fullname
		int size = userList.size();
		int insertIdx = 0;
		for(int idx=0;idx<size;idx++){
			insertIdx = idx+1;
			UserModel exi = (UserModel) userList.get(idx);
			//for same user model, just replace old one
			if(exi.equals(model)){
				userList.remove(idx);
				userList.add(idx,model);
				return idx;
			}
			if(exi.getLoginname().toUpperCase().compareTo(model.getLoginname().toUpperCase()) > 0){
				insertIdx = idx;
				break;
			}
		}
		userList.add(insertIdx,model);
		return insertIdx;
	}

	/**
	 * @param item
	 * @return
	 */
	public boolean remove(UserModel item) {
		for (Iterator<UserModel>  iter = userList.iterator();iter.hasNext();) {
			UserModel model = iter.next();
			if(model.equals(item)){
				Window.alert("remove" + model.getLoginname());
				iter.remove();
				//show current page
				return true;
			}
		}
		return false;
	}

}
