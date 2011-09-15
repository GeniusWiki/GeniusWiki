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
package com.edgenius.wiki.gwt.client.page.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.SearchResultItemModel;
import com.edgenius.wiki.gwt.client.model.SearchResultModel;
import com.edgenius.wiki.gwt.client.model.SpaceListModel;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.SearchControllerAsync;
import com.edgenius.wiki.gwt.client.server.SpaceControllerAsync;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.Pagination;
import com.edgenius.wiki.gwt.client.widgets.PaginationListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HorizontalSplitPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Space <=> Pages dialog, used in "Move to" and "Copy to" function.
 * @author Dapeng.Ni
 */
public class SpacePagesDialog extends DialogBox implements ClickHandler, AsyncCallback<PageModel> {
	private static final int SPACE_LIST_SIZE = 10;
	public static final int MOVE =1;
	public static final int COPY =2;
	
	private PageMain main;
	private Button okButton = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
	private Button cancelButton = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	private CheckBox withChildrenButton = new CheckBox(Msg.consts.with_children());
	private TreePanel treePanel;
	private MessageWidget message = new MessageWidget();
	private Label target = new Label(); 
	//1:move 2:copy
	private int type;
	
	public SpacePagesDialog(String title, PageMain main, int type){
		this.setText(title);
		if(type == MOVE){
			this.setIcon(new Image(IconBundle.I.get().page_go()));
		}else{
			this.setIcon(new Image(IconBundle.I.get().page_copy()));
		}
		
		this.main = main;
		this.type = type;
		
		
		treePanel = new TreePanel(main);
		okButton.addClickHandler(this);
		cancelButton.addClickHandler(this);
		
		getButtonBar().add(cancelButton);
		getButtonBar().add(okButton);
		
		FlowPanel targetPanel = new FlowPanel();
		targetPanel.setStyleName(Css.TARGET);
		targetPanel.add(new Label(Msg.consts.to()+": "));
		targetPanel.add(target);
		
		targetPanel.add(target);
		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(message);
		mainPanel.add(treePanel);
		//TODO: temporarily don't add this for UI pretty reason
//		mainPanel.add(targetPanel);
		
		this.addStyleName(Css.COPY_MOVE_DIALOG);
		setWidget(mainPanel);
		
	}

	/* OK button, Cancel button click event
	 */
	public void onClick(ClickEvent event) {
		Object widget = event.getSource();
		if(widget == okButton){
			okButton.setEnabled(false);
			//it is better get from LoationTree rather than treePanel.getSpace(), this ensure consistent with selected page in PageTree
			String spaceUname = treePanel.locationTree.getSpaceUname();
			if(spaceUname == null || spaceUname.trim().length() == 0){
				Window.alert(Msg.consts.choose_space_first());
				return;
			}
			String targetParentPageUuid = treePanel.getChosenPageUuid();
			PageControllerAsync action = ControllerFactory.getPageController();
			boolean withChildren = withChildrenButton.getValue(); 
			if(type == MOVE){
				action.move(main.getSpaceUname(), main.getPageUuid(), spaceUname, targetParentPageUuid,withChildren ,this);
			}else if(type == COPY){
				action.copy(main.getSpaceUname(), main.getPageUuid(), spaceUname, targetParentPageUuid,withChildren ,this);
			}
		}else if(widget == cancelButton){
			this.hidebox();
		}
	}
	
	/**
	 * Move or copy failure
	 */
	public void onFailure(Throwable error) {
		okButton.setEnabled(true);
		GwtClientUtils.processError(error);
	}
	/**
	 * Move or copy success
	 */
	public void onSuccess(PageModel model) {
		okButton.setEnabled(true);
		this.hidebox();

		if(!GwtClientUtils.preSuccessCheck(model,message)){
			return;
		}
		main.viewPanel.fillPanel(model);
	}
	
	/**
	 * This panel has 2 vertical panel. Left is available spaces, and right is page tree of this space. 
	 * It is useful for choosing target when moving/copying page.
	 * @author Dapeng.Ni
	 */
	private class TreePanel extends SimplePanel implements AsyncCallback<SpaceListModel>, PaginationListener{

		//maximum spaces list in left panel 
		
