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
package com.edgenius.core.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Dapeng.Ni
 */
public class CompareToComparator<T extends Comparable<T>> implements Comparator<T>, Serializable{
	private static final long serialVersionUID = -7453316449504497364L;
	
	//if choose this type, any object has same key value (int), new object will replace old one
	public static final int TYPE_OVERWRITE_SAME_VALUE = 1<<1;
	public static final int TYPE_KEEP_SAME_VALUE = 1<<2;
	public static final int ASCEND= 1<<3;
	public static final int DESCEND = 1<<4;

	private int type;
	public CompareToComparator(){
		this.type = TYPE_KEEP_SAME_VALUE|ASCEND;
	}
	public CompareToComparator(int type){
		this.type = type;
	}
	public int compare(T o1, T o2) {
		if(o1 == null)
			return -1;
		int rs = o1.compareTo(o2);
		if(rs == 0){
			//never return zero if TYPE_KEEP_SAME_VALUE,so that old object won't override by new object which has same key.
			rs = ((type & TYPE_OVERWRITE_SAME_VALUE) > 0)?0:1;
		}
		if((type & DESCEND) > 0)
			rs = -1*rs;
		return rs;
	}

}
