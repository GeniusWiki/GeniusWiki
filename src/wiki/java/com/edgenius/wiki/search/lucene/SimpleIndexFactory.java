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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * @author Dapeng.Ni
 */
public class SimpleIndexFactory  implements IndexFactory, DisposableBean, InitializingBean  {
	private static final Logger log = LoggerFactory.getLogger(SimpleIndexFactory.class);

	private boolean useCompoundFile = false;
	private int maxBufferedDocs = LuceneConfig.DEFAULT_MAX_BUFFERED_DOCS;
	private int maxMergeDocs = LuceneConfig.DEFAULT_MAX_MERGE_DOCS;
	private int mergeFactor = LuceneConfig.DEFAULT_MERGE_FACTOR;
	private int termIndexInterval = LuceneConfig.DEFAULT_TERM_INDEX_INTERVAL;
	private int writeLockTimeout = LuceneConfig.DEFAULT_WRITE_LOCK_TIMEOUT;

	//This is not spring managed fields
	private IndexWriter writer;
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Properties
	private Directory directory;
	private AnalyzerProvider analyzerProvider;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Methods
	public SimpleIndexFactory(Directory directory) {
		this.directory  = directory;
	}


	@Override
	public IndexWriter getIndexWriter() {
		
		try {
			//here doesn't check IndexWriter is close or not, so it is dangerous if close IndexWriter outside this class!!! Must use this.close() to close.
			if(writer != null)
				return writer;
			
			try {
				if(IndexWriter.isLocked(directory)){
					IndexWriter.unlock(directory);
				}
			} catch (Exception e) {
				log.error("Try to unlock failed" + directory, e);
			}
			
			IndexWriterConfig conf = getIndexWriterConfig();
			writer = new IndexWriter(directory, conf);
			
			return writer;
		} catch(IOException ex) {
			throw new IndexAccessException("Error during creating the writer:"+ directory,ex);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (directory == null) {
			throw new IllegalArgumentException("directory is required");
		}
		if(analyzerProvider == null){
			throw new BeanInitializationException("Must declare analyzerProvider");
		}
		createEmptyIndex();
	}


	@Override
	public void createEmptyIndex() {
		try {
			//try to create an empty index.
			boolean exist = IndexReader.indexExists(directory);
			if(!exist) {
				writer = this.getIndexWriter();
				this.closeIndex();
				log.info("A new Index is created on {}", directory);
			}
		} catch (Exception e) {
			log.error("Unable to create an empty index:" + directory, e);
		}
	}
	@Override
	public void closeIndex() {
		if(writer == null)
			return;
		
		try {
			writer.close();
			writer = null;
			
		} catch (Exception e) {
			log.error("Unable to close Index:" + directory, e);
		} finally{
			try {
				if( IndexWriter.isLocked(directory)){
					IndexWriter.unlock(directory);
				}
			} catch (Exception e) {
				log.error("Unable to unlock Index" + directory, e);
			}
		}
	}


	@Override
	public void destroy() throws Exception {
		this.closeIndex();
	}
	
	private IndexWriterConfig getIndexWriterConfig() {
		IndexWriterConfig conf = new IndexWriterConfig(LuceneConfig.VERSION, analyzerProvider.getIndexAnalyzer());
		conf.setMaxBufferedDocs(maxBufferedDocs);
		conf.setTermIndexInterval(termIndexInterval);
		conf.setWriteLockTimeout(writeLockTimeout);
		
		LogMergePolicy mergePolicy = new LogDocMergePolicy();
		mergePolicy.setUseCompoundFile(useCompoundFile);
		mergePolicy.setMaxMergeDocs(maxMergeDocs);
		mergePolicy.setMergeFactor(mergeFactor);
		conf.setMergePolicy(mergePolicy);
		
		return conf;
	}


	/**
	 * @param analyzerProvider the analyzerProvider to set
	 */
	public void setAnalyzerProvider(AnalyzerProvider analyzerProvider) {
		this.analyzerProvider = analyzerProvider;
	}
}
