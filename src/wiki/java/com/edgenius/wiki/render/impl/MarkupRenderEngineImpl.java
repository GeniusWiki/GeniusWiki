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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.render.Filter;
import com.edgenius.wiki.render.FilterPipe;
import com.edgenius.wiki.render.MarkupUtil;
import com.edgenius.wiki.render.ReferenceContentFilter;
import com.edgenius.wiki.render.Region;
import com.edgenius.wiki.render.RegionContentFilter;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.RenderEngine;
import com.edgenius.wiki.render.RenderUtil;
import com.edgenius.wiki.render.SubRegionFilter;
import com.edgenius.wiki.render.filter.MacroFilter;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.ObjectHandlerPool;
import com.edgenius.wiki.render.object.ObjectPosition;
import com.edgenius.wiki.render.object.RenderHandlerException;

/**
 * This render engine input Markup content and return HTML content
 * @author Dapeng.Ni
 */
public class MarkupRenderEngineImpl  implements RenderEngine{
	private static final Logger log = LoggerFactory.getLogger(MarkupRenderEngineImpl.class);
	protected FilterPipe filterProvider;
	private ObjectHandlerPool  objectHandlerPool;
	
	public MarkupRenderEngineImpl(FilterPipe filterProvider){
		this.filterProvider = filterProvider;
	}

	public List<RenderPiece> render(String text, RenderContext context) {
		long codeStart = System.currentTimeMillis();
		
		MacroFilter macroFilter = (MacroFilter) filterProvider.getFilter(MacroFilter.class.getName());		
		text = macroFilter.initialGroup(text);
		
		List<RegionContentFilter> regionFilters = filterProvider.getRegionFilterList();
		text = RenderUtil.buildRegions(regionFilters, text, context);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// text process by filters except regions filters 
		List<Filter> filters = filterProvider.getFilterList();
		for (Filter filter : filters) {
			text = filter.filter(text, context);
		}
		
		text = processRegions(text, context, filters);

		//replace to new line
		List<RenderPiece> pieces = processObjects(context, text);
		
		pieces = processReferences(context,pieces);
		
		log.info(new StringBuilder("Render markup content takes: ").append(System.currentTimeMillis() - codeStart).append("ms").toString());
		
		return pieces;
	}



	/**
	 * @param text
	 * @param context
	 * @param filters
	 * @return
	 */
	private String processRegions(String text, RenderContext context, List<Filter> filters) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// process by all regions filter
		
