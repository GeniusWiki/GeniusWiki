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

import static com.edgenius.core.Constants.TABLE_PREFIX;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;

import com.edgenius.core.dao.hibernate.BaseDAOHibernate;
import com.edgenius.core.model.Resource;
import com.edgenius.wiki.dao.SpaceDAO;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;

/**
 * @author Dapeng.Ni
 */
@Repository("spaceDAO")
public class SpaceDAOHibernate extends BaseDAOHibernate<Space> implements SpaceDAO {

	private static String GET_BY_UNAME = "from " + Space.class.getName() + " as s where s.unixName=?";
	private static String GET_BY_TITLE = "from " + Space.class.getName() + " as s where s.name=?";
	
	//ORACLE: "table_name as alias" -- "as" is illegal, shit oracle.
	//NOTE: this assume draft/removed page won't need security policy!!! Need confirm later 
	private static String NATIVE_GET_PAGE_RESOURCE_BY_SPACE =  "select distinct * from   "+TABLE_PREFIX+"RESOURCES  r " +
			" left outer join  "+TABLE_PREFIX+"PAGES  p  on r.resource_name = p.page_uuid " +
			" left outer join  "+TABLE_PREFIX+"SPACES  s on p.space_puid=s.puid	where  s.unix_name=:spaceUname and s.s_type >=0 ";
	
	private final static String GET_SPACES = "from " + Space.class.getName() + " as s  ";
	
	private final static String GET_SPACES_COUNT = "select count(*) from " + Space.class.getName() +" as s "; 

	protected static final String GET_USER_AUTHORED_SPACES = "from " + Space.class.getName() + " as r " +
			" where r.removed = false and r.creator.username=:name and  r.type >=0 order by r.score desc, r.createdDate desc";
	
	protected static final String GET_SYSTEM_SPACE = "from " + Space.class.getName() + " as r where r.type =-1";
	
	private static final String GET_COUNT_BY_USER_AUTHORED= "select count(*) from " + Space.class.getName() 
					+ " as s where s.creator.username=?";
	
	private static final String GET_ALL_SPACE_UNAME = "select s.unixName from " + Space.class.getName() + " as s where s.type > 0";
	
	
	private static final String GET_ALL_SPACE_PAGE_COUNT= "select p.space.uid, count(*) from " + Page.class.getName() 
			+ " as p where p.removed=0 group by p.space.uid";
	
