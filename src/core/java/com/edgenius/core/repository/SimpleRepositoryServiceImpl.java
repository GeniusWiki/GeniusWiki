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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import com.edgenius.core.Global;
import com.edgenius.core.dao.CrFileNodeDAO;
import com.edgenius.core.dao.CrWorkspaceDAO;
import com.edgenius.core.model.CrFileNode;
import com.edgenius.core.model.CrWorkspace;
import com.edgenius.core.model.User;
import com.edgenius.core.util.AuditLogger;
import com.edgenius.core.util.FileUtil;
import com.edgenius.core.util.FileUtilException;
import com.edgenius.core.util.ThreadInterruptManager;
import com.edgenius.core.util.ZipFileUtil;
import com.edgenius.core.util.ZipFileUtilException;
/**
 * 
 * Repository Root -> Space ->  Node Type(Page, Space or User) -> IdentifierUUID (PageUuid or Username) -> NodeUUID -> 
 * ->1 (version number) -> physical file
 *   2 (version number) -> physical file
 * <br>
 * The Node Type, please refer to RepositoryService.TYPE_* static variables.
 * @author dapeng
 *
 */
@Transactional
public class SimpleRepositoryServiceImpl implements RepositoryService, InitializingBean{
	private static final Logger log = LoggerFactory.getLogger(SimpleRepositoryServiceImpl.class);
	private static final String QUOTA_FILE_NAME = "quota";
	private static final String TMP_BULK_CHECKIN = "bulkcheckin";
	
	public static final String LOCK_DIR = "_lock_";
	//any file will rename to this name, so that system can be handle any special character in physical file name.
	public static final String DEFAULT_FILE_NAME = "0";
	
	
	//120 seconds
	private static final int TIMEOUT = 120;
	
	private MessageDigest md5Digest = null;
	private ReentrantLock writeLock = new ReentrantLock();
	private HashMap<String, ConditionGroup> spaceLockMap = new HashMap<String, ConditionGroup>();
	
	private Resource homeDirResource;
	private String homeDir;
	private CrFileNodeDAO  crFileNodeDAO;
	private CrWorkspaceDAO crWorkspaceDAO;
	
	public void createWorkspace(String spacename, String username,
			String password) throws RepositoryException, RepositoryTiemoutExcetpion {
		//TODO: need verify permission?
		try {
			
			acquireLock(spacename,null,null);
			String uuid = UUID.nameUUIDFromBytes(spacename.getBytes()).toString();
			CrWorkspace crW = new CrWorkspace();
			//leave user/pass empty
			crW.setName(spacename);
			crW.setSpaceUuid(uuid);
			crW.setQuota(Global.SpaceQuota);

			String spaceDir = FileUtil.getFullPath(homeDir,uuid);
			File space = new File(spaceDir);
			if(space.exists()){
				throw new RepositoryException("Space already exist in repository " + spacename);
			}
			if(!space.mkdir()){
				log.error("Space "+ spacename + " directory can not created successfully");
				throw new RepositoryException("Space "+ spacename + " directory can not created successfully");
			}else{
				crWorkspaceDAO.saveOrUpdate(crW);
//				does not user Quota file 16/11/2007
//				createQuotaFile(spacename);
				log.info("Space  "+ spacename + " is created");
			}
		} finally {
			releaseLock(spacename,null,null);
		}
		
	}


