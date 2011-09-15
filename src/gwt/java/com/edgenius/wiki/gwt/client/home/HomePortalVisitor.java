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
package com.edgenius.wiki.gwt.client.home;

import java.util.ArrayList;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.portal.Portal;
import com.edgenius.wiki.gwt.client.portal.PortalVisitor;
import com.edgenius.wiki.gwt.client.server.PortalControllerAsync;

/**
 * @author Dapeng.Ni
 */
public class HomePortalVisitor implements PortalVisitor{
	private Portal portal;
	
	public HomePortalVisitor(){}

	public void fetchPortlet(int column) {
		PortalControllerAsync portalController = ControllerFactory.getPortalController();
		portalController.getPortal(column, portal.getLoadAsyncCallback());
	}
	public void saveLayout(ArrayList<String> list) {
		PortalControllerAsync homeController = ControllerFactory.getPortalController();
		homeController.updatePortal(list,portal.getSaveAsyncCallback());		
	}

	public void setPortal(Portal portal) {
		this.portal = portal;
	}

}
