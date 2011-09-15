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

import java.util.List;

import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.model.User;

/**
 * Level structure in repository:<br>
 * <br>
 * Repository Root -> Space(an UUID for CrWorkspace) ->  Node Type(Page, Space or User) 
 * -> IdentifierUUID (PageUuid or Username) -> NodeUUID -> 
 * ->1 (version number) -> physical file
 *   2 (version number) -> physical file
 *  
 * @author Dapeng.Ni
 */
public interface RepositoryService {
	String SERVICE_NAME = "repositoryService";
	//saving page attachment
	String TYPE_ATTACHMENT = "page";
	//only for default system space, saving user portrait 
	String TYPE_PORTRAIT = "user";
	//for each space, saving space logo
	String TYPE_SPACE = "space";
	//for each space, saving instance logo
	String TYPE_INSTNACE = "instance";
	
	//default space attachment allow all user download!!!
	String DEFAULT_SPACE_NAME = "_geniuswiki_";
//	This is Jackrabbit default space name
//	String DEFAULT_SPACE_NAME = "default";

	/**
	 * @param spacename
	 * @param username
	 * @param password
	 * @return
	 */
	ITicket login(String spacename, String username, String password) throws RepositoryException;
	/**
	 * 
	 * @param spacename
	 * @param username
	 * @param password
	 * @throws RepositoryException
	 */
	void createWorkspace(String spacename, String username, String password) throws RepositoryException, RepositoryTiemoutExcetpion;

	/**
	 * This method update FileNode.setNodeUuid() and setVersion() as well;
	 * 
	 * @param stream
	 * @param md5DigestRequired  - true, calculated the MD5 digest. 
	 * @param discardSaveDiffMd5  - only effective when md5DigestRequired is true. If false, always save, otherwise, new item won't be saved if MD5 digest is samex.
	 * @return Checked-in file node.  
	 * Normally, it is same with in-parameter <code>attachment</code>, except it is bulk check-in.
	 * Return null if md5DigestRequired and discardSaveDiffMd5 is true and digests are equals. This means new uploading is discarded.
	 * It will bring node UUID, Version, check-in date etc. information back from repository.
	 */
	List<FileNode> saveFile(ITicket ticket, FileNode attachment, boolean md5DigestRequired, boolean discardSaveDiffMd5) throws RepositoryException , RepositoryTiemoutExcetpion, RepositoryQuotaException;
	/**
	 * 
	 * @param ticket
	 * @param spacename
	 * @return [0] usage byte, [1] space quota.
	 */
	long[] getSpaceQuoteUsage(ITicket ticket, String spacename);
	/**
	 * 
	 * Update wikbok:file node metadata except jcr:contenct node.<br>
	 * This method won't increaes version number of this node and also won't duplicate attached file. The limitation is 
	 * it only can update latest node. No way to update node history version. <br>
	 * Note, FileNode parameter must contain nodeUuid and version information to assign which node will be updated.
	 * @param ticket
	 * @param attachment
	 * @throws RepositoryException
	 */
	void updateMetaData(ITicket ticket, FileNode attachment) throws RepositoryException;
	
	/**
	 * Update wikbok:file node metadata except jcr:contenct node.<br>
	 * This method won't increaes version number of this node and also won't duplicate attached file. The limitation is 
	 * it only can update latest node. No way to update node history version. <br>
	 * NOTE: this method only update base node but left history unchanged.
	 * @param ticket
	 * @param attachment
	 * @throws RepositoryException
	 */
	FileNode updateMetaData(ITicket ticket, String nodeUuid, String name, String desc) throws RepositoryException;
	
	/**
	 * Remove all history if version equals 0 or null.
	 * @param ticket
	 * @param attachmentNodeUuid
	 * @param version
	 * @return removed file node
	 */
	FileNode removeFile(ITicket ticket, String nodeUuid, String version)  throws RepositoryException , RepositoryTiemoutExcetpion;

