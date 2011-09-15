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
package com.edgenius.wiki.render;

import java.io.Serializable;
import java.util.Comparator;

/**
 * if region A is right on region B, then A is before B;
 * <br> 
 * if region A is inside region B, A is before B
 * <br>
 * Such as (s is start of region, e is end of region):
 * <pre> 
 *  4s 3s 2s 2e 3e 4e 1s 1e
 * </pre>
 * 
 * The sort order is 1,2,3,4.
 * @author Dapeng.Ni
 */
public class RegionComparator implements Comparator<Region>, Serializable {

	private static final long serialVersionUID = -1340658034533405132L;

	//JDK1.6 @Override
	public int compare(Region o1, Region o2) {
		//equals must before isContain() as later method also includes equals() condition.
		if(o1.equals(o2) && o1.getKey() != null){
			//this is cooperate with RegionBorderPointComparator.compare()
			//if Region is equal then compare its key. 
			return o1.getKey().compareTo(o2.getKey());
		}
		if(o1.isContain(o2))
			return 1;
		if(o2.isContain(o1))
			return -1;
		
		if(o1.isLeft(o2))
			return 1;
		
		
		return -1;
	}

}
