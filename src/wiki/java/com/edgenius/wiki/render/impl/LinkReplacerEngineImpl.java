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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.edgenius.wiki.render.FilterPipe;
import com.edgenius.wiki.render.LinkReplacerEngine;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RegionComparator;
import com.edgenius.wiki.render.RegionContentFilter;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.filter.ImageFilter;
import com.edgenius.wiki.render.filter.MacroFilter;

/**
 * This Engine will find out Link markup in content, and  
 * @author Dapeng.Ni
 */
public class LinkReplacerEngineImpl implements LinkReplacerEngine {
//	private static final Logger log = LoggerFactory.getLogger(LinkReplacerEngineImpl.class);
	protected FilterPipe filterProvider = new FilterPipeImpl();
	private LinkReplacerFilter replacer;

	public LinkReplacerEngineImpl(FilterPipe filterProvider){
		replacer = filterProvider.getLinkReplacerFilter();
		
		//only these RegionContentFilter before LinkFilter in Filter import order
		((FilterPipeImpl)this.filterProvider).appendRegionFilter((RegionContentFilter) filterProvider.getFilter(MacroFilter.class.getName()));
		((FilterPipeImpl)this.filterProvider).appendRegionFilter((RegionContentFilter) filterProvider.getFilter(ImageFilter.class.getName()));
		((FilterPipeImpl)this.filterProvider).appendRegionFilter(replacer);
		
	}
	
	//JDK1.6 @Override
	public String render(String content, RenderContext context) {

		List<RegionContentFilter> regionFilters = filterProvider.getRegionFilterList();
		
		//don't replace original content by regionContent
		RenderUtil.buildRegions(regionFilters, content, context);
		
		Collection<Region> regions = context.getRegions();
		
		if(regions != null && regions.size() > 0){
			StringBuilder contentBuf = new StringBuilder(content);
			//basic assumptions is link can not embedded another link. For example, [view [link] to>link], the the valid
			//link should be [view [link]. So that, we just update by below logic
			
			for (Region region : regions) {
				if(region.getFilter() instanceof LinkReplacerFilter){
					((RenderContextImpl)context).setCurrentRegion(region);
					region.setBody(replacer.filter(region.getBody(), context));
				}
			}
			Set<Region> sortRegions = new TreeSet<Region>(new RegionComparator());
			sortRegions.addAll(regions);
			for (Region region : sortRegions) {
				//region sort from right to left (and no embedded) so that region.getStart() could point to correct positions.
				if(region.getFilter() instanceof LinkReplacerFilter){
					contentBuf.replace(region.getStart(), region.getEnd(), region.getBody());
				}
			}
			
			return contentBuf.toString();
		}
		
		return content;
	}

}
