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
package com.edgenius.wiki.integration.rest.model.mapper;

import com.edgenius.wiki.integration.rest.model.SpaceBean;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
public class SpaceMapper {
	

	public static SpaceBean spaceToBean(Space space){
		SpaceBean bean = new SpaceBean();
		
		bean.setUnixName(space.getUnixName());
		bean.setName(space.getName());
		bean.setDescription(space.getDescription());
		
		bean.setHomepageUuid(space.getHomepage() == null?null:space.getHomepage().getPageUuid());
		
		//only large logo
		bean.setLogo(space.getLogoLarge());
		AbstractMapper.mapper(bean, space);
		
		return bean;
	}
}
