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

import java.io.File;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
 * @author Dapeng.Ni
 */
public class MonitoredDiskFileItemFactory extends DiskFileItemFactory {
	private OutputStreamListener listener = null;

	public MonitoredDiskFileItemFactory(OutputStreamListener listener) {
		super();
		this.listener = listener;
	}

	public MonitoredDiskFileItemFactory(int sizeThreshold, File repository, OutputStreamListener listener) {
		super(sizeThreshold, repository);
		this.listener = listener;
	}

	public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
		//hardcode: skip other fields except FILE type in upload form
		if(!fieldName.startsWith("file"))
			return super.createItem(fieldName, contentType, isFormField, fileName);
		else
			return new MonitoredDiskFileItem(fieldName, contentType, isFormField, fileName, getSizeThreshold(),
					getRepository(), listener);
	}

}
