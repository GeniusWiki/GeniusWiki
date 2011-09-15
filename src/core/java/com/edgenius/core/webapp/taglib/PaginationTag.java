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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example JSP code for URL: "test.jsp?page=10":
 * <pre>
 * <code>
 *   &lt;%@ taglib uri="edgenius" prefix="page" %>
 *
 *   current Page: &lt;c:out value="${param.page}"/><br>
 *    &lt;page:page totalPage="${pageInfo.totalPage}" currentPage="${pageInfo.currentPage}" KEYWORD="${formObj.Keyword}" show="true"/>
 *  or
 *    &lt;page:page totalPage="30" currentPage="${param.page}" displayPage="11"/>
 * </code>
 * displayPage means how many number between first and last. such as displayPage is 3, but total page is 10, the pagination only show
 * <pre>first previous 1,2,3 next last</pre>
 * 
 * This Tag allow append any dynamic parameters in URL, such as  &lt;page:page parm1="value1" parm2="value2"/>
 * </pre>
 * Note, current page is start from 1
 * attribute "show" true, then the pagination is always show up, even it only one page
 * @author Dapeng Ni
 */
public class PaginationTag extends SimpleTagSupport implements DynamicAttributes{

	private static  Logger log = LoggerFactory.getLogger(PaginationTag.class);
	private static String txt_first_page ="First";
	private static String txt_last_page = "Last";
	private static String txt_previous_page = "Previous";
	private static String txt_next_page = "Next";
	
	private int totalPage;
	private int currentPage;
	private int displayPage = 11;
	private boolean show = false;
	private String url;
	
	private ArrayList keys = new ArrayList();
	private ArrayList values = new ArrayList();
	
	static{
		ResourceBundle bundle = null;
		try {
			bundle = ResourceBundle.getBundle("page");
		} catch (Exception e) {
			log.info("Page Tag can not find resource bundle file, use default value instead.");
		}
		if(bundle != null){
			txt_first_page =  bundle.getString("FIRST");
			txt_last_page =  bundle.getString("LAST");
			txt_previous_page =  bundle.getString("PREVIOUS");
			txt_next_page =  bundle.getString("NEXT");
		}
	}
	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.SimpleTag#doTag()
	 */
	public void doTag() throws JspException, IOException {
		
		StringBuffer sb = new StringBuffer();
		if(totalPage < 2 && !show){
			getJspContext().getOut().write(sb.toString());
			return;
		}
		StringBuffer urlsb = new StringBuffer();
		if(!StringUtils.isEmpty(txt_first_page)){
//			already in first page
			if(currentPage <= 1){
				sb.append(txt_first_page).append("&nbsp;");;
			}else{
				urlsb = new StringBuffer();
				if(!StringUtils.isEmpty(url))
					urlsb.append(url);
				urlsb.append("?page=1");
				appendDynamicParam(urlsb);
				sb.append("<a href=\"").append(urlsb.toString()).append("\">");
				sb.append(txt_first_page).append("</a>&nbsp;");
			}
		}
		if(!StringUtils.isEmpty(txt_previous_page)){
//			already in first page
			if(currentPage <= 1){
				sb.append(txt_previous_page).append("&nbsp;");;
			}else{
				urlsb = new StringBuffer();
				if(!StringUtils.isEmpty(url))
					urlsb.append(url);
				urlsb.append("?page=").append(currentPage-1);
				appendDynamicParam(urlsb);
				sb.append("<a href=\"").append(urlsb.toString()).append("\">");
				sb.append(txt_previous_page).append("</a>&nbsp;");
			}
		}
		
		//page list
		int right = displayPage / 2;
		int left = displayPage - right;
		//index from 0
		int start = currentPage - left;
		int end = currentPage + right;
		if(start < 0){
			start = 0;
			end = displayPage;
		}
		if(end > totalPage){
			end = totalPage;
			start = totalPage - displayPage < 0?0:totalPage - displayPage;
		}
		for(int idx = start;idx < end; idx++){
			if(idx+1==currentPage){
				sb.append(idx+1).append("&nbsp;");
				continue;
			}
			urlsb = new StringBuffer();
			if(!StringUtils.isEmpty(url))
				urlsb.append(url);
			urlsb.append("?page=").append(idx+1);
			appendDynamicParam(urlsb);
			sb.append("<a href=\"").append(urlsb.toString()).append("\">");
			sb.append(idx+1).append("</a>&nbsp;");
		}
			
		if(!StringUtils.isEmpty(txt_next_page)){
//			last page
			if(currentPage == totalPage){
				sb.append(txt_next_page).append("&nbsp;");
			}else{
				urlsb = new StringBuffer();
				if(!StringUtils.isEmpty(url))
					urlsb.append(url);
				urlsb.append("?page=").append(currentPage+1);
				appendDynamicParam(urlsb);
				sb.append("<a href=\"").append(urlsb.toString()).append("\">");
				sb.append(txt_next_page).append("</a>&nbsp;");
			}
		}
		if(!StringUtils.isEmpty(txt_last_page)){
//			last page
			if(currentPage == totalPage){
				sb.append(txt_last_page).append("&nbsp;");;
			}else{
				urlsb = new StringBuffer();
				if(!StringUtils.isEmpty(url))
					urlsb.append(url);
				urlsb.append("?page=").append(totalPage);
				appendDynamicParam(urlsb);
				sb.append("<a href=\"").append(urlsb.toString()).append("\">");
				sb.append(txt_last_page).append("</a>&nbsp;");
			}
		}
		getJspContext().getOut().write(sb.toString());
	}
	 
	/**
	 * @param sb
	 * @throws UnsupportedEncodingException
	 */
	private void appendDynamicParam(StringBuffer sb) throws UnsupportedEncodingException {
		  for (int idx = 0; idx < keys.size(); idx++) {
			sb.append("&");
			String key = (String) keys.get(idx);
			Object value = URLEncoder.encode((values.get(idx)==null?"":values.get(idx).toString()),"UTF-8");
			
			sb.append(key).append("=").append(value);
		}

	}

	/**
	 * @return Returns the currentPage.
	 */
	public int getCurrentPage() {
		return currentPage;
	}
	/**
	 * @param currentPage The currentPage to set.
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	/**
	 * @return Returns the displayPage.
	 */
	public int getDisplayPage() {
		return displayPage;
	}
	/**
	 * @param displayPage The displayPage to set.
	 */
	public void setDisplayPage(int displayPage) {
		this.displayPage = displayPage;
	}
	/**
	 * @return Returns the totalPage.
	 */
	public int getTotalPage() {
		return totalPage;
	}
	/**
	 * @param totalPage The totalPage to set.
	 */
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	/**
	 * @return Returns the url.
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url The url to set.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.jsp.tagext.DynamicAttributes#setDynamicAttribute(java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void setDynamicAttribute(String url, String localName, Object value) throws JspException {
		  keys.add( localName );
		  values.add( value );
	}

	/**
	 * @return the show
	 */
	public boolean isShow() {
		return show;
	}

	/**
	 * @param show the show to set
	 */
	public void setShow(boolean show) {
		this.show = show;
	}
}
