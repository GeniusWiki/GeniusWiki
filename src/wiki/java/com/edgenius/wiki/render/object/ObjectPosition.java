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
package com.edgenius.wiki.render.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * @author Dapeng.Ni
 */
public class ObjectPosition  implements Serializable {
	private static final long serialVersionUID = 3824138923207626263L;
	public String uuid;
	//object name - normally, it uses for render error message to tell user which one is broken.
	public String name;
	public String serverHandler;
	
	//this value will pass back to ObjectHandler.handle(RenderContext,value);
	public Map<String,String> values = new HashMap<String, String>();
	
	public ObjectPosition(String name){
		this.name = name;
	}
	/**
	 * For same kind ObjectPositon, return ture but their uuid maybe different
	 */
	public boolean equals(Object obj){
		if(!(obj instanceof ObjectPosition))
			return false;
		
		return StringUtils.equals(serverHandler, ((ObjectPosition)obj).serverHandler);
	}
	
	public int hashCode(){
		return serverHandler != null?serverHandler.hashCode():0;
	}
}
