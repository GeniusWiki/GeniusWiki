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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * This class override code from gwt.DialogBox to lightbox style DialogBox when calling showbox()/hidebox() method.
 * 
 * Another reason don't extends from gwt DialogBox directly is Caption need more complex element. This box support
 * Icon, Text and Close Button.
 */
public class DialogBox extends PopupPanel implements HasHTML, HasText,MouseOverHandler,MouseOutHandler, 
			MouseDownHandler, MouseUpHandler,MouseMoveHandler {
	private Lightbox lightbox;
	
	private boolean dragging;
	private int dragStartX, dragStartY;
	private int windowWidth;
	private int clientLeft;
	private int clientTop;
	
	private FocusPanel icon = new FocusPanel();
	private FocusPanel caption = new FocusPanel();
	private ButtonBar buttonBar = new ButtonBar();
	private CloseButton closeBtn = new CloseButton();
	
	//if this dialog contains native javascript element which also needs events - especially in model dialog which will block event from any non-children element
	// this list will allow native javascript elements are able to acquire event even they are not dialog children.
	private Set<String> adoptedElementIds;
	
	private String text;
	
	private List<DialogListener> listeners;

	private SimplePanel body = new SimplePanel(); 
	private FlexTable captionTable = new FlexTable();
	
	public DialogBox(UIObject parent) {
		this(parent,false);
	}
	public DialogBox() {
		this(null,false);
	}
	 public DialogBox(UIObject parent,boolean autoHide) {
	    this(parent, autoHide, true, true);
	 }

	public DialogBox(UIObject parent, boolean autoHide, boolean withBackground, boolean modal) {
		super(autoHide, modal);
		if(modal && withBackground)
			lightbox = new Lightbox(parent, this);

		closeBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				hidebox();
			}
		});
		
		VerticalPanel panel = new VerticalPanel();
		//caption part: caption focus panel(drag/drop handler) and close button
		captionTable.setWidget(0, 0, icon);
		captionTable.setWidget(0, 1, caption);
		captionTable.setWidget(0, 2, closeBtn);
		
		captionTable.getCellFormatter().setWidth(0, 0, "42px");
		captionTable.getCellFormatter().setWidth(0, 2, "42px");
		captionTable.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_BOTTOM);
		captionTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
		captionTable.getFlexCellFormatter().setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_BOTTOM);
		captionTable.getFlexCellFormatter().setStyleName(0, 0, "dlg-top-left");
		captionTable.getFlexCellFormatter().setStyleName(0, 1, "dlg-border-top");
		captionTable.getFlexCellFormatter().setStyleName(0, 2, "dlg-top-right");
		captionTable.setCellPadding(0);
		captionTable.setCellSpacing(0);
		
		FlexTable bodyTable = new FlexTable();
		bodyTable.getFlexCellFormatter().setStyleName(0, 0, "dlg-border-left");
		bodyTable.getFlexCellFormatter().setStyleName(0, 2, "dlg-border-right");
		bodyTable.setWidget(0,1, body);
		bodyTable.setWidget(1, 0,buttonBar);
		bodyTable.setCellPadding(0);
		bodyTable.setCellSpacing(0);
		
		bodyTable.getFlexCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
		bodyTable.getFlexCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_BOTTOM);
		bodyTable.getFlexCellFormatter().setStyleName(0, 1, Css.MAIN_BODY);
		bodyTable.getFlexCellFormatter().setStyleName(1, 0, Css.BUTTONS_BAR);
		bodyTable.getFlexCellFormatter().setRowSpan(0, 0, 2);
		bodyTable.getFlexCellFormatter().setRowSpan(0, 2, 2);
		bodyTable.getCellFormatter().setWidth(0, 0, "17px");
		bodyTable.getCellFormatter().setWidth(0, 2, "17px");
		
		FlexTable bottomTable = new FlexTable();
		bottomTable.setWidget(0, 0, new HTML("&nbsp;")); //just a placeholder
		bottomTable.setWidget(0, 1, new HTML("&nbsp;")); //just a placeholder
		bottomTable.setWidget(0, 2, new HTML("&nbsp;")); //just a placeholder
		bottomTable.getFlexCellFormatter().setStyleName(0, 0, "dlg-bottom-left");
		bottomTable.getFlexCellFormatter().setStyleName(0, 1, "dlg-border-bottom");
		bottomTable.getFlexCellFormatter().setStyleName(0, 2, "dlg-bottom-right");
		bottomTable.getCellFormatter().setWidth(1, 0, "34px");
		bottomTable.getCellFormatter().setWidth(1, 2, "34px");
		bottomTable.setCellPadding(0);
		bottomTable.setCellSpacing(0);
		
		panel.add(captionTable);
		panel.add(bodyTable);
		panel.add(bottomTable);
	
		//style
		captionTable.setWidth("100%");
		bodyTable.setWidth("100%");
		bottomTable.setWidth("100%");
		
		body.setSize("100%","100%");
		caption.setWidth("100%");
		
		panel.setStyleName(Css.DIALOG_BOX_TABLE);
		panel.setBorderWidth(0);
		caption.addMouseOutHandler(this);
		caption.addMouseOverHandler(this);
		caption.addMouseDownHandler(this);
		caption.addMouseUpHandler(this);
		caption.addMouseMoveHandler(this);
