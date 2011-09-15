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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.dao.WidgetDAO;
import com.edgenius.wiki.model.Widget;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.WidgetService;
import com.edgenius.wiki.util.WikiUtil;
import com.edgenius.wiki.widget.WidgetException;
import com.edgenius.wiki.widget.WidgetTemplate;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class WidgetServiceImpl implements WidgetService, ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(WidgetServiceImpl.class);
	private String widgetResource = "META-INF/services/" + WidgetTemplate.class.getName();
	
	//type,widget
	Map<String,WidgetTemplate> container = new HashMap<String,WidgetTemplate>();
	private WidgetDAO widgetDAO;
	private ApplicationContext applicationContext;
	private UserReadingService userReadingService;
	private SecurityService securityService;
	
	/**
	 * This method is executed in Spring init-method.
	 */
	public void loadWidgetTemplates(){
		ClassLoader classLoader = WidgetTemplate.class.getClassLoader();
		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(widgetResource)));
			String widgetClz;
			while((widgetClz = is.readLine()) != null){
				//get macro class
				if(StringUtils.isBlank(widgetClz) || widgetClz.trim().startsWith("#")){
					//skip comment
					continue;
				}
				try {
					Object obj = classLoader.loadClass(widgetClz.trim()).newInstance();
					if(obj instanceof WidgetTemplate){
						WidgetTemplate widget = ((WidgetTemplate)obj);
						widget.init(applicationContext);
						String type = widget.getType();
						container.put(type, widget);
						log.info("Widget class loading success:" + type);
					}
				} catch (Exception e) {
					log.error("Initial widget class " + widgetClz + " failed. This widget is ignored!!!",e);
				}
			}
		} catch (IOException e) {
			log.error("Widget resource file " + widgetResource + " not found.",e);
		} finally{
			IOUtils.closeQuietly(is);
		}
	}
	
	//JDK1.6 @Override
	public List<Widget> getListedWidgets(User viewer) {

		List<Widget> retList = new ArrayList<Widget>();
		Collection<WidgetTemplate> list = container.values();
		for (WidgetTemplate widget : list) {
			Widget obj = widget.createWidgetObject(null);
			if(obj != null){
				retList.add(obj);
			}
		}
		

		return retList;
	}

	//JDK1.6 @Override
	public WidgetTemplate getWidget(String type){
		return container.get(type);
	}

	//JDK1.6 @Override
	public Widget getWidgetByKey(String key) {
		//if postgreSQL, key is null may cause error - I don't investigate 
		//what exact case will cause widget UUID is null,but happen in my local test 
		//server(running from very old version,so may caused by bug in old version). Anyway, use trimToNull() to avoid this.
		return widgetDAO.getByUUID(StringUtils.trimToEmpty(key));
	}


	//JDK1.6 @Override
	public Widget invokeWidget(String type, String key, User viewer) throws WidgetException {
		WidgetTemplate templ = container.get(type);
		if(templ == null)
			return null;
		
		
		Widget widget = templ.invoke(key,viewer);
		
		int[] perms = new int[2]; 
		perms[0] = securityService.isAllowWidget(OPERATIONS.READ, key, viewer)?1:0;
		perms[1] = securityService.isAllowWidget(OPERATIONS.WRITE, key, viewer)?1:0;
		
		widget.setPermimssion(perms);
		
		return widget;
	}

	public void resetWidgets() {
		// TODO Auto-generated method stub
		for(WidgetTemplate widget: container.values()){
			widget.reset();
		}
	}

	//JDK1.6 @Override
	public Widget saveOrUpdateWidget(String uuid, String type, String content, String title, String description, boolean shared) {
		
		boolean update = true;
		Widget widget = null;
		if(!StringUtils.isBlank(uuid)){
			widget = widgetDAO.getByUUID(uuid);
		}
		if(widget == null){
			update = false;
			widget = new Widget();
			widget.setUuid(UUID.randomUUID().toString());
		}
		
		widget.setType(type);
		widget.setContent(content);
		widget.setTitle(title);
		widget.setDescription(description);
		//at moment, always share
		widget.setShared(shared);
		
		WikiUtil.setTouchedInfo(userReadingService, widget);
		widgetDAO.saveOrUpdate(widget);
		
		if(!update){
			// initial permission as well
			securityService.initResourcePermission(widget);
		}
		
		return widget;
	}
	//JDK1.6 @Override
	public Widget removeWidget(String key) {
		Widget widget = widgetDAO.getByUUID(key);
		if(widget != null){
			securityService.removeResource(key);
			widgetDAO.remove(widget.getUid());
		}
		return widget; 
	}
		

	//JDK1.6 @Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	//********************************************************************
	//               set / get
	//********************************************************************
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}

	public void setWidgetDAO(WidgetDAO widgetDAO) {
		this.widgetDAO = widgetDAO;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
