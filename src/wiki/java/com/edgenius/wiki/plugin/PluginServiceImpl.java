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
package com.edgenius.wiki.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.edgenius.core.Global;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.server.utils.LinkUtil;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroManager;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.impl.BaseMacroParameter;
import com.edgenius.wiki.service.DataBinder;
import com.edgenius.wiki.service.ExportService;

import freemarker.core.ParseException;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * @author Dapeng.Ni
 */
public class PluginServiceImpl implements PluginService, InitializingBean, ApplicationContextAware {
	
	public static final Logger log = LoggerFactory.getLogger(PluginServiceImpl.class);
	
	private static final String DEFAULT_PROP_CONTEXT_PATH = "contextPath";
	private static final String DEFAULT_PROP_RESOURCE_PATH = "resourcePath";
	
	private String linkResource = "META-INF/services/" + LinkPlugin.class.getName();
	private String pluginResource = "META-INF/services/" + PluginServiceProvider.class.getName();
	private FreeMarkerConfigurer pluginTemplateEngine;
	private MacroManager macroManager;
	
	private Map<String, String> pluginContainer = new HashMap<String, String>();
	
	private Map<String, String> linkPluginContainer = new HashMap<String, String>();
	private PluginPool  pluginPool;
	private ApplicationContext applicationContext;
	
	public String invoke(String serviceName, String operation, String[] params) throws PluginServiceProviderException, PluginServiceException {
		
		Object obj =  applicationContext.getBean(serviceName);
		if(!(obj instanceof PluginServiceProvider)){
			throw new PluginServiceException("Failed to lookup servcie ["+serviceName +"]");
		}
		
		PluginServiceProvider provider = (PluginServiceProvider)obj;
		return provider.invokePluginService(operation, params);
	}

	public void backup(DataBinder binder) {
		for(String beanName: pluginContainer.keySet()){
			Object obj =  applicationContext.getBean(beanName);
			if(!(obj instanceof PluginServiceProvider)){
				log.error("Failed to lookup servcie ["+beanName +"]");
				AuditLogger.error("Failed to lookup servcie ["+beanName +"]");
				continue;
			}
			PluginServiceProvider provider = (PluginServiceProvider)obj;
			provider.backup(binder);
		}
	}

	public void restore(DataBinder binder) {
		for(String beanName: pluginContainer.keySet()){
			Object obj =  applicationContext.getBean(beanName);
			if(!(obj instanceof PluginServiceProvider)){
				log.error("Failed to lookup servcie ["+beanName +"]");
				AuditLogger.error("Failed to lookup servcie ["+beanName +"]");
				continue;
			}
			PluginServiceProvider provider = (PluginServiceProvider)obj;
			provider.restore(binder);
		}
		
	}

	public void resorePreClean() {
		for(String beanName: pluginContainer.keySet()){
			Object obj =  applicationContext.getBean(beanName);
			if(!(obj instanceof PluginServiceProvider)){
				log.error("Failed to lookup servcie ["+beanName +"]");
				AuditLogger.error("Failed to lookup servcie ["+beanName +"]");
				continue;
			}
			PluginServiceProvider provider = (PluginServiceProvider)obj;
			provider.resorePreClean();
		}
		
	}
	public  Map<File, String> getPluginResourceForExport() {
		
		Map<File,String> resources = new HashMap<File, String>();
		for(String beanName: pluginContainer.keySet()){
			Object obj = null;
			try {
				obj =  applicationContext.getBean(beanName);
			} catch (Exception e) {
				log.error("Failed to lookup servcie ["+beanName +"]", e);
			}
			if(!(obj instanceof PluginServiceProvider)){
				log.error("Failed to lookup servcie ["+beanName +"]");
				AuditLogger.error("Failed to lookup servcie ["+beanName +"]");
				continue;
			}
			
			PluginServiceProvider provider = (PluginServiceProvider)obj;
			Map<File, String> exportResources = provider.exportResources();
			if(exportResources != null && exportResources.size() > 0){
				resources.putAll(exportResources);
			}
		}
		
		return resources;
	}
	
	public String renderMacro(String macroName, Map<String,Object> map, RenderContext renderContext) throws PluginRenderException {
		Template templ = getTempl(macroName,renderContext.getRenderTarget());
		String content = null;
		if(templ != null){
			try {
				map = initialDefaultProperties(macroName, map,renderContext.getRenderTarget());
				content = FreeMarkerTemplateUtils.processTemplateIntoString(templ, map);
			} catch (Exception e) {
				log.error("Error while processing plugin FreeMarker template. ", e);
				throw new PluginRenderException("Unable to process macro by template:" + macroName, e);
			}
		}
		if(content != null){
			return content;
		}else{
			AuditLogger.warn("Unable to process macro by template:" + macroName);
			throw new PluginRenderException("Unable to process macro by template:" + macroName);
		}
	}
	public String renderMacro(MacroModel macroModel, RenderContext renderContext) {
		
		Macro macro = macroManager.getMacro(macroModel.macroName);
		if(macro == null){
			log.error("Unable to get {} macro to render.", macroModel.macroName);
			return  macroModel.toRichAjaxTag();
		}
			
		BaseMacroParameter params = new BaseMacroParameter();
		params.setParams(macroModel.values);
		params.setRenderContext(renderContext);
		String content = null;
		
		Template templ = getTempl(macroModel.macroName, renderContext.getRenderTarget());
		if(templ != null){
			Map<String, Object> map = macro.getTemplValues(params, renderContext, applicationContext);
			map = initialDefaultProperties(macroModel.macroName, map, renderContext.getRenderTarget());
			try {
				content = FreeMarkerTemplateUtils.processTemplateIntoString(templ, map);
			} catch (TemplateException e) {
				log.error("Error while processing plugin FreeMarker template. ", e);
			} catch (FileNotFoundException e) {
				AuditLogger.error("Error while open plugin template file. ");
			} catch (IOException e) {
				log.error("Error while generate plugin content. ", e);
			}
		}
		
		if(content != null){
			return content;
		}else{
			AuditLogger.warn("Unable to process macro by template:" + macroModel.macroName);
			return macroModel.toRichAjaxTag();
		}
	}
	



