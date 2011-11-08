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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

/**
 * @author Dapeng.Ni
 */
public class AttachmentSearchServiceImpl  extends AbstractSearchService implements AttachmentSearchService {

	public Document searchByNodeUuid(final String nodeUuid) throws SearchException {
		
		return (Document) this.search(new SearcherCallback() {
			public Object doWithSearcher(IndexSearcher searcher) throws SearchException {
				try {
					Term identifierTerm = new Term(FieldName.KEY,nodeUuid.toLowerCase());
					TermQuery query = new TermQuery(identifierTerm);
					TopDocs hits = searcher.search(query, LuceneVersion.MAX_RETURN);
					Document doc = null;
					
					if(hits.totalHits > 0){
						//assume only one
						doc = searcher.doc(hits.scoreDocs[0].doc);
					}
					return doc;
				} catch (Exception e) {
					throw new SearchException(e);
				}
			}
		});
	}

}
