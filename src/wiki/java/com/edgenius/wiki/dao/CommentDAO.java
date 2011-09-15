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

import java.util.List;

import com.edgenius.core.dao.DAO;
import com.edgenius.wiki.model.PageComment;

public interface CommentDAO  extends DAO<PageComment>{


	List<PageComment> getCommentsByPageUid(Integer pageUid);

	int getCommentCountByPageUid(Integer pageUid);

	/**
	 * How many comments this use posted
	 * @param username
	 * @return
	 */
	int getUserCommentSize(String username);

	/**
	 * Clean all comment notify flags
	 */
	void cleanNotifyFlag();

	/**
	 * @return Page Uid
	 */
	List<Integer> getNeedNotifyCommentPageUids();

	/**
	 * @return
	 */
	int getNotifySentCount(Integer pageUid);


}
