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
package com.edgenius.wiki.gwt.client;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;

/**
 * @author Dapeng.Ni
 */
public class ClientConstants {
		
	//	exactly value ordinal of OPERATIONS
		public static final int READ = 0;
		public static final int WRITE = 1;
		public static final int REMOVE = 2;
		public static final int ADMIN = 3;
		public static final int COMMENT_READ = 4;
		public static final int COMMENT_WRITE = 5;
		public static final int EXPORT = 6;
		public static final int RESTRICT = 7;
		 //remove some offline_code here(0726)
//	    public static final int OFFLINE = 8;
	    
		public static final int SPACE_BASE = SharedConstants.PERM_SPACE_BASE;
		
		public static final int PAGE_ITEM_COUNT = 8;

		
		//SOME keyCode not define in KeyboardListener
		public static final int KEY_F1 = 112;
	    public static final int KEY_F10 = 121;
	    public static final int KEY_F11 = 122;
	    public static final int KEY_F12 = 123;
	    public static final int KEY_F2 = 113;
	    public static final int KEY_F3 = 114;
	    public static final int KEY_F4 = 115;
	    public static final int KEY_F5 = 116;
	    public static final int KEY_F6 = 117;
	    public static final int KEY_F7 = 118;
	    public static final int KEY_F8 = 119;
	    public static final int KEY_F9 = 120;
		public static final int a = 65;
	    public static final int b =	66;
	    public static final int c =	67;
	    public static final int d =	68;
	    public static final int e =	69;
	    public static final int f =	70;
	    public static final int g =	71;
	    public static final int h =	72;
	    public static final int i =	73;
	    public static final int j =	74;
	    public static final int k =	75;
	    public static final int l =	76;
	    public static final int m =	77;
	    public static final int n =	78;
	    public static final int o =	79;
	    public static final int p =	80;
	    public static final int q =	81;
	    public static final int r =	82;
	    public static final int s =	83;
	    public static final int t =	84;
	    public static final int u =	85;
	    public static final int v =	86;
	    public static final int w =	87;
	    public static final int x =	88;
	    public static final int y =	89;
	    public static final int z =	90;
		public static final int LIMIT_CHAR = 140;
		public static final int LEFT = 0;
		public static final int RIGHT = 1;
		public static final int LEFT_SIDE_MENU_WIDTH = 225;
		public static final int DEFAULT_MENU_TOP = 58+40;

		
}
