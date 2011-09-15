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

import java.util.ArrayList;
import java.util.List;

import com.edgenius.wiki.integration.rest.model.PageBean;
import com.edgenius.wiki.integration.rest.model.TagBean;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageTag;

/**
 * @author Dapeng.Ni
 */
public class PageMapper {

	/**
	 * Please note, this method doesn't fill in Content and Comments.
	 * @param page
	 * @return
	 */
	public static PageBean pageToBean(Page page) {
		PageBean bean = new PageBean();

		//only put spaceUname to space 
		bean.setSpaceUname(page.getSpace().getUnixName());
		
//		bean.setContent(page.getContent().getContent());
		List<PageTag> tags = page.getTags();
		if(tags != null){
			List<TagBean> tagBeans = new ArrayList<TagBean>();
			for (PageTag tag : tags) {
				TagBean tagBean = new TagBean();
				tagBean.setName(tag.getName());
			}
			bean.setTags(tagBeans);
		}
		bean.setTitle(page.getTitle());
		bean.setUuid(page.getPageUuid());
		
		AbstractMapper.mapper(bean, page);
		return bean;
	}

}
