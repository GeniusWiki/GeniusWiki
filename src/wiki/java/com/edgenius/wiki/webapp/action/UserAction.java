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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.ImageProcessException;
import com.edgenius.core.util.ScaleImage;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.server.UserUtil;

@SuppressWarnings("serial")
public class UserAction extends BaseAction{
	
	private Integer userUid;
	private File file;
    private String fileContentType;
    private String fileFileName;
	/**
	 * Update portrait picture
	 * @return
	 */
	public String doPortrait(){
		if(userUid == null){
			try {
				getResponse().getWriter().write("failed");
			} catch (IOException e) {
				log.error("IO Error " , e);
			}
			return null;
		}
		
		//TODO: need to file is image checking.
		try{
			//So far, the file is temporary file, it has ".tmp" extension, so don't use it.
			File out = ScaleImage.scale(file,FileUtil.getFileExtension(fileFileName), WikiConstants.PORTRAIT_WIDTH);
			User user = userReadingService.getUser(userUid);
			FileNode att = new FileNode();
			att.setShared(false);
			att.setFile(new FileInputStream(out));
			att.setFilename(fileFileName);
			att.setContentType(fileContentType);
			att.setType(RepositoryService.TYPE_PORTRAIT);
			att.setIdentifier(user.getUsername());
			att.setCreateor(user.getFullname());
			att.setStatus(PageType.NONE_DRAFT.value());
			att.setSize(0);

			userService.uploadPortrait(user, att);
			getResponse().getWriter().write(UserUtil.getPortraitUrl(user.getPortrait()));
		} catch (ImageProcessException e) {
			//image handle error
			try {
				getResponse().getWriter().write(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR);
			} catch (IOException e1) {
				log.error("Unable to response" ,e);
			}
			return null;
		} catch (AuthenticationException e) {
			sendAjaxFormRedir(SharedConstants.FORM_RET_AUTH_EXP);
			return null;
		} catch (AccessDeniedException e) {
			sendAjaxFormRedir(SharedConstants.FORM_RET_ACCESS_DENIED_EXP);
			return null;
		} catch (Exception e) {
			log.error(e.toString(),e);
			//any error, maybe user upload incorrect format image
			try {
				getResponse().getWriter().write(SharedConstants.FORM_RET_HEADER+SharedConstants.FORM_RET_ERROR);
			} catch (IOException e1) {
				log.error("Unable to response" ,e);
			}
		}
		return null;
	}
	public String doList(){
		
		return SUCCESS;
	}
	
	//********************************************************************
	//               Set / Get
	//********************************************************************

	public void setFile(File portrait) {
		this.file = portrait;
	}

	public Integer getUserUid() {
		return userUid;
	}
	public void setUserUid(Integer userUid) {
		this.userUid = userUid;
	}
	public String getFileContentType() {
		return fileContentType;
	}
	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}
	public String getFileFileName() {
		return fileFileName;
	}
	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}	
}
