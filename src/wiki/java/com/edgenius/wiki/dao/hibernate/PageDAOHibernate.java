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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.User;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.UserPageMark;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("unchecked")
public class PageDAOHibernate extends BaseDAOHibernate<Page> implements PageDAO {

	private static final String GET_CURRENT_BY_UUID = "from " + Page.class.getName() + " as p where p.pageUuid =? and p.removed=0";
	private static final String GET_CURRENT_BY_UXNAME = "from " + Page.class.getName() 
		+ " as p where p.space.unixName=? and p.unixName =? and p.removed=0";
	
	private static final String GET_ALL_BY_UUID  = "from " + Page.class.getName() + " as p where p.pageUuid=?";
	
	private static final String GET_CURRENT_BY_TITLE = "from " + Page.class.getName() 
			+ " as p where p.space.unixName=? and p.title=? and p.removed=0 ";
	
	private static final String GET_ALL_TITLE_BY_UUID  = "select p.uid, p.title from " + Page.class.getName() 
					+ " as p where p.pageUuid=? and p.removed=0 ";
	
	
	private static final String GET_TREE = "select p.uid, p.pageUuid, p.title, p.parent.uid, p.level from " + Page.class.getName() 
						+ " as p where p.space.unixName=?  and p.removed=0";
	
	private static final String GET_CHILDREN_BY_PARENTUID = "from " + Page.class.getName() 
					+ " as p where p.parent.uid=? and p.removed=0 ";
	
	private static final String GET_CHILDREN_COUNT_BY_PARENT_UID = "select count(*) from " + Page.class.getName() 
					+ " as p where p.parent.uid=? and p.removed=0 ";

	private static final String GET_REMOVED_IN_SPACE = "from " + Page.class.getName() 
			+ " as p where p.space.unixName=? and p.removed>0 order by p.modifiedDate desc";
	
	protected static final String GET_INSTANCE_CURRENT_PAGES = "from " + Page.class.getName() 
	+ " as p where p.removed=0 ";
	
	protected static final String GET_SPACE_CURRNET_PAGES =  "from " + Page.class.getName() 
	+ " as p where p.space.unixName=:spaceUname and p.removed=0 ";
	
	protected static final String GET_SPACE_ALL_PAGES =  "from " + Page.class.getName() 
	+ " as p where p.space.unixName=:spaceUname";
	
	private static final String GET_USER_PAGES = "from " + Page.class.getName()
	+ " as p where (p.creator=? or p.modifier=?) and p.space.unixName=?" 
	+ " and p.removed=0 order by p.modifiedDate desc";
	
	private static final String GET_ANONYMOUS_PAGES = "from " + Page.class.getName()
		+ " as p where (p.creator is null or p.modifier is null) and p.space.unixName=?" 
		+ " and p.removed=0 order by p.modifiedDate desc";
	
	protected static final String GET_USER_CONTRIBUTED_PAGES = "from " + Page.class.getName()
			+ " as p where (p.creator=:user or p.modifier=:user) " 
			+ " and p.removed=0 order by p.createdDate";
	
	protected static final String GET_ANONYMOUS_CONTRIBUTED_PAGES = "from " + Page.class.getName()
	+ " as p where (p.creator is null or p.modifier is null) " 
	+ " and p.removed=0 order by p.createdDate";
	
	private static final String GET_PAGES = "from " + Page.class.getName()
					+ " as p where p.space.unixName=? and p.removed=0";
	
	private static final String GET_PAGES_AFTER_TOUCHED =  "from " + Page.class.getName()
	+ " as p where p.space.unixName=? and p.touchedDate > ? and p.removed=0";
	
	private static final String GET_PAGES_UUID = "select p.pageUuid from " + Page.class.getName()
					+ " as p where p.space.unixName=? and p.removed=0";

	private final static String GET_INSTANCE_PAGES_COUNT = "select count(*) from " + Page.class.getName() 
	+ " as p where p.removed =0";
	
	private static final String GET_COUNT_BY_AUTHORED =  "select count(*) from " + Page.class.getName() 
	+ " as p where p.creator.username=?";
	
	private static final String GET_COUNT_BY_MODIFIED =  "select count(*) from " + Page.class.getName() 
	+ " as p where p.modifier.username=?";
	

	private static final String GET_PAGE_FOR_INDEXING  = "select p.pageUuid, p.title, p.modifiedDate, p.space.uid, c.content from " + Page.class.getName() 
	+ " as p, " + PageContent.class.getName() + " as c where p.removed=0 and p.content.uid = c.uid order by p.uid";

	private static final String GET_PAGES_BY_PROGRESS_EXT_LINK_ID =  "from " + Page.class.getName()
		+ " as p where p.space.unixName=? and p.pageProgress.linkExtID=? and p.removed=0";
	
