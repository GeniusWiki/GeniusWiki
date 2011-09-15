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
package com.edgenius.wiki.search.service;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springmodules.lucene.index.factory.IndexFactory;
import org.springmodules.lucene.index.factory.LuceneIndexWriter;

/**
 * @author Dapeng.Ni
 */
public class SimpleIndexFactoryBean implements FactoryBean,InitializingBean {

	private SimpleIndexFactory factory;
	private Directory directory;

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return factory;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return IndexFactory.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	public void afterPropertiesSet() throws Exception {
		if (getDirectory() == null) {
			throw new IllegalArgumentException("directory is required");
		}
		
		this.factory = new SimpleIndexFactory(getDirectory());
		
		try {
			//try to create an empty index.
			boolean exist = IndexReader.indexExists(getDirectory());
			if(!exist) {
				LuceneIndexWriter writer =  this.factory.getIndexWriter();
				writer.close();
			}
		} catch (Exception e) {
		} finally{
			//clean lock when factory initial
			if(IndexReader.isLocked(getDirectory())){
				IndexReader.unlock(getDirectory());
			}
		}
	}

	//********************************************************************
	//               set /get 
	//********************************************************************
	public void setDirectory(Directory directory) {
		this.directory = directory;
	}

	public Directory getDirectory() {
		return directory;
	}

}
