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
package com.edgenius.core.dao;

import java.util.List;

import com.edgenius.core.model.CrFileNode;
/**
 * 
 * @author Dapeng.Ni
 */
public interface CrFileNodeDAO  extends DAO<CrFileNode>{

	/**
	 * Get base version for this nodeUuid. Base version equals current version.
	 * @param nodeUuid
	 * @return
	 */
	CrFileNode getBaseByNodeUuid(String nodeUuid);
	/**
	 * Get node by UUID, returned with version histories 
	 * @param nodeUuid
	 * @return
	 */
	List<CrFileNode> getByNodeUuid(String nodeUuid);

	List<CrFileNode> getAllCurrentNode();
	/**
	 * Get FileNode by nodeUuid and version, if version is null, this method is same with getBaseByNodeUuid(String).
	 * @param nodeUuid
	 * @param version
	 * @return
	 */
	CrFileNode getVersionNode(String nodeUuid, Integer version);

	/**
	 * Get all nodes under this Identifier. Nodes will contain its version history.
	 * This result must be sorted by nodeUuid and version (desc).
	 * @param fromIdentifierUuid
	 * @param identifier 
	 * @return
	 */
	List<CrFileNode> getIdentifierNodes(String nodeType, String identifierUuid);
	/**
	 * Get all nodes with given name under this Identifier. Nodes will contain its version history.
	 * This result must be sorted by version (desc).
	 */
	List<CrFileNode> getIdentifierNodes(String type, String identifierUuid, String fileName);
	List<CrFileNode> getSpaceNodes(String nodeType, String spaceUname);

	boolean removeVersion(String nodeUuid, Integer version);

	/**
	 * Remove node with its version history
	 * @param nodeUuid
	 * @return
	 */
	boolean removeByNodeUuid(String nodeUuid);
	/**
	 * Remove all nodes under this identifier
	 * @param identifierUuid
	 */
	boolean removeByIdentifier(String identifierUuid);
	/**
	 * @param type
	 * @param identifierUuid
	 * @param fileName
	 * @return
	 */




}
