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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
public class LuceneIndexWriter {
	private static final Logger log = LoggerFactory.getLogger(LuceneIndexWriter.class);
	
	private Analyzer analyzer;
	private IndexFactory indexFactory;

	public LuceneIndexWriter(IndexFactory indexFactory,Analyzer analyzer) {
		this.indexFactory = indexFactory;
		this.analyzer = analyzer;
	}
	
	public void deleteDocuments(Term term) {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.deleteDocuments(term);
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during deleting a document.",ex);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				log.error("Unable to close IndexWriter",e);
			}
		}
	}

	/**
	 * @param documentCreator
	 */
	public void addDocument(Document document) {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.addDocument(document);
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during deleting a document.",ex);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				log.error("Unable to close IndexWriter",e);
			}
		}
	}

	/**
	 * 
	 */
	public void optimize() {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.optimize();
		} catch(IOException ex) {
			throw new LuceneIndexAccessException("Error during deleting a document.",ex);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				log.error("Unable to close IndexWriter",e);
			}
		}
	}
	
}
