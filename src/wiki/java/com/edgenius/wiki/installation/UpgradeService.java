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
package com.edgenius.wiki.installation;

import java.io.File;


/**
 * @author Dapeng.Ni
 */
public interface UpgradeService {

	String SERVICE_NAME = "upgradeService";

	/**
	 * @param version
	 * @param version2
	 */
	void doUpgarde(String from, String to) throws Exception;

	/**
	 * This is for importing backup package upgrade. The "to" version is system current version,ie,  Version.VERSION 
	 * @param from
	 * @param binderFile 
	 * @throws Exception
	 */
	void doBackupPackageUpgardeForBinder(String from, File binderFile) throws Exception;

	/**
	 * @param from
	 */
	void doBackupPackageUpgardeForDataFiles(String from) throws Exception;
	
}
