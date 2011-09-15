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
package com.edgenius.wiki.plugin;

import java.io.File;
import java.util.Map;

import com.edgenius.wiki.service.DataBinder;

/**
 * 
 * @author Dapeng.Ni
 */
public interface PluginServiceProvider {

	String invokePluginService(String operation, String[] params) throws PluginServiceProviderException;

	/**
	 * Export plugin database objects to DataBinder.  
	 * @see com.edgenius.wiki.service.impl.BackupServiceImpl.exportData()
	 * @param binder
	 */
	void backup(DataBinder binder);

	/**
	 * Restore plugin database objects from DataBinder. 
	 * @see com.edgenius.wiki.service.impl.BackupServiceImpl.importData()
	 * @param binder
	 */
	void restore(DataBinder binder);

	/**
	 * Clean plugin database objects. 
	 * @see com.edgenius.wiki.service.impl.BackupServiceImpl.cleanDatabase()
	 */
	void resorePreClean();
	
	/**
	 * Get files that need to be exported when system requires page or space export. For example, style sheet or images
	 * for render properly in export signle HTML page.
	 * @return File and path info. In export zip, path info is relative path to file root.
	 * For example, file is c:/temp/style/abc.css, relative path is style/, then zip file full path will be /style/abc.css.
	 */
	Map<File, String> exportResources();
	
}