	//can not use distinct for Page,as Postgresql reports error: for SELECT DISTINCT, ORDER BY expressions must appear in select list
	private static final String GET_PIN_TOP_PAGES_BY_SPACEUID =  "select p from " + Page.class.getName()
				+ " as p, " + UserPageMark.class.getName() 
				+ " as u where p.space.uid=? and u.type=" + SharedConstants.USER_PAGE_TYPE_PINTOP 
				+ " and p.removed=0 and u.page.uid=p.uid order by u.createdDate asc";
	
	//********************************************************************
	//               Method
	//********************************************************************
	public Page getCurrentByUuid(String uuid) {
		List<Page> list = getHibernateTemplate().find(GET_CURRENT_BY_UUID,uuid);
		if(list == null || list.isEmpty())
			return null;
		if(list.size() > 1)
			log.error("Unexception page, over one page with same pageUuid mark as current. Uuid is " + uuid);
		return list.get(0);
	}

	public Page getCurrentByUnixName(String spaceUname, String unixName) {
		List<Page> list = getHibernateTemplate().find(GET_CURRENT_BY_UXNAME,new String[]{spaceUname, unixName});
		if(list == null || list.isEmpty())
			return null;
		if(list.size() > 1)
			log.error("Unexception page, over one page with same unixname mark as current. unixName is " + unixName);
		return list.get(0);
	}

	public Page getByUuid(String pageUuid) {
		List<Page> pages =  getHibernateTemplate().find(GET_ALL_BY_UUID,pageUuid);
		if(pages == null || pages.size() == 0)
			return null;
		
		return pages.get(0);
	}

	public Map<Integer,String> getTitlesByUuid(String uuid) {
		Map<Integer,String> map = new HashMap<Integer, String>();
		List<Object[]> rs = getHibernateTemplate().find(GET_ALL_TITLE_BY_UUID,uuid);
		if(rs == null){
			return map;
		}
		for (Object[] objects : rs) {
			Integer uid = (Integer) objects[0];
			String title = (String) objects[1];
			map.put(uid, title);
		}
		return map;
	}
	public Page getCurrentPageByTitle(String spaceUname, String pageUname) {
		List list = getHibernateTemplate().find(GET_CURRENT_BY_TITLE,new Object[]{spaceUname,pageUname});
		if(list == null || list.isEmpty())
			return null;
		if(list.size() > 1)
			log.error("Unexception page, over one page with same space and page unixname mark as current. " +
					"Space uname is " + spaceUname + ", page uname is " + pageUname);
		return (Page) list.get(0);
	}

	@SuppressWarnings("unchecked")
	public List<Page> getTree(String spaceUname) {
		//if modify select fields, must change PageServiceImpl.refreshCache() method fields simultaneously
		List<Object[]> list = getHibernateTemplate().find(GET_TREE,spaceUname);
		List<Page> pageList = new ArrayList<Page>();
		if(list == null){
			return pageList;
		}
		
		log.info("Get Tree: " + list.size());
		for (Object[] objects : list) {
			Page page = new Page();
			int idx=0;
			//uid,title,parent,level
			page.setUid((Integer) objects[idx++]);
			page.setPageUuid((String) objects[idx++]);
			page.setTitle((String) objects[idx++]);
			//have to get parentUid rather than directly to get parent.pageUuid, as this causes records whose parent is null does not return
			Integer parentUid = (Integer) objects[idx++];
			Page parent = null;
			if(parentUid != null){
				parent = new Page();
				parent.setUid(parentUid);
			}
			page.setParent(parent);
			page.setLevel((Integer)objects[idx++]);
			
			pageList.add(page);
		}
		return pageList;
	}

	
	public List<Page> getChildren(Integer parentUid) {
		return getHibernateTemplate().find(GET_CHILDREN_BY_PARENTUID,parentUid);
	}
	public Integer getChildrenCount(Integer parentUid) {
		@SuppressWarnings("rawtypes")
		List list = getHibernateTemplate().find(GET_CHILDREN_COUNT_BY_PARENT_UID,parentUid);
		if(list != null && list.size() > 0){
			return (int) ((Long)list.get(0)).longValue();
		}
		return 0;
	}

	public List<Page> getRemovedPagesInSpace(String spaceUname) {
		return getHibernateTemplate().find(GET_REMOVED_IN_SPACE,spaceUname);
	}

