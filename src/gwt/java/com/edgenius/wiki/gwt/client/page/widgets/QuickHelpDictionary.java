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
package com.edgenius.wiki.gwt.client.page.widgets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.edgenius.wiki.gwt.client.KeyCombine;

/**
 * @author Dapeng.Ni
 */
public class QuickHelpDictionary{
	public static final KeyCombine DEFAULT_Q_HELP_KEY = new KeyCombine(-1);
	
	private static Map<KeyCombine,String> quickHelpMap = new HashMap<KeyCombine,String>();
	
	private static Set<String> attachmentList = new HashSet<String>();
	
//	private static Set<String> macroList = new HashSet<String>();
	
	static {
		quickHelpMap.put(DEFAULT_Q_HELP_KEY,
			"00<b>Ctrl-S</b>: save draft; <b>Ctrl-Alt-S</b>: Publish and exit; <b>Ctrl-Alt-Q</b>: Preview; Press <b>F1</b> for more help");
		
		quickHelpMap.put(new KeyCombine((int)'*', KeyCombine.MODIFIER_SHIFT)
			,"01* unordered list | *<b>bold</b>*");
		
		quickHelpMap.put(new KeyCombine((int)'_', KeyCombine.MODIFIER_SHIFT)
			,"02_<span style=\"text-decoration: underline;\">underline</span>_ | __<sub>subscript</sub>__ ");
		
		quickHelpMap.put(new KeyCombine((int)'-')
			,"03-<span style=\"text-decoration: line-through;\">strikethrough</span>- | <b>----</b> to horizontal rule ");
		
		quickHelpMap.put(new KeyCombine((int)'^', KeyCombine.MODIFIER_SHIFT)
			,"04^<sup>superscript</sup>^");
		
		quickHelpMap.put(new KeyCombine((int)'~', KeyCombine.MODIFIER_SHIFT)
			,"05~<span style=\"font-style: italic;\">italic</span>~");
		
		quickHelpMap.put(new KeyCombine((int)'#', KeyCombine.MODIFIER_SHIFT)
			,"06# ordered list | #anchorName# to link anchor");
		
		quickHelpMap.put(new KeyCombine((int)'{', KeyCombine.MODIFIER_SHIFT)
			,"07{macro_name:attrbuteA|attributeB}");

		quickHelpMap.put(new KeyCombine((int)'!', KeyCombine.MODIFIER_SHIFT)
			,"08!image.jpg:right|small! Options: (right,center,left) align image; (big|small) display as thumbnail");
		
		quickHelpMap.put(new KeyCombine((int)'[')
			,"09[view>page title] or [page title] or [view>page title#anchor@external-space-shortname]");
		
		quickHelpMap.put(new KeyCombine((int)'@', KeyCombine.MODIFIER_SHIFT)
			,"10@user login name@");
		
//		quickHelpMap.put(new KeyCombine((int)'h', KeyCombine.MODIFIER_SHIFT),"07heading if following number 1-6, dot then one space at least");
		
	}

	/**
	 * @param filename
	 */
	public static void addAttachment(String filename) {
		attachmentList.add(filename);
	}

	public static String[] getAttachmentList(){
		String[] sort = new String[attachmentList.size()];
		attachmentList.toArray(sort);
		Arrays.sort(sort);
		
		return sort;
	}

	/**
	 * 
	 */
	public static void clearAttachmentList() {
		attachmentList.clear();
	}

	/**
	 * @param keyCode
	 * @param modifier
	 * @return
	 */
	public static String findQuickHelpText(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode) {
		if(keyCode == -1){
			return (String) quickHelpMap.get(DEFAULT_Q_HELP_KEY);
		}
		
		String text = (String) quickHelpMap.get(new KeyCombine(ctrlKey, altKey, shiftKey, metaKey, keyCode));
		if(text == null)
			text = (String) quickHelpMap.get(DEFAULT_Q_HELP_KEY);
		
		return text;
	}
}
