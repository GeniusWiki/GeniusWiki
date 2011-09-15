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

/**
 * @author Dapeng.Ni
 */
public class RoleListModel extends GeneralModel{

	public int total = -1;

	private ArrayList<RoleModel> roleList = new ArrayList<RoleModel>();
	
	public List<RoleModel> getRoleList(){
		return roleList;
	}
	public int  add(RoleModel model){
		//sorted by user loginname now
		//possible extends to sorted by Fullname
		int size = roleList.size();
		int insertIdx = 0;
		for(int idx=0;idx<size;idx++){
			RoleModel exi = (RoleModel) roleList.get(idx);
			//for same user model, just replace old one
			if(exi.equals(model)){
				roleList.remove(idx);
				roleList.add(idx,model);
				return idx;
			}
			if(exi.getDisplayName().compareTo(model.getDisplayName()) > 0){
				insertIdx = idx;
				break;
			}
			insertIdx = idx;
		}
		roleList.add(insertIdx,model);
		return insertIdx;
	}
	public boolean remove(RoleModel item) {
		this.total--;
		for (Iterator iter = roleList.iterator();iter.hasNext();) {
			RoleModel model = (RoleModel) iter.next();
			if(model.equals(item)){
				iter.remove();
				//show current page
				return true;
			}
		}
		return false;
	}
	/**
	 * @param modelList
	 */
	public void addAll(List modelList) {
	
		if(modelList == null)
			return;
		
		for(Iterator iter = modelList.iterator();iter.hasNext();)
			add((RoleModel) iter.next());
		
	}

}
