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
package com.edgenius.wiki.rss;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.jdom.Element;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleParser;
import com.sun.syndication.io.impl.DateParser;

/**
 * @author Dapeng.Ni
 */
public class PageRSSParser implements ModuleParser {
//	private static final Logger log = LoggerFactory.getLogger(PageRSSParser.class);
	public String getNamespaceUri() {
		return PageRSSModule.URI;
	}

	public Module parse(Element item) {
		PageRSSModule pageModule = new PageRSSModuleImpl();
		List<Element> children = item.getChildren();
		if(children != null){
			for (Element element : children) {
				if (element.getNamespace().equals(PageRSSModule.NAMESPACE)) {
					if (element.getName().equals(PageRSSModule.SPACEUNAME)) {
						pageModule.setSpaceUname(element.getTextTrim());
					}else if (element.getName().equals(PageRSSModule.PAGE_UUID)) {
						pageModule.setPageUuid(element.getTextTrim());
					}else if (element.getName().equals(PageRSSModule.CREATOR)) {
						pageModule.setCreator(element.getTextTrim());
					}else if (element.getName().equals(PageRSSModule.MODIFIER)) {
						pageModule.setModifier(element.getTextTrim());
					}else if (element.getName().equals(PageRSSModule.CREATE_DATE)) {
						Date date = DateParser.parseRFC822(element.getTextTrim());
						pageModule.setCreateDate(date);
					}else if (element.getName().equals(PageRSSModule.MODIFIED_DATE)) {
						Date date = DateParser.parseRFC822(element.getTextTrim());
						pageModule.setModifiedDate(date);
					}else if (element.getName().equals(PageRSSModule.VERSION)) {
						pageModule.setVersion(NumberUtils.toInt(element.getTextTrim()));
					}
				}
			}
		}
		return pageModule;
	}
}
