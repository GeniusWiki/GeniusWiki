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
package com.edgenius.wiki.gwt.client.home;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.space.ShellDialog;
import com.edgenius.wiki.gwt.client.space.SpaceCreateForm;
import com.edgenius.wiki.gwt.client.space.SpaceUpdateListener;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Image;

/**
 * 
 * @author Dapeng.Ni
 */
public class CreateSpaceDialogue extends DialogBox implements SpaceUpdateListener{
	
	private Vector<ListDialogueListener> listeners = new Vector<ListDialogueListener>();
	
	public CreateSpaceDialogue(){
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
		//add to home page dashboard
		List<PortletModel> portlets = new ArrayList<PortletModel>();
		//convert spaceModel to portletModel
		PortletModel portlet = new PortletModel(); 
		portlet.type = PortletModel.SPACE;
		portlet.row = 0;
		portlet.column = 0;
		portlet.key = model.unixName;
		portlet.title = model.name;
		portlet.description = model.description;
		portlets.add(portlet);
		
		//fire event, tell observer to update portlet
		for(Iterator<ListDialogueListener> iter = listeners.iterator();iter.hasNext();){
			ListDialogueListener lis = iter.next();
			lis.dialogClosed(this,portlets);
		}
		
		//This class only is executed on home Dashboard "Create space" button, so redir can be used here.  
		History.newItem(GwtUtils.getSpacePageToken(model.unixName,null));
		hidebox();
		
		//To open shell intro and theme picker dialogue if it is enabled.
		if(model.isShellEnabled && !model.isShellAutoEnabled){
			ShellDialog shellDlg = new ShellDialog(model.shellThemeBaseURL, model.unixName);
			shellDlg.showbox();
		}
		
	}

	public void logoUpdated(String smallLogoUrl, String largeLogoUrl) {
		// TODO Auto-generated method stub
		
	}

	public void addListDialogueListener(ListDialogueListener listener){
		listeners.add(listener);
	}

}
