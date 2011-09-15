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

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"EXT_TODO_ITEMS")
public class TodoItem extends TouchedInfo implements Cloneable, Serializable{
	private static final long serialVersionUID = -3381706830953283441L;
	private static final Logger log = LoggerFactory.getLogger(TodoItem.class);

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"TODO_ITEM_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Type(type="text")
	@Column(name="ITEM_CONTENT", length=4000)
	private String content;
	
	@Column(name="ITEM_PRIORITY")
	private int priority;
	
	//non-null, ensure freemarker always get value for rendering
	//not directly link to TodoStatus object as this status maybe not exist in TodoStatus table.
	@Column(name="ITEM_STATUS", nullable=false)
	private String status;
	

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TODO_PUID", nullable=false)
	private Todo todo;

	
	//this field is maybe not persist object from TodoStatus table as user can easily update status list by macro status parameters.
	//However, for display purpose, statusObj must have TodoStatus.text and TodoStatus.sequence filled in.
	@Transient
	private TodoStatus statusObj;
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		TodoItem node = null;
		try {
			node = (TodoItem) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}
	

	//********************************************************************
	//               set /get 
	//********************************************************************
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public Todo getTodo() {
		return todo;
	}
	public void setTodo(Todo todo) {
		this.todo = todo;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public TodoStatus getStatusObj() {
		return statusObj;
	}


	public void setStatusObj(TodoStatus statusObj) {
		this.statusObj = statusObj;
	}

}
