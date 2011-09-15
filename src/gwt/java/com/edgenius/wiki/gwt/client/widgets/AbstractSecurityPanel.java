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
import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.PermissionListModel;
import com.edgenius.wiki.gwt.client.model.PermissionModel;
import com.edgenius.wiki.gwt.client.model.RoleListModel;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.page.widgets.PageSecurityDialogue;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.space.SpaceSecurityPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public abstract class AbstractSecurityPanel extends SimplePanel implements LazyLoadingPanel {
	protected static final int OWNER_TYPE_ROLE = 1;
	protected static final int OWNER_TYPE_USER = 2;

	protected MessageWidget message = new MessageWidget();
	protected ZebraTable table = new ZebraTable();
	
	//start after table header
	protected int startRow;
	
	//first column is image, second row is role/user name
	private int startCol=2;  
	protected int roleRowCount,userRowCount;
	
	//XXX: i18n
	protected Button addRole = new Button(Msg.consts.add_group(),ButtonIconBundle.groupImage());
	protected Button addUser = new Button(Msg.consts.add_user(),ButtonIconBundle.userImage());
	
	private boolean allowAddRoleUser;
	//edit <-> save/cancel
	private DeckPanel buttonDeckPanel = new DeckPanel();;
	private String resourceName;
	private Image loadingImg = IconBundle.I.loading();
	private final SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
	
	public AbstractSecurityPanel(final String resourceName){
		this(resourceName, null, true);
	}
	public AbstractSecurityPanel(final String resourceName, final PageSecurityDialogue pageDialog, boolean allowAddRoleUser){
		this.resourceName = resourceName;
		this.allowAddRoleUser = allowAddRoleUser;
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(loadingImg);
		if(allowAddRoleUser){
			ButtonBar btnBar = new ButtonBar(ButtonBar.CENTER|ButtonBar.POSITIVE);
			//disable button first, then enable when panel loaded. So, the exceptList will be filled
			addRole.setVisible(false);
			addUser.setVisible(false);
			addRole.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					final ListDialogue roleListDialogue = new ListDialogue(Msg.consts.add_group(), ListDialogue.GROUP);
					roleListDialogue.setIcon(new Image(IconBundle.I.get().group()));
					roleListDialogue.addListener(new AddGroupDialogListener());
					
					roleListDialogue.showbox();
					roleListDialogue.loading(true);
					
					//in initial page, only show system default ROLE, space role should in search result
					securityController.getRoleList(SharedConstants.ROLE_TYPE_SYSTEM,new AsyncCallback<RoleListModel>(){
						public void onFailure(Throwable error) {
							GwtClientUtils.processError(error);
							roleListDialogue.loading(false);
						}
						public void onSuccess(RoleListModel model) {
							roleListDialogue.loading(false);
							if(!GwtClientUtils.preSuccessCheck(model, message))
								return;
							roleListDialogue.fillPanel(model.getRoleList());
						}
						
					});
				}
			});
			addUser.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					ListDialogue userListDialogue = new ListDialogue(Msg.consts.add_user(), ListDialogue.USER);
					userListDialogue.setIcon(ButtonIconBundle.userImage());
					userListDialogue.addListener(new AddUserDialogListener());
					userListDialogue.showbox();
