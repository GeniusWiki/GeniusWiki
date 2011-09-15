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
package com.edgenius.wiki.security.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;

/**
 * @author Dapeng.Ni
 */
public class PatternFactoryFactory  {

    private static final Logger log = LoggerFactory.getLogger(PatternFactoryFactory.class);
    
	private PatternFactory pagePatternFactory;
	private PatternFactory spacePatternFactory;
	private PatternFactory instancePatternFactory;
	private PatternFactory widgetPatternFactory;
	
	/**
	 * @param type
	 * @return
	 */
	public PatternFactory getFactory(RESOURCE_TYPES type) {
		PatternFactory patternFac = null;
		if(type == RESOURCE_TYPES.PAGE){
			return pagePatternFactory;
		}else if(type == RESOURCE_TYPES.SPACE){
			return spacePatternFactory;
		}else if(type == RESOURCE_TYPES.INSTANCE){
			return instancePatternFactory;
		}else if(type == RESOURCE_TYPES.WIDGET){
			return widgetPatternFactory;
		}else{
			log.error("Unsupported security resource type:" + type);
		}
		
		return patternFac;
	}


	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setPagePatternFactory(PatternFactory pageFactory) {
		this.pagePatternFactory = pageFactory;
	}

	public void setSpacePatternFactory(PatternFactory spaceFactory) {
		this.spacePatternFactory = spaceFactory;
	}

	public void setInstancePatternFactory(PatternFactory instanceFactory) {
		this.instancePatternFactory = instanceFactory;
	}
	
	public void setWidgetPatternFactory(PatternFactory widgetFactory) {
		this.widgetPatternFactory = widgetFactory;
	}
	

}
