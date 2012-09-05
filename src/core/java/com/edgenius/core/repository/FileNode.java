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
package com.edgenius.core.repository;

import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.edgenius.core.Constants;
import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.model.User;
import com.edgenius.core.service.MessageService;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.core.util.DateUtil;
import com.edgenius.core.util.WebUtil;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gson.Gson;


public class FileNode  implements Serializable{
	private static final long serialVersionUID = 4849308949298514211L;
	
	public final static String  NAMESPACE = "http://www.geniuswiki.com/geniuswiki";
	public static final String  PREFIX = "geniuswiki";
	public final static String  ATTR_NAME_COMMENT = PREFIX + ":" + "comment";
	public final static String  ATTR_NAME_CONTENTTYPE = PREFIX + ":" + "contentType";
	public final static String  ATTR_NAME_STATUS = PREFIX + ":" + "status";
	public final static String  ATTR_NAME_CREATOR = PREFIX + ":" + "creator";
	public final static String  ATTR_NAME_SHARED = PREFIX + ":" + "shared";
	public final static String  ATTR_NAME_TYPE = PREFIX + ":" + "type";
	public final static String  ATTR_NAME_FILENAME = PREFIX + ":" + "filename";
	public final static String  ATTR_NAME_SIZE = PREFIX + ":" + "size";
	public final static String  ATTR_NAME_DATE = PREFIX + ":" + "date";

	
	private String filename;
	
	private String contentType;
	
	private String nodeUuid;

	private String comment;
	
	private boolean shared;
	//it is useful for lucence search, but no idea how to extract this value from MS WORD, EXCEL or PDF. Left it just for future use.
	private String encoding;
	
	private long size;

//	version.getName()
	private String version;
	
	//page or user 's attachment
	private transient String type;
	//for browser display index
	private String index;
	//pageUuid or username etc.
	private String identifier;
	
	//is zip format for bulk uploading?
	private boolean bulkZip;
	//some transient fields which won't pass back to 
	private transient InputStream file;
	
	//current, auto draft, manual draft
	private transient int status;
	//username, it won't display on page. userFullname is good.
	private String createor;
	private long date;

	//********************************************************************
	//for display use
	private String userFullname;

    private String errorCode;
    
	//these used by upload.jsp javascript templ for display purpose
    public String displayDate;
    public String url;
    public String deleteUrl;
    private String error;
	//********************************************************************
	//               Function methods
	//********************************************************************
	
	public String toString(){
		return "Filename:" + filename + ";nodeUuid:" + nodeUuid + ";identifier"+ identifier;
	}
	
    public void closeStream() {
        if(file != null){
            try {
                file.close();
            } catch (Exception e) {
            }
        }
        
    }

	public static void copyNodeToPersist(FileNode my, CrFileNode filenode) {
		filenode.setNodeType(my.getType());
		filenode.setIdentifierUuid(my.getIdentifier());
		filenode.setNodeUuid(my.getNodeUuid());
		
		filenode.setFilename(my.getFilename());
		filenode.setComment(my.getComment());
//		filenode.setVersion(my.getVersion());
		//does not include spaceUname
		
		filenode.setEncoding(my.getEncoding());
		filenode.setContentType(my.getContentType());
		filenode.setShared(my.isShared());
		filenode.setSize(my.getSize());
		filenode.setStatus(my.getStatus());
		
		filenode.setCreator(my.getCreateor());
		filenode.setModifiedDate(new Date());
	}
	
	public static FileNode copyPersistToNode(CrFileNode fileNode) {
		//does not include spaceUname
		
		FileNode my = new FileNode();
		my.setType(fileNode.getNodeType());
		my.setIdentifier(fileNode.getIdentifierUuid());
		my.setNodeUuid(fileNode.getNodeUuid());
		
		my.setFilename(fileNode.getFilename());
		my.setComment(fileNode.getComment());
		
		my.setEncoding(fileNode.getEncoding());
		my.setContentType(fileNode.getContentType());
		my.setStatus(fileNode.getStatus());
		my.setCreateor(fileNode.getCreator());
		my.setShared(fileNode.isShared());
		my.setDate(fileNode.getModifiedDate().getTime());
		my.setSize(fileNode.getSize());
		my.setVersion(Integer.valueOf(fileNode.getVersion()).toString());
		return my;
	}
	
