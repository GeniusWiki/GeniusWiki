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
package com.edgenius.wiki.render.handler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.server.utils.PageAttribute;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;

/**
 * @author Dapeng.Ni
 */
public class PageAttributeHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(PageAttributeHandler.class);
	private AbstractPage page;

	//sum up page attribute, from java field name....
	public static final Map<String,Integer> attributeNames = new HashMap<String, Integer>();
	static{
		Field[] fields = PageAttribute.class.getDeclaredFields();
		for (Field field : fields) {
			if(field.getName().startsWith(PageAttribute.ATTRIBUTE_PREFIX)){
				try {
					attributeNames.put(field.getName().substring(PageAttribute.ATTRIBUTE_PREFIX.length()).toLowerCase(),field.getInt(null));
				} catch (Exception e) {
					log.error("Unable to build page attribute fields list");
				}
			}
		}
	}

	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) {
		int attribute = 0;
		//sort out value from PageAttribute class NO_ prefix fields
		Set<Entry<String, Integer>> entries = attributeNames.entrySet();
		for (Entry<String, Integer> entry : entries) {
			String val = values.get(entry.getKey());
			if(StringUtils.equalsIgnoreCase(val, "false") || StringUtils.equalsIgnoreCase(val, "no")){
				attribute |= entry.getValue();
				log.info("page attribute '" + entry.getKey() + "' set to false.");
			}
		}
		page.setAttribute(attribute);
		
		return null;
		
	}

	public void init(ApplicationContext context) {
		
	}

	public void renderEnd() {
		page = null;
	}

	public void renderStart(AbstractPage page) {
		this.page = page;
	}

}
