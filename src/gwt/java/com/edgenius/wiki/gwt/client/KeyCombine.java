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

/**
 * @author Dapeng.Ni
 */
public class KeyCombine {
	public static final int  MODIFIER_CTRL = 1;
	public static final int  MODIFIER_SHIFT = 1<<1;
	public static final int  MODIFIER_ALT = 1<<2;
	public static final int  MODIFIER_META = 1<<3;

	int keyCode;

	boolean ctrlKey;

	boolean altKey;

	boolean shiftKey;

	boolean metaKey;

	public KeyCombine(int keyCode) {
		this.keyCode = keyCode;
	}

	
	/**
	 * @param ctrlKey
	 * @param altKey
	 * @param shiftKey
	 * @param metaKey
	 * @param keyCode2
	 */
	public KeyCombine(boolean ctrlKey, boolean altKey, boolean shiftKey, boolean metaKey, int keyCode) {
		this.ctrlKey = ctrlKey;
		this.altKey = altKey;
		this.shiftKey = shiftKey;
		this.metaKey = metaKey;
		this.keyCode = keyCode;
	}

	public KeyCombine(int keyCode, int modifier) {
		if((modifier & MODIFIER_CTRL) > 0)
			this.ctrlKey = true;
		if((modifier & MODIFIER_ALT) > 0)
			this.altKey = true;
		if((modifier & MODIFIER_SHIFT) > 0)
			this.shiftKey = true;
		if((modifier & MODIFIER_META) > 0)
			this.metaKey = true;
		
		this.keyCode = keyCode;
		
	}


	public boolean equals(Object obj){
		if(!(obj instanceof KeyCombine))
			return false;
		
		if(((KeyCombine)obj).keyCode == this.keyCode
				&& ((KeyCombine)obj).ctrlKey == this.ctrlKey
				&& ((KeyCombine)obj).altKey == this.altKey
				&& ((KeyCombine)obj).shiftKey == this.shiftKey
				&& ((KeyCombine)obj).metaKey == this.metaKey)
			return true;
		
		return false;
	}
	public int hashCode(){
		return keyCode+ (ctrlKey?1000:0) + (altKey?2000:0) + (shiftKey?3000:0) + (metaKey?4000:0);
	}
}