	public String createIdentifier(ITicket ticket, String type,
			String identifierUuid) throws RepositoryException, RepositoryTiemoutExcetpion {
		// so far use identifierUuid as directory name, the weakness is, identifierUuid must be REAL unique. 
		// maybe I need create another unique folder and mapping this folder to this identifierUuid.
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for identifierUuid " + identifierUuid;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		try{
			acquireLock(ticket.getSpacename(),identifierUuid,null);
			
			log.info("New identifier will be generated for identifierUuid:" + identifierUuid);
			CrWorkspace crW = getCrWorkspace(ticket);
			String typeNodeDir = FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),type);
			File typeNode = new File(typeNodeDir);
			if(!typeNode.exists()){
				if(!typeNode.mkdirs()){
					throw new RepositoryException("create type " + type + " in space " + ticket.getSpacename() + " failed");
				}
			}
			//pageuuid or username
			
			File identifier = new File(FileUtil.getFullPath(typeNodeDir,identifierUuid));
			if(identifier.exists()){
				log.warn("Identifier directory already exsit in creating request " + identifierUuid);
			}else{
				if(!identifier.mkdir())
					throw new RepositoryException("New identifier directory created failed " + identifierUuid);
			}
			log.info("Repository create a new type " +type+ " in space " + ticket.getSpacename() + " UUID is" + identifierUuid);
		} finally {
			releaseLock(ticket.getSpacename(),identifierUuid,null);
		}

		//so far return same value with input
		return identifierUuid;
		
	}


	public void removeWorkspace(ITicket ticket, String spacename)  throws RepositoryException, RepositoryTiemoutExcetpion {
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for space " + spacename;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		try {
			acquireLock(spacename,null,null);
			CrWorkspace crW = getCrWorkspace(ticket);
			String spaceDir = FileUtil.getFullPath(homeDir,crW.getSpaceUuid());
			File space = new File(spaceDir);
			FileUtils.deleteDirectory(space);
			crWorkspaceDAO.removeObject(crW);
			log.info("Repository space " + spacename + " is removed");
			
		} catch (IOException e) {
			throw new RepositoryException("Remove space " + spacename + " failed from repository");
		}finally {
			releaseLock(spacename,null,null);
		}
		
		
	}


	public FileNode downloadFile(ITicket ticket, String nodeUuid, String version, User downloader) throws RepositoryException {
		if(!ticket.isAllowRead()){
			String error = "Workspace has not read permission " + ticket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		try{
			log.info("Download file from " + ticket.getSpacename() + " by nodeUuid:" + nodeUuid + ". version:" + version);
			CrFileNode crFilenode;
			if(NumberUtils.toInt(version) == 0){
				//download current version
				crFilenode = crFileNodeDAO.getBaseByNodeUuid(nodeUuid);
			}else{
				//chose special version for download
				crFilenode = crFileNodeDAO.getVersionNode(nodeUuid,NumberUtils.toInt(version));
			}
			if(crFilenode == null){
				log.warn("Could get file from reposiotry by uuid " + nodeUuid);
				return null;
			}

			if(crFilenode.getStatus() !=  0 && downloader != null){
				//if file is draft - see Draft.MANUAL_DRAFT Draft.AUTO_DRAFT - then need check if download user has permission
				if(!downloader.getUsername().equals(crFilenode.getCreator())){
					log.warn("User " + downloader + " has no permission to read file node:" + nodeUuid);
					return null;
				}
			}
			return getFileNode(ticket, crFilenode,true);
		} catch (Exception e) {
			log.error("Failed on download" , e);
			return null;
		}
		
	}
	
	
	public void copy(ITicket fromTicket, String fromType, String fromIdentifierUuid, ITicket toTicket, String toType, String toIdentifierUuid) 
			throws RepositoryException, RepositoryTiemoutExcetpion {
		if(!fromTicket.isAllowRead()){
			String error = "Workspace has not read permission " + fromTicket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		if(!toTicket.isAllowRead()){
			String error = "Workspace has not write permission " + toTicket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		String msg = "Copy Identifier " + fromIdentifierUuid + " from space " + fromTicket.getSpacename() 
		+ " to " + toTicket.getSpacename()+" to Identifier " + toIdentifierUuid;
		try {
			acquireLock(toTicket.getSpacename(),toIdentifierUuid,null);
			
			CrWorkspace crWFrom = getCrWorkspace(fromTicket);
			CrWorkspace crWTo = getCrWorkspace(toTicket);
			log.info(msg);
			String srcDir = FileUtil.getFullPath(homeDir,crWFrom.getSpaceUuid(),fromType,fromIdentifierUuid);
			String toDir = FileUtil.getFullPath(homeDir,crWTo.getSpaceUuid(),toType,toIdentifierUuid);
			
			//copy physical files 
			FileUtils.copyDirectory(new File(srcDir), new File(toDir));
			
			//copy records in DB
			List<CrFileNode> fromList = crFileNodeDAO.getIdentifierNodes(fromType,fromIdentifierUuid);
			for (CrFileNode crFileNode : fromList) {
				CrFileNode node = (CrFileNode) crFileNode.clone();
				//treat this as TO rather than PO
				node.setUid(null);
				node.setIdentifierUuid(toIdentifierUuid);
				node.setNodeType(toType);
				node.setSpaceUname(toTicket.getSpacename());
				
				//need update the target directory to new nodeUUID, which is unique for a file node (but could have version).
				String oldNodeUuid = node.getNodeUuid();
				String newNodeUuid = UUID.randomUUID().toString();
				String oldFileNodeDir = FileUtil.getFullPath(toDir,oldNodeUuid);
				String newFileNodeDir = FileUtil.getFullPath(toDir,newNodeUuid);
				if(FileUtil.rename(oldFileNodeDir,newFileNodeDir)){
					node.setNodeUuid(newNodeUuid);
					crFileNodeDAO.saveOrUpdate(node);
				}else{
					log.error("Some file node can not be rename to new. Lost data.old NodeUuid" + oldNodeUuid + " new NodeUuid" + newNodeUuid);
				}
			}
		} catch (IOException e) {
			log.error("Copy node failed:" + msg,e);
			throw new RepositoryException(msg);
		} finally {
			releaseLock(toTicket.getSpacename(),toIdentifierUuid,null);
		}
		
		
	}
	public CrFileNode getLatestCRFileNode(ITicket ticket, String type, String identifierUuid, String fileName) {
		List<CrFileNode> nodes = crFileNodeDAO.getIdentifierNodes(type, identifierUuid, fileName);
		if(nodes == null ||nodes.size() ==0){
			return null;
		}
		
		//get latest version - we assume filename is unique under identifierUuid, duplicated filename will be versionable.
		return nodes.get(0);
	}

	public List<FileNode> getAllIdentifierNodes(ITicket ticket, String type,
			String identifierUuid, boolean withResource)
			throws RepositoryException {
		
		if(!ticket.isAllowRead()){
			String error = "Workspace has not read permission " + ticket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		CrWorkspace crW = getCrWorkspace(ticket);
		List<CrFileNode> nodes = crFileNodeDAO.getIdentifierNodes(type, identifierUuid);
		List<FileNode> list = new ArrayList<FileNode>();
		//retrieve all geniuswiki:file nodes under this identifier
		for(Iterator<CrFileNode> iter = nodes.iterator();iter.hasNext();){ 
			CrFileNode fileNode = iter.next();
			//copy geniuswiki:file history as well
			
			FileNode my = FileNode.copyPersistToNode(fileNode);

			//copy geniuswiki:filenode->nt:resource node as well
			if(withResource){
				File file = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),fileNode.getNodeType(),fileNode.getIdentifierUuid()
						,fileNode.getNodeUuid(),Integer.valueOf(fileNode.getVersion()).toString(),SimpleRepositoryServiceImpl.DEFAULT_FILE_NAME));
				try {
					my.setFile(FileUtils.openInputStream(file));
				} catch (IOException e) {
					log.warn("Failed get node " + file.getAbsolutePath() + " with error " + e);
				} 
			}
			list.add(my);
		}
		
		return list;
	}
	
	public List<FileNode> getAllSpaceNodes(ITicket ticket, String type, String spaceUname, boolean withResource) throws RepositoryException {
		
		if(!ticket.isAllowRead()){
			String error = "Workspace has not read permission " + ticket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		CrWorkspace crW = getCrWorkspace(ticket);
		List<CrFileNode> nodes = crFileNodeDAO.getSpaceNodes(type, spaceUname);
		List<FileNode> list = new ArrayList<FileNode>();
		//retrieve all geniuswiki:file nodes under this identifier
		for(Iterator<CrFileNode> iter = nodes.iterator();iter.hasNext();){ 
			CrFileNode fileNode = iter.next();
			//copy geniuswiki:file history as well
			
			FileNode my = FileNode.copyPersistToNode(fileNode);
			
			//copy geniuswiki:filenode->nt:resource node as well
			if(withResource){
				File file = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),fileNode.getNodeType(),fileNode.getIdentifierUuid()
						,fileNode.getNodeUuid(),Integer.valueOf(fileNode.getVersion()).toString(),SimpleRepositoryServiceImpl.DEFAULT_FILE_NAME));
				try {
					my.setFile(FileUtils.openInputStream(file));
				} catch (IOException e) {
					log.warn("Failed get node " + file.getAbsolutePath() + " with error " + e);
				} 
			}
			list.add(my);
		}
		
		return list;
	}



	public boolean hasIdentifierNode(ITicket ticket, String type,
			String identifierUuid) throws RepositoryException {
		if(!ticket.isAllowRead()){
			String error = "Workspace has not read permission " + ticket.getSpacename();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		CrWorkspace crW = getCrWorkspace(ticket);
		File id= new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),type,identifierUuid));
		return id.exists();
	}

	public ITicket login(final String spacename, final  String username, final  String password)
			throws RepositoryException {
		return new ITicket(){

			public String getPassword() {
				return password;
			}

			public String getSpacename() {
				if(StringUtils.isBlank(spacename))
					return DEFAULT_SPACE_NAME;
				
				return spacename;
			}

			public String getUsername() {
				return username;
			}

			public boolean isAllowRead() {
				return true;
			}

			public boolean isAllowWrite() {
				return true;
			}
			
		};
	}

	public FileNode removeFile(ITicket ticket, String nodeUuid, String version)
			throws RepositoryException {
		
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for node " + nodeUuid;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		log.info("File will removed from " + ticket.getSpacename() + ". NodeUUID: " + nodeUuid + ". Version: " + version);
		
		//get this node base, so that we can know NodeType and IdentifierUuid
		CrFileNode filenode = crFileNodeDAO.getBaseByNodeUuid(nodeUuid);
		try {
			
			acquireLock(ticket.getSpacename(),filenode.getIdentifierUuid(),nodeUuid);
			CrWorkspace crW = getCrWorkspace(ticket);
			if(NumberUtils.toInt(version) == 0){
				//remove history together 
				crFileNodeDAO.removeByNodeUuid(nodeUuid);
				File fileDir = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),filenode.getNodeType(),filenode.getIdentifierUuid(),filenode.getNodeUuid()));
				FileUtils.cleanDirectory(fileDir);
				if(fileDir.delete())
					log.info("All history "+filenode.getNodeUuid() +" is removed");
				else
					log.info("Node "+filenode.getNodeUuid() +"physcial file can not be removed");
			}else{
				//only remove special version
				if(!crFileNodeDAO.removeVersion(nodeUuid,NumberUtils.toInt(version))){
					log.warn("No version " + version + " exist in Database");
				}
				String verDir = FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),filenode.getNodeType(),filenode.getIdentifierUuid()
						,filenode.getNodeUuid(),Integer.valueOf(filenode.getVersion()).toString());
				File ver = new File(verDir);
				FileUtils.cleanDirectory(ver);
				if(ver.delete())
					log.info("Version " + version + " is removed");
				else
					log.warn("Version " + version + " physcial file can not be removed");
			}
			log.info("Remove is done");
			return FileNode.copyPersistToNode(filenode);
		} catch (Exception e) {
			log.error("Remove file node failed " , e);
			throw new RepositoryException("Remove file node failed " + e);
		}finally {
			releaseLock(ticket.getSpacename(),filenode.getIdentifierUuid(),nodeUuid);
		}
	}

	public void removeIdentifier(ITicket ticket, String type, String identifierUuid)
										throws RepositoryException, RepositoryTiemoutExcetpion {
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for identifierUuid " + identifierUuid;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		
		String msg = "Type " +type+ " in space " + ticket.getSpacename() + " identifier:" + identifierUuid + " is going to remove";
		try {
			acquireLock(ticket.getSpacename(),identifierUuid,null);
			log.info(msg);
			CrWorkspace crW = getCrWorkspace(ticket);
			File id= new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),type,identifierUuid));
			FileUtils.deleteDirectory(id);
			
			crFileNodeDAO.removeByIdentifier(identifierUuid);
		} catch (IOException e) {
			log.error("Remove identifier failed :" + msg , e);
			throw new RepositoryException(msg);
		}finally {
			releaseLock(ticket.getSpacename(),identifierUuid,null);
		}
		
		log.info("Type " +type+ " in space " + ticket.getSpacename() + " identifier:" + identifierUuid + " is removed");
		
	}



	@SuppressWarnings("unchecked")
	public List<FileNode> saveFile(ITicket ticket, FileNode attachment, boolean md5DigestRequired, boolean discardSaveDiffMd5)
			throws RepositoryException, RepositoryTiemoutExcetpion, RepositoryQuotaException  {
		List<FileNode> checkedIn = new ArrayList<FileNode>(); 
		if(!attachment.isBulkZip()){
			//TODO: does it need return only check-in successfully?
			checkedIn.add(attachment); 
		}else{
			//process bulk upload
			String dir = null;
			try {
				dir = FileUtil.createTempDirectory(TMP_BULK_CHECKIN);
				ZipFileUtil.expandZipToFolder(attachment.getFile(), dir);
				
				//retrieve all files and check-in
				
				Collection<File> files = FileUtils.listFiles(new File(dir), null, true);
				if(files != null){
					MimetypesFileTypeMap mineMap = new MimetypesFileTypeMap();
					for (File file : files) {
						try {
							FileNode node = new FileNode();
							//use same comment for all upload
							node.setComment(attachment.getComment());
							node.setShared(attachment.isShared());
							node.setFile(new FileInputStream(file));
							node.setFilename(FileUtil.getFileName(file.getName()));
							node.setContentType(mineMap.getContentType(file));
							node.setType(RepositoryService.TYPE_ATTACHMENT);
							node.setIdentifier(attachment.getIdentifier());
							node.setCreateor(attachment.getCreateor());
							node.setStatus(attachment.getStatus());
							node.setSize(file.length());
							node.setBulkZip(false);
							
							checkedIn.addAll(saveFile(ticket, node, md5DigestRequired, discardSaveDiffMd5));
							
						} catch (Exception e) {
							log.error("Unable process some files in bulk zip",e);
						}
					}
				}
			} catch (FileUtilException e) {
				throw new RepositoryException("Unable create temp dir for bulk upload",e);
			} catch (ZipFileUtilException e) {
				throw new RepositoryException("Unable unzip bulk uploaded file",e);
			}finally{
				if(dir != null){
					try {
						FileUtil.deleteDir(dir);
					} catch (IOException e) {
						log.error("Unable to delete directory " + dir);
					}
				}
			}
			
			return checkedIn;
		}
			
		//TODO: consider thread-safe
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for identifierUuid " + attachment.getIdentifier();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		checkSpaceQuota(ticket,attachment.getSize());
		try {
			//lock at identifier level so that multiple users upload will still keep version works.
			acquireLock(ticket.getSpacename(),attachment.getIdentifier(),null);
			CrWorkspace crW = getCrWorkspace(ticket);
			List<CrFileNode> nodes = getBaseNodes(attachment.getType(),attachment.getIdentifier());
			CrFileNode existFile=null, filenode = new CrFileNode();
			
			log.info("File is going to save to " + ticket.getSpacename() + "");
	
			//page->attachment->file->resource
			for(Iterator<CrFileNode> iter = nodes.iterator();iter.hasNext();){
				CrFileNode node = iter.next();
				//if file is under same Identifier(page), and file name is same, then version the item.
				if(StringUtils.equalsIgnoreCase(node.getFilename(),attachment.getFilename())){
					existFile = node;
					break;
				}
			}
			if(existFile != null){
				//increase version
				filenode.setVersion(existFile.getVersion() + 1);
				filenode.setNodeUuid(existFile.getNodeUuid());
				log.info("FileNode is appending version to a existed node :" + filenode.getNodeUuid() + " with new version " + filenode.getVersion());
			}else{
				//this node name is useless now, so just create a random unique one
				filenode.setVersion(1);
				filenode.setNodeUuid(UUID.randomUUID().toString());
				//TODO: encoding is important for lucene index building and search, here just set empty.
				filenode.setEncoding("");
				File id = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),attachment.getType(),attachment.getIdentifier(),filenode.getNodeUuid()));
				if(id.exists()){
					throw new RepositoryException("Node uuid directory already exist");
				}
				if(!id.mkdirs()){
					throw new RepositoryException("Node uuid directory create failed. Full path is " + id.getAbsolutePath());
				}
				log.info("FileNode is creating a new node :" + filenode.getNodeUuid());
			}
			filenode.setSpaceUname(ticket.getSpacename());
		
			resetMetaData(attachment, filenode);
			
			String verRootDir = FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),filenode.getNodeType(),filenode.getIdentifierUuid()
						,filenode.getNodeUuid(), new Integer(filenode.getVersion()).toString());
			File verFile = new File(verRootDir);
			if(!verFile.mkdirs()){
				//this is just ensure the case if MD5 is duplicated, system try to delete that version directory but failed...
				//at that case, only empty directory left there.
				if(verFile.exists() && verFile.list().length > 0){
					throw new RepositoryException("Node uuid " + filenode.getNodeUuid() + " can not create version directory " 
						+  Integer.valueOf(filenode.getVersion()).toString());
				}
			}
			
			OutputStream file = null;
			File ofile = new File(verRootDir,DEFAULT_FILE_NAME);
			try {
				file = new FileOutputStream(ofile);
				//save physical file
				byte[] content = new byte[1024*1024];
				int len;
				
				md5DigestRequired = md5DigestRequired && (md5Digest != null);
				while((len = attachment.getFile().read(content)) != -1){
					if(md5DigestRequired){
						md5Digest.update(content,0,len);
					}
					file.write(content, 0, len);
				}
				file.flush();
				
				if(md5DigestRequired){
					filenode.setMd5Digest(new String(Hex.encodeHex(md5Digest.digest())));
				}
				if(discardSaveDiffMd5 && filenode.getVersion() > 1){
					//compare
					if(filenode.getMd5Digest().equals(existFile.getMd5Digest())){
						//tell to delete version directory as well in finally{}!
						checkedIn = null;
						log.info("MD5 is same and ignore checked in");
						return null;
					}
				}
				//create new record in DB
				crFileNodeDAO.saveOrUpdate(filenode);
				
				
				//set back NodeUuid and Version to attachment
				attachment.setNodeUuid(filenode.getNodeUuid());
				attachment.setVersion(Integer.valueOf(filenode.getVersion()).toString());
				attachment.setDate(filenode.getModifiedDate().getTime());
				
				log.debug("File node create on " + filenode.getModifiedDate() + " by version " + attachment.getVersion());
			} catch (Exception e) {
				throw new RepositoryException("Failed save node " + e);
			}finally{
				if(file != null){
					try{file.close();}catch (Exception e) {
						log.error("Unable to close uploaded file");
					}
					if(checkedIn == null){
						if(!ofile.delete()){
							log.error("Version file {} deleted failed when MD5 duplicated case", ofile.getAbsolutePath());
							ofile.deleteOnExit();
						}
					}
				}
				if(checkedIn == null){
					//ignored check-in
					if(!verFile.delete()){
						log.error("Version directory {} deleted failed when MD5 duplicated case", verRootDir);
					}
				}
			}
		}finally {
			releaseLock(ticket.getSpacename(),attachment.getIdentifier(),null);
			if(attachment.getFile() != null){
				try{attachment.getFile().close();}catch (Exception e) {}
			}
		}
		return checkedIn;
	}




	public void updateMetaData(ITicket ticket, FileNode attachment)
			throws RepositoryException {
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for nodeUUID " 
			+ attachment.getNodeUuid() + " version " + attachment.getVersion();
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		CrFileNode filenode;
		int ver = NumberUtils.toInt(attachment.getVersion(),-1);
		if(ver == -1)
			filenode = crFileNodeDAO.getBaseByNodeUuid(attachment.getNodeUuid());
		else
			filenode = crFileNodeDAO.getVersionNode(attachment.getNodeUuid(),ver);
		
		//update all info except nodeUUID and version
		FileNode.copyNodeToPersist(attachment, filenode);
		crFileNodeDAO.saveOrUpdate(filenode);
		
	}

	public FileNode updateMetaData(ITicket ticket, String nodeUuid, String name,
			String desc) throws RepositoryException {
		if(!ticket.isAllowWrite()){
			String error = "Workspace has not write permission " + ticket.getSpacename() + " for nodeUUID " + nodeUuid;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		CrFileNode filenode = crFileNodeDAO.getBaseByNodeUuid(nodeUuid);
		if(filenode == null){
			log.error("unable to find file node by nodeUUID:" + nodeUuid);
			return null;
		}
		filenode.setComment(desc);
		filenode.setFilename(name);
		filenode.setModifiedDate(new Date());
		crFileNodeDAO.saveOrUpdate(filenode);
		
		//update - currently, this useful for index service -which need get back createDate() 
		return FileNode.copyPersistToNode(filenode);
	}
	//JDK1.6 @Override
	public FileNode getMetaDate(ITicket ticket, String nodeUuid, Integer version) throws RepositoryException {
		if(!ticket.isAllowRead()){
			String error = "Workspace has not reading permission " + ticket.getSpacename() + " for nodeUUID " + nodeUuid;
			log.warn(error);
			throw new RepositoryException("Permission denied: " + error);
		}
		CrFileNode crFilenode = crFileNodeDAO.getVersionNode(nodeUuid, version);
		
		try {
			return getFileNode(ticket, crFilenode,false);
		} catch (FileNotFoundException e) {
			//it won't happen
		}
		return null;
	}
	

	public int updateExistWorkspacesQuota(long size) {
		return crWorkspaceDAO.updateExistWorkspacesQuota(size);
	}
	
	public void updateWorkspaceQuota(String spacename, long size) {
		crWorkspaceDAO.updateWorkspacesQuota(spacename, size);
	}


	public long[] getSpaceQuoteUsage(ITicket ticket, String spacename) {
		long[] ret = new long[2];
		
		CrWorkspace crW = getCrWorkspace(ticket);
		if(crW == null){
			//maybe $SYSTEM$ 
			log.warn("Unable to get CR Workspace: " + spacename);
			return new long[]{0,0};
		}
		long quota = crW.getQuota();
		long used = 0;
		
		File spaceDir = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid()));
		//if dir not exist, then just treat it as 0 size and valid 
		if(spaceDir.exists()){
			//append file size
			used = FileUtils.sizeOfDirectory(spaceDir);
		}
		ret[0] = used;
		ret[1] = quota;
		
		return ret;
	}

	//********************************************************************
	//                       Private methods
	//********************************************************************

	/**
	 * @param ticket
	 * @param filenode
	 * @return
	 * @throws FileNotFoundException
	 */
	private FileNode getFileNode(ITicket ticket, CrFileNode filenode, boolean withDownload) throws FileNotFoundException {
		FileNode attachment = new FileNode();
		attachment.setFilename(filenode.getFilename());
		if(withDownload){
			CrWorkspace crW = getCrWorkspace(ticket);
			String baseFilename = FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),filenode.getNodeType(),filenode.getIdentifierUuid()
					,filenode.getNodeUuid(),Integer.valueOf(filenode.getVersion()).toString(),DEFAULT_FILE_NAME);
			
			InputStream baseFile = new FileInputStream(baseFilename);
			attachment.setFile(baseFile);
		}
		attachment.setType(filenode.getNodeType());
		attachment.setContentType(filenode.getContentType());
		attachment.setIdentifier(filenode.getIdentifierUuid());
		return attachment;
	}
	/**
	 * get CrWorkspace from database. Also need handle default workspace. 
	 * @param ticket
	 * @return
	 */
	private CrWorkspace getCrWorkspace(ITicket ticket) {
		CrWorkspace crW;
		if(ticket.getSpacename().equals(DEFAULT_SPACE_NAME)){
			crW = new CrWorkspace();
			crW.setName(DEFAULT_SPACE_NAME);
			crW.setQuota(0);
			crW.setSpaceUuid(DEFAULT_SPACE_NAME);
		}else{
			crW = crWorkspaceDAO.getBySpaceName(ticket.getSpacename());
		}
		return crW;
	}

	/**
	 * @param ticket
	 * @param size
	 * @return 
	 * @throws RepositoryQuotaException 
	 */
	private void checkSpaceQuota(ITicket ticket, long size) throws RepositoryQuotaException {
		long[] quota = getSpaceQuoteUsage(ticket,ticket.getSpacename());
		//space not exist or unlimited
		if(quota[0] <= 0 || quota[1] <= 0)
			return;
		
		long spaceSize = quota[0] + size;
		if (spaceSize  > quota[1]){
			String error = "Space "+ ticket.getSpacename() + " quota is " + Arrays.toString(quota) + " and current size is " + spaceSize;
			log.info(error);
			throw new RepositoryQuotaException(error);
		}
		
	}
	/**
	 * @obsolete,use CrWorksSpace table replace Quota file (I thought, every space should has a file which contains Quota info)
	 * @throws RepositoryQuotaException 
	 */
	private void checkSpaceQuotaFile(ITicket ticket, long size) throws RepositoryQuotaException {
		
		CrWorkspace crW = getCrWorkspace(ticket);
		String spaceDir = FileUtil.getFullPath(homeDir,crW.getSpaceUuid());
		File quotaFile = new File(FileUtil.getFullPath(spaceDir,QUOTA_FILE_NAME));
		long quota = Global.SpaceQuota;
		if(!quotaFile.exists()){
			//it is already be default quota size
			createQuotaFile(ticket);
		}else{
			//read quota from file
			InputStream is = null;
			try {
				is = new FileInputStream(quotaFile);
				byte[] out = new byte[1024];
				int len = -1;
				StringBuffer sb = new StringBuffer();
				while((len = is.read(out)) != -1){
					sb.append(new String(out,0,len));
				}
				quota = NumberUtils.toLong(sb.toString(), Global.SpaceQuota);
			} catch (Exception e) {
				log.error("Failed get quota size for space " + ticket.getSpacename(),e);
				//nothing
			}finally{
				IOUtils.closeQuietly(is);
			}
		}		
		//append file size
		long spaceSize = FileUtils.sizeOfDirectory(new File(spaceDir)) + size;
		
		if (spaceSize  > quota){
			String error = "Space "+ ticket.getSpacename() + " quota is " + quota + " and current size is " + spaceSize;
			log.info(error);
			throw new RepositoryQuotaException(error);
		}
		
	}


	/**
	 * @obsolete,use CrWorksSpace table replace Quota file
	 * @param ticket
	 * @param quota
	 */
	private void createQuotaFile(ITicket ticket) {
		CrWorkspace crW = getCrWorkspace(ticket);
		File quota = new File(FileUtil.getFullPath(homeDir,crW.getSpaceUuid(),QUOTA_FILE_NAME));
		//failure tolerance: try to create new default one
		OutputStream os = null;
		try {
			os = new FileOutputStream(quota);
			os.write(Long.valueOf(Global.SpaceQuota).toString().getBytes());
		} catch (IOException e) {
			log.error("Quota file for space " + ticket.getSpacename() + " created failed",e);
		}finally{
			try {
				if(os != null) os.close();
			} catch (IOException e) {
				//nothing
			}
		}
	}
	private void acquireLock(String spacename, String identifier, String nodeUuid) throws RepositoryTiemoutExcetpion {
		
		try {
			writeLock.lock();

			//this aquireLock may recover from another lock, which will terminate in TIMEOUT, now cancel  it, so that 
			//once lock acquired correct, the thread can go through without any timeout limit(for big file upload, it is necessary).
			ThreadInterruptManager.removeThread(Thread.currentThread());
			ConditionGroup cond = spaceLockMap.get(spacename);
			//maybe it is performance bottleneck
			if(identifier == null && nodeUuid == null){
				//space level lock
				if(cond == null){
					cond = new ConditionGroup();
					spaceLockMap.put(spacename, cond);
					//this space never has a lock whatever any level, success acquired lock
					cond.spaceCondition = writeLock.newCondition();
				}else{
					
					//need check itself and all sub-level node lock, if found any, then choose first one to lock thread
					Condition relCond = null;
					if(cond.spaceCondition != null){
						relCond = cond.spaceCondition;
					}if(cond.identifierConditionMap.size() > 0){
						relCond = cond.identifierConditionMap.values().iterator().next();
					}else if(cond.nodeConditionMap.size() > 0){
						Map<String, Condition> nodeMap = cond.nodeConditionMap.values().iterator().next();
						if(nodeMap.size() > 0){
							relCond = nodeMap.values().iterator().next();
						}
					}
					if(relCond == null){
						//not relative lock, success acquired lock , success acquired lock
						cond.spaceCondition = writeLock.newCondition();
					}else{
						//2 minutes wait other relative lock release
						ThreadInterruptManager.addThread(Thread.currentThread(), TIMEOUT);
						relCond.await();
						//do next cycle compete to acquire lock.
						acquireLock(spacename, identifier, nodeUuid);
					}
				}
				
			}else if(nodeUuid == null){
				//identifier level lock
				if(cond == null){
					cond = new ConditionGroup();
					spaceLockMap.put(spacename, cond);
					//this space never has a lock whatever any level, success acquired lock
					Condition idCond = writeLock.newCondition();
					cond.identifierConditionMap.put(identifier, idCond);
				}else{
					//need check space level, and sub-level
					Condition relCond = null;
					if(cond.spaceCondition != null){
						relCond = cond.spaceCondition;
					}else if(cond.identifierConditionMap.containsKey(identifier)){
						relCond = cond.identifierConditionMap.get(identifier);
					}else if(cond.nodeConditionMap.containsKey(identifier)){
						Map<String, Condition> nodeMap = cond.nodeConditionMap.get(identifier);
						if(nodeMap.size() > 0){
							relCond = nodeMap.values().iterator().next();
						}
					}
					if(relCond == null){
						//not relative  lock, success acquired lock , success acquired lock
						Condition idCond = writeLock.newCondition();
						cond.identifierConditionMap.put(identifier, idCond);
					}else{
						//2 minutes wait, if 
						ThreadInterruptManager.addThread(Thread.currentThread(), TIMEOUT);
						relCond.await();
						//do next cycle compete to acquire lock.
						acquireLock(spacename, identifier, nodeUuid);
					}
				}
			}else{
				//node level lock
				if(cond == null){
					cond = new ConditionGroup();
					spaceLockMap.put(spacename, cond);
					//this space never has a lock whatever any level, success acquired lock
					Condition nodeCond = writeLock.newCondition();
					Map<String,Condition> nodeMap = new HashMap<String,Condition>();
					nodeMap.put(nodeUuid, nodeCond);
					cond.nodeConditionMap.put(identifier,nodeMap);
				}else{
					//need check space level, and its direct identifier level
					Condition relCond = null;
					if(cond.spaceCondition != null){
						relCond = cond.spaceCondition;
					}else if(cond.identifierConditionMap.containsKey(identifier)){
						relCond = cond.identifierConditionMap.get(identifier);
					}else if(cond.nodeConditionMap.containsKey(identifier)){
						Map<String, Condition> nodeMap = cond.nodeConditionMap.get(identifier);
						relCond = nodeMap.get(nodeUuid);
					}
					if(relCond == null){
						//not relative  lock, success acquired lock , success acquired lock
						Condition nodeCond = writeLock.newCondition();
						Map<String,Condition> nodeMap = cond.nodeConditionMap.get(identifier);
						if(nodeMap == null){
							nodeMap = new HashMap<String,Condition>();
						}
						nodeMap.put(nodeUuid, nodeCond);
						cond.nodeConditionMap.put(identifier,nodeMap);
					}else{
						//2 minutes wait, if 
						ThreadInterruptManager.addThread(Thread.currentThread(), TIMEOUT);
						relCond.await();
						//do next cycle compete to acquire lock.
						acquireLock(spacename, identifier, nodeUuid);
					}
				}
			}
			log.info("Repository space " + spacename + " identifier " + identifier + " node " + nodeUuid + " acquire write lock successfully. " );
		} catch (InterruptedException e) {
			log.info("Acquire space lock concurrent interrupted");
			throw new RepositoryTiemoutExcetpion("Repository can acquired write permssion in time period " + TIMEOUT +". Timeout exception");
		}finally{
			writeLock.unlock();
		}
	}

	/**
	 * release space level lock. 
	 */
	private void releaseLock(String spacename, String identifier, String nodeUuid) {
		
		try {
			writeLock.lock();

			ConditionGroup cond = spaceLockMap.get(spacename);
			if(cond == null){
				AuditLogger.error("Unexpected case. No lock to relase for space " + spacename);
			}
			//maybe it is performance bottleneck
			if(identifier == null && nodeUuid == null){
				//space level lock
				if(cond.spaceCondition != null){
					cond.spaceCondition.signal();
					cond.spaceCondition = null;
				}
			}else if(nodeUuid == null){
				//identifier level lock
				Condition idCond = cond.identifierConditionMap.get(identifier);
				if(idCond != null){
					idCond.signal();
					cond.identifierConditionMap.remove(identifier);
				}
			}else{
				//node level lock
				Map<String, Condition> nodeMap = cond.nodeConditionMap.get(identifier);
				if(nodeMap != null){
					Condition nodeCond = nodeMap.get(nodeUuid);
					if(nodeCond != null){
						nodeCond.signal();
						nodeMap.remove(nodeUuid);
						//no more node level lock, then clear this node identifier map.
						if(nodeMap.size() == 0){
							cond.nodeConditionMap.remove(identifier);
						}
					}
				}
			}
			log.info("Repository space " + spacename + " identifier " + identifier + " node " + nodeUuid + " acquire release lock successfully. " );
		}finally{
			writeLock.unlock();
		}
	}

	private void resetMetaData(FileNode attachment, CrFileNode filenode) {
		filenode.setIdentifierUuid(attachment.getIdentifier());
		filenode.setNodeType(attachment.getType());
		filenode.setShared(attachment.isShared());
		filenode.setStatus(attachment.getStatus());
		filenode.setFilename(attachment.getFilename()==null?"":attachment.getFilename());
		filenode.setComment(attachment.getComment()==null?"":attachment.getComment());
		filenode.setEncoding(attachment.getEncoding() ==null?"":attachment.getEncoding());
		
		//some properties saved in fileNode:
		filenode.setCreator(attachment.getCreateor()==null?"":attachment.getCreateor());
		filenode.setContentType(attachment.getContentType()==null?"":attachment.getContentType());
		filenode.setSize(attachment.getSize());
		filenode.setModifiedDate(Calendar.getInstance().getTime());
	}
	/**
	 * Get base node of all nodes under identifier.
	 * @param identifier
	 * @return
	 */
	private List<CrFileNode> getBaseNodes(String nodeType, String identifier) {
		//first, get all nodes with version history: this result sorted by nodeUuid and version, so that, can easily find out base version
		List<CrFileNode> all = crFileNodeDAO.getIdentifierNodes(nodeType, identifier);
		//filter out base version node
		List<CrFileNode> baseNodes = new ArrayList<CrFileNode>();
		String uuid = null;
		for (CrFileNode crFileNode : all) {
			if(!crFileNode.getNodeUuid().equals(uuid)){
				//first one must be base version
				baseNodes.add(crFileNode);
				uuid = crFileNode.getNodeUuid();
			}
		}
		
		return baseNodes;
	}
	//********************************************************************
	//               InitializingBean method
	//********************************************************************
	public void afterPropertiesSet() throws Exception {
	    if (homeDirResource == null) {
            throw new BeanInitializationException("Must specify a repository homeDirResource property");
        }
	    try {
	    	md5Digest = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			log.error("Unable to initialize MD digest API. Digest function is disabled in Repository method",e);
		}
	    
		//check if Repository root exists, if no, create one
        File locationDir = homeDirResource.getFile();
        if (!locationDir.exists()){
        	if(!locationDir.mkdirs()){
        		throw new BeanInitializationException("Repository home dir can not created " + homeDirResource);
        	}
        }
		this.homeDir = locationDir.getAbsolutePath();
		
        //clean all lock files when system start.
        File lockDir = new File(FileUtil.getFullPath(homeDir,LOCK_DIR));
        if(!lockDir.exists()){
        	lockDir.mkdir();
        }else{
        	FileUtils.cleanDirectory(lockDir);
        }
        
	}
	//********************************************************************
	//               private class
	//********************************************************************
	private static class ConditionGroup{
		//3 level condition
//		static final int LEVEL_NODE = 0;
//		static final int LEVEL_IDENTIFIER = 1;
//		static final int LEVEL_SPACE = 2;
		
		public Condition spaceCondition;
		
		//so far don't use ConcurrentMap since these fields will be operated under synchorized Lock.
		public Map<String,Condition> identifierConditionMap = new HashMap<String,Condition>();
		
		//2 String keys: identifierUuid, nodeUuid 
		public Map<String,Map<String, Condition>> nodeConditionMap = new HashMap<String,Map<String, Condition>>();
		
	}

	//********************************************************************
	//               set / get method
	//********************************************************************
	public void setHomeDirResource(Resource homeDirResource) {
		this.homeDirResource = homeDirResource;
	}

	public void setCrFileNodeDAO(CrFileNodeDAO crFileNodeDAO) {
		this.crFileNodeDAO = crFileNodeDAO;
	}



	public void setCrWorkspaceDAO(CrWorkspaceDAO crWorkspaceDAO) {
		this.crWorkspaceDAO = crWorkspaceDAO;
	}

}
