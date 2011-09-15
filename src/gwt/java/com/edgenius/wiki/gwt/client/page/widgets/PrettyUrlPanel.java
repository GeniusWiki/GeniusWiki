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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class PrettyUrlPanel extends SimplePanel{
	private final String tinyID = HTMLPanel.createUniqueId();
	private final String meaningID = HTMLPanel.createUniqueId();
	private final String shellID = HTMLPanel.createUniqueId();
	
	private HTML tinyLink = new HTML();
	private HTML tinyMail = new HTML();
	
	private HTML meaningLink = new HTML();
	private HTML meaningMail = new HTML();
	
	private HTML shellLink = new HTML();
	private HTML shellMail = new HTML();
	
	private ClickLink meaningCopy = new ClickLink(Msg.consts.copy_clip());
	private ClickLink tinyCopy = new ClickLink(Msg.consts.copy_clip());
	private ClickLink shellCopy = new ClickLink(Msg.consts.copy_clip());
	
	private JavaScriptObject tinyClip;
	private JavaScriptObject meaningClip;
	private JavaScriptObject shellClip;
	
	public PrettyUrlPanel(){
		FlexTable panel = new FlexTable();
		
		DOM.setElementAttribute(tinyCopy.getElement(), "id", tinyID+"copyclip");
		DOM.setElementAttribute(meaningCopy.getElement(), "id", meaningID+"copyclip");
		DOM.setElementAttribute(shellCopy.getElement(), "id", shellID+"copyclip");
		
		//tiny url
		int idx=0;
		panel.setWidget(0, idx, new Label(Msg.consts.tiny_url() + ": "));
		panel.getColumnFormatter().setWidth(idx,"120px");
		
		idx++;
		panel.setWidget(0, idx, tinyLink);
		
		idx++;
		panel.setWidget(0, idx, tinyMail);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(0, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		idx++;
		panel.setWidget(0, idx, tinyCopy);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(0, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		//meaning url
		idx=0;
		panel.setWidget(1, idx, new Label(Msg.consts.meaning_url() + ": "));
		panel.getColumnFormatter().setWidth(idx,"120px");
		
		idx++;
		panel.setWidget(1, idx, meaningLink);
		
		idx++;
		panel.setWidget(1, idx, meaningMail);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(1, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		idx++;
		panel.setWidget(1, idx, meaningCopy);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(1, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		//shell url
		idx=0;
		panel.setWidget(2, idx, new Label(Msg.consts.shell_url() + ": "));
		panel.getColumnFormatter().setWidth(idx,"120px");
		
		idx++;
		panel.setWidget(2, idx, shellLink);
		
		idx++;
		panel.setWidget(2, idx, shellMail);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(2, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		idx++;
		panel.setWidget(2, idx, shellCopy);
		panel.getColumnFormatter().setWidth(idx,"120px");
		panel.getFlexCellFormatter().setHorizontalAlignment(2, idx, HasHorizontalAlignment.ALIGN_CENTER);
		
		
		//panel
		panel.setSize("100%", "100%");
		this.setWidget(panel);
		this.setStyleName(Css.ATTACHMNET_PANEL);
		
		//only panel initialised , the ID field is valid 
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				tinyClip = initClip(tinyID+"copyclip", tinyID, PrettyUrlPanel.this);
				meaningClip = initClip(meaningID+"copyclip", meaningID, PrettyUrlPanel.this);
				shellClip = initClip(shellID+"copyclip", shellID, PrettyUrlPanel.this);
				if(!PrettyUrlPanel.this.isVisible()){
					hideClip(tinyClip);
					hideClip(meaningClip);
					hideClip(shellClip);
				}
			}
			
		});
	}

	public void refresh(){
		String tlink = GwtClientUtils.getBaseUrl()+ SharedConstants.URL_TINY_PAGE+"/" + PageMain.getPageUuid();
		String turl = "<a title=\"tiny url\" id=\""+tinyID+"\" href=\""+tlink+"\">" +tlink+"</a>";
		String tmail = "<a title=\"Send tiny url by email\" href=\"mailto:?body="+URL.encodeQueryString(tlink)+"\">"+ Msg.consts.email_it()+"</a>";
		tinyLink.setHTML(turl);
		tinyMail.setHTML(tmail);
		
		//detect if this page support meaning URL
		if(GwtUtils.isSupportInURL(PageMain.getSpaceUname()) && GwtUtils.isSupportInURL(PageMain.getCurrentPageTitle())){
			meaningCopy.setVisible(true);
			meaningMail.setVisible(true);
			String mlink = GwtClientUtils.getBaseUrl()+ SharedConstants.URL_PAGE+"/"  + URL.encodeQueryString(PageMain.getSpaceUname()) 
			+ "/" + URL.encodeQueryString(PageMain.getCurrentPageTitle());
			String murl = "<a title=\"meaning url\" id=\""+meaningID+"\" href=\""+mlink+"\">" +mlink+"</a>";
			String mmail = "<a title=\"Send meaning url by email\" href=\"mailto:?body="+URL.encodeQueryString(mlink)+"\">"+ Msg.consts.email_it()+"</a>";
			meaningLink.setHTML(murl);
			meaningMail.setHTML(mmail);
		}else{
			meaningCopy.setVisible(false);
			meaningMail.setVisible(false);
			meaningLink.setHTML(Msg.consts.meaning_url_not_available());
		}
		
		if(PageMain.getShellUrl()  != null){
			String slink = PageMain.getShellUrl() + SharedConstants.URL_PAGE+"/" + URL.encodeQueryString(PageMain.getSpaceUname()) 
					+ "/" + URL.encodeQueryString(PageMain.getCurrentPageTitle());
			String surl = "<a title=\"Shell url\" id=\""+shellID+"\" href=\""+slink+"\">" +slink+"</a>";
			String smail = "<a title=\"Send Shell url by email\" href=\"mailto:?body="+URL.encodeQueryString(slink)+"\">"+ Msg.consts.email_it()+"</a>";
			shellLink.setHTML(surl);
			shellMail.setHTML(smail);
		}else{
			shellCopy.setVisible(false);
			shellMail.setVisible(false);
			shellLink.setHTML(Msg.consts.shell_url_not_available());
		}
	}
	public void setVisible(boolean visible){
		super.setVisible(visible);
		showCopyClip(visible);
	}
	
	private void showCopyClip(boolean visible) {
		if(visible){
			if(tinyClip != null)
				showClip(tinyClip);
			if(meaningClip != null)
				showClip(meaningClip);
			if(shellClip != null)
				showClip(shellClip);
		}else{
			if(tinyClip != null)
				hideClip(tinyClip);
			if(meaningClip != null)
				hideClip(meaningClip);
			if(shellClip != null)
				hideClip(shellClip);
		}
	}
	/**
	 * Call by native method
	 * @param textAreaID
	 */
	private void copyComplete(String textAreaID){
		if(tinyID.equals(textAreaID)){
			//flash a message, the go back...
			tinyCopy.setText(Msg.consts.done());
			new Timer(){
				public void run() {
					tinyCopy.setText(Msg.consts.copy_clip());
				}
			}.schedule(2000);
		}else if(meaningID.equals(textAreaID)){
			//flash a message, the go back...
			meaningCopy.setText(Msg.consts.done());
			new Timer(){
				public void run() {
					meaningCopy.setText(Msg.consts.copy_clip());
				}
			}.schedule(2000);
		}else if(shellID.equals(textAreaID)){
			//flash a message, the go back...
			shellCopy.setText(Msg.consts.done());
			new Timer(){
				public void run() {
					shellCopy.setText(Msg.consts.copy_clip());
				}
			}.schedule(2000);
		}
	
	}
	private static native void showClip(JavaScriptObject clip)/*-{
		clip.show();
	}-*/;
	private static native void hideClip(JavaScriptObject clip)/*-{
		clip.hide();
	}-*/;

	//!!!There is flash width/height hardcode here!!!
	private static native JavaScriptObject initClip(String clipBtnId, String textAreaID, PrettyUrlPanel btn)/*-{
		var clip = new $wnd.ZeroClipboard.Client();
		clip.setHandCursor( true );
		clip.addEventListener('mouseover', function () {
			var inElement = $wnd.document.getElementById(textAreaID);
	        clip.setText(inElement.innerHTML);
		});
		clip.addEventListener('complete',function() {
			btn.@com.edgenius.wiki.gwt.client.page.widgets.PrettyUrlPanel::copyComplete(Ljava/lang/String;)(textAreaID);
		});
		clip.glue(clipBtnId, 210, 21);
		
		return clip;
	}-*/;

}
