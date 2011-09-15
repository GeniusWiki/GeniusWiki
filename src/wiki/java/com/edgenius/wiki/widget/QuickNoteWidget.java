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
package com.edgenius.wiki.widget;

import java.util.List;

import org.springframework.context.ApplicationContext;

import com.edgenius.core.UserSetting.QuickNote;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.wiki.gwt.client.model.QuickNoteModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.service.RenderService;

/**
 * @author Dapeng.Ni
 */
public class QuickNoteWidget  extends AbstractWidgetTemplate {
	private Widget obj = null;
	private RenderService renderService;
	private MessageService messageService;
	@Override
	public boolean isAllowView(User viewer) {
		if(viewer == null || viewer.isAnonymous())
			return false;
		else
			return true;
	}
	public void reset() {
		obj = null;
	}
	//JDK1.6 @Override
	public Widget createWidgetObject(String key) {
		if(obj == null){
			obj = new Widget();
			obj.setType(getType());
			obj.setUuid(SharedConstants.QUICKNOTE_KEY);
			obj.setTitle(messageService.getMessage("quicknote.title"));
			obj.setDescription(messageService.getMessage("quicknote.desc"));
		}
		return obj;
	}
	//JDK1.6 @Override
	public Widget invoke(String key, User viewer) throws WidgetException {
		
		//invoke - get first page messages
		List<QuickNote> notes = viewer.getSetting().getQuickNotes();
		QuickNoteModel model = new QuickNoteModel();
		if(notes != null && notes.size() > 0){
			//get latest version 
			for (QuickNote note : notes) {
				if(model.version <= note.getVersion()){ //could be both 0, so also include equals
					model.version = note.getVersion();
					model.renderContent = renderService.renderHTML(note.getNote());
					model.content = note.getNote();
					model.createDate = note.getCreateDate();
				}
			}
		}else{
			//default as null, let client side to decide
			model = null;
		}
		Widget obj = new Widget();
		obj.setRenderContent(model);
		return obj;
	}
	public void init(ApplicationContext applicationContext){
		renderService = (RenderService) applicationContext.getBean(RenderService.SERVICE_NAME);
		messageService = (MessageService) applicationContext.getBean(MessageService.SERVICE_NAME);
	}
}