		private FlexTable spaceTable = new FlexTable();
		private PageTreeWidget locationTree;
		private String chosenPageUuid = null;
		private PageMain main;
		private  HintTextBox spaceFilterBox = new HintTextBox(Msg.consts.input_keyword());
		private Label pageTitle = new Label(Msg.consts.page_tree());
		private MessageWidget rsMessage = new MessageWidget();
		private Pagination pagination = new Pagination();
		private String keyword;
		public TreePanel(PageMain main){
			
			this.main = main;
			pagination.addPaginationListener(this);
			pagination.setPageSize(SPACE_LIST_SIZE);
			
			//page tree
			locationTree = new PageTreeWidget();
			locationTree.addSelectionHandler(new SelectionHandler<TreeItem>(){
				public void onSelection(SelectionEvent<TreeItem> event) {
					chosenPageUuid = (String) event.getSelectedItem().getUserObject();
					target.setText("Space " + getSpace().getSpaceUname() + " Parent page " + event.getSelectedItem().getText());
				}
				
			});
			
			FlexTable pagePanel = new FlexTable();
			DockPanel funcPanel = new DockPanel();
			funcPanel.add(pageTitle, DockPanel.WEST);
			HorizontalPanel treeBtns = locationTree.getFunctionButtons();
			funcPanel.add(treeBtns, DockPanel.EAST);
			funcPanel.setCellHorizontalAlignment(treeBtns, HasHorizontalAlignment.ALIGN_RIGHT);
			
			pagePanel.setWidget(0, 0, funcPanel);
			pagePanel.setWidget(1, 0, locationTree);
			pagePanel.getFlexCellFormatter().setStyleName(0, 0, Css.HEADER);
			
			funcPanel.setWidth("100%");
			pagePanel.setWidth("100%");
			pagePanel.setCellPadding(4);
			pagePanel.setCellSpacing(0);
			//space
			spaceFilterBox.addKeyDownHandler(new KeyDownHandler() {
				public void onKeyDown(KeyDownEvent event) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						keyword = spaceFilterBox.getText();
						if (keyword == null || keyword.trim().length() == 0)
							return;
						SearchControllerAsync action = ControllerFactory.getSearchController();
						action.searchSpace(keyword,0,SPACE_LIST_SIZE, new SpaceSearchAsync());
					}
					
				}
			});

