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

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Sample:
 * {categoryName=Plan, parentId=0, htmlUrl=http://10.88.30.82:82/wordpress/?cat=5, 
 * categoryDescription=, description=Plan, categoryId=5, rssUrl=http://10.88.30.82:82/wordpress/?feed=rss2&amp;cat=5}
 * 
 * @author Dapeng.Ni
 */
@XStreamAlias("blogCategory")
public class BlogCategory implements Serializable, CascadeObject<BlogCategory>{
	private static final long serialVersionUID = 2116818457727920343L;

	private String name;
	private String id;
	private String parentId;
	private String description;
	private boolean checked;
	
	private BlogCategory parent;
	private int level;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public BlogCategory getParent() {
		return parent;
	}
	public void setParent(BlogCategory parent) {
		this.parent = parent;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public boolean before(BlogCategory obj) {
		return obj.name.compareTo(this.name)>0;
	}
}
