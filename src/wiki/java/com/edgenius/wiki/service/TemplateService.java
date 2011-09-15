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
package com.edgenius.wiki.service;

import java.util.List;

import com.edgenius.core.model.User;
import com.edgenius.wiki.model.Template;

/**
 * @author Dapeng.Ni
 */
public interface TemplateService {

	String SERVICE_NAME = "templateService";

	/**
	 * If viewer has no permission(space admin or space write), return null.
	 * @param spaceUname
	 * @param viewer
	 * @param withOtherSpaces 
	 * @return
	 */
	List<Template> getSpaceTemplates(String spaceUname, User viewer, boolean withOtherSpaces);
	void saveOrUpdateTemplates(Template templ);
	/**
	 * @param templID
	 */
	void remove(Integer templID);
	/**
	 * @param templID
	 * @return
	 */
	Template getTemplate(Integer templID);

}
