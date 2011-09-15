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
package com.edgenius.wiki.render.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.wiki.MenuItem;
import com.edgenius.wiki.SpaceSetting;
import com.edgenius.wiki.gwt.client.model.LinkModel;
import com.edgenius.wiki.gwt.client.model.MacroModel;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.impl.LinkRenderHelperImpl;
import com.edgenius.wiki.render.impl.RenderContextImpl;
import com.edgenius.wiki.render.macro.MenuItemMacro;
import com.edgenius.wiki.render.macro.MenuMacro;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.render.object.RenderHandlerException;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.service.SpaceService;

/**
 * Handler for both MenuMacro and MenuItemMacro.
 * @author Dapeng.Ni
 */
public class MenuMacroHandler implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(MenuMacroHandler.class);
	private SpaceSetting setting;
	private SpaceService spaceService;
	private PageService pageService;
	private String spaceUname;
	
	
	public List<RenderPiece> handle(RenderContext renderContext, Map<String, String> values) throws RenderHandlerException {
		String name = values.remove(NameConstants.MACRO);
		if(MenuMacro.NAME.equals(name)){
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// MenuMacro handler

			if(setting == null){
				return new ArrayList<RenderPiece>();
				//better to hide if any errors as menu may be show on all pages in space
				//throw new RenderHandlerException("Failed to retrieve the space menu list.");
			}
			if(setting.getMenuItems() == null || setting.getMenuItems().size() == 0){
				//render to blank
				return new ArrayList<RenderPiece>();
			}
			
			return buildMenu(renderContext, setting.getMenuItems());
		}else{
			//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
			// MenuItemMacro handler
			
			//get parent title with parent pageUUID 
			String parentTitle = values.get(NameConstants.PARENT);
			if(parentTitle != null){
				//menu item
				Page parent = null;
				try {
					parent = pageService.getCurrentPageByTitleWithoutSecurity(spaceUname, parentTitle, false);
					if(parent != null){
						values.put(NameConstants.PARENT_UUID,parent.getPageUuid());
					}
				} catch (SpaceNotFoundException e) {
				}
				if(parent == null){
					throw new RenderHandlerException("The page parent with title ["+parentTitle+"] does not exist.");
				}
			}
			
			//leave PageService.pageSave() to process.
			List<RenderPiece> pieces = new ArrayList<RenderPiece>();
			MacroModel model = new MacroModel();
			model.macroName = MenuItemMacro.NAME;
			//put all others to value object
			model.values = new HashMap<String, String>(values);
			pieces.add(model);
			
			return pieces;
		}
		
	}

	public static void main(String[] args) {
		RenderContextImpl context = new RenderContextImpl();
		LinkRenderHelperImpl linkRenderHelper = new LinkRenderHelperImpl();
		linkRenderHelper.initialize(context, null, null);
		
		context.setLinkRenderHelper(linkRenderHelper);
		MenuMacroHandler h = new MenuMacroHandler();
		Set<MenuItem> menuItems = new TreeSet<MenuItem>(new MenuItem.MenuItemComparator());
		MenuItem m1 = new MenuItem();
		m1.setTitle("m1");
		m1.setPageTitle("t1");
		m1.setPageUuid("pm1");
		
		MenuItem m2 = new MenuItem();
		m2.setTitle("m2");
		m2.setPageTitle("t2");
		m2.setPageUuid("pm2");
		
		MenuItem m21 = new MenuItem();
		m21.setTitle("m21");
		m21.setPageUuid("pm21");
		m21.setPageTitle("t21");
		m21.setParent("pm2");
		
		MenuItem m22 = new MenuItem();
		m22.setTitle("m22");
		m22.setPageUuid("pm22");
		m22.setPageTitle("t22");
		m22.setParent("pm2");
		
		MenuItem m221 = new MenuItem();
		m221.setTitle("m221");
		m221.setPageTitle("t221");
		m221.setPageUuid("pm221");
		m221.setParent("pm22");
		
		MenuItem m222 = new MenuItem();
		m222.setTitle("m222");
		m222.setPageTitle("t222");
		m222.setPageUuid("pm222");
		m222.setParent("pm22");
		
		MenuItem m3 = new MenuItem();
		m3.setTitle("m3");
		m3.setPageTitle("t2");
		m3.setPageUuid("pm3");
		
		menuItems.add(m1);
		menuItems.add(m2);
		menuItems.add(m21);
		menuItems.add(m22);
		menuItems.add(m221);
		menuItems.add(m222);
		menuItems.add(m3);
		
		System.out.println(h.buildMenu(context, menuItems));
	}
	private List<RenderPiece> buildMenu(RenderContext renderContext, Set<MenuItem> menuItems) {
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		pieces.add(new TextModel("<div class='macroMenu'><ul class='menubar'>"));

		for(MenuItem item: menuItems ){
			if(item.getParent() == null){
				//root
				pieces.add(new TextModel("<li class='item0'>"));
				
				pieces.add(buildLink(renderContext, item));

				buildSubMenu(true, item, menuItems, new HashSet<MenuItem>(), pieces, renderContext);
				pieces.add(new TextModel("</li>"));
			}
		}
		pieces.add(new TextModel("</ul></div>"));
		return pieces;
	}

	private boolean buildSubMenu(boolean firstLevel, MenuItem parent, Set<MenuItem> menuItems, Set<MenuItem> container, List<RenderPiece> pieces, RenderContext renderContext) {
		if(container.contains(parent)){
			log.warn("Recursive contained menu item, it may cause infinite looping. Stop process");
			return false;
		}
		boolean subMenu = false;
		for (MenuItem item : menuItems) {
			if(parent.getPageUuid() != null && parent.getPageUuid().equals(item.getParent())){
				//a valid submenu
				if(!subMenu){
					pieces.add(new TextModel("<ul class='menu "+(firstLevel?"first":"")+"'>"));
					subMenu = true;
				}
				//add it to container to avoid infinite looping
				container.add(item);
				pieces.add(new TextModel("<li class='itemi'>"));
				
				LinkModel link = buildLink(renderContext, item);
				pieces.add(link);
				int linkIndex = pieces.size();
				
				boolean hasSubMenu = buildSubMenu(false, item, menuItems, new HashSet<MenuItem>(), pieces, renderContext);
				if(hasSubMenu){
					link.setView(link.getView() + "<span class='subid'>" + SharedConstants.NEXT_LINK+"</span>");
				}
				pieces.add(new TextModel("</li>"));
			}
		}
		
		if(subMenu) pieces.add(new TextModel("</ul>"));
		
		return subMenu;
	}
	private LinkModel buildLink(RenderContext renderContext, MenuItem item) {
		LinkModel link = new LinkModel();
		link.setType(LinkModel.LINK_TO_VIEW_FLAG);
		link.setView(StringUtils.isEmpty(item.getTitle())?item.getPageTitle():item.getTitle());
		link.setLink(item.getPageTitle());
		link.setSpaceUname(spaceUname);
		link.setLinkTagStr(renderContext.buildURL(link));
		return link;
	}

	public void init(ApplicationContext context) {
		spaceService = (SpaceService) context.getBean(SpaceService.SERVICE_NAME);
		pageService =  (PageService) context.getBean(PageService.SERVICE_NAME);
	}

	public void renderEnd() {
	}

	public void renderStart(AbstractPage page) {
		if(page != null && page.getSpace() != null){
			spaceUname = page.getSpace().getUnixName();
			Space space = spaceService.getSpaceByUname(spaceUname);
			if(space != null){
				setting = space.getSetting();
			}
		}
	}


}
