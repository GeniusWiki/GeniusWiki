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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.QueryModel;
import com.edgenius.wiki.gwt.client.render.SearchRenderWidget;
import com.edgenius.wiki.gwt.client.render.SearchRenderWidget.TypeImageBundle;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.DateBox;

/**
 * @author Dapeng.Ni
 */
public class AdvSearchPanel extends SimplePanel implements ClickHandler{

	public static final String WIDTH = "300px";
	private TextBox keyword = new TextBox();
	private RadioButton anyWords = new RadioButton("ktype",Msg.consts.any_words());
	private RadioButton allWords = new RadioButton("ktype",Msg.consts.must_words());
	private RadioButton exactWords = new RadioButton("ktype",Msg.consts.exact_words());
	
	private SortClickHandler sortClickHandler = new SortClickHandler();
	private ToggleLink pageType = new ToggleLink(new Image(TypeImageBundle.I.get().page()), new Image(TypeImageBundle.I.get().page_dis())
				,Msg.consts.page(),Msg.consts.page());
	private ToggleLink spaceType = new ToggleLink(new Image(TypeImageBundle.I.get().space()), new Image(TypeImageBundle.I.get().space_dis())
			,Msg.consts.space(),Msg.consts.space());
	private ToggleLink commentType = new ToggleLink(new Image(TypeImageBundle.I.get().comment()), new Image(TypeImageBundle.I.get().comment_dis())
			,Msg.consts.comment(),Msg.consts.comment());
	private ToggleLink attachmentType = new ToggleLink(new Image(TypeImageBundle.I.get().attachment()), new Image(TypeImageBundle.I.get().attachment_dis())
			,Msg.consts.attachment(),Msg.consts.attachment());
	private ToggleLink pTagType = new ToggleLink(new Image(TypeImageBundle.I.get().ptag()), new Image(TypeImageBundle.I.get().ptag_dis())
			,Msg.consts.tag_on_page(),Msg.consts.tag_on_page());
	private ToggleLink sTagType = new ToggleLink(new Image(TypeImageBundle.I.get().stag()), new Image(TypeImageBundle.I.get().stag_dis())
			,Msg.consts.tag_on_space(),Msg.consts.tag_on_space());
	private ToggleLink userType = new ToggleLink(new Image(TypeImageBundle.I.get().user()), new Image(TypeImageBundle.I.get().user_dis())
			,Msg.consts.user(),Msg.consts.user());
	private ToggleLink widgetType = new ToggleLink(new Image(TypeImageBundle.I.get().widget()), new Image(TypeImageBundle.I.get().widget_dis())
			,Msg.consts.widgets(),Msg.consts.widgets());
	
	private SpaceSuggestBox spaceBox = new SpaceSuggestBox(Msg.consts.all_spaces());
	
	private ToggleLink defaultSort = new ToggleLink(new Image(TypeImageBundle.I.get().score()), new Image(TypeImageBundle.I.get().score_dis())
			,Msg.consts.defaultK(),Msg.consts.defaultK());
	private ToggleLink typeSort = new ToggleLink(new Image(TypeImageBundle.I.get().type()), new Image(TypeImageBundle.I.get().type_dis())
			,Msg.consts.type(),Msg.consts.type());
	private ToggleLink spaceSort = new ToggleLink(new Image(TypeImageBundle.I.get().space()), new Image(TypeImageBundle.I.get().space_dis())
			,Msg.consts.space(),Msg.consts.space());
	
	private DateBox modifiedDate = new DateBox();
	private SearchRenderWidget widget;
	
