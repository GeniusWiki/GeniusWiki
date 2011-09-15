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
package com.edgenius.wiki.gwt.client.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.DialogListener;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * This Editor bind PlainEditor and TinyMCEEDitor
 * @author Dapeng.Ni
 */
public class Editor extends SimplePanel  implements DialogListener{
	//indicator if contentbox allow using Rich Editor, may always enable if this function complete.
	public static final boolean RICH_ENABLE = true;
	
	private PlainEditor plainEditor;
	TinyMCE richEditor;
	private boolean richEnabled;
	private PageMain main;
	private Vector<SwitchEditorListener> listeners = new Vector<SwitchEditorListener>();
	private Set<DialogBox> dlgBoxContainer;
	private AttachmentPanel attachmentPanel;
	
	public Editor(PageMain main,boolean richEnabled){
		this.main = main;
		this.richEnabled = RICH_ENABLE && richEnabled;
		plainEditor = new PlainEditor(this);
		richEditor = new TinyMCE(this,plainEditor.getID(),richEnabled);
		richEditor.registerShortkey(main);
		
		this.setWidget(plainEditor);
		this.setStyleName(Css.EDITOR);
		this.addSwitchEditorListener(plainEditor);
	}
	public void bindAttachmentPanel(AttachmentPanel panel){
		this.attachmentPanel = panel;
	}
	public AttachmentPanel getAttachmentPanel() {
		return this.attachmentPanel;
	}
    public void enableRich(boolean enable){
    	this.richEnabled = enable;
   		richEditor.enable(enable);
   		plainEditor.enable(!enable);
    }
    
    public boolean isRichEnabled() {
		return richEnabled;
	}
    
	public String getText() {
		if(richEnabled){
			richEditor.flushContentFrom();
		}
		return plainEditor.getText();
	}

	public void setText(final String text){
		//defer setText(), so that it could be execute corresponding sequence correctly with enable() 
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				plainEditor.setText(text);
				if(RICH_ENABLE && richEnabled)
					richEditor.flushContentTo();
			}
		});
	}

	public void addSwitchEditorListener(SwitchEditorListener listener){
		listeners.add(listener);
	}
	public void toRichEditor() {
		if(!richEnabled){
			PageControllerAsync pageController = ControllerFactory.getPageController();
			pageController.switchEditor(main.getSpaceUname(), main.getPageUuid(), true,getText(),getAttachmentNodeUuidList(), new SwitchEditorAsync());
		}		
	}
	public void toPlainEditor() {
		//try to avoid double click "switch" to lead double invoke
		if(richEnabled){
			PageControllerAsync pageController = ControllerFactory.getPageController();
			pageController.switchEditor(main.getSpaceUname(), main.getPageUuid(), false,getText(),getAttachmentNodeUuidList(), new SwitchEditorAsync());
		}
		
	}
	public void setTabIndex(int idx) {
		plainEditor.setTabIndex(idx);
		if(RICH_ENABLE)
			richEditor.setTabIndex(idx);
	}
	public void addFocusHandler(FocusHandler listener) {
		
		plainEditor.addFocusHandler(listener);
		if(RICH_ENABLE)
			richEditor.addFocusHandler(listener);
	}

	public void addChangeHandler(ChangeHandler handler) {
		plainEditor.addChangeHandler(handler);
		if(RICH_ENABLE)
			richEditor.addChangeHandler(handler);
	}

	public void addKeyPressHandler(KeyPressHandler listener) {
		plainEditor.addKeyPressHandler(listener);
		if(RICH_ENABLE)
			richEditor.addKeyPressHandler(listener);
	}

	public void setFocus(boolean focus) {
		if(richEnabled)
			richEditor.setFocus(focus);
		else
			plainEditor.setFocus(focus);
	}

	/**
	 * Some TinyMCE dialog's parent is editor region rather than whole browser page, so close if there is such dialog opening 
	 */
	public void closeChildDialog() {
		if(dlgBoxContainer != null){
			for (DialogBox box : dlgBoxContainer) {
				box.hidebox();
			}
		}
	}
	public void dialogClosed(DialogBox box) {
		dlgBoxContainer.remove(box);
	}
	public void dialogOpened(DialogBox box) {
		
		if(dlgBoxContainer == null)
			dlgBoxContainer = new HashSet<DialogBox>();
		
		dlgBoxContainer.add(box);
		
	}
	public void dialogRelocated(DialogBox dialog) {}
	public boolean dialogClosing(DialogBox dialog) {
		return true;
	}

	public boolean dialogOpening(DialogBox dialog) {
		return true;
	}

	//********************************************************************
	//               private class
	//********************************************************************
    class SwitchEditorAsync implements AsyncCallback<PageModel>{
    	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    	//  failure/success on switch editor               
    	public void onFailure(Throwable error) {
    		for (SwitchEditorListener listener : Editor.this.listeners) {
				listener.failedOnSwitch();
			}
    		GwtClientUtils.processError(error);
    		
    	}

    	public void onSuccess(PageModel model) {
    		for (SwitchEditorListener listener : Editor.this.listeners) {
				listener.successOnSwitch();
			}

    		if(!GwtClientUtils.preSuccessCheck(model,null)){
    			Window.alert(Msg.consts.switch_editor_error());
    			return;
    		}
    		//if use double click "switch", this request will invoke twice, don't switch 
    		if(model.isRichContent != richEnabled){ 
    			enableRich(!richEnabled);
    		}
    		//must call after enableRich() so that text point to correct textarea. 
    		setText(model.content);
    	}
    	
    	
    }

	/**
	 * @return
	 */
	public String getSpaceUname() {
		return main.getSpaceUname();
	}
	public String getPageUuid() {
		return main.getPageUuid();
	}

	/**
	 * @return
	 */
	public List<AttachmentModel> getAttachmentList() {
		return main.getAttachmentList();
	}
	public String[] getAttachmentNodeUuidList() {
		return main.getAttachmentNodeUuidList();
	}

	
	public String getID() {
		return plainEditor.getID();
	}


	
}
