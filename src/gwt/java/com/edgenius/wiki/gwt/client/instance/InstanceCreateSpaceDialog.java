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
package com.edgenius.wiki.gwt.client.instance;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.space.SpaceCreateForm;
import com.edgenius.wiki.gwt.client.space.SpaceUpdateListener;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class InstanceCreateSpaceDialog  extends DialogBox implements SpaceUpdateListener{
	
	public InstanceCreateSpaceDialog(){
		this.setText(Msg.consts.create_new_space());
		this.setIcon(new Image(IconBundle.I.get().wand()));
		//create space
		SpaceCreateForm panel = new SpaceCreateForm(this);
		panel.addListener(this);
		addStyleName(Css.CREATE_SPACE_DIALOG_BOX);
		this.setWidget(panel);
	}

	public void spaceUpdateCancelled() {
		hidebox();
	}
	public void spaceUpdated(SpaceModel model) {
		hidebox();
		spaceCreated(model.unixName);
	}
	
	public native void spaceCreated(String unixName)/*-{
	   $wnd.spaceCreated(unixName);
	}-*/;
	
	public void logoUpdated(String smallLogoUrl, String largeLogoUrl) {
		// TODO Auto-generated method stub
		
	}
}
