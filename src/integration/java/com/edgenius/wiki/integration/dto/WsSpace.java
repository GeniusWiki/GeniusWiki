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
package com.edgenius.wiki.integration.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.integration.WsContants;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
@XmlAccessorType(XmlAccessType.FIELD)  
@XmlType(name = "Space", namespace=WsContants.NS)  
public class WsSpace {

	private String spaceKey;
	private String spaceTitle;
	private String description;
	private String tags;
	private boolean privateSpace;
	//defaultWiki, defaultBlog
	private String theme;
	//********************************************************************
	//               Function method
	//********************************************************************
	public void copyTo(Space pSpace) {
		pSpace.setUnixName(this.getSpaceKey());
		pSpace.setName(this.getSpaceTitle());
		pSpace.setDescription(this.getDescription());
		
		SpaceSetting set = pSpace.getSetting();
		set.setTheme(this.getTheme());
		//MUST put it back, because getSetting() may return a new SpaceSetting instance which is not reference to space.
		pSpace.setSetting(set);
		
		pSpace.setType((short)(this.isPrivateSpace()?1:0));
		pSpace.setTagString(this.getTags());
		
	}
	
	public int hashCode(){
		return spaceKey != null?spaceKey.hashCode():-1;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof WsSpace)){
			return false;
		}
		
		return StringUtil.equalsIgnoreCase(this.spaceKey, ((WsSpace)obj).spaceKey);
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getSpaceKey() {
		return spaceKey;
	}
	public void setSpaceKey(String spaceKey) {
		this.spaceKey = spaceKey;
	}
	public String getSpaceTitle() {
		return spaceTitle;
	}
	public void setSpaceTitle(String spaceName) {
		this.spaceTitle = spaceName;
	}
	public boolean isPrivateSpace() {
		return privateSpace;
	}
	public void setPrivateSpace(boolean privateSpace) {
		this.privateSpace = privateSpace;
	}
	public String getTheme() {
		return theme;
	}
	public void setTheme(String theme) {
		this.theme = theme;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
}
