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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dapeng.Ni
 */
public class IndexWriterTemplate {
	private static final Logger log = LoggerFactory.getLogger(IndexWriterTemplate.class);
	
	private IndexFactory indexFactory;
	
	public IndexWriterTemplate(IndexFactory indexFactory) {
		this.indexFactory = indexFactory;
		
	}
	
	public void deleteDocuments(Term term) {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.deleteDocuments(term);
			writer.commit();
		} catch(IOException ex) {
			log.error("Error during deleting a document", ex);
			throw new IndexAccessException("Error during deleting a document.",ex);
		} 
	}

	public void doIndexing(IndexCallback callback) {
		
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			callback.addDocument(writer);
			writer.commit();
		} catch(IOException ex) {
			log.error("Error during deleting a document", ex);
			throw new IndexAccessException("Error during deleting a document.",ex);
		} 
		
	}
	/**
	 * @param documentCreator
	 */
	public void addDocument(Document document) {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.addDocument(document);
			writer.commit();
		} catch(IOException ex) {
			log.error("Error during add a document", ex);
			throw new IndexAccessException("Error during add a document.",ex);
		}
	}

	/**
	 * 
	 */
	public void optimize() {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.optimize();
			writer.commit();
		} catch(IOException ex) {
			log.error("Error during optimize documents", ex);
			throw new IndexAccessException("Error optimize documents.",ex);
		}
	}

	/**
	 * This method will close IndexWriter. 
	 * This method can be used to create an empty index.
	 */
	public void close() {
		IndexWriter writer = indexFactory.getIndexWriter();
		try {
			writer.close();
		} catch (Exception e) {
			log.error("Unable to close Index", e);
		} finally{
			try {
				if( IndexWriter.isLocked(writer.getDirectory())){
					IndexWriter.unlock(writer.getDirectory());
				}
			} catch (Exception e) {
				log.error("Unable to unlock Index", e);
			}
		}
	}
	
}
