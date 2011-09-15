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
package com.edgenius.wiki.ext.macro;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.edgenius.core.Global;
import com.edgenius.core.util.FileUtil;
import com.edgenius.wiki.plugin.PluginServiceProvider;
import com.edgenius.wiki.plugin.PluginServiceProviderException;
import com.edgenius.wiki.service.DataBinder;

/**
 * Only for export function
 * @author Dapeng.Ni
 */
public class ShareService implements PluginServiceProvider {


	public Map<File, String> exportResources() {
		Map<File, String> map = new HashMap<File, String>();
		map.put(new File(FileUtil.getFullPath(Global.ServerInstallRealPath, "plugins","share","resources")),
				FileUtil.getFullPath( "plugins","share"));
		return map;
	}
	
	public String invokePluginService(String operation, String[] params) throws PluginServiceProviderException {
		return null;
	}
	public void backup(DataBinder binder) {
	}

	public void restore(DataBinder binder) {
	}

	public void resorePreClean() {
	}


}
