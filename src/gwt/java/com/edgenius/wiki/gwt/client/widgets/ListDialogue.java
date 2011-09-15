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

import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * List user or role dialog, used  for add user/role to Security table list. 
 * @author Dapeng.Ni
 */
public class ListDialogue extends DialogBox {

	public static final int GROUP = 1;
	public static final int USER = 2;
	
	private ListPanel listPanel;
	private Vector<ListDialogueListener> listeners = new Vector<ListDialogueListener>();
	private Image loadingInd = IconBundle.I.loading();
	
	public ListDialogue(String title,final int type){
		listPanel = new ListPanel(type);
		
		VerticalPanel panel = new VerticalPanel();
		
		ButtonBar btnBar = this.getButtonBar();
		Button okButton = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
		okButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				List values = listPanel.getCandidates();
				if(values == null || values.size() == 0){
					if(type == ListDialogue.GROUP){
						Window.alert(Msg.params.no_role_user_select(Msg.consts.group()));
					}else{
						Window.alert(Msg.params.no_role_user_select(Msg.consts.user()));
					}
					return;
				}else{
					for(ListDialogueListener lis :listeners){
						lis.dialogClosed(ListDialogue.this,values);
					}
				}
				hidebox();
			}
		});
		Button cancelButton = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
		cancelButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				ListDialogue.this.hidebox();
			}
		});
		btnBar.add(cancelButton);
		btnBar.add(okButton);
		panel.add(loadingInd);
		panel.add(listPanel);
		
		panel.setSize("100%","100%");
		
		loading(false);
		this.setText(title);
		this.setWidget(panel);
		
	}
	/**
	 * @param list
	 */
	public void fillPanel(List list) {
		listPanel.fillPanel(list);
	}
	public void addListener(ListDialogueListener listener){
		listeners.add(listener);
	}
	/**
	 * Show loading indicator
	 */
	public void loading(boolean loading) {
		loadingInd.setVisible(loading);
	}

}
