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
package com.edgenius.wiki.webapp.action;

import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.service.ExportService;
import com.edgenius.wiki.service.PageService;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class PrintAction extends BaseAction{
	
	//pageUuid
	private String i;
	//history or not
	private boolean h;
	//if history, this is version number
	private int v;
	private PageService pageService;
	private ExportService exportService;
	private AbstractPage page;
	
	public String execute(){
		if(h){
			//get current page by current PageUid, then get history by version number
			page = pageService.getCurrentPageByUuid(i);
			page = pageService.getHistoryByVersion(page.getPageUuid(), v);
		}else
			page = pageService.getCurrentPageByUuid(i);
		if(page == null)
			return ERROR;
		
		getRequest().setAttribute("content",exportService.exportPageHTML(RenderContext.RENDER_TARGET_PLAIN_VIEW, page));
		return SUCCESS;
	}
	
	//********************************************************************
	//               set / get
	//********************************************************************

	public PageService getPageService() {
		return pageService;
	}

	public void setPageService(PageService pageService) {
		this.pageService = pageService;
	}

	public void setExportService(ExportService exportService) {
		this.exportService = exportService;
	}

	public void setPage(AbstractPage page) {
		this.page = page;
	}

	public String getI() {
		return i;
	}

	public void setI(String u) {
		this.i = u;
	}

	public void setH(boolean h) {
		this.h = h;
	}

	public boolean isH() {
		return h;
	}

	public AbstractPage getPage() {
		return page;
	}

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

}
