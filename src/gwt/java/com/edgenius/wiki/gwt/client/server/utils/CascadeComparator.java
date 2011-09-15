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
package com.edgenius.wiki.gwt.client.server.utils;

import java.io.Serializable;
import java.util.Comparator;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.model.CascadeObject;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
public class CascadeComparator implements Comparator<CascadeObject> , Serializable{
	private static final long serialVersionUID = 4131873435689042718L;
	//currently, only can use old style: append post to last, rather than dynamically adjust sequence and put latest to first.
	//The reason is in client side, it won't refresh all topics after submit. It sorts topic according to OLD_STYLE. 
	public static final boolean OLD_FORUM_STYLE = true;
	
	public int compare(CascadeObject msgSeq1, CascadeObject msgSeq2) {
		
		int level1,level2;
		level1 = msgSeq1.getLevel();
		level2 = msgSeq2.getLevel();
		CascadeObject parent1,parent2;
		//choose the smaller level value
		int lessLevel = level1>level2? level2:level1;
		for(int compareLevel=0;compareLevel <= lessLevel;compareLevel++){
			//init value, loop from current message
			parent1 = msgSeq1;
			parent2 = msgSeq2;
			level1 = msgSeq1.getLevel();
			level2 = msgSeq2.getLevel();
			while(level1 > compareLevel){
				//get parent until assigned level
				if(parent1 == null){
					Log.error("Message "+ parent1 +" level "+ level1 +" has null parent");
					return 0;
				}
				parent1 = parent1.getParent();
				level1--;
			}
			while(level2 > compareLevel){
				//get parent until assigned level
				if(parent2 == null){
					Log.error("Message "+ parent2 +" level "+ level2 +" has null parent");
					return 0;
				}
				parent2 = parent2.getParent();
				level2--;
			}
			//this comparation will handle different branch node
			if(parent1 != parent2){
				if(parent1 == null){
					return 1;
				}else{
					//compare last modified date, the latest is at beginning
					if(OLD_FORUM_STYLE)
						return  parent1.before(parent2)?-1:1;
					else
						return  parent1.before(parent2)?1:-1;
				}
			}
			//this comparation will handle same branch node
			//the direct parent level, their parent(or themselves) are still equal
			if(compareLevel==lessLevel){
				if(msgSeq1.getLevel() != msgSeq2.getLevel())
					return msgSeq1.getLevel() -msgSeq2.getLevel();
				else{
					if(OLD_FORUM_STYLE)
						return msgSeq1.before(msgSeq2)?-1:1;
					else
						return msgSeq1.before(msgSeq2)?1:-1;
				}
			}
			
		}
		if(OLD_FORUM_STYLE)
			return msgSeq1.before(msgSeq2)?-1:1;
		else
			return msgSeq1.before(msgSeq2)?1:-1;
	}

}

