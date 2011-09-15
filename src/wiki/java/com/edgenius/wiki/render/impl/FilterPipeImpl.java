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
package com.edgenius.wiki.render.impl;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.edgenius.core.util.CompareToComparator;
import com.edgenius.wiki.render.Filter;
import com.edgenius.wiki.render.FilterInitializeException;
import com.edgenius.wiki.render.FilterPipe;
import com.edgenius.wiki.render.MacroManager;
import com.edgenius.wiki.render.PatternFilter;
import com.edgenius.wiki.render.RegionContentFilter;
import com.edgenius.wiki.render.filter.LinkFilter;
import com.edgenius.wiki.render.filter.MacroFilter;

/**
 * @author Dapeng.Ni
 */
public class FilterPipeImpl implements FilterPipe, InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(FilterPipeImpl.class);

	private List<FilterMetadata> filterMetaList = new ArrayList<FilterMetadata>();

	private List<Filter> filterList = new ArrayList<Filter>();
	private List<RegionContentFilter> regionFilterList = new ArrayList<RegionContentFilter>();

	private Map<String,Filter> filterNameMap = new HashMap<String, Filter>();
	
	// default value, could modify by set() method
	private String filterResource = "META-INF/services/" + Filter.class.getName();
	private String filterResourcePattern = "META-INF/services/" + Filter.class.getName()+".properties";
	
	private LinkReplacerFilter linkReplacerFilter = new LinkReplacerFilter();
	private MacroManager macroManager;
	
	public void load() throws FilterInitializeException {
		ClassLoader classLoader = Filter.class.getClassLoader();
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Load filter configure XML file
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			XMLReader reader = parser.getXMLReader();
			
			reader.setContentHandler(this.new FilterMetaParaser());
			reader.parse(new InputSource(classLoader.getResourceAsStream(filterResource)));
		} catch (Exception e) {
			log.error("Unable load filter configuare file " + filterResource,e);
			throw new FilterInitializeException(e);
		}
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Load filter Pattern resource bundle file
		Properties patternResource = new Properties();
		try {
			patternResource.load(classLoader.getResourceAsStream(filterResourcePattern));
		} catch (Exception e) {
			log.error("Unable load PatternFilter pattern file " + filterResourcePattern,e);
			throw new FilterInitializeException(e);
		}
		
		Map<Integer, Filter> sortSet = new TreeMap<Integer, Filter>(new CompareToComparator());
		Map<Integer, RegionContentFilter> regionSortSet = new TreeMap<Integer, RegionContentFilter>(new CompareToComparator());
		for (FilterMetadata filterMeta : filterMetaList) {
			try {
				Object obj = classLoader.loadClass(filterMeta.getClassName().trim()).newInstance();
				if (obj instanceof Filter) {
					if(obj instanceof MacroFilter)
						((MacroFilter)obj).setMacroMgr(macroManager);
					
					//initial filter, if it is patternFilter, then do further initial
					if(obj instanceof PatternFilter){
						//!!! this markupPring must happen before setRegex() which may need getMarkupPrint() to build regex
						String markupPrintVal = patternResource.getProperty(((PatternFilter)obj).getPatternKey() + PatternFilter.SUFFIX_MARKUP_PRINT);
						if(!StringUtils.isBlank(markupPrintVal)){
							((PatternFilter)obj).setMarkupPrint(markupPrintVal);
						}
						String matchVal = patternResource.getProperty(((PatternFilter)obj).getPatternKey() + PatternFilter.SUFFIX_MATCH);
						if(!StringUtils.isBlank(matchVal)){
							//special for link replacer
							if(obj instanceof LinkFilter){
								linkReplacerFilter.setRegex(matchVal);
							}
							((PatternFilter)obj).setRegex(matchVal);
						}
						String printVal = patternResource.getProperty(((PatternFilter)obj).getPatternKey() + PatternFilter.SUFFIX_PRINT);
						if(!StringUtils.isBlank(printVal)){
							((PatternFilter)obj).setReplacement(printVal);
						}
						String htmlIDVal = patternResource.getProperty(((PatternFilter)obj).getPatternKey() + PatternFilter.SUFFIX_HTML_IDENTIFIER);
						if(!StringUtils.isBlank(htmlIDVal)){
							((PatternFilter)obj).setHTMLIdentifier(htmlIDVal);
						}

					}
					filterNameMap.put(obj.getClass().getName(), (Filter) obj);
					
					((Filter)obj).init();
					
					//new line filter always be last, but need special handle. see MarkupRenderEngineImpl.render()
					sortSet.put(filterMeta.getOrder(), (Filter) obj);
					if(obj instanceof RegionContentFilter){
						regionSortSet.put(filterMeta.getOrder(), (RegionContentFilter) obj);
					}
						
					log.info("Filter loaded into FilterPipe:" + obj.getClass().getName());
				}else{
					log.warn("Class " + obj.getClass().getName() + " does not implement Filter interface. " +
							"It cannot be loaded into FilterPipe.");
				}
			} catch (InstantiationException e) {
				log.error("Filter failed on Instantiation " + filterMeta, e);
			} catch (IllegalAccessException e) {
				log.error("Filter failed on IllegalAccessException " + filterMeta, e);
			} catch (ClassNotFoundException e) {
				log.error("Filter ClassNotFoundException failed " + filterMeta, e);
			}
		}
		
		linkReplacerFilter.init();
		
		filterList.clear();
		filterList.addAll(sortSet.values());
		regionFilterList.clear();
		regionFilterList.addAll(regionSortSet.values());
	}

	//JDK1.6 @Override
	public List<Filter> getFilterList() {
		return filterList;
	}
	//JDK1.6 @Override
	public List<RegionContentFilter>  getRegionFilterList() {
		return regionFilterList;
	}
	//JDK1.6 @Override
	public Filter getFilter(String filterClzName) {
		return filterNameMap.get(filterClzName);
	}

	public void setFilterResource(String filterResource) {
		this.filterResource = filterResource;
	}
	
	/**
	 * Be careful, this method does not take care order of filter, just append!
	 * @param filter
	 */
	public void appendFilter(Filter filter) {
		filterList.add(filter);
	}
	/**
	 * Be careful, this method does not take care order of filter, just append!
	 * @param filter
	 */
	public void appendRegionFilter(RegionContentFilter filter) {
		regionFilterList.add(filter);
	}
	
	//JDK1.6 @Override
	public LinkReplacerFilter getLinkReplacerFilter() {
		return linkReplacerFilter;
	}
	public void setMacroManager(MacroManager macroManager) {
		this.macroManager = macroManager;
	}


	//********************************************************************
	//               private class
	//********************************************************************

	private class FilterMetaParaser extends DefaultHandler {
		public static final String FILTERS = "filters";

		public static final String FILTER = "filter";

		public static final String CLASS_NAME = "classname";

		public static final String ORDER = "order";

		private CharArrayWriter characters;

		private FilterMetadata meta;

		public void characters(char[] ch, int start, int length) throws SAXException {
			characters.write(ch, start, length);

		}

		public void endDocument() throws SAXException {
			characters.close();
			characters = null;
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
			if (FILTER.equalsIgnoreCase(name)) {
				filterMetaList.add(meta);
			} else if (CLASS_NAME.equalsIgnoreCase(name)) {
				meta.setClassName(characters.toString());
			} else if (ORDER.equalsIgnoreCase(name)) {
				meta.setOrder(NumberUtils.toInt(characters.toString(), 1));
			}

		}

		public void startDocument() throws SAXException {
			filterMetaList.clear();
			characters = new CharArrayWriter();
		}

		public void startElement(String uri, String localName, String name, Attributes atts) throws SAXException {
			if (FILTER.equalsIgnoreCase(name)) {
				meta = new FilterMetadata();
			}
			characters.reset();
		}

	}


	public void afterPropertiesSet() throws Exception {
		load();
	}
}
