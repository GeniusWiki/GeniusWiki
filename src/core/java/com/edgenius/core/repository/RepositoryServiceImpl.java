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

/**
 * THIS CLASS IS REPLACED BY SimpleRepositoryServcieImpl. Reasons is jackrabbit API has some problem.
 * <li>I can not simply update property with increasing version?!!! This function is quite important when user update 
 * attachment filename/comment but without uploading</li>
 * <li>Jackrabbit version node will duplicated node once node created, it means uploaded file will duplicated even no any version</li>
 * <li>Jackrabbit has very low performance to handle large file.</li>
 * <li>Jackrabbit block reading same identifier node during uploading a file.</li>
 * <li>Jackrabbit does not support remove workspace. For versioned node, it is pretty difficult because you can not simply remove 
 * a directory</li>
 * 
 * I guess above weakness may caused by misunderstanding of jackrabbit from me. But I really don't have enough time to investigate it.
 * Jackrabbit may be back in future if it is mature enough.
 * 
 * Following comment is for some discrepancies during upgrading to SimpleRepositoryServcieImpl. 
 * <li>ITicket will contain isAllowXXX() method in SimpleRepositoryServcieImpl</li>
 * <li>FileNode class comment all API from Jackrabbit</li>
 * <li>This class does not contain lucene index function</li>
 * <li>This class does not contain Encoding handling in FileNode</li>
 * <li>Method download does not return identifier UUID(pageUUID), this make impossible in security check during download</li>
 * <li></li>
 * 
 * ------------------------------------------------------------------------------
 * Structure of repository:
 * Root->type(page,user)->idenifier(pageUuid,username)-> 
 * -> geniuswiki:file(filename)->resourceNode(stream data)
 * 
 * (30/07/2008) This class comments all code, as I maintain Jackrabbit library, except jackrabbit-text-extractors. 
 * This class put here just for future reference usage.
 * @author dapeng
 *
 */
