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
package com.edgenius.wiki.security;

import java.util.Map;

import com.edgenius.core.SecurityValues.RESOURCE_TYPES;

/**
 * This interface provide the ability to check method returned value according to some conditions. 
 * <code>conditions</code> is transfered from method pattern suffix in <code>StrategyFactory</code> definition. The
 * method suffix is separated by "->" sign. E.g, ClassName.methodName(params) -> condition1, condition2
 * 
 * @author Dapeng.Ni
 */
public interface MethodValueProvider {
	/**
	 * Return this returned value is match in conditions. If this method return true, <code>DBAfterInvocationProvider</code> 
	 * will get this ConfigureAttribute of this Policy.
	 * @param clz
	 * @param mi
	 * @param conditions
	 * @param ret
	 * @return
	 */
	Map<RESOURCE_TYPES,String> getFromOutput(String mi, Object returnValue);
	Map<RESOURCE_TYPES,String> getFromInput(String mi, Object[] args);
	boolean isSupport(String clz);
	
}
