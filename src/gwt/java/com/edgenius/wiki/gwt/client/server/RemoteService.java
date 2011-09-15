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

import com.edgenius.wiki.gwt.client.model.SerializableWhiteList;


/**
 * This interface just for involve some Class which will put into GWT SerializationPolicy.
 * These class won't appear in normal controller method, but it will be used in GWT RPC.encodeXXX() method.
 * Otherwise, the sample the exception will be thrown:<br>
 *  
 * com.google.gwt.user.client.rpc.SerializationException: 
 * Type 'com.edgenius.wiki.gwt.client.CaptchaVerifiedException' was not included in the 
 * set of types which can be serialised by this SerializationPolicy. For security purposes, 
 * this type will not be serialised.
 *  
 * @author Dapeng.Ni
 */
public interface RemoteService extends com.google.gwt.user.client.rpc.RemoteService{
	public SerializableWhiteList serialPolicy(SerializableWhiteList list);

}
