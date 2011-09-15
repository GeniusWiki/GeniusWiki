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


import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ColorPicker;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 * 
 */
// This Class name is use in Javascript, please be careful when refactor class name 
public class MCEInsertTableDialog extends MCEDialog implements ClickHandler {
	private static final int DEFAULT_BORDER_SIZE = 1;
	
	private RadioButton asTable = new RadioButton("options",Msg.consts.as_table());
	private RadioButton asGrid= new RadioButton("options",Msg.consts.as_grid());
	private TextBox rows = new TextBox();
	private TextBox cols = new TextBox();
	private TextBox gridRows = new TextBox();
	private TextBox gridCols = new TextBox();
	private TextBox border = new TextBox();
//	private CheckBox hasCaption = new CheckBox(Msg.consts.has_caption());
	private CheckBox hasTitle = new CheckBox(Msg.consts.first_row_title());
	private String width;
	private String height;
	
	private ColorPicker bkPicker = new ColorPicker(SharedConstants.TABLE_BG_DEFAULT_COLOR,null);
	private ColorPicker borderPicker = new ColorPicker(SharedConstants.TABLE_BORDER_DEFAULT_COLOR,null);
	private DeckPanel deck = new DeckPanel();
	private boolean update;
	
	/**
	 * @param tiny
	 */
	public MCEInsertTableDialog(TinyMCE tiny, boolean update) {
		super(tiny);
		this.update = update;
		this.setText(Msg.consts.insert_table());
		
		
		FlexTable options = new FlexTable();

		options.setWidget(0,0,asTable);
		options.setWidget(0,1,asGrid);
		asTable.setValue(true);
		asTable.addClickHandler(this);
		asGrid.addClickHandler(this);
		options.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		options.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		options.setStyleName(Css.OPTIONS);
		
		FlexTable tableParamLayout = new FlexTable();
		tableParamLayout.setWidget(0,0,new Label(Msg.consts.rows()));
		tableParamLayout.setWidget(0,1,rows);
		
		tableParamLayout.setWidget(0,2,new Label(Msg.consts.cols()));
		tableParamLayout.setWidget(0,3,cols);
		
		tableParamLayout.setWidget(1,0,new Label(Msg.consts.bk_color()));
		tableParamLayout.setWidget(1,1,bkPicker);
		
		tableParamLayout.setWidget(1,2,new Label(Msg.consts.border_color()));
		tableParamLayout.setWidget(1,3,borderPicker);
		
		tableParamLayout.setWidget(2,0,new Label(Msg.consts.border()));
		tableParamLayout.setWidget(2,1,border);
		
		tableParamLayout.setWidget(2,2,hasTitle);
		tableParamLayout.getFlexCellFormatter().setColSpan(2, 2, 2);
		
		tableParamLayout.getColumnFormatter().setWidth(0, "120px");
		tableParamLayout.getColumnFormatter().setWidth(2, "120px");
		rows.setStyleName(Css.TINY_TEXT_BOX);
		cols.setStyleName(Css.TINY_TEXT_BOX);
		border.setStyleName(Css.TINY_TEXT_BOX);
		
		FlexTable gridParamLayout = new FlexTable();
		gridParamLayout.setWidget(0,0,new Label(Msg.consts.rows()));
		gridParamLayout.setWidget(0,1,gridRows);
		
		gridParamLayout.setWidget(0,2,new Label(Msg.consts.cols()));
		gridParamLayout.setWidget(0,3,gridCols);
		gridRows.setStyleName(Css.TINY_TEXT_BOX);
		gridCols.setStyleName(Css.TINY_TEXT_BOX);
		
		tableParamLayout.setWidth("100%");
		tableParamLayout.setCellSpacing(5);
		gridParamLayout.setWidth("100%");
		gridParamLayout.setCellSpacing(5);
		options.setWidth("100%");
		options.setCellSpacing(5);
		deck.setWidth("100%");

		deck.insert(tableParamLayout, 0);
		deck.insert(gridParamLayout, 1);

		if(update){
			JsArrayString list = getProperties();
			if(list.get(8).toLowerCase().indexOf("macrogrid") != -1){
				asGrid.setValue(true);
				gridRows.setText(list.get(0));
				gridCols.setText(list.get(1));
				//set it to same value for switch back
				rows.setText(list.get(0));
				cols.setText(list.get(1));
				//disable some unable to change attributes
				gridRows.setEnabled(false);
				gridCols.setEnabled(false);
				deck.showWidget(1);
			}else{
				asTable.setValue(true);
				deck.showWidget(0);
				//this sequence is according to method getProperties() on  table.js in TinyMCE table plugin
				rows.setText(list.get(0));
				cols.setText(list.get(1));
				gridRows.setText(list.get(0));
				gridCols.setText(list.get(1));
				
				final String color = StringUtil.isBlank(list.get(2))?SharedConstants.TABLE_BG_DEFAULT_COLOR:list.get(2);
				if(GwtClientUtils.isIE()){
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							//stupid IE need this deferred set
							bkPicker.setColor(color);
						}
					});
				}else
					bkPicker.setColor(color);
				final String bcolor = StringUtil.isBlank(list.get(3))?SharedConstants.TABLE_BORDER_DEFAULT_COLOR:list.get(3);
				if(GwtClientUtils.isIE()){
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						public void execute() {
							//stupid IE need this deferred set
							borderPicker.setColor(bcolor);
						}
					});
				}else
					borderPicker.setColor(bcolor);
				
				hasTitle.setValue(BooleanUtil.toBoolean(list.get(4)));
				width = GwtUtils.removeUnit(list.get(5));
				height =GwtUtils.removeUnit(list.get(6));
				String borderS = GwtUtils.removeUnit(list.get(7));
				border.setText(StringUtil.isBlank(borderS)?DEFAULT_BORDER_SIZE+"":borderS);
				
				//disable some unable to change attributes
				rows.setEnabled(false);
				cols.setEnabled(false);
			}
		}else{
			//initial values
			gridRows.setText("1");
			gridCols.setText("3");
			
			rows.setText("2");
			cols.setText("2");
			border.setText("1");
			hasTitle.setValue(true);
			deck.showWidget(0);
			
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					rows.setFocus(true);
				}
			});
		}
		
		VerticalPanel form = new VerticalPanel();
		form.setWidth("100%");
		form.add(options);
		form.add(deck);
		this.setWidget(form);
		
	
	}

	@Override
	protected void okEvent() {
		int row;
		int col;
		String borderColor="", bgColor="", borderWidth="",cellpadding="",cellspacing="", clz;
		StringBuilder styleBuf = new StringBuilder();
		String hasTitleB = "false";
		if(asGrid.getValue()){
			row = NumberUtil.toInt(gridRows.getText(), -1);
			col =NumberUtil.toInt(gridCols.getText(), -1);
			if(row <=0 || col <= 0){
				Window.alert(Msg.consts.error_input_number_only());
				return;
			}
			styleBuf.append("border-width:0px;width:100%;");
			//!!!please note: this size has same value in MceInsertTableMacro.java, please keep consist.
			cellpadding = "5";
			cellspacing = "5";
			clz = "macroGrid";
		}else{
			clz = "macroTable";
			row = NumberUtil.toInt(rows.getText(), -1);
			col =NumberUtil.toInt(cols.getText(), -1);
			if(row <=0 || col <= 0){
				Window.alert(Msg.consts.error_input_number_only());
				return;
			}
			hasTitleB = Boolean.valueOf(hasTitle.getValue()).toString();
			int borderN =NumberUtil.toInt(border.getText(), -1);
			if(borderN == -1){
				border.setText("0");
				borderN = 0;
			}
			//according to TinyMCE, style string is for creating table, hidden is for update use 
			//although style=border-color is useless as it must put into td/th level,but it is useful to 
			//get back this value when editing table properties
			if(!SharedConstants.TABLE_BORDER_DEFAULT_COLOR.equalsIgnoreCase(borderPicker.getColor())){
				borderColor = borderPicker.getColor();
				styleBuf.append("border-color:").append(borderColor);
			}
			if(!SharedConstants.TABLE_BG_DEFAULT_COLOR.equalsIgnoreCase(bkPicker.getColor())){
				//only background color is not white.
				if(styleBuf.length() > 0)
					styleBuf.append(";");
				
				bgColor = bkPicker.getColor();
				styleBuf.append("background-color:").append(bgColor);
			}
			if(!StringUtil.isBlank(width)){
				if(styleBuf.length() > 0)
					styleBuf.append(";");
				styleBuf.append("width:").append(width);
			}
			if(!StringUtil.isBlank(height)){
				if(styleBuf.length() > 0)
					styleBuf.append(";");
				styleBuf.append("height:").append(height);
			}
			
			//border also pass to client even it is default size, this makes "1px solid #121212" valid 
			borderWidth = borderN+"px";
			if(borderN != DEFAULT_BORDER_SIZE){
				if(styleBuf.length() > 0)
					styleBuf.append(";");
				
				styleBuf.append("border-width:").append(borderWidth);
			}
		}
		//Original design is use form.elements['name'].value to retrieve values. But fuck IE, it does work:
		//http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/5144115ec715ce1a/78507fc52fab2429?lnk=gst&q=setName+ie#78507fc52fab2429
		//It means pure javascript can not get value by name. So I collect them into list then pass it to javascript. Fuck IE again.
		JsArrayString input = (JsArrayString) JavaScriptObject.createArray();
		input.set(0, String.valueOf(row));
		input.set(1, String.valueOf(col));
		input.set(2, bgColor);
		input.set(3, borderColor);
		input.set(4, hasTitleB);
		input.set(5, borderWidth);
		input.set(6, styleBuf.toString());
		input.set(7, cellpadding);
		input.set(8, cellspacing);
		input.set(9, clz);
		
		tiny.restoreEditorBookmark();
		if(update){
			updateTable(input);
		}else{
			//insert table
			insertTable(input);
		}
		close();
		
	}
	
	public void onClick(ClickEvent event) {
		if(event.getSource() == asGrid){
			deck.showWidget(1);
		}else{
			deck.showWidget(0);
		}
		
	}
	private native void updateTable(JsArrayString input) /*-{
		 $wnd.action='update';
	  	 $wnd.insertTable(input);
	}-*/;
	private native void insertTable(JsArrayString input) /*-{
		 $wnd.action='insert';
	  	 $wnd.insertTable(input);
	}-*/;
	
	private native JsArrayString getProperties() /*-{
	  	 return $wnd.getProperties();
	}-*/;



}