			ClickLink resetSearch = new ClickLink(Msg.consts.reset());
			resetSearch.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					spaceFilterBox.setText("");
					//return default space list
					SpaceControllerAsync spaceAction = ControllerFactory.getSpaceController();
					spaceAction.getSpacesInfo(null, SPACE_LIST_SIZE,null, TreePanel.this);					
				}
				
			});
			HorizontalPanel topPanel = new HorizontalPanel();
			spaceFilterBox.setStyleName(Css.SEARCH_INPUT);
			topPanel.add(spaceFilterBox);
			topPanel.add(resetSearch);
			
			FlexTable spacePanel = new FlexTable();
			int row=0;
			spacePanel.setWidget(row++, 0, new Label(Msg.consts.space_list()));
			spacePanel.setWidget(row++, 0, topPanel);
			spacePanel.setWidget(row++, 0, spaceTable);
			spacePanel.setWidget(row++, 0, rsMessage);
			spacePanel.setWidget(row++, 0, pagination);
			
			spacePanel.getFlexCellFormatter().setStyleName(0, 0, Css.HEADER);
			spacePanel.setCellPadding(4);
			spacePanel.setCellSpacing(0);
			spacePanel.setWidth("100%");
			//main
			HorizontalSplitPanel mainPanel = new HorizontalSplitPanel();
			mainPanel.setLeftWidget(spacePanel);
			mainPanel.setRightWidget(pagePanel);
			mainPanel.setPixelSize(600, 500);
			spaceTable.setStyleName(Css.SPACE_LIST_TABLE);
		
			setWidget(mainPanel);
			this.setStyleName(Css.SPLIT_PANEL_TABLE);
			//call async: for space list and page tree
			SpaceControllerAsync spaceAction = ControllerFactory.getSpaceController();
			spaceAction.getSpacesInfo(null, SPACE_LIST_SIZE, null,this);
		}

		public void pageChanging(int pageno) {
			if (keyword == null || keyword.trim().length() == 0)
				return;
			SearchControllerAsync action = ControllerFactory.getSearchController();
			action.searchSpace(keyword,pageno,SPACE_LIST_SIZE, new SpaceSearchAsync());
		}
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
			
		}
		//get initial space list from database
		public void onSuccess(SpaceListModel listModel) {

			if(!GwtClientUtils.preSuccessCheck(listModel,message)){
				return;
			}
			//first time initial, to show current space page tree
			resetSpaces(main.getSpaceUname(), listModel.spaceList);
			
		}
		//********************************************************************
		//               Private methods
		//********************************************************************
		private SpaceItem getSpace() {
			int row = spaceTable.getRowCount();
			for(int idx=0;idx<row;idx++){
				SpaceItem space = (SpaceItem) spaceTable.getWidget(idx, 0);
				if(space.isSelected()){
					return space;
				}
			}
			return null;
		}


		private String getChosenPageUuid() {
			return chosenPageUuid;
		}

		private TreeItem getTreeRoot(String spaceUname) {
			TreeItem root = new TreeItem();
			root.setUserObject(null);
			//XXX:hardcode
			root.setText(spaceUname+ ":Root");
			return root;
		}



		private void resetSpaces(String spaceUname, List<SpaceModel> list) {
			//clear table
			int rowSize = spaceTable.getRowCount();
			for(int idx=rowSize-1;idx>=0;idx--)
				spaceTable.removeRow(idx);
			
			if(list != null && list.size() > 0){
				
				SpaceItem selectedSpace = null;
				Iterator<SpaceModel> iter = list.iterator();
				for(int row=0;iter.hasNext();row++){
					SpaceModel model  =iter.next();
					SpaceItem space = new SpaceItem(model.name, model.unixName);
					space.setText(model.unixName);
					space.setSelected(false);
					if(spaceUname != null && spaceUname.equalsIgnoreCase(model.unixName)){
						selectedSpace = space;
					}
					spaceTable.setWidget(row,0,space);
				}
				
				if(selectedSpace == null){
					//no space selected then choose first one as selected
					selectedSpace = (SpaceItem) spaceTable.getWidget(0, 0);
				}
				selectedSpace.setSelected(true);
				locationTree.createTree(getTreeRoot(selectedSpace.getSpaceUname()),selectedSpace.getSpaceUname(),null);
			}
		}
		//********************************************************************
		//               Private class
		//********************************************************************
		private class SpaceSearchAsync implements AsyncCallback<SearchResultModel>{
			public void onFailure(Throwable error) {
				GwtClientUtils.processError(error);
			}
			public void onSuccess(SearchResultModel model) {

				if(!GwtClientUtils.preSuccessCheck(model,message)){
					return;
				}
				
				if(model == null ||model.results == null || model.results.size() == 0){
					rsMessage.info("No matched result");
					return;
				}
				rsMessage.cleanMessage();
				
				//change SearchResultItemModel to spaceModel arrayList
				List<SpaceModel> list = new ArrayList<SpaceModel>();
				for(SearchResultItemModel item : model.results){
					SpaceModel space = new SpaceModel();
					space.unixName = item.spaceUname;
					space.name = item.title;
					space.description = item.desc;
					list.add(space);
				}
				resetSpaces(main.getSpaceUname(), list);
				pagination.setTotalItem(model.totalItems);
				pagination.setCurrentPage(model.currPage);

			}
			
		}

		private class SpaceItem extends Label implements ClickHandler{
			private String name;
			private String spaceUname;
			private boolean isSelected;
			public SpaceItem(String name, String spaceUname){
				super(name);
				this.name = name;
				this.spaceUname = spaceUname;
				this.addClickHandler(this);
				this.addClickHandler(this);
			}

			/**
			 * @param b
			 */
			public void setSelected(boolean b) {
				isSelected = b;
				if(b)
					setStyleName(Css.SELECTED);
				else
					setStyleName(Css.DESELECTED);
			}
			
			public boolean isSelected() {
				return isSelected;
			}
			
			public void onClick(ClickEvent event) {
				SpaceItem space = (SpaceItem) event.getSource();
				int row = spaceTable.getRowCount();
				//deselect all space items
				for(int idx=0;idx<row;idx++){
					SpaceItem item = (SpaceItem) spaceTable.getWidget(idx, 0);
					item.setSelected(false);
				}
				
				space.setSelected(true);
				locationTree.createTree(getTreeRoot(space.getSpaceUname()),space.getSpaceUname(),null);
				//important, when user choose different space, pageUid must be reset as well.
				chosenPageUuid = null;
				
				target.setText("Space " + space.getName() + " root");
			}


			public String getName() {
				return name;
			}

			public String getSpaceUname() {
				return spaceUname;
			}
		}

	
	}

}
