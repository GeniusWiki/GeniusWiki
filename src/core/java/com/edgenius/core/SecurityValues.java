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
package com.edgenius.core;


/**
 * @author Dapeng.Ni
 * TODO: bind with wiki package!!!
 */
public class SecurityValues {
	//The real type for springframework.security(acegi) API: all above type will transform to these type before authentication/authorization
	public static enum RUNTIME_RESOURCE_TYPES{
		URL,
		METHOD;
	}

	//Possible operations in Permission table: 
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!! VERY IMPORTANT : DON'T CHANGE SEQUENCE of fields
	//position in its enum declaration of fields is very important, it is equalivant number in permission table(operation)
	
	//for instance 5 [0,1,3,8](read,write,admin, and offline)
	//for space 7 [0,1,2,3,4,5,6,7,8]  (read,write,remove, admin, comments-read, comments-write,page restrict and offline)
	//for page 5 [0,1,2,4,5,8]  (read,write,remove, comments-read, comments-write and offline )
	//for widget 2 [0,3] (read, admin)
	public static enum OPERATIONS{
		READ, // 0
		WRITE, // 1 create, modify
		REMOVE, // 2
		ADMIN, // 3
		COMMENT_READ, // 4
		COMMENT_WRITE, // 5
		EXPORT, // 6
		RESTRICT, // 7
		OFFLINE; // 8
	}
	
	//page permission size (Page READ,WRITE,REMOVE,COMMENT READ,COMMENT WRITE,OFFLINE)
	public static final int PAGE_WIKIOPER_SIZE = 6;
	// wiki system resource type, will convert RUNTIME_RESOURCE_TYPES thru
	// strategy factory.
	//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//!!!!!!!!!!!! VERY IMPORTANT : DON'T CHANGE SEQUENCE of fields: their ordinal saved in database
	public static enum RESOURCE_TYPES{
		INSTANCE,
		SPACE,
		PAGE,
		WIDGET,
		USER, COMMENT, ATTACHMENT;   //so far, USER,COMMENT,ATTACHMENT type doesn't have any valid resource <-> permissions mapping, but it used in ActivityLog
	}
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//               You need change 4 place if update role name 
	//* here, AbstractSecurityPanel, anonymousProcessingFilter in applicationContext-security.xml, and mysql-init-table.sql.
	//anonymous user name
	public static enum SYSTEM_ROLES{
		//ALL is not real role in system. It means all roles include ANONYMOUS will be allowed
		ALL("$ALL$"),
		ANONYMOUS("ROLE_SYS_ANONYMOUS"),
		ADMIN("ROLE_SYS_ADMIN"),
		USERS("ROLE_SYS_USERS");
		
		private SYSTEM_ROLES(String name){
			this.name = name;
		}
		private String name;
		public String getName(){
			return name;
		}
	}
	
}
