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

import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.DataBinder;

import freemarker.template.Template;

/**
 * @author Dapeng.Ni
 */
public interface PluginService {
	
	String SERVICE_NAME = "pluginService";
	/**
	 * @param linkPluginClz
	 * @return
	 */
	String getPluginUuid(String linkPluginClz);
	
	LinkPlugin getCLinkByUuid(String uuid);

	String renderMacro(MacroModel macroModel, RenderContext renderContext);
	String renderMacro(String macroName, Map<String,Object> map, RenderContext renderContext) throws PluginRenderException;

	/**
	 * @param serviceName
	 * @param operation
	 * @param params
	 * @throws PluginServiceProviderException 
	 */
	String invoke(String serviceName, String operation, String[] params) throws PluginServiceException, PluginServiceProviderException;
	
	void backup(DataBinder binder);
	
	void restore(DataBinder binder);

	void resorePreClean();
	
	/**
	 * Get Freemarker template by given name from specified macro resource directory.
	 * @param macroName
	 * @param templName
	 * @return
	 */
	public Template getPluginTemplate(String macroName, String templName);

	/**
	 * @return
	 */
	Map<File, String> getPluginResourceForExport();
	
}
