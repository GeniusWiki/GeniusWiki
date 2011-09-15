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
package com.edgenius.wiki.gwt.client;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dapeng.Ni
 * Please NOTE: this class will match the KeyCode(int), rather than the Char. 
 * So, it works in int keyCode = DOM.eventGetKeyCode(event);, but not works in keyPress(char key,....);
 * 
 * Please use following to convert charCode to keyCode: 
 * int key = Character.toUpperCase(charCode);
 * 
 * Very good reference on http://unixpapa.com/js/key.html
 */
//@SuppressWarnings("unchecked")
public class KeyMap {
	public final static Integer HELP = Integer.valueOf(1);
	public final static Integer GOTO_DASHBOARD = Integer.valueOf(2);
	public final static Integer DEBUG = Integer.valueOf(3);
	
	public final static Integer VIEW_CREATE = Integer.valueOf(10); 
	public final static Integer VIEW_EDIT = Integer.valueOf(11);
	public static final Integer VIEW_NEW_COMMENT = Integer.valueOf(12);
	public static final Integer VIEW_NEW_TAG = Integer.valueOf(13);
	public static final Integer VIEW_NEW_ATTACHMENT = Integer.valueOf(14);
	public static final Integer VIEW_TOGGLE_HISTORY = Integer.valueOf(15);
	public static final Integer VIEW_TOGGLE_ATTACHMENT = Integer.valueOf(16);
	public static final Integer VIEW_TOGGLE_COMMENT = Integer.valueOf(17);
	public static final Integer VIEW_CLOSE_PIN_PANEL = Integer.valueOf(18);
	public static final Integer VIEW_TOGGLE_TREE = Integer.valueOf(19);
	
	public static final Integer EDIT_PUBLISH = Integer.valueOf(20);
	public static final Integer EDIT_SAVE = Integer.valueOf(21);
	public static final Integer EDIT_PREVIEW = Integer.valueOf(22);
	public static final Integer EDIT_CANCEL = Integer.valueOf(23);
	public static final Integer EDIT_FORCE_SAVE = Integer.valueOf(24);
	
	public static final Integer PREVIEW_RESUME = Integer.valueOf(30);
	
	private static Map<Integer,KeyCombine> keys = new HashMap<Integer,KeyCombine>();
	static{
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               View model
		keys.put(HELP, new KeyCombine(ClientConstants.KEY_F1));
		keys.put(GOTO_DASHBOARD, new KeyCombine(ClientConstants.d, KeyCombine.MODIFIER_CTRL|KeyCombine.MODIFIER_ALT));
		keys.put(DEBUG, new KeyCombine(ClientConstants.KEY_F12, KeyCombine.MODIFIER_SHIFT|KeyCombine.MODIFIER_ALT));
		
		keys.put(VIEW_CREATE, new KeyCombine(ClientConstants.c));
		keys.put(VIEW_EDIT, new KeyCombine(ClientConstants.e));
		
		keys.put(VIEW_TOGGLE_COMMENT, new KeyCombine(ClientConstants.m));
		keys.put(VIEW_TOGGLE_ATTACHMENT, new KeyCombine(ClientConstants.a));
		keys.put(VIEW_TOGGLE_TREE, new KeyCombine(ClientConstants.l));
		keys.put(VIEW_TOGGLE_HISTORY, new KeyCombine(ClientConstants.h));
		
		keys.put(VIEW_NEW_COMMENT, new KeyCombine(ClientConstants.m,KeyCombine.MODIFIER_SHIFT));
		keys.put(VIEW_NEW_ATTACHMENT, new KeyCombine(ClientConstants.a,KeyCombine.MODIFIER_SHIFT));
		keys.put(VIEW_NEW_TAG, new KeyCombine(ClientConstants.t));
		
		keys.put(VIEW_CLOSE_PIN_PANEL, new KeyCombine(ClientConstants.k));
		
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               EDIT Model
		keys.put(EDIT_PUBLISH, new KeyCombine(ClientConstants.s,KeyCombine.MODIFIER_CTRL|KeyCombine.MODIFIER_ALT));
		keys.put(EDIT_SAVE, new KeyCombine(ClientConstants.s,KeyCombine.MODIFIER_CTRL));
		keys.put(EDIT_PREVIEW, new KeyCombine(ClientConstants.q,KeyCombine.MODIFIER_CTRL|KeyCombine.MODIFIER_ALT));
		keys.put(EDIT_CANCEL, new KeyCombine(ClientConstants.c,KeyCombine.MODIFIER_CTRL|KeyCombine.MODIFIER_ALT));
		keys.put(EDIT_FORCE_SAVE, new KeyCombine(ClientConstants.f,KeyCombine.MODIFIER_CTRL|KeyCombine.MODIFIER_ALT));
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Preview Model
		keys.put(PREVIEW_RESUME, new KeyCombine(ClientConstants.r));
		
		
	}
	public static boolean isKey(Integer function, boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode){

		KeyCombine key = (KeyCombine) keys.get(function);
		if(keyCode == key.keyCode
			&& key.ctrlKey == ctrlKey
			&& key.altKey == altKey
			&& key.shiftKey == shiftKey
			&& key.metaKey == metaKey){
			return true;
		}
		return false;
	}
	

}
