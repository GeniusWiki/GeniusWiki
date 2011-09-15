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
package com.edgenius.wiki.gwt.client.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public abstract class RenderPanel extends FlowPanel{
	private List content = new ArrayList();
	private String panelID;
	/**
	 * All RenderPanel instance must have an unique id value. This is useful for renderCallback(id) to 
	 * look up the specified RenderPanel scripts and eval them.
	 * @param id
	 */
	public RenderPanel(){
		panelID = HTMLPanel.createUniqueId();
		DOM.setElementAttribute(this.getElement(),"id", panelID);
	}
	
	public void insert(Widget widget, int beforeIndex){
		if(beforeIndex > content.size())
			return;
		
		content.add(beforeIndex, widget);
	}
	
	public void insert(String text, int beforeIndex){
		if(beforeIndex > content.size())
			return;
		
		content.add(beforeIndex, text);
	}
	public void add(String text){
		content.add(text);
	}
	public void add(Widget widget){
		content.add(widget);	
	}
	
	public int getContentWidgetCount(){
		return content.size();
	}
	/**
	 * @return
	 */
	public Iterator<Object> iteratorContent(){
		return new Iterator(){
			Iterator iter = content.iterator();
			public boolean hasNext() {
				return iter.hasNext();
			}

			public Object next() {
				return iter.next();
			}

			public void remove() {
				iter.remove();
			}
		};
	}
	public Object getContentWidget(int idx){
		return content.get(idx);
	}
	
	public boolean removeCotentWidget(int idx){
		if(idx >= content.size())
			return false;
		
		content.remove(idx);
		return true;
	}
	
	public void clear(){
		clearPanel();
		content.clear();
	}


	/**
	 * I don't remember exactly as old bug database is hacked and removed. The reason why use this method to 
	 * add render content is because HTML DOM Hierarchy model. If simply use FlowPanel.add(), it may break DOM hierarchy in 
	 * some case.
	 * Submit the latest content, must be call after any modify content method, such as remove(), add(), insert()
	 * or iterator.remove() etc.
	 */
	public void submit(){
		StringBuffer render = new StringBuffer();
		Map<String, Widget> widgetMap = new HashMap<String, Widget>();
		
		int size = content.size();
		if(size == 0){
			//IE issue, if content is blank, the right size author info panel will 100% width to occupy entire page
			render.append("&nbsp;");
		}else{
			for(int idx=0;idx<size;idx++){
				Object obj = content.get(idx);
				if(obj instanceof String){
					render.append((String)obj);
				}else if(obj instanceof Widget){
					String uid = HTMLPanel.createUniqueId();
					widgetMap.put(uid, (Widget)obj);
					//surround with <![CDATA[]]>: This is fix bug when render something like: {html}<!{html}{feedback}
					//Render content "!<<span id=123></span>" in FF and IE will break document.getElementById()
					//This means the HTMLPanel can't work as the span element can not be found out.
					//use <![CDATA[]]> rather than <!----> is because if "<![" is before text, then <!----> won't work.
					//but <![CDATA[]]> work both <! or <![ before the macro widget.
					render.append("<![CDATA[]]><span id='"+uid+"'></span><![CDATA[]]>");
				}
			}
		}
		
		HTMLPanel panel = new HTMLPanel(render.toString());
		
		for(Entry<String, Widget> entry : widgetMap.entrySet()){
			panel.add(entry.getValue(),entry.getKey());
			
		}
		//reset panel to empty, then render new content
		clearPanel();
	    
		super.add(panel);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				renderCallback(panelID);
			}
		});
	}
	
	/**
	 * clean all widget from this panel
	 */
	private void clearPanel() {
		//cannot call super.clear directly, because Panel.clear() execute iterator.remove(), it already overwrite by this class
	    Iterator it = super.iterator();
	    while (it.hasNext()) {
	      Widget w = (Widget) it.next();
	      super.remove(w);
	    }
	}
	private native void renderCallback(String panelID) /*-{
	    $wnd.renderCallback(panelID);
	}-*/;
	
}
