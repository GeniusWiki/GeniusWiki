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
package com.edgenius.wiki.service;

/**
 * @author Dapeng.Ni
 */
public class ThemeInvalidVersionException extends ThemeInvalidException {
	private static final long serialVersionUID = 3717397505845288106L;

	private float installVersion;
	private float existVersion;
	/**
	 * @param string
	 */
	public ThemeInvalidVersionException(String msg,float installVersion, float existVersion) {
		super(msg);
	
		this.installVersion = installVersion;
		this.existVersion = existVersion;
	}
	public float getInstallVersion() {
		return installVersion;
	}
	public float getExistVersion() {
		return existVersion;
	}
	public void setInstallVersion(int installVersion) {
		this.installVersion = installVersion;
	}
	public void setExistVersion(int existVersion) {
		this.existVersion = existVersion;
	}


}
