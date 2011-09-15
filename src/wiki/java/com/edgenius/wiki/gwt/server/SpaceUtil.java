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
package com.edgenius.wiki.gwt.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.SpaceModel;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.WikiSecurityValues.WikiOPERATIONS;
import com.edgenius.wiki.service.ThemeService;

/**
 * @author Dapeng.Ni
 */
public class SpaceUtil {
	private static final Logger log = LoggerFactory.getLogger(SpaceUtil.class);
	
	public static void copySpaceToModel(Space space,SpaceModel value, User viewer, ThemeService themeService){
		value.name=space.getName();
		value.unixName=space.getUnixName();
		value.description = space.getDescription();
		value.isRemoved = space.isRemoved();
		value.type = space.getType();
		value.createdDate = DateUtil.getLocalDate(viewer, space.getCreatedDate());

		value.isShellEnabled = Shell.enabled && !space.isPrivate() && !space.containExtLinkType(Space.EXT_LINK_SHELL_DISABLED);
		//Shell.getThemeBaseURL() method request shell key if system doesn't have, it may trigger large amount shell requests if multiple portlets from dashbaord.
		//So far, this method ShellModel.shellThemeBaseURL field only filled when space creating.
//		if(value.isShellEnabled){
//			space.isShellAutoEnabled = Shell.autoEnabled;
//			value.shellThemeBaseURL = Shell.getThemeBaseURL();
//		}
		value.smallLogoUrl = getSpaceLogoUrl(space, themeService, space.getLogoSmall(), false);
		value.largeLogoUrl = getSpaceLogoUrl(space, themeService, space.getLogoLarge(), true);
		//size of possible space permission
		value.permissions = new int[10];
		value.tags = space.getTagString();
	
		List<WikiOPERATIONS> perms = space.getWikiOperations();
		if(perms != null){
			for (WikiOPERATIONS perm : perms) {
				value.permissions[perm.operation.ordinal()] = 1;
			}
		}
		
	}
	/**
	 * For performance and security consideration, copy linkBlogMeta value(with password) is separated in this method.
	 * @param space
	 * @param value
	 * @return 
	 */
	public static Collection<BlogMeta> getSpaceLinkMetaToModel(Space space){
		
		if(space.getSetting() != null){
			Collection<BlogMeta> linkBlogMetas = space.getSetting().getLinkedMetas();
			if(linkBlogMetas != null && linkBlogMetas.size() > 0){
				Collection<BlogMeta> plainBlogs = new ArrayList<BlogMeta>(); 
				for (BlogMeta blog : linkBlogMetas) {
					BlogMeta meta = (BlogMeta) blog.clone();
					try {
						meta.setPassword(space.getSetting().restorePlainPassword(meta.getPassword()));
					} catch (Exception e) {
						log.error("Restore space blog password failed:" + space.getUnixName(),e);
					}
					plainBlogs.add(meta);
				}
				return plainBlogs;
			}			
		}
		return null;
	}
	/**
	 * @param largeLogoUuid
	 * @return
	 */
	public static String getSpaceLogoUrl(Space space, ThemeService themeService, String logoUuid, boolean large) {
		String imgUrl;
		if (logoUuid == null || logoUuid.trim().length() == 0) {
			// return default user portrait
			Theme theme = themeService.getSpaceTheme(space);
			if(large)
				imgUrl = theme.getLargeLogoURL();
			else
				imgUrl = theme.getSmallLogoURL();
		} else {
			String spaceUname  = space.getUnixName();
			try {
				spaceUname = URLEncoder.encode(spaceUname,Constants.UTF8);
			} catch (UnsupportedEncodingException e) {
			}
			if(large)
				imgUrl = WebUtil.getWebConext() + "download?space="+ spaceUname +"&logo=" + logoUuid;
			else
				imgUrl = WebUtil.getWebConext() + "download?space="+ spaceUname +"&slogo=" + logoUuid;
		}

		return imgUrl;
		
	}
	/**
	 * @param space
	 * @param space2
	 */
	public static void copyModelToSpace(SpaceModel model, Space space) {
		space.setName(model.name);
		space.setUnixName(model.unixName);
		SpaceSetting set = space.getSetting();
		
		set.setTheme(model.themeName);
		//MUST put it back, because getSetting() may return a new SpaceSetting instance which is not reference to space.
		space.setSetting(set);
		
		space.setDescription(model.description);
		space.setType((short) model.type);
		space.setTagString(model.tags);
		
		if(model.linkBlogMetas != null && model.linkBlogMetas.size() != 0){
			//new blog type
			space.addExtLinkType(Space.EXT_LINK_BLOG);
		}else{
			space.removeExtLinkType(Space.EXT_LINK_BLOG);
		}
	}
}