	//JDK1.6 @Override
	public LinkPlugin getCLinkByUuid(String uuid){
		String clz = linkPluginContainer.get(uuid);
		if(clz == null)
			return null;
		try {
			Object pluginObj = pluginPool.borrowObject(clz);
			if(!(pluginObj instanceof LinkPlugin)){
				log.error("Failed get a Plugin Object " + clz);
				return null;
			}
			
			pluginPool.returnObject(clz,pluginObj);
			
			return (LinkPlugin) pluginObj;
		} catch (Exception e) {
			log.error("Unable get plugin object " + clz, e);
		}
	
		return null;
	}

	
	//JDK1.6 @Override
	public String getPluginUuid(String pluginClz) {
		return UUID.nameUUIDFromBytes(pluginClz.getBytes()).toString();
	}


	//********************************************************************
	//               set / get methods
	//********************************************************************
	public void setPluginPool(PluginPool pluginPool) {
		this.pluginPool = pluginPool;
	}
	public void setPluginTemplateEngine(FreeMarkerConfigurer pluginTemplateEngine) {
		this.pluginTemplateEngine = pluginTemplateEngine;
	}

	public void setMacroManager(MacroManager macroManager) {
		this.macroManager = macroManager;
	}
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		
	}
	
	//JDK1.6 @Override
	public void afterPropertiesSet() throws Exception {
		//register plugin
		InputStream input = FileUtil.getFileInputStream(linkResource);
		BufferedReader is = new BufferedReader(new InputStreamReader(input));
		String linkPluginClz;
		while((linkPluginClz = is.readLine()) != null){
			//get macro class
			if(StringUtils.isBlank(linkPluginClz) || linkPluginClz.trim().startsWith("#")){
				//skip comment
				continue;
			}
			linkPluginContainer.put(getPluginUuid(linkPluginClz), linkPluginClz);
		}
		IOUtils.closeQuietly(is);
		IOUtils.closeQuietly(input);
		
		
		input = FileUtil.getFileInputStream(pluginResource);
		Properties prop = new Properties();
		prop.load(input);
		for(Iterator<Object> iter = prop.keySet().iterator();iter.hasNext();){
			String key = (String) iter.next();
			pluginContainer.put(key, prop.getProperty(key));
		}
		IOUtils.closeQuietly(input);
	}
	public Template getPluginTemplate(String macroName, String templName) {
		try {
			pluginTemplateEngine.getConfiguration().setLocale(Global.getDefaultLocale());
			return pluginTemplateEngine.getConfiguration().getTemplate(macroName+templName);
		}catch (ParseException e) {
			log.error("Parse " + macroName + " template failed", e);
		}catch (IOException e) {
			log.warn("Unable to find templ {}; Using toRichAjaxTag() instead.", macroName);
		}
	
		return null;
	}

	//********************************************************************
	//               private methods
	//********************************************************************
	private Template getTempl(String macroName, String renderTarget) {
		Template templ = null;
		if(RenderContext.RENDER_TARGET_EXPORT.equals(renderTarget)
			|| RenderContext.RENDER_TARGET_PLAIN_VIEW.equals(renderTarget)){
			templ = getPluginTemplate(macroName, "/macro-print.ftl");
			if(templ != null)
				return templ;
		}else if(RenderContext.RENDER_TARGET_RICH_EDITOR.equals(renderTarget)){
			templ = getPluginTemplate(macroName, "/macro-editor.ftl");
			if(templ != null)
				return templ;
		}
		return getPluginTemplate(macroName, "/macro.ftl");
	}
	/**
	 * @param macroModel
	 * @param map
	 * @param renderTarget 
	 * @return
	 */
	private Map<String, Object> initialDefaultProperties(String macroName, Map<String, Object> map, String renderTarget) {
		if(map == null){
			map = new HashMap<String, Object>();
		}
		String ctx;
		if(RenderContext.RENDER_TARGET_EXPORT.equals(renderTarget)){
			ctx = ExportService.EXPORT_HTML_SUB_DIR;
		}else{
			ctx = WebUtil.getWebConext();
			//remove last / for easy use in template
			if(ctx.endsWith("/"))
				ctx = ctx.substring(0,ctx.length()-1);
		}
		
		map.put(DEFAULT_PROP_CONTEXT_PATH, ctx);
		map.put(DEFAULT_PROP_RESOURCE_PATH, ctx+ "/plugins/" + macroName+"/resources");
		return map;
	}

	public static void main(String[] args) {
		String t = "1:2:a1c52d6d7-fa60-3e6c-933f-b2fd38559debabc";
		String[] ts = LinkUtil.parseCLinkParamters(t);
		if(ts != null)
		for (int i1 = 0; i1 < ts.length; i1++) {
			System.out.println(ts[i1]);
		}
		
	}

	public void setPluginResource(String pluginResource) {
		this.pluginResource = pluginResource;
	}

}
