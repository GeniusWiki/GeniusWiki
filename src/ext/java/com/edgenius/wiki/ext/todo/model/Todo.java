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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;
import com.edgenius.wiki.ext.todo.TodoMacro;

/**
 * @author Dapeng.Ni
 */
@Entity
@Table(name=TABLE_PREFIX+"EXT_TODOS",uniqueConstraints={@UniqueConstraint(columnNames={"PAGE_UUID","TODO_NAME"})})
@org.hibernate.annotations.Table(appliesTo = Constants.TABLE_PREFIX+"EXT_TODOS",
indexes = { @Index(name = "TODOS_PUUID_IDX", columnNames = {"PAGE_UUID"})})
public class Todo extends TouchedInfo implements Cloneable, Serializable{
	private static final long serialVersionUID = 8161465759131906336L;
	private static final Logger log = LoggerFactory.getLogger(Todo.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"TODO_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="PAGE_UUID")
	private String pageUuid;
	
	@Column(name="TODO_NAME")
	private String name;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy="todo", orphanRemoval=true)
	@OrderBy("createdDate DESC")
	private List<TodoItem> items;

	@Transient
	private List<TodoStatus> statuses;
	
	//********************************************************************
	//               function method
	//********************************************************************
	public Object clone(){
		Todo node = null;
		try {
			node = (Todo) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed ", e);
		}
		return node;
	}
	/**
	 * Set Status string, which will replace old statuses if existing or added into status list.
	 * 
	 * @param statusString
	 */
	public void setStatusString(String statusString, String deleteAction) {
		if(StringUtils.isBlank(statusString)){
			statusString = TodoMacro.DEFAULT_STATUS;
			deleteAction = TodoMacro.DEFAULT_DELETE_ON;
		}
		
		statuses = new ArrayList<TodoStatus>();
		
		//Normally, status separated by comma, however, status can not include space because it will use as HTML "class" attribute
		//so, here also split it by space, means, status can be separated by comma and space. 
		String[] list = statusString.split("[, ]+");
		
		//sequence from large to short
		int idx = 0;
		for (String str : list) {
			TodoStatus status = new TodoStatus();
			status.setText(str);
			status.setSequence(idx);
			status.setPersisted(true);
			status.setDeleteAction(StringUtils.equalsIgnoreCase(deleteAction, str));
			statuses.add(status);
			idx++;
		}
		
	}
	public boolean isDirtyStatus(String statusString, String deleteAction) {
		if(StringUtils.isBlank(statusString)){
			//if input is blank, set it as dirty, then it will be replaced by system status replaced.  
			return true;
		}
		String[] list = statusString.split("[, ]+");
		if(list.length != statuses.size())
			return true;

		String currDeleteAction = null;
		boolean dirty;
		for (TodoStatus status : statuses) {
			dirty = true;
			for (String str : list) {
				if(StringUtils.equalsIgnoreCase(str, status.getText())){
					dirty = false;
					break;
				}
			}
			if(dirty)
				return dirty;
			
			currDeleteAction = status.isDeleteAction()?status.getText():null;
		}
		if(!StringUtils.equalsIgnoreCase(deleteAction, currDeleteAction))
			return false;
		
		return false;
	}

	//********************************************************************
	//               set / get
	//********************************************************************
	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getPageUuid() {
		return pageUuid;
	}

	public void setPageUuid(String pageUuid) {
		this.pageUuid = pageUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TodoItem> getItems() {
		return items;
	}

	public void setItems(List<TodoItem> items) {
		this.items = items;
	}
	public List<TodoStatus> getStatuses() {
		return statuses;
	}
	public void setStatuses(List<TodoStatus> statuses) {
		this.statuses = statuses;
	}
	
}
