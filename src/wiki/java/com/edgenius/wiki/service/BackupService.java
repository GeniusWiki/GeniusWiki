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

import java.io.File;
import java.util.List;

import com.edgenius.license.InvalidLicenseException;

/**
 * @author Dapeng.Ni
 */
public interface BackupService {
	public static final String SERVICE_NAME = "backupService";
	public static final String backup = "backup";
	public static final String restore = "restore";
	
	public static final int BACKUP_DATA = 1<<1; 
	public static final int BACKUP_ATTACHMENT = 1<<2;
	public static final int BACKUP_THEME = 1<<4;
	public static final int BACKUP_INDEX = 1<<5;
	public static final int BACKUP_RSS = 1<<6;
	//backup global.xml, server.xml etc?
	public static final int BACKUP_CONF = 1<<7;
	public static final int BACKUP_SKIN = 1<<8;
	
	//BACKUP_INDEX and BACKUP_RSS is useless to backup as spaceUid is RSS xml name
	//and index also include some UID stuff... so need rebuild after restore. 
	public static final int BACKUP_DEFAULT = BACKUP_DATA|BACKUP_ATTACHMENT|BACKUP_THEME|BACKUP_SKIN|BACKUP_CONF;
	
	/**
	 * 
	 * @param options
	 * @return backup file name
	 * @throws BackupException
	 */
	String backup(int options, String comment) throws BackupException;
	

	void restore(File binder) throws BackupException, InvalidLicenseException;

	/**
	 * 
	 */
	List<File> getRestoreFileList();

	/**
	 * @return
	 */
	List<File> getBackupFileList();

	String getFileComment(File zipFile);
	/**
	 * Put given file into restore directory and ready for list
	 * @param restoreFile
	 */
	void addFileToRestoreList(File restoreFile,String filename);


	/**
	 * 
	 */
	void moveBackupFileToRestoreList(File srcFile);
}
