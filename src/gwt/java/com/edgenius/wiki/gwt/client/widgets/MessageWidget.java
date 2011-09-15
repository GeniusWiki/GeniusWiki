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

import java.util.Iterator;

import com.edgenius.wiki.gwt.client.Css;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * TODO: this call can not handle mixed message type display
 * @author Dapeng.Ni
 */
public class MessageWidget extends SimplePanel {
	public static final int INFO = 1;
	public static final int WARNING = 2;
	public static final int ERROR = 3;
	public static final int SUCCESS = 4;
	//15s
	public static final int TIMEOUT = 15000;
	
	private FlowPanel msgContainer = new FlowPanel();
	private boolean allowClose;
	
	public MessageWidget(){
		this(true);
	}
	public MessageWidget(boolean allowClose){
		this.allowClose = allowClose;
		this.setStyleName(Css.MESSAGE_PANEL);
		setWidget(msgContainer);
	}
	
	public String success(HorizontalPanel panel){
		return show(panel,-1,SUCCESS, false,allowClose);
	}
	public String error(HorizontalPanel panel){
		return show(panel,-1,ERROR, false,allowClose);
	}
	public String error(String msg){
		return show(msg,-1,ERROR, false,allowClose);
	}

	public String warning(HorizontalPanel panel){
		return show(panel,-1,WARNING, false,allowClose);
	}
	public String warning(HorizontalPanel panel, boolean allowClose){
		return show(panel,-1,WARNING, false,allowClose);
	}
	public String warning(String msg){
		return show(msg,-1,WARNING, false,allowClose);
	}
	public String warning(String msg, boolean allowClose){
		return show(msg,-1,WARNING, false,allowClose);
	}

	public String info(String msg){
		return show(msg,-1,INFO, false,allowClose);
	}
	
	public String info(HorizontalPanel message, int timeout,boolean append){
		return show(message, timeout, INFO, append,allowClose);
	}
	public String warning(HorizontalPanel message, int timeout,boolean append){
		return show(message, timeout, WARNING, append,allowClose);
		
	}
	/**
	 * 
	 */
	public void fadeout() {
		//TODO: implement fadeout effect, so far only simple remove message
		this.cleanMessage();
	}


	public void cleanMessage(){
		msgContainer.clear();
	}
	
	/**
	 * Remove message by given message UUID
	 * @param uuid
	 */
	public void removeMessage(String uuid){
		if(uuid == null || uuid.trim().length() == 0)
			return;
		
		for(Iterator iter = msgContainer.iterator();iter.hasNext();){
			MessagePanel panel = (MessagePanel) iter.next();
			if(panel.getUuid().equals(uuid)){
				iter.remove();
				break;
			}
		}
		
	}

	//********************************************************************
	//               Private methods
	//********************************************************************
	/**
	 * 
	 * Set simple text as message content. And message will disappear after timeout.
	 * If timeout equals -1, it will always appear.
	 * @param msg
	 * @param timeout
	 * @return message UUID
	 */
	private String show(String msg,int timeout, int type, boolean append, boolean allowClose){
		Label msgLabel = new Label(msg);
		HorizontalPanel panel = new HorizontalPanel();
		panel.add(msgLabel);
		return show(panel, timeout, type, append, allowClose);
	}
	
	private String show(final HorizontalPanel message, int timeout, int type, boolean append, boolean allowClose) {
		//if this message does not append to others, then clear whole panel
		if(!append){
			cleanMessage();
		}
		
		final MessagePanel msgPanel = new MessagePanel(message,type,allowClose);
		msgContainer.add(msgPanel);
		msgContainer.setVisible(true);
		
		if(timeout > 0){
			Timer timer = new Timer(){
				public void run() {
					removeMessage(msgPanel.getUuid());
				}
			};
			timer.schedule(timeout);
		}
		
		return msgPanel.getUuid();
	}


	private class MessagePanel extends HorizontalPanel{
		private String uuid;
		
		public MessagePanel(HorizontalPanel msg, int type, boolean allowClose){
			uuid = Integer.valueOf(Random.nextInt()).toString();
			
			Image img = null;
			if(type == ERROR){
				img = new Image(IconBundle.I.get().error());
			}else if(type == WARNING){
				img = new Image(IconBundle.I.get().warning());
			}else if(type == INFO){
				img = new Image(IconBundle.I.get().info());
			}else if(type == SUCCESS){
				img = new Image(IconBundle.I.get().success());
			}
			if(img != null){
				this.add(img);
				this.setCellWidth(img, "16px");
			}
			
			this.add(msg);
			
			for(Iterator iter = msg.iterator();iter.hasNext();){
				Widget widget = (Widget) iter.next();
				if((widget instanceof Label) ||(widget instanceof HTML)){
					widget.setStyleName(Css.MESSAGE);
				}
			}
			if(allowClose){
				CloseButton closeBtn = new CloseButton();
				closeBtn.addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event) {
						removeMessage(uuid);
					}
				});
				closeBtn.setStyleName(Css.RIGHT);
				this.add(closeBtn);
			}
				
			
			String style = getStyle(type);
			this.setStyleName(style);
		}
		public String getUuid() {
			return uuid;
		}
		
		/**
		 * @param type
		 * @return
		 */
		private String getStyle(int type) {
			//XXX:hardcode
			String style="";
			if(type == INFO){
				style= Css.MESSAGE_INFO;
			}else if(type == WARNING){
				style=Css.MESSAGE_WARNING;
			}else if(type == ERROR){
				style=Css.ERROR;
			}else if(type == SUCCESS){
				style=Css.MESSAGE_SUCCESS;
			}
			return style;
		}
	}


}
