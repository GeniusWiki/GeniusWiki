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

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.widgets.AbstractSecurityPanel;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public class PageSecurityDialogue extends DialogBox{

	public PageSecurityDialogue(PageMain main){
		
		PageSecurityPanel panel = new PageSecurityPanel(main.getPageUuid());
		this.setText(Msg.consts.page_restrict());
		this.setIcon(new Image(IconBundle.I.get().lock_add()));
		this.setWidget(panel);
		this.addStyleName(Css.PAGE_SECUIRTY_DIALOG);
		
		panel.load();
	}

	private class PageSecurityPanel extends AbstractSecurityPanel{

		/**
		 * @param resourceName page UUID
		 */
		public PageSecurityPanel(String resourceName) {
			super(resourceName, PageSecurityDialogue.this, false);
		}

		public int buildTableHeader(int startCol) {
			int row = 0;
			int col = startCol;

			table.setText(row,col++,Msg.consts.view());
			table.setText(row,col++,Msg.consts.modify());
			table.setText(row,col++,Msg.consts.remove());
			table.setText(row,col++,Msg.consts.view_comments());
			table.setText(row,col++,Msg.consts.create_comments());
			table.setText(row,col++,Msg.consts.offline());
			
			return ++row;
		}

		public boolean getRoleAdminReadonly(int operation) {
			return false;
		}

		public boolean getRoleAnonymousReadonly(int operation) {
			return false;
		}

		public int[] getValidOperations() {
			//READ,WRITE,REMOVE,COMMENTS_READ,COMMENTS_WRITE,OFFLINE
			return new int[]{ClientConstants.READ,ClientConstants.WRITE,ClientConstants.REMOVE,ClientConstants.COMMENT_READ
					,ClientConstants.COMMENT_WRITE,ClientConstants.OFFLINE};
		}
		public int getRsourceTypeOrdinal() {
//			SecurityValues.RESOURCE_TYPES.PAGE
			return 2;
		}
		
	}
}
