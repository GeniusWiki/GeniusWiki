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

import java.util.Date;

import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PluginModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.PluginControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class CalendarDetailDialog extends DialogBox implements DialogListener, ClickHandler, AsyncCallback<PluginModel> {

	interface PanelUiBinder extends UiBinder<Widget, CalendarDetailDialog> {}
	private static PanelUiBinder uiBinder = GWT.create(PanelUiBinder.class);
	
	@UiField DeckPanel deck;
	
	//view part
	@UiField Label vSubject;
	@UiField Label vTime;
	@UiField Label vAllDayEvent;
	@UiField Label vLocation;
	@UiField Label vDescription;
//	@UiField Label vRepeatEvent;
	
	//edit part
	@UiField FormPanel form;
	@UiField Hidden pageUuid; 
	@UiField Hidden calendarName; 
	@UiField Hidden eventID; 
	@UiField Hidden color; 
	@UiField TextBox subject; 
	@UiField TextBox sdate; 
	@UiField TextBox stime; 
	@UiField Label toLabel; 
	@UiField TextBox edate; 
	@UiField TextBox etime; 
	@UiField TextBox location; 
	@UiField TextArea description;
	@UiField CheckBox isAllDayEvent;
	
	@UiField MessageWidget message;
	
	private Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
	private Button saveBtn = new Button(Msg.consts.save(),ButtonIconBundle.tickImage());
	private Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	
	private JavaScriptObject callback;
	public CalendarDetailDialog(){
		this.addDialogListener(this);
		this.setWidget(uiBinder.createAndBindUi(this));
		
		//must after uiBinder initialized.
		DOM.setElementAttribute(sdate.getElement(), "id", "stpartdate");
		DOM.setElementAttribute(stime.getElement(), "id", "stparttime");
		DOM.setElementAttribute(edate.getElement(), "id", "etpartdate");
		DOM.setElementAttribute(etime.getElement(), "id", "etparttime");
		DOM.setElementAttribute(isAllDayEvent.getElement(), "id", "isAllDayEvent");
		DOM.setElementAttribute(color.getElement(), "id", "colorvalue");
		
		isAllDayEvent.addClickHandler(this);
		okBtn.addClickHandler(this);
		saveBtn.addClickHandler(this);
		cancelBtn.addClickHandler(this);
		//show busy
		deck.showWidget(2);
	}
	
	public void dialogRelocated(DialogBox dialog) {
		hideDropdown();
	}

	public boolean dialogClosing(DialogBox dialog) {
		hideDropdown();

		return true;
	}

	public void dialogOpened(DialogBox dialog) {
	}

	public void dialogClosed(DialogBox dialog) {
		abortElements();
	}

	public boolean dialogOpening(DialogBox dialog) {
		return true;
	}

	public void onClick(ClickEvent evt) {
		if(evt.getSource() == isAllDayEvent){
			onAllDayEvent();
		}else if(evt.getSource() == okBtn){
			this.hidebox();
		}else if(evt.getSource() == cancelBtn){
			this.hidebox();
		}else if(evt.getSource() == saveBtn){
			PluginControllerAsync action = ControllerFactory.getPluginController();
			try {
				if(StringUtil.isEmpty((subject.getText()))){
					message.error("Please input event subject.");
					return;
				}
				DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy hh:mm");
				Date st = format.parse(StringUtil.trimToEmpty(sdate.getText()) + " " + StringUtil.trimToEmpty(stime.getText()));
				Date ed = format.parse(StringUtil.trimToEmpty(edate.getText()) + " " + StringUtil.trimToEmpty(etime.getText()));
				if(!isAllDayEvent.getValue() && (ed.before(st) || st.equals(ed))){
					message.error("End time must greater than start time.");
					return;
				}
				//TODO
				String repeatRule = "";
				action.request(PageMain.getSpaceUname(),PageMain.getPageUuid(),"calendarService","saveEvent",
						new String[]{
					pageUuid.getValue(),
					calendarName.getValue(),
					eventID.getValue(),
					color.getValue(),
					subject.getText(), 
					String.valueOf(st.getTime()), 
					String.valueOf(ed.getTime()), 
					location.getText(),
					description.getText(),
					String.valueOf(isAllDayEvent.getValue()),
					repeatRule
				},this);
				this.hidebox();
			} catch (Exception e) {
				message.error("Invalid date format. Expect MM/dd/yyyy hh:mm");
			}
		}
	}
	public void onFailure(Throwable caught) {
		GwtClientUtils.processError(caught);
	}
	public void onSuccess(PluginModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		
		if(this.callback != null)
			doCallback(this.callback);

	}
	//********************************************************************
	//               private methods
	//********************************************************************
	private void initViewPanel(int color, Long startT, Long endT, boolean isAllday, String repeatR, String title, String loca, String content) {
		vSubject.setText(title);
		String time;
		if(isAllday)
			time = GwtClientUtils.toDisplayDate(startT);
		else{
			time = GwtClientUtils.toDisplayDate(startT) + " - " + GwtClientUtils.toDisplayDate(endT);
		}
		vTime.setText(time);
		if(isAllday)
			vAllDayEvent.setVisible(true);
		else
			vAllDayEvent.setVisible(false);
		vLocation.setText(loca);
		vDescription.setText(content);
//		vRepeatEvent.setText(repeatR);
		
		ButtonBar btnPanel = getButtonBar();
		btnPanel.clear();
		btnPanel.add(okBtn);

		//show view panel
		deck.showWidget(1);
		this.center();
	}
	private void initEditPanel(int eUid, String pUuid, String calName, int color, long st, long et, boolean isAllday, String repeatR, String title, String loca, String content) {
		initDropdown(color);
		
		adoptElement("divcalendarcolor");
		adoptElement("divstimedrop");
		adoptElement("divetimedrop");
		adoptElement("BBIT_DP_CONTAINER");
		
		eventID.setValue(String.valueOf(eUid));
		pageUuid.setValue(pUuid);
		calendarName.setValue(calName);
		subject.setText(StringUtil.trimToEmpty(title));
		description.setText(StringUtil.trimToEmpty(content));
		location.setText(StringUtil.trimToEmpty(loca));
		isAllDayEvent.setValue(isAllday);
		
		Date start;
		Date end;
		if(st < 0){
			start = new Date();
			//add 60 minutes
			end = new Date(new Date().getTime() + 60*60*1000);
		}else{
			start = new Date(st);
			end = new Date(et);
		}
		
		DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy");
		sdate.setText(format.format(start));
		edate.setText(format.format(end));
		
		DateTimeFormat format2 = DateTimeFormat.getFormat("hh:mm");
		stime.setText(format2.format(start));
		etime.setText(format2.format(end));
		
		onAllDayEvent();
		
		ButtonBar btnPanel = getButtonBar();
		btnPanel.clear();
		btnPanel.add(cancelBtn);
		btnPanel.add(saveBtn);
		
		//show edit panel
		deck.showWidget(0);
		this.center();
	}
	/**
	 * @param eventID
	 * @param edit
	 */
	private void load(int eventID, boolean edit) {
		Log.info("load event by " + eventID + " for edit " + edit);
		PluginControllerAsync action = ControllerFactory.getPluginController();
		action.request(PageMain.getSpaceUname(),PageMain.getPageUuid(),"calendarService","getEvent",
				new String[]{String.valueOf(eventID)},new LoadEventAsync(edit));
	}


	private void onAllDayEvent() {
		if(isAllDayEvent.getValue()){
			//all day event
			stime.setVisible(false);
			etime.setVisible(false);
			edate.setVisible(false);
			toLabel.setVisible(false);
			DOM.setStyleAttribute(DOM.getElementById("endcalpick"),"display","none");
		}else{
			stime.setVisible(true);
			etime.setVisible(true);
			edate.setVisible(true);
			toLabel.setVisible(true);
			DOM.setStyleAttribute(DOM.getElementById("endcalpick"),"display","");
		}
	}
	/**
	 * hide native javascript(jQuery) dropdown pop.
	 */
	private void hideDropdown() {
		Element ele = DOM.getElementById("divcalendarcolor");
		if(ele != null)
			DOM.setStyleAttribute(ele,"display","none");
		
		ele = DOM.getElementById("divstimedrop");
		if(ele != null)
			DOM.setStyleAttribute(ele,"display","none");
		
		ele = DOM.getElementById("divetimedrop");
		if(ele != null)
			DOM.setStyleAttribute(ele,"display","none");
		
		ele = DOM.getElementById("BBIT_DP_CONTAINER");
		if(ele != null)
			DOM.setStyleAttribute(ele,"display","none");
		
	}
	//call native javascript method to initial dropdown - color & date & time picker
	private native void initDropdown(int colorVal)/*-{
	    $wnd.initEventDialog(colorVal);
	}-*/;

	//********************************************************************
	//exposed GWT methods
	//********************************************************************
	//please note:JSNI disallow long type: http://code.google.com/webtoolkit/doc/latest/DevGuideCodingBasicsJSNI.html
	public static void newEvent(String pageUuid, String calendarName, double start, double end, boolean isAllday, String title, JavaScriptObject callback){
		CalendarDetailDialog dialog = new CalendarDetailDialog();
		dialog.setText(Msg.consts.ext_cal_new_event());
		dialog.setIcon(new Image(IconBundle.I.get().calendar()));
		dialog.setCallback(callback);
		dialog.showbox();
		dialog.initEditPanel(0,pageUuid, calendarName, -1, (long)start, (long)end, isAllday, "",title,"", "");
	}

	
	public static void editEvent(int eventID, JavaScriptObject callback){
		CalendarDetailDialog dialog = new CalendarDetailDialog();
		dialog.setText(Msg.consts.ext_cal_edit_event());
		dialog.setIcon(new Image(IconBundle.I.get().calendar()));
		dialog.setCallback(callback);
		dialog.showbox();
		dialog.load(eventID,true);
	}
	

	/**
	 * @param callback
	 */
	private void setCallback(JavaScriptObject callback) {
		this.callback = callback;
	}

	public static void viewEvent(int eventID){
		CalendarDetailDialog dialog = new CalendarDetailDialog();
		dialog.setText(Msg.consts.ext_cal_view_event());
		dialog.showbox();
		dialog.load(eventID,false);
	}
	private native void doCallback(JavaScriptObject callback)/*-{
	   callback();
	}-*/;
	public static native void bindJsMethod()/*-{
		$wnd.gwtNewEventDialog = function(pageUuid, calendarName, start,end,isAllDay,title, callback) {
			@com.edgenius.wiki.gwt.client.widgets.CalendarDetailDialog::newEvent(Ljava/lang/String;Ljava/lang/String;DDZLjava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(pageUuid, calendarName, start,end,isAllDay,title,callback);
		};
		$wnd.gwtEditEventDialog = function(eventID, callback) {
			@com.edgenius.wiki.gwt.client.widgets.CalendarDetailDialog::editEvent(ILcom/google/gwt/core/client/JavaScriptObject;)(eventID,callback);
		};
		$wnd.gwtViewEventDialog = function(eventID) {
			@com.edgenius.wiki.gwt.client.widgets.CalendarDetailDialog::viewEvent(I)(eventID);
		};
	}-*/;
	
	private class LoadEventAsync implements AsyncCallback<PluginModel>{
		private boolean isEdit;
		/**
		 * @param edit
		 */
		public LoadEventAsync(boolean edit) {
			isEdit = edit;
		}
		public void onFailure(Throwable caught) {
			GwtClientUtils.processError(caught);
		}
		public void onSuccess(PluginModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			if(!StringUtil.isBlank(model.response)){
				//parse json, and fill-in
				JSONValue jsonValue = JSONParser.parse(model.response);
				//com.edgenius.wiki.ext.calendar.model.CaledarEvent
				JSONObject evtObj = jsonValue.isObject();
				Log.info("Get json object " + model.response);
				
				int eventID = (int)evtObj.get("eventID").isNumber().doubleValue();
				long start = (long)evtObj.get("startDate").isNumber().doubleValue();
				long end = (long)evtObj.get("endDate").isNumber().doubleValue();
				int color = (int)(evtObj.get("color").isNumber().doubleValue());
				boolean isAllday = evtObj.get("isAllDayEvent").isBoolean().booleanValue();
				String repeatR = evtObj.get("repeatRule").isString().stringValue();
				String title = evtObj.get("subject").isString().stringValue();
				String location = evtObj.get("location").isString().stringValue();
				String content = evtObj.get("content").isString().stringValue();
				if(isEdit){
					initEditPanel(eventID,"","", color, start,end, isAllday,repeatR,title,location, content);
				}else{
					initViewPanel(color, start,end, isAllday,repeatR,title,location, content);
				}
				
			}
		}
		
	}


}