//					Does not provide use list: reason is: 
//					1. This does not has pagination function yet
//					2. It is useless if users too much to retrieve page by page  
//					userListDialogue.loading(true);
//					securityController.getUserList(new AsyncCallback(){
//						public void onFailure(Throwable error) {
//							GwtClientUtils.processError(error);
//							userListDialogue.loading(false);
//						}
//						public void onSuccess(Object obj) {
//							userListDialogue.loading(false);
//							UserListModel model= (UserListModel) obj;
//							if(!GwtClientUtils.preSuccessCheck(model, message))
//								return;
//							userListDialogue.fillPanel(model.getUserModelList());
//						}
//					});
				}
			});
			btnBar.add(addRole);
			btnBar.add(addUser);
			panel.add(btnBar);
		}		
		
		ButtonBar btnBar = new ButtonBar(ButtonBar.CENTER|ButtonBar.POSITIVE);
	
		Button editButton = new Button(Msg.consts.edit(),ButtonIconBundle.editImage());
		btnBar.add(editButton);
		
		//in page permission panel, it is inside a dialogue, this allow user close dialogue.
		if(pageDialog != null){
			Button dlgCloseBtn = new Button(Msg.consts.close(),ButtonIconBundle.crossImage());
			btnBar.add(dlgCloseBtn);
			dlgCloseBtn.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					pageDialog.hidebox();
				}
			});
		}	
		
		buttonDeckPanel.insert(btnBar, 0);
		
		ButtonBar donePanel = new ButtonBar(ButtonBar.CENTER|ButtonBar.POSITIVE);
		Button cancelButton = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
		Button doneButton = new Button(Msg.consts.save(),ButtonIconBundle.diskImage());
		donePanel.add(doneButton);
		donePanel.add(cancelButton);
		buttonDeckPanel.insert(donePanel,1);
		
		buttonDeckPanel.showWidget(0);
		editButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				buttonDeckPanel.showWidget(1);
				setTableEditable(true);
				if(AbstractSecurityPanel.this.allowAddRoleUser){
					addRole.setVisible(true);
					addUser.setVisible(true);
				}
			}
		});
		final UpdatePermissionAsync updatePermissionAsync = new UpdatePermissionAsync();
		cancelButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				cancelEdit();
			}
		
		});
		doneButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				updateEdit(securityController, updatePermissionAsync);
			}

		});
		
		panel.add(table);
		panel.add(buttonDeckPanel);
		this.setWidget(panel);

		//load security stuff for special resource
		loadingImg.setVisible(true);
	}
	/**
	 * @param resourceName
	 * @param securityController
	 */
	public void load() {
		securityController.getResourcePermissions(getRsourceTypeOrdinal(),resourceName, new GetPermAsync());
	}
	
	public void refresh(List<PermissionModel> list){
		//clear table.
		int rowSize = table.getRowCount();
		for(int idx=rowSize-1;idx>=0;idx--)
			table.removeRow(idx);
		
		//group(role) rows
		userRowCount = 0; 
		roleRowCount = 0;
		//header row
		startRow = buildTableHeader(startCol);
		
		for (PermissionModel model :list) {
			addRow(model);
		}
		
	}

	public abstract boolean getRoleAdminReadonly(int operation);
	public abstract boolean getRoleAnonymousReadonly(int operation);
	public abstract int buildTableHeader(int startCol);
	public abstract int[] getValidOperations();
	
	public abstract int getRsourceTypeOrdinal();
	
	//********************************************************************
	//               private method
	//********************************************************************
	private void addRow(final PermissionModel model) {
		//need check if this group/user already in panel, if so, skip it: don't add duplicated value
		int rowCount = table.getRowCount();
		for(int idx=startRow;idx<rowCount;idx++){
			WritableCheckbox box = (WritableCheckbox) table.getWidget(idx, startCol);
			PermissionModel myModel = (PermissionModel) box.getObject();
			if(myModel.equals(model)){
				//duplicated! don't add to new row
				return;
			}
		}
		
		int[] validOperations = getValidOperations();

		int row = startRow;
		if(model.mask){
			//always put mask in last row
			row = table.getRowCount();
			table.setText(row,startCol,Msg.consts.masks());
			table.getFlexCellFormatter().setColSpan(row, startCol,validOperations.length);
			
			row++;
			table.setText(row,startCol,Msg.consts.masks());
			for(int idx=startCol;idx< validOperations.length;idx++){
				model.operation = validOperations[idx];
				addColumn(model,false,row,idx+1);
			}
			return;
		}
		
		row = calculateRow(startRow, model);
		
		//first column: role/user name
		if(model.ownerType == OWNER_TYPE_ROLE){
			table.setWidget(row, 0, new Image(IconBundle.I.get().group()));
			if(this instanceof SpaceSecurityPanel && model.roleType == SharedConstants.ROLE_TYPE_SPACE){
				//display this space's user group if they are friend space
				ClickLink groupLink = new ClickLink(model.ownerDisplayName);
				groupLink.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						((SpaceSecurityPanel)AbstractSecurityPanel.this).showGroupUsers(resourceName,model.ownerName.substring(SharedConstants.ROLE_SPACE_PREFIX.length()));
					}
				});
				table.setWidget(row,1,groupLink);
			}else{
				table.setText(row,1,model.ownerDisplayName);
			}
		}else if(model.ownerType == OWNER_TYPE_USER){
			table.setWidget(row, 0, ButtonIconBundle.userImage());
			//TODO: page or space resource need to set spaceUname?
			UserProfileLink userLink = new UserProfileLink(model.ownerDisplayName, null , model.ownerName, null);
			table.setWidget(row, 1, userLink);
		}
		
		for(int idx=0;idx< validOperations.length;idx++){
//			when new added user/role which chosen from user/role list, the ownerName is null
			//but for admin and anonymous, it always on list when this panel first show, so, owner name always has value.
			boolean readonly = false;
			if(model.ownerName != null){
				//XXX:HARDCODE:role name
				if(model.ownerName.equalsIgnoreCase(SharedConstants.ROLE_ADMIN)){
					readonly = getRoleAdminReadonly(validOperations[idx]);
				}else if(model.ownerName.equalsIgnoreCase(SharedConstants.ROLE_ANONYMOUS)){
					readonly = getRoleAnonymousReadonly(validOperations[idx]);
				}
			}

			//column is from 1, because first is ownerDisplayName
			model.operation = validOperations[idx];
			addColumn(model,readonly,row,idx+startCol);
		}
		
		if(model.ownerType == OWNER_TYPE_ROLE){
			roleRowCount++;
		}else if(model.ownerType == OWNER_TYPE_USER){
			userRowCount++;
		}

	}


	private void addColumn(PermissionModel model,boolean readOnly, int row, int column ){
		//special for page:only allow tick off, does allow turn on, except this permission is tick off in page level
		boolean checked = model.operations[model.operation];
//		SecurityValues.RESOURCE_TYPES.PAGE
		if(getRsourceTypeOrdinal() == 2){
			//if current is off
			if(!model.operations[model.operation]){
				//this node mark off by page permission (model.dead == true)
				readOnly = !model.dead[model.operation];
			}
			//if this operation is marked off on upper level (space/instance), page must mark it as off.
			if(model.dead[model.operation])
				checked = false;
		}else{
			//mark different for space/instance, these setting is useless currently, until corresponding attribute turn on
			//such as Role1 space read, must wait Role1 instance read open
			if(model.dead[model.operation]){
				//TODO
			}
		}
		//the index of operations decided by SecurityValues.OPERATIONS enum positions
		WritableCheckbox box = new WritableCheckbox(checked,model.editing,readOnly);
		PermissionModel myModel = (PermissionModel) model.clone();
		box.setObject(myModel);
		table.setWidget(row, column,  box);
	}
	/*
	 * Set permission table to enable edit or disable edit.
	 */
	private void setTableEditable(boolean editable) {
		int rowSize = table.getRowCount();
		for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
			int colSize = table.getCellCount(rowIdx);
			for(int colIdx=startCol;colIdx<colSize;colIdx++){
				Widget w = table.getWidget(rowIdx, colIdx);
				if(!(w instanceof WritableCheckbox))
					continue;
				WritableCheckbox box = (WritableCheckbox) w;
				box.setEditing(editable);
			}
		}
		
	}
	private boolean hasMaskRow(){
		int rowSize = table.getRowCount();
		for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
			int colSize = table.getCellCount(rowIdx);
			for(int colIdx=startCol;colIdx<colSize;colIdx++){
				Widget w = table.getWidget(rowIdx, colIdx);
				if(!(w instanceof WritableCheckbox))
					continue;
				WritableCheckbox box = (WritableCheckbox) w;
				PermissionModel pm = (PermissionModel) box.getObject();
				if(pm.mask)
					return true;
			}
		}
		return false;
	}
	private boolean hasAnonymous(){
		int rowSize = table.getRowCount();
		for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
			int colSize = table.getCellCount(rowIdx);
			for(int colIdx=startCol;colIdx<colSize;colIdx++){
				Widget w = table.getWidget(rowIdx, colIdx);
				if(!(w instanceof WritableCheckbox))
					continue;
				WritableCheckbox box = (WritableCheckbox) w;
				PermissionModel pm = (PermissionModel) box.getObject();
				if(pm.ownerName == SharedConstants.ROLE_ANONYMOUS)
					return true;
			}
		}
		return false;
	}
	/*
	 * get row by given PermissionModel by sorted policy
	 */
	private int calculateRow(int startRow, PermissionModel model) {
		int row = startRow;
		//calculate row: according to sorted policy: ROLE_ADMIN always first, then roles sorted by name
		//, then user sorted by name, ROLE_ANONYMOUS always last
		if(model.ownerName == SharedConstants.ROLE_ADMIN){			//HARDCODE
			if(hasAnonymous())
				row = startRow+1;
			if(row < table.getRowCount())
				table.insertRow(row);
		}else if(model.ownerName == SharedConstants.ROLE_ANONYMOUS){			//HARDCODE
			row = startRow;
			if(row < table.getRowCount())
				table.insertRow(row);
		}else{
			boolean getRow = false;
			int rowSize = table.getRowCount();
			for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
				int colSize = table.getCellCount(rowIdx);
				for(int colIdx=startCol;colIdx<colSize;colIdx++){
					Widget w = table.getWidget(rowIdx, colIdx);
					if(!(w instanceof WritableCheckbox))
						continue;
					
					PermissionModel pm = (PermissionModel) ((WritableCheckbox)w).getObject();
					if(pm.ownerName == SharedConstants.ROLE_ANONYMOUS){
						continue;
					}else if(model.ownerName == SharedConstants.ROLE_ADMIN){
						continue;
					}
					if(pm.ownerType == model.ownerType){
						//this model should show before this this pm line
						if(model.ownerDisplayName.compareTo(pm.ownerDisplayName) < 0){
							if(getRow)
								row = rowIdx< row ? rowIdx : row;
							else
								row = rowIdx;
							getRow = true;
						}
					}
				}
			}
			//Possible: no user/role in list. the new added user/role should be in last position.
			if(!getRow){
				if(model.ownerType == 1){
					//role
					row = startRow + roleRowCount;
				}else if(model.ownerType == 2){
					//user
					row = startRow + roleRowCount + userRowCount;
				}
			}
//			Window.alert(model.ownerDisplayName + " on " + row);
			if(row < table.getRowCount())
				table.insertRow(row);
		}
		return row;
	}
	/*
	 * When user click "save" button while editing
	 */
	private void updateEdit(final SecurityControllerAsync adminController,
			final UpdatePermissionAsync updatePermissionAsync) {
		ArrayList<PermissionModel> changedModelList = new ArrayList<PermissionModel>();
		int rowSize = table.getRowCount();
		for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
			int colSize = table.getCellCount(rowIdx);
			for(int colIdx=startCol;colIdx<colSize;colIdx++){
				Widget w = table.getWidget(rowIdx, colIdx);
				if(!(w instanceof WritableCheckbox))
					continue;
				WritableCheckbox box = (WritableCheckbox) w;
				PermissionModel model = (PermissionModel) box.getObject();
				//call server and update 
				if(box.isChanged()){
//						Window.alert("Change " + model.ownerName + " oper:" + model.operation);
					model.checked = box.isChecked();
					changedModelList.add(model);
				}
			}
		}
		if(changedModelList.size() == 0){
			//nothing change, so just do same with cancel
			cancelEdit();
		}else{
			loadingImg.setVisible(true);
			adminController.updatePermission(getRsourceTypeOrdinal(),changedModelList,updatePermissionAsync);
		}
	}
	/*
	 * When user click "cancel" button while editing
	 */
	private void cancelEdit() {
		//does allow add role/user in view status
		buttonDeckPanel.showWidget(0);
		addRole.setVisible(false);
		addUser.setVisible(false);

		setTableEditable(false);
		//need remove all new added users/roles when cancelling
		List newAddedRow = new ArrayList();
		int rowSize = table.getRowCount();
		
		for(int rowIdx=startRow;rowIdx<rowSize;rowIdx++){
			int colSize = table.getCellCount(rowIdx);
			for(int colIdx=startCol;colIdx<colSize;colIdx++){
				Widget w = table.getWidget(rowIdx, colIdx);
				if(!(w instanceof WritableCheckbox))
					continue;
				WritableCheckbox box = (WritableCheckbox) w;
				box.reset();
				if(colIdx == startCol){
					//for each row, only need check first one is enough, break to next row.
					PermissionModel model = (PermissionModel) box.getObject();
					if(model.newadded){
						if(!newAddedRow.contains(Integer.valueOf(rowIdx))){
							newAddedRow.add(Integer.valueOf(rowIdx));
						}
						continue;
					}
				}
			}
		}
		//remove row from maximum to minimum so that the table row are always correct when removing.
		for(int idx=newAddedRow.size() -1;idx>=0;idx--){
			table.removeRow(((Integer)newAddedRow.get(idx)).intValue());
		}
	}

	//********************************************************************
	//               private classes
	//********************************************************************
	private class AddGroupDialogListener implements ListDialogueListener{
		public void dialogClosed(DialogBox sender,List keyValueList) {
			int vis = buttonDeckPanel.getVisibleWidget();
			for(Iterator iter = keyValueList.iterator();iter.hasNext();){
				RoleModel role = (RoleModel) iter.next();
				//use default operation: all false
				PermissionModel model = new PermissionModel();
				model.ownerName = role.getName();
				model.ownerDisplayName = role.getDisplayName();
				model.ownerType = OWNER_TYPE_ROLE;
				model.resourceName = resourceName;
				model.roleType = role.getType();
				model.editing = vis == 0?false:true;
				model.newadded = true;
				AbstractSecurityPanel.this.addRow(model);
			}
		}
	}
	
	
	private class AddUserDialogListener implements ListDialogueListener{
		public void dialogClosed(DialogBox sender, List keyValueList) {
			int vis = buttonDeckPanel.getVisibleWidget();
			for(Iterator iter = keyValueList.iterator();iter.hasNext();){
				UserModel user = (UserModel) iter.next();
				//use default operation: all false
				PermissionModel model = new PermissionModel();
				model.ownerName = user.getLoginname();
				model.ownerDisplayName = user.getFullname();
				model.ownerType = OWNER_TYPE_USER;
				model.resourceName = resourceName;
				model.roleType = 0;
				model.editing = vis == 0?false:true;
				model.newadded = true;
				AbstractSecurityPanel.this.addRow(model);
			}
			
		}
	}
	private class UpdatePermissionAsync implements AsyncCallback<PermissionListModel>{
		public void onFailure(Throwable obj) {
			loadingImg.setVisible(false);
			GwtClientUtils.processError(obj);
			
		}
		public void onSuccess(PermissionListModel model) {
			loadingImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model, message)){
				return;
			}
		
			buttonDeckPanel.showWidget(0);
			
			//does allow add role/user in view status
			addRole.setVisible(false);
			addUser.setVisible(false);
			
			setTableEditable(false);
		
			refresh(model.list);
		}
		
	}
	private class  GetPermAsync implements AsyncCallback<PermissionListModel>{
		public void onFailure(Throwable obj) {
			loadingImg.setVisible(false);
			GwtClientUtils.processError(obj);
			
		}
		public void onSuccess(PermissionListModel model){
			loadingImg.setVisible(false);
			if(!GwtClientUtils.preSuccessCheck(model, message)){
				return;
			}
			
			refresh(model.list);

		}
	}
}
