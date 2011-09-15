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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.octo.captcha.service.image.ImageCaptchaService;
import com.opensymphony.xwork2.ActionSupport;

/**
 * @author Dapeng.Ni
 */
@SuppressWarnings("serial")
public class CaptchaImageCreateAction extends ActionSupport{
	private static final Logger log = LoggerFactory.getLogger(CaptchaImageCreateAction.class);
	private ImageCaptchaService captchaImageService;
	
	public String execute(){
		HttpServletRequest request = ServletActionContext.getRequest();
		
        // get the session id that will identify the generated captcha. 
        //the same id must be used to validate the response
        String captchaId = request.getSession().getId();
        
        BufferedImage challenge = captchaImageService.getImageChallengeForID(captchaId,request.getLocale());
        try {
	        ByteArrayOutputStream output = new ByteArrayOutputStream();
	        ImageIO.write(challenge, "png", output);

	        // flush it in the response
	        HttpServletResponse response = ServletActionContext.getResponse();
	        response.setHeader("Cache-Control", "no-store");
	        response.setHeader("Pragma", "no-cache");
	        response.setDateHeader("Expires", 0);
	        response.setContentType("image/png");
	        ServletOutputStream responseOutputStream =
	        response.getOutputStream();
	        responseOutputStream.write(output.toByteArray());
		} catch (IOException e) {
			log.error("IO Error " , e);
		}

        
        return null;
	}

	public void setCaptchaImageService(ImageCaptchaService jcaptchaService) {
		this.captchaImageService = jcaptchaService;
	}
}
