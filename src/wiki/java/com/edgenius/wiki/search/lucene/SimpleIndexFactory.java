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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.store.Directory;


/**
 * @author Dapeng.Ni
 */
public class SimpleIndexFactory  implements IndexFactory  {


	private boolean useCompoundFile = false;
	private int maxBufferedDocs = LuceneConfig.DEFAULT_MAX_BUFFERED_DOCS;
	private int maxMergeDocs = LuceneConfig.DEFAULT_MAX_MERGE_DOCS;
	private int mergeFactor = LuceneConfig.DEFAULT_MERGE_FACTOR;
	private int termIndexInterval = LuceneConfig.DEFAULT_TERM_INDEX_INTERVAL;
	private int writeLockTimeout = LuceneConfig.DEFAULT_WRITE_LOCK_TIMEOUT;
	
	private StandardAnalyzer analyzer = new StandardAnalyzer(LuceneConfig.VERSION);
	private Directory directory;
	
	public SimpleIndexFactory(Directory directory) {
		this.directory  = directory;
	}

	private void checkIndexLocking() throws IOException {
		if( IndexWriter.isLocked(directory) ) {
			throw new LuceneIndexAccessException("The index is locked");
		}
	}


	@Override
	public IndexWriter getIndexWriter() {
		try {
			checkIndexLocking();
			
			IndexWriterConfig conf = getIndexWriterConfig();
			IndexWriter writer = new IndexWriter(directory, conf);
			
			return writer;
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during creating the writer",ex);
		}
	}

	@Override
	public IndexWriter getRebuildIndexWriter() {
		try {
			checkIndexLocking();
			
			IndexWriterConfig conf = getIndexWriterConfig();
			conf.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(directory, conf);
			
			return writer;
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during creating the writer",ex);
		}
	}
	


	private IndexWriterConfig getIndexWriterConfig() {
		IndexWriterConfig conf = new IndexWriterConfig(LuceneConfig.VERSION, analyzer);
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

}
