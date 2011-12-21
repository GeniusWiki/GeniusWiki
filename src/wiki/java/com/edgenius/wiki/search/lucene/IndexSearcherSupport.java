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
package com.edgenius.wiki.search.lucene;

import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.wiki.search.service.SearchException;

/**
 * @author Dapeng.Ni
 */
public class IndexSearcherSupport  {
	private static final Logger log = LoggerFactory.getLogger(IndexSearcherSupport.class);
	
	protected SearcherFactory searcherFactory;

	protected Object search(SearcherCallback searcherCallback) throws SearchException {
		try {
			IndexSearcher searcher = searcherFactory.getSearcher();
			
			Object rs = searcherCallback.doWithSearcher(searcher);
			
			return rs;
			
		} finally{
			try {
				searcherFactory.close();
			} catch (Exception e) {
				log.error("Close IndexSearcher/IndexReader failed after search", e);
			}
		}
	}

	public SearcherFactory getSearcherFactory() {
		return searcherFactory;
	}

	public void setSearcherFactory(SearcherFactory searcherFactory) {
		this.searcherFactory = searcherFactory;
	}
}
