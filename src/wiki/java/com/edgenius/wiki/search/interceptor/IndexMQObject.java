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
package com.edgenius.wiki.search.interceptor;

import java.io.Serializable;

/**
 * @author Dapeng.Ni
 */
public class IndexMQObject implements Serializable{

	private static final long serialVersionUID = -3018003227382923941L;
	public static final int TYPE_INSERT_PAGE = 1;
	public static final int TYPE_REMOVE_PAGE = 2;

	public static final int TYPE_INSERT_SPACE = 5;
	public static final int TYPE_REMOVE_SPACE = 6;
	
	public static final int TYPE_INSERT_USER = 10;
	public static final int TYPE_REMOVE_USER = 11;
	
	//page tag
	public static final int TYPE_INSERT_PTAG = 15;
	public static final int TYPE_REMOVE_PTAG = 16;
	//---only for Shell service request page tag change - batch on one page, not individual for single tag.
	//---This type message is not be used for Indexing!!!
	public static final int TYPE_INSERT_PTAG_BATCH = 14;
	
	//space tag
	public static final int TYPE_INSERT_STAG = 17;
	public static final int TYPE_REMOVE_STAG = 18;
	
	public static final int TYPE_INSERT_ATTACHMENT = 20;
	public static final int TYPE_REMOVE_ATTACHMENT = 21;
	public static final int TYPE_UPDATE_ATTACHMENT = 22;
	//---only for Shell service request attachment change - batch on one page, not individual for single attachment.
	//---This type message is not be used for Indexing!!!
	public static final int TYPE_INSERT_ATTACHMENT_BATCH = 23;
	
	public static final int TYPE_INSERT_COMMENT = 30;
	public static final int TYPE_REMOVE_COMMENT = 31;
	
	public static final int TYPE_INSERT_WIDGET = 40;
	public static final int TYPE_REMOVE_WIDGET = 41;
	
	public static final int TYPE_INSERT_ROLE = 50;
	
	private int type;
	private Object obj;
	
	//default constructor for serializable
	public IndexMQObject(){
		
	}
	
	public IndexMQObject(int type, Object obj){
		this.type = type;
		this.obj = obj;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}

}
