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
package com.edgenius.wiki.render;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
public class Region implements Cloneable{
	public static final String REGION_SUFFIX = "K";
	
	private static final Logger log = LoggerFactory.getLogger(Region.class);
	private String key;
	private int keyIndex = -1;
	
	private RegionContentFilter filter;
	
	//body start: include markup size, for example {pre}abc{pre}, this start/end is from 0 to 14
	private int start;
	//body end offset
	private int end;
	
	//this is only content start and end
	private int contentStart;
	//offset
	private int contentEnd;
	
	private boolean immutable;

	private String content;
	private String body;
	
	private Region subRegion;
	//********************************************************************
	//               function method
	//********************************************************************
	public Region clone(){
		Region cRegion = null;
		try {
			cRegion = (Region) super.clone();
		
		} catch (CloneNotSupportedException e) {
			log.error("Clone "+this.getClass().getName()+" object failed " , e);
		}
		return cRegion;
	}
	public Region(int start, int end) {
		this(null,false,start,end,0,0);
	}

	public Region(RegionContentFilter filter , boolean immutable, int start,int end, int contentStart, int contentEnd) {
		this.filter = filter;
		this.immutable = immutable;
		this.start = start;
		this.end = end;
		this.contentStart = contentStart; 
		this.contentEnd= contentEnd; 
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof Region))
			return false;
		return ((Region)obj).start == this.start 
		&& ((Region)obj).end == this.end;
		
	}

	
	public int hashCode(){
		return (this.filter!= null? this.filter.getClass().getName().hashCode():0) + start + end;
	}

	/**
	 * Detect current region is inside given scope.
	 * @param start2
	 * @param end2
	 * @return
	 */
	public boolean isInside(int start2, int end2) {
		
		return start2<start && end2>end;
	}

	public boolean isInside(Region region) {
		
		return isInside(region.start,region.end);
	}
	/**
	 * If region is contained or equals, return true.
	 */
	public boolean isContain(Region region){
		return isContain(region.start,region.end);
	}
	public boolean isContain(int start2, int end2) {
		return start<=start2 && end >= end2;
	}

	public String toString(){
		return "["+start+"-"+end+"]";
	}
	/**
	 * if given region has any scope overlap only( NOT inside or contain) 
	 * This method won't pre-check: regions is null or region.end < region.start case. Please ensure both outside
	 * this method. 
	 * @param region
	 * @return
	 */
	public boolean isOverlap(Region region) {
		return isOverlap(region.start,region.end);
	}


	/**
	 * If use A, B as two region border, this check return true if
	 * <pre>
	 * A B A B
	 * B A B A
	 * </pre>
	 * 
	 * @param start2
	 * @param end2
	 * @return
	 */
	public boolean isOverlap(int start2, int end2) {
		//please note: end is offset, start is index!
		if((end2 > this.end && start2 < this.end && start2 > this.start)
			||(start2 < this.start && end2 > start && end2 < this.end))
			return true;
			
		return false;
	}
	/**
	 * Given region must be overlap with current one (this must ensure outside this method). For example:
	 * <pre>
	 * A B A B
	 * </pre>
	 * A is on left side of B region. A is this , B is input parameter.
	 * 
	 * @param block
	 * @return
	 */
	public boolean isLeft(Region block) {
		return (this.start < block.start);
	}
	public void offset(int offset) {
		if(offset == 0)
			return;
		
		start += offset;
		end += offset;
		
	}
	/**
	 * @param context 
	 * @return
	 */
	public String getRegionKey(RenderContext context) {
		return new StringBuilder(context.getRegionPrimaryKey()).append(key).append(REGION_SUFFIX).toString();
	}
	//********************************************************************
	//               set /get
	//********************************************************************
	public int getContentStart() {
		return contentStart;
	}

	public int getContentEnd() {
		return contentEnd;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public int getStart() {
		return start;
	}

	/**
	 * @return the keyIndex
	 */
	public int getKeyIndex() {
		return keyIndex;
	}
	/**
	 * @param keyIndex the keyIndex to set
	 */
	public void setKeyIndex(int keyIndex) {
		this.keyIndex = keyIndex;
	}
	public int getEnd() {
		return end;
	}

	public boolean isImmutable() {
		return immutable;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public RegionContentFilter getFilter() {
		return filter;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Region getSubRegion() {
		return subRegion;
	}
	public void setSubRegion(Region subRegion) {
		this.subRegion = subRegion;
	}
	
}
