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
package com.edgenius.wiki.gwt.client.page;

import java.util.Iterator;
import java.util.List;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.DiffListModel;
import com.edgenius.wiki.gwt.client.model.DiffModel;
import com.edgenius.wiki.gwt.client.model.PageItemModel;
import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.page.widgets.FunctionWidget;
import com.edgenius.wiki.gwt.client.render.TextRenderPanel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.UserProfileLink;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Abstract panel makes children class panel support Diff list on panel.
 * @author Dapeng.Ni
 */
public abstract class DiffPanel extends MessagePanel{
	
	public VersionConflictAsync versionAsync = new VersionConflictAsync();
	protected TextRenderPanel diffContent = new TextRenderPanel();

	protected FunctionWidget functionBtnBar;
	protected PageMain main;
	protected MessageWidget diffMessage = new MessageWidget();
	private boolean isConflictMerged = false;
	
	public DiffPanel(final PageMain main){
		this.main = main;
		
		diffContent.setVisible(false);
	    //function buttons
	    functionBtnBar = new FunctionWidget(main);
	}

	/**
	 * NOTE: <b>Override method must call super.diffRendered();</b>
	 * sub class could call this method to make diff panel visible.
	 */
	protected void diffRendered(){
		diffMessage.setVisible(true);
		message.setVisible(false);
	}
	/**
	 * NOTE: <b>Override method must call super.diffResume();</b>
	 */
	protected void diffResume(){
		diffMessage.setVisible(false);
		message.setVisible(true);
	}
		
	
	protected String getDiffMergeResult(){
		if(!isConflictMerged)
			return null;
		
		//restore edit page from Diff compare panel
		StringBuffer text = new StringBuffer();
		if(isConflictMerged){
			//in edit mode, so although page modified, no need submit immediately. Just refresh editor text box
			for(Iterator iter = diffContent.iteratorContent();iter.hasNext();){
				Object obj = iter.next();
				if(obj instanceof String){
					text.append((String)obj);
				}else if(obj instanceof ClickLink){
					DiffModel m = (DiffModel) ((ClickLink)obj).getObject();
					text.append(m.content);
				}
			}
			
		}
		
		//This is reverse handling is refer to DiffServiceImpl.split();
		return text.toString().replaceAll("<br>","\r");
	}

	//********************************************************************
	//               Private class
	//********************************************************************
	/*
	 * There are 2 different type DiffListModel returned from server side:
	 * <li>History comparison: Just flat html and only one DiffModel in DiffListModel.revs(DiffModel) list </li>
	 * <li>Saving version conflict: Has multiple DiffModel in list, need show popup menu to ask user "reject" or "merge". </li>
	 */
	private void fillDiffContent(DiffListModel model){
		List<DiffModel> diffList = model.revs;
		//now render diff object
		diffContent.clear();
		
		isConflictMerged = false;
		int idx=0;
		for(Iterator<DiffModel> iter = diffList.iterator();iter.hasNext();){
			final DiffModel diff = iter.next();
			if(model.type == DiffListModel.FLAT_TYPE){
				diffContent.add(diff.content);
				//only one element exist, so not necessary to continue;
				break;
			}
			diff.index = idx++;
			if(diff.type == DiffModel.NOCHANGE){
				diffContent.add(diff.content);
			}else{
				ClickLink link = new ClickLink(diff.content);
				if(diff.type == DiffModel.ADD)
					link.addStyleName(Css.DIFF_INSERTION);
				else
					link.addStyleName(Css.DIFF_DELETION);
				
				link.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						UIObject sender = (UIObject) event.getSource();
						DiffActionMenu menu = new DiffActionMenu(diff);
						int left = sender.getAbsoluteLeft() + 10;
						int top = sender.getAbsoluteTop() + 10;
						menu.setPopupPosition(left, top);
						menu.show();
						
						//TODO: need add windowResizeListener
						
					}
				});
				
