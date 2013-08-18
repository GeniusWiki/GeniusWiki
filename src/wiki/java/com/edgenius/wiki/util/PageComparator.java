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
package com.edgenius.wiki.util;

import java.io.Serializable;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.model.Page;

/**
 * @author Dapeng.Ni
 */
public class PageComparator implements Comparator<Page>, Serializable{
	private static final long serialVersionUID = -5498408024519027386L;

	private static final Logger log = LoggerFactory.getLogger(PageComparator.class);

	private String homepageUuid;
	public PageComparator(String uuid) {
		homepageUuid = uuid;
	}

	public int compare(Page p1, Page p2) {
		if(p1 == p2) return 0;
		
		//must not null, if both null, then need to do following compare.
		if(p1.getPageUuid() != null && p1.getPageUuid().equals(p2.getPageUuid())) 
			return 0;
		
		int level1,level2;
		level1 = p1.getLevel();
		level2 = p2.getLevel();
		Page parent1,parent2;
		//choose the smaller level value
		int lessLevel = level1>level2? level2:level1;
		for(int compareLevel=0;compareLevel <= lessLevel;compareLevel++){
			//init value, loop from current message
			parent1 = p1;
			parent2 = p2;
			level1 = p1.getLevel();
			level2 = p2.getLevel();
			while(level1 > compareLevel){
				//get parent until assigned level
				if(parent1 == null){
					log.error("Message "+ parent1 +" level "+ level1 +" has null parent");
					return 0;
				}
				parent1 = parent1.getParent();
				level1--;
			}
			while(level2 > compareLevel){
				//get parent until assigned level
				if(parent2 == null){
					log.error("Message "+ parent2 +" level "+ level2 +" has null parent");
					return 0;
				}
				parent2 = parent2.getParent();
				level2--;
			}
			//this comparison will handle different branch node
			if(parent1 == null || !parent1.equals(parent2)){
				//sort by title
				if(parent1 == null){
					//it means parent1 is root, parent2 must be after it
					return 1;
				}if(parent2 == null){
					return -1;
				}else
					return  ct(parent1, parent2);
			}
			//this comparison will handle same branch node
			//the direct parent level, their parent(or themselves) are still equal
			if(compareLevel==lessLevel){
				if(p1.getLevel() != p2.getLevel())
					return p1.getLevel() -p2.getLevel();
				else{
					return ct(p1, p2);
				}
			}
			
		}
		//always put homepage in the first level
		if(p1.getPageUuid().equals(homepageUuid))
			return 1;
		if(p2.getPageUuid().equals(homepageUuid))
			return -1;
		
		return ct(p1, p2);

	}

	private int ct(Page p1, Page p2) {
		//even title are same, then try to compare pageUuid: This critical when deleting space
		//if there are same title in trash bin, this can ensure deleted duplicated page is still in page list 
		int ret = p1.getTitle() != null? p1.getTitle().compareTo(p2.getTitle()):1;
		if(ret == 0 && p1.getPageUuid() != null){
			ret = p1.getPageUuid().equals(p2.getPageUuid())?0:1;
		}else if(ret == 0){
			//if pageUuid is null, I assume it is different page
			ret = 1;
		}
		return ret; 
	}

}
