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
package com.edgenius.wiki.gwt.client.space;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.widgets.AbstractSecurityPanel;

public class SpaceSecurityPanel extends AbstractSecurityPanel{
	private SpacePermissionPanel parent;
	/**
	 * @param resourceName
	 */
	public SpaceSecurityPanel(String resourceName, SpacePermissionPanel deck) {
		super(resourceName);
		this.parent = deck;
	}
	
	public void showGroupUsers(String srcSpace, String tgtSpace) {
		parent.showGroupUsers(srcSpace, tgtSpace);
	}
	public int[] getValidOperations() {

		return new int[]{ClientConstants.READ,ClientConstants.WRITE,ClientConstants.REMOVE,ClientConstants.RESTRICT
				,ClientConstants.COMMENT_READ,ClientConstants.COMMENT_WRITE
				,ClientConstants.ADMIN};
	}

	public int buildTableHeader(int startCol) {
		int row = 0;
		int col = startCol;

		
		table.setText(row, col, Msg.consts.page());
		table.getFlexCellFormatter().setColSpan(row, col++, 4);
		table.setText(row, col, Msg.consts.comment());
		table.getFlexCellFormatter().setColSpan(row, col++, 2);
		table.setText(row, col, Msg.consts.space());
		table.getFlexCellFormatter().setColSpan(row, col++, 1);
		
		//another new row
		row++;
		col = startCol;
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.view());
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.create());
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.remove());
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.restrict());
		//comments
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.view());
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.create());
		//space
//		table.setText(row,col++,"export");
		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
		table.setText(row,col++,Msg.consts.admin());
		//remove some offline_code here(0726)
//		table.getFlexCellFormatter().setStyleName(row, col, Css.NOWRAP);
//		table.setText(row,col++,Msg.consts.offline());
		
		return ++row;
	}

	public boolean getRoleAdminReadonly(int operation) {
		//admin role always has all function for spaces
		return true;
	}

	public boolean getRoleAnonymousReadonly(int operation) {
		//anonymous does not allow admin/restrict space
		if(operation == ClientConstants.ADMIN
			|| operation == ClientConstants.RESTRICT)
			return true;
		return false;
	}
	public int getRsourceTypeOrdinal() {
//		SecurityValues.RESOURCE_TYPES.SPACE
		return 1;
	}
}
