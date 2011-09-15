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
package com.edgenius.core.webapp.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.edgenius.core.Constants;
import com.edgenius.core.Global;
import com.edgenius.core.util.WebUtil;

/**
 * @author Dapeng.Ni
 */
public class SkinPathTag extends TagSupport {
	private static final long serialVersionUID = 2562737322719057886L;

	@Override
	public int doStartTag() throws JspException {
		try {
			String skinId = Global.Skin;
			//TODO: if allow user level customized skin, 
			//first, UpgradeService needs to delete all UserSetting.skin default value (before ver2.4, it is "default")
			//Then, turn on below code
//			HttpServletRequest req = (HttpServletRequest) this.pageContext.getRequest();
//			String skinId;
//			String userkey = req.getRemoteUser();
//			if (StringUtils.isBlank(userkey)) {
//				skinId = Global.Skin;
//			} else {
//				ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.pageContext.getServletContext());
//				UserReadingService service = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
//				User user = service.getUserByName(userkey);
//				if(user != null){
//					UserSetting setting = user.getSetting();
//					skinId = setting.getSkin();
//					skinId = skinId==null? Global.Skin:skinId;
//				}else
//					skinId = Global.Skin;
//			}
			JspWriter writer = pageContext.getOut();
			writer.println(WebUtil.getWebConext() + Constants.SkinDir + "/" + skinId);
		} catch (IOException e) {
			throw new JspException("Failed on IOException on ThemePathTag");
		}
		return TagSupport.SKIP_BODY;
	}
}
