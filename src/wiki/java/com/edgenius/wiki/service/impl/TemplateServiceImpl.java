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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.TemplateDAO;
import com.edgenius.wiki.model.Template;
import com.edgenius.wiki.service.TemplateService;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class TemplateServiceImpl implements TemplateService {
	private TemplateDAO templateDAO;
	
	@Transactional(readOnly= true)
	public List<Template> getSpaceTemplates(String spaceUname, User viewer, boolean withAllShared) {
		return  templateDAO.getSpaceTemplates(spaceUname,withAllShared);
	}
	public void remove(Integer templID) {
		templateDAO.remove(templID);
	}
	public Template getTemplate(Integer templID) {
		return templateDAO.get(templID);
	}
	//********************************************************************
	//               set / get methods
	//********************************************************************
	public void saveOrUpdateTemplates(Template templ) {
		templateDAO.saveOrUpdate(templ);
	}

	public void setTemplateDAO(TemplateDAO templateDAO) {
		this.templateDAO = templateDAO;
	}

}
