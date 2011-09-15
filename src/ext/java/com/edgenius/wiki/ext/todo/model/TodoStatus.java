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
package com.edgenius.wiki.ext.todo.model;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */

public class TodoStatus implements Cloneable, Serializable{
	private static final long serialVersionUID = 872779496069114888L;
	private static final Logger log = LoggerFactory.getLogger(TodoStatus.class);
	
	private String text;
	
	private int sequence;
	
	private boolean deleteAction;
	
	private boolean persisted;
	
	private int itemsCount;
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		TodoStatus node = null;
		try {
			node = (TodoStatus) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}
	public boolean equals(Object obj){
		if(!(obj instanceof TodoStatus))
			return false;
		
		return (((TodoStatus)obj).getSequence() == this.sequence) 
			&& (StringUtils.equalsIgnoreCase(((TodoStatus)obj).getText(), this.text));
	}
	
	public int hashCode(){
		return new HashCodeBuilder().append(text.toLowerCase()).append(sequence).toHashCode();
	}

	public String toString(){
		return text+"("+sequence+")";
	}
	//********************************************************************
	//               set /get 
	//********************************************************************

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getSequence() {
		return sequence;
	}


	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public boolean isPersisted() {
		return persisted;
	}
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	public boolean isDeleteAction() {
		return deleteAction;
	}
	public void setDeleteAction(boolean deleteAction) {
		this.deleteAction = deleteAction;
	}
	public int getItemsCount() {
		return itemsCount;
	}
	public void setItemsCount(int itemsCount) {
		this.itemsCount = itemsCount;
	}
	
}
