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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.util.FileUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Dapeng.Ni
 */
@XStreamAlias("Installation")
public class Installation {
	private static final Logger log = LoggerFactory.getLogger(Installation.class);
	public static final String FILE = "installation.xml";
	public static final String DEFAULT_FILE = "classpath:geniuswiki/installation.default.xml";
	
	private static final String installResource= "installation.xml";
	
	public static final String STATUS_WELCOME ="welcome";
	public static final String STATUS_COMPLETED ="completed";
	public static final String STATUS_DATAROOT = "dataroot";
	public static final String STATUS_LICENSE = "license";
	public static final String STATUS_MQ = "mq";
	public static final String STATUS_DB = "db";
	public static final String STATUS_TABLES = "tables";
	public static final String STATUS_UPGRADE = "upgarde";
	
	//instance type
	public static final String INSTANCE_STANDARD = "standard";
	public static final String INSTANCE_HOSTING = "hosting";
	
	public static boolean DONE = false;
	public static String INSTANCE_ID;
	public static String INSTANCE_TYPE;

	
	private String version;
	private String status = STATUS_WELCOME;
	private String[] steps;
	private String instanceID;
	private String instanceType;
	private String license;

	public static Installation refreshInstallation(){
		
		Installation install = new Installation();
		try {
			File installFile = FileUtil.getFile(DataRoot.getDataRoot()+installResource);

		    if(installFile.exists()){
		    	XStream xs = new XStream();
		    	xs.processAnnotations(Installation.class);
		    	install = (Installation) xs.fromXML(new FileInputStream(installFile));
		    	if(Installation.STATUS_COMPLETED.equalsIgnoreCase(install.getStatus())){
		    		Installation.DONE = true;
		    	}
		    	
		    	Installation.INSTANCE_ID = StringUtils.trimToEmpty(install.getInstanceID());
		    	Installation.INSTANCE_TYPE = install.getInstanceType();
		    	
		    }
		} catch (Exception e) {
			log.error("Unable load installation.xml, treat as system not install");
		}
	    
	    return install;
	}

	/**
	 * This method only used in single request model such as installation, startup. Otherwise, please use
	 * SettingService.saveInstallation() for thread-safe.
	 * @param install
	 */
	public static void saveInstallation(Installation install){
		FileOutputStream os = null;
		try {
			File installFile = FileUtil.getFile(DataRoot.getDataRoot()+installResource);
			os = new FileOutputStream(installFile);
			XStream xs = new XStream();
			xs.processAnnotations(Installation.class);
			xs.toXML(install,os);
			os.flush();
		} catch (Exception e) {
			log.error("Failed save or update installation file, this may cause install step redo.",e);
		} finally{
			if(os != null)
				try {os.close();} catch (IOException e) {}
		}
		
	}
	
	public static Installation loadDefault() throws IOException{
		XStream xs = new XStream();
    	xs.processAnnotations(Installation.class);
    	return (Installation) xs.fromXML(FileUtil.getFileInputStream(Installation.DEFAULT_FILE));
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String[] getSteps() {
		return steps;
	}
	public void setSteps(String[] steps) {
		this.steps = steps;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getInstanceID() {
		return instanceID;
	}

	public void setInstanceID(String instanceID) {
		this.instanceID = instanceID;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
}
