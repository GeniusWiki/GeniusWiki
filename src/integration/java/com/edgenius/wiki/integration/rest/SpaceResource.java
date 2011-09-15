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
package com.edgenius.wiki.integration.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.edgenius.wiki.Shell;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.integration.rest.model.SpaceBean;
import com.edgenius.wiki.integration.rest.model.TagBean;
import com.edgenius.wiki.integration.rest.model.mapper.SpaceMapper;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.model.SpaceTag;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.RenderService;
import com.edgenius.wiki.service.SpaceService;
import com.google.gson.Gson;

/**
 * Get:
 * /space?uname=SpaceUname
 * 
 * @author Dapeng.Ni
 */
@Path("/space")
@Component
@Scope("singleton")
public class SpaceResource {
	private @Autowired SpaceService spaceService;
	private @Autowired RenderService renderService;

	@GET
	@Produces("application/json")
	public String getSpace(@QueryParam("uname") String spaceUname, @QueryParam("myurl") String myURL) {

		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space != null){
			SpaceBean bean = SpaceMapper.spaceToBean(space);
			//fill tags
			List<SpaceTag> tags = space.getTags();
			if(tags != null){
				List<TagBean> tagBeans = new ArrayList<TagBean>();
				for (SpaceTag spaceTag : tags) {
					
					TagBean tag = new TagBean();
					tag.setName(spaceTag.getName());
					tag.setType(TagBean.TYPE_SPACE);
					tagBeans.add(tag);
				}
				
				bean.setTags(tagBeans);
			}
			
			//fill space menu
			if(space.getSetting().getMenuItems() != null && space.getSetting().getMenuItems().size() > 0){
				//URL has 3 level, input parameter > Shell.url if shell enabled > WebUtil.getHostApp() i.e., current host URL.
				if(myURL == null && Shell.enabled){
					//try shell url if it enabled
					myURL = Shell.url;
				}
				Page dummyPage = new Page();
				dummyPage.setSpace(space);
				PageContent content = new PageContent();
				content.setContent("{menu}");
				dummyPage.setContent(content);
				
				List<RenderPiece> pieces = renderService.renderHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, myURL, dummyPage);
				bean.setMenuRender(renderService.renderNativeHTML(spaceUname, "dummy-page", pieces));
			}			

			Gson gson = new Gson();
			String json = gson.toJson(bean);
			return json;
		}
		
		return "";
	}
}
