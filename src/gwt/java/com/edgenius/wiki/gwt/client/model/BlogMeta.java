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

import java.io.Serializable;
import java.util.List;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Dapeng.Ni
 */
@XStreamAlias("blogMeta")
public class BlogMeta implements Serializable{

	private static final long serialVersionUID = 5733385368266080581L;
	
	public static final int TYPE_WORDPRESS = 1;
	public static final int TYPE_BLOGGER = 2;
	public static final int TYPE_MT = 3;
	
	private String key;
	private int type;
	private String id;
	private String name;
	private boolean isAdmin;
	private String xmlrpc;
	private String url;
	private String username;
	private String password;
	private List<BlogCategory> categories;
	
	//this is not cloned
	private String error;
	private BlogPostMeta postValue;

	public Object clone(){
		//as this class in GWT side, don't use Java default clone
		BlogMeta meta = new BlogMeta();
		meta.key  = this.key;
		meta.type = this.type;
		meta.id = this.id;
		meta.name = this.name;
		meta.isAdmin = this.isAdmin;
		meta.xmlrpc = this.xmlrpc;
		meta.url = this.url;
		meta.username = this.username;
		meta.password = this.password;
		meta.categories = this.categories;
		return meta;
		
	}
	public boolean equals(Object obj){
		if(!(obj instanceof BlogMeta))
			return false;
		
		if(!StringUtil.isBlank(xmlrpc)){
			return StringUtil.equals(((BlogMeta)obj).xmlrpc, this.xmlrpc);
		}else{
			return StringUtil.equals(((BlogMeta)obj).key, this.key);
		}
		
	}
	
	public int hashCode(){
		return (StringUtil.isBlank(xmlrpc)?xmlrpc.hashCode():0) +(StringUtil.isBlank(key)?key.hashCode():0); 
	}
	public String toString(){
		return username + "@" + xmlrpc + ":Type" + type;
	}
	
	public int getType() {
		return type;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAdmin() {
		return isAdmin;
	}
	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	public String getXmlrpc() {
		return xmlrpc;
	}
	public void setXmlrpc(String xmlrpc) {
		this.xmlrpc = xmlrpc;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<BlogCategory> getCategories() {
		return categories;
	}
	public void setCategories(List<BlogCategory> categories) {
		this.categories = categories;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public void setPostValue(BlogPostMeta postValue) {
		this.postValue = postValue;
	}
	

	public BlogPostMeta getPostValue() {
		return postValue;
	}
}
