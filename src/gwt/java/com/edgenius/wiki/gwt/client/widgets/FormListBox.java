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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Dapeng.Ni
 */
public class FormListBox extends Composite{
	protected Label msg = new Label();
	protected ListBox box = new ListBox();
	
	public FormListBox(){
		
		FlowPanel parent = new FlowPanel();
		parent.add(msg);
		parent.add(box);
		
		msg.setVisible(false);
		msg.setStyleName(Css.ERRORDIV);
		
		this.initWidget(parent);
	}

	public void setError(String message){
		if(StringUtil.isBlank(message)){
			//clean
			box.removeStyleName(Css.ERROR);
			msg.setText("");
			msg.setVisible(false);
		}else{
			msg.setText(message);
			msg.setVisible(true);
			box.addStyleName(Css.ERROR);
		}
	}
	public Object getEventSource(){
		return box;
	}
	public void setSelectedIndex(int index) {
		box.setSelectedIndex(index);
		
	}

	public int getSelectedIndex() {
		return box.getSelectedIndex();
	}


	public String getSelectedValue() {
		return box.getValue(box.getSelectedIndex());
	}

	public void addItem(String item, String value) {
		box.addItem(item,value);
	}

	/**
	 * @param spaceLinkedBlogForm
	 */
	public void addChangeHandler(ChangeHandler handler) {
		box.addChangeHandler(handler);
		
	}
}
