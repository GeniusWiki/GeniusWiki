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

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class CandidateListPanel  extends SimplePanel{
	FlowPanel panel = new FlowPanel();
	List values = new ArrayList();
	public CandidateListPanel(){
		panel.setWidth("99%");
		this.setWidth("100%");
		this.setWidget(panel);
		this.setStyleName(Css.CANDIDATE_LIST);
	}
	public void addCandidate(String text, final Object model){
		for(Iterator iter = values.iterator();iter.hasNext();){
			//don't allow add same value over once. 
			Object obj = iter.next();
			if(obj.equals(model))
				return;
		}
			
		//cache value
		values.add(model);
		
		//UI part
		final ClickLink candidate = new ClickLink(text);
		candidate.setTitle(Msg.consts.click_remove());
		if(panel.getWidgetCount() > 0){
			//if there are more candidate, then add ","
			HTML sep = new HTML(", ");
			panel.add(sep);
		}
		panel.add(candidate);
		candidate.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				values.remove(model);
				Widget sep = null;
				//remove this candidate and its following "," if it has
				for(Iterator iter = panel.iterator();iter.hasNext();){
					if(iter.next() == candidate){
						iter.remove();
						if(iter.hasNext()){
							sep = (Widget) iter.next();
						}
						break;
					}
					
				}
				if(sep instanceof HTML){
					panel.remove(sep);
				}else{
					//maybe remove last second candidate, which has not "," but before it, there are should be ","
					int count = panel.getWidgetCount();
					if(count > 0 && (panel.getWidget(count-1) instanceof HTML)){
						panel.remove(count-1);
					}
				}
				if(panel.getWidgetCount() == 0){
					CandidateListPanel.this.setStyleName(Css.CANDIDATE_LIST);
				}else{
					CandidateListPanel.this.setStyleName(Css.CANDIDATE_LIST_ENALBE);
				}
			}
		});
		
		if(panel.getWidgetCount() == 0){
			this.setStyleName(Css.CANDIDATE_LIST);
		}else{
			this.setStyleName(Css.CANDIDATE_LIST_ENALBE);
		}
	}
	public void clear(){
		panel.clear();
		values.clear();
		this.setStyleName(Css.CANDIDATE_LIST);
	}
	public List getCandidates(){
		return values;
	}
}
