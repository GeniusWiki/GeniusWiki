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
package com.edgenius.wiki.gwt.client.instance;

import com.edgenius.wiki.gwt.client.ClientConstants;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.widgets.AbstractSecurityPanel;

/**
 * @author Dapeng.Ni
 */
public class InstanceSecurityPanel extends AbstractSecurityPanel {

	/**
	 * @param resourceName
	 */
	public InstanceSecurityPanel(String resourceName) {
		super(resourceName);
		
	}

	public int buildTableHeader(int startCol) {
		int row = 0;
		int col = startCol;
		table.setText(row,col++,Msg.consts.access());
		table.setText(row,col++,Msg.consts.create_space());
		table.setText(row,col++,Msg.consts.system_admin());
		table.setText(row,col++,Msg.consts.offline());
		
		return ++row;
	}
	
	public int[] getValidOperations() {
		//HARDCODE: ordinal of SecurityValues.OPERATIONS
//		for instance [0,1,3](read,write and admin)
		return new int[]{ClientConstants.READ,ClientConstants.WRITE,ClientConstants.ADMIN, ClientConstants.OFFLINE};
	}

	public boolean getRoleAdminReadonly(int operation) {
		//always return true: does not allow disable admin permission on instance
		return true;
	}

	public boolean getRoleAnonymousReadonly(int operation) {
//		HARDCODE: ordinal of SecurityValues.OPERATIONS
		//anonymous user only allow modify READ permission. because it can not create/admin spaces
		if(operation == ClientConstants.WRITE || operation == ClientConstants.ADMIN)
			return true;
		
		return false;
	}

	public int getRsourceTypeOrdinal() {
//		SecurityValues.RESOURCE_TYPES.INSTANCE
		return 0;
	}

}
