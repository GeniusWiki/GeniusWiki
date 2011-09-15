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
package com.edgenius.wiki.gwt.client.model;

import java.util.ArrayList;

/**
 * @author Dapeng.Ni
 */
public class DiffListModel extends GeneralModel  {
	//used in history comparision: need not ask merge etc., and just one DiffModel element to save flat html text 
	public static final int FLAT_TYPE = 1;
	//used in saving version conflict merge: allow user uses popup menu to choose reject or agree etc. Could contain many DiffModel
	//which is list of unchanged text and changed text.
	public static final int MERGE_TYPE = 2;
	public ArrayList<DiffModel> revs;
	
	public int type;
	
	//comparing version1:version2, if current version, it is ==0. if ver == -1, it means unable get version number since some exception
	public int ver1 = -1;
	public int ver2 = -1;
	
}