    /**
     * Warning: this method will modify FileNode in parameter list directly. 
     * 
     * Construct display information for upload.jsp javascript display purpose.
     * @param files
     * @param spaceUname
     * @param messageService
     * @param userReadingService 
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String toAttachmentsJson(List<FileNode> files, String spaceUname, MessageService messageService, UserReadingService userReadingService) throws UnsupportedEncodingException {
        // convert fileNode to json that for JS template in upload.jsp.
        Gson gson = new Gson();

        for (FileNode fileNode : files) {
            if(StringUtils.isEmpty(fileNode.getFilename())){
                //This could be an error node, skip further message processing/
                continue;
            }
            fileNode.displayDate = DateUtil.toDisplayDate(WikiUtil.getUser(), new Date(fileNode.getDate()),messageService);
            fileNode.url = WebUtil.getPageRepoFileUrl(WebUtil.getHostAppURL(),spaceUname, fileNode.getFilename(), fileNode.getNodeUuid(), true);
            fileNode.deleteUrl = WebUtil.getHostAppURL() + "pages/pages!deleteAttachment.do?s=" + URLEncoder.encode(spaceUname, Constants.UTF8) 
                    + "&u=" + URLEncoder.encode(fileNode.getIdentifier(), Constants.UTF8)
                    + "&nodeUuid=" + URLEncoder.encode(fileNode.getNodeUuid(), Constants.UTF8);
            
            //pass back user fullname
            User user = userReadingService.getUserByName(fileNode.createor);
            fileNode.setUserFullname(user.getFullname());
     
        }
        return gson.toJson(files);
    }

	
//	/**
//	 * @param child
//	 * @param withHistory 
//	 * @throws RepositoryException 
//	 */
//	public static void copy(Node fileNode, List<FileNode> filelist, boolean withHistory, boolean withResource) throws RepositoryException {
//		
//		//node and all history have same uuid, so get it here.
//		String uuid = fileNode.getUUID();
//		if(!withHistory){
//			//just copy baseverion info
//			filelist.add(copy(uuid,fileNode.getBaseVersion(),fileNode,withResource));
//			return;
//		}
//	
//		VersionHistory history = fileNode.getVersionHistory();
//		VersionIterator verIter = history.getAllVersions();
//		//skip jcr:rootVersion
//		verIter.skip(1);
//		while(verIter.hasNext()){
//            Version ver = (Version) verIter.next();
//            NodeIterator nodeIter = ver.getNodes();
//            while(nodeIter.hasNext()){
//            	Node child = (Node) nodeIter.next();
//            	filelist.add(copy(uuid,ver, child,withResource));
//            }
//         }
//	}
//	
//	private static FileNode copy(String uuid, Version ver, Node node, boolean withResource) throws RepositoryException {
//		FileNode my = new FileNode();
//		my.nodeUuid = uuid;
//		my.filename = node.getProperty(ATTR_NAME_FILENAME).getString();
//		my.comment =  node.getProperty(ATTR_NAME_COMMENT).getString();
//		my.contentType =  node.getProperty(ATTR_NAME_CONTENTTYPE).getString();
//		my.status =  (int) node.getProperty(ATTR_NAME_STATUS).getLong();
//		my.createor =  node.getProperty(ATTR_NAME_CREATOR).getString();
//		my.shared = node.getProperty(ATTR_NAME_SHARED).getBoolean();
//		my.type = node.getProperty(ATTR_NAME_TYPE).getString();
//		my.date= node.getProperty(ATTR_NAME_DATE).getDate().getTime();
//		my.size = node.getProperty(ATTR_NAME_SIZE).getLong();
//		my.version = ver.getName();
//
//		//copy geniuswiki:filenode->nt:resource node as well
//		if(withResource){
//			Node resNode = node.getNode("jcr:content");
//			Property data = resNode.getProperty("jcr:data");
//			my.file = data.getStream();
//		}
//		return my;
//	}
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public String getComment() {
		
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public InputStream getFile() {
		return file;
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getNodeUuid() {
		return nodeUuid;
	}

	public void setNodeUuid(String nodeUuid) {
		this.nodeUuid = nodeUuid;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCreateor() {
		return createor;
	}

	public void setCreateor(String createor) {
		this.createor = createor;
	}


	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}


	public String getUserFullname() {
		return userFullname;
	}

	public void setUserFullname(String userFullname) {
		this.userFullname = userFullname;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isBulkZip() {
		return bulkZip;
	}

	public void setBulkZip(boolean bulkZip) {
		this.bulkZip = bulkZip;
	}


    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }


}
