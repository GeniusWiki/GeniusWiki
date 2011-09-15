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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.io.ModuleGenerator;
import com.sun.syndication.io.impl.DateParser;

/**
 * @author Dapeng.Ni
 */
public class PageRSSGenerator implements ModuleGenerator {

	private static final Set<Namespace> NAMESPACES;
	static {
		Set<Namespace> namespaces = new HashSet<Namespace>();
		namespaces.add(PageRSSModule.NAMESPACE);
		NAMESPACES = Collections.unmodifiableSet(namespaces);
	}

	public String getNamespaceUri() {
		return PageRSSModule.URI;
	}

	public Set<Namespace> getNamespaces() {
		return NAMESPACES;
	}

	public void generate(Module module, Element element) {
		PageRSSModule pageModule = (PageRSSModule) module;
		Element myElement = null;
		
		if (pageModule.getSpaceUname() != null) {
			myElement = new Element(PageRSSModule.SPACEUNAME, PageRSSModule.NAMESPACE);
			myElement.setText(pageModule.getSpaceUname());
			element.addContent(myElement);
		} 
		if (pageModule.getPageUuid() != null) {
			myElement = new Element(PageRSSModule.PAGE_UUID, PageRSSModule.NAMESPACE);
			myElement.setText(pageModule.getPageUuid());
			element.addContent(myElement);
		} 
		if (pageModule.getCreator() != null) {
			myElement = new Element(PageRSSModule.CREATOR, PageRSSModule.NAMESPACE);
			myElement.setText(pageModule.getCreator());
			element.addContent(myElement);
		} 
		if (pageModule.getPageUuid() != null) {
			myElement = new Element(PageRSSModule.MODIFIER, PageRSSModule.NAMESPACE);
			myElement.setText(pageModule.getModifier());
			element.addContent(myElement);
		} 
		if (pageModule.getPageUuid() != null) {
			myElement = new Element(PageRSSModule.CREATE_DATE, PageRSSModule.NAMESPACE);
			myElement.setText(DateParser.formatRFC822(pageModule.getCreateDate()));
			element.addContent(myElement);
		} 
		if (pageModule.getPageUuid() != null) {
			myElement = new Element(PageRSSModule.MODIFIED_DATE, PageRSSModule.NAMESPACE);
			myElement.setText(DateParser.formatRFC822(pageModule.getModifiedDate()));
			element.addContent(myElement);
		} 
		if (pageModule.getPageUuid() != null) {
			myElement = new Element(PageRSSModule.VERSION, PageRSSModule.NAMESPACE);
			myElement.setText(Integer.valueOf(pageModule.getVersion()).toString());
			element.addContent(myElement);
		}
			
	}
}
