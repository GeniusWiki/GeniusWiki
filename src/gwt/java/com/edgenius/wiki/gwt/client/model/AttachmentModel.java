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
package com.edgenius.wiki.gwt.client.model;

import com.edgenius.wiki.gwt.client.server.utils.StringUtil;



/**
 * @author Dapeng.Ni
 */
public class AttachmentModel extends GeneralModel {
	
	public String index="";
	public String nodeUuid="";
	public String version;
	//display element
	public String filename="";
	public String creator="";
	public long date;
	//bytes
	public long size;
	public String desc="";
	public boolean bulkZip = false;
	public int draftStatus;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// following fields only used in offline model
	public int offlineEdited;
	public String pageUuid;
	public String spaceUname;
	//determine if this attachment is uploading to server side and how many time it retried 
	public long submissionDate;
	public int submissionRetry;
	
	//identify it is draft attachment or page attachment, same with CrFileNode.status
	public int status;
	public boolean equals(Object obj){
		if(!(obj instanceof AttachmentModel))
			return false;
		
		return StringUtil.equalsIgnoreCase(((AttachmentModel)obj).nodeUuid,this.nodeUuid) 
		&& StringUtil.equalsIgnoreCase(((AttachmentModel)obj).version, this.version);
	}
	public int hashCode(){
		return (this.nodeUuid==null?0:this.nodeUuid.toUpperCase().hashCode())
		+ (this.version==null?0:this.version.toUpperCase().hashCode());
	}
	public AttachmentModel clone(){
		AttachmentModel model = new AttachmentModel();
		model.index = this.index;
		model.nodeUuid=this.nodeUuid;
		model.version=this.version;
		model.filename=this.filename;
		model.creator=this.creator;
		model.date=this.date;
		model.size=this.size;
		model.desc=this.desc;
		model.bulkZip =this.bulkZip;
		model.draftStatus = this.draftStatus;
		
		//offline only
		model.offlineEdited = this.offlineEdited;
		model.pageUuid = this.pageUuid;
		model.spaceUname = this.spaceUname;
		model.status = this.status;
		model.submissionDate = this.submissionDate;
		model.submissionRetry = this.submissionRetry;
		return model;
	}
	public String toString(){
		return "uuid=" + nodeUuid + ";ver=" + version + ";filename=" + filename 
		+ ";creator=" + creator + ";date=" +date+ "size=" + size + ";desc=" + desc + ";error=" + errorCode;
	}
}
