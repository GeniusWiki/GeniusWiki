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

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Dapeng.Ni
 */
public class JDBCTemplateDAO  {
	private static final String NATIVE_DELETE_REL1 = "delete from  "+TABLE_PREFIX+"USER_ROLE";
	private static final String NATIVE_DELETE_REL2 = "delete from  "+TABLE_PREFIX+"USER_PERMISSIONS";
	private static final String NATIVE_DELETE_REL3 = "delete from  "+TABLE_PREFIX+"ROLE_PERMISSIONS";
	private static final String NATIVE_DELETE_REL4 = "delete from  "+TABLE_PREFIX+"SPACES_TAGS";
	private static final String NATIVE_DELETE_REL5 = "delete from  "+TABLE_PREFIX+"PAGES_TAGS";
	private static final String NATIVE_DELETE_REL11 = "delete from  "+TABLE_PREFIX+"USER_FOLLOW";
	
	private static final String NATIVE_DELETE_REL6 = "update "+TABLE_PREFIX+"SPACES set homepage_puid=null";
	private static final String NATIVE_DELETE_REL7 = "update "+TABLE_PREFIX+"PAGES set parent_page_puid=null";
	private static final String NATIVE_DELETE_REL8 = "update "+TABLE_PREFIX+"DRAFTS set parent_page_puid=null";
	private static final String NATIVE_DELETE_REL9 = "update "+TABLE_PREFIX+"HISTORIES set parent_page_puid=null";
	private static final String NATIVE_DELETE_REL10 = "update "+TABLE_PREFIX+"PAGE_COMMENTS set parent_puid=null";
	
	private static final String NATIVE_DELETE_CONTENT1 = "delete from  "+TABLE_PREFIX+"PAGES_CONTENT";
	private static final String NATIVE_DELETE_CONTENT2 = "delete from  "+TABLE_PREFIX+"DRAFTS_CONTENT";
	private static final String NATIVE_DELETE_CONTENT3 = "delete from  "+TABLE_PREFIX+"HISTORIES_CONTENT";
	private JdbcTemplate jdbcTemplate;
	
	public JDBCTemplateDAO(DataSource ds){
		jdbcTemplate = new JdbcTemplate(ds);
	}
	public void cleanTableRelations(){
		jdbcTemplate.execute(NATIVE_DELETE_REL1);
		jdbcTemplate.execute(NATIVE_DELETE_REL2);
		jdbcTemplate.execute(NATIVE_DELETE_REL3);
		jdbcTemplate.execute(NATIVE_DELETE_REL4);
		jdbcTemplate.execute(NATIVE_DELETE_REL5);
		jdbcTemplate.execute(NATIVE_DELETE_REL6);
		jdbcTemplate.execute(NATIVE_DELETE_REL7);
		jdbcTemplate.execute(NATIVE_DELETE_REL8);
		jdbcTemplate.execute(NATIVE_DELETE_REL9);
		jdbcTemplate.execute(NATIVE_DELETE_REL10);
		jdbcTemplate.execute(NATIVE_DELETE_REL11);
	}
	
	public void cleanNonDAOTables(){
		jdbcTemplate.execute(NATIVE_DELETE_CONTENT1);
		jdbcTemplate.execute(NATIVE_DELETE_CONTENT2);
		jdbcTemplate.execute(NATIVE_DELETE_CONTENT3);
		
	}
}