	@SuppressWarnings("unchecked")
	public Space getByUname(String spaceUname) {
		List<Space> list = getHibernateTemplate().find(GET_BY_UNAME,spaceUname);
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}
	@SuppressWarnings("unchecked")
//JDK1.6 @Override
	public Space getByTitle(String title) {
		List<Space> list = getHibernateTemplate().find(GET_BY_TITLE,title);
		if(list == null || list.size() == 0)
			return null;
		return list.get(0);
	}
	@SuppressWarnings("unchecked")
	public List<Resource> getSpacePageResources(final String spaceUname) {
		return  getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery query = session.createSQLQuery(NATIVE_GET_PAGE_RESOURCE_BY_SPACE).addEntity(Resource.class);
				query.setString("spaceUname", spaceUname);
				return query.list();
			}
			
		});
	}
	
	public int getSpaceCount(final String filter){
		
		return (Integer) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String filterWith="";
				if(!StringUtils.isBlank(filter)){
					filterWith = " where s.name like :filter or s.unixName like :filter or s.description like :filter ";
				}
				Query query = session.createQuery(GET_SPACES_COUNT + filterWith);
				if(!StringUtils.isBlank(filter)){
					query.setString("filter","%"+filter.trim()+"%");
				}
				List list = query.list();
				if(list != null && list.size() > 0){
					return (int) ((Long)list.get(0)).longValue();
				}
				return 0;
			}
		});
		
	}
	
	@SuppressWarnings("unchecked")
	public List<Space> getSpaces(final int start, final int returnSize, final String sortBy,final String filter, final boolean sortByDesc) {
		return (List<Space>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String orderBy="";
				String filterWith="";
				if(!StringUtils.isBlank(filter)){
					filterWith = "where s.name like :filter or s.unixName like :filter or s.description like :filter ";
				}
				if(!StringUtils.isBlank(sortBy)){
					StringBuffer orderSb = new StringBuffer(" order by ");
					String[] sortStr = sortBy.split("\\|");
					String seq= (sortByDesc?" desc":" asc");
					for (String str : sortStr) {
						int sort = NumberUtils.toInt(str, -1);
						if(sort == -1) 
							continue;
						if(sort == Space.SORT_BY_PAGE_SCORE)
							orderSb.append("s.score ").append(seq).append(",");
						else if(sort == Space.SORT_BY_CREATEON)
							orderSb.append("s.createdDate ").append(seq).append(",");
						else if(sort == Space.SORT_BY_CREATEBY)
							orderSb.append("s.creator ").append(seq).append(",");
						else if(sort == Space.SORT_BY_SPACE_TITLE)
							orderSb.append("s.name ").append(seq).append(",");
						else if(sort == Space.SORT_BY_SPACEKEY)
							orderSb.append("s.unixName ").append(seq).append(",");
					}
					if(orderSb.length() > 0){
						orderBy = orderSb.toString();
						if(orderBy.endsWith(",")){
							//remove last ","
							orderBy = orderBy.substring(0, orderBy.length()-1);
						}
					}
				}else{
					//default order
					orderBy = " order by s.score desc, s.createdDate desc";
				}
				
				Query query = session.createQuery(GET_SPACES + filterWith + orderBy);
				if(!StringUtils.isBlank(filter)){
					query.setString("filter","%"+filter.trim()+"%");
				}
				query.setFirstResult(start);
				if(returnSize > 0)
					query.setMaxResults(returnSize);
				
				return query.list();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public List<Space> getUserCreatedSpaces(final String username, final int limit) {
		return (List<Space>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery(GET_USER_AUTHORED_SPACES);
				query.setString("name", username);
				if(limit > 0)
					query.setMaxResults(limit);
				return query.list();
			}
		});	
	}

	//JDK1.6 @Override
	public int getUserAuthoredSize(String username) {
		List list = getHibernateTemplate().find(GET_COUNT_BY_USER_AUTHORED,username);
		if(list != null && list.size() > 0){
			return (int) ((Long)list.get(0)).longValue();
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public Space getSystemSpace() {
		List<Space> list = getHibernateTemplate().find(GET_SYSTEM_SPACE);
		if(list == null || list.size() == 0){
			log.error("Failed to find out system space, please initialize database");
			return null;
		}
		return list.get(0);
	}
	@SuppressWarnings("unchecked")
	//JDK1.6 @Override
	public List<String> getAllSpaceUnames() {
		return  getHibernateTemplate().find(GET_ALL_SPACE_UNAME);
	}
	
	@SuppressWarnings("unchecked")
	public Map<Integer, Long> getAllSpacePageCount(){
		List<Object[]> list = getHibernateTemplate().find(GET_ALL_SPACE_PAGE_COUNT);
		
		Map<Integer, Long> spaceCount = new HashMap<Integer, Long>();
		if(list == null){
			return spaceCount;
		}
		for (Object[] objects : list) {
			Integer uid = (Integer) objects[0];
			Long count = (Long) objects[1];
			spaceCount.put(uid, count);
		}
		
		return spaceCount;
	}

	public void cleanTable(){
		//instance space(puid=1) won't be remove during clean space table 
		//reason is this record is verify object in DBCP connection pool validationQuery property. 
		//If removed, DBCP will broken and DB connect can not get successfully.   
		getHibernateTemplate().bulkUpdate("delete from " + entityClass.getName()  + " as s where s.unixName!=?",SharedConstants.SYSTEM_SPACEUNAME);
	}
}
