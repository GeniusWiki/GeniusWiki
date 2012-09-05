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
import java.util.Set;
import java.util.Vector;

import com.edgenius.wiki.gwt.client.AbstractEntryPoint;
import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.KeyCaptureListener;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.model.CaptchaCodeModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.UploadProgressModel;
import com.edgenius.wiki.gwt.client.page.EditPanel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.CaptchaVerifiedException;
import com.edgenius.wiki.gwt.client.server.HelperControllerAsync;
import com.edgenius.wiki.gwt.client.server.PageControllerAsync;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.CloseButton;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.UploadDialog;
import com.edgenius.wiki.gwt.client.widgets.Writable;
import com.edgenius.wiki.gwt.client.widgets.WritableListener;
import com.edgenius.wiki.gwt.client.widgets.ZebraTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProgressBar;
import com.google.gwt.user.client.ui.ProgressBar.TextFormatter;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class AttachmentPanel extends SimplePanel implements AttachmentListener,ClickHandler {

	public static final String MODULE_ACTION_URI = "pages/upload";
	// 5seconds
	private static int uploadingIndex = 0;
	private final String panelID = HTMLPanel.createUniqueId();
	
	private MessageWidget message = new MessageWidget();
	private UploadedPanel uploadedPanel = new UploadedPanel(); 
	private UploadingPanel uploadingPanel = new UploadingPanel();
	
	private UploadFormPanel uploadFormPanel = new UploadFormPanel();
	
	private ClickLink addMoreAttBtn = new ClickLink(Msg.consts.add_attachments());
	private HTML noPermLabel = new HTML("<b>"+Msg.consts.no_perm_upload()+"</b>");
	
	private CloseButton closeBtn = new CloseButton();
	private FlowPanel btmPanel = new FlowPanel();
	private Button uploadBtn = new Button(Msg.consts.upload());
	private Label counter = new Label();

	private Hidden removedList = new Hidden("removedList");

	private static final int SUBMIT_COUNTER_SUM = 8;
	private int submitCounter = 0;
	private Timer submitTimer = new SubmitTimer();
	private PageMain main;
	//1 second
	private final static int PROGRESS_DELAY = 1000;
	//10 minutes
	private final static long PROGRESS_TIMEOUT = PROGRESS_DELAY * 600;
	
	private boolean readonly;
	private Vector<AttachmentListener> attachmentListeners = new Vector<AttachmentListener>();

	//********************************************************************
	//                       Methods
	//********************************************************************
	public AttachmentPanel(PageMain main,boolean readonly){
		this.readonly = readonly;
		this.main = main;
		VerticalPanel panel = new VerticalPanel();
		
		closeBtn.addClickHandler(this);
		uploadBtn.addClickHandler(this);
		addMoreAttBtn.addClickHandler(this);
		
		FlexTable topPanel = new FlexTable();
		int col=0;
		topPanel.getCellFormatter().addStyleName(0,col, Css.COL1);
		FlowPanel btnPanel = new FlowPanel();
		btnPanel.add(addMoreAttBtn);
		btnPanel.add(noPermLabel);
		topPanel.setWidget(0, col++,btnPanel);
		
		topPanel.getCellFormatter().addStyleName(0,col, Css.COL2);
		topPanel.setWidget(0, col++,counter);
		
		topPanel.getCellFormatter().addStyleName(0,col, Css.COL3);
		col++;
		
		topPanel.getCellFormatter().addStyleName(0,col, Css.COL4);
		topPanel.getColumnFormatter().setWidth(col, "16");
		topPanel.setWidget(0, col++,closeBtn);
		
		//always hide, only visible when user manually call setAllowEdit(true);
		
		//only a new uploader item appear, this button will enable.
		uploadBtn.setEnabled(false);
		btmPanel.add(uploadBtn);
		
		panel.add(topPanel);
		panel.add(message);
		panel.add(uploadedPanel);
		panel.add(uploadingPanel);
		
		panel.add(uploadFormPanel);
		panel.add(btmPanel);
		panel.add(removedList);
		
		setReadonly(readonly);
		
		//style
//		btmPanel.setStyleName(Style.BUTTONS);
		this.setStyleName(Css.ATTACHMNET_PANEL);
		topPanel.setStyleName(Css.FUNCTION);
		panel.setSize("100%", "100%");
		panel.setCellHorizontalAlignment(uploadedPanel, HasHorizontalAlignment.ALIGN_CENTER);
		this.add(panel);
		
		DOM.setElementAttribute(this.getElement(), "id", panelID);
	}
	
	public String getPanelID(){
		return panelID;
	}
	public void setReadonly(boolean readonly){
		addMoreAttBtn.setVisible(!readonly);
		noPermLabel.setVisible(readonly);
		
		btmPanel.setVisible(!readonly);
		uploadFormPanel.setVisible(!readonly);
		
		uploadedPanel.setReadonly(readonly);
		this.readonly = readonly;
	}
	public boolean hasAttachments(){
		return uploadFormPanel.hasAtt();
	}
	
	public void uploadWithCaptcha(String captchaResponse, final boolean toView, final int draftStatus, final CaptchaDialog captcha){
		CaptchaCodeModel model = null;
		if(captchaResponse != null){
			model = new CaptchaCodeModel();
			model.captchaCode = captchaResponse;
			model.reqireCaptcha = true;
		}
		SecurityControllerAsync action = ControllerFactory.getSecurityController();
		action.captchaValid(model, new AsyncCallback<Integer>(){

			public void onFailure(Throwable error) {
				if(captcha != null){
					captcha.enableSubmit();
				}
				if(error instanceof CaptchaVerifiedException && captcha != null){
					captcha.refreshCaptch();
				}else{
					GwtClientUtils.processError(error);
				}
			}

			public void onSuccess(Integer result) {
				if(captcha != null){
					captcha.hidebox();
				}
				if(result == null || result == -1)
					return;
				
				uploadFormPanel.setToView(toView);
				uploadFormPanel.setDraftStatus(draftStatus);
				uploadFormPanel.submitForm();	
			}
			
		});
	}

	public void upload(boolean toView, int draftStatus){
		if(PageMain.isAnonymousLogin()){
			//popup captcha enquire for anonymous user
			CaptchaDialog captcha = new CaptchaDialog(this,toView,draftStatus);
			captcha.showbox();
		}else{
			uploadFormPanel.setToView(toView);
			uploadFormPanel.setDraftStatus(draftStatus);
			uploadFormPanel.submitForm();
		}
	}
	public void startSubmitTimer() {
		//has attachment and not start yet
		if(hasAttachments() && submitCounter == 0){
			submitCounter = SUBMIT_COUNTER_SUM;
			//every 1s to count down.
			submitTimer.scheduleRepeating(1000);
		}		
	}
	public void cancleSubmitTimer() {
		submitCounter = SUBMIT_COUNTER_SUM;
		counter.setText("");
		submitTimer.cancel();
	}
	/**
	 * Merge given JSON string with current attachment list on UploadedPanel.
	 * @param results
	 */
	public void mergeAttachments(String results) {
		if(results == null || results.trim().length() == 0)
			return;
		
		List<AttachmentModel> modelList = parseAttachmentJSON(results);
		
		//check uploadingPanel: user may manually remove attachment from uploadingPanel, then it won't be in modelList
		for (Iterator<AttachmentModel> iter= modelList.iterator();iter.hasNext();) {
			AttachmentModel model = iter.next();
			if(checkRemovedList(model.index, model.nodeUuid, model.version)){
				//put item into list only when it is not inside RemovedList(create on removing item on uploading panel).
				iter.remove();
			}else{
				//uploading item still there(not manually removed), then remove it now.
				uploadingPanel.removeItem(model.index);
			}
			
		}
		
		if(modelList.size() > 0 ){
			List<AttachmentModel> uploaded = new ArrayList<AttachmentModel>();
			
			for (Iterator<AttachmentModel> iter = modelList.iterator(); iter.hasNext();){
				AttachmentModel att = iter.next();
				//only non-bulk-uploaded could display on uploaded panel...
				if(!att.bulkZip){
					uploaded.add(att);
					QuickHelpDictionary.addAttachment(att.filename);
				}
			}
		
			//fire event to add item in uploadedPanel
			for(AttachmentListener  listener: attachmentListeners){
				listener.addOrUpdateItem(uploaded);
			}
		}
	}
	
	public void addAttachmentListener(AttachmentListener listener){
		attachmentListeners.add(listener);
	}

	/**
	 * @param mceImageDialog
	 */
	public void removeAttachmentListener(AttachmentListener listener) {
		attachmentListeners.remove(listener);
	}

	public void addOrUpdateItem(List<AttachmentModel> modelList) {
		if(modelList == null || modelList.size() == 0)
			return;
		
		//check if there any errors, if has, popup message in main panel.
		for(Iterator<AttachmentModel> iter = modelList.iterator();iter.hasNext();){
			AttachmentModel att = iter.next();
			if(ErrorCode.hasError(att)){
				main.errorOnVisiblePanel(ErrorCode.getMessageText(att.errorCode,att.errorMsg));
				iter.remove();
				//remove uploading panel
				uploadingPanel.clear();
			}
		}
		
		uploadedPanel.addUploadItem(modelList);
		main.setAttachmentCount(uploadedPanel.getUploadedCount());
		
	}
	public void removeItem(String nodeUuid) {
		uploadedPanel.removeItem(nodeUuid);
		main.setAttachmentCount(uploadedPanel.getUploadedCount());
	}

	public void onClick(ClickEvent event) {
		Object sender = event.getSource();
		if (sender == closeBtn) {
			this.setVisible(false);
		} else if (sender == addMoreAttBtn) {
			//uploadFormPanel.addUploderItem();
		    int draftStatus = SharedConstants.AUTO_DRAFT;
            if(main.getVisiblePanelIndex() == PageMain.EDIT_PANEL){
                //anyway, save initial status: maybe user will choose save or save-draft, then status will modified in server side.
                draftStatus = SharedConstants.AUTO_DRAFT;
            }else if(main.getVisiblePanelIndex() == PageMain.VIEW_PANEL){
                //for viewPanel uploading, it always immediately become formal attachment
                draftStatus = SharedConstants.NONE_DRAFT;
            }
            
		    UploadDialog dialog = new UploadDialog(this, PageMain.getSpaceUname(), PageMain.getPageUuid(), draftStatus);
            dialog.showbox();
		} else if (sender == uploadBtn){
			int draftStatus = SharedConstants.AUTO_DRAFT;
			if(main.getVisiblePanelIndex() == PageMain.EDIT_PANEL){
				//anyway, save initial status: maybe user will choose save or save-draft, then status will modified in server side.
				draftStatus = SharedConstants.AUTO_DRAFT;
			}else if(main.getVisiblePanelIndex() == PageMain.VIEW_PANEL){
				//for viewPanel uploading, it always immediately become formal attachment
				draftStatus = SharedConstants.NONE_DRAFT;
			}
			
		
			upload(false,draftStatus);
		}
	}

	/**
	 * This only return list without history
	 */
	public ArrayList<String> getUploadedItemsUuid() {
		return uploadedPanel.getUploadedItemsUuid();
	}
	/**
	 * This only return list without history
	 */
	public List<AttachmentModel> getUploadedItems() {
		return uploadedPanel.getUploadedItems();
	}

	public void reset(){
		uploadedPanel.clear();
		uploadingPanel.clear();
		uploadFormPanel.clear();
		message.cleanMessage();
		main.setAttachmentCount(uploadedPanel.getUploadedCount());
		
		QuickHelpDictionary.clearAttachmentList();
		
	}
	public boolean isReadonly(){
		return readonly;
	}
	public void addOneFileUploadBox(){
		uploadFormPanel.addUploderItem();
	}
	/**
	 * @param results
	 * @return 
	 */
	public static List<AttachmentModel> parseAttachmentJSON(String results) {
		List<AttachmentModel> modelList = new ArrayList<AttachmentModel>();
		try {
			JSONValue jsonValue = JSONParser.parse(results);
			JSONArray attachmentArray;
			if ((attachmentArray = jsonValue.isArray()) != null) {
				//AttachmentList
				int size = attachmentArray.size();
				for (int idx = 0; idx < size; ++idx) {
					//attachment
					JSONObject attachObj = attachmentArray.get(idx).isObject();
					AttachmentModel model = retrieve(attachObj);
					modelList.add(model);
				}
			}
		} catch (JSONException e) {
			Window.alert(Msg.consts.error_request());
		}
		
		return modelList;
	}
	
	public void refresh(String spaceUname, String pageUuid, int draftStatus){
	    PageControllerAsync action = ControllerFactory.getPageController();
        action.getAttachments(spaceUname, pageUuid, draftStatus, new AsyncCallback<String>() {
            
            @Override
            public void onSuccess(String json) {
            	if(json != null){
            		AttachmentPanel.this.clear();
            		AttachmentPanel.this.mergeAttachments(json);
            	}
            }
            
            @Override
            public void onFailure(Throwable arg0) {
                //nothing
            }
        });
	}

	//********************************************************************
	//               Private methods
	//********************************************************************

	
	private static AttachmentModel retrieve(JSONObject attachObj){
		AttachmentModel model = new AttachmentModel();
		
		//retrieve Attachment->Filename, Comment and Shared fields.
		//Attachment fields
		Set<String> attKeys = attachObj.keySet();
		
		for(Iterator<String> attIter = attKeys.iterator();attIter.hasNext();){
			String attKey = attIter.next();
			//these values come from com.edgenius.core.repository.FileNode.java
			if (attKey.equalsIgnoreCase("filename")) {
				model.filename = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("comment")) {
				model.desc = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("date")) {
				model.date = (long) attachObj.get(attKey).isNumber().doubleValue();
			}else if(attKey.equalsIgnoreCase("size")) {
				model.size = (long) attachObj.get(attKey).isNumber().doubleValue();
			}else if(attKey.equalsIgnoreCase("userFullname")) {
				model.creator = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("index")) {
				model.index = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("nodeUuid")) {
				model.nodeUuid = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("version")) {
				model.version = attachObj.get(attKey).isString().stringValue();
			}else if(attKey.equalsIgnoreCase("bulkZip")) {
				model.bulkZip = attachObj.get(attKey).isBoolean().booleanValue();
			}else if(attKey.equalsIgnoreCase("errorCode")) {
				model.errorCode = attachObj.get(attKey).isString().stringValue();
			}
		}
		
		return model;
		
	}
	/**
	 * @return true, then this attachment is already removed from UploadingPanel by user
	 */
	private boolean checkRemovedList(String index, String nodeUuid, String version){
		//check if the item already be remove during uploading process!!!
		if(index != null && (index=index.trim()).length() > 0){
			String removed = removedList.getValue();
			if(removed != null && removed.trim().length() > 0){
				boolean needRemove = false;
				int len;
				if((len= removed.indexOf("/"+index+"/")) > -1){
					removed = removed.substring(0,len) + removed.substring(len+index.length()+1);
					needRemove = true;
				}else if (removed.trim().endsWith("/"+index)){
					removed = removed.substring(0,removed.length() - index.length() -1);
					needRemove = true;
				}
				removedList.setValue(removed);
				//if this item already removed from uploadingPanel, then send request to server side
				//and ignore add this item to uploadedPanel.
				if(needRemove){
					removeAttachment(nodeUuid,version);
					return true;
				}
			}
		}
		return false;
	}
	private void removeAttachment(String nodeUuid, String version){
		PageControllerAsync action = ControllerFactory.getPageController();
		action.removeAttachment(main.getSpaceUname(),main.getPageUuid(),nodeUuid,version, new AsyncCallback<PageModel>(){
			//nothing to do here
			public void onFailure(Throwable error) {
				GwtClientUtils.processError(error);
			}
			public void onSuccess(PageModel model) {
				if(!GwtClientUtils.preSuccessCheck(model, message)){
					return;
				}
			}
		});
	}



	//********************************************************************
	//               Private class : Uploaded Panel 
	//********************************************************************
	private class UploadedPanel extends SimplePanel{
		//(itemID,open/close image),name,author,size,date,desc,edit/remove:
		private static final int MAIN_ITEM_LEN = 7;
		private static final int HISTORY_IMG_COL = 0;
		private ZebraTable table = new ZebraTable();
		//row 0 is header
		private static final int startRow =1;
		
		private Label msg = new Label(Msg.consts.no_attachment());
			
		private VerticalPanel panel = new VerticalPanel();
		private boolean readonly;
		public UploadedPanel(){
			clear();
			msg.setStyleName(Css.BLANK_MSG);
			this.setWidget(panel);
		}
		public void setReadonly(boolean readonly){
			this.readonly = readonly;
			int count = table.getRowCount();
			for(int row = startRow;row<count;row++){
				ItemID id = (ItemID) table.getWidget(row, 0);
				if(!id.isHistory()){
					//skip first one(itemID,img) and last (remove btn)
					for(int col=1;col<MAIN_ITEM_LEN-1;col++){
						Writable label = (Writable) table.getWidget(row, col);
						//make some editing label switch to text. 
						label.cancelEditing();
					}
					FuncPanel funcPanel = (FuncPanel) table.getWidget(row, MAIN_ITEM_LEN-1);
					funcPanel.setVisible(!readonly);
				}
			}
			
			if(startRow <= count)
				//show/hide header part "Function"
				table.getWidget(0, MAIN_ITEM_LEN-1).setVisible(!readonly);
		}
		/**
		 * Override to only clean table
		 */
		public void clear(){
			//DO NOT user table.clear simply, which does not clear all cell in table, see HtmlTable Jdoc. 
			panel.clear();
			msg.setVisible(true);
			table  = new ZebraTable();

			panel.add(table);
			panel.add(msg);
			table.setWidth("97%");
			panel.setSize("99%", "100%");
		}
		
		public void removeItem(String nodeUuid) {
			int count = table.getRowCount();
			int removeRow = -1;
			for(int row = startRow;row<count;row++){
				ItemID id = (ItemID) table.getWidget(row, 0);
				if(id.getUuid().equals(nodeUuid)){
					removeRow = row;
					break;
				}
			}
			
			if(removeRow != -1){
				//try to remove history first.
				if(removeRow+1 < count){
					ItemID hisID = (ItemID) table.getWidget(removeRow+1, 0);
					if(hisID.getUuid().equals(nodeUuid)){
						table.removeRow(removeRow+1);
					}
				}
				//remove main item row
				table.removeRow(removeRow);
			}
			if(table.getRowCount() == startRow){
				clear();
			}
		}
		/*
		 * Return how many items in uploaded panel, it won't count history items
		 */
		public int getUploadedCount(){
			int rowSize = table.getRowCount();
			int count = 0;
			for(int idx = startRow;idx<rowSize;idx++){
				ItemID itemId = (ItemID) table.getWidget(idx, 0);
				if(itemId.isHistory())
					continue;
				
				count++;
			}
			return count;
		}
		public ArrayList<String> getUploadedItemsUuid(){
			int rowSize = table.getRowCount();
			ArrayList<String> uuidList = new ArrayList<String>();
			for(int idx = startRow;idx<rowSize;idx++){
				ItemID itemId = (ItemID) table.getWidget(idx, 0);
				if(itemId.isHistory())
					continue;
				
				uuidList.add(itemId.getUuid());
			}
			
			return uuidList;
		}
		public List<AttachmentModel> getUploadedItems() {
			int rowSize = table.getRowCount();
			List<AttachmentModel> list = new ArrayList<AttachmentModel>();
			for(int idx = startRow;idx<rowSize;idx++){
				ItemID itemId = (ItemID) table.getWidget(idx, 0);
				if(itemId.isHistory())
					continue;
				
				list.add(itemId.getModel());
			}
			
			return list;
		}

		/*
		 * This method will merge current item with input items in list. If input item is could be a new item, a item
		 * which is newer than exist one(nodeUuid is same), then the old one will push into history list.
		 * 
		 * Structure:
		 * Main Item Row -> ItemID(uuid,ver,false), displayed fields in column
		 * HistoryItem Row -> ItemID(uuid,"",true), HistoryTable -> ItemID(uuid,ver,true), displayed field in column 
		 * 
		 */
		public void addUploadItem(List<AttachmentModel> modelList){
			int size = modelList.size();
			//remove "No attachment message" 
			if(size >= 0 && msg.isVisible()){
				msg.setVisible(false);
				buildTableHeader();
			}

			for(int s=0;s<size;s++){
				final AttachmentModel model = modelList.get(s);
				//check if same attachment already exist: if so, will update.
				int count = table.getRowCount();
				boolean exist = false;
				
				for(int idx = startRow;idx<count;idx++){
					ItemID itemId = (ItemID) table.getWidget(idx, 0);
					if(itemId.isHistory())
						continue;
					
					//find same item exist, then need compare incoming item with all item(root and history) to decide 
					//arrange items by versions
					if(itemId.getUuid().equals(model.nodeUuid)){
						float ver = itemId.getVersion();
						float newVer = getFloatVersion(model.version);
						if(newVer > ver){
							//make this item become main item and convert old item to first history item
							convertToHistory(idx,model);
						}else if( newVer == ver){
							//update main item
							ItemID item = (ItemID) table.getWidget(idx, HISTORY_IMG_COL);
							setItem(idx,model,item.isOpen());
						}else{
							//compare income with history item, update or insert new one 
							refreshHistory(idx,model);
						}
						//update this item and return
						exist = true;
						break;
					}
				}
				if(exist)
					continue;
				//could don't found exist item, then create new row
				
				//TODO: this does not sort by file name
				//Don't use above count field. table.getRowCount(): row is possible increase if multiple files upload.
				setItem(table.getRowCount(),model,false);
			}
		

		}
		
		private void buildTableHeader() {
			//skip ItemID, and History Image cells 
			int col = HISTORY_IMG_COL+1;
			table.getCellFormatter().setWordWrap(0, col, false);
			table.setWidget(0, col++, new HTML(Msg.consts.file_name()));
			table.getCellFormatter().setWordWrap(0, col, false);
			table.setWidget(0, col++, new HTML(Msg.consts.author()));
			table.getCellFormatter().setWordWrap(0, col, false);
			table.setWidget(0, col++, new HTML(Msg.consts.size()));
			table.getCellFormatter().setWordWrap(0, col, false);
			table.setWidget(0, col++, new HTML(Msg.consts.date()));
			table.getCellFormatter().setWordWrap(0, col, false);
			table.setWidget(0, col++, new HTML(Msg.consts.description()));
			HTML f = new HTML(Msg.consts.actions());
			table.setWidget(0, col++, f);
			f.setVisible(!readonly);
			
			col = HISTORY_IMG_COL + 1;
			for(int idx=0;idx<col;idx++)
				table.getColumnFormatter().setWidth(idx,"1%");
						
			table.getColumnFormatter().setStyleName(col++, Css.COL1); //name 
			table.getColumnFormatter().setStyleName(col++, Css.COL2); //author
			table.getColumnFormatter().setStyleName(col++, Css.COL3); //size
			table.getColumnFormatter().setStyleName(col++, Css.COL4); //date
			col++; //desc SKIP!
			table.getColumnFormatter().setStyleName(col++, Css.COL6); //funct
		}

		/*
		 * mainRow is main item row, history will be in next row.
		 */
		private void refreshHistory(int mainRow, AttachmentModel model) {
			//firstly, try to get history item panel, if no, then create new one
			FlexTable historyTable = null;
			int hisRow = mainRow + 1;
			if(hisRow < table.getRowCount()){
				//main row is not last row, so need check if next row is its history row
				ItemID id = (ItemID) table.getWidget(mainRow+1, 0);
				if(id.getUuid().equals(model.nodeUuid)){
					//find exist history table
					historyTable =(FlexTable) table.getWidget(mainRow+1, HISTORY_IMG_COL+1);
				}
			}
			//no history exist, create new one
			if(historyTable == null){
				if(hisRow < table.getRowCount()){
					//create a new row to hold new HistoryTable if main item is not last item.
					table.insertRow(hisRow);
				}
				historyTable = new FlexTable();
				historyTable.setStyleName(Css.HISTORY_TABLE);
				//table first column always is ItemID
				table.setWidget(hisRow,0,new ItemID(model,true));
				table.setWidget(hisRow,HISTORY_IMG_COL+1, historyTable);
				table.getFlexCellFormatter().setColSpan(hisRow, HISTORY_IMG_COL+1,MAIN_ITEM_LEN-1);
				int total = table.getCellCount(hisRow);
				//if the row of history is new one, it could contain extra columns, remove them.
				if(total > HISTORY_IMG_COL+1)
					table.removeCells(hisRow, HISTORY_IMG_COL+2, total - 2 - HISTORY_IMG_COL);
				//last field: remove button, will leave empty
			}
			int hisCount = historyTable.getRowCount();
			
			boolean historyDone = false;
			float newVer = getFloatVersion(model.version);
			for(int hrow = 0;hrow < hisCount;hrow++){
				 ItemID itemId = (ItemID) historyTable.getWidget(hrow,0);
				 float ver = itemId.getVersion();
				 if(newVer > ver){
					 //insert incoming before this history item
					 historyTable.insertRow(hrow);
					 setHisotryItem(model,hrow,historyTable);
					 historyDone = true;
					 break;
				 }else if(newVer == ver){
					 setHisotryItem(model,hrow,historyTable);
					 historyDone = true;
					 break;
				 }
			}
			if(!historyDone){
				//insert this history to last one
				setHisotryItem(model,historyTable.getRowCount(),historyTable);
			}			
			
			//enable history open/close image to visible
			ItemID itemId = (ItemID) table.getWidget(mainRow, HISTORY_IMG_COL);
			itemId.setSwitch(itemId.isOpen()?ItemID.SWITCH_CLOSE:ItemID.SWITCH_OPEN);
			//also set history visible or not according to image switch status
			table.getRowFormatter().setVisible(hisRow, itemId.isOpen());
		}


		/*
		 * Convert given row in table to first history item. If history row does not exist, then create a new one
		 */
		private void convertToHistory(int row, AttachmentModel mainModel) {

			//get current row all value and convert them to AttachmentModel, then create new row in history table
			int idx = 0;
			ItemID itemId = (ItemID) table.getWidget(row,idx++);
			AttachmentModel model = itemId.getModel();
			refreshHistory(row,model);
			
			setItem(row, mainModel, itemId.isOpen());
			
		}
		/**
		 * @param model
		 * @param hisRow
		 * @param historyTable
		 */
		private void setHisotryItem(AttachmentModel model, int hisRow, FlexTable historyTable) {
			int hisCol = 0;
			
			historyTable.setWidget(hisRow, hisCol++, new ItemID(model,true));
			historyTable.setWidget(hisRow, hisCol++, new Label("ver"+model.version));
			
			historyTable.setWidget(hisRow, hisCol++, GwtClientUtils.buildDownloadURLWidget(main.getSpaceUname(),model.filename,model.nodeUuid,model.version));
			historyTable.setWidget(hisRow, hisCol++, new Label(model.creator));
			historyTable.setWidget(hisRow, hisCol++, new Label(GwtUtils.convertHumanSize(model.size)));
			historyTable.setWidget(hisRow, hisCol++, new Label(GwtClientUtils.toDisplayDate(model.date)));
			historyTable.setWidget(hisRow, hisCol++, new Label(model.desc));
			
		}

		private void setItem(int row, final AttachmentModel model, boolean closeHistory) {
			int col = 0;
			ItemID itemID = new ItemID(model,false);
			table.getCellFormatter().setWidth(row,col,"1%");
			table.setWidget(row, col++, itemID);

			//check if this item has history: if no, mark image invisible
			if(row+1 < table.getRowCount()){
				ItemID historyItemId = (ItemID) table.getWidget(row+1, 0);
				if(historyItemId.isHistory() && historyItemId.getUuid().equals(model.nodeUuid)){
					itemID.setSwitch(closeHistory?ItemID.SWITCH_CLOSE:ItemID.SWITCH_OPEN);
				}
			}
			
		
			Writable nameLabel = new Writable(GwtClientUtils.buildDownloadURLWidget(main.getSpaceUname(),model.filename,model.nodeUuid,model.version),model.filename);
			nameLabel.addFocusHandler(KeyCaptureListener.instance());
			nameLabel.addBlurHandler(KeyCaptureListener.instance());
			nameLabel.addStyleName(Css.NAME);
			final Label descText = new Label(model.desc);
			Writable descLabel = new Writable(descText,model.desc);
			descLabel.addFocusHandler(KeyCaptureListener.instance());
			descLabel.addBlurHandler(KeyCaptureListener.instance());
			descLabel.addStyleName(Css.DESC);
			
			table.getCellFormatter().setWordWrap(row, col, false);
			table.setWidget(row, col++, nameLabel);
			
			table.getCellFormatter().setWordWrap(row, col, false);
			table.setWidget(row, col++, new Writable(new Label(model.creator),model.creator,true));
			
			table.getCellFormatter().setWordWrap(row, col, false);
			table.setWidget(row, col++, new Writable(new Label(GwtUtils.convertHumanSize(model.size)),GwtUtils.convertHumanSize(model.size),true));
			
			table.getCellFormatter().setWordWrap(row, col, false);
			table.setWidget(row, col++, new Writable(new Label(GwtClientUtils.toDisplayDate(model.date)),GwtClientUtils.toDisplayDate(model.date),true));
			
			//skip desc wrap
			table.setWidget(row, col++, descLabel);
			
			table.getCellFormatter().setWordWrap(row, col, false);
			FuncPanel func = new FuncPanel(itemID);
			func.setVisible(!readonly);
			table.setWidget(row, col++,func);
			
			//name and description are editable
			nameLabel.addListener(new WritableListener(){
				public void editDone(Writable sender, String text) {
					model.filename = text; 
					sender.resetWidget(GwtClientUtils.buildDownloadURLWidget(main.getSpaceUname(),model.filename,model.nodeUuid,model.version));
					
				}

				public void editing(Writable sender) {
					
				}
				public void editCancelled(Writable sender, String originalText) {
					// TODO Auto-generated method stub
					
				}
			});
			descLabel.addListener(new WritableListener(){
				public void editDone(Writable sender, String text) {
					//reset desc text in display. 
					descText.setText(text);
				}

				public void editing(Writable sender) {
					
				}

				public void editCancelled(Writable sender, String originalText) {
				}
			});
		}

		private float getFloatVersion(String verstr) {
			try{
				return new Float(verstr).floatValue();
			}catch(Exception e){
				return 0;
			}
		}
		private class ItemID extends SimplePanel{
			public final static int SWITCH_NONE = 0;
			public final static int SWITCH_CLOSE = 1;
			public final static int SWITCH_OPEN = 2;
			
			//switch image setting
			final Image closeImg =  new Image(IconBundle.I.get().closeArrow());
			final Image openImg =  new Image(IconBundle.I.get().openArrow());
			private boolean history;
			private int switchType;

			private AttachmentModel model;
			public ItemID(final AttachmentModel model, boolean history){
				//clone useful field... rather than keep model object
				this.model = model.clone();
				this.history = history;
				
				FlowPanel panel = new FlowPanel();
				openImg.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						switchGate(ItemID.this,false);
					}
				});
				
				closeImg.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						switchGate(ItemID.this,true);
					}
				});
				
				closeImg.setVisible(false);
				openImg.setVisible(false);
				panel.add(closeImg);
				panel.add(openImg);
				this.setWidget(panel);
			}


			private void switchGate(ItemID itemId, boolean open) {
				//retrieve table to find out the history panel and hide it
				//DON'T use row, this row maybe adjust when new item added,it is not always correct.
				int size = table.getRowCount();
				for(int idx=startRow;idx < size;idx++){
					ItemID historyItemID = (ItemID) table.getWidget(idx, 0);
					if(historyItemID.isHistory() && historyItemID.getUuid().equals(model.nodeUuid)){
						//make this history show/hide
						table.getRowFormatter().setVisible(idx, open);
						//switch img
						itemId.setSwitch(open?SWITCH_CLOSE:SWITCH_OPEN);
						break;
					}
				}
			}
			/**
			 * @return
			 */
			public boolean isOpen() {
				//if switch is close image, it means history is opened
				return switchType == SWITCH_CLOSE;
			}

			public void setSwitch(int type) {
				switchType = type;
				if(type == SWITCH_NONE){
					//hide all
					openImg.setVisible(false);
					closeImg.setVisible(false);
				}else if (type == SWITCH_CLOSE){
					//show open
					openImg.setVisible(true);
					closeImg.setVisible(false);
				}else{
					//show open
					openImg.setVisible(false);
					closeImg.setVisible(true);
				}
			}

			public boolean isHistory(){
				return history;
			}

			public float getVersion(){
				return getFloatVersion(model.version);
			}
			public String getUuid(){
				return this.model.nodeUuid;
			}

			public AttachmentModel getModel() {
				return model.clone();
			}
		}
		
		/**
		 * Each attachment item has a Function Panel which holds possible operation for this attachment. Such as 
		 * Edit,Remove, Done(edit), Cancel(Edit).
		 */
		private class FuncPanel extends SimplePanel implements ClickHandler, AsyncCallback<String>{
			private FlowPanel panel = new FlowPanel();
			private ClickLink removeBtn = new ClickLink(Msg.consts.remove());
			private ClickLink editBtn = new ClickLink(Msg.consts.edit());
			private ClickLink doneBtn = new ClickLink(Msg.consts.done());
			private ClickLink cancelBtn = new ClickLink(Msg.consts.cancel());
			private ItemID itemID;

			/**
			 * @param itemID
			 */
			public FuncPanel(ItemID itemID) {
				this.itemID = itemID;
				
				
				if(!AbstractEntryPoint.isOffline()){
					HTML sep1 = new HTML("|");
					doneBtn.setVisible(false);
					cancelBtn.setVisible(false);
					panel.add(editBtn);
					panel.add(cancelBtn);
					panel.add(sep1);
					panel.add(doneBtn);
					
					editBtn.addClickHandler(this);
					doneBtn.addClickHandler(this);
					cancelBtn.addClickHandler(this);
					DOM.setStyleAttribute(sep1.getElement(), "display", "inline");
				}
				
				panel.add(removeBtn);
				removeBtn.addClickHandler(this);
				
				this.setWidget(panel);
			}
			public void onClick(ClickEvent event) {
				Object sender = event.getSource();
				if(sender == removeBtn){
					if(Window.confirm(Msg.consts.confirm_remove_attachment())){
						//remove whole node with its history
						removeAttachment(itemID.getUuid(),null);
						for(AttachmentListener  listener: attachmentListeners){
							listener.removeItem(itemID.getUuid());
						}
					}
				}else if(sender == editBtn){
					if(!AbstractEntryPoint.isOffline()){
						editBtn.setVisible(false);
						doneBtn.setVisible(true);
						cancelBtn.setVisible(true);
					}
					removeBtn.setVisible(false);
					switchEdit(1);
				}else if(sender == doneBtn){
					if(!AbstractEntryPoint.isOffline()){
						editBtn.setVisible(true);
						doneBtn.setVisible(false);
						cancelBtn.setVisible(false);
					}	
					removeBtn.setVisible(true);
					
					//save attachment metadata to server side.
					String[] meta = getMetaData();
					PageControllerAsync pageController = ControllerFactory.getPageController();
					//NodeUuid, Name, Desc
					pageController.updateAttachmentMeta(main.getSpaceUname(),main.getPageUuid(), meta[0],meta[1],meta[2],this);
					
				}else if(sender == cancelBtn){
					if(!AbstractEntryPoint.isOffline()){
						editBtn.setVisible(true);
						doneBtn.setVisible(false);
						cancelBtn.setVisible(false);
					}
					removeBtn.setVisible(true);
					switchEdit(3);
				}
				
			}
			private String[] getMetaData(){
				//NodeUuid, Name, Desc
				String[] meta = null;
				
				int rowSize = table.getRowCount();
				for(int row=startRow;row<rowSize;row++){
					ItemID id = (ItemID) table.getWidget(row, 0);
					if(!id.isHistory() && id.getUuid().equals(itemID.getUuid())){
						int colSize = table.getCellCount(row);
						meta = new String[3];
						meta[0] = itemID.getUuid();
						int idx=0;
						for(int col=0;col<colSize;col++){
							Widget w = table.getWidget(row, col);
							//so far, to get Name and desc, is check if they are readonly, first should be name, then desc
							if(w instanceof Writable && !((Writable)w).isReadonly()){
								meta[++idx] = ((Writable)w).getEditingText();
								//already get name and desc
								if(idx == 2)
									break;
							}
						}
						break;
					}
				}
				
				return meta;
			}
			private void switchEdit(int flag) {
				//invoke edit model for this row items
				int rowSize = table.getRowCount();
				for(int row=startRow;row<rowSize;row++){
					ItemID id = (ItemID) table.getWidget(row, 0);
					if(!id.isHistory() && id.getUuid().equals(itemID.getUuid())){
						int colSize = table.getCellCount(row);
						for(int col=0;col<colSize;col++){
							Widget w = table.getWidget(row, col);
							if(w instanceof Writable){
								Writable wl = (Writable)w;
								if(flag == 1){
									wl.enableEdit();
								}else if(flag == 2){
									wl.doneEdit();
								}else if(flag == 3){
									wl.cancelEditing();
								}
							}
						}
						break;
					}
				}
			}
			public void setVisible(boolean visible){
				if(!visible){
					//if visible, then must set function bar to edit|remove status.
					if(!AbstractEntryPoint.isOffline()){
						editBtn.setVisible(true);
						doneBtn.setVisible(false);
						cancelBtn.setVisible(false);
					}
					removeBtn.setVisible(true);
				}
				super.setVisible(visible);
			}
			/*
			 * Update attachment item metadata failed 
			 */
			public void onFailure(Throwable error) {
				GwtClientUtils.processError(error);
			}
			/* 
			 * Update attachment item metadata success. 
			 */
			public void onSuccess(String errCode) {
				if(errCode != null && errCode.trim().length() > 0){
					message.error(ErrorCode.getMessage(errCode,null));
				}
				switchEdit(2);
			}
		}
	}
	//********************************************************************
	//                       Private class : Uploading Panel
	//********************************************************************
	private class UploadingPanel extends SimplePanel{
		private VerticalPanel uploadingListPanel = new VerticalPanel();
		private HorizontalPanel monitor = new HorizontalPanel();
		private UploadProcessAsync uploadingAsync = new UploadProcessAsync();
		private ProgressBar progressBar = new ProgressBar();
		public UploadingPanel(){
			monitor.add(progressBar);
			progressBar.setVisible(false);
			
			VerticalPanel main = new VerticalPanel();
			main.add(monitor);
			main.add(uploadingListPanel);

			DOM.setElementAttribute(this.getElement(), "width","100%");
			
			monitor.setSize("100%", "100%");
			uploadingListPanel.setSize("100%", "100%");
			main.setSize("100%", "100%");
			this.setWidget(main); 
		}
		
		public void clear(){
			uploadingListPanel.clear();
			progressBar.setVisible(false);
		}
		/**
		 * @param index
		 */
		public void removeItem(String index) {
			for(Iterator<Widget> iter = uploadingListPanel.iterator();iter.hasNext();){
				Widget obj = iter.next();
				if(obj instanceof HorizontalPanel){
					HorizontalPanel itemPanel = (HorizontalPanel)obj;
					Hidden indexHidden = (Hidden) itemPanel.getWidget(0);
					if(indexHidden.getValue().trim().equals(index.trim())){
						iter.remove();
						break;
					}
				}
			}
			
		}

		public void addUploadingItem(final String filename, final String index){
			
			final HorizontalPanel itemPanel = new HorizontalPanel();
			Hidden indexHidden = new Hidden("itemIndex");
			indexHidden.setValue(index);
			Label nameLabel = new Label(filename);
			ClickLink removeButton = new ClickLink(Msg.consts.remove());
			removeButton.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					//The remove string pattern: /filename1/filename2/filename3
					String removed = removedList.getValue();
					if(removed == null)
						removed = "";
					removed += "/" + index;
					removedList.setValue(removed);
					uploadingListPanel.remove(itemPanel);
				}
			});
			itemPanel.add(indexHidden);
			itemPanel.add(nameLabel);
			itemPanel.add(removeButton);
			itemPanel.setCellWidth(removeButton, "100px");
			itemPanel.setWidth("100%");
			uploadingListPanel.add(itemPanel);
		}

		/**
		 * 
		 */
		public void checkProgress() {
			progressBar.setVisible(true);
			HelperControllerAsync helperController = ControllerFactory.getHelperController();
			helperController.checkUploadingStatus(uploadingAsync);
			
		}

		public void setProcess(double percent,final String text) {
			progressBar.setVisible(true);
			progressBar.setTextFormatter(new TextFormatter(){
				protected String getText(ProgressBar bar, double curProgress) {
					return text;
				}
			});
			progressBar.setProgress(percent);

		}

		public void done() {
			progressBar.setTextFormatter(new TextFormatter(){
				protected String getText(ProgressBar bar, double curProgress) {
					return "";
				}
			});
			progressBar.setProgress(0);
			progressBar.setVisible(false);
			
			//don't clear uploadingListPanel, which will clear from UploadedPanel calls UploadingPanel.removeItem()
		}
	}

	//********************************************************************
	//               Private class : Upload Form Panel
	//********************************************************************
	
	private class UploadFormPanel extends SimplePanel  implements SubmitHandler, SubmitCompleteHandler{
		
		private VerticalPanel panel = new VerticalPanel();
		
		private FormPanel uploadForm = new FormPanel();
		private Hidden spaceUnameHidden = new Hidden("spaceUname");
		private Hidden pageUuidHidden = new Hidden("pageUuid");
		private Hidden draftHidden = new Hidden("draft");
		
		//the widget index of upload in item panel. They are hardcode, need modify if FileItem panel changed
		private static final int WIDGET_INDEX_UPLOADER = 1; 
		private static final int WIDGET_INDEX_DESC = 3; 
		private boolean toView;


		public UploadFormPanel() {
			String baseUrl = GwtClientUtils.getBaseUrl();

			uploadForm.setAction(baseUrl + MODULE_ACTION_URI);
			uploadForm.setMethod(FormPanel.METHOD_POST);
			uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);
			uploadForm.addSubmitHandler(this);
			uploadForm.addSubmitCompleteHandler(this);
			
			clear();

			panel.setStyleName(Css.UPLOAD_FORM);
			uploadForm.setWidget(panel);
			this.setWidget(uploadForm);
			
		}
		public void clear(){
			panel.clear();
			panel.add(spaceUnameHidden);
			panel.add(pageUuidHidden);
			panel.add(draftHidden);
		}
	
		/**
		 * Add new blank upload item for upload. 
		 * @param itemsPanel
		 */
		private void addUploderItem() {
			final HorizontalPanel itemPanel = new HorizontalPanel();
			final FileUpload upload = new FileUpload();
			final TextBox desc = new TextBox();
			final CheckBox shared = new CheckBox();
			desc.addFocusHandler(KeyCaptureListener.instance());
			desc.addBlurHandler(KeyCaptureListener.instance());
			
			String id = Integer.valueOf(++uploadingIndex).toString();
			upload.setName("file" + id);
			desc.setName("desc" + id);
			shared.setName("shar" + id);
			CheckBox bulkUpload = new CheckBox(Msg.consts.bulk_upload());
			DOM.setElementAttribute(bulkUpload.getElement(), "value", "true");
			bulkUpload.setName("bulk" + id);
			
//			Image helpOnBulk = new Image(IconBundle.I.get().help());
//			helpOnBulk.addClickHandler(new ClickHandler(){
//				public void onClick(ClickEvent event) {	
//					HelpPopup pop = new HelpPopup(HelpPopup.BULK_ATTACHMENT_UPLOAD);
//					int left = sender.getAbsoluteLeft() + 10;
//					int top = sender.getAbsoluteTop() + 10;
//					pop.setPopupPosition(left, top);
//					pop.show();
//				}
//			});
			final ClickLink removeLink = new ClickLink(Msg.consts.remove());
			itemPanel.add(new HTML(Msg.consts.file()+":"));
			itemPanel.add(upload);
			itemPanel.add(new HTML(Msg.consts.description() + ":"));
			itemPanel.add(desc);
			if(!AbstractEntryPoint.isOffline()){
				//temporary disable bulk upload for offline although it is possbile.
				itemPanel.add(bulkUpload);
//				itemPanel.add(helpOnBulk);
			}
			itemPanel.add(removeLink);
			itemPanel.setCellWidth(removeLink, "200px");
			itemPanel.setCellHorizontalAlignment(removeLink, HasHorizontalAlignment.ALIGN_RIGHT);
//			itemPanel.add(shared);
			removeLink.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					itemPanel.clear();
					panel.remove(itemPanel);
					
					//only 3 hidden widget left.
					if(panel.getWidgetCount() <= 3)
						uploadBtn.setEnabled(false);
				}
			});
			
			uploadBtn.setEnabled(true);
			upload.setStyleName(Css.UPLOAD);
			desc.setStyleName(Css.BOX);
			panel.add(itemPanel);
		}


		private boolean hasAtt() {
			boolean hasAtt = false;
			for (Iterator<Widget> iter = panel.iterator();iter.hasNext();) {
				Widget obj = iter.next();
				if(obj instanceof HorizontalPanel){
					HorizontalPanel itemPanel = (HorizontalPanel) obj;
					FileUpload up = (FileUpload) itemPanel.getWidget(WIDGET_INDEX_UPLOADER);
					if (up.getFilename() != null && up.getFilename().trim().length() > 0) {
						hasAtt = true;
					}
				}
			}
			return hasAtt;
		}

		private void submitForm() {
			if(!hasAtt()){
				//try to cancel first, In this case: timer start from saving draft require, but attachment removed
				submitTimer.cancel();
				return;
			}
			if (main.getPageUuid() == null || main.getPageUuid().trim().length() == 0) {
				// just save draft to try to get pageUuid. Then after
				main.editPanel.setUploadReqired(true);
				main.editPanel.saveDraft(EditPanel.SAVE_AUTO_DRAFT_STAY_IN_EDIT);
				return;
			}
			pageUuidHidden.setValue(main.getPageUuid());
			spaceUnameHidden.setValue(main.getSpaceUname());
			
			//must before submit; as offline model, upload happens immediately after uploadForm.submit();
			//so add upload items to uploadingPanel to ensure remove items correctly.
			for (final Iterator<Widget> iter = panel.iterator(); iter.hasNext(); ) {
				final Widget obj = iter.next();
				if(obj instanceof HorizontalPanel){
					final HorizontalPanel itemPanel = (HorizontalPanel) obj;
					final FileUpload up = (FileUpload) itemPanel.getWidget(WIDGET_INDEX_UPLOADER);
					if(up.getFilename() != null && up.getFilename().trim().length() > 0){
						//only keep file name, remove path info.
						uploadingPanel.addUploadingItem(GwtUtils.getFileName(up.getFilename()),up.getName().substring(4));
					}
				}
			}
			//TODO: this will add one level history to browser, how to avoid?
			uploadForm.submit();
			
			//must after submit: remove item from FORM.
			for (final Iterator<Widget> iter = panel.iterator(); iter.hasNext(); ) {
				final Widget obj = iter.next();
				if(obj instanceof HorizontalPanel){
					final HorizontalPanel itemPanel = (HorizontalPanel) obj;
					final FileUpload up = (FileUpload) itemPanel.getWidget(WIDGET_INDEX_UPLOADER);
					if(up.getFilename() != null && up.getFilename().trim().length() > 0){
						iter.remove();
					}
				}
			}
			//only left 3 hidden widget.
			if(panel.getWidgetCount() <= 3)
				uploadBtn.setEnabled(false);
			
			if(!AbstractEntryPoint.isOffline())
				uploadingPanel.checkProgress();

		}
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// method from SubmitHandler
		public void onSubmit(SubmitEvent event) {
		    //remove some offline_code here(0726)
//			if(AbstractEntryPoint.isOffline()){
//				//don't submit 
//				event.cancel();
//				
//				//to ask offline upload 
//				OfflineUploader offUploader = new OfflineUploader();
//				
//				//add each item to OfflineUploadModel
//				for (Iterator<Widget> iter = panel.iterator(); iter.hasNext(); ) {
//					Widget obj = iter.next();
//					if(obj instanceof HorizontalPanel){
//						HorizontalPanel itemPanel = (HorizontalPanel) obj;
//						FileUpload up = (FileUpload) itemPanel.getWidget(WIDGET_INDEX_UPLOADER);
//						TextBox desc = (TextBox) itemPanel.getWidget(WIDGET_INDEX_DESC);
//						if(up.getFilename() != null && up.getFilename().trim().length() > 0){
//							offUploader.add(up,desc.getText());
//						}
//					}
//				}//end for
//				
//				
//				String jsonAtts = offUploader.upload(spaceUnameHidden.getValue(),pageUuidHidden.getValue()
//						,NumberUtil.toInt(draftHidden.getValue(),0));
//				mergeAttachments(jsonAtts);
//			}
		}

		public void onSubmitComplete(SubmitCompleteEvent event) {
			String results = GwtClientUtils.getFormResult(event);
			if(results == null)
				return;
			mergeAttachments(results);

			//This will happen when user put some attachment on FILE input, and click "Save Page" button
			//uploading attachments will occur, the page will jump to view page once uploading finish: 
			if(toView){
				main.editPanel.exitConfirm(false);
				main.editPanel.setDirty(false);
				main.switchTo(PageMain.VIEW_PANEL);
				main.viewPanel.resetToken();
			}
		}
		public void setToView(boolean toView){
			this.toView = toView;
		}
		public void setDraftStatus(int draftStatus) {
			//0:normal,1:manual draft,2:auto draft
			uploadFormPanel.draftHidden.setValue(Integer.valueOf(draftStatus).toString());
		}
	}
	
	// ********************************************************************
	// Form upload handler methods
	// ********************************************************************
	private class UploadProcessAsync implements AsyncCallback<UploadProgressModel>{

		private Timer timer = new CheckTimer();
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}

		public void onSuccess(UploadProgressModel model) {

			if(!GwtClientUtils.preSuccessCheck(model,null)){
				return;
			}
			if(model.status == UploadProgressModel.UPLOADING){
				float percent = (float) Math.ceil(((float)model.bytesRead / (float)model.totalSize) * 100);
				String read = GwtUtils.convertHumanSize(model.bytesRead);
				String total = GwtUtils.convertHumanSize(model.totalSize);

				uploadingPanel.setProcess(percent,read+"/" +total);
				timer.schedule(PROGRESS_DELAY);
			}else{
				//upload is done. clean
				uploadingPanel.done();
			}
			
		}
		private class CheckTimer extends Timer{
			private int reqestCount = 0;
			public void run() {
				reqestCount++;
				if(PROGRESS_DELAY * reqestCount > PROGRESS_TIMEOUT){
					//stop update progress bar.
					return;
				}
				uploadingPanel.checkProgress();
			}
		}

	}
	//********************************************************************
	//               Timer class
	//********************************************************************
	
	private class SubmitTimer extends Timer{

		public void run() {
			if(submitCounter < 1){
				upload(false,SharedConstants.AUTO_DRAFT);
				submitCounter = 0;
				submitTimer.cancel();
				counter.setText("");
			}else{
				counter.setText(Integer.valueOf(submitCounter).toString());
				submitCounter--;
			}
		}
	}



}
