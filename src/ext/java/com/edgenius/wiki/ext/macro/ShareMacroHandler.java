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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.plugin.PluginRenderException;
import com.edgenius.wiki.plugin.PluginService;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;

/**
 * @author Dapeng.Ni
 */
public class ShareMacroHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(ShareMacroHandler.class);
	
	private PluginService pluginService;

	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		try{	
			Map<String, Object> map = new HashMap<String, Object>();
			pieces.add(new TextModel(pluginService.renderMacro("share",map,renderContext)));
		} catch (PluginRenderException e) {
			log.error("ShareMacro render failed",e);
			throw new RenderHandlerException("Calendar can't be rendered.");
		}
		
		return pieces;
	}

	public void renderStart(AbstractPage page) {
	}
	

	public void init(ApplicationContext context) {
		pluginService = (PluginService) context.getBean(PluginService.SERVICE_NAME);
		
	}
	public void renderEnd() {
	}

}
