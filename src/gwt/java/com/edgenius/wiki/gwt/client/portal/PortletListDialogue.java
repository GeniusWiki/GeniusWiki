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
package com.edgenius.wiki.gwt.client.portal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PortalModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.render.SearchRenderWidget.TypeImageBundle;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.CandidateListPanel;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Pagination;
import com.edgenius.wiki.gwt.client.widgets.PaginationListener;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
/**
 * Get available spaces, my page (watched, draft, recently, favourite etc.) list and allow user choose
 * one or more to put on the dashboard page.
 * 
 * @author Dapeng.Ni
 */
public class PortletListDialogue extends DialogBox implements SelectionHandler<Integer> {
 
	private static final int SPACE_LIST_SIZE = 10;
	private static final int TAB_POPULAR = 0;
	private static final int TAB_WIDGET = 1;
	private static final int TAB_SEARCH = 2;
	
	private MessageWidget message = new MessageWidget();
	private MessageWidget rsMessage = new MessageWidget();
	private Pagination pagination = new Pagination();
	private Label summary = new Label();
	private HintTextBox filter = new HintTextBox(Msg.consts.input_keyword());
	private CandidateListPanel candidatePanel = new CandidateListPanel();
	private DecoratedTabPanel tabPanel = new DecoratedTabPanel();
	private Vector<ListDialogueListener> listeners = new Vector<ListDialogueListener>();
	private String keyword;
	private Image widgetBusyImg = IconBundle.I.loading();
	private Image popularBusyImg = IconBundle.I.loading();
	private Image searchBusyImg = IconBundle.I.loading();
	
	private boolean widgetInit = false;
	//three Tab in deck board
	private ZebraTable widgetTable = new ZebraTable();
	private ZebraTable popularTable = new ZebraTable();
	private ZebraTable searchRsTable = new ZebraTable();
	
	//********************************************************************
	//               Methods
	//********************************************************************
	public PortletListDialogue(boolean anonymousLogin){
		
		this.setText(Msg.consts.more_space_dialog_title());
		this.setIcon(new Image(IconBundle.I.get().application_add()));
		
		FlexTable topPanel = new FlexTable();
		Label label1 = new Label(Msg.consts.search_space_widget());
		label1.setStyleName(Css.FORM_LABEL);
		topPanel.setWidget(0, 0, label1);
		label1.setWordWrap(false);
		
		filter.setStyleName(Css.SEARCH_INPUT);
		topPanel.setWidget(0, 1, filter);
		
		filter.setTitle(Msg.consts.input_keyword());
		filter.addKeyDownHandler(new KeyDownHandler() {
			public void onKeyDown(KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					keyword = filter.getText();
					if (keyword == null || keyword.trim().length() == 0)
						return;
					
					searchBusyImg.setVisible(true);
					SearchControllerAsync action = ControllerFactory.getSearchController();
					//first page, return 10
					action.searchWidgetSpace(keyword,0,SPACE_LIST_SIZE, new SpaceSearchAsync());
					tabPanel.selectTab(TAB_SEARCH);
				}
			}
		});
		
		topPanel.getFlexCellFormatter().setWidth(0, 0, "1%");
		topPanel.getFlexCellFormatter().setWordWrap(0, 0, false);
		topPanel.getFlexCellFormatter().setWidth(0, 1, "97%");
		int topPanelColume = 2;
		
