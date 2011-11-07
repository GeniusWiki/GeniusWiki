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

import java.io.File;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * @author Dapeng.Ni
 */
public class FSDirectoryFactoryBean  implements FactoryBean<FSDirectory>, InitializingBean,DisposableBean {

    private Resource location;
    private Resource lockLocation;
    private FSDirectory directory;

    @Override
    public FSDirectory getObject() throws Exception {
        return directory;
    }

    @Override
    public Class<FSDirectory> getObjectType() {
        return FSDirectory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * This method constructs a filesystem Lucene directory.
     *  
     * <p>The location property must be set, and be a directory
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (location == null) {
            throw new BeanInitializationException("Must specify a location property");
        }
        if(lockLocation == null){
        	throw new BeanInitializationException("Must specify a lock location property");
        }
        
        File locationDir = location.getFile();
        if (!locationDir.exists()){
        	locationDir.mkdirs();
        }
        File lockDir = lockLocation.getFile();
        if(!lockDir.exists()){
        	lockDir.mkdirs();
        }
        directory = FSDirectory.open(locationDir, new SimpleFSLockFactory(lockDir));
    }
	public void destroy() throws Exception {
		directory.close();
	}
    /**
     * Specify the path on the filesystem to use for this directory storage
     */
    public void setLocation(Resource location) {
        this.location = location;
    }

	public void setLockLocation(Resource lockLocation) {
		this.lockLocation = lockLocation;
	}

}
