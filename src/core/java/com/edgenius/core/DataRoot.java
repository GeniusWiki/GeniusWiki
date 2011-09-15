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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.FileUtil;

/**
 * Try to load geniuswiki.properties from classpath and decide the geniuswiki data root. Before load the properties file,
 * it will try to get "geniuswiki.data.root" value from System.properties, it has this value then it uses this value and stop
 * load properties. 
 * 
 * @author Dapeng.Ni
 */
public class DataRoot {
	private static final Logger log = LoggerFactory.getLogger(DataRoot.class);
	public static String rootResource = "classpath:geniuswiki/geniuswiki.properties";
	public static final String rootKey = "geniuswiki.data.root";
	
	public static String getDataRoot(){
		String root =null;
		root = System.getProperty(rootKey);
		if(root != null && root.trim().length() > 0){
			log.info("System property includes {}, using this value instead of geniuswiki.properties",rootKey);
			root = formatRoot(root);
			return root;
		}
			
		//default value
		root = "file:///var/data/geniuswiki/";
		try {
			Properties prop = FileUtil.loadProperties(rootResource);
			root = prop.getProperty(rootKey);
			log.info("System data root from geniuswiki.properties is {}",root);
			//we don't do format for each get request
			//root = formatRoot(root);
		} catch (Exception e) {
			log.error("Load system root failed, please assign it in geniuswiki.properites.",e);
			throw new SystemInitException("Load system root failed, please assign it in geniuswiki.properites.",e);
		}
		return root;
	}


	/**
	 * @param root
	 * @return 
	 */
	public static String saveDataRoot(String root) {
		
		if(StringUtils.isBlank(root)){
			log.error("Unable to save blank root:" +root);
			return root;
		}
		
		String sysRoot = System.getProperty(rootKey);
		if(sysRoot != null && sysRoot.trim().length() > 0){
			log.warn("User set {} in System properties, the persisted system root won't be saved." +
					" User must ensure the consist while restarting the system", rootKey);
			return formatRoot(sysRoot);
		}
		
		root = formatRoot(root);
		FileOutputStream out = null;
		try {
			
			Properties prop = new Properties();
			prop.setProperty(rootKey,root);
			out = FileUtil.getFileOutputStream(rootResource);
			prop.store(out, "IMPORTANT: The directory must start with file:// and end with /. The path separator is /.");
			log.info("Classpath geniuswiki properties file is saved.");
		} catch (Exception e) {
			log.error("Save system root failed.",e);
		} finally{
			IOUtils.closeQuietly(out);
		}
		
		return root;
	}

	/**
	 * Root must start with "file://" and end by "/". It only has "\", all "\" will be replace to "/"
	 * @return
	 */
	public static String formatRoot(String root) {
		if(root.startsWith("file://")){
			root = root.substring(7);
		}
		try {
			root = new File(root).getCanonicalPath();
		} catch (IOException e) {
			log.error("Format root to getCanonicalPath() error",e);
		}
		
		if(root.indexOf("\\") != -1)
			root = root.replaceAll("\\\\+", "/");
		
		if(!root.startsWith("file://")){
			root = "file://"+root;
		}
		if(!root.endsWith("/")){
			root=root+"/";
		}
		return root;
	}
}
