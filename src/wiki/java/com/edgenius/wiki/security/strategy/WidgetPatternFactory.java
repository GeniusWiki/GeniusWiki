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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.security.Policy;

/**
 * @author Dapeng.Ni
 */
public class WidgetPatternFactory extends AbstractPatternFactory {

	//JDK1.6 @Override
	public List<Policy> getPolicies(Resource resource) {
		
		List<Policy> policies = new ArrayList<Policy>();
		
		//if it is not instance resources, return default list
		if(resource.getType() != RESOURCE_TYPES.WIDGET)
			return policies;

		String widgetUuid = resource.getResource();
		//retrieve all permission of this page, get back policies
		Set<Permission> perms = resource.getPermissions();
		for (Permission permission : perms) {
			Policy policy = getPolicy(permission,widgetUuid);
			if(policy != null)
				policies.add(policy);
		}

		return policies;
	}

}
