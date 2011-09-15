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
package com.edgenius.wiki.search.service;

/**
 * @author Dapeng.Ni
 */
public interface IndexRebuildListener {
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static final int PAGE = 1;
	public static final int SPACE = 2;
	public static final int ATTACHMENT = 3;
	public static final int PTAG = 4;
	public static final int STAG = 5;
	public static final int USER = 6;
	public static final int COMMENT = 7;
	public static final int WIDGET = 8;
	public static final int ROLE = 9;
	
	public static final int INDEX_SIZE = PAGE+SPACE+ATTACHMENT+PTAG+STAG+USER+COMMENT+WIDGET+ROLE; 
	
	public void indexComplete(int type);
}
