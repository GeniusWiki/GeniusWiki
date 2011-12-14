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
 * @author Dapeng.Ni
 */
public class RegionBorderPointComparator implements Comparator<RegionBorderPoint>, Serializable{
	private static final long serialVersionUID = 4032071068588625412L;

	@Override
	public int compare(RegionBorderPoint o1, RegionBorderPoint o2) {
		int rs = o2.getPoint() - o1.getPoint();
		if(rs !=0)
			return rs;
		
		if(!o1.start && o2.start)
			//put o2(start) before o1(end) 
			return 1;
		
		if(o1.start && !o2.start)
			//put o1(start) before o2(end)
			return -1;
		
		
		rs = o1.pos2 - o2.pos2;
		if(rs != 0)
			return rs;
		
		
		return o1.start?o1.getRegionKey().compareTo(o2.regionKey):o2.getRegionKey().compareTo(o1.regionKey);
	}

}
