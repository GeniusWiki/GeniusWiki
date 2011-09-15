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
import org.apache.lucene.search.TermQuery;
import org.springmodules.lucene.search.core.SearcherCallback;
import org.springmodules.lucene.search.factory.LuceneHits;
import org.springmodules.lucene.search.factory.LuceneSearcher;

/**
 * @author Dapeng.Ni
 */
public class AttachmentSearchServiceImpl  extends AbstractSearchService implements AttachmentSearchService {

	public Document searchByNodeUuid(final String nodeUuid) throws SearchException {
		
		return (Document) this.getLuceneSearcherTemplate().search(new SearcherCallback() {
			public Object doWithSearcher(LuceneSearcher searcher) throws Exception {
				Term identifierTerm = new Term(FieldName.KEY,nodeUuid.toLowerCase());
				TermQuery query = new TermQuery(identifierTerm);
				LuceneHits hits = searcher.search(query);
				Document doc = null;
				if(hits.length() > 0){
					doc = hits.doc(0);
				}
				return doc;
			}
		});
	}

}
