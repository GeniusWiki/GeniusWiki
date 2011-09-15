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

import java.util.HashMap;
import java.util.Map;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.wiki.security.MethodValueProvider;

/**
 * @author Dapeng.Ni
 */
public class SecurityDummyMethodValueProvider implements MethodValueProvider {
	
	
	public Map<RESOURCE_TYPES, String> getFromInput(String mi, Object[] args) {
		Map<RESOURCE_TYPES, String> map = new HashMap<RESOURCE_TYPES, String>(3);
		if (SecurityDummy.checkSpaceAdmin.equals(mi)
			|| SecurityDummy.checkSpaceWrite.equals(mi)
			|| SecurityDummy.checkSpaceRead.equals(mi)
			|| SecurityDummy.checkSpaceOffline.equals(mi)) {
			map.put(RESOURCE_TYPES.SPACE, (String) args[0]);
		}else if(SecurityDummy.checkPageRestrict.equals(mi)
			|| SecurityDummy.checkPageRead.equals(mi)){
			map.put(RESOURCE_TYPES.SPACE, (String)args[0]);
			map.put(RESOURCE_TYPES.PAGE, (String)args[1]);
		}else
			//reset map
			map = null;
		
		return map;
	}
	public Map<RESOURCE_TYPES, String> getFromOutput(String mi, Object returnValue) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean isSupport(String clz) {
		return SecurityDummy.class.getName().equals(clz);
	}
}
