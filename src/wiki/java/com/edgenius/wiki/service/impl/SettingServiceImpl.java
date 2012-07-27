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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.GlobalSetting;
import com.edgenius.core.Installation;
import com.edgenius.core.Server;
import com.edgenius.core.UserSetting;
import com.edgenius.core.dao.ConfigurationDAO;
import com.edgenius.core.model.Configuration;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserService;
import com.edgenius.wiki.InstanceSetting;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.SettingServiceException;
import com.edgenius.wiki.service.SpaceService;
import com.thoughtworks.xstream.XStream;
/**
 * @author Dapeng.Ni
 */
@Transactional
public class SettingServiceImpl implements SettingService,  InitializingBean{
	private Logger log = LoggerFactory.getLogger(SettingServiceImpl.class);
	
	@Autowired private ConfigurationDAO configurationDAO;
	private UserService userService; 
	private Resource globalConf;

	private SpaceService spaceService;

	private InstanceSetting cachedInstanceSetting = null;
	
	private ReentrantReadWriteLock globalXMLLock = new ReentrantReadWriteLock(); 
	private ReentrantReadWriteLock serverPropLock = new ReentrantReadWriteLock(); 
	private ReentrantReadWriteLock installXMLLock = new ReentrantReadWriteLock(); 
	//***********************************************************
	// Service methods
	//***********************************************************

	public GlobalSetting getGlobalSetting(){
		//try to load global.default.xml from class path.
		GlobalSetting setting = null;
		try {
			setting = GlobalSetting.loadGlobalSetting(new FileInputStream(globalConf.getFile()));
		} catch (Exception e) {
			log.warn("Loading global default xml failed, using Global class default instead.");
		}
		return setting;
	}
	public void saveOrUpdateGlobalSetting(GlobalSetting setting) throws SettingServiceException{
		//persistent new value to files
		OutputStream os = null;
		WriteLock lock = globalXMLLock.writeLock();
		lock.lock();
		try {
			log.info("Global setting is updating.");
			os = new FileOutputStream(globalConf.getFile());
			XStream xstream = new XStream();
			xstream.toXML(setting,os);
			os.flush();
			log.info("Global setting is updated");
			
			//refresh sync if it is global setting
			Global.syncFrom((GlobalSetting)setting);
		} catch (FileNotFoundException e) {
			throw new SettingServiceException(e);
		} catch (IOException e) {
			throw new SettingServiceException(e);
		}finally{
			try {
				if(os != null)
					os.close();
			} catch (Exception e) {
				//do nothing
			}
			lock.unlock();
		}
		
		
	}
	public Server getServerSetting() {
		Properties prop = null;
		try {
			prop = Server.load();
		} catch (IOException e) {
			log.error("System server.properties file load failed", e);
		}
		Server server = new Server();
		if(prop != null)
			server.syncFrom(prop);
		return server;
	}
	public void saveOrUpdateServerSetting(Server server) throws SettingServiceException{
		try {
			serverPropLock.writeLock().lock();
			server.saveTo();
		} catch (Exception e) {
			log.error("Unable save server.properties.",e);
			throw new SettingServiceException(e);
		} finally{
			serverPropLock.writeLock().unlock();
		}

	}
	
	public void saveInstallation(Installation install){
		try {
			installXMLLock.writeLock().lock();
			Installation.saveInstallation(install);
		} catch (Exception e) {
			log.error("Save installation failed.",e);
		} finally{
			installXMLLock.writeLock().unlock();
		}
	}
	public Configuration saveOrUpdateUserSetting(User user, UserSetting setting){
		if(user == null || user.isAnonymous()){
			log.info("anonymous can not update its user setting.");
			if(user != null)
				return user.getConfiguration();
			else
				return null;
		}
		
		Configuration conf = user.getConfiguration();
		conf = saveOrUpdateSetting(setting, conf);
		
		//this conf maybe new one
		user.setConfiguration(conf);
		//reset user setting so that setting can be read out from configuration in next call out.
		//NOTE: setting is transient value
		user.setSetting(null);
		
		//call userService so that user cache can be update as well. 
		userService.updateUser(user);
		return conf;
		
		
	}
	public Configuration saveOrUpdateSpaceSetting(Space space, SpaceSetting setting){
		
		Configuration conf = space.getConfiguration();
		conf = saveOrUpdateSetting(setting, conf);
		
		//this conf maybe new one
		space.setConfiguration(conf);
		//reset user setting so that setting can be read out from configuration in next call out.
		//NOTE: setting is transient value
		space.setSetting(null);
		
		//call spaceService so that user cache can be update as well. 
		spaceService.updateSpace(space, false);

		return conf;
		
		
	}

	//JDK1.6 @Override
	public InstanceSetting getInstanceSetting() {
		//try to use singleton? good or bad?
		if(cachedInstanceSetting != null){
			return cachedInstanceSetting;
		}
		
		Configuration conf = configurationDAO.getByType(InstanceSetting.class.getName());
		InstanceSetting setting = null;
		if(conf != null && conf.getValue() != null){
			XStream xstream = new XStream();
			setting = (InstanceSetting) xstream.fromXML(conf.getValue());
		}
		if(setting == null){
			log.warn("Unable load instance setting, using default one instead.");
			setting = new InstanceSetting();
		}
		if(StringUtils.isBlank(setting.getDashboardMarkup())){
			setting.setDashboardMarkup(SharedConstants.DEFAULT_DAHSBOARD_MARKUP);
		}
	
		this.cachedInstanceSetting = setting;
		
		return setting;
	}


	//JDK1.6 @Override
	public void saveOrUpdateInstanceSetting(InstanceSetting setting) {
		this.cachedInstanceSetting = setting;
		Configuration conf = configurationDAO.getByType(InstanceSetting.class.getName());
		saveOrUpdateSetting(setting, conf);
	}


	//JDK1.6 @Override
	public void resetSetting(){
		cachedInstanceSetting = null;
	}
	//********************************************************************
	//               Private methods
	//********************************************************************
	private <T> Configuration saveOrUpdateSetting(T setting, Configuration conf) { 
		if(conf == null){
			conf = new Configuration();
			conf.setType(setting.getClass().getName());
		}
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		String str = xstream.toXML(setting);
		conf.setValue(str);
		configurationDAO.saveOrUpdate(conf);
		
		return conf;
	}

	
	//***********************************************************
	// GET / SET METHODS
	//***********************************************************
	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	public void setGlobalConf(Resource globalConf) {
		this.globalConf = globalConf;
	}


	public void setSpaceService(SpaceService spaceService) {
		this.spaceService = spaceService;
	}

	public void setOfflineDBVersion(int offlineDBVersion) {
		WikiConstants.offlineDBVersion = offlineDBVersion;
	}
	public void setOfflineDefaultDBVersion(int offlineDefaultDBVersion) {
		WikiConstants.offlineMainDBVersion = offlineDefaultDBVersion;
	}


	public void afterPropertiesSet() throws Exception {
		if(globalConf == null){
			throw new BeanInitializationException("Must assign global setting configure XML file");
		}
		
		if("true".equals(System.getProperty("rebuild.global.xml"))){
			try {
				GlobalSetting setting = new GlobalSetting();
				Global.syncTo(setting);
				saveOrUpdateGlobalSetting(setting);
			} catch (SettingServiceException e) {
				log.error("System try to create a global XML configure file, but failed with error." ,e);
			}	
			System.setProperty("rebuild.global.xml","");
		}
	}

}
