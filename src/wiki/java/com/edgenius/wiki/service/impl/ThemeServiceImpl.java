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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryQuotaException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.repository.RepositoryTiemoutExcetpion;
import com.edgenius.core.util.CompareToComparator;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.ZipFileUtil;
import com.edgenius.core.util.ZipFileUtilException;
import com.edgenius.wiki.PageTheme;
import com.edgenius.wiki.Skin;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.SettingService;
import com.edgenius.wiki.service.ThemeInvalidException;
import com.edgenius.wiki.service.ThemeInvalidVersionException;
import com.edgenius.wiki.service.ThemeNotFoundException;
import com.edgenius.wiki.service.ThemeSaveException;
import com.edgenius.wiki.service.ThemeService;

/**
 * @author Dapeng.Ni
 */
@Transactional
public class ThemeServiceImpl implements ThemeService, InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(ThemeServiceImpl.class);
	
	private static final String INSTALL_EXT_NAME = ".zip";
	private static final String SKIN_OR_THEME_EXT = ".xml";
	private static final String THEME_NAME = "theme";
	private static final String SKIN_NAME = "skin";
	
	private static final String DEFUALT_THEME_NAME = "defaultwiki";

	//private static final String DEFUALT_BLOG_THEME_NAME = "defaultblog";

	private Cache themeCache;
	private Resource themeResourcesRoot;
	private Resource themeExplosionRoot;
	private Resource skinResourcesRoot;
	private Resource skinExplosionRoot;
	private RepositoryService repositoryService;
	@Autowired
	private SettingService settingService;

	//********************************************************************
	//               Skin
	//********************************************************************
	public List<Skin> getAvailableSkins() {
		/*
		 * Get skin from install zip files list first, then validate if they are explode....
		 * I am just not very sure explode mode is good way to implement skin. maybe change to 
		 * servlet download theme resource rather than put them into web root directory 
		 * However, ... so far, read zip is not quite necessary...
		 */
		Map<Long, Skin> skinList = new TreeMap<Long, Skin>(new CompareToComparator<Long>(CompareToComparator.TYPE_KEEP_SAME_VALUE|CompareToComparator.DESCEND));
		ArrayList<Skin> skins = new ArrayList<Skin>();
		try {
			if(!skinResourcesRoot.getFile().isDirectory()){
				log.error("The skin install root is not directory {}, no install skin detected",skinResourcesRoot.getFile().getAbsolutePath());
				return skins;
			}

			File[] files = skinResourcesRoot.getFile().listFiles( (FileFilter)new SuffixFileFilter(INSTALL_EXT_NAME));
			String appliedSkin = Global.Skin;
			if(StringUtils.isBlank(appliedSkin)){
				appliedSkin = Skin.DEFAULT_SKIN;
			}
			for(File zip : files){
				String skinXML = getMetafile(zip,"skin.xml");
				InputStream sis = IOUtils.toInputStream(skinXML);
				Skin skin = Skin.fromXML(sis);
				IOUtils.closeQuietly(sis);
				
				if(StringUtils.isBlank(skin.getPreviewImageName())){
					skin.setPreviewImageName(Skin.DEFAULT_PREVIEW_IMAGE);
				}

				//set initial status
				long factor = zip.lastModified();
				if(appliedSkin.equalsIgnoreCase(skin.getName())){
					factor = Long.MAX_VALUE; //always first. 
					skin.setStatus(Skin.STATUS_APPLIED);
					//can not remove applied skin
					skin.setRemovable(false);
				}else{
					skin.setStatus(Skin.STATUS_CANDIDATE);
					skin.setRemovable(true);
					//we also assume user may put default skin zip to install directory
					if(Skin.DEFAULT_SKIN.equalsIgnoreCase(skin.getName())){
						//can not remove default skin
						skin.setRemovable(false);	
					}
				
				}
				
				//put into list
				skinList.put(factor,skin);
			}
			
			skins = new ArrayList<Skin>(skinList.values());
			
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// Append default skin and  Decide Skin.STATUS_DEPLOYED
			if(!skinExplosionRoot.getFile().isDirectory()){
				log.error("The skin explosion root is not directory {}, no deployed skin detected",skinExplosionRoot.getFile().getAbsolutePath());
			}else{
				File[] deployDirs = skinExplosionRoot.getFile().listFiles((FileFilter)DirectoryFileFilter.INSTANCE);
				for(File dir : deployDirs){
					File file = getSkinXML(dir);
					
					if(!file.exists()){
						log.warn("Unable to find skin.xml on skin directory {}", dir.getAbsolutePath());
						continue;
					}
					
					//Load the default value of this skin from file system
					FileInputStream is = null; 
					try {
						is = new FileInputStream(file);
						Skin skin = Skin.fromXML(is);
						if(skin != null){
							int idx = skins.indexOf(skin);
							if(idx != -1){
								skin = skins.get(idx);
								if(skin.getStatus() < Skin.STATUS_APPLIED){
									skin.setStatus(Skin.STATUS_DEPLOYED);
								}
							}else{
								//No zipped default skin in install directory, so insert it to return list.
								if(Skin.DEFAULT_SKIN.equalsIgnoreCase(skin.getName())){
									//can not remove default skin
									int ins; // if it is applied, always first, otherwise, second
									skin.setRemovable(false);	
									if(appliedSkin.equalsIgnoreCase(skin.getName())){
										ins = 0;
										skin.setStatus(Skin.STATUS_APPLIED);
									}else{
										ins = skins.size() > 0?1:0;
										skin.setStatus(Skin.STATUS_DEPLOYED);
									}
									//insert default skin
									skins.add(ins, skin);
								}
							}
						}
					} catch (Exception e) {
						log.error("Failed load skin " + file.getAbsolutePath(), e);
					} finally{
						IOUtils.closeQuietly(is);
					}
				}
				
				for (Iterator<Skin> iter = skins.iterator();iter.hasNext();) {
					Skin skin = iter.next();
					if(skin.getStatus() == Skin.STATUS_CANDIDATE){
						//remove it from list 
						iter.remove();
						log.warn("Skin {} is removed from visible list as it was not deployed.", skin.getName());
					}
					
				}
			}
		} catch (IOException e) {
			log.error("Unable retrieve skin home directory.",e);
		}
						
		return skins;
	}


	public Skin getAppliedSkin() throws IOException {
		List<Skin> list = getAvailableSkins();
		String appliedSkin = Global.Skin;
		if(StringUtils.isBlank(appliedSkin)){
			appliedSkin = Skin.DEFAULT_SKIN;
		}
		Skin currentSkin = null;
		for (Skin skin : list) {
			if(appliedSkin.equalsIgnoreCase(skin.getName())){
				currentSkin = skin;
				break;
			}
		}
		File dir = new File(FileUtil.getFullPath(skinExplosionRoot.getFile().getAbsolutePath(), appliedSkin, "layout"));
		
		//we suppose system must has an applied skin, so currentSkin never be null
		File viewLayoutFile = new File(dir, "view.html");
		File editLayoutFile = new File(dir, "edit.html");
		
		if(!viewLayoutFile.exists()){
			//if not exist, then try to get default one
			File defaultDir = new File(FileUtil.getFullPath(skinExplosionRoot.getFile().getAbsolutePath(), Skin.DEFAULT_SKIN, "layout"));
			viewLayoutFile = new File(defaultDir, "view.html");
		}
		if(!editLayoutFile.exists()){
			//if not exist, then try to get default one
			File defaultDir = new File(FileUtil.getFullPath(skinExplosionRoot.getFile().getAbsolutePath(), Skin.DEFAULT_SKIN, "layout"));
			editLayoutFile = new File(defaultDir, "edit.html");
		}
		
		currentSkin.setViewLayout(StringEscapeUtils.escapeJavaScript(FileUtils.readFileToString(viewLayoutFile)));
		currentSkin.setEditLayout(StringEscapeUtils.escapeJavaScript(FileUtils.readFileToString(editLayoutFile)));
		
		return currentSkin;
	}

	public void installSkin(File skinFile) throws ThemeInvalidException,ThemeInvalidVersionException {

		try {
			String name = explodeSkin(skinFile, true);
			
			//put install file to resource directory, replace old one if existing.
			File installFile = new File(skinResourcesRoot.getFile(),name+INSTALL_EXT_NAME);
			if(installFile.exists()){
				installFile.delete();
			}
			FileUtils.moveFile(skinFile, installFile);
		} catch (ThemeInvalidException e) {
			log.error("Unable to install skin.", e);
			throw e;
		} catch (IOException e) {
			log.error("Unable to install skin.", e);
			throw new ThemeInvalidException(e);
		}
	}
	
	public boolean removeSkin(String skinName) throws ThemeSaveException {
		try {
			File dir = new File(skinExplosionRoot.getFile(), skinName);
			if(dir.isDirectory()){
				File file = new File(dir, "skin.xml");
				if(file.exists()){
					InputStream sis = new FileInputStream(file);
					Skin skin = Skin.fromXML(sis);
					IOUtils.closeQuietly(sis);
					if(skin != null && StringUtils.equalsIgnoreCase(skin.getName(),skinName)
						&& !Skin.DEFAULT_SKIN.equalsIgnoreCase(skinName) 
						&& !StringUtils.equalsIgnoreCase(skinName, Global.Skin)){
						//delete exploded files
						FileUtils.deleteDirectory(dir);
						
						
						//delete installed file
						File install = new File(skinResourcesRoot.getFile(),skinName + INSTALL_EXT_NAME);
						if(install.exists() && install.isFile()){
							if(!install.delete()){
								install.deleteOnExit();
							}
						}
						return true;
					}
				}
			}
		} catch (IOException e) {
			log.error("Remove skin file failed.",e);
			throw new ThemeSaveException(e);
		}
		
		
		return false;
	}
	public File downloadSkin(String name) {
		try {
			File installFile = new File(skinResourcesRoot.getFile(),name+INSTALL_EXT_NAME);
			if(installFile.exists())
				return installFile;
			
			File dir = new File(skinExplosionRoot.getFile(),name);
			File file = new File(dir, "skin.xml");
			if(file.exists()){
				 InputStream is = null;
				 try {
					is = new FileInputStream(file);
					Skin skin = Skin.fromXML(is);
					if(skin != null && StringUtils.equalsIgnoreCase(name,skin.getName())){
						//zip dir and return it.
						String downDir = FileUtil.createTempDirectory("_sd");
						String downFile = FileUtil.getFullPath(downDir,name + INSTALL_EXT_NAME);
						Map<File, String> listToZip = new HashMap<File, String>();
						String rootDir = dir.getCanonicalPath();
						listToZip.put(dir, rootDir);
						ZipFileUtil.createZipFile(downFile, listToZip, true);
						
						return new File(downFile);
					}
				} catch (Exception e) {
					log.error("Failed load skin " + file.getAbsolutePath(), e);
				} finally{
					IOUtils.closeQuietly(is);
				}
			 }
		} catch (IOException e) {
			log.error("Download skin failed : " + name, e);
		}
		return null;
	}

	//********************************************************************
	//               Theme
	//********************************************************************
	public void enableTheme(String name, boolean enable){
		try {
			File dir = new File(themeExplosionRoot.getFile(),name);
			File file = new File(dir, "theme.xml");
			if(file.exists()){
				InputStream is = new FileInputStream(file);
				Theme theme = Theme.fromXML(is);
				IOUtils.closeQuietly(is);
				
				if(theme != null && StringUtils.equalsIgnoreCase(name,theme.getName())){
					theme.setStatus(enable?0:1);
					theme.toFile(file);
					
					//need refresh when loading
					themeCache.remove(name);
				}
			}
		} catch (IOException e) {
			log.error("Download theme failed : " + name, e);
		}
	}
	public File downloadTheme(String name){
		try {
			File installFile = new File(themeResourcesRoot.getFile(),name+INSTALL_EXT_NAME);
			if(installFile.exists())
				return installFile;
			
			File dir = new File(themeExplosionRoot.getFile(),name);
			File file = new File(dir, "theme.xml");
			if(file.exists()){
				 InputStream is = null;
				 try {
					is = new FileInputStream(file);
					Theme theme = Theme.fromXML(is);
					if(theme != null && StringUtils.equalsIgnoreCase(name,theme.getName())){
						//zip dir and return it.
						String downDir = FileUtil.createTempDirectory("_td");
						String downFile = FileUtil.getFullPath(downDir,name + INSTALL_EXT_NAME);
						Map<File, String> listToZip = new HashMap<File, String>();
						String rootDir = dir.getCanonicalPath();
						listToZip.put(dir, rootDir);
						ZipFileUtil.createZipFile(downFile, listToZip, true);
						
						return new File(downFile);
					}
				} catch (Exception e) {
					log.error("Failed load theme " + file.getAbsolutePath(), e);
				} finally{
					IOUtils.closeQuietly(is);
				}
			 }
		} catch (IOException e) {
			log.error("Download theme failed : " + name, e);
		}
		return null;
	}
	/**
	 * Default or theme is already adopted by spaces can not be removed. 
	 * @param themeName
	 * @return
	 * @throws ThemeSaveException
	 */
	public boolean removeTheme(String themeName) throws ThemeSaveException {
		try {
			File dir = new File(themeExplosionRoot.getFile(), themeName);
			if(dir.isDirectory()){
				File file = new File(dir, "theme.xml");
				if(file.exists()){
					InputStream sis = new FileInputStream(file);
					Theme theme = Theme.fromXML(sis);
					IOUtils.closeQuietly(sis);
					
					if(theme != null && StringUtils.equalsIgnoreCase(theme.getName(),themeName)
						&& theme.isRemovable()
						&& theme.getUsedBySpaces() == 0 
						&& (!Theme.DEFAULT_THEME_WIKI.equalsIgnoreCase(themeName)
							|| !Theme.DEFAULT_THEME_BLOG.equalsIgnoreCase(themeName))){
						
						themeCache.remove(themeName);
						
						//delete exploded files
						FileUtils.deleteDirectory(dir);
						
						//delete installed file
						File install = new File(themeResourcesRoot.getFile(),themeName + INSTALL_EXT_NAME);
						if(install.exists() && install.isFile()){
							if(!install.delete()){
								install.deleteOnExit();
							}
						}
						return true;
					}
				}
			}
		} catch (IOException e) {
			log.error("Remove theme file failed.",e);
			throw new ThemeSaveException(e);
		}
		
		
		return false;
	}
	public void installTheme(File themeFile) throws ThemeInvalidException,ThemeInvalidVersionException {
		try {
			String name = explodeTheme(themeFile, true);
			
			//put install file to resource directory, replace old one if existing.
			File installFile = new File(themeResourcesRoot.getFile(),name+INSTALL_EXT_NAME);
			if(installFile.exists()){
				installFile.delete();
			}
			FileUtils.moveFile(themeFile, installFile);
		} catch (ThemeInvalidException e) {
			log.error("Unable to install theme.", e);
			throw e;
		} catch (IOException e) {
			log.error("Unable to install theme.", e);
			throw new ThemeInvalidException(e);
		}
		
	}

	public List<Theme> getAvailableThemes(boolean includeDisabled) {
		Map<Integer, Theme> themeList = new TreeMap<Integer, Theme>(new CompareToComparator<Integer>());
		try {
			if(!themeExplosionRoot.getFile().isDirectory()){
				log.error("The theme explosion root is not directory {}, no theme detected",themeExplosionRoot.getFile().getAbsolutePath());
				return new ArrayList<Theme>();
			}
			File[] files = themeExplosionRoot.getFile().listFiles( (FileFilter)DirectoryFileFilter.INSTANCE);
			for(File dir : files){
				File file = getThemeXML(dir);
				
				if(!file.exists()){
					log.warn("Unable to find theme.xml on theme directory {}", dir.getAbsolutePath());
					continue;
				}
				
				FileInputStream is = null; 
				try {
					is = new FileInputStream(file);
					Theme theme = Theme.fromXML(is);
					
					if(theme.getStatus() == 0 || (theme.getStatus() != 0 && includeDisabled)){
						themeList.put(theme.getDisplaySequenceNumber(),theme);
					}
				} catch (Exception e) {
					log.error("Failed load theme from " + dir, e);
				} finally{
					IOUtils.closeQuietly(is);
				}
			
			}
		} catch (IOException e) {
			log.error("Unable retrieve theme home directory.",e);
		}
						
		return new ArrayList<Theme>(themeList.values());
	}

	/**
	 * Save customized theme into space setting
	 * @param theme
	 * @throws ThemeSaveException
	 */
	//JDK1.6 @Override
	public void saveOrUpdatePageTheme(Space space, PageTheme theme) throws ThemeSaveException {
		OutputStream os = null;
		try {
			String name = getThemeName(space);
			
			SpaceSetting setting = space.getSetting();
			//update or add 
			List<PageTheme> pageThemes = setting.getPageThemes();
			if(pageThemes == null){
				pageThemes = new ArrayList<PageTheme>();
				setting.setPageThemes(pageThemes);
			}else{
				//as Theme use scope as key, so remove same scope first, then add it again.
				pageThemes.remove(theme);
			}
			pageThemes.add(theme);
			
			//update space setting : customized theme
			space.getSetting().setCustomizedTheme(true);
			settingService.saveOrUpdateSpaceSetting(space, setting);
			
			//clean cache, so that getTheme() will refresh from dish file
			themeCache.remove(name);
		} catch (Exception e) {
			throw new ThemeSaveException(e);
		}finally{
			try {
				if(os != null)
					os.close();
			} catch (Exception e) {
				//do nothing
			}
		}
	}


	/**
	 * Get theme by given space name.
	 */
	public Theme getSpaceTheme(Space space) {
		String name;
		name = getThemeName(space);
		
		Theme theme = null;
		Element ele = themeCache.get(name);
		if(ele == null){
			//Load the default value of this theme from file system
			theme = getSystemDefaultTheme(space.getSetting().getTheme());
			
			//Then, merge PageTheme from space default setting as well.
			SpaceSetting setting = space.getSetting();
			if(setting.isCustomizedTheme() && setting.getPageThemes() != null && setting.getPageThemes().size() > 0){
				
				List<PageTheme> spaceLevelPageThemes = theme.getPageThemes();
				for(PageTheme pt :setting.getPageThemes()){
					if(PageTheme.SCOPE_DEFAULT.equalsIgnoreCase(StringUtils.trim(pt.getScope()))
						|| PageTheme.SCOPE_HOME.equalsIgnoreCase(StringUtils.trim(pt.getScope()))){
						//replace
						spaceLevelPageThemes.remove(pt); // PageTheme only compare scope, so it is OK to do remove here. 
						spaceLevelPageThemes.add(pt);
					}else{
						//add
						spaceLevelPageThemes.add(pt);
					}
				}
				ele = new Element(WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX + space.getUnixName(),theme);
				themeCache.put(ele);
			}
		}else{
			theme = (Theme) ele.getObjectValue();
		}
		
		//need return clonable object, so that any change on the return object won't impact original value. 
		return (Theme) theme.clone();
	}

	/**
	 * There is priority for current page theme: 
	 * <li>First, try to get by pageUuid, if exist, set it as current page theme.</li>
	 * <li>Then try to get pageType, if exist, set it as current page theme.</li>
	 * <li>Finally, try to get default page theme(SCOPE_DEFAULT=0), and set it as current page theme.</li>
	 * 
	 * <br>
	 * All theme values also do inherit(except PageThemeObject.type): 
	 * <li>If value is not empty (blank is also valid value), using current value.</li>
	 * <li>If value is null, extends from default page theme(SCOPE_DEFAULT=0)</li>
	 * 
	 */
	public Theme getPageTheme(AbstractPage page, String pageScope) {
		Space space = page.getSpace(); 
		
		Theme theme = getSpaceTheme(space);
		
		//fill Theme.currentMarkup then return
		PageTheme pTheme = null;
		if(theme.getPageThemes() != null){
			//priority: pageUuid > pageType
			pTheme = theme.getPageThemeByScope(page.getPageUuid());
			if(pTheme == null)
				pTheme = theme.getPageThemeByScope(pageScope);
			
			if(pTheme == null){
				//no page theme given, then use default value from theme objects
				theme.setCurrentPageTheme(theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT));
			}else{
				if(!PageTheme.SCOPE_DEFAULT.equals(pageScope)){
					//some special page theme are set, check if need do inherit value,then put them into current value
					pTheme.inheritValue(theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT));
				}
				theme.setCurrentPageTheme(pTheme);
			}
		}
		
		return theme;
	}
	//********************************************************************
	//               Other system service methods
	//********************************************************************

	/*
	 * Invoked by spring init-method setting
	 */
	public void init() throws IOException{
		//detect if explode skin has /skin/init_flag file. If it has, first package this skin, then copy it {DataRoot}/data/skins/ directory.
		
		//For unit test case, explode directory doesn't exist.
		if(skinExplosionRoot.exists()){
			File[] deployDirs = skinExplosionRoot.getFile().listFiles((FileFilter)DirectoryFileFilter.INSTANCE);
			for(File dir : deployDirs){
				File file = new File(dir,"init_flag");
				File skinFile = getSkinXML(dir);
				if(!file.exists() || !skinFile.exists()){
					continue;
				}
				//delete init_flag file, don't zip it! To avoid do this process again in same deployment.
				file.delete();
				
				File tgtFile = new File(skinResourcesRoot.getFile(), dir.getName()+ INSTALL_EXT_NAME);
				if(!tgtFile.exists()){
					try {
						Map<File, String> listToZip = new HashMap<File, String>();
						String rootDir = dir.getCanonicalPath();
						listToZip.put(dir, rootDir);
						ZipFileUtil.createZipFile(tgtFile.getCanonicalPath(), listToZip, false);
						
						//this ensure this skin will be deploy in next steps. As skin.xml not exist, the zip file will be exploded.
						skinFile.delete();
					} catch (ZipFileUtilException e) {
						log.error("Pack system initial skin failed" + tgtFile.getCanonicalFile(), e);
					}
				}
			}
		}
		//retrieve all zip files under themes and skins, compare its name and version, deploy it if it doesn't install or newer.
		File[] files = skinResourcesRoot.getFile().listFiles( (FileFilter)new SuffixFileFilter(INSTALL_EXT_NAME));
		for (File skinFile : files) {
			try {
				explodeSkin(skinFile, false);
			} catch (ThemeInvalidException e) {
				log.info("Skin skip deploy {}", e.getMessage());
			}
		}
		
		files = themeResourcesRoot.getFile().listFiles( (FileFilter)new SuffixFileFilter(INSTALL_EXT_NAME));
		for (File themeFile : files) {
			try {
				explodeTheme(themeFile, false);
			} catch (ThemeInvalidException e) {
				log.info("Theme skip deploy {}", e.getMessage());
			}
		}
	}
	//JDK1.6 @Override
	public void afterPropertiesSet() throws Exception {
		// Initial check theme directory, and create wiki and blog default theme if non-exist
	    if (themeResourcesRoot == null) {
            throw new BeanInitializationException("Must specify a theme resource root 'themeResourcesRoot' property");
        }
	    
	    
		//check if  root exists, if no, create one
        File locationDir = themeResourcesRoot.getFile();
        if (!locationDir.exists()){
        	if(!locationDir.mkdirs()){
        		throw new BeanInitializationException("Theme home dir can not created " + themeResourcesRoot);
        	}
        }
        if (themeExplosionRoot == null) {
        	throw new BeanInitializationException("Must specify a theme explostion target directory 'themeExplosionRoot' property");
        }

        //------------------------------ skin
        if (skinResourcesRoot == null) {
        	throw new BeanInitializationException("Must specify a skin resource root 'skinResourcesRoot' property");
        }
        
        //check if  root exists, if no, create one
        locationDir = skinResourcesRoot.getFile();
        if (!locationDir.exists()){
        	if(!locationDir.mkdirs()){
        		throw new BeanInitializationException("Skin home dir can not created " + skinResourcesRoot);
        	}
        }
        if (skinExplosionRoot == null) {
        	throw new BeanInitializationException("Must specify a skin explostion target directory 'skinExplosionRoot' property");
        }
        
        
        //don't check themeExplosionRoot, in zipped deploy mode, theme won't work, but just a warning?
	}

	public void uploadSystemLogo(FileNode logo) {
		try {
			ITicket ticket = repositoryService.login(RepositoryService.DEFAULT_SPACE_NAME, 
					RepositoryService.DEFAULT_SPACE_NAME, RepositoryService.DEFAULT_SPACE_NAME);
			
			if(repositoryService.hasIdentifierNode(ticket, RepositoryService.TYPE_INSTNACE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME)){
				List<FileNode> items = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_INSTNACE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME, false);
				if(items != null && items.size() > 0){
					//update: delete first
					for (FileNode fileNode : items) {
						repositoryService.removeFile(ticket, fileNode.getNodeUuid(), null);
					}
				}
			}else{
				//add
				repositoryService.createIdentifier(ticket, RepositoryService.TYPE_INSTNACE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			}
			
			logo.setIdentifier(WikiConstants.CONST_INSTANCE_RESOURCE_NAME);
			repositoryService.saveFile(ticket, logo, false, false);
			
		} catch (RepositoryException e) {
			log.error("Repository error " , e);
		} catch (RepositoryTiemoutExcetpion e) {
			log.error("Repository error " , e);		
		} catch (RepositoryQuotaException e) {
			log.error("Repository error " , e);		
		}
		
	}
	
	public String getSystemLogo(){
		try {
			
			ITicket ticket = repositoryService.login(RepositoryService.DEFAULT_SPACE_NAME, 
					RepositoryService.DEFAULT_SPACE_NAME, RepositoryService.DEFAULT_SPACE_NAME);
			List<FileNode> nodes = repositoryService.getAllIdentifierNodes(ticket, RepositoryService.TYPE_INSTNACE, WikiConstants.CONST_INSTANCE_RESOURCE_NAME, false);
			if(nodes != null && nodes.size() > 0){
				return nodes.get(0).getNodeUuid();
			}
		} catch (Exception e) {
			log.error("Failed get system logo",e);
		}
		
		return null;
	}

	@Override
	public void cleanThemeCache() {
		themeCache.removeAll();
	}
	//********************************************************************
	//               private method
	//********************************************************************
	/**
	 * Deploy skin to web root. If force is false, only deploy when the existing version is less than installation version.  
	 * 
	 * The skin is overwrite mode, i.e.,  copy default skin first, then overwrite same files from explode zip file.  
	 * 
	 * @throws ThemeInvalidException 
	 */
	private String  explodeSkin(File skinFile, boolean force) throws ThemeInvalidException {
		InputStream sis = null;
		InputStream is = null;
		try {
			String skinXML = getMetafile(skinFile,"skin.xml");
			sis = IOUtils.toInputStream(skinXML);
			Skin skin = Skin.fromXML(sis);
			String name = skin.getName();
			
			float ver = getExistingVersion("skin",name);
			if(force || (!force && skin.getVersion() > ver)){
				String skinPath = FileUtil.getFullPath(skinExplosionRoot.getFile().getCanonicalPath(), name);
				//first duplicated DEFAULT skin, then explode zip file to overwrite
				FileUtils.copyDirectory(
						new File(skinExplosionRoot.getFile().getCanonicalPath(), Skin.DEFAULT_SKIN), 
						new File(skinPath));
				
				//explode skin
				is = new FileInputStream(skinFile);
				ZipFileUtil.expandZipToFolder(is, skinPath);
				log.info("Skin {} is deployed successfully", name);
			}else{
				log.warn("Skin version is less or equals than existing {}", ver);
				throw new ThemeInvalidVersionException("Skin version is less or equals than existing " + ver,skin.getVersion(), ver);
			}
			
			return name;
		} catch (ThemeInvalidVersionException e) {
			throw e;
		} catch (Exception e) {
			log.error("Invalid skin zip file.",e);
			throw new ThemeInvalidException(e);
		} finally{
			IOUtils.closeQuietly(sis);
			IOUtils.closeQuietly(is);
		}
	}
	/**
	 * Deploy theme to web root. If force is false, only deploy when the existing version is less than installation version.
	 * @throws ThemeInvalidException 
	 */
	private String  explodeTheme(File themeFile, boolean force) throws ThemeInvalidException {
		//we must keep existing theme's runtime value: 
		//status(enable/disable) 
		
		InputStream sis = null;
		InputStream is = null;
		try {
			String themeXML = getMetafile(themeFile,"theme.xml");
			sis = IOUtils.toInputStream(themeXML);
			Theme theme = Theme.fromXML(sis);
			//Theme must has default scope pageTheme - this avoid NullPoint exception when theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT);
			PageTheme defaultPageTheme = theme.getPageThemeByScope(PageTheme.SCOPE_DEFAULT);
			if(defaultPageTheme == null){
				throw new ThemeInvalidException("Theme has at least one PageTheme with scope 'any' as default value." );
			}
			
			String name = theme.getName();
			
			float ver = getExistingVersion("theme",name);
			if(force || (!force && theme.getVersion() > ver)){
				
				//get existing theme runtime value
				Theme existTheme = null;
				File dir = new File(themeExplosionRoot.getFile(), name);
				if(dir.isDirectory()){
					File file = new File(dir, "theme.xml");
					if(file.exists()){
						InputStream eis = new FileInputStream(file);
						existTheme = Theme.fromXML(eis);
						IOUtils.closeQuietly(eis);
					}
				}
				
				//explode theme
				is = new FileInputStream(themeFile);
				ZipFileUtil.expandZipToFolder(is, FileUtil.getFullPath(themeExplosionRoot.getFile().getCanonicalPath(), name));
				
				if(existTheme != null){
					//update explode theme xml.
					theme.setStatus(existTheme.getStatus());
					theme.toFile(new File(dir, "theme.xml"));
				}
				log.info("Theme {} is deployed successfully", name);
			}else{
				log.warn("Theme version is less or equals than existing {}", ver);
				throw new ThemeInvalidVersionException("Theme version is less or equals than existing " + ver,theme.getVersion(), ver);
			}
			
			return name;
		} catch (ThemeInvalidVersionException e) {
			throw e;
		} catch (Exception e) {
			log.error("Invalid theme zip file.",e);
			throw new ThemeInvalidException(e);
		} finally{
			IOUtils.closeQuietly(sis);
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * @param name
	 * @return Theme or Skin version. Return -1 if there is no specified skin or theme existing.
	 */
	private float getExistingVersion(String type, String name) {
		try {
			if("theme".equals(type)){
				File file = new File(themeExplosionRoot.getFile(), name);
				file = new File(file,"theme.xml");
				if(file.exists()){
					FileInputStream is = new FileInputStream(file);
					Theme theme = Theme.fromXML(is);
					IOUtils.closeQuietly(is);
					
					return theme.getVersion();
				}
			}else{ //skin
				File file = new File(skinExplosionRoot.getFile(), name);
				file = new File(file,"skin.xml");
				if(file.exists()){
					FileInputStream is = new FileInputStream(file);
					Skin skin = Skin.fromXML(is);
					IOUtils.closeQuietly(is);
					
					return skin.getVersion();
				}
				
			}
		} catch (Exception e) {
			log.error("Get theme/skin existing version failed", e);
		}
		return -1;
	}
	private String getMetafile(File zipFile, String fileName){
		String content = "";
		ZipFile zip = null;
		try {
			zip = new ZipFile(zipFile);
			ZipEntry entry = zip.getEntry(fileName);
			if(entry != null){
				content = IOUtils.toString(zip.getInputStream(entry));
			}
		} catch (Exception e) {
			log.info("backup/restore file comment not available:" + zipFile.getAbsolutePath());
		} finally{
			if(zip != null)
				try {
					zip.close();
				} catch (Exception e) {}
		}
		
		return content;
	}
	/**
	 * 
	 * @param space
	 * @return space unixname constructed name or system default theme name if no specified.
	 */
	private String getThemeName(Space space) {
		String name;
		SpaceSetting set = space.getSetting();
		if(set.isCustomizedTheme() && set.getPageThemes() != null && set.getPageThemes().size() > 0){
			name = WikiConstants.CONST_NONSPACE_RESOURCE_PREFIX + space.getUnixName();
		}else{
			name = set.getTheme();
		}
		if(name == null){
			name = DEFUALT_THEME_NAME;
		}
		return name;
	}
	/**
	 * @param theme
	 * @return
	 * @throws ThemeInvalidException 
	 */
	private Theme getSystemDefaultTheme(String themeName) throws ThemeNotFoundException {
		Element ele  = themeCache.get(themeName);
		if(ele != null){
			return (Theme) ((Theme) ele.getObjectValue()).clone();
		}
		
		FileInputStream is = null; 
		try {
			if(StringUtils.isBlank(themeName)){
				themeName = DEFUALT_THEME_NAME;
			}
			File root = new File(themeExplosionRoot.getFile(),themeName);
			if(!root.exists() || !root.isDirectory()){
				log.error("Unable to locate system theme {} on directory {}", themeName,themeExplosionRoot.getFile().getAbsolutePath());
				throw new ThemeNotFoundException("Unable to locate system theme " + themeName + "on directory " +themeExplosionRoot.getFile().getAbsolutePath());
			}
			
			//if non-English, try find localize version.
			File file = getThemeXML(root);
			if(!file.exists()){
				log.error("Unable to locate system theme {} definition file", file.getAbsolutePath());
				throw new ThemeNotFoundException("Unable to locate system theme " + file.getAbsolutePath() + "definition file.");
			}
			
			//need read from file
			is = new FileInputStream(file);
			Theme theme = Theme.fromXML(is);
			if(theme != null){
				ele = new Element(themeName,theme);
				themeCache.put(ele);
			}
			return (Theme) theme.clone();
		} catch (IOException e) {
			log.error("Failed to parse theme configuration file " + themeName, e);
			throw new ThemeNotFoundException("Failed to parse theme configuration file " + themeName, e);
		} finally{
			IOUtils.closeQuietly(is);
		}
	}


	private File getThemeXML(File root){
		return getXML(root, THEME_NAME);
	}


	private File getSkinXML(File root) {
		return getXML(root, SKIN_NAME);
	}
	
	private File getXML(File root, String name) {
		File file = null;
		if(!Global.isLanguage(Locale.ENGLISH)){ //doesn't tell different English yet.
			//try to find locale file, such as theme_zh_CN.xml or theme_tr_TR.xml etc.
			file = new File(root, name+"_" + Global.DefaultLanguage.toLowerCase() + "_" + Global.DefaultCountry.toUpperCase() + SKIN_OR_THEME_EXT);
			
			//if not found, reset to null then try to find theme.xml
			if(!file.exists()) file = null;
		}
		if(file == null){
			file = new File(root, name+SKIN_OR_THEME_EXT);
		}
		return file;
	}

	//********************************************************************
	//               set / get method
	//********************************************************************

	public void setThemeResourcesRoot(Resource homeDirResource) {
		this.themeResourcesRoot = homeDirResource;
	}

	public void setThemeExplosionRoot(Resource themeExplosionRoot) {
		this.themeExplosionRoot = themeExplosionRoot;
	}

	public void setThemeCache(Cache themeCache) {
		this.themeCache = themeCache;
	}
	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setSkinResourcesRoot(Resource skinResourcesRoot) {
		this.skinResourcesRoot = skinResourcesRoot;
	}

	public void setSkinExplosionRoot(Resource skinExplosionRoot) {
		this.skinExplosionRoot = skinExplosionRoot;
	}


}
