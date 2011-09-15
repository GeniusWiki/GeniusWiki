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

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * TinyMCE wrapper class
 * @author Dapeng.Ni
 */
class TinyMCE extends SimplePanel implements HasFocusHandlers, HasBlurHandlers, HasChangeHandlers, 
			HasKeyDownHandlers, HasKeyPressHandlers, HasKeyUpHandlers{

    boolean enabled;
	private String id;
	private Editor editor;
	private PageMain main;
	private JavaScriptObject bookmark;
	private boolean initialized = false;
	
	//********************************************************************
	//               method
	//********************************************************************
    public TinyMCE(Editor editor,String holdID, boolean enable) {
    	this.editor = editor;
        this.enabled = enable;
        this.id = holdID;
    }
    public Editor getEditor(){
    	return editor;
    }
    public String getSpaceUname(){
    	return editor.getSpaceUname();
    }
    public String getPageUuid(){
    	return editor.getPageUuid();
    }
    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    protected void onLoad() {
        super.onLoad();
        if(enabled){
        	//to wait TextArea initialized and decide the size.
        	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        		public void execute() {
        			addMCE(id);
        		}
        	});
        }
    }
    /**
     * Must use Scheduler.scheduleDeferred() mode, it can not get correct size from PlainEditor.TextArea  as Editor may not show up
     * while this method call (for example, while user default editor is RichEditor, EditPanel.fillPanel(){contain this enable() method} 
     * executes before EditorPanel show up).
     *  
     * @param enableRich
     */
	public void enable(final boolean enableRich) {
		if(enableRich == this.enabled){
			//to avoid enable/disable multiple times
			return;
		}
		this.enabled = enableRich;
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				if(enableRich){
					addMCE(id);
				}else{
					initialized = false;
					removeMCE(id);
				}
			}
		});
	}
	/**
	 * @param focus
	 */
	public void setFocus(boolean focus) {
		if(focus && initialized){
			//TODO: if tinyMCE is first initialized, onInit() method always set focus in...
			focusMCE(id);
		}
	}
	/**
	 * @param idx
	 */
	public void setTabIndex(int idx) {
		// TODO: I cannot find a way to do this.. 
	}

	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return addHandler(handler, KeyDownEvent.getType());
	}
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return addHandler(handler, KeyPressEvent.getType());
	}
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return addHandler(handler, KeyUpEvent.getType());
	}

	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return addHandler(handler, FocusEvent.getType());
	}
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return addHandler(handler, BlurEvent.getType());
	}
	
	public HandlerRegistration addChangeHandler(ChangeHandler handler) {
		return this.addHandler(handler, ChangeEvent.getType());
	}

	/**
	 * Save TinyMCE rich area content to plainText editor 
	 */
    public native void flushContentFrom() /*-{
	    $wnd.tinyMCE.triggerSave();
	}-*/;
    
    /**
     * In reverse with flushContentFrom(), this method save plainText editor content to rich area.
     */
    public void flushContentTo(){
    	if(initialized){
	    	updateContent(id);
	    	//content flush from text editor into rich editor. This action won't be record.
	    	clearUndo();
    	}
    }
	


    /**
     * Insert content to RichEditor 
     * @param content
     */
    public native void insertContent(String content)/*-{
	    $wnd.tinyMCE.execCommand('mceInsertContent', false, content);
	}-*/;
	public void registerShortkey(final PageMain main) {
		this.main = main;
	}
	
	/**
	 * Get current selected node attribute values. Input are list of attribute names, such as {"href","title"} etc.
	 * 
	 * @param atts
	 * @return
	 */
    public native String[] getSelectedModeAttributes(String... atts) /*-{
		var dom = $wnd.tinyMCE.activeEditor.dom;
	    var ele = $wnd.tinyMCE.activeEditor.selection.getNode();
	    
	    var v =new Array();
	    for(var idx=0;idx<atts.length;idx++){
	    	v[idx] = dom.getAttrib(ele, atts[idx]);
	    }
	    
	    return v;
	    
	 }-*/;
    
    //This is special for A link, as it may embed some other element, such as image. So try to get selected node's parent to locate A. 
    public native String[] getHrefLinkSelectedModeAttributes(String... atts) /*-{
		var dom = $wnd.tinyMCE.activeEditor.dom;
	    var elm = $wnd.tinyMCE.activeEditor.selection.getNode();
	    
	    elm = dom.getParent(elm, "A");
		if (elm == null || elm.nodeName != "A")
			return null;
			
	    
	    var v =new Array();
	    for(var idx=0;idx<atts.length;idx++){
	    	v[idx] = dom.getAttrib(elm, atts[idx]);
	    }
	    
	    return v;
	    
	 }-*/;
    
	/**
	 * @return
	 */
	public native String getSelectedText() /*-{
       return $wnd.tinyMCE.activeEditor.selection.getContent({format : 'raw'});
    }-*/;
	/**
	 * Select the selected element's parent by specified tag name, for example, 
	 * getParentElement("A") means get the selected element's parent link(<a>) tag. If the parent is
	 * not specified tag, return null.  
	 * @return
	 */
	
	public Element getParentElement(String tagname){
		JavaScriptObject js = getParentJSElement(tagname);
		if(js == null)
			return null;
		
		return Element.as(js);
	}
	private native JavaScriptObject getParentJSElement(String tagname) /*-{
     	var elm = $wnd.tinyMCE.activeEditor.selection.getNode();
		return $wnd.tinyMCE.activeEditor.dom.getParent(elm, tagname);
    }-*/;
	


	//wrapper of getEditorBookmark()
	public void saveEditorBookmark(){
		if(GwtClientUtils.isIE()){
			bookmark = getEditorBookmark();
		}
	}
	/**
	 * In IE browser, any popup dialog will capture the caret from TinyMCE editor, which cause tinyMCE lost its focus and unable 
	 * to get back correct selection or insert content. This will restore the bookmark which is saved when dialog open.
	 */
	public void restoreEditorBookmark(){
		if(GwtClientUtils.isIE()){
			restoreEditorBookmark(bookmark);
		}
	}
	
	private native JavaScriptObject getEditorBookmark() /*-{
       return $wnd.tinyMCE.activeEditor.selection.getBookmark();
    }-*/;
	private native void restoreEditorBookmark(JavaScriptObject bookmark) /*-{
       return $wnd.tinyMCE.activeEditor.selection.moveToBookmark(bookmark);
    }-*/;

	//********************************************************************
	//               
	//********************************************************************
	public void openImageDialog(){
		
		MCEImageDialog imgDlg = new MCEImageDialog(this);
		String[] atts = getSelectedModeAttributes(NameConstants.SRC,NameConstants.WAJAX, NameConstants.TITLE);
		imgDlg.initField(atts[0], atts[1],atts[2]);
		imgDlg.open();
	}
	
	public void openLinkDialog(){
		
		MCELinkDialog linkDlg = new MCELinkDialog(this);		
		String[] atts = getHrefLinkSelectedModeAttributes(NameConstants.HREF,NameConstants.WAJAX);
		if(atts != null && atts.length == 2){
			//update
			linkDlg.initField(atts[0],atts[1]);
		}
		linkDlg.open();
		
	}
	public void openAnchorDialog(){
		MCEAnchorDialog anchorDlg = new MCEAnchorDialog(this);
		String[] atts = getSelectedModeAttributes(NameConstants.NAME);
		anchorDlg.initField(atts[0]);
		anchorDlg.open();
	}
	public void openInsertTableDialog(boolean update){

		MCEInsertTableDialog tableDlg = new MCEInsertTableDialog(this,update);
		tableDlg.open();
	}
	public void openMergeCellsDialog(){
		
		MCEMergeCellsDialog mergeDlg = new MCEMergeCellsDialog(this);
		mergeDlg.open();
	}
	
	public void openEmotionsDialog(){
		
		MCEEmotionsDialog emotionsDlg = new MCEEmotionsDialog(this);
		emotionsDlg.showbox();
	}
	public void openRowPropsDialog(){
		//at moment, this function is disabled in RichEditor
	}
	public void openCellPropsDialog(){
		//at moment, this function is disabled in RichEditor
	}

	public void onFocus(boolean focus){
    	if(focus)
    		FocusEvent.fireNativeEvent(Document.get().createFocusEvent(), this);
    	else
    		FocusEvent.fireNativeEvent(Document.get().createBlurEvent(), this);
    }
	
	public void onInit() {
		initialized = true;
		
		focusMCE(id);
		flushContentTo();
		
		Log.info("TinyMCE " + id + " is initialized");
	}
	
	public void onChange(){
		ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }
	public boolean onKeyPress(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
    	KeyPressEvent.fireNativeEvent(Document.get().createKeyPressEvent(ctrlKey, altKey, shiftKey, metaKey, keyCode), this);
    	return true;
    }
	public boolean onKeyUp(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
		KeyUpEvent.fireNativeEvent(Document.get().createKeyUpEvent(ctrlKey, altKey, shiftKey, metaKey, keyCode), this);
    	return true;
    }
	public boolean onKeyDown(boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
		KeyDownEvent.fireNativeEvent(Document.get().createKeyDownEvent(ctrlKey, altKey, shiftKey, metaKey, keyCode), this);
    	if(main != null){
    		return main.bindGloablKeyShortcut(ctrlKey, altKey, shiftKey, metaKey, keyCode);
    	}
    	return true;
    }

	//********************************************************************
	//               Macro dialog methods
	//********************************************************************
	public void openMessageMacroDialog(String type) {
		MCEMacroMessageDialog dlg = new MCEMacroMessageDialog(this, type);
		dlg.showbox();
		
	}
	public void openMacroDialog(String type, String[] params) {
		MCEMacroDialog dlg = new MCEMacroDialog(this, type, params);
		dlg.showbox();
		
	}
    //********************************************************************
	//               Private method
	//********************************************************************

    private native void updateContent(String id) /*-{
	    $wnd.tinyMCE.execInstanceCommand(id,'mceSetContent', true, $wnd.document.getElementById(id).value,true);
	}-*/;
    private native void clearUndo() /*-{
	    $wnd.tinyMCE.activeEditor.undoManager.clear();
	}-*/;

    /**
     * Change a text area to a tiny MCE editing field
     */
    private native void addMCE(String id) /*-{
        $wnd.tinyMCE.execCommand('mceAddControl', false, id);
    }-*/;

    /**
     * Remove a tiny MCE editing field from a text area
     */
    private native void removeMCE(String id) /*-{
        $wnd.tinyMCE.execCommand('mceRemoveControl', false, id);
    }-*/;

    private native void focusMCE(String id) /*-{
        $wnd.tinyMCE.execCommand('mceFocus', true, id);
    }-*/;

}
