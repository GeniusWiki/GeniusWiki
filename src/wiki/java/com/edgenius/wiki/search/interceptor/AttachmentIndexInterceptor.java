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
package com.edgenius.wiki.search.interceptor;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Global;
import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.Shell;
import com.edgenius.wiki.service.PageService;

/**
 * @author Dapeng.Ni
 */
public class AttachmentIndexInterceptor  extends IndexInterceptor {
	
	
	@SuppressWarnings("unchecked")
	public void afterReturning(Object retValue, Method method, Object[] args, Object target) throws Throwable {
		if(StringUtils.equals(method.getName(), PageService.uploadAttachments)){
			String  spaceUname= (String) args[0];
			String  pageUuid= (String) args[1];
			List<FileNode> ret = (List<FileNode>) retValue;
			if(ret != null && ret.size() > 0){
				for(FileNode attachment: ret){
					log.info("JMS message send for Attachment index creating. File name: " + attachment.getFilename());
					IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_ATTACHMENT,new Object[]{spaceUname,attachment});
					jmsTemplate.convertAndSend(queue, mqObj);
				}
				if(Shell.enabled && Global.restServiceEnabled && pageUuid != null){
					log.info("JMS message send for Attachment creating shell service.");
					IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_INSERT_ATTACHMENT_BATCH,pageUuid);
					jmsTemplate.convertAndSend(queue, mqObj);
				}
			}
		}else if(StringUtils.equals(method.getName(), PageService.removeAttachment)){
			String pageUuid = (String) args[1];
			String nodeUuid = (String) args[2];
			String version = (String) args[3];
			log.info("JMS message send for Attachment index remove. NodeUUID: " + nodeUuid + ". Version:" + version);
			IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_REMOVE_ATTACHMENT,new String[]{pageUuid, nodeUuid,version});
			jmsTemplate.convertAndSend(queue, mqObj);
		}else if(StringUtils.equals(method.getName(), PageService.updateAttachmentMetaData)){
			FileNode ret = (FileNode) retValue;
			String spaceUname= (String) args[0];
			String pageUuid = (String) args[1];
			//only update name and comment of attachment, the attachment file body (extract content) won't be updated.
			if(ret != null){
				IndexMQObject mqObj = new IndexMQObject(IndexMQObject.TYPE_UPDATE_ATTACHMENT,new Object[]{spaceUname,pageUuid, ret});
				jmsTemplate.convertAndSend(queue, mqObj);
			}
		}
	}

}
