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
package com.edgenius.wiki.gwt.client.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;


/**
 * @author Dapeng.Ni
 */
public class MessagePieceModel extends GeneralModel {

	public static final int TYPE_TEXT = 0;
	public static final int TYPE_ACTION = 1;
	
	public static final String ACTION_ID="id";
	public static final String ACTION_TITLE="title";
	//when do any action (send request to server) if Javascript confirm window is need.
	public static final String ACTION_CONFIRM_MSG="confirmmessage";
	
	public Integer messageUid;
	public int type;
	public String text;
	
	public HashMap<String,String> actionsParams;

	public String toString(){
		String ret =  "Type is " + type + "; Text '" + text + "'";
		if(actionsParams != null){
			ret +=" Params is '";
			for(Iterator<Entry<String,String>> iter = actionsParams.entrySet().iterator();iter.hasNext();){
				Entry<String,String> entry = iter.next();
				ret += "  " + entry.getKey() + " = " + entry.getValue(); 
			}
			ret +="'";
		}
		return ret;
	}
}