public class RepositoryServiceImpl { 
//implements RepositoryService{
//	private static final Logger log = LoggerFactory.getLogger(RepositoryServiceImpl.class);
//	private JcrSessionFactory sessionFactory;
//	private Resource filenodeCnd;
//
//
//	public void createWorkspace(String spacename, String username, String password) throws RepositoryException {
//		WorkspaceImpl ws;
//		try {
//			//clear namespace: use default one
//			sessionFactory.setNamespaces(null);
//			//always use "default" workspace to create another new one
//			sessionFactory.setWorkspaceName(DEFAULT_SPACE_NAME);
//			ws = (WorkspaceImpl) sessionFactory.getSession().getWorkspace();
//			ws.createWorkspace(spacename);
//			RegisterCustomNodeTypes(ws, filenodeCnd);
//		} catch (Exception e) {
//			log.error("Create workspace failed.", e);
//			throw new RepositoryException(e);
//		}
//		
//	}
//
//	public void removeWorkspace(ITicket fromTicket, String spaceUname) {
//		//TODO: need check login?
//		
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see com.edgenius.core.repository.RepositoryService#downloadFile(com.edgenius.core.repository.ITicket, java.lang.String, long)
//	 */
//	public FileNode downloadFile(ITicket ticket, String nodeUuid, String version) throws RepositoryException {
//		Session session = getSession(ticket);
//		Node filenode = null;
//		try {
//			filenode = session.getNodeByUUID(nodeUuid);
//		} catch (ItemNotFoundException e) {
//			log.warn("Could get file from reposiotry by uuid " + nodeUuid);
//			return null;
//		}
//		
//		if(filenode == null){
//			log.warn("Could get file from reposiotry by uuid " + nodeUuid);
//			return null;
//		}
//		try{
//			log.info("Download file from " + ticket.getSpacename() + " by nodeUuid:" + nodeUuid + ". version:" + version);
//			FileNode attachment = new FileNode();
////			TODO:
////			attachment.setIdentifier(filenode.getProperty().getString());
//			if(NumberUtils.toFloat(version) == 0){
//				//download current version
//				attachment.setFilename(filenode.getProperty(ATTR_NAME_FILENAME).getString());
//				Node resNode = filenode.getNode("jcr:content");
//				Property data = resNode.getProperty("jcr:data");
//				attachment.setFile(data.getStream());
//				attachment.setContentType(resNode .getProperty("jcr:mimeType").getString());
//			}else{
//				//chose special version for download
//				VersionHistory history = filenode.getVersionHistory();
//				Version ver = history.getVersion(new Float(version).toString());
//	            NodeIterator nodeIter = ver.getNodes();
//	            if(nodeIter.hasNext()){
//	            	Node node = (Node) nodeIter.next();
//		    		attachment.setFilename(filenode.getProperty(ATTR_NAME_FILENAME).getString());
//		    		Node resNode = node.getNode("jcr:content");
//		    		Property data = resNode .getProperty("jcr:data");
//		    		attachment.setFile(data.getStream());
//		    		attachment.setContentType(resNode .getProperty("jcr:mimeType").getString());
//		         }
//			}
//
//			return attachment;
//		} catch (Exception e) {
//			log.error("Failed on downlaod." , e);
//			return null;
//		}
//		
//	}
//
//	public boolean removeFile(ITicket ticket, String nodeUuid, String version) throws RepositoryException {
//		try {
//			log.info("File will removed from " + ticket.getSpacename() + ". NodeUUID: " + nodeUuid + ". Version: " + version);
//			Session session = getSession(ticket);
//			Node filenode = session.getNodeByUUID(nodeUuid);
//			if(NumberUtils.toFloat(version,0.0f) == 0.0f){
//				//remove history together 
//				filenode.remove();
//				log.info("All history is removed");
//			}else{
//				//only remove special version
//				VersionHistory history = filenode.getVersionHistory();
//				history.removeVersion(version);
//				log.info("Version " + version + " is removed");
//			}
//			session.save();
//		} catch (Exception e) {
//			return false;
//		}
//		log.info("Remove is done");
//		return true;
//	}
//
//	public void saveFile(ITicket ticket, FileNode attachment) throws RepositoryException {
//		Session session = getSession(ticket);
//		
//		Node identifierNode = getIdentifierNode(attachment.getType(),attachment.getIdentifier(), session);
//		Node filenode=null,resNode;
//		
//		log.info("File is going to save to " + ticket.getSpacename() + "");
//
//		//page->attachment->file->resource
//		NodeIterator iter = identifierNode.getNodes();
//		boolean found = false;
//		while(iter.hasNext()){
//			filenode = iter.nextNode();
//			if(StringUtils.equalsIgnoreCase(filenode.getProperty(ATTR_NAME_FILENAME).getString()
//					,attachment.getFilename())){
//				found = true;
//				break;
//			}
//		}
//		if(found){
//			filenode.checkout();
//			resNode = filenode.getNode("jcr:content");
//			log.info("FileNode is appending version to a existed node :" + filenode.getUUID());
//		}else{
//			//this node name is useless now, so just create a random unique one
//			filenode = identifierNode.addNode(UUID.randomUUID().toString(),"geniuswiki:file");
//			filenode.addMixin("mix:versionable");
//			resNode = filenode.addNode("jcr:content", "nt:resource");
//			log.info("FileNode is creating a new node :" + filenode.getUUID());
//		}
//		resetMetaData(attachment, filenode);
//		
//
//		resNode.setProperty("jcr:mimeType", attachment.getContentType());
//		resNode.setProperty("jcr:lastModified", Calendar.getInstance());
//		resNode.setProperty("jcr:data", attachment.getFile());
//		session.save();
//		Version ver = filenode.checkin();
//		
//		attachment.setNodeUuid(filenode.getUUID());
//		
//		attachment.setVersion(ver.getName());
//		
//		log.debug("File node create on " + filenode.getBaseVersion().getCreated() + " by version " + attachment.getVersion());
//		
//	}
//
//
//	public void updateMetaData(ITicket ticket, FileNode attachment) throws RepositoryException {
//		if(attachment.getNodeUuid() == null){
//			log.error("Failed update meta data because the given NodeUUID is null");
//			return;
//		}
//		Session session = getSession(ticket);
//		Node filenode = session.getNodeByUUID(attachment.getNodeUuid());
//		filenode.checkout();
//		resetMetaData(attachment, filenode);
//		session.save();
//		
//		log.info("Metadata update on " +ticket.getSpacename() + ". Detail:"+ attachment );
//		//DO NOT call node.checkin() to avoid:
//		//1. update metadata won't increase version number
//		//2. Checkin() will duplicated jcr:content node, it makes the attachment file duplicated as well, repository size increase fast. 
//	}
//
//	public void updateMetaData(ITicket ticket, String nodeUuid, String name, String desc) throws RepositoryException{
//
//		Session session = getSession(ticket);
//		Node filenode = session.getNodeByUUID(nodeUuid);
//		filenode.checkout();
//		
//		filenode.setProperty(ATTR_NAME_FILENAME, name==null?"":name);
//		filenode.setProperty(ATTR_NAME_COMMENT, desc==null?"":desc);
//		
//		session.save();
//		//FUCK,FUCK, why I can not simply update property with increasing version?!!!
//		filenode.checkin();
//		log.info("Metadata update on " +ticket.getSpacename()+ " of node " + nodeUuid + ". Name:"+ name + " .Comment:"+ desc);
//		//DO NOT call node.checkin() to avoid:
//		//1. update metadata won't increase version number
//		//2. Checkin() will duplicated jcr:content node, it makes the attachment file duplicated as well, repository size increase fast. 
//	}
//	
//	public String createIdentifier(ITicket ticket, String type, String identifierUuid)  throws RepositoryException{
//		log.info("New identifier will be generated for identifierUuid:" + identifierUuid);
//		Session session = getSession(ticket);
//		Node root = session.getRootNode();
//		//page or user, general, they are not in same space, because user should in default space, page in special space
//		Node typeNode;
//		if(root.hasNode(type))
//			typeNode = root.getNode(type);
//		else
//			typeNode = root.addNode(type,"nt:unstructured");
//		//pageuuid or username
//		Node identifierNode = typeNode.addNode(identifierUuid,"nt:unstructured");
//		identifierNode.addMixin("mix:referenceable");
//		
//		session.save();
//		
//		log.info("Repository create a new type " +type+ " in space " + ticket.getSpacename() + " UUID is" + identifierNode.getUUID());
//		
//		return identifierNode.getUUID();
//		
//	}
//	
//	public void removeIdentifier(ITicket ticket, String type, String identifier) throws RepositoryException {
//		log.info("Type " +type+ " in space " + ticket.getSpacename() + " identifier:" + identifier + " is going to remove");
//		
//		Session session = getSession(ticket);
//		Node identifierNode = getIdentifierNode(type,identifier, session);
//		log.info("Node UUID " +  identifierNode.getUUID() + " is going to removed from repository.");
//		identifierNode.remove();
//		
//		session.save();
//		
//		log.info("Type " +type+ " in space " + ticket.getSpacename() 
//				+ " identifier:" + identifier + " is removed");
//		
//	}
//	public List<FileNode> getAllIdentifierNodes(ITicket ticket,String type, String identifier, boolean withResource) throws RepositoryException {
//		List<FileNode> list = new ArrayList<FileNode>();
//		Session session = getSession(ticket);
//		
//		Node identifierNode = getIdentifierNode(type,identifier, session);
//		
//		NodeIterator iter = identifierNode.getNodes();
//		
//		//retrieve all geniuswiki:file nodes under this identifier
//		while(iter.hasNext()){
//			Node child = (Node) iter.next();
//			//copy geniuswiki:file history as well
//			FileNode.copy(child, list, true,withResource);
//		}
//		
//		return list;
//	}
//
//	public void copy(ITicket fromTicket, String fromType, String fromIdentifierUuid, ITicket toTicket, String toType, String toIdentifierUuid) throws RepositoryException {
//		List<FileNode> fromList = getAllIdentifierNodes(fromTicket,fromType, fromIdentifierUuid,true);
//		
//		log.info("Copy from Ticket name(SpaceUname) " + fromTicket.getSpacename() + " to " + toTicket.getSpacename());
//		log.info("Copy Identifier " + fromIdentifierUuid + " to toIdentifierUuid " + toIdentifierUuid);
//		for (FileNode attachment : fromList) {
//			attachment.setIdentifier(toIdentifierUuid);
//			saveFile(toTicket, attachment);
//		}
//		
//	}
//
//
//	public ITicket login(final String spacename, final String username, final String password) throws RepositoryException {
//		
//		return new ITicket(){
//
//			public String getPassword() {
//				return password;
//			}
//
//			public String getSpacename() {
//				if(StringUtils.isBlank(spacename))
//					return DEFAULT_SPACE_NAME;
//				
//				return spacename;
//			}
//
//			public String getUsername() {
//				return username;
//			}
//
//			public boolean isAllowRead() {
//				return true;
//			}
//
//			public boolean isAllowWrite() {
//				return true;
//			}
//			
//		};
//	}
//	public boolean hasIdentifierNode(ITicket ticket, String type, String identifierUuid) throws PathNotFoundException, RepositoryException {
//		Session session = getSession(ticket);
//		Node root = session.getRootNode();
//		if(root == null)
//			return false;
//		if(!root.hasNode(type))
//			return false;
//		Node typeNode = root.getNode(type);
//		//identifier->
//		return typeNode.hasNode(identifierUuid);
//	}
//
//	//********************************************************************
//	//                       Private methods
//	//********************************************************************
//	private void resetMetaData(FileNode attachment, Node filenode) throws ValueFormatException, VersionException, LockException, ConstraintViolationException, RepositoryException {
//		filenode.setProperty(ATTR_NAME_SHARED, attachment.isShared());
//		filenode.setProperty(ATTR_NAME_STATUS, attachment.getStatus());
//		filenode.setProperty(ATTR_NAME_FILENAME, attachment.getFilename()==null?"":attachment.getFilename());
//		filenode.setProperty(ATTR_NAME_TYPE, attachment.getType()==null?"":attachment.getType());
//		filenode.setProperty(ATTR_NAME_COMMENT, attachment.getComment()==null?"":attachment.getComment());
//		
//		//some properties saved in fileNode:
//		filenode.setProperty(ATTR_NAME_CREATOR, attachment.getCreateor()==null?"":attachment.getCreateor());
//		filenode.setProperty(ATTR_NAME_CONTENTTYPE, attachment.getContentType()==null?"":attachment.getContentType());
//		filenode.setProperty(ATTR_NAME_SIZE, attachment.getSize());
//		attachment.setDate(new Date());
//		filenode.setProperty(ATTR_NAME_DATE,Calendar.getInstance());
//	}
//	/**
//	 * @return
//	 * @throws RepositoryException 
//	 */
//	private Session getSession(ITicket ticket) throws RepositoryException {
//		//authentication
//		Credentials credentials = new SimpleCredentials(ticket.getUsername(),ticket.getPassword().toCharArray());
//		sessionFactory.setCredentials(credentials);
//		sessionFactory.setWorkspaceName(ticket.getSpacename());
//		
//		return sessionFactory.getSession();
//	}
//	
//
//	private Node getIdentifierNode(String type, String identifierUuid, Session session) throws PathNotFoundException, RepositoryException {
//		//this node will be create once page created
//		//type(page,user)->
//		Node typeNode = session.getRootNode().getNode(type);
//		//identifier->
//		Node identifierNode = typeNode.getNode(identifierUuid);
//		return identifierNode;
//	}
//	
//    private void RegisterCustomNodeTypes(Workspace ws, Resource resource) throws Exception{
//
//    	//check if the prefix already exist.
//    	String[] existPrefixs = ws.getNamespaceRegistry().getPrefixes();
//    	for (String prefix : existPrefixs) {
//			if(StringUtils.equals(FileNode.PREFIX, prefix)){
//				return;
//			}
//		}
//    	
//        // Read in the CND file
//    	ws.getNamespaceRegistry().registerNamespace(FileNode.PREFIX, FileNode.NAMESPACE);
//    	
//        Reader fileReader = new InputStreamReader(resource.getInputStream());
//        // Create a CompactNodeTypeDefReader
//        CompactNodeTypeDefReader cndReader = new CompactNodeTypeDefReader(fileReader
//        			, FileUtil.getFileName(resource.getFilename()));
//
//        // Get the List of NodeTypeDef objects
//        List ntdList = cndReader.getNodeTypeDefs();
//
//        // Get the NodeTypeManager from the Workspace.
//        NodeTypeManagerImpl ntmgr =(NodeTypeManagerImpl)ws.getNodeTypeManager();
//
//        // Acquire the NodeTypeRegistry
//        NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
//
//        // Loop through the prepared NodeTypeDefs
//        for (Iterator i = ntdList.iterator(); i.hasNext();) {
//            // Get the NodeTypeDef...
//            NodeTypeDef ntd = (NodeTypeDef)i.next();
//            ntreg.registerNodeType(ntd);
//        }
//    }
//
//	//********************************************************************
//	//               set / get
//	//********************************************************************
//	public void setSessionFactory(JcrSessionFactory sessionFactory) {
//		this.sessionFactory = sessionFactory;
//	}
//
//	public void setFilenodeCnd(Resource filenodeCnd) {
//		this.filenodeCnd = filenodeCnd;
//	}
//
//	@Override
//	public FileNode getMetaDate(ITicket ticket, String uuid, Integer version) throws RepositoryException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
	
}
