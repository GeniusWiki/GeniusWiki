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
package com.edgenius.core.dao.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.edgenius.core.Constants;
import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.SecurityValues.RESOURCE_TYPES;
import com.edgenius.core.dao.PermissionDAO;
import com.edgenius.core.dao.UserDAO;
import com.edgenius.core.model.Permission;
import com.edgenius.core.model.Role;
import com.edgenius.core.model.User;
import com.edgenius.core.util.AuditLogger;

/**
 * This class interacts with Spring's HibernateTemplate to save/delete and
 * retrieve User objects.
 *
*/
@Repository("userDAO")
public class UserDAOHibernate extends BaseDAOHibernate<User> implements UserDAO, UserDetailsService {
	
	private static final String SQL_GET_BY_USERNAME = "from " + User.class.getName() + " as u where u.username=?";
	private static final String SQL_GET_USERS = "from User u ";
	private static final String SQL_GET_BY_EMAIL =  "from " + User.class.getName() + " as u where u.contact.email=?";
	
	//also include disabled user???
	private static final String GET_USERS_COUNT =  "select count(*) from " + User.class.getName() + " as u ";
	
	//not good practice here - inject bean in DAO class
	private PermissionDAO permissionDAO;
	//JDK1.6 @Override
	public int getUserTotalCount(final String filter) {
		
		return (Integer) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String filterWith="";
				if(!StringUtils.isBlank(filter)){
					filterWith =" where u.username like :filter or u.fullname like :filter or u.contact.email like :filter ";
				}
				Query query = session.createQuery(GET_USERS_COUNT + filterWith);
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
	//JDK1.6 @Override
	public List<User> getUsers(final int start, final int returnSize, String sortBy, final String filter, boolean sortByDesc) {
		String orderBy="";
		String filterWith="";
		
		if(!StringUtils.isBlank(filter)){
			filterWith = " where u.username like :filter or u.fullname like :filter or u.contact.email like :filter ";
		}
		if(!StringUtils.isBlank(sortBy)){
			StringBuffer orderSb = new StringBuffer(" order by ");
			String[] sortStr = sortBy.split("\\|");
			String seq= (sortByDesc?" desc":" asc");
			for (String str : sortStr) {
				int sort = NumberUtils.toInt(str, -1);
				if(sort == -1) 
					continue;
				if(sort == User.SORT_BY_CREATED_DATE)
					orderSb.append("u.createdDate ").append(seq).append(",");
				else if(sort == User.SORT_BY_EMAIL)
					orderSb.append("u.contact.email ").append(seq).append(",");
				else if(sort == User.SORT_BY_USERNAME)
					orderSb.append("u.username ").append(seq).append(",");
				else if(sort == User.SORT_BY_FULL_NAME)
					orderSb.append("u.fullname ").append(seq).append(",");
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
			orderBy = " order by u.createdDate desc";
		}
		
		final String sql = SQL_GET_USERS +filterWith+orderBy;
		return (List<User>) getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.createQuery(sql);
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


    /**
     * @see com.edgenius.paradise.dao.UserDAO#getUserByName(java.lang.String)
     */
    @SuppressWarnings("unchecked")
	public User getUserByName(String username) {
        List<User> list = (List<User>) getHibernateTemplate().find(SQL_GET_BY_USERNAME,username);
        User user = null;
        if(list != null && list.size() > 0){
        	user = list.get(0);
        	//put here as SpringSecuirty UserDetailsService will get user directly from here rather than UserService..
        	refreshInstancePermissionCache(user);
        }else{
            log.warn("user '" + username + "' not found");
            throw new ObjectRetrievalFailureException(User.class, username);
        }

        return user;
    }

	@SuppressWarnings("unchecked")
	public User getUserByEmail(String email) {
		List<User> list = (List<User>) getHibernateTemplate().find(SQL_GET_BY_EMAIL,email);
		if(list != null && list.size() > 0){
			return  list.get(0);
		}
		
		return null;
	}


    /**
     * @see com.edgenius.paradise.dao.UserDAO#saveUser(com.edgenius.paradise.model.User)
     */
    public void saveUser(final User user) {
        
        getHibernateTemplate().saveOrUpdate(user);
        getHibernateTemplate().flush();
    }

    /**
     * @see com.edgenius.paradise.dao.UserDAO#removeUser(java.lang.String)
     */
    public void removeUser(String username) {
        getHibernateTemplate().delete(getUserByName(username));
    }

    /** 
    * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
    */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
        	//use cloned object so that some lazy loading object can be initialized: put to userCache.
            return (UserDetails) getUserByName(username).clone();
        } catch (ObjectRetrievalFailureException e) {
            throw new UsernameNotFoundException("user '" + username + "' not found");
        }
    }
    /**
     * NOTE: The reason that put this method here rather than UserService is, <code>loadUserByUsername()</code>
     * will called by Acegi API, and that method also need initial user Wiki permission. And, UserDAO CANNOT refer
     * to  UserService object. 
     * 
     * User object will hold instance permission (OPERATIONS), this method will reset this value;
     */
	public void refreshInstancePermissionCache(User user){
		if(user == null) 
			return;
		
		List<OPERATIONS> wikiPermissions = new ArrayList<OPERATIONS>();

		//get this user's personal permission
		Set<Permission> perms = user.getPermissions();
		if(perms == null){
			AuditLogger.warn("The user permission is null:" + user);
		}else{
			getInstancePermission(wikiPermissions, perms);
		}
		//get this user's all user permission
		Set<Role> roles = user.getRoles();
		if(roles != null){ //only backup service, roles can be null.
			for (Role role : roles) {
				//performance consideration: don't use role.getPermission() as admin or user has too many permissions in large space volume case.
				perms = new HashSet<Permission>(permissionDAO.getByRoleResource(role.getName(),Constants.INSTANCE_NAME));
	
				getInstancePermission(wikiPermissions, perms);
			}
		}
		user.setWikiPermissions(wikiPermissions);
		
	}	
	/**
	 * Utility method to set INSTANCE permission into <code>wikiPermissions</code>.
	 * @param wikiPermissions
	 * @param perms
	 */
	public static void getInstancePermission(List<OPERATIONS> wikiPermissions, Set<Permission> perms) {
		if(perms == null){
			AuditLogger.warn("The role/user instance permission is null");
			return;
		}
		for (Permission permission : perms) {
			if(RESOURCE_TYPES.INSTANCE.equals(permission.getResource().getType())){
				//don't contain duplicated operations
				if(!wikiPermissions.contains(permission.getOperation()))
					wikiPermissions.add(permission.getOperation());
			}
		}
	}

	public void setPermissionDAO(PermissionDAO permissionDAO) {
		this.permissionDAO = permissionDAO;
	}


}
