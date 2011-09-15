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
package com.edgenius.wiki.integration.rest.model.mapper;

import com.edgenius.wiki.integration.rest.model.CommentBean;
import com.edgenius.wiki.model.PageComment;

/**
 * @author Dapeng.Ni
 */
public class CommentMapper {

	/**
	 * Doesn't fill in CommentBean.page field;
	 * @param pageComment
	 * @return
	 */
	public static CommentBean commentToBean(PageComment comment) {
		CommentBean bean = new CommentBean();
		bean.setUuid(String.valueOf(comment.getUid()));
		bean.setLevel(comment.getLevel());
		bean.setContent(comment.getBody());
		bean.setParentUuid(comment.getParent() != null?String.valueOf(comment.getParent().getUid()):null);
		AbstractMapper.mapper(bean, comment);
		
		return bean;
	}

	
}
