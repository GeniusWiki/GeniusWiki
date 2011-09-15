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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.RenderPiece;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.model.AbstractPage;
import com.edgenius.wiki.render.RenderContext;
import com.edgenius.wiki.render.object.ObjectHandler;
import com.edgenius.wiki.service.PageService;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class AttachmentHandler  implements ObjectHandler{
	private static final Logger log = LoggerFactory.getLogger(AttachmentHandler.class);
	
	private PageService pageService; 
	private MessageService messageService; 
	private List<FileNode> atts;
	private UserReadingService userReadingService;


	public List<RenderPiece> handle(RenderContext renderContext, Map<String,String> values) {
		List<RenderPiece> pieces = new ArrayList<RenderPiece>();
		
		List<FileNode> files = new ArrayList<FileNode>();
		if(atts != null){
			files = getFileList(values != null?values.get("filter"):null, renderContext.getPageVisibleAttachments());
		}
		
		List<String> urls = new ArrayList<String>();
		//try to find the image from repository
		for (FileNode node : files) {
			//found attachment
			urls.add(renderContext.buildDownloadURL(node.getFilename(),node.getNodeUuid(), true));
		}
		
		
		int id = renderContext.createIncremetalKey();
		StringBuffer attachBuf = new StringBuffer("<div aid=\"attachments\" id=\"attachment-")
			.append(id).append("\" class=\"").append(WikiConstants.mceNonEditable).append("\" ");
		if(values != null && values.size() > 0){
			attachBuf.append("wajax=\"").append(RichTagUtil.buildWajaxAttributeString(this.getClass().getName(),values)).append("\" ");
		}
		attachBuf.append(">");
		
		attachBuf.append("<table name=\"attachments\" class=\"macroAttachment\">");
		//TODO: i18n, header
		attachBuf.append("<tr><th>File</th><th>Author</th><th>Size</th><th>Date</th><th>Comment</th></tr>");
		int sum = urls.size();
		if(sum > 0){
			for (int idx = 0; idx < sum; idx++) {
				FileNode node = files.get(idx);
				attachBuf.append("<tr>");
				//filename
				attachBuf.append("<td class='column2'><a href=\"");
				attachBuf.append(urls.get(idx));
				attachBuf.append("\" title=\"Download file\">");
				attachBuf.append(node.getFilename());
				attachBuf.append("</a></td>");
				//author
				attachBuf.append("<td class='column2'>").append(StringEscapeUtils.escapeHtml(node.getCreateor())).append("</td>");
				//size
				attachBuf.append("<td class='column3'>").append(GwtUtils.convertHumanSize(node.getSize())).append("</td>");
				//date
				attachBuf.append("<td class='column4'>").append(DateUtil.toDisplayDate(WikiUtil.getUser(), new Date(node.getDate()),messageService)).append("</td>");
				//comment
				attachBuf.append("<td>").append(StringEscapeUtils.escapeHtml(node.getComment())).append("</td>");
				attachBuf.append("</tr>");
			}
		}else{
			//TODO: i18n
			attachBuf.append("<tr><td colspan='5'>").append("No matched files").append("</td></tr>");
		}
		
		attachBuf.append("</table></div>");
		pieces.add(new TextModel(attachBuf.toString()));
		return pieces;
		
	}

	/**
	 * @param string
	 * @return
	 */
	private List<FileNode> getFileList(String filterStr, String[] visibles) {
		List<String> filter = new ArrayList<String>();
		if(!StringUtils.isEmpty(filterStr)){
			String[] fs = filterStr.split(",");
			for (String str : fs) {
				if(!StringUtils.isEmpty(str))
					filter.add(str.trim());
			}
		}
		List<FileNode> matched = new ArrayList<FileNode>();
		for (FileNode node : atts) {
			if(visibles != null && visibles.length > 0){
				if(!StringUtil.containsIgnoreCase(visibles, node.getNodeUuid()))
					continue;
			}else{
				//normal render, for example, page view. Then only view non-draft
				if(node.getStatus() > 0)
					continue;
			}
			
			String name = node.getFilename();
			if(filter.size() > 0){
				for (String flt : filter) {
					if(FilenameUtils.wildcardMatch(name.toLowerCase(), flt.toLowerCase())){
						matched.add(node);
						break;
					}
				}
			}else{
				//no filter, then put all attachments
				matched.add(node);
			}
		}
		return matched;
	}

	public void renderEnd() {

	}

	public void renderStart(AbstractPage page) {
		if(page != null && page.getSpace() != null){
			String spaceUname = page.getSpace().getUnixName();
			String pageUuid = page.getPageUuid();
			//only when page not found(display create new page info) pageUuid show be null.
			if(pageUuid == null){
				log.error("File can not render with null pageUuid");
				return;
			}
			try {
				atts = pageService.getPageAttachment(spaceUname, pageUuid, false,true, WikiUtil.getUser(userReadingService));
			} catch (RepositoryException e) {
				log.error("render atttachment failed to get file attachment list");
				//create a empty att, then all image will display error message
				atts = new ArrayList<FileNode>();
			}
		}
	}
	public void init(ApplicationContext context) {
		pageService = (PageService) context.getBean(PageService.SERVICE_NAME);
		userReadingService = (UserReadingService) context.getBean(UserReadingService.SERVICE_NAME);
		messageService = (MessageService) context.getBean(MessageService.SERVICE_NAME);
	}

}