	private KeyDownHandler submitKeyboardListener = new SubmitKeyboardListener();
	public AdvSearchPanel(SearchRenderWidget widget){
		this.widget = widget;
		keyword.addFocusHandler(KeyCaptureListener.instance());
		spaceBox.addFocusHandler(KeyCaptureListener.instance());
		modifiedDate.getTextBox().addFocusHandler(KeyCaptureListener.instance());
		keyword.addBlurHandler(KeyCaptureListener.instance());
		spaceBox.addBlurHandler(KeyCaptureListener.instance());
		modifiedDate.getTextBox().addBlurHandler(KeyCaptureListener.instance());
		//so far only keyword can submit if users click enter
		keyword.addKeyDownHandler(submitKeyboardListener);
		
		modifiedDate.setFormat(new DateBox.DefaultFormat(DateTimeFormat.getFormat("yyyy/MM/dd")));
		
		FlexTable main = new FlexTable();
		FlowPanel t1 = new FlowPanel();
		t1.add(new Label(Msg.consts.filter()));
		FlowPanel t2 = new FlowPanel();
		t2.add(new Label(Msg.consts.type()));
		FlowPanel t3 = new FlowPanel();
		t3.add(new Label(Msg.consts.from_space()));
		FlowPanel t4 = new FlowPanel();
		t4.add(new Label(Msg.consts.sortby()));
		FlowPanel t5 = new FlowPanel();
		t5.add(new Label(Msg.consts.modified_date()));
		FlowPanel t6 = new FlowPanel();
//		Hr sep = new Hr();
//		sep.setStyleName(Css.SEPARATOR);
		t6.add(new HTML("<br>"));
		
		FlexTable filterPanel = new FlexTable();
		FlexTable typePanel = new FlexTable();
		FlexTable spacePanel = new FlexTable();
		FlexTable sortbyPanel = new FlexTable();
		FlexTable modifiedPanel = new FlexTable();
		
		filterPanel.setWidget(0, 0, keyword);
		filterPanel.setWidget(1, 0, anyWords);
		filterPanel.setWidget(2, 0, allWords);
		filterPanel.setWidget(3, 0, exactWords);
		
		anyWords.setValue(true);
		
		final ClickLink toggleNoneType = new ClickLink(Msg.consts.none_upper());
		toggleNoneType.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
					pageType.setEnable(false);
					spaceType.setEnable(false);
					attachmentType.setEnable(false);
					commentType.setEnable(false);
					pTagType.setEnable(false);
					sTagType.setEnable(false);
					userType.setEnable(false);
					widgetType.setEnable(false);
			}
		});
		final ClickLink toggleAllType = new ClickLink(Msg.consts.all());
		toggleAllType.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				pageType.setEnable(true);
				spaceType.setEnable(true);
				attachmentType.setEnable(true);
				commentType.setEnable(true);
				pTagType.setEnable(true);
				sTagType.setEnable(true);
				userType.setEnable(true);
				widgetType.setEnable(true);
			}
		});
		
		typePanel.setWidget(0, 0, pageType);
		typePanel.setWidget(0, 1, spaceType);
		typePanel.setWidget(1, 0, attachmentType);
		typePanel.setWidget(1, 1, commentType);
		typePanel.setWidget(2, 0, pTagType);
		typePanel.setWidget(2, 1, sTagType);
		typePanel.setWidget(3, 0, userType);
		typePanel.setWidget(3, 1, widgetType);
		
		FlowPanel togglePanel = new FlowPanel();
		togglePanel.add(toggleAllType);
		togglePanel.add(new Label("/"));
		togglePanel.add(toggleNoneType);
		typePanel.setWidget(4, 0, togglePanel);
		typePanel.getFlexCellFormatter().setColSpan(4, 0, 2);
		typePanel.getCellFormatter().setAlignment(4, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_MIDDLE);
		
		spacePanel.setWidget(0, 0, new Label(Msg.consts.space()));
		spacePanel.setWidget(0, 1, spaceBox);
		
		sortbyPanel.setWidget(0, 0, defaultSort);
		sortbyPanel.setWidget(0, 1, typeSort);
		sortbyPanel.setWidget(0, 2, spaceSort);
		defaultSort.addClickHandler(sortClickHandler);
		typeSort.addClickHandler(sortClickHandler);
		spaceSort.addClickHandler(sortClickHandler);
		defaultSort.setEnable(true);
		spaceSort.setEnable(false);
		typeSort.setEnable(false);
		
		modifiedPanel.setWidget(0, 0, new Label(Msg.consts.from()));
		modifiedPanel.setWidget(0, 1, modifiedDate);
		Button filterBtn = new Button(Msg.consts.search());
		filterBtn.addClickHandler(this);
		
		int row = 0;
		main.setWidget(row++, 0, t1);
		main.setWidget(row++, 0, filterPanel);
		main.setWidget(row++, 0, t2);
		main.setWidget(row++, 0, typePanel);
		main.setWidget(row++, 0, t3);
		main.setWidget(row++, 0, spacePanel);
		main.setWidget(row++, 0, t4);
		main.setWidget(row++, 0, sortbyPanel);
		main.setWidget(row++, 0, t5);
		main.setWidget(row++, 0, modifiedPanel);
		main.setWidget(row++, 0, t6);
		main.setWidget(row, 0, filterBtn);
		main.getCellFormatter().setAlignment(row++, 0, HasHorizontalAlignment.ALIGN_CENTER, HasVerticalAlignment.ALIGN_BOTTOM);
		
		t1.setStyleName(Css.HEADER);
		t2.setStyleName(Css.HEADER);
		t3.setStyleName(Css.HEADER);
		t4.setStyleName(Css.HEADER);
		t5.setStyleName(Css.HEADER);
		//t6.setStyleName(Css.HEADER);
		main.setStyleName(Css.ADV_SEARCH);
		
		spaceBox.setStyleName(Css.FORM_INPUT);
		keyword.setStyleName(Css.FORM_INPUT);
		modifiedDate.setStyleName(Css.FORM_INPUT);
		
		main.setWidth(WIDTH);
		filterPanel.setCellSpacing(4);
		typePanel.setCellSpacing(4);
		spacePanel.setCellSpacing(4);
		sortbyPanel.setCellSpacing(4);
		modifiedPanel.setCellSpacing(4);
		main.setCellPadding(0);
		main.setCellSpacing(0);
		this.setWidget(main);
	}
	/**
	 * @param keyword2
	 */
	public void setKeyword(String text) {
		this.keyword.setText(text);
	}
	public void onClick(ClickEvent event) {
		widget.search();
	}
	private class SortClickHandler implements ClickHandler{
		public void onClick(ClickEvent event) {
			Object sender = event.getSource();
			if(sender == defaultSort){
				typeSort.setEnable(false);
				spaceSort.setEnable(false);
			}else if(sender == typeSort){
				defaultSort.setEnable(false);
				spaceSort.setEnable(false);
			}else if(sender == spaceSort){
				typeSort.setEnable(false);
				defaultSort.setEnable(false);
			}
		}
		
	}
	
	private class SubmitKeyboardListener implements KeyDownHandler{

		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				widget.search();
			}
		}
	}
	/**
	 * @return
	 */
	public QueryModel getQuery() {
		QueryModel query = new QueryModel();
		query.keyword = keyword.getText();
		query.space = spaceBox.getText();
		
		if(modifiedDate.getValue() != null){
			query.from = DateTimeFormat.getFormat("yyyy/MM/dd").format(modifiedDate.getValue());
		}
		
		if(allWords.getValue())
			query.keywordType = SharedConstants.ADV_SEARCH_KEYWORD_ALL;
		if(anyWords.getValue())
			query.keywordType = SharedConstants.ADV_SEARCH_KEYWORD_ANY;
		if(exactWords.getValue())
			query.keywordType = SharedConstants.ADV_SEARCH_KEYWORD_EXACT;
		
		if(defaultSort.isEnable())
			query.sortBy = 0;
		if(typeSort.isEnable())
			query.sortBy = SharedConstants.ADV_SEARCH_GROUP_TYPE;
		if(spaceSort.isEnable())
			query.sortBy = SharedConstants.ADV_SEARCH_GROUP_SPACE;

		query.type = 0;
		if(pageType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_PAGE;
		if(spaceType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_SPACE;
		if(commentType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_COMMENT;
		if(attachmentType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_ATTACHMENT;
		if(pTagType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_TAGONPAGE;
		if(sTagType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_TAGONSPACE;
		if(userType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_USER;
		if(widgetType.isEnable())
			query.type |= SharedConstants.ADV_SEARCH_INDEX_WIDGET;
		
		if(query.type == SharedConstants.ADV_SEARCH_INDEX_ALL)
			query.type = 0;
		
		return query;
	}

}