		Collection<Region> regions = context.getRegions();
		if(regions != null && regions.size() > 0){
			StringBuilder buffer = new StringBuilder(text);
			for (Region region : regions) {
				String body =  region.getBody();
				((RenderContextImpl)context).setCurrentRegion(region);
				
				if(region.isImmutable()){
					//in some case, filter maybe null, then just keep body unchanged, for example "view" in [view>link] which has no filter 
					Filter currFilter = ((Filter)region.getFilter());
					if(currFilter != null){
						//still need do markup entity, eg, {pre} my \{pre} is only one valid {pre}, the second {pre} need to be replace to entity
						body = MarkupUtil.escapeMarkupToEntity(body);
						
						// as this immutable filter is processed by escape first, So, please ensure 
						// context.getCurrentRegion().getContent() to get back immutable content, rather than directly use matcher.group(xx)
						body = currFilter.filter(body, context);
					} 
				}else{
					//TODO: here need do performance improvement, basic idea is merge all region body into one piece text with some separator
					//then looping filters once for all regions.
					for (Filter filter : filters) {
						body = filter.filter(body, context);
					}
				}
				
				region.setBody(body);
			}
			

			//replace back
			int FAILURE_TOLERANCE_COUNT = 500;
			int seq, count = 0;
			boolean referRegion;
			List<String> processedRegions = new ArrayList<String>(regions.size());
			do{
				count++;
				if(count > FAILURE_TOLERANCE_COUNT){
					AuditLogger.error("Unable to replace back region over 500 times looping. Please check:"+ text);
					break;
				}
				for (Region region : regions) {
					String regionKey = region.getRegionKey(context);
					if(processedRegions.contains(regionKey)){
						continue;
					}
					StringBuilder body = new StringBuilder(region.getBody());
					if(body.indexOf(context.getRegionPrimaryKey()) != -1){
						//this region embeds some other region, handle it later
						continue;
					}
					processedRegions.add(regionKey);
					
					referRegion = context.hasReferrer() && (region.getFilter() instanceof ReferenceContentFilter);
					
					if((seq = buffer.indexOf(regionKey) )!= -1){
						//cannot use replaceAll() as it is regex replace, it not work if body has replacement string such as "\" etc.
						//this need above code ensure region must be unique for whole text
						buffer.delete(seq, seq + regionKey.length());
						if(referRegion){
							resetReferenceBody(context, regionKey, body);
						}
						buffer.insert(seq,body);
					}else{
						//region embedded - a region is inside another region 
						for (Region reg : context.getRegions()) {
							if(reg.getBody().indexOf(regionKey) != -1){
								if(referRegion){
									resetReferenceBody(context, regionKey, body);
								}
								reg.setBody(reg.getBody().replace(regionKey, body));
								break;
							}
						}
					}
					
				}
			}while(processedRegions.size() != regions.size());
			
			//sub region
			for (Region region : regions) {
				if(region.getSubRegion() == null)
					continue;
				//comment here is for performance reason -- is it necessary?
				//just valid sub region is not same with input text, to avoid infinite looping
				//if(text.equals(region.getBody()))
				// continue;
				Region subReg = region.getSubRegion();
				RenderContext subContext = context.subContext(subReg.getContent());
				List<RenderPiece> subPieces = render(subReg.getContent(),subContext);
				 
				//use upper level context, subContext is only for the sub region render....
				((SubRegionFilter)subReg.getFilter()).subRegion(subReg, subPieces, context);
			}
			return buffer.toString();
		}else{
			return text;
		}
	}

	
	

	/**
	 * @param page
	 * @param wikiText
	 * @param renderContext
	 * @param renderText
	 * @return
	 */
	private List<RenderPiece> processObjects(RenderContext renderContext, String renderText) {
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//page object handle:
		
		List<ObjectPosition> objList = renderContext.getObjectList();
		List<ObjectHandler> keyList = new ArrayList<ObjectHandler>();
		
		//Objects may disordered, so here use sorted map to sort object by start position in render text
		Map<Integer, List<RenderPiece>> objectMap = new TreeMap<Integer, List<RenderPiece>>(new CompareToComparator());
		for (ObjectPosition pos : objList) {
			//put index is here, so that if any exception happens following, the object UUID in render text can be replace 
			//by error message.
			int start  = renderText.indexOf(pos.uuid);
			if(start == -1){
				if(pos.uuid.endsWith("\n")){
					//scenario: table filter, which will trim cell content - if cell like |{toc}|,  the {toc} is object which has multiple line key
					//but here it will be trimmed last newline in key, so here try to use unique part as key to find the object.
					//the reason, we don't put newline inside middle of key string is, it is totally break regex parse, for example,
					//|aSx1y\n|TE, note here we assume insert new line inside key, i.e., aSx1y\nTE, but when parse table cell, this 
					//key is treat as two part ie. <td>aSx1y</td></tr></table><br>TE... It is too bad.
					log.info("Multiple line object key is possible trimmed last newline {}",pos.uuid);
					start = renderText.indexOf(pos.uuid.substring(0,pos.uuid.length()-1));
					if(start != -1){
						//here code is sucks: Just multiple -1 to tell this object key is trimmed newline one - tells its length is minus 1 than normal.
						//so here just play as a flag stuff.
						start *= -1;
					}
				}
				if(start == -1){
					AuditLogger.warn("The object KEY is eaten by some other filter. Please validate your input; " 
							+ (renderContext.getPageTitle() != null?renderContext.getPageTitle() :""));
					continue;
				}
			}
			try {
				Object hObj = objectHandlerPool.borrowObject(pos);
				if(!(hObj instanceof ObjectHandler)){
					log.error("Failed get a object handler during rendering " + pos);
					continue;
				}
				ObjectHandler handler = (ObjectHandler) hObj;
				objectHandlerPool.returnObject(pos,handler);
				
				if(!keyList.contains(handler)){
					keyList.add(handler);
					//call init method of Handler
					handler.renderStart(renderContext.getPage());
				}
				
				//parse out values
				List<RenderPiece> objPieces;
				try{
					objPieces = handler.handle(renderContext,pos.values);
				}catch (RenderHandlerException e) {
					objPieces = new ArrayList<RenderPiece>();
					objPieces.add(RenderUtil.renderError(e.getMessage(), pos.name));
				}
				objectMap.put(start, objPieces);
				
			} catch (Exception e) {
				//error handle, but it still put message to into objectMap so that the UUID can be replaced by following handling
				List<RenderPiece> objPieces = new ArrayList<RenderPiece>();
				objPieces.add(RenderUtil.renderError("Unable to handle this markup, please correct it.", 
						pos.name != null?pos.name:""));
				objectMap.put(start, objPieces);
				log.error("Object handler " + pos.serverHandler + " failed render. " , e);
			}
		}
		for (ObjectHandler handler : keyList) {
			handler.renderEnd();
		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//finish all Object handle, then split render string by object and string
		int start = 0,objStart;
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
	
		//object must sort by its start position in render text so that the render text can be split correctly
		Set<Entry<Integer, List<RenderPiece>>> entries = objectMap.entrySet();
		
		String strText;
		boolean trimmedKey;
		for (Entry<Integer, List<RenderPiece>> entry : entries) {
			objStart = entry.getKey().intValue();
			trimmedKey = false;
			if(objStart < 0){
				objStart *= -1;
				trimmedKey = true;
			}
			strText = renderText.substring(start,objStart);
			if(strText.length() != 0){
				pieces.add(new TextModel(strText));
			}
			if(entry.getValue() != null){
				//maybe return null, such as PageAttribute Macro
				for (RenderPiece piece: entry.getValue()) {
					pieces.add(piece);
				}
			}
			start = objStart + (trimmedKey?WikiConstants.UUID_KEY_SIZE-1:WikiConstants.UUID_KEY_SIZE);
		}
		
		//append tailed part
		strText = renderText.substring(start);
		if(strText.length() != 0)
			pieces.add(new TextModel(strText));
		

		return pieces;
	}
	
	/**
	 * @param context
	 * @param text
	 */
	private List<RenderPiece> processReferences(RenderContext context, List<RenderPiece> pieces) {
		Map<String, String> referenceKeys = ((RenderContextImpl)context).getReferenceTextKeys();
		if(referenceKeys == null || referenceKeys.size() == 0)
			return pieces;
		
		Map<String, List<RenderPiece>> reference = new HashMap<String, List<RenderPiece>>();
		//save  refer key which already processed, aka, the paired region border is found.
		Set<String> hasPair = new HashSet<String>();
		//save only found these refer regions which only have one paired and expects its end pair border key. 
		Set<String> expectPair = new HashSet<String>();
		//if region key is inside of this renderPiece(TextModel), then this renderPiece should be filled in to reference text body.
		//skip these keys when try to add RenderPiece to reference text body.
		Set<String> hasAlreadyFillin = new HashSet<String>();
		StringBuilder text;
		for (RenderPiece renderPiece : pieces) {
			
			hasAlreadyFillin.clear();
			// LinkModel and MacroModel don't expect has any ReferenceRegion border
			if(renderPiece instanceof TextModel){
				for(Entry<String,String> entry:referenceKeys.entrySet()){
					String referKey = entry.getKey();
					
					//this key is already processed (paired reference border found, then means processed)
					if(hasPair.contains(referKey))
						continue;
					
					String surroundingKey = entry.getValue();
					text = new StringBuilder(((TextModel) renderPiece).getText());
					
					int pos = 0, start = -1, end = -1;
					List<RenderPiece> referPieces = reference.get(referKey);
					do{
						if((pos = text.indexOf(surroundingKey, pos)) != -1){
							//remove surroundingKey
							text.delete(pos, pos+ surroundingKey.length());
							if(referPieces == null){
								//start of reference text body
								referPieces = new ArrayList<RenderPiece>();
								reference.put(referKey, referPieces);
								expectPair.add(referKey);
								start = pos;
							}else{
								//end of reference text body
								hasPair.add(referKey);
								expectPair.remove(referKey);
								end = pos;
								break;
							}
						}
					}while(pos != -1);
					//reset text which is already remove surrounding key
					((TextModel) renderPiece).setText(text.toString());
					
					if(start != -1 || end != -1){
						if(start != -1 && end != -1){
							//this textModel include pair
							hasAlreadyFillin.add(referKey);
							referPieces.add(new TextModel(text.substring(start, end)));
						}else if(start != -1){
							//Now, it only has pair start, add the left part of text after start
							hasAlreadyFillin.add(referKey);
							referPieces.add(new TextModel(text.substring(start)));
						}else{
							//don't need set hasAlreadyFillin.add(referKey), as at this point, expectPair.remove() is already remove this key.
							//Now, it is pair end, add text before start
							referPieces.add(new TextModel(text.substring(0, end)));
						}
					}
				}
			}
			
			//if the renderPiece is inside reference region, then append them to reference text body
			for (String key : expectPair) {
				if(hasAlreadyFillin.contains(key))
					continue;
				reference.get(key).add(renderPiece);
			}
			
			//if all reference text is found, break out
			if(hasPair.size() == referenceKeys.size())
				break;
		}
		
		//remove these don't have end paired from referencePieces
		if(expectPair.size() != 0){
			AuditLogger.error("Some reference can't find its paired border on page: " 
					+ (context.getPageTitle() != null?context.getPageTitle():"Unknown"));
			//remove these without end paired key, they are invalid
			for (String key : expectPair) {
				reference.remove(key);
			}
		}
		if(reference.size() < referenceKeys.size()){
			AuditLogger.error("Some reference text can't be found: " 
					+ (context.getPageTitle() != null?context.getPageTitle():"Unknown"));
		}
		
		//OK, now try to find referKey in text model
		
		LinkModel link;
		for (ListIterator<RenderPiece> iter = pieces.listIterator();iter.hasNext();) {
			RenderPiece renderPiece = iter.next();
			
			link = null;
			if(renderPiece instanceof TextModel){
				text = new StringBuilder(((TextModel) renderPiece).getText());
			}else if(renderPiece instanceof LinkModel){
				link = (LinkModel) renderPiece;
				text = new StringBuilder(link.getView());
			}else{
				//Is possible  MacroModel include a reference of key? So far, I can't see this case happen
				continue;
			}
			for(Entry<String,List<RenderPiece>> entry:reference.entrySet()){
				String referKey = entry.getKey();
				int start = 0;
				do{
					if((start = text.indexOf(referKey, start)) != -1){
						//remove referKey from text
						text.delete(start, start+ referKey.length());
						
						if(link != null){
							//serialise referencePieces and put it into Link.view()
							String pieceStr = RenderUtil.serialPieces(entry.getValue());
							text.insert(start, pieceStr);
							start += pieceStr.length();
						}else{
							//split this key and put this key part with List<RenderPiece>
							iter.remove();
							//add this text before index part
							if(start != 0)
								iter.add(new TextModel(text.substring(0, start)));
							for (RenderPiece p : entry.getValue()) {
								iter.add(p);
							}
							//add this text after index part
							if(start < text.length())
								iter.add(new TextModel(text.substring(start)));
						}
							
					}
				}while(start != -1);
			}
			if(link != null){
				link.setView(text.toString());
				//reset tag string as well
				link.setLinkTagStr(context.buildURL(link));
			}
		}
		return pieces;
	}
	

	private void resetReferenceBody(RenderContext context, String regionKey, StringBuilder body) {
		String surroundingKey = context.createUniqueKey(false);
		((RenderContextImpl)context).getReferenceTextKeys().put(regionKey, surroundingKey);
		body.insert(0, surroundingKey).append(surroundingKey);
	}
	
	public void setObjectHandlerPool(ObjectHandlerPool objectHandlerPool) {
		this.objectHandlerPool = objectHandlerPool;
	}

}
