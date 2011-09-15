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
package com.edgenius.wiki.gwt.client.render;


/**
 * !!! This interface is just intent to extend by PageRender. So far, there is no any possible scenario to 
 * let external classes implement it. !!! 
 * @author Dapeng.Ni
 */
public interface RenderWidgetListener {

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Below method will be invoked from RenderWidget - renderStarted() from PageRender;
	
	/**
	 * This method is call before any ajax begin loading and before renderStarted() method. This method 
	 * is treated as a register to tell how many RenderWidget is inside current PageRender so that Render 
	 * can decide when the render finally completed (all ajax call completed). 
	 * Aka, onSuccessLoad() + onFailedLoad() = onLoading(), means the render completed. 
	 * 
	 * !!! MUST VERY CAREFULLY - if a renderWidget call this method, then it must implement onSuccessLoad() and 
	 * onFailedLoader() to deduct - otherwise, RenderContentListener.renderEnd() CANNOT be invoked. 
	 * 
	 * @param component
	 */
	public void onLoading(String componentKey);
	
	
	public void onSuccessLoad(String componentKey, String content);
	public void onFailedLoad(String componentKey, String errorMsg);
	
}
