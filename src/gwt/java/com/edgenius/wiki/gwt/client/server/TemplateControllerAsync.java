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
package com.edgenius.wiki.gwt.client.server;

import com.edgenius.wiki.gwt.client.model.PageModel;
import com.edgenius.wiki.gwt.client.model.TemplateListModel;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Dapeng.Ni
 */
public interface TemplateControllerAsync extends RemoteServiceAsync{
	
	void getTemplates(String spaceUname, boolean withOtherSpaces, boolean adminOnly, AsyncCallback<TemplateListModel> callback);

	void deleteTemplate(String spaceUname,  Integer templID, AsyncCallback<TemplateListModel> callback);

	void editTemplate(String spaceUname, Integer templID, AsyncCallback<PageModel> callback);


	void previewTemplate(String spaceUname, String content, boolean richEnabled,AsyncCallback<PageModel> callback);

	
	void saveTemplate(String spaceUname, Integer templID, String title, String desc, String content, boolean richEnabled,
			boolean shared,  AsyncCallback<TemplateListModel> callback);

	void createTemplate(String spaceUname,  AsyncCallback<PageModel> callback);

	void getTemplate(String spaceUname, Integer templID,  AsyncCallback<TextModel> callback);
}
