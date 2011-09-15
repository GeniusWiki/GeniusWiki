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
package com.edgenius.wiki.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.TouchedInfo;
import com.edgenius.wiki.gwt.client.model.GeneralModel;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
@Entity
@Table(name=Constants.TABLE_PREFIX+"WIDGETS")
public class Widget extends TouchedInfo implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(Widget.class);
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "key_seq")  
	@SequenceGenerator(name = "key_seq", sequenceName = Constants.TABLE_PREFIX+"WIDGETS_SEQ")  
	@Column(name="PUID")
	private Integer uid;
	
	//class name, normally
	@Column(name="WIDGET_TYPE")
	private String type;
	
	//UUID
	@Column(name="WIDGET_UUID", unique=true)
	private String uuid;

	@Column(name="TITLE")
	private String title;
	
	@Column(name="DESCRIPTION")
	private String description;
	
	@Type(type="text")
	@Column(name="CONTENT",length=409600)
	private String content;
	
	//How many users use this widget in dashboard, no use so far
	@Column(name="REFERED_COUNT")
	private int referedCount;
	
	//logo image NODE UUID
	@Column(nullable=true, name="LOGO_LARGE")
	private String logoLarge;
	
	@Column(nullable=true, name="LOGO_SMALL")
	private String logoSmall;
	
	@Column(name="SHARED")
	private boolean shared;
	
	
	@Transient
	//only widget is invoked, this field has value 
	private GeneralModel renderContent;
	
	@Transient
	private String titleURL;

	@Transient
	private int[] permimssion = new int[]{0,0};

	//I suppose Widget can bring back some indicator information - it can be combination value of integer etc. 
	//However, it only used by ActivityLogWidget for pagination to indicate if it has next page
	@Transient
	private int flag = 0;
	/**
	 * all clone will set its Uid as null, to future saving.
	 */
	public Widget clone(){
		Widget cWidget = null;
		try {
			cWidget = (Widget) super.clone();
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cWidget;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContent() {
		return content == null?"":content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getReferedCount() {
		return referedCount;
	}

	public void setReferedCount(int referedAmount) {
		this.referedCount = referedAmount;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String key) {
		this.uuid = key;
	}

	public String getLogoLarge() {
		return logoLarge;
	}

	public void setLogoLarge(String logoLarge) {
		this.logoLarge = logoLarge;
	}

	public String getLogoSmall() {
		return logoSmall;
	}

	public void setLogoSmall(String logoSmall) {
		this.logoSmall = logoSmall;
	}

	public GeneralModel getRenderContent() {
		return renderContent;
	}

	public void setRenderContent(GeneralModel renderContent) {
		this.renderContent = renderContent;
	}

	public String getTitleURL() {
		return titleURL;
	}

	public void setTitleURL(String titleURL) {
		this.titleURL = titleURL;
	}

	public int[] getPermimssion() {
		return permimssion;
	}

	public void setPermimssion(int[] permimssion) {
		this.permimssion = permimssion;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}

}
