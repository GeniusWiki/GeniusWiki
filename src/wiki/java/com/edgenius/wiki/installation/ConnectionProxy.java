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
package com.edgenius.wiki.installation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Server;

/**
 * @author Dapeng.Ni
 */
public class ConnectionProxy {
	private static final Logger log = LoggerFactory.getLogger(ConnectionProxy.class);
	private Connection conn;
	private String schema;
	private String type;

	public ConnectionProxy(String type, Connection conn, String schema){
		this.type = type;
		this.conn = conn;
		this.schema = schema;
	}
	
	public Statement createStatement() throws SQLException{
		Statement stmt = conn.createStatement();
		
		if(!StringUtils.isBlank(schema)){
			if(Server.DBTYPE_DB2.equalsIgnoreCase(type)){
				stmt.execute("set current schema = " + schema);
			}else if(Server.DBTYPE_ORACLE9I.equalsIgnoreCase(type)){
				stmt.execute("alter session set current_schema=" + schema);
			}else if(Server.DBTYPE_POSTGRESQL.equalsIgnoreCase(type)){
				stmt.execute("SET search_path TO " + schema);
			}else{
				//TODO: how about MYSQL? does it have schema?
			}
		}
		return stmt;
	}

	/**
	 * 
	 */
	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			log.error("Close DB connection failed",e);
		}
	}

	/**
	 * @param sql
	 * @return
	 * @throws SQLException 
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		//this is only for change schema
		if(!StringUtils.isBlank(schema)){
			Statement stat = createStatement();
			stat.close();
		}		
		PreparedStatement prepStat = conn.prepareStatement(sql);

		
		return prepStat;
		
	}
}