		if(!anonymousLogin){
			//so far, don't allow anonyomus to create widgets
			ClickLink newWidgetLn = new ClickLink(Msg.consts.create_widget());
			newWidgetLn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					PortletCreateDialog dia = new PortletCreateDialog(null);
					//inherent all listener from parent, so that new portlet can add to dashboard
					for (ListDialogueListener listener : listeners) {
						dia.addListDialogueListener(listener);
					}
					//close PortletListDialog -- don't ask 2 dialog open at same time
					hidebox();
					dia.showbox();
				}
			});
			topPanel.setWidget(0, 2,new Image(IconBundle.I.get().star()));
			topPanel.setWidget(0, 3,newWidgetLn);
			topPanel.getFlexCellFormatter().setWidth(0, 2, "1%");
			topPanel.getFlexCellFormatter().setWidth(0, 3, "1%");
			topPanel.getFlexCellFormatter().setWordWrap(0, 3, false);
			topPanelColume = 4;
		}
		
		//candidate list after user chooses
		topPanel.setWidget(1, 0, candidatePanel);
		topPanel.getFlexCellFormatter().setColSpan(1, 0, topPanelColume);
		
		Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				hidebox();
				//collect data(PortletModel) from candidate panel 
				List values = candidatePanel.getCandidates();
				
				//reverse value: because the portlet insert before the one which has same row number, 
				//so far all portlets row are all 0. So, reverse them, so that first chosen portlet will display ahead others
				List portlets = new ArrayList();
				int size = values.size();
				for(int idx=size-1;idx>=0;idx--){
					portlets.add(values.get(idx));
				}
				//fire event, tell observer to update portlet
				for(Iterator<ListDialogueListener> iter = listeners.iterator();iter.hasNext();){
					ListDialogueListener lis =  iter.next();
					lis.dialogClosed(PortletListDialogue.this,portlets);
				}
			}
		});
		Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				PortletListDialogue.this.hidebox();
			}
		});
		
		
		getButtonBar().add(cancelBtn);
		getButtonBar().add(okBtn);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Popular Tab
		VerticalPanel pPanel = new VerticalPanel();
		pPanel.add(popularBusyImg);
		popularBusyImg.setVisible(true);
		pPanel.add(popularTable);
		initTable(popularTable, false);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Search Tab
		VerticalPanel rPanel = new VerticalPanel();
		HorizontalPanel h1 = new HorizontalPanel();
		pagination.addPaginationListener(new SearchPaginationCallback());
		pagination.setPageSize(SPACE_LIST_SIZE);
		h1.add(pagination);
		h1.add(summary);
		h1.setSpacing(5);
	
		rPanel.add(searchBusyImg);
		searchBusyImg.setVisible(false);
		rPanel.add(searchRsTable);
		rPanel.add(rsMessage);
		rPanel.add(h1);
		initTable(widgetTable, true);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Widget Tab
		VerticalPanel wPanel = new VerticalPanel();
		widgetBusyImg.setVisible(false);
		wPanel.add(widgetBusyImg);
		wPanel.add(widgetTable);
		initTable(widgetTable, false);
		
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               Tab panel
		tabPanel.add(pPanel, Msg.consts.popular_spaces());
		tabPanel.add(wPanel, Msg.consts.widgets());
		tabPanel.add(rPanel, Msg.consts.search_result());
		tabPanel.selectTab(TAB_POPULAR);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(topPanel);
		panel.add(tabPanel);
		
		pPanel.setHeight("1%");
		wPanel.setHeight("1%");
		rPanel.setHeight("1%");
		tabPanel.setWidth("100%");
		panel.setSize("100%", "98%");
		panel.setCellHeight(tabPanel, "100%");
		
		topPanel.setStyleName(Css.BOX);
		tabPanel.addSelectionHandler(this);
		panel.setSpacing(5);
		
		this.addStyleName(Css.PORTLET_DIALOG_BOX);
		
		this.setWidget(panel);
		
		SpaceControllerAsync spaceController = ControllerFactory.getSpaceController();
		spaceController.getSpacesInfo(null, SPACE_LIST_SIZE, null,new PopularCallback());
		
	}


	public void onSelection(SelectionEvent<Integer> event) {
		Integer tabIndex = event.getSelectedItem();
		if(tabIndex == TAB_WIDGET && !widgetInit){
			//only get from server once. 
			widgetInit = true;
			widgetBusyImg.setVisible(true);
			PortalControllerAsync portalController = ControllerFactory.getPortalController();
			portalController.getListedWidgets(SPACE_LIST_SIZE,0,new WidgetCallback());
		}		
	}
	
	public void addListDialogueListener(ListDialogueListener listener){
		listeners.add(listener);
	}

	//********************************************************************
	//               private method
	//********************************************************************
	private void fillListTable(ZebraTable table,List<PortletModel> portletList, boolean showLogo) {
		
		int row = rebuildTable(table,showLogo);
		
		int col;

		for(Iterator<PortletModel> iter =portletList.iterator();iter.hasNext();){
			final PortletModel sModel = iter.next();
			col = 0;
			if(showLogo){
				Image logo; 
				if(sModel.type == PortletModel.SPACE){
					logo = new Image(TypeImageBundle.I.get().space());
					logo.setTitle(Msg.consts.space());
				}else{
					logo = new Image(TypeImageBundle.I.get().widget());
					logo.setTitle(Msg.consts.widget());
				}
				table.setWidget(row, col++, logo);
			}
			//spaceTitle or widget Title
			table.setWidget(row, col++, new Label(sModel.title));
			
			ClickLink add = new ClickLink(Msg.consts.add());
			add.setTitle(Msg.params.add_widget_title(sModel.title));
			add.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					addCandidate(sModel);
				}
			});
			table.setWidget(row, col++, new HTML(sModel.description));
			table.setWidget(row,col++, add);
			row++;
		}

		initTable(table, showLogo);
		
	}


	/**
	 * @param table
	 * @param showLogo
	 */
	private void initTable(ZebraTable table, boolean showLogo) {
		int col;
		col = 0;
		table.addStyleName(Css.SPACE_LIST);
		if(showLogo){
			table.getColumnFormatter().setStyleName(col++, Css.SPACE_LOGO);
		}
		table.getColumnFormatter().setStyleName(col++, Css.SPACE_NAME);
		table.getColumnFormatter().setStyleName(col++, Css.SPACE_DESC);
		table.getColumnFormatter().setStyleName(col++, Css.SPACE_FUNC);
	}
	/**
	 * @param table
	 */
	private int rebuildTable(ZebraTable table,boolean showLogo) {
		//need PortletModel in list
		int rowSize = table.getRowCount();
		for(int idx=rowSize-1;idx>=0;idx--)
			table.removeRow(idx);
		
		int row = 0,col=0;
		//show logo, then first column is log
		if(showLogo)
			++col;
		table.setWidget(row, col++, new Label(Msg.consts.name()));
		table.setWidget(row, col++, new Label(Msg.consts.description()));
		table.setWidget(row, col++, new Label(Msg.consts.action()));
		
		return ++row;
	}

	private void addCandidate(final PortletModel sModel) {
		String title = sModel.title;
		
		//init some value for portlet
		sModel.column = 0;
		sModel.row = 0;
		
		candidatePanel.addCandidate(title,sModel);
	}
	/**
	 * @param type 
	 * @param model
	 * @return
	 */
	private List<PortletModel> convertSpacesToPortletModel(List<SpaceModel> spaceList) {
		List<PortletModel> portletList = new ArrayList<PortletModel>();
		if(spaceList == null)
			return portletList;
		
		for(Iterator<SpaceModel> iter = spaceList.iterator();iter.hasNext();){
			SpaceModel space =  iter.next();
			PortletModel portlet = new PortletModel();
			portlet.key = space.unixName;
			portlet.title = space.name;
			portlet.description = space.description;
			portlet.type = PortletModel.SPACE;
			portletList.add(portlet);
		}
		return portletList;
	}
	
	/**
	 * @param results
	 * @return
	 */
	private List<PortletModel> convertSearchItemsToPortletModel(List<SearchResultItemModel> results) {
		
		List<PortletModel> portletList = new ArrayList<PortletModel>();
		for(Iterator<SearchResultItemModel>  iter = results.iterator();iter.hasNext();){
			SearchResultItemModel item = iter.next();
			PortletModel portlet = new PortletModel();
			//this could be SpaceUname or widget key
			portlet.key = item.spaceUname;
			portlet.title = item.title;
			portlet.description = item.desc;
			if(item.type == SharedConstants.SEARCH_WIDGET){
				portlet.type = item.itemUid; //widget Type(class name of WidgetTemplate)
			}else{
				portlet.type = PortletModel.SPACE;
			}
			portletList.add(portlet);
		}
		
		return portletList;
	}
	//********************************************************************
	//               Private Class 
	//********************************************************************
	private class WidgetCallback implements AsyncCallback<PortalModel>{
		public void onFailure(Throwable error) {
			widgetBusyImg.setVisible(false);
			GwtClientUtils.processError(error);
		}
		/*
		 * Spaces list success return.
		 */
		public void onSuccess(PortalModel model) {
			widgetBusyImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			fillListTable(widgetTable,model.portlets,false);
		}
	}
	private class PopularCallback implements AsyncCallback<SpaceListModel>{
		public void onFailure(Throwable error) {
			popularBusyImg.setVisible(false);
			GwtClientUtils.processError(error);
		}
		/*
		 * Spaces list success return.
		 */
		public void onSuccess(SpaceListModel model) {
			popularBusyImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			List<PortletModel> portlets = convertSpacesToPortletModel(model.spaceList);
			fillListTable(popularTable,portlets,false);
		}

	}
	
	public class SpaceSearchAsync implements AsyncCallback<SearchResultModel>{
		public void onFailure(Throwable error) {
			searchBusyImg.setVisible(false);
			GwtClientUtils.processError(error);
			
		}

		public void onSuccess(SearchResultModel model) {
			searchBusyImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
			
			
			if(model == null ||model.results == null || model.results.size() == 0){
				rsMessage.info(Msg.consts.no_result());
				summary.setText("");
				pagination.setTotalItem(0);
				pagination.setCurrentPage(1);
				
				//anyway, clean last time search result.
				int rowSize = searchRsTable.getRowCount();
				for(int idx=rowSize-1;idx>=0;idx--)
					searchRsTable.removeRow(idx);

				return;
			}
			rsMessage.cleanMessage();

			pagination.setTotalItem(model.totalItems);
			pagination.setCurrentPage(model.currPage);
			summary.setText(Msg.params.total_result(model.totalItems+""));
			
			fillListTable(searchRsTable, convertSearchItemsToPortletModel(model.results),true);
		}

	}
	private class SearchPaginationCallback implements PaginationListener{
		public void pageChanging(int pageno) {
			if (keyword == null || keyword.trim().length() == 0)
				return;
			SearchControllerAsync action = ControllerFactory.getSearchController();
			action.searchWidgetSpace(keyword,pageno,SPACE_LIST_SIZE,new SpaceSearchAsync());
		}
	}
}
