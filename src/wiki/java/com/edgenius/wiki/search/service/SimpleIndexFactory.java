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

import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.springmodules.lucene.index.LuceneIndexAccessException;
import org.springmodules.lucene.index.factory.AbstractIndexFactory;
import org.springmodules.lucene.index.factory.IndexFactory;
import org.springmodules.lucene.index.factory.LuceneIndexReader;
import org.springmodules.lucene.index.factory.LuceneIndexWriter;
import org.springmodules.lucene.index.factory.SimpleLuceneIndexReader;
import org.springmodules.lucene.index.factory.SimpleLuceneIndexWriter;

/**
 * @author Dapeng.Ni
 */
public class SimpleIndexFactory  extends AbstractIndexFactory implements IndexFactory {

	public SimpleIndexFactory(Directory directory) {
		setDirectory(directory);
		//set default analyser
		setAnalyzer(new StandardAnalyzer(Version.LUCENE_CURRENT));
	}

	private void checkIndexLocking() throws IOException {
		if( IndexWriter.isLocked(getDirectory()) ) {
			throw new LuceneIndexAccessException("The index is locked");
		}
	}

	public LuceneIndexReader getIndexReader() {
		try {
			checkIndexLocking();

			boolean exist = IndexReader.indexExists(getDirectory());
			if(!exist) {
				throw new LuceneIndexAccessException("The index doesn't exist for the specified directory");
			}
			return new SimpleLuceneIndexReader(IndexReader.open(getDirectory()));
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during opening the reader",ex);
		}
	}

	public LuceneIndexWriter getIndexWriter() {
		try {
			checkIndexLocking();
			
			IndexWriter writer = new IndexWriter(getDirectory(), getAnalyzer(),MaxFieldLength.UNLIMITED);
			setIndexWriterParameters(writer);
			return new SimpleLuceneIndexWriter(writer);
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during creating the writer",ex);
		}
	}
	public LuceneIndexWriter getRebuildIndexWriter() {
		try {
			checkIndexLocking();
			
			IndexWriter writer = new IndexWriter(getDirectory(), getAnalyzer(),true, MaxFieldLength.UNLIMITED);
			setIndexWriterParameters(writer);
			return new SimpleLuceneIndexWriter(writer);
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during creating the writer",ex);
		}
	}

}
