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
package com.edgenius.wiki.render.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.edgenius.core.util.AuditLogger;
import com.edgenius.wiki.render.ImmutableContentMacro;
import com.edgenius.wiki.render.Macro;
import com.edgenius.wiki.render.MacroManager;

/**
 * @author Dapeng.Ni
 */
public class MacroManagerImpl implements MacroManager, ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(MacroManagerImpl.class);
	private static final String macroResource = "META-INF/services/" + Macro.class.getName();
	
	private Map<String[],Macro> container = new LinkedHashMap<String[], Macro>();
	//a name by name container to so quick locate when try to get by name
	private Map<String,String[]> containerIndex = new LinkedHashMap<String,String[]>();
	
	private List<Macro> immutableContentMacroMap = new ArrayList<Macro>();
	private ApplicationContext applicationContext;
	
	//JDK1.6 @Override
	public Macro getMacro(String macroName) {
		String[] key = containerIndex.get(macroName.toLowerCase());
		return container.get(key);
	}

	//JDK1.6 @Override
	public Collection<Macro> getMacros() {
		return container.values();
	}


	//JDK1.6 @Override
	public Collection<Macro> getImmutableContentMacros() {
		return immutableContentMacroMap;
	}
	
	public void addMacro(Macro macro, boolean immutable){
		if(macro == null)
			return;
		
		macro.init(applicationContext);
		
		String[] names = macro.getName();
		container.put(names, macro);
		//put to containerIndex, so that macro can quick locate when try to get by name
		for (String name: names) {
			containerIndex.put(name.toLowerCase(), names);
		}
		
		if(immutable)
			immutableContentMacroMap.add(macro);
		
	}
	//This method is executed by Spring init attribute
	public void load() {
		ClassLoader classLoader = Macro.class.getClassLoader();

		BufferedReader is = null;
		try {
			is = new BufferedReader(new InputStreamReader(classLoader.getResourceAsStream(macroResource)));
			String macroClz;
			while((macroClz = is.readLine()) != null){
				//get macro class
				if(StringUtils.isBlank(macroClz) || macroClz.trim().startsWith("#")){
					//skip comment
					continue;
				}
				try {
					Object obj = classLoader.loadClass(macroClz.trim()).newInstance();
					if(obj instanceof Macro){
						Macro macro = (Macro) obj;
						macro.init(applicationContext);
						String[] names = macro.getName();
						container.put(names, macro);
						log.info("Macro class loading success:" + names[0]);
						//put to containerIndex, so that macro can quick locate when try to get by name
						for (String name: names) {
							containerIndex.put(name.toLowerCase(), names);
						}
			
						if(obj instanceof ImmutableContentMacro){
							if(!macro.isPaired()){
								AuditLogger.error("Unexpected case: ImmutableMacro is not paired macro, is that correct?" + Arrays.toString(macro.getName()));
								throw new Exception("Unexpected case: ImmutableMacro is not paired macro, is that correct?" + Arrays.toString(macro.getName()));
							}
							immutableContentMacroMap.add(macro);
						}
					}
				} catch (Exception e) {
					log.error("Initial macro class " + macroClz + " failed. This macro is ignored!!!",e);
				}
			}
		} catch (IOException e) {
			log.error("Macro resource file " + macroResource + " not found.",e);
		} finally{
			IOUtils.closeQuietly(is);
		}
		
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
