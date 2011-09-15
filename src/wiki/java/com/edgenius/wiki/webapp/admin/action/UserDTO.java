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
package com.edgenius.wiki.webapp.admin.action;

import com.edgenius.core.model.User;

/**
 * @author Dapeng.Ni
 */
public class UserDTO {
	private User user;
	private String createdDate;
	
	private long authorSize;
	private long modifierSize;
	private int spaceAuthorSize;
	private int commentSize;
	private boolean removable;
	
	public boolean isRemovable() {
		return removable;
	}
	public void setRemovable(boolean removable) {
		this.removable = removable;
	}
	public long getAuthorSize() {
		return authorSize;
	}
	public void setAuthorSize(long authorSize) {
		this.authorSize = authorSize;
	}
	public long getModifierSize() {
		return modifierSize;
	}
	public void setModifierSize(long modifierSize) {
		this.modifierSize = modifierSize;
	}
	public int getSpaceAuthorSize() {
		return spaceAuthorSize;
	}
	public void setSpaceAuthorSize(int spaceAuthorSize) {
		this.spaceAuthorSize = spaceAuthorSize;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	public int getCommentSize() {
		return commentSize;
	}
	public void setCommentSize(int commentSize) {
		this.commentSize = commentSize;
	}
}
