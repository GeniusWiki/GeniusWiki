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

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author Dapeng.Ni
 */
public interface DAO<T> {
	/**
     * Generic method used to get all objects of a particular type. This
     * is the same as lookup up all rows in a table.
     * @return List of populated objects
     */
    public List<T> getObjects();
    
    /**
     * Generic method to get an object based on class and identifier. An 
     * ObjectRetrievalFailureException Runtime Exception is thrown if 
     * nothing is found.
     * 
     * @param clazz model class to lookup
     * @param id the identifier (primary key) of the class
     * @return a populated object
     * @see org.springframework.orm.ObjectRetrievalFailureException
     */
    public T get(Serializable id);

    /**
     * Generic method to save an object - handles both update and insert.
     * @param o the object to save
     */
    public void saveOrUpdate(T o);

    /**
     * Generic method to delete an object based on class and id
     * @param id the identifier (primary key) of the class
     */
    public void remove(Serializable id);

    /**
     * Generic method to delete an object based on class and id
     */
    public void removeObject(Object obj);
    /**
     * Refresh object from database. It is useful when read object within same session if this object updated by some reason
     */
    public void refresh(T o);
    public void merge(T o);
    /**
     * !! It is dangerous !!
     * Clean all object from database! This method will failed if object has dependency limitation with other tables.
     */
    public void cleanTable();
}
