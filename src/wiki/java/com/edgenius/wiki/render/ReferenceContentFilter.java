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


/**
 * A identifier class to tell its sub-class is region text which should be referred by other text.
 * Usage Scenario:
 * Heading title maybe referred by {toc} macro. We want to put rendered text in toc indexed title. In HeadingFilter, we put
 * regionKey(which is because region is already processed before Filter.execute()) into HeadingModel.setTitle() which is as
 * reference rather than real text. After all render complete, we do special handing to replace this HeadingModel.title back to 
 * rendered text.
 * 
 * For example:
 *  "h2. Test {font:size=1}hello{font} and *bold* heading", it should get back someRegionKey in HeadingFilter.execute(), 
 *   
 * @author Dapeng.Ni
 * @see MarkupRenderEngineImpl.processRegions()
 * @see MarkupRenderEngineImpl.processReferences()
 */
public interface ReferenceContentFilter extends RegionContentFilter {

}

