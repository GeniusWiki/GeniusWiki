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
package com.edgenius.wiki.gwt.server;

import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.PluginModel;
import com.edgenius.wiki.gwt.client.server.PluginController;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.gwt.server.handler.GWTSpringController;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.plugin.LinkPlugin;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.plugin.PluginServiceException;
import com.edgenius.wiki.plugin.PluginServiceProviderException;

/**
 * @author Dapeng.Ni
 */
public class PluginControllerImpl  extends GWTSpringController implements PluginController {
	
	private PluginService pluginService; 
	
	//JDK1.6 @Override
	public PageModel invokeLink(String spaceUname,  String params) {
		PageModel model = new PageModel();
		
		String[] paramsList = LinkUtil.parseCLinkParamters(params);
		if(paramsList != null && paramsList.length > 0){
			LinkPlugin link = pluginService.getCLinkByUuid(paramsList[0]);
			String[] tokens = null;
			if(paramsList.length > 1){
				tokens = new String[paramsList.length-1];
				System.arraycopy(paramsList,1 , tokens, 0, tokens.length);
			}
			Page page = link.invoke(spaceUname,  tokens);
			PageUtil.copyPageToModel(page, model, userReadingService, PageUtil.COPY_ATTACHMENT_WITHOUT_DRAFT);
			
		}
		return model;
	}
	
	public PluginModel request(String spaceUname, String pageUuid, String serviceName, String operation, String[] params) {
		PluginModel model = new PluginModel();
		try {
			model.response = pluginService.invoke(serviceName,operation, params);
		} catch (PluginServiceProviderException e) {
			model.errorCode = ErrorCode.PLUGIN_INVOKE_FAILED;
		} catch (PluginServiceException e) {
			model.errorCode = ErrorCode.PLUGIN_LOAD_FAILED;
		}
		
		return model;
	}
	
	//********************************************************************
	//               set / get method
	//********************************************************************
	public void setPluginService(PluginService pluginService) {
		this.pluginService = pluginService;
	}
	

}
