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
package com.edgenius.wiki.webapp.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.model.User;
import com.edgenius.core.repository.FileNode;
import com.edgenius.core.repository.ITicket;
import com.edgenius.core.repository.RepositoryException;
import com.edgenius.core.repository.RepositoryService;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.WebUtil;
import com.edgenius.core.webapp.BaseServlet;
import com.edgenius.wiki.Theme;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.security.service.SecurityService;
import com.edgenius.wiki.service.ExportService;
import com.edgenius.wiki.service.SpaceService;
import com.edgenius.wiki.service.ThemeService;
import com.edgenius.wiki.util.WikiUtil;
/**
 * This class support 8 kind parameters collection. One is for general download:
 * <br>
 * URL sample: /download?space=XX&uuid=XX&version=XX&download=true
 * 
 * <br>
 * URL sample: /download?space=XX&uuid=XX(nodeUuid)&file=XX(fileName)&version=XX(optional,default is latest)&download=true
 * 
 * <br>
 * Another is special for User Portrait image, require portraitUuid (FileNode UUID)
 * URL sample: /download?portrait=userPortraitUuid
 * 
 * <br>
 * Another is special for User Portrait image, but require user username
 * URL sample: /download?user=username
 * 
 * <br>
 * Another is special for Space Logo image, require logoUuid  (FileNode UUID)
 * URL sample: /download?space=spaceUnixName&logo=logoFileUuid
 * 
 * <br>
 * Small Logo, parameter name is "slogo"
 * URL sample: /download?space=spaceUnixName&slogo=logoFileUuid
 * 
 * <br>
 * Display instance logo
 * /download?instance=logo
 * 
 * <br>
 * Export - get file from ExportServiceImpl.targetDir and download - need delete this file after download!
 * /download?export=filename
 * 
 * For case 1, page attachment uses this url. case 2 use for user portrait download. Case 3 is useless now, because 
 * this URL does not works well if user change portrait, the image won't refresh because user name does not change and browser
 * won't refresh from server side anymore!
 *  
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class DownloadServlet extends BaseServlet {
	private static final Logger log = LoggerFactory.getLogger(DownloadServlet.class);

	private RepositoryService repoService;

	private SecurityService securityService;
	private UserReadingService userReadingService;
	private ThemeService themeService;
	private SpaceService spaceService;
	private ExportService exportService;
	private MessageService messageService;
	protected void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		initServiceBean();
		
		String instance = request.getParameter("instance");
		String logo = request.getParameter("logo");
		String slogo = request.getParameter("slogo");
		String portrait = request.getParameter("portrait");
		String username = request.getParameter("user");
		String export = request.getParameter("export");
		String spaceUname;
		String uuid;
		String version;
		
		if(!StringUtils.isBlank(export)){
			FileNode attachment = null;
			try {
				attachment = exportService.getExportFileNode(export);
				if(attachment != null)
					handleFileNode(response, attachment, true);
			}finally{
				if(attachment != null){
					//some trick, the return Identifier from exportService.getExportFileNode() is full file name with path info...
					File file = new File(attachment.getIdentifier());
					if(!file.delete())
						file.deleteOnExit();
				}
			}
			return;
		}else if(!StringUtils.isBlank(portrait)){
			//special for user portrait image download
			spaceUname = RepositoryService.DEFAULT_SPACE_NAME;
			uuid = portrait;
			version = null;
			if(uuid == null){
				//this user does not have customized portrait. replace system default one
				response.sendRedirect(WebUtil.getWebConext() +"static/images/"+SharedConstants.NO_PORTRAIT_IMG);
				return;
			}
		}else if(!StringUtils.isBlank(logo) || !StringUtils.isBlank(slogo)){
			spaceUname = request.getParameter("space");
			uuid = StringUtils.isBlank(logo)?slogo:logo;
			version = null;
			if(uuid == null){
				//this space does not has customized logo,get from theme
				Space space = spaceService.getSpaceByUname(spaceUname);
				Theme theme = themeService.getSpaceTheme(space);
				response.sendRedirect(StringUtils.isBlank(logo)?theme.getSmallLogoURL():theme.getLargeLogoURL());
				return;
			}
		}else if(!StringUtils.isBlank(username)){ 
			//special for user portrait image download
			spaceUname = RepositoryService.DEFAULT_SPACE_NAME;
			User user = userReadingService.getUserByName(username);
			uuid = user==null?null:user.getPortrait();
			version = null;
			if(uuid == null){
				//this user does not have customized portrait. replace system default one
				response.sendRedirect(WebUtil.getWebConext() +"static/images/"+SharedConstants.NO_PORTRAIT_IMG);
				return;
			}
		}else if(!StringUtils.isBlank(instance)){ 
			spaceUname = RepositoryService.DEFAULT_SPACE_NAME;
			version = null;
			uuid = themeService.getSystemLogo();
			if(uuid == null){
				//this user does not have customized portrait. replace system default one
				response.sendRedirect(WebUtil.getWebConext() +"static/images/"+SharedConstants.INSANCE_LOGO);
				return;
			}
		}else{
			//general download by node UUID and Version.
			spaceUname = request.getParameter("space");
			uuid = request.getParameter("uuid");
			version = request.getParameter("version");
		}

		boolean downloadFile = Boolean.parseBoolean(request.getParameter("download"));
		FileNode attachment = null;
		try {
			ITicket ticket = repoService.login(spaceUname, spaceUname, spaceUname);
			attachment = repoService.downloadFile(ticket, uuid, version, WikiUtil.getUser());
			if (attachment != null) {
				// do security check first
				User user = userReadingService.getUserByName(request.getRemoteUser());
				boolean readAllow = true;
				//default space attachment allow all user download!!!
				//only not default repository space, it need check attachment permission.
				if(!RepositoryService.DEFAULT_SPACE_NAME.equals(spaceUname)){
					if (Boolean.valueOf(attachment.isShared()).booleanValue()
							|| attachment.getType().equalsIgnoreCase(RepositoryService.TYPE_SPACE)) {
						// shared, only need check space level permission
						readAllow = securityService.isAllowSpaceReading(spaceUname, user);
					} else {
						// non-shared, need check page level permission
						readAllow = securityService.isAllowPageReading(spaceUname, attachment.getIdentifier(), user);
					}
				}
				if(readAllow){
					// process download
					handleFileNode(response, attachment, downloadFile);
				}else{
					response.getOutputStream().write(messageService.getMessage("no.perm.download").getBytes(Constants.UTF8));
				}
			}else{
				response.getOutputStream().write(messageService.getMessage("no.file.download").getBytes(Constants.UTF8));
			}
		} catch (RepositoryException e) {
			log.error("Failed get file from repository: " , e);
		}finally{
			if(attachment != null) attachment.closeStream();
		}

	}

	private void handleFileNode(HttpServletResponse response, FileNode attachment, boolean downloadFile)
			throws IOException {

		String mimeType = attachment.getContentType();
		if (mimeType == null) {
			mimeType = "application/octet-stream";
		}
		response.setContentType(mimeType);

		// Get the filename stored with the file
		String filename = attachment.getFilename();

		log.debug("Downloading file " + filename + " mime type " + mimeType);

		if (downloadFile) {
			log.debug("Sending as attachment");
			response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
		} else {
			log.debug("Sending as inline");
			response.setHeader("Content-Disposition", "inline;filename=\"" + filename + "\"");
		}
		//response.setHeader("Cache-control", "must-revalidate");
		if (filename != null) {
			response.addHeader("Content-Description", filename);
		}

		InputStream in = new BufferedInputStream(attachment.getFile());
		OutputStream out = response.getOutputStream();
		try {
			int count = 0;

			int len=0;
			byte[] ch = new byte[10240];
			while ((len = in.read(ch)) != -1) {
				out.write(ch,0,len);
				count +=len;
			}
			log.debug("Wrote out " + count + " bytes");
			response.setContentLength(count);
		} catch (IOException e) {
			log.error("Exception occured writing out file:" + attachment.getFilename(),e);
			throw e;
		} finally {
			try {
				if (in != null)
					in.close(); // very important
			} catch (IOException e) {
				log.error("Error Closing file. File already written out - no exception being thrown.", e);
			}
		}
	}

	private void initServiceBean() {
		ServletContext context = this.getServletContext();
		ApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
		userReadingService = (UserReadingService) ctx.getBean(UserReadingService.SERVICE_NAME);
		securityService = (SecurityService) ctx.getBean(SecurityService.SERVICE_NAME);
		repoService = (RepositoryService) ctx.getBean(RepositoryService.SERVICE_NAME);
		themeService = (ThemeService) ctx.getBean(ThemeService.SERVICE_NAME);;
		spaceService = (SpaceService) ctx.getBean(SpaceService.SERVICE_NAME);;
		exportService = (ExportService) ctx.getBean(ExportService.SERVICE_NAME);;
		messageService = (MessageService) ctx.getBean(MessageService.SERVICE_NAME);;
	}


}
