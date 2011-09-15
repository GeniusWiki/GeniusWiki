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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Some data is sensitive by other table data, such as attachment table modified, it also means page information changed. 
 * This sensitive touched information(Date) is useful to do version check,such as offline program.
 *   
 * @author Dapeng.Ni
 */
@MappedSuperclass
public abstract class SensitiveTouchedInfo extends TouchedInfo {
	
	@Column(name="TOUCHED_DATE")
	private Date touchedDate;

	public Date getTouchedDate() {
		return touchedDate;
	}

	public void setTouchedDate(Date touchedDate) {
		this.touchedDate = touchedDate;
	}
}