	/**
	 * This method will fill necessary fields in FileNode for download use(filename, contentType and data)<Br>
	 * If version is 0("0", or null or empty), download current version.
	 * 
	 * @param ticket
	 * @param uuid
	 * @param version 
	 * @param downloader the user is doing download. This is for checking permission if file's status is not 0, i.e.,  status 
	 * maybe auto or manual draft. If downloader is null, then skip this check - means whatever the file belonging, it always
	 * is downloaded. It is useful in system export service. 
	 *   
	 * @return
	 * @throws RepositoryException
	 */
	FileNode downloadFile(ITicket ticket, String uuid, String version , User downloader)throws RepositoryException;

	/**
	 * This method only get FileNode metadata, but no any file read out from repository.
	 * @param ticket
	 * @param uuid
	 * @param version
	 * @return
	 */
	FileNode getMetaDate(ITicket ticket, String uuid, Integer version)  throws RepositoryException;
	
	/**
	 * Create a unique page or user node. Before that, it will try to create Level 2 in repository structure if it 
	 * does not exist<br>
	 * 
	 * @param ticket
	 * @param type
	 * @return
	 * @throws RepositoryException
	 */
	String createIdentifier(ITicket ticket, String type ,String identifierUuid)  throws RepositoryException , RepositoryTiemoutExcetpion;
	
	void removeIdentifier(ITicket ticket, String type ,String identifierUuid)  throws RepositoryException , RepositoryTiemoutExcetpion;
	/**
	 * 
	 * Give level 2 type and level 3 nodeUuid (Identifier), return all children node (file desc, level 4) with any old versions. <BR>
	 * 
	 * This method only read file description from level 4. and it will skip nt:file and jcr:resource data. 
	 * @param ticket 
	 * @param type : page or user (2nd level)
	 * @param identifierUuid : pageUuid or UserUuid(username?) etc
	 * @param withResource : copy nt:resource file stream content if true
	 * @return
	 */
	List<FileNode> getAllIdentifierNodes(ITicket ticket, String type, String identifierUuid, boolean withResource) throws RepositoryException;
	
	/**
	 * Give level 2 type and level 3 nodeUuid (Identifier), return all children node (file desc, level 4) with any old versions. <BR>
	 * 
	 * This method only read file description from level 4. and it will skip nt:file and jcr:resource data. 
	 * @param ticket 
	 * @param type : page or user (2nd level)
	 * @param spaceUname : 
	 * @param withResource : copy nt:resource file stream content if true
	 * @return
	 */
	List<FileNode> getAllSpaceNodes(ITicket ticket, String typeAttachment, String spaceUname, boolean withResource) throws RepositoryException;

	void copy(ITicket fromTicket, String fromType, String fromIdentifierUuid,ITicket toTicket, String toType, String toIdentifierUuid) 
			throws RepositoryException , RepositoryTiemoutExcetpion;
	
	/**
	 * @param fromTicket
	 * @param spaceUname
	 */
	void removeWorkspace(ITicket fromTicket, String spaceUname)  throws RepositoryException , RepositoryTiemoutExcetpion;

	/**
	 * Check if this given identifier exist or not.
	 * @param ticket
	 * @param type
	 * @param identifierUuid
	 * @return
	 * @throws PathNotFoundException
	 * @throws RepositoryException
	 */
	boolean hasIdentifierNode(ITicket ticket, String type, String identifierUuid) throws RepositoryException;
	
	/**
	 * update existed all spaces's quota 
	 * @param size
	 * @return
	 */
	int updateExistWorkspacesQuota(long size);
	
	/**
	 * update existed all spaces's quota 
	 * @param size
	 * @return
	 */
	void updateWorkspaceQuota(String spaceName, long size);
	/**
	 * @param ticket
	 * @param typeAttachment
	 * @param pageUuid
	 * @param fileName
	 * @return
	 */
	CrFileNode getLatestCRFileNode(ITicket ticket, String type, String identifierUuid, String fileName);
	
	
}
