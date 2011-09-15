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


/**
 * @author Dapeng.Ni
 */
public class DiffModel extends GeneralModel {
	public static final int FLAT_HTML = -1;
	
	public static final int NOCHANGE = 0;
	public static final int ADD = 1;
	public static final int DEL = 2;
	
	//index of model used in merge diff during version conflict comparison
	public int index;
	public int type;
	public String content;
	
	public DiffModel(){
	}
	public DiffModel(int type, String content){
		this.type = type;
		this.content = content;
	}
}
