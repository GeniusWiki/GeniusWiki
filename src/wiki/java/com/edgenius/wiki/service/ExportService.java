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
package com.edgenius.wiki.service;

import java.io.File;

import com.edgenius.core.repository.FileNode;
import com.edgenius.wiki.model.AbstractPage;

/**
 * @author Dapeng.Ni
 */
public interface ExportService {
	String SERVICE_NAME = "exportService";
	public static final String EXPORT_HTML_SUB_DIR = "export_files";
	//********************************************************************
	//               export service ?? not implementation, maybe remove to another service bean
	//********************************************************************
	File exportPDF(String spaceUname,String pageTitle) throws ExportException;
	File exportHTML(String spaceUname,String pageTitle) throws ExportException;
	/**
	 * @param page
	 * @return
	 */
	String exportPageHTML(String hostUrl, AbstractPage page);
	/**
	 * @param export
	 * @return
	 */
	FileNode getExportFileNode(String filename);
}
