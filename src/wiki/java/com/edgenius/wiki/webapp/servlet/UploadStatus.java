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

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dapeng.Ni
 */
public class UploadStatus implements OutputStreamListener, Serializable {
	public static final String NAME = "uploadStatus";
	public static final int STATUS_UPLOADING = 1;
	
    private long totalSize = 0;
    private long bytesRead = 0;
    private int elapsedSecond = 0;
    private String filename;
    private int fileIndex;
    private int status;
    
	private long start = 0;
	private HttpServletRequest request;
	public UploadStatus(HttpServletRequest request) {
		this.request = request;
		//this may contain multiple files in one request, so it maybe summary size of multiple files.
		this.totalSize = request.getContentLength();
		this.start = System.currentTimeMillis();
		this.fileIndex = 0;
		status = STATUS_UPLOADING;
		request.getSession().setAttribute(UploadStatus.NAME, this);
	}
	/**
	 * Every file will invoke this start, so it could invoke multiple time.
	 */
	public void start(String filename) {
		this.filename = filename;
		++fileIndex;
	}

	public void bytesRead(int bytesRead) {
		this.bytesRead +=  bytesRead;
		elapsedSecond = (int) ((System.currentTimeMillis() - start) / 1000);
	}

	public void done() {
//		this.status = END_UPLOADING;
		request.getSession().removeAttribute(UploadStatus.NAME);
		
	}
    //********************************************************************
	//               Set / Get
	//********************************************************************
	public long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}
	public long getBytesRead() {
		return bytesRead;
	}
	public void setBytesRead(long bytesRead) {
		this.bytesRead = bytesRead;
	}
	public int getElapsedSecond() {
		return elapsedSecond;
	}
	public void setElapsedTime(int elapsedTime) {
		this.elapsedSecond = elapsedTime;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getFileIndex() {
		return fileIndex;
	}
	public void setFileIndex(int fileIndex) {
		this.fileIndex = fileIndex;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