	//JDK1.6 @Override
	public List<Page> getRecentPages(final String spaceUname, final int start, final int count, final boolean sortByModify) {
		if(StringUtils.isBlank(spaceUname)){
			return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					String sql;
					if(!sortByModify){
						sql = GET_INSTANCE_CURRENT_PAGES + "order by p.createdDate  desc";
					}else{
						sql = GET_INSTANCE_CURRENT_PAGES + "order by p.modifiedDate desc";
					}
					Query query = session.createQuery(sql);
					if(start > 0)
						query.setFirstResult(start);
					return query.setMaxResults(count).list(); 
				}
			});
		}else{
			return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					String sql;
					if(!sortByModify){
						sql = GET_SPACE_CURRNET_PAGES + "order by p.createdDate  desc";
					}else{
						sql = GET_SPACE_CURRNET_PAGES + "order by p.modifiedDate desc";
					}
					Query query = session.createQuery(sql);
					query.setString("spaceUname", spaceUname);
					if(start > 0)
						query.setFirstResult(start);
					return query.setMaxResults(count).list(); 
				}
			});
		}
	}


	public List<Page> getUserUpdatedPagesInSpace(final String spaceUname, final User user, final int returnNum) {
		
		return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				if(user == null || user.isAnonymous()){
					query = session.createQuery(GET_ANONYMOUS_PAGES);
					query.setString(0, spaceUname);
				}else{
					query = session.createQuery(GET_USER_PAGES);
					query.setEntity(0, user);
					query.setEntity(1, user);
					query.setString(2, spaceUname);
				}
				if(returnNum > 0)
					query.setMaxResults(returnNum);
				
				return query.list();
			}
		});
	}
	public List<Page> getPagesInSpace(final String spaceUname,final Date touchedDate, final int returnNum) {
		return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				if(touchedDate == null){
					query = session.createQuery(GET_PAGES);
					query.setString(0, spaceUname);
				}else{
					query = session.createQuery(GET_PAGES_AFTER_TOUCHED);
					query.setString(0, spaceUname);
					//DON'T user query.setDate()!!! It only compare Date rather than time, it means the time part is truncated!
					//or use query.setTimestamp();
					query.setParameter(1, touchedDate);
				}
				if(returnNum > 0)
					query.setMaxResults(returnNum);
				
				return query.list();
			}
		});
	}
	public List<String> getPagesUuidInSpace(String spaceUname){
		return getHibernateTemplate().find(GET_PAGES_UUID,new Object[]{spaceUname});
	}
	public List<Page> getUserContributedPages(final User user,final int limit) {
		return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query;
				if(user == null || user.isAnonymous()){
					query = session.createQuery(GET_ANONYMOUS_CONTRIBUTED_PAGES);
				}else{
					query = session.createQuery(GET_USER_CONTRIBUTED_PAGES);
					query.setEntity("user", user);
				}
				if(limit > 0)
					query.setMaxResults(limit);
				return query.list();
			}
		});
	}


	public List<Page> getSpaceAllPages(String spaceUname) {
		return getHibernateTemplate().findByNamedParam(GET_SPACE_ALL_PAGES,"spaceUname",spaceUname);
	}

	//JDK1.6 @Override
	public long getSystemPageCount() {
		List list = getHibernateTemplate().find(GET_INSTANCE_PAGES_COUNT);
		
		if(list != null && list.size() > 0){
			return (Long)list.get(0);
		}
		return 0;

	}

	//JDK1.6 @Override
	public long getUserAuthoredSize(String username) {
		List list = getHibernateTemplate().find(GET_COUNT_BY_AUTHORED,username);
		if(list != null && list.size() > 0){
			return (Long)list.get(0);
		}
		return 0;
	}

	//JDK1.6 @Override
	public long getUserModifiedSize(String username) {
		List list = getHibernateTemplate().find(GET_COUNT_BY_MODIFIED,username);
		if(list != null && list.size() > 0){
			return (Long)list.get(0);
		}
		return 0;
	}

	public List<Page> getPageForIndexing(final int start, final int returnNum) {
		return (List<Page>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery(GET_PAGE_FOR_INDEXING);
				if(start > 0)
					query.setFirstResult(start);
				List<Object[]> list =  query.setMaxResults(returnNum).list(); 
				if(list == null){
					return null;
				}

				List<Page> pageList = new ArrayList<Page>(list.size());
				for (Object[] objects : list) {
					Page page = new Page();
					int idx=0;
					//pageUuid, title, spaceUid, and content
					page.setPageUuid((String) objects[idx++]);
					page.setTitle((String) objects[idx++]);
					page.setModifiedDate((Date) objects[idx++]);
					
					Space space = new Space();
					space.setUid((Integer)objects[idx++]);
					
					PageContent content = new PageContent();
					content.setContent((String) objects[idx++]);
					
					page.setSpace(space);
					page.setContent(content);
					
					pageList.add(page);
				}
				return pageList;
			}
		});

	}

	public Page getPageByProgressExtLinkID(String spaceUname, String extLinkID) {
		List<Page> list = getHibernateTemplate().find(GET_PAGES_BY_PROGRESS_EXT_LINK_ID,new String[]{spaceUname, extLinkID});
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Page> getPinTopPagesInSpace(Integer spaceUid) {
		return getHibernateTemplate().find(GET_PIN_TOP_PAGES_BY_SPACEUID,spaceUid);
	}


}
