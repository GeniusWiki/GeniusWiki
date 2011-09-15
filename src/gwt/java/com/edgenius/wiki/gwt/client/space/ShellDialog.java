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
package com.edgenius.wiki.gwt.client.space;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class ShellDialog extends DialogBox implements ClickHandler, AsyncCallback<String>{
	private Button okBtn = new Button(Msg.consts.ok(), ButtonIconBundle.createImage());
	private Button linkBtn = new Button(Msg.consts.link(), ButtonIconBundle.createImage());
	private Button cancelBtn = new Button(Msg.consts.do_later(), ButtonIconBundle.crossImage());
	
	private FlowPanel mainPanel = new FlowPanel();
	private String spaceUname;
	
	private boolean needCallback = false;
	
	public ShellDialog(String shellThemeBaseURL, String spaceUname){
		//default to intro page
		this(shellThemeBaseURL, spaceUname, null, false);
	}
	
	public ShellDialog(String shellThemeBaseURL, String spaceUname, String shellThemeName, boolean list){
		this.spaceUname = spaceUname;
		this.setText(Msg.consts.link_to_shell());
		this.setIcon(new Image(IconBundle.I.get().shell()));
		
		//shellThemeUrl: http://localhost:8888/theme?instance=1, append spaceUname
		Frame frame = new Frame(shellThemeBaseURL + "&space="+URL.encodeQueryString(spaceUname) 
				+ (list?"&action=list":"") + (shellThemeName!=null?"&theme="+URL.encodeQueryString(shellThemeName):""));
		
		//dirty code: space admin open this GWT dialog with "list" is true, that dialog needs to call back pure Javascript.
		needCallback = list;
		mainPanel.add(frame);
		
		this.setWidget(mainPanel);
		
		
		DOM.setElementAttribute(frame.getElement(), "frameborder", "0");
		frame.setSize("100%", "100%");
		okBtn.addClickHandler(this);
		linkBtn.addClickHandler(this);
		cancelBtn.addClickHandler(this);
		
		getButtonBar().add(cancelBtn);
		getButtonBar().add(linkBtn);
		this.addStyleName(Css.SHELL_DIALOG_BOX);
		mainPanel.setStyleName(Css.DECK);
	}
	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		
		if(sender == cancelBtn){
			this.hidebox();
			if(!needCallback){
				//if from space admin panel, cancel won't trigger delete space shell ajax call.
				SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
				spaceController.updateShellLink(spaceUname, false,  this);
			}
		}else if(sender == linkBtn){
			
			SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
			spaceController.updateShellLink(spaceUname, true,  this);
			linkBtn.setBusy(true);
			
		}else if(sender == okBtn){
			this.hidebox();
		}
			
		
		
	}
	public void onFailure(Throwable err) {
		setDoneCallback(Msg.consts.shell_link_failed());
		
	}
	public void onSuccess(String url) {
		if(url == null){
			setDoneCallback(Msg.consts.shell_link_failed());
		}else{
			setDoneCallback(Msg.params.shell_link_success(url));
		}
		
	}
	/**
	 * @param url
	 */
	private void setDoneCallback(String msg) {
		if(!this.isShowing()){
			//this call back from cancelBtn, i.e., that "link later" button 
			return;
		}
		
		linkBtn.setBusy(false);
		
		if(needCallback){
			//this is call from space admin -> shell JSP page, it won't display confirm panel again.
			themeChanged();
			this.hidebox();
			return;
		}
		
		getButtonBar().clear();
		getButtonBar().add(okBtn);
		
		mainPanel.clear();
		HTML html = new HTML(msg);
		html.setStyleName("message");
		mainPanel.add(html);
		
		this.removeStyleName(Css.SHELL_DIALOG_BOX);
		this.addStyleName(Css.SHELL_DONE_DIALOG_BOX);
		this.center();
	}
	
	//call by below native method
	public static void openChangeThemeDialog(String shellThemeBaseURL, String selectedShellThemeName, String spaceUname){
		ShellDialog dialog = new ShellDialog(shellThemeBaseURL, spaceUname, selectedShellThemeName, true);
		dialog.showbox();
	}
	public native void themeChanged()/*-{
	   $wnd.themeChanged();
	}-*/;
	
	/**
	 * For Space admin Shell setting bind to change theme.
	 */
	public static native void bindJsMethod() /*-{
		$wnd.gwtOpenChangeThemeDialog = function(shellThemeBaseURL, selectedShellThemeName,  spaceUname) {
			@com.edgenius.wiki.gwt.client.space.ShellDialog::openChangeThemeDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(shellThemeBaseURL, selectedShellThemeName, spaceUname);
		};
	}-*/;


}
