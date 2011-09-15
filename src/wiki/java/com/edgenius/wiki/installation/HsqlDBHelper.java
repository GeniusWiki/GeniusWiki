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

import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Server;


/**
 * @author Dapeng.Ni
 */
public class HsqlDBHelper {
	private static Logger log = LoggerFactory.getLogger(HsqlDBHelper.class);
	public static void main(String[] args)  {
		try{
			
			//shutdown
			log.info("HSQLDB port {}", args[0]);
			
			String port = args[0] == null?":9001":":"+args[0].trim();
			DBLoader loader = new DBLoader();
			String url = "jdbc:hsqldb:hsql://localhost" + port + "/geniuswiki";
			log.info("Shutdown HSQLDB {}", url);
			ConnectionProxy conn = loader.getConnection(Server.DBTYPE_HSQLDB, url, null, "sa", null);
			Statement stat = conn.createStatement();
			stat.executeUpdate("SHUTDOWN");
	//		conn.close();
			
			log.info("HSQLDB shutted down");
		}catch(Exception e){
			log.error("Shutdown HSQLDB with errors",e);
		}
	}
}
