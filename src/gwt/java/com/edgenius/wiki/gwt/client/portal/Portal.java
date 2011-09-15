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

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.allen_sauer.gwt.log.client.Log;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.home.CreateSpaceDialogue;
import com.edgenius.wiki.gwt.client.home.porlet.ActivityPortlet;
import com.edgenius.wiki.gwt.client.home.porlet.MessagePortlet;
import com.edgenius.wiki.gwt.client.home.porlet.MyPagePortlet;
import com.edgenius.wiki.gwt.client.home.porlet.QuickNotePortlet;
import com.edgenius.wiki.gwt.client.home.porlet.SpacePortlet;
import com.edgenius.wiki.gwt.client.model.GeneralModel;
import com.edgenius.wiki.gwt.client.model.PortalModel;
import com.edgenius.wiki.gwt.client.model.PortletModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.FunctionWidget;
import com.edgenius.wiki.gwt.client.render.LogoRenderWidget;
import com.edgenius.wiki.gwt.client.render.RenderWidget;
import com.edgenius.wiki.gwt.client.render.RenderWidgetListener;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.ListDialogueListener;
import com.edgenius.wiki.gwt.client.widgets.PortletContext;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class Portal extends SimplePanel implements AsyncCallback<PortalModel>,PortletListener, DragHandler, ListDialogueListener, RenderWidget {

	private AbsolutePanel boundaryPanel = new AbsolutePanel();
	private HorizontalPanel columnsPanel = new HorizontalPanel();
	private PickupDragController dragController;
	private AsyncCallback<Boolean> saveAsyncCallback = new SaveAsyncCallback();
	private int totalColumns;
	private PortalVisitor visitor;
	private UserModel viewer;
	private Image indicator = IconBundle.I.indicator();
	private RenderWidgetListener listener;
	
	private FunctionWidget functionBtnBar;
	private String componentKey;
	private int column;
	
	public Portal(PortalVisitor visitor, int column, boolean showLogo) {
		this.visitor = visitor;
		this.column = column;
		
		visitor.setPortal(this);
		
		functionBtnBar = new FunctionWidget(null);
		VerticalPanel panel = new VerticalPanel();
		
		//logo and function bar
		FlowPanel logoBar = new FlowPanel();
		if(showLogo){
			LogoRenderWidget logo = new LogoRenderWidget();
			logoBar.add(logo);
		}
		logoBar.add(functionBtnBar);
		logoBar.setWidth("100%");
		panel.add(logoBar);
		
		panel.setCellVerticalAlignment(logoBar, HasVerticalAlignment.ALIGN_BOTTOM);
		DOM.setElementAttribute(panel.getElement(), "width", "100%");

		boundaryPanel.setSize("100%", "100%");
		boundaryPanel.addStyleName(Css.PORTAL);
		columnsPanel.addStyleName(Css.PORTAL_CONTAINER);
		boundaryPanel.add(columnsPanel);
		panel.add(boundaryPanel);
		
		this.setWidget(panel);
	}

	public void onLoad(String widgetKey, UserModel user, RenderWidgetListener listener) {
		this.listener = listener;
		this.componentKey = widgetKey;
		columnsPanel.clear();
		columnsPanel.add(indicator);
			
		//dragController need refresh one. otherwise, switch back PageMain, it need unregisterContrller(). but this method never call.
		dragController = new PickupDragController(boundaryPanel, false);
		dragController.setBehaviorDragStartSensitivity(8);
		dragController.addDragHandler(this);
		
		if(listener != null)
			listener.onLoading(componentKey);
		
		functionBtnBar.loadDashboardFunc(user, this);
		visitor.fetchPortlet(column);
	}

	public void onUserChanged(UserModel user) {
		//initial status: assume not user login
		Log.info("Portal get user change message " + user);
		functionBtnBar.loadDashboardFunc(user, this);
	}

	public AsyncCallback<PortalModel> getLoadAsyncCallback(){
		return this;
	}
	public AsyncCallback<Boolean> getSaveAsyncCallback(){
		return saveAsyncCallback;
	}
	
	
	public void onFailure(Throwable error) {
		GwtClientUtils.processError(error);
		if(listener != null)
			listener.onFailedLoad(componentKey, error.getMessage());
	}
	/*
	 * List home page success
	 */
	public void onSuccess(PortalModel model) {
		if(!GwtClientUtils.preSuccessCheck(model,null)){
			columnsPanel.add(ErrorCode.getMessage(model.errorCode, model.errorMsg));
			if(listener != null)
				listener.onFailedLoad(componentKey, ErrorCode.getMessageText(model.errorCode, model.errorMsg));
			return;
		}
		
		//TODO: at moment - it is not necessary to pass meaningful text - just tell listener, it is success with non-empty content
		if(listener != null)
			listener.onSuccessLoad(componentKey, "Portal");
		
		viewer = model.viewingUser;
		
		//failure tolerance: if totalColumns does not set on server side. then use default value
		totalColumns = model.totalColumns>0?model.totalColumns:SharedConstants.DEFAULT_PORTAL_COLUMNS;
		initColumns(model.totalColumns);
		addPortlets(model.portlets, true);
		//above portlet columns and row is just come form server side. For column, it use
		//failure tolerance to reset it columns if it is overflow. But row, it only arrange
		//by order,e.g., row could be 2,3,5. They could display on correct order 2 < 3 < 5. But these row number
		//can not be used in remove event, which need the real row now to get PortletPanel. So here 
		//reset all columns and rows according to the real location in page.
		resetPositions(false);
			
	}


	public int getTotalColumns() {
		return totalColumns;
	}

	public void setTotalColumns(int totalColumns) {
		this.totalColumns = totalColumns;
	}
	
	public void addPortlets(List<PortletModel> portlets,  boolean render) {
		//initialised portlets
		for(Iterator<PortletModel>  iter = portlets.iterator();iter.hasNext();){
			PortletModel portletModel = iter.next();
			Portlet portlet  = null;
			if(portletModel.type == PortletModel.SPACE)
				portlet = new SpacePortlet();
			else if(portletModel.type == PortletModel.DRAFT_LIST
					||portletModel.type == PortletModel.WATCHED_LIST
					||portletModel.type == PortletModel.FAVORITE_LIST){
				portlet = new MyPagePortlet();
			}else if(portletModel.type == PortletModel.MESSAGE_BOARD){
				portlet = new MessagePortlet();
			}else if(portletModel.type == PortletModel.QUICK_BOARD){
				portlet = new QuickNotePortlet();
			}else if(portletModel.type == PortletModel.ACTIVITYLOG_BOARD){
				portlet = new ActivityPortlet();
			}else{
				portlet = new Portlet();
			}
			
			portlet.addPortletListener(this);
			PortletContext context = new PortletContext();
			//for failure tolerance : to check it does not beyond boundary.
			//totalColumns start from 1 (size), column start from 0 (index)
			context.setColumn(portletModel.column >= totalColumns?totalColumns -1:portletModel.column);
			context.setRow(portletModel.row);
			context.addAttribute(Portlet.ATTR_MODEL, portletModel);
			portlet.init(context);
			portlet.setViewUser(viewer);

			dragController.makeDraggable(portlet,portlet.getDragHandler());
			if(render){
				//render portlet: it need space security and will invoke a RPC call. 
				portlet.render();
			}
			ColumnPanel colPanel = (ColumnPanel) columnsPanel.getWidget(context.getColumn());
			colPanel.removeInvisibleHolder();
			//retrieve all cell in this column, and insert cell to correct position according to row number
			int rowNum = colPanel.getWidgetCount();
			if(rowNum == 0)
				colPanel.add(portlet);
			else{
				for(int idx=0;idx<rowNum;idx++){
					Widget w= colPanel.getWidget(idx);
					if(!(w instanceof Portlet))
						continue;
					Portlet p = (Portlet) w;
					boolean insert = false;
					if(p.getContext().getRow() >= portlet.getContext().getRow()){
						colPanel.insert(portlet, idx);
						insert = true;
						break;
					}
					if(!insert)
						//maximum so far, just append to last one.
						colPanel.add(portlet);
				}
			}
			
		}
		//need check if some column is empty
		int colSize = columnsPanel.getWidgetCount();
		for(int idx=0;idx<colSize;idx++){
			ColumnPanel colPanel = (ColumnPanel) columnsPanel.getWidget(idx);
			colPanel.tryInvisibleHolder();
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//              method from Add Space Dialog Listener
	public void dialogClosed(DialogBox sender, List<? extends GeneralModel> values) {
		//list should be PortletModel
		if(values != null && values.size() > 0){
			if(sender instanceof CreateSpaceDialogue){
				//don't need render: it will jump to space home page, so that it is no need to render.
				addPortlets((List<PortletModel>)values, false);
			}else{
				//PortletListDialogue or PortletCreateDialog
				addPortlets((List<PortletModel>)values, true);
			}
			resetPositions(true);		
		}
		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//              method from Portlet listener
	// remove sender portlet from home page
	public void close(Portlet sender) {
		int cs = columnsPanel.getWidgetCount();

		int row = sender.getContext().getRow();
		int col = sender.getContext().getColumn();
		if(col < cs){
			ColumnPanel colPanel = (ColumnPanel) columnsPanel.getWidget(col);
			int rs = colPanel.getWidgetCount();
			if(row < rs){
				Portlet portlet = (Portlet) colPanel.getWidget(row);
				colPanel.remove(portlet);
				resetPositions(true);
			}
		}
		
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//                       Methods from DragHandler
	public void onDragEnd(DragEndEvent event) {
		//after drap and drop need reset portletContext row/column information.
		resetPositions(false);
		
	}

	public void onDragStart(DragStartEvent event) {}
	public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {	}
	public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {	}
	//********************************************************************
	//               private methods
	//********************************************************************
	/*
	 * This method will reset Portlet column and row information according to its real position in dashboard.
	 * This method also update login user's setting of layout in server side. 
	 * 
	 * @param forceSave true let this method force to save layout to server side. Otherwise, saving only if the position have
	 * modification comparing with original value
	 */
	private void resetPositions(boolean forceSave) {
		int cs = columnsPanel.getWidgetCount();
		boolean reset = false;
		ArrayList<String> list = new ArrayList<String>();
		for(int col=0; col < cs; col++){
			ColumnPanel colPanel = (ColumnPanel) columnsPanel.getWidget(col);
			int rs = colPanel.getWidgetCount();
			int row = 0; //row !=idx, because some invisble Widget in colPanel
			for(int idx=0; idx < rs; idx++){
				Widget w= colPanel.getWidget(idx);
				if(!(w instanceof Portlet))
					continue;
				Portlet portlet = (Portlet) w;
				PortletContext context = portlet.getContext();
				if(context.getColumn() != col || context.getRow() != row){
					reset = true;
					context.setColumn(col);
					context.setRow(row);
				}
				list.add(portlet.getUid() + SharedConstants.PORTLET_SEP + row + SharedConstants.PORTLET_SEP  + col);
				row++;
			}
			//the column panel may from 0 portlet to more porlets or in reverse.
			colPanel.removeInvisibleHolder();
			colPanel.tryInvisibleHolder();
		}
		//if some portlet position is updated, 
		if(forceSave || reset){
			//update user home page setting.
			visitor.saveLayout(list);
		}
	}
	
	private void initColumns(int columns) {
		columnsPanel.clear();
		//initial column panel to User setting columns
		int width = 100 / columns;
		for(int idx=0;idx<columns;idx++){
			ColumnPanel columnPanel = new ColumnPanel();
			columnPanel.addStyleName(Css.PORTAL_COLUMN);
			DOM.setElementAttribute(columnPanel.getElement(), "cellSpacing", "4px");
			DOM.setElementAttribute(columnPanel.getElement(), "cellPadding", "2px");
			//set dnd feature
			VerticalPanelDropController dropController = new VerticalPanelDropController(columnPanel);
			dragController.registerDropController(dropController);
			columnsPanel.add(columnPanel);
			columnsPanel.setCellWidth(columnPanel, width + "%");
		}
	}



	//********************************************************************
	//                       private class
	//********************************************************************
	public class ColumnPanel extends VerticalPanel{
		
		public void tryInvisibleHolder() {
			int rs = this.getWidgetCount();
			if(rs == 0){
				//if this column has not any portlet, it is impossible to drag something in.  
				//so here, put a invisible placeholder which make it possible to drag another portlet to this empty column.
				HorizontalPanel invisHolder = new HorizontalPanel();
				invisHolder.setWidth("100%");
				this.add(invisHolder);
			}			
		}
		public void removeInvisibleHolder() {
			for(Iterator<Widget> iter = this.iterator();iter.hasNext();){
				Widget w = iter.next();
				if(w instanceof HorizontalPanel){
					iter.remove();
					break;
				}
			}
			
		}
		
	}

	public class SaveAsyncCallback implements AsyncCallback<Boolean>{

		public void onFailure(Throwable obj) {
			GwtClientUtils.processError(obj);
		}

		public void onSuccess(Boolean succ) {
			
		}
		
	}


}
