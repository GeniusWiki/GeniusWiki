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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.model.PageComment;

/**
 * 
 * @author Dapeng.Ni
 */
public class CommentComparator  implements Comparator<PageComment> , Serializable{
	private static final long serialVersionUID = -3653941758574279372L;
	private static final Logger log = LoggerFactory.getLogger(CommentComparator.class);
	//currently, only can use old style: append post to last, rather than dynamically adjust sequence and put latest to first.
	//The reason is in client side, it won't refresh all topics after submit. It sorts topic according to OLD_STYLE. 
	public static final boolean OLD_FORUM_STYLE = true;
	
	public int compare(PageComment msgSeq1, PageComment msgSeq2) {
		
		int level1,level2;
		level1 = msgSeq1.getLevel();
		level2 = msgSeq2.getLevel();
		PageComment parent1,parent2;
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
			//this comparation will handle different branch node
			if(parent1 != parent2){
				if(parent1 == null){
					return 1;
				}else{
					//compare last created date, the latest is at beginning
					if(OLD_FORUM_STYLE)
						return  parent1.getCreatedDate().before(parent2.getCreatedDate())?-1:1;
					else
						return  parent1.getCreatedDate().before(parent2.getCreatedDate())?1:-1;
				}
			}
			//this comparation will handle same branch node
			//the direct parent level, their parent(or themselves) are still equal
			if(compareLevel==lessLevel){
				if(msgSeq1.getLevel() != msgSeq2.getLevel())
					return msgSeq1.getLevel() -msgSeq2.getLevel();
				else{
					if(OLD_FORUM_STYLE)
						return msgSeq1.getCreatedDate().before(msgSeq2.getCreatedDate())?-1:1;
					else
						return msgSeq1.getCreatedDate().before(msgSeq2.getCreatedDate())?1:-1;
				}
			}
			
		}
		if(OLD_FORUM_STYLE)
			return msgSeq1.getCreatedDate().before(msgSeq2.getCreatedDate())?-1:1;
		else
			return msgSeq1.getCreatedDate().before(msgSeq2.getCreatedDate())?1:-1;
	}
	/**
	 * This method ensure all comment's parents before itself into container.
	 * @param container
	 * @param comment
	 * @param parents
	 * @return 
	 */
	public static List<PageComment> getParentBeforeSortedList(List<PageComment> list) {
		List<PageComment> sortedComments = new ArrayList<PageComment>();
		
		//below code will ensure any parents will saved before their children.
		for (PageComment comment : list) {
			if(comment.getParent() != null){
				//but also need avoid infinite looping, e.g, A parent is B, B parent is A, although this is unexpected, but need take care 
				List<PageComment> parents = new ArrayList<PageComment>();
				processParentComment(sortedComments, comment, parents);
			}
			sortedComments.add(comment);
		}
		
		return sortedComments;
	}
	private static void processParentComment(List<PageComment> container, PageComment comment, List<PageComment> parents) {
		PageComment parent = comment.getParent();
		if(parent != null){
			if(parents.indexOf(parent) == -1){
				if(container.indexOf(parent) == -1){
					parents.add(parent);
					processParentComment(container, parent, parents);
					container.add(parent);
				}
			}else{
				//there are infinite looping in parents
				AuditLogger.error("There are infinit parents looping:" + Arrays.toString(parents.toArray()) + "; Comment parent is set to null" + comment);
				//just make this page's parent as null and stop looping...
				comment.setParent(null);
			}
		}
		//top of page tree: parent == null
	}


}
