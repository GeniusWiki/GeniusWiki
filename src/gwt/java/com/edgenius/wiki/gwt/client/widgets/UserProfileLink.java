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

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.user.UserPopup;
import com.google.gwt.user.client.ui.EventfulHyperLink;

/**
 * @author Dapeng.Ni
 */
public class UserProfileLink extends EventfulHyperLink{
	/**
	 * 
	 * @param fullname 
	 * @param spaceUname optional, if no, then display recently updated pages for all spaces. 
	 * @param username 
	 * @param portrait optional, if no, then portrait loaded only when popup display.
	 */
	public UserProfileLink(String fullname, String spaceUname, String username, String portrait){
		super(fullname,GwtUtils.buildToken(GwtUtils.getCPageToken(SharedConstants.CPAGE_USER_PROFILE), username));
		this.setTitle(Msg.consts.goto_profile());
		new UserPopup(this, spaceUname,username,portrait);
	}
}
