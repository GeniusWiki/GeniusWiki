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

import java.util.HashMap;
import java.util.Map;

import com.edgenius.wiki.gwt.client.widgets.PaletteDialog;
import com.edgenius.wiki.gwt.client.widgets.PaletteListener;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * To allow on HTML page has multiple RichEditor(TinyMCE), this class is used to register each RichEditor on HTML page
 * and dispatch event correspondingly.
 * 
 * @author Dapeng.Ni
 */
@SuppressWarnings("unused")
public class EditorRegistry {
	private Map<String, Editor> editors = new HashMap<String, Editor>();

	public EditorRegistry(){
		bindJsMethod(this);
	}
	public void register(Editor editor){
		editors.put(editor.getID(), editor);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// following method is called by JS native method
	private void toPlainEditor(String id){
		editors.get(id).toPlainEditor();
	}
	
	private void openImageDialog(String id){
		editors.get(id).richEditor.openImageDialog();
	}
	private void openEmotionsDialog(String id){
		editors.get(id).richEditor.openEmotionsDialog();
	}
	private void openLinkDialog(String id){
		editors.get(id).richEditor.openLinkDialog();
	}
	private void openAnchorDialog(String id){
		editors.get(id).richEditor.openAnchorDialog();
	}
	private void openInsertTableDialog(String id){
		editors.get(id).richEditor.openInsertTableDialog(false);
	}
	private void openUpdateTableDialog(String id){
		editors.get(id).richEditor.openInsertTableDialog(true);
	}
	private void openMergeCellsDialog(String id){
		editors.get(id).richEditor.openMergeCellsDialog();
		
	}
	private void openRowPropsDialog(String id){
		//at moment, this function is disabled in RichEditor
		editors.get(id).richEditor.openRowPropsDialog();
	}
	private void openCellPropsDialog(String id){
		//at moment, this function is disabled in RichEditor
		editors.get(id).richEditor.openCellPropsDialog();
	}
	private void onFocus(String id,boolean focus){
		editors.get(id).richEditor.onFocus(focus);
	   
	}
	
	private void onInit(String id){
		editors.get(id).richEditor.onInit();
	}
	private void onChange(String id){
    	editors.get(id).richEditor.onChange();
    }
	private boolean onKeyPress(String id, boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
    	return editors.get(id).richEditor.onKeyPress(ctrlKey, altKey, shiftKey, metaKey, keyCode, charCode);
    }
	private boolean onKeyUp(String id, boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
    	return editors.get(id).richEditor.onKeyUp(ctrlKey, altKey, shiftKey, metaKey, keyCode, charCode);
  
    }
	private boolean onKeyDown(String id, boolean ctrlKey, boolean altKey,
		      boolean shiftKey, boolean metaKey, int keyCode, int charCode){
    	return editors.get(id).richEditor.onKeyDown(ctrlKey, altKey, shiftKey, metaKey, keyCode, charCode);
    	
  
    }
	
	private void paletteDialog(String id, String receiverId, String defaultColor, JavaScriptObject callback){
		editors.get(id).richEditor.saveEditorBookmark();
		PaletteDialog palette = new PaletteDialog(receiverId, defaultColor);
		palette.addPaletteListener(new JsPaletteListener(id, callback));
		palette.showbox();
	}
	

	private class JsPaletteListener implements PaletteListener{
		private JavaScriptObject callback;
		private String tinyMCEID;
		public JsPaletteListener(String tinyMCEID, JavaScriptObject callback){
			this.callback = callback;
			this.tinyMCEID = tinyMCEID;
		}
		public void selectedColor(String receiverId, String color) {
			editors.get(tinyMCEID).richEditor.restoreEditorBookmark();
			doCallback(callback,color);
			
		}
		private native void doCallback(JavaScriptObject callback, String color)/*-{
		   callback(color);
		}-*/;
	}
	private void openMessageMacroDialog(String id, String type){ 
		editors.get(id).richEditor.openMessageMacroDialog(type);
	}
	private void openMacroDialog(String id, String type, String[] params){ 
		editors.get(id).richEditor.openMacroDialog(type, params);
	}
	private native void bindJsMethod(EditorRegistry editor)/*-{


		$wnd.gwtPaletteDialog = function(id, receiverID, defaultColor,callback) {
			editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::paletteDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(id, receiverID,defaultColor,callback);
		};
	   $wnd.gwtGotoPlainEditor= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::toPlainEditor(Ljava/lang/String;)(id);
	   };
	    $wnd.gwtWbEmotionsDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openEmotionsDialog(Ljava/lang/String;)(id);
	   };

	   $wnd.gwtWbImageDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openImageDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtWbLinkDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openLinkDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtWbAnchorDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openAnchorDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtOpenInsertTableDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openInsertTableDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtOpenUpdateTableDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openUpdateTableDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtOpenMergeCellsDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openMergeCellsDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtOpenRowPropsDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openRowPropsDialog(Ljava/lang/String;)(id);
	   };
	   $wnd.gwtOpenCellPropsDialog= function (id) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openCellPropsDialog(Ljava/lang/String;)(id);
	   };
	   
	  $wnd.gwtTinyMCEEventOnInit = function(id, receiverID, defaultColor,callback) {
			editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onInit(Ljava/lang/String;)(id);
	  };
	  $wnd.gwtTinyMCEEventOnChange = function (id) {
	       editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onChange(Ljava/lang/String;)(id);
	   };
	  $wnd.gwtTinyMCEEventOnFocus = function (id) {
	       editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onFocus(Ljava/lang/String;Z)(id,focus);
	   };
	  $wnd.gwtTinyMCEEventOnKeyPress = function (id,ctrlKey, altKey,shiftKey, metaKey, keyCode,charCode) {
	       return editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onKeyPress(Ljava/lang/String;ZZZZII)(id,ctrlKey,altKey,shiftKey, metaKey,keyCode,charCode);
	   };
	  $wnd.gwtTinyMCEEventOnKeyDown = function (id,ctrlKey, altKey,shiftKey, metaKey, keyCode,charCode) {
	       return editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onKeyDown(Ljava/lang/String;ZZZZII)(id,ctrlKey, altKey,shiftKey, metaKey,keyCode,charCode);
	   };
	  $wnd.gwtTinyMCEEventOnKeyUp = function (id,ctrlKey, altKey,shiftKey, metaKey, keyCode,charCode) {
	       return editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::onKeyUp(Ljava/lang/String;ZZZZII)(id,ctrlKey, altKey,shiftKey, metaKey,keyCode,charCode);
	   };
	   
	   
	   $wnd.gwtOpenMessageMacroDialog= function (id, type) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openMessageMacroDialog(Ljava/lang/String;Ljava/lang/String;)(id,type);
	   };
	   $wnd.gwtOpenMacroDialog= function (id, type, params) {
	          editor.@com.edgenius.wiki.gwt.client.editor.EditorRegistry::openMacroDialog(Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)(id,type,params);
	   };
	   
	}-*/;

}
