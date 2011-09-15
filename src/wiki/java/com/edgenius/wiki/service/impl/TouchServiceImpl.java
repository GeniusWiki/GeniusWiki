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
package com.edgenius.wiki.service.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.TouchService;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class TouchServiceImpl implements TouchService {
	private static final Logger log = LoggerFactory.getLogger(TouchServiceImpl.class);

	private PageDAO pageDAO;
	private SpaceDAO spaceDAO;

	//JDK1.6 @Override
	public void touchPage(String pageUuid) {
		Page page = pageDAO.getByUuid(pageUuid);
		
		if(page == null){
			log.info("Unable get page to touch it by uuid "+ pageUuid + ". This may cause by new page is creating.");
			return;
		}
		
		page.setTouchedDate(new Date());
		pageDAO.saveOrUpdate(page);
	}
	//JDK1.6 @Override
	public void touchSpace(String spaceUname) {
		Space space = spaceDAO.getByUname(spaceUname);
		
		if(space == null){
			log.info("Unable get space to touch it by spaceUname "+ spaceUname+ ". This may cause by new space is creating.");
			return;
		}
		
		space.setTouchedDate(new Date());
		spaceDAO.saveOrUpdate(space);
	}
	
	//********************************************************************
	//               set / get method
	//********************************************************************
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}
	public void setSpaceDAO(SpaceDAO spaceDAO) {
		this.spaceDAO = spaceDAO;
	}
}
