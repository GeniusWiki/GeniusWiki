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
package com.edgenius.core.model;

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.edgenius.core.Constants;

@SuppressWarnings("serial")
@Entity
@Table(name=TABLE_PREFIX+"CONF")
public class Configuration implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator="key_seq")
	@SequenceGenerator(name="key_seq", sequenceName=Constants.TABLE_PREFIX+"CONF_SEQ")
	@Column(name="PUID")
	private Integer uid;
	
	@Column(name="SETTING_TYPE")
	private String type;
	
	@Type(type="text")
	@Column(name="SETTING_VALUE", length=409600)  //400K about 40,000 line text
	private String value;
	
	public String getType() {
		return type;
	}
	public void setType(String keyClass) {
		this.type = keyClass;
	}
	public String getValue() {
		return value == null?"":value;
	}
	public void setValue(String keyValue) {
		this.value = keyValue;
	}
	public Integer getUid() {
		return uid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	
}
