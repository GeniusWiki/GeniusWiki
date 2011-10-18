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
package com.edgenius.wiki.dao.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.DraftDAO;
import com.edgenius.wiki.model.Draft;

/**
 * @author Dapeng.Ni
 */
@Repository("draftDAO")
public class DraftDAOHibernate extends BaseDAOHibernate<Draft> implements DraftDAO {
	
	private static final String GET_DRAFT_BY_UUID = "from " + Draft.class.getName() 
	+ " as p where p.space.unixName=? and p.pageUuid=? and p.creator=? and p.type=? ";
	
	private static final String HAS_DRAFT_BY_TITLE = "select p.uid,p.modifiedDate,p.type from " + Draft.class.getName() 
	+ " as p where p.space.unixName=? and p.title=? and p.creator=? and p.type > 0  order by p.modifiedDate desc";
	
	//get a space or all spaces by type (auto or manual)
	private static final String GET_DRAFTS = "from " + Draft.class.getName() 
		+ " as p where p.creator.username=? and p.type=? order by p.modifiedDate desc";
	private static final String GET_SPACE_DRAFTS = "from " + Draft.class.getName() 
	+ " as p where p.space.unixName=? and p.creator.username=? and p.type=? order by p.modifiedDate desc";
	
	//get a space or all spaces whatever type
	private static final String GET_ALL_DRAFTS = "from " + Draft.class.getName() 
		+ " as p where p.creator.username=? order by p.type asc, p.modifiedDate desc";
	private static final String GET_SPACE_ALL_DRAFTS = "from " + Draft.class.getName() 
	+ " as p where p.space.unixName=? and p.creator.username=? order by p.type asc, p.modifiedDate desc";
	

	protected static final String REMOVE_SPACE_DRAFTS = "delete " + Draft.class.getName() + " as p where p.space.uid=?";
	
	@SuppressWarnings("unchecked")
	public List<Draft> hasDraftByTitle(String spaceUname,String title,User owner){
		if(owner == null || owner.isAnonymous()){
			//for anonymous, no draft
			//in postgresql, if owner is null, SQL will throw exception.
			return null;
		}
		List<Object[]> list = getHibernateTemplate().find(HAS_DRAFT_BY_TITLE,new Object[]{spaceUname, title,owner});
		if(list == null || list.isEmpty())
			return null;
		
		//only get necessary fields
		List<Draft> ret = new ArrayList<Draft>();
		int idx =0;
		for (Object[] rs: list) {
			Draft draft = new Draft();
			draft.setUid((Integer) rs[0]);
			draft.setModifiedDate((Date) rs[1]);
			draft.setType(((Integer) rs[2]));
			ret.add(draft);
			if(idx > 1){
				log.error("Found over 2 drafts (auto and manual) for page " + title + " on space " + spaceUname);
				break;
			}
			idx++;
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public List<Draft> getDrafts(String spaceUname, String username, int type) {
		if(type == 0){
			if(StringUtils.isBlank(spaceUname))
				return getHibernateTemplate().find(GET_ALL_DRAFTS,new Object[]{username});
			else
				return getHibernateTemplate().find(GET_SPACE_ALL_DRAFTS,new Object[]{spaceUname, username});
		}else{
			if(StringUtils.isBlank(spaceUname))
				return getHibernateTemplate().find(GET_DRAFTS,new Object[]{username,type});
			else
				return getHibernateTemplate().find(GET_SPACE_DRAFTS,new Object[]{spaceUname, username,type});
		}
	}
	
	@SuppressWarnings("unchecked")
	public Draft removeDraftByUuid(String spaceUname,  String uuid, User owner, int type) {
		if(owner == null || owner.isAnonymous()){
			//for anonymous, no draft
			//in postgresql, if owner is null, SQL will throw exception.
			return null;
		}
		List<Draft> list = getHibernateTemplate().find(GET_DRAFT_BY_UUID,new Object[]{spaceUname,uuid,owner,type});
		Draft draft = null;
		if(list != null && list.size() > 0){
			draft  = (Draft)list.get(0);
			log.info("Remove "+ list.size() +  " drafts for page " + uuid + " on space " + spaceUname);
			getHibernateTemplate().deleteAll(list);
		}
		return draft;
	}

	@SuppressWarnings("unchecked")
	public Draft getDraftByUuid(String spaceUname, String uuid, User owner, int type) {
		if(owner == null || owner.isAnonymous()){
			//for anonymous, no draft
			//in postgresql, if owner is null, SQL will throw exception.
			return null;
		}
		
		List<Draft> list = getHibernateTemplate().find(GET_DRAFT_BY_UUID,new Object[]{spaceUname, uuid,owner,type});
		if(list == null || list.isEmpty())
			return null;
		if(list.size() > 1)
			log.error("Unexception draft, over one draft with same pageUuid. Title is " + uuid + " Owner is " + owner);
		return (Draft) list.get(0);
	}

	public void removeSpaceDrafts(Integer spaceUid) {
		//WARNING: Hibernate can not construct join properly in buldUpdate()!!! 
		//if using spaceUname, SQL should be a join-sql,unfortunately, it cause exception.
		//use spaceUid, it could be a simple query only for draft table.
		getHibernateTemplate().bulkUpdate(REMOVE_SPACE_DRAFTS,spaceUid);
	}
	
}