//		icon.addMouseOutHandler(this);
//		icon.addMouseOverHandler(this);
//		icon.addMouseDownHandler(this);
//		icon.addMouseUpHandler(this);
//		icon.addMouseMoveHandler(this);

		captionTable.setStyleName(Css.CAPTION);
		setStyleName(Css.DIALOG_BOX);
		
		
		windowWidth = Window.getClientWidth();
	    clientLeft = Document.get().getBodyOffsetLeft();
	    clientTop = Document.get().getBodyOffsetTop();

		super.setWidget(panel);
		
	}
	
	public void addDialogListener(DialogListener lis){
		if(listeners == null)
			listeners = new ArrayList<DialogListener>();
		
		listeners.add(lis);
	}
	public void setWidget(Widget widget){
		body.setWidget(widget);
	}
	/**
	 * Set Dialog box Caption Image
	 * @param img
	 */
	public void setIcon(Image img){
		icon.clear();
		icon.add(img);
		img.setStyleName(Css.ICON);
		
	}
	
	public void setText(String captionTitle){
		caption.clear();
		caption.add(new Label(captionTitle));
		this.text = captionTitle;
	}

	public void setHTML(String captionTitle){
		caption.clear();
		caption.add(new HTML(captionTitle));
		this.text = captionTitle;
	}
	

	public void showbox(){
		//give a chance to stop open dialog 
		boolean opening = true;
		if(listeners != null){
			for (DialogListener listener : listeners) {
				boolean s = listener.dialogOpening(null);
				if(!s) opening = false;
			}
		}
		if(opening){
			if(lightbox != null){
				lightbox.show();
			}else{
				 this.center();
			}
			if(listeners != null){
				for (DialogListener listener : listeners) {
					listener.dialogOpened(null);
				}
			}
		}
	}
	public void hidebox(){
		//give a chance to stop close dialog 
		boolean closing = true;
		if(listeners != null){
			for (DialogListener listener : listeners) {
				boolean s = listener.dialogClosing(null);
				if(!s) closing = false;
			}
		}
		
		if(closing){
			Log.info("login form height:"+this.getOffsetHeight());
			if(lightbox != null){
				lightbox.hide();
			}else{
				 this.hide();
			}
			if(listeners != null){
				for (DialogListener listener : listeners) {
					listener.dialogClosed(null);
				}
			}
		}
	
	}
	/**
	 * @return
	 */
	public ButtonBar getButtonBar() {
		
		return buttonBar;
	}
	
	
	//********************************************************************
	//               Override method
	//********************************************************************
	public String getHTML() {
		return text;
	}

	public String getText() {
		return text;
	}
	public boolean remove(Widget w) {
		return body.remove(w);
	}

	/**
	   * Override, so that interior panel reflows to match parent's new width.
	   *
	   * @Override
	   */
	 public void setWidth(String width) {
	    super.setWidth(width);

	    // note that you CANNOT call panel.setWidth("100%") until parent's width
	    // has been explicitly set, b/c until then parent's width is unconstrained
	    // and setting panel's width to 100% will flow parent to 100% of browser
	    // (i.e. can't do this in constructor)
	    body.setWidth("100%");
	    caption.setWidth("100%");
	 }
	 
	 /**
	  * Add element ID to adopt elements list. The element is able to acquire the event.
	  * @param id
	  */
	 protected void adoptElement(String id){
			if(adoptedElementIds == null)
				adoptedElementIds = new HashSet<String>();
			
			adoptedElementIds.add(id);
	}
	/**
	 * Clear all adopts element IDs.
	 */
	protected void abortElements() {
		if(adoptedElementIds != null)
			adoptedElementIds.clear();
	}


    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		// We need to preventDefault() on mouseDown events (outside of the
		// DialogBox content) to keep text from being selected when it
		// is dragged.
		NativeEvent nativeEvent = event.getNativeEvent();

		if (!event.isCanceled() && (event.getTypeInt() == Event.ONMOUSEDOWN)
			&& isCaptionEvent(nativeEvent)) {
			nativeEvent.preventDefault();
			
		} else if (!event.isCanceled() && event.getTypeInt() == Event.ONKEYDOWN) {
			int keyCode = event.getNativeEvent().getKeyCode();
			// ESC clicked
			if (keyCode == KeyCodes.KEY_ESCAPE) {
				DialogBox.this.hidebox();
			}
		}
		
		//check if event is from adopted elements, if so, avoid these events cancelled by consume() method. 
		if(adoptedElementIds != null && adoptedElementIds.size() > 0){
			EventTarget target = event.getNativeEvent().getEventTarget();
			if (Element.is(target)) {
				for (String id : adoptedElementIds) {
					Element ele = DOM.getElementById(id);
					if(ele != null && ele.isOrHasChild(Element.as(target))){
						event.consume();
						break;
					}
				}
			}
		}
		super.onPreviewNativeEvent(event);
	}

	private boolean isCaptionEvent(NativeEvent event) {
		EventTarget target = event.getEventTarget();
		if (Element.is(target)) {
			return captionTable.getElement().isOrHasChild(Element.as(target));
		}
		return false;
	}
	public void onMouseUp(MouseUpEvent event) {
		dragging = false;
		DOM.releaseCapture(caption.getElement());
	}
	public void onMouseDown(MouseDownEvent event) {
		dragging = true;
		DOM.setCapture(caption.getElement());
		dragStartX = event.getX();
		dragStartY = event.getY();
	}

	public void onMouseOver(MouseOverEvent event) {
		DOM.setStyleAttribute(caption.getElement(), "cursor", "move");
	}

	public void onMouseOut(MouseOutEvent event) {
		DOM.setStyleAttribute(caption.getElement(), "cursor", null);
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (dragging) {
			int absX = event.getX()+ getAbsoluteLeft();
			int absY = event.getY()+ getAbsoluteTop();
			

		      // if the mouse is off the screen to the left, right, or top, don't
		      // move the dialog box. This would let users lose dialog boxes, which
		      // would be bad for modal popups.
		      if (absX < clientLeft || absX >= windowWidth || absY < clientTop) {
		        return;
		      }
		      
			setPopupPosition(absX - dragStartX, absY - dragStartY);
		}
	}
	/**
	 * @return 
	 * @return
	 */
	public List<DialogListener> getDialogListeners() {
		return listeners;
	}	

}
