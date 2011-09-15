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
package com.edgenius.wiki;

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.widget.ActivityLogWidget;
import com.edgenius.wiki.widget.MyDraftWidget;
import com.edgenius.wiki.widget.MyFavoriteWidget;
import com.edgenius.wiki.widget.MyMessageWidget;
import com.edgenius.wiki.widget.MyWatchedWidget;
import com.edgenius.wiki.widget.QuickNoteWidget;

/**
 * @author Dapeng.Ni
 */
public class InstanceSetting {

	//system default Dashboard markup - just put it as null, it set as default in SettingServiceImpl.getInstanceSetting()
	private String dashboardMarkup = null;
	//system default Portal layout
	private List<String> homeLayout = new ArrayList<String>();
	private static final String S = SharedConstants.PORTLET_SEP;
	
	public InstanceSetting(){
		//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		//               system default Dashboard layout
	    //column one
//		homeLayout.add(WelcomeWidget.class.getName()+S+S+"0"+S+"0");
//		homeLayout.add(FeedbackWidget.class.getName()+S+S+"1"+S+"0");
//		homeLayout.add(SavemeWidget.class.getName()+S+S+"2"+S+"0");

		//column 2nd
		homeLayout.add(QuickNoteWidget.class.getName()+S+S+"0"+S+"1");
		homeLayout.add(ActivityLogWidget.class.getName()+S+S+"1"+S+"1");
		
		//column 3rd
		homeLayout.add(MyMessageWidget.class.getName()+S+S+"0"+S+"2");
		homeLayout.add(MyDraftWidget.class.getName()+S+S+"1"+S+"2");
		homeLayout.add(MyFavoriteWidget.class.getName()+S+S+"2"+S+"2");
		homeLayout.add(MyWatchedWidget.class.getName()+S+S+"3"+S+"2");
	}
	public List<String> getHomeLayout() {
		return homeLayout;
	}

	public void setHomeLayout(List<String> homeLayout) {
		this.homeLayout = homeLayout;
	}
	
	public String getDashboardMarkup() {
		return dashboardMarkup;
	}
	public void setDashboardMarkup(String dashboardMarkup) {
		this.dashboardMarkup = dashboardMarkup;
	}
}
