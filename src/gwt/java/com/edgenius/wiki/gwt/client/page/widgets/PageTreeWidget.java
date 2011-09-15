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

import com.edgenius.wiki.gwt.client.ElementRequester;
import com.edgenius.wiki.gwt.client.ElementRequesterCallback;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.TreeItemListModel;
import com.edgenius.wiki.gwt.client.model.TreeItemModel;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class PageTreeWidget extends SimplePanel implements HasSelectionHandlers<TreeItem>,	HasOpenHandlers<TreeItem>, 
	HasCloseHandlers<TreeItem>,	SelectionHandler<TreeItem>, OpenHandler<TreeItem>, CloseHandler<TreeItem>, ElementRequesterCallback{

	private Tree tree = new Tree();
	private TreeItem rootItem;
	private MessageWidget message = new MessageWidget();
	private List<TreeItemModel> treeList; 
	private String spaceUname;
	private String selectedPageUuid;
	private ElementRequester request = new ElementRequester(message);
	
	public PageTreeWidget(){
		this(null);
	}
	public PageTreeWidget(TreeItem root){
		request.addCallback(this);
		tree.addCloseHandler(this);
		tree.addOpenHandler(this);
		tree.addSelectionHandler(this);
		
		this.rootItem = root;
		
		ScrollPanel pagePanel = new ScrollPanel();
		pagePanel.add(tree);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(pagePanel);
		//this is just left one empty line between the tree and down border
		panel.add(new HTML("&nbsp;"));
		this.setWidget(panel);
		
	}
	/**
	 * RPC call to get all page tree info for this space. The selectedPageUid will be selected and visible after tree reset
	 */
	public void refreshTree(String spaceUname, String selectedPageUuid){
		this.selectedPageUuid = selectedPageUuid;
		this.spaceUname = spaceUname;
		if( spaceUname != null && spaceUname.trim().length() > 0){
			request.needPageTree(spaceUname);
		}else{
			//spaceUname is blank, just show root node in tree: pass in an empty list
			buildTree(spaceUname, new ArrayList<TreeItemModel>());
		}

	}
	
	public void createTree(TreeItem root, String spaceUname, String selectedPageUuid) {
		this.selectedPageUuid = selectedPageUuid;
		this.spaceUname = spaceUname;
		rootItem = root;
		message.cleanMessage();
		request.needPageTree(spaceUname);
	}
	
	public String getSpaceUname(){
		return spaceUname;
	}
	public void setSelection(String selectedPageUuid){
		for(Iterator<TreeItem> iter = tree.treeItemIterator();iter.hasNext();){
			TreeItem item = iter.next();
			if(StringUtil.equals(selectedPageUuid,item.getUserObject().toString())){
				tree.setSelectedItem(item, false);
				break;
			}
		}
	}
	public void expand(){
		for(Iterator<TreeItem> iter = tree.treeItemIterator();iter.hasNext();){
			TreeItem item = iter.next();
			item.setState(true, false);
		}
	}
	public void collapse(){
		rootItem.setState(false, false);
	}
	
	/**
	 * Create HorizontalPanel for expand, collapse, etc. functions
	 * @return
	 */

	public HorizontalPanel getFunctionButtons(){
		HorizontalPanel panel = new HorizontalPanel();
		Image expand = new Image(IconBundle.I.get().expand());
		expand.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				expand();
			}
		});
		Image collapse = new Image(IconBundle.I.get().collapse());
		collapse.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				collapse();
			}
		});
		Image refresh = new Image(IconBundle.I.get().refresh());
		refresh.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				String selected = null;
				if(rootItem != null)
					selected = (String) rootItem.getUserObject();
				for(Iterator<TreeItem> iter = tree.treeItemIterator();iter.hasNext();){
					TreeItem item = iter.next();
					if(item.isSelected()){
						selected = (String) item.getUserObject();
						break;
					}
				}
				refreshTree(spaceUname,selected);
			}
		});
		
		panel.add(expand);
		panel.add(collapse);
		panel.add(refresh);
		return panel;
	}
	
	//********************************************************************
	//               Listener methods
	//********************************************************************
	public void onOpen(OpenEvent<TreeItem> event) {
		this.fireEvent(event);
		
	}
	public void onClose(CloseEvent<TreeItem> event) {
		this.fireEvent(event);
		
	}
	public void onSelection(SelectionEvent<TreeItem> event) {
		this.fireEvent(event);		
	}
	
	public HandlerRegistration addSelectionHandler(SelectionHandler<TreeItem> handler) {
		return this.addHandler(handler, SelectionEvent.getType());
	}
	public HandlerRegistration addOpenHandler(OpenHandler<TreeItem> handler) {
		return this.addHandler(handler, OpenEvent.getType());
	}
	public HandlerRegistration addCloseHandler(CloseHandler<TreeItem> handler) {
		return this.addHandler(handler, CloseEvent.getType());	
	}
	

	
	//********************************************************************
	//               Private methods
	//********************************************************************
	private void buildTree(String spaceUname, List<TreeItemModel> treeList){
		
		this.spaceUname = spaceUname;
		
		Iterator<TreeItemModel> iter = treeList.iterator();

		//first time: it is level=0; and parent= null
		TreeItemModel lastModel = new TreeItemModel();
		
		//clean last tree
		tree.setSelectedItem(null,false);
		tree.removeItems();
		
		boolean hasSelected = false;
		if(rootItem != null){
			//this rootItem could be add some child in last time buildTree(), so remove them as well.
			rootItem.removeItems();
			tree.addItem(rootItem);
		}
		while(iter.hasNext()){
			TreeItemModel model = iter.next();
			TreeItem item = new TreeItem();
			item.setText(model.title);
			item.setUserObject(model.pageUuid);
			if(model.level > lastModel.level){
				//child of last item
				lastModel.item.addItem(item);
			}else if(model.level < lastModel.level){
				//same or upper level
				if(model.level == 0){
					if(rootItem != null)
						rootItem.addItem(item);
					else
						tree.addItem(item);
				}else{
					while(model.level < lastModel.level){
						if(lastModel.parent == null){
							//error, model.level != 0 and last.level > item.level, 
							//it means last some parent must has same level with mode 
							break;
						}else{
							lastModel = lastModel.parent;
						}
					}
					lastModel.parent.item.addItem(item);
				}
			}else if(model.level == lastModel.level){
				if(model.level == 0){
					if(rootItem != null)
						rootItem.addItem(item);
					else
						tree.addItem(item);
				}else{
					lastModel.parent.item.addItem(item);
				}
			}
			if(StringUtil.equalsIgnoreCase(model.pageUuid,selectedPageUuid)){
				hasSelected = true;
				tree.setSelectedItem(item,false);
			}
			model.item = item;
			lastModel = model; 
		}
		//expand selected(current) item
		if(!hasSelected){
			//if there is no selected, open root level
			if(rootItem != null){
				rootItem.setState(true,false);
			}
		}else{
			tree.ensureSelectedItemVisible();
		}
		
		
	}
	
	public void pageTree(TreeItemListModel model) {
		treeList = model.list;
		buildTree(model.spaceUname,treeList);
	}
	public void pageTreeRequestFailed(String errorCode) {
		message.info(Msg.consts.refresh_tree());
	}
	public void pageTitleList(String spaceUname, List<String> titles) {	}
	public void pageTitleListRequestFailed(String errorCode) {	}
	public void spaceUnameList(List<String> spaces) {}
	public void spaceUnameListRequestFailed(String errorCode) {	}


}
