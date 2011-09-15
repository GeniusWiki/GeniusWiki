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
package com.edgenius.wiki.dao;

import java.util.Date;
import java.util.List;

import com.edgenius.core.dao.DAO;
import com.edgenius.core.model.User;
import com.edgenius.wiki.model.History;

/**
 * @author Dapeng.Ni
 */
public interface HistoryDAO extends DAO<History>{

	/**
	 * @param uuid
	 * @param touchedDate 
	 * @param returnCount 
	 * @param startVer return histories less than this startVer  
	 * 
	 * @return
	 */
	List<History> getByUuid(String uuid,int startVer, int returnCount, Date touchedDate);

	/**
	 * @param version
	 * @param pageUuid
	 * @return
	 */
	History getVersionByUuid(int version, String pageUuid);

	/**
	 * @param fromPageUuid
	 * @param touchedDate 
	 * @return
	 */
	List<History> getByUuid(String fromPageUuid);

	/**
	 * @param user
	 * @return
	 */
	List<History> getUserContributedHistories(User user);
}
