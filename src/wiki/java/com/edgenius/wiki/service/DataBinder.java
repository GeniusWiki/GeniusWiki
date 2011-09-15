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
package com.edgenius.wiki.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * @author Dapeng.Ni
 */
public class DataBinder {

	public static final String HOME_PAGE_BINDER_NAME = "com.edgenius.wiki.HomepageBinder";
	public static final String USER_FOLLOWING_BINDER_NAME = "com.edgenius.wiki.UserFollowingBinder";

	private String version;
	
	private List<DataObject> objects = new ArrayList<DataObject>();
	private int options;
	//key: download options, relative dir in zip file
	private Map<Integer,String> dirs = new HashMap<Integer, String>(); 
	
	public int getObjectsSize(){
		return objects.size();
	}
	public int getOptions() {
		return options;
	}
	public void setOptions(int options) {
		this.options = options;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @param list
	 */
	public void addAll(String name, List<? extends Object> list) {
		DataObject exist = findObject(name);
		if(exist == null){
			exist = new DataObject(name,list); 
			objects.add(exist);
		}else{
			exist.addAll(list);
		}
	}
	public void add(String name, Object obj) {
		DataObject exist = findObject(name);
		if(exist == null){
			exist = new DataObject(name,obj); 
			objects.add(exist);
		}else{
			exist.add(obj);
		}
		
	}

	public List<? extends Object> get(String name) {
		DataObject obj = findObject(name);
		if(obj == null)
			return null;
		
		return obj.getList();
	}

	/**
	 * Get back relative directory in backup zip, for example option BackService.BACKUP_ATTACHMENT returns /data/repository
	 */
	public String getDir(int option) {
		return dirs.get(option);
	}
	
	public void addDir(int option,String dir){
		dirs.put(option, dir);
	}
	
	
	private DataObject findObject(String name) {
		for (DataObject obj : objects) {
			if(obj.getName().equals(name))
				return obj; 
		}
		
		return null;
		
	}
	public static class DataObject{
		private String name;
		private List<Object> list = new ArrayList<Object>();

		public DataObject(String name, List<? extends Object> list) {
			this.name = name;
			this.list.addAll(list);
		}
		public DataObject(String name, Object obj) {
			this.name = name;
			this.list.add(obj);
		}

		public void addAll(List<? extends Object> list) {
			this.list.addAll(list);
		}

		public void add(Object obj) {
			this.list.add(obj);
		}

		public String getName() {
			return this.name;
		}
		public List<? extends Object> getList() {
			return list;
		}
	
		
		public boolean equals(Object obj){
			if(!(obj instanceof DataObject))
				return false;
			//very simple check, ignore pre-check
			return this.name.equals(((DataObject)obj).getName());
		}
		public int hashCode(){
			return new HashCodeBuilder().append(this.name).toHashCode();
		}
	}

}
