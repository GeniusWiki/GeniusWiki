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
package com.edgenius.wiki.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.edgenius.core.DataRoot;
import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.util.FileUtil;

/**
 * I try to make geniuswiki.war file have as few as possible properties. But put most properties outside deployed war file. 
 * This make deployment and migration becomes easier.
 *  
 * It try to solve this problem: only one properties is inside deployed package classpath and this file has a property point to a 
 * external directory(rootKey=root.directory). By this root directory, link to another properties, which can define any properties which
 * could be placeholder property in spring configuration file. <br>
 * 
 * This class also support a default Server properties file which can be clone while default {DATA_ROOT}/server.properties file does not exist.<br>
 * 
 * @author Dapeng.Ni
 */
public class SystemPropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor{
	private static final Logger log = LoggerFactory.getLogger(SystemPropertyPlaceholderConfigurer.class);


	private static final String SYS_TMP_DIR = "geniuswiki.tmp.dir";
	private static final String INSTANCE_ID = "geniuswiki.instance.id";
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		Properties props = loadProperties();
		
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource globalConf = loader.getResource(props.get(DataRoot.rootKey) + Global.FILE);
		initGlobal(globalConf);
		
		super.postProcessBeanFactory(beanFactory);
	}

	/**
	 * @return
	 */
	public Properties loadProperties() {
		try {
			
			String root = DataRoot.getDataRoot();
			log.info("System root directory is " + root);
			
			//All below values also treat as Spring placeholder properties....
			Properties props = new Properties();
			
			//Setup server.properties into placeholder properties
			props = Server.load();
			
			//Setup data root 
			props.put(DataRoot.rootKey, root);
			
			//Setup system default directory
			String tmpDir = FileUtil.TEMP_DIR;
			if(!tmpDir.endsWith(File.separator))
				tmpDir += File.separator;
			log.info("System temporary directory is " + tmpDir);
			props.put(SYS_TMP_DIR, tmpDir);
			
			//Setup instance ID
			Installation.refreshInstallation();
			props.put(INSTANCE_ID, Installation.INSTANCE_ID);

			//OK, put them as placeholder properties
			this.setProperties(props);
			
			//Setup ehcache disk store - for different instance, they user different path.
			System.setProperty("ehcache.disk.store.dir", FileUtil.getFullPath(tmpDir,"cache"));
			
			return props;
		} catch (Exception e) {
			log.error("Failed to load external properties.",e);
			throw new BeanInitializationException("Failed to load external properties.",e);
		}
	}

	/**
	 * Loading global content from Global configure xml file (defined by geniuswiki.properties), then
	 * push all value to <code>com.edgenius.core.Global</code> class, which value become static and ready for 
	 * later use. 
	 *
	 * There 3 level global setting. First, server.properties will assign a global.xml, this file usually is outside
	 * the deploy file, and put together with data file, this is makes upgrade easily. <br>
	 * Second, there is global.default.xml is inside classpath. This file is useful when installer initialized setup 
	 * system. see publish process.<br>
	 * Third, if both above file not exist, Global.java even has its default value. and it also will automatically generate
	 * global.xml in this case. <br>
	 * @param globalConf 
	 *   
	 */
	private void initGlobal(Resource globalConf){
		GlobalSetting setting = null;
		try {
			//http://forum.springframework.org/showthread.php?p=201562#post201562
			//don't use Resource.getInputStream() as it can not handle file://c:/var/data format: error is try to get unknown host "c"
			setting = GlobalSetting.loadGlobalSetting(new FileInputStream(globalConf.getFile()));
		} catch (Exception e) {
			log.info("Unable to load global xml, try load global default xml then...");
			setting = null;
		}
		if(setting == null){
			//try to load global.default.xml from class path.
			try {
				setting = GlobalSetting.loadGlobalSetting(FileUtil.getFileInputStream(Global.DEFAULT_GLOBAL_XML));
			} catch (Exception e) {
				log.warn("Loading global default xml failed, using Global class default instead.");
			}
			if(setting == null){
				//the third option, just use Global.java value
				//no global file found, so keep Global default static value instead.
				setting = new GlobalSetting();
				Global.syncTo(setting);
			}
			
			if(globalConf.exists()){
				//global exist, maybe wrong format, then try to backup original one
				try {
					String dir = FileUtil.getFileDirectory(globalConf.getFile().getAbsolutePath());
					String name = globalConf.getFilename() +"."+ new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".failure.backup";
					File orig  = new File(FileUtil.getFullPath(dir,name)); 
					FileUtils.copyFile(globalConf.getFile(),orig);
					log.info("Original global conf file rename to " + name);
				} catch (Exception e) {
					log.warn("Unable backup original global conf file, old one will replaced.");
				}
			}
			//Anyway, global.xml file under data root is missed or crashed, then create or recreate required.
			//As I want to user SettingService.saveOrUpdateGlobalSetting() to save rather than create duplicated code,
			//so here a little bit tricky,  I put a flag value to tell SettingService trigger saving in afterProperties()
			log.info("System is going to create/recreate new global.xml in your data root directory.");
			System.setProperty("rebuild.global.xml", "true");
			
		}
		
		//finally, initial Global static varible according to setting
		Global.syncFrom(setting);
	}
}
