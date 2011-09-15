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


/**
 * Permission model
 * @author Dapeng.Ni
 */
public class PermissionModel extends GeneralModel  implements Cloneable{
	//1: role(group), 2:user : indicate this PermissionModel hole role or user permission.
	public int ownerType;
	
	public String ownerName;
	public String ownerDisplayName;
	public String ownerDesc;
	
	//Role.TYPE_SPACE or Role.TYPE_SYSTEM 
	public int roleType;
	//this object will put into a special checkbox which refer a operation, this field marks same operation with checkbox
	//it is value ordial of  OPERATIONS, such as  OPERATIONS.READ.ordinal. And this field is only using at  bring value 
	// from client to server side.
	public int operation;
	public boolean checked;
	// cache this user's all possible operations: so far, this fields only bring value back to client side.  
	// It is useless in server pass data to client side.
	public boolean[] operations;
	//if this operations[idx] already dead in upper level resource permission
	//which has same index with operations
	public boolean[] dead;
	
	public String resourceName;
	public int resourceType;

	//identify if this model is Mask attribute:only page available currently
	public boolean mask;
	//identify if this model is added by click "add user/role" dialog, useful when client is doing cancel. 
	public boolean newadded;
	
	//transient: 
	public boolean readonly;
	public boolean editing;


	
	/**
	 * @param i
	 * @return
	 */
	public Object clone() {
		//super.clone() does not support, copy fields one by one
		PermissionModel model = new PermissionModel();
		model.ownerType = this.ownerType;
		model.ownerName = this.ownerName;
		model.roleType = this.roleType;
		model.ownerDisplayName = this.ownerDisplayName;
		model.ownerDesc = this.ownerDesc;
		model.operation = this.operation;
		model.checked = this.checked;
		//not necessary clone? they just bring status for show
//		model.readonly = this.readonly;
//		model.editing = this.editing;
		model.newadded = this.newadded;
		model.resourceName = this.resourceName;
		model.resourceType = this.resourceType;
		model.dead = this.dead;
		model.mask= this.mask;
		if(operations  == null)
			model.operations = this.operations; 
		else{
			model.operations = new boolean[operations.length];
			for(int idx=0;idx< operations.length;idx++){
				model.operations[idx] = this.operations[idx];
			}
		}
		return model;
	}
	
	public PermissionModel(){
		//the size must large or equal than  by SecurityValues.OPERATIONS enum size
		operations = new boolean[10];
		for (int idx = 0; idx < operations.length; idx++) {
			operations[idx] = false;
		}
		dead = new boolean[10];
		for (int idx = 0; idx < dead.length; idx++) {
			dead[idx] = false;
		}
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof PermissionModel))
			return false;
		PermissionModel pm = (PermissionModel) obj;
		if((pm.ownerName != null && pm.ownerName.equalsIgnoreCase(this.ownerName))
			&& pm.ownerType == this.ownerType && pm.roleType == this.roleType)
			return true;
		
		return false;
	}
	public int hashCode(){
		return this.ownerName.hashCode();
	}

	public String toString(){
		return "Owner:"+this.ownerName + " on resource " + resourceName + " can do " + operation;
	}
}