				link.setObject(diff);
				diffContent.add(link);
			}
		}
		
		diffContent.submit();
	}
	
	//********************************************************************
	//               Inner Class
	//********************************************************************
	private class VersionConflictAsync implements  AsyncCallback<DiffListModel>{
		public void onFailure(Throwable error) {
			GwtClientUtils.processError(error);
		}
		
		public void onSuccess(DiffListModel model) {
			if(!GwtClientUtils.preSuccessCheck(model,diffMessage)){
				return;
			}
			
			diffMessage.cleanMessage();
			HorizontalPanel msgPanel = new HorizontalPanel();
			msgPanel.add(new Label(Msg.consts.comparing_view()));
			
			//if both version is not current
			if(model.ver1 != SharedConstants.CURRENT && model.ver2 != SharedConstants.CURRENT){
				String ver1 = model.ver1 == 0?Msg.consts.latest():Msg.consts.revision()+ " "+model.ver1;
				String ver2 = model.ver2 == 0?Msg.consts.latest():Msg.consts.revision()+ " "+model.ver2;
				msgPanel.add(new Label(Msg.consts.comparing() +" "+ver1+" <> "+ver2+"."));
			}
			
			diffMessage.warning(msgPanel, false);
			//switch to diffContent panel
			diffRendered();
			fillDiffContent(model);
			
		}
	}
	
   private void diffMessage(final PageModel model) {
        
        message.cleanMessage();
        
        String id1 = HTMLPanel.createUniqueId();
        //head
        StringBuffer buf = new StringBuffer("<div class='historyAction'><div class='msg' id='").append(id1).append("'></div>");
        buf.append("<div class='action'>");
        
        //diff 1
        String idp1 = null,idp2 = null,idp3 = null,idn1 = null,idn2 = null,idn3 = null;
        idn1 = HTMLPanel.createUniqueId();
        idn2 = HTMLPanel.createUniqueId();
        idn3 = HTMLPanel.createUniqueId();
        buf.append("<div class='round next'><div class='version' id='").append(idn1).append("'></div><div class='author' id='")
        .append(idn2).append("'></div><div class='date' id='").append(idn3).append("'></div></div>");
        //diff 2
        idp1 = HTMLPanel.createUniqueId();
        idp2 = HTMLPanel.createUniqueId();
        idp3 = HTMLPanel.createUniqueId();

        buf.append("<div class='round prev'><div class='version' id='").append(idp1).append("'></div><div class='author' id='")
            .append(idp2).append("'></div><div class='date' id='").append(idp3).append("'></div></div>");
        
        //end
        buf.append("</div></div>");
        
        
        //put this message into 
        HTMLPanel msgPanel = new HTMLPanel(buf.toString());
        msgPanel.add(new Label(Msg.consts.comparing_view()), id1);
        
        diffActionMsg(msgPanel, model.prevHistoryItem, idn1, idn2, idn3);
        diffActionMsg(msgPanel, model.nextHistoryItem, idp1, idp2, idp3);
        
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(msgPanel);
        message.warning(panel,false);
    }
   
   private void diffActionMsg(HTMLPanel msgPanel, final PageItemModel history, String idp1,String idp2, String idp3) {
       
       String spaceUname = PageMain.getSpaceUname();
       
       Hyperlink preLink;
       if(history.version == -1){
           //latest page
           preLink= new Hyperlink(Msg.consts.latest(), GwtUtils.getSpacePageToken(spaceUname, history.title));
       }else{
           preLink= new Hyperlink(Msg.consts.revision() + " " + history.version
                   ,GwtUtils.buildToken(PageMain.TOKEN_HISTORY,spaceUname, String.valueOf(history.uid)));
       }
       
       UserProfileLink modifier = new UserProfileLink(history.modifier, spaceUname, history.modifierUsername,history.modifierPortrait);
       Label modifiedDate = new Label(GwtClientUtils.toDisplayDate(history.modifiedDate));
  
       msgPanel.add(preLink,idp1);
       msgPanel.add(modifier, idp2);
       msgPanel.add(modifiedDate, idp3);
   }
   
   
	private class DiffActionMenu extends PopupPanel{
		public DiffActionMenu(final DiffModel diff){
			//autohide popup: when mouse out click, this menu will hide
			super(true);
			
			VerticalPanel panel = new VerticalPanel();
			Label accept = new Label(Msg.consts.accept());
			Label deny = new Label(Msg.consts.deny());
			accept.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					DiffActionMenu.this.hide();
					accept(diff);
				}

			});
			deny.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					DiffActionMenu.this.hide();
					deny(diff);
				}
			});
			
			accept.setWidth("120");
			deny.setWidth("120");
			
			panel.add(accept);
			panel.add(deny);
			setStyleName(Css.DIFF_CONFLICT_HANDLE_MENU);
			setWidget(panel);
			
		}
		private void accept(DiffModel diff) {
			modify(diff,true);
		}
		private void deny(DiffModel diff) {
			modify(diff,false);
		}
		private void modify(DiffModel diff, boolean accept) {
			//mark dirty flag so that must render this part code to server for preview
			isConflictMerged = true;
			
			int size = diffContent.getContentWidgetCount();
			for(int idx=0;idx<size;idx++){
				Object obj = diffContent.getContentWidget(idx);
				if(obj instanceof ClickLink){
					ClickLink link = (ClickLink) obj;
					DiffModel model = (DiffModel) link.getObject();
					//same object, do accept
					if(model.index == diff.index){
						if(diff.type == DiffModel.ADD){
							//if this piece different is "ADD", if user click accept, remove old DiffModel add HTML.  
							//Otherwise user choose deny, just remove old DiffModel (means delete) 
							diffContent.removeCotentWidget(idx);
							if(accept){
								//TODO: there is bug in GWT to insert in FlowPanel.need waiting new version to validate or add patch myself. 
								if(size == idx+1){
									diffContent.add(diff.content);
								}else{
									diffContent.insert(diff.content, idx);
								}
								
							}
						}else if(diff.type == DiffModel.DEL){
							//it do reverse action with "ADD": if accept, only remove DiffModel(means delete)
							//,otherwise, remove DiffModel and add HTML
							diffContent.removeCotentWidget(idx);
							if(!accept){
								if(size == idx+1)
									diffContent.add(diff.content);
								else{
									diffContent.insert(diff.content, idx);
								}
							}
						}
						break;
					}
				}
			}
			diffContent.submit();
		}
	}
}
