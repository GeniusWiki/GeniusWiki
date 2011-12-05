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

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.edgenius.core.Global;
import com.edgenius.wiki.search.service.SearchException;

/**
 * 
 * @author Dapeng.Ni
 */
public class ParallelSearcherFactory implements InitializingBean, DisposableBean, SearcherFactory{
	private Directory[] directories;
	private ExecutorService exectorService = Executors.newCachedThreadPool();
	
	private ThreadLocal<IndexSearcher> container = new ThreadLocal<IndexSearcher>();
	private Analyzer analyzer;
	
	@Override
	public IndexSearcher getSearcher() throws SearchException{
		try {
			if(container.get() != null){
				return container.get();
			}
			IndexReader reader;
			if(directories.length > 1){
				IndexReader[] readers = new IndexReader[directories.length]; 
				for (int idx=0; idx< directories.length; idx++) {
					readers[idx] = IndexReader.open(directories[idx], true);
				}
				reader = new MultiReader(readers,true);
			}else{
				reader = IndexReader.open(directories[0]);
			}
			
			IndexSearcher searcher = new IndexSearcher(reader, exectorService);
			
			container.set(searcher);
			
			return searcher;
			
		} catch (CorruptIndexException e) {
			throw new SearchException(e);
		} catch (IOException e) {
			throw new SearchException(e);
		}
	}
	@Override
	public void close() throws SearchException {
		if(container.get() != null){
			IndexSearcher searcher = container.get();
			container.remove();
			try {
				searcher.getIndexReader().close();
				searcher.close();
			} catch (IOException e) {
				throw new SearchException(e);
			}
		}
		
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		if(directories == null || directories.length == 0){
			throw new BeanInitializationException("Must declare at least one directory");
		}
		

		if("zh".equalsIgnoreCase(Global.DefaultLanguage)){
			analyzer = new PaodingAnalyzer();
		}else{
			analyzer = new StandardAnalyzer(LuceneConfig.VERSION);
		}
	}

	@Override
	public void destroy() throws Exception {
		this.close();
		exectorService.shutdown();
	}


	//********************************************************************
	//               Set / Get
	//********************************************************************
	
	public Directory[] getDirectories() {
		return directories;
	}
	
	public void setDirectories(Directory[] directories) {
		this.directories = directories;
	}
	@Override
	public Analyzer getAnalyzer() {
		
		return analyzer;
	}


}
