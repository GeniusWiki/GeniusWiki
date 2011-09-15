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
package com.edgenius.wiki.render.impl;

import java.io.Serializable;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class LinkReplacer implements Serializable{
	
	private int type;
	private String fromSpaceUname;
	private String newTitle;
	private String oldTitle;

	private String toSpaceUname;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getFromSpaceUname() {
		return fromSpaceUname;
	}
	public void setFromSpaceUname(String fromSpaceUname) {
		this.fromSpaceUname = fromSpaceUname;
	}
	public String getToSpaceUname() {
		return toSpaceUname;
	}
	public void setToSpaceUname(String toSpaceUname) {
		this.toSpaceUname = toSpaceUname;
	}
	public String getNewTitle() {
		return newTitle;
	}
	public void setNewTitle(String newTitle) {
		this.newTitle = newTitle;
	}
	public String getOldTitle() {
		return oldTitle;
	}
	public void setOldTitle(String oldTitle) {
		this.oldTitle = oldTitle;
	}

}
