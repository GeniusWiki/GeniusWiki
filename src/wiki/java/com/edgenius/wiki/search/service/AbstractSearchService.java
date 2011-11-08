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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.edgenius.core.SecurityValues.OPERATIONS;
import com.edgenius.core.model.User;
import com.edgenius.core.service.UserReadingService;
import com.edgenius.wiki.gwt.client.model.WidgetModel;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.security.service.SecurityService;

/**
 * @author Dapeng.Ni
 */
public abstract class AbstractSearchService extends LuceneSearchSupport{
	protected static final int FRAGMENT_LEN = 200;

	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
	protected SecurityService securityService;
	protected UserReadingService userReadingService;

	protected SearchResult emptyResult(final String keyword, final int currPageNumber) {
		
		SearchResult rs = new SearchResult();
		rs.setCurrentPage(currPageNumber);
		rs.setTotalPage(0);
		rs.setTimeSecond(0);
		rs.setKeyword(keyword);
		return rs;
	}

	protected SearchResult commonSearch(final String keyword, final int currPageNumber, 
				final int returnCount, final User user, final String... advance)
			throws SearchException {
		
		
		try {
			return (SearchResult) this.search(new SearcherCallback() {
				public Object doWithSearcher(IndexSearcher searcher) throws SearchException {
					try {
						if(StringUtils.trimToEmpty(keyword).length() > 0){
							Query[] queries = createQuery(keyword, advance);
							Sort sort = createSort(advance);
							// Don't use Lucene default filter function to filter out no reading permission results 
							// it is too slow - it will retrieve all documents in index - whatever it is matched or not
							//Filter filter = new SecurityFilter(user);
							TopDocs hits = searcher.search(queries[0], LuceneVersion.MAX_RETURN, sort);
							SearchResult rs = getResult(searcher, hits, keyword, currPageNumber, returnCount, user,queries[1]);
							return rs;
						}else{
							return emptyResult(keyword,currPageNumber);
						}
					} catch (Exception e) {
						throw new SearchException(e);
					}
				}

				
			});
		} catch (Exception e) {
			log.error("Search failed " , e);
			throw new SearchException(e);
		}
	}

		// ********************************************************************
	// private class
	// ********************************************************************
	private Sort createSort(String[] advance) {
		if(advance  != null){
			//parse all possible type
			for (String str : advance) {
				if(str.length() < 2) continue;
				if(str.charAt(0) == SearchService.ADV_GROUP_BY){
					if(NumberUtils.toInt(Character.valueOf(str.charAt(1)).toString()) == SearchService.GROUP_SPACE){
						return new Sort(new SortField(FieldName.UNSEARCH_SPACE_UNIXNAME, SortField.STRING));
					}else if(NumberUtils.toInt(Character.valueOf(str.charAt(1)).toString())  == SearchService.GROUP_TYPE){
						return new Sort(new SortField(FieldName.DOC_TYPE,SortField.INT));
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param keyword
	 * @param advance
	 * @return 2 elements: first is keyword query with advance query criteria. Last is highlight query.
	 * @throws ParseException
	 */
	private Query[] createQuery(String keyword, String ...advance) throws ParseException {
		
		//default value
		int advKeyword = 0;
		String space = null;
		int srcType = 0;
		long from = 0, to=0;
		//group by space - need filter out some special type
		boolean groupBySpace= false;
		if(advance  != null){
			//parse all possible type
			for (String str : advance) {
				if(str.length() < 2) continue;
				
				if(str.charAt(0) == SearchService.ADV_KEYWORD_TYPE)
					advKeyword = NumberUtils.toInt(str.substring(1));
				
				if(str.charAt(0) == SearchService.ADV_SPACE)
					space = str.substring(1);
				
				if(str.charAt(0) == SearchService.ADV_SOURCE_TYPES)
					srcType = NumberUtils.toInt(str.substring(1));
				
				if(str.charAt(0) == SearchService.ADV_DATE_SCOPE){
					String fromStr = str.substring(1);
					int sep = fromStr.indexOf(":");
					if(sep != -1){
						from = NumberUtils.toLong(fromStr.substring(0,sep));
						to = NumberUtils.toLong(fromStr.substring(sep+1));
						if(from != 0 && to == 0){
							//from: (to is missed)
							to = new Date().getTime();
						}
					}else{
						from = NumberUtils.toLong(fromStr);
						to = new Date().getTime();
					}
				}
				
				if(str.charAt(0) == SearchService.ADV_GROUP_BY
					&& NumberUtils.toInt(Character.valueOf(str.charAt(1)).toString()) == SearchService.GROUP_SPACE){
					groupBySpace = true;
				}
				
			}
		}
		

		keyword = QueryParser.escape(StringUtils.trimToEmpty(keyword));
		QueryParser parser = new QueryParser(LuceneVersion.VERSION, FieldName.CONTENT,new StandardAnalyzer(LuceneVersion.VERSION));
		
		if (advKeyword == SearchService.KEYWORD_EXACT)
			keyword = "\"" + keyword + "\"";
		else if(advKeyword == SearchService.KEYWORD_ALL){
			String[] words = keyword.split(" ");
			StringBuffer buf = new StringBuffer();
			for (String word : words) {
				buf.append("+").append(word).append(" ");
			}
			keyword = buf.toString().trim();
		}
		
		//highlight query on return result, could be null
		Query hlQuery = null;
		if(keyword.length() > 0){
			//Lucene throw exception if keyword is empty
			hlQuery = parser.parse(keyword);
		}
		//date scope
		if(from != 0 && to != 0){
			keyword += " [" + from + " TO " + to + "]"; 
		}
		Query keyQuery = parser.parse(keyword);
		
		Query spaceQuery = null;
		
		//space scope
		if(!StringUtils.isBlank(space)){
			//as here is not necessary to limit search type: as only page, comment, pageTag, attachment 
			//has UNSEARCH_SPACE_UNIXNAME field, so the search type is already restrict these four types
			spaceQuery = new TermQuery(new Term(FieldName.UNSEARCH_SPACE_UNIXNAME, space.trim().toLowerCase()));
		}
		
		//type filter
		List<Query> typeQueries = null;
		if(srcType != 0){
			typeQueries = new ArrayList<Query>();
			TermQuery tq;
			if((srcType & SearchService.INDEX_PAGE) > 0){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_PAGE+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_SPACE) > 0 && !groupBySpace){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_SPACE+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_COMMENT) > 0){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_COMMENT+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_ROLE) > 0 && !groupBySpace){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_ROLE+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_USER) > 0 && !groupBySpace){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_USER+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_TAGONPAGE) > 0){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_PAGE_TAG+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_TAGONSPACE) > 0 && !groupBySpace){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_SPACE_TAG+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_ATTACHMENT) > 0){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_ATTACHMENT+""));
				typeQueries.add(tq);
			}
			if((srcType & SearchService.INDEX_WIDGET) > 0 && !groupBySpace){
				tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_WIDGET+""));
				typeQueries.add(tq);
			}
		}
		
		BooleanQuery query = new BooleanQuery();
		boolean queryIsEmpty = true;
		if(keyQuery != null){
			queryIsEmpty = false;
			query.add(keyQuery, Occur.MUST);
		}
		if(spaceQuery != null){
			queryIsEmpty = false;
			query.add(spaceQuery, Occur.MUST);
		}
		if(groupBySpace && (typeQueries == null || typeQueries.size() == 0)){
			//users don't choose any special type, but sort by space, so it need give limitation on search type
			//so far - page, comment, attachment, tag on page are for Space type
			typeQueries = new ArrayList<Query>();
			TermQuery tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_PAGE+""));
			typeQueries.add(tq);
			tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_COMMENT+""));
			typeQueries.add(tq);
			tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_COMMENT+""));
			typeQueries.add(tq);
			tq = new TermQuery(new Term(FieldName.DOC_TYPE, SharedConstants.SEARCH_PAGE_TAG+""));
			typeQueries.add(tq);
		}
		
		if(typeQueries != null && typeQueries.size() > 0){
			BooleanQuery typeQuery = new BooleanQuery();
			for (Query typeQ : typeQueries) {
				typeQuery.add(typeQ, Occur.SHOULD);
			}
			queryIsEmpty = false;
			query.add(typeQuery,Occur.MUST);
		}
		
		return new Query[]{queryIsEmpty?null:query,hlQuery};
	}

	
	
	private SearchResult getResult(IndexSearcher searcher, TopDocs hits, String keyword, int currPageNumber, int ps, User user, Query hlQuery)
			throws IOException {
		// failure tolerance
		if (currPageNumber < 1)
			currPageNumber = 1;

		long time = System.currentTimeMillis();
		List<SearchResultItem> detachedDocs = new ArrayList<SearchResultItem>();

		int from = (currPageNumber - 1) * ps;
		int total = detach(searcher, detachedDocs, hits, hlQuery, from, from + ps, user);
		
		int numberOfPages = (int) Math.ceil((float) total / ps);

		time = System.currentTimeMillis() - time;
		SearchResult rs = new SearchResult();
		rs.setCurrentPage(currPageNumber);
		rs.setTotalPage(numberOfPages);
		rs.setTimeSecond(time / 1000);
		rs.setItems(detachedDocs);
		rs.setKeyword(keyword);
		rs.setTotalItem(total);
		return rs;
	}

	private int detach(IndexSearcher searcher, List<SearchResultItem> viewableMatchedResults, TopDocs hits, Query hlQuery, int from, int to, User user) throws IOException {

		Assert.isTrue(from <= to && from >= 0 && (to >= 0 || to == -1));

		//For performance issue, we simply return total result set length without permission filter out.
		//This means is total length might be larger than the set that user can view, as some result will be filter out
		//if user doesn't have permission to see.
		int len = hits.totalHits;
		
		if (len > 0 && from < len) {
			to = to == -1 ? len : (len > to ? to : len);
			//security filter from return result
			
			List<Integer> resultIdx = new ArrayList<Integer>();
			for(int idx=from ;idx < to; idx++){
				//does not include "to" , For example, from:to is 0:10, then return index is 0-9
				
				//TODO: if page includes some result that invisible to user, it is better display message to tell user
				//some result is hidden for security reason.
				if(!isAllowView(searcher.doc(hits.scoreDocs[idx].doc), user))
					continue;
				
				resultIdx.add(idx);
			}
			
			//create a highlighter for all fragment parser
			Formatter formatter = new SimpleHTMLFormatter("<span class=\"highlighter\">", "</span>");
			Highlighter hl = null;
			if(hlQuery != null){
				Scorer scorer = new QueryScorer(hlQuery);
				hl = new Highlighter(formatter, scorer);
				Fragmenter fragmenter = new SimpleFragmenter(FRAGMENT_LEN);
				hl.setTextFragmenter(fragmenter);
			}
			
			for (int idx : resultIdx) {
				SearchResultItem item = new SearchResultItem();

				Document doc = searcher.doc(hits.scoreDocs[idx].doc);
				String docType = doc.get(FieldName.DOC_TYPE);

				//common items in search results
				item.setType(NumberUtils.toInt(docType));
				item.setDatetime(doc.get(FieldName.UPDATE_DATE));
				if (userReadingService != null && !new Integer(SharedConstants.SEARCH_USER).toString().equals(docType)){
					String username = doc.get(FieldName.CONTRIBUTOR);
					User contirUser = userReadingService.getUserByName(username);
					if(contirUser != null){
						item.setContributor(contirUser.getFullname());
						item.setContributorUsername(username);
					}
				}
				if (Integer.valueOf(SharedConstants.SEARCH_PAGE).toString().equals(docType)) {
					String content = doc.get(FieldName.PAGE_CONTENT);
					item.setTitle(doc.get(FieldName.PAGE_TITLE));
					item.setSpaceUname(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME));
					
					//does set item.desc() as content, which maybe very big string. no necessary returned
					item.setFragment(createFragment(hl, StringUtil.join(" ", item.getTitle(),content)));

				}else if (Integer.valueOf(SharedConstants.SEARCH_COMMENT).toString().equals(docType)) {
						String content = doc.get(FieldName.CONTENT);
						
						item.setItemUid(doc.get(FieldName.COMMENT_UID));
						item.setSpaceUname(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME));
						item.setTitle(doc.get(FieldName.UNSEARCH_PAGE_TITLE));
						//does set item.desc() as content, which maybe very big string. no necessary returned
						item.setFragment(createFragment(hl, content));
						
				} else if (Integer.valueOf(SharedConstants.SEARCH_SPACE).toString().equals(docType)) {
					String title = doc.get(FieldName.SPACE_NAME);
					item.setTitle(title);
					item.setSpaceUname(doc.get(FieldName.SPACE_UNIXNAME));
					item.setDesc(doc.get(FieldName.SPACE_DESC));
					item.setFragment(createFragment(hl, StringUtil.join(" ", item.getTitle(),item.getDesc())));
					
				} else if (Integer.valueOf(SharedConstants.SEARCH_WIDGET).toString().equals(docType)) {
					//wTitle-> title; wDesc-> desc; wTitle(could be pageTitle or markup title) ->spaceUname
					String widgetType = doc.get(FieldName.WIDGET_TYPE);
					String title = doc.get(FieldName.WIDGET_TITLE);
					
					//does content need transfer back?? so far no
					String content = doc.get(FieldName.WIDGET_CONTENT);
					if(WidgetModel.TYPE_PAGE_LINKER.equals(widgetType)){
						//don't use as Highlighter fragment
						content = "";
					}
					
					String desc = doc.get(FieldName.WIDGET_DESC);
					
					item.setDesc(desc);
					item.setTitle(title);
					
					//add little confuse field mapping :(
					item.setSpaceUname(doc.get(FieldName.WIDGET_KEY));
					item.setItemUid(widgetType);
					
					item.setFragment(createFragment(hl,StringUtil.join(" ", item.getDesc(),content)));
					
				} else if (Integer.valueOf(SharedConstants.SEARCH_PAGE_TAG).toString().equals(docType)) {
					//page tag
					item.setTitle(doc.get(FieldName.PAGE_TAG_NAME));
					item.setSpaceUname(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME));
					item.setFragment(createFragment(hl,item.getTitle()));
				} else if (Integer.valueOf(SharedConstants.SEARCH_SPACE_TAG).toString().equals(docType)) {
					//space tag
					item.setTitle(doc.get(FieldName.SPACE_TAG_NAME));
					item.setFragment(createFragment(hl, item.getTitle()));
					
				} else if (Integer.valueOf(SharedConstants.SEARCH_USER).toString().equals(docType)) {
					String username = doc.get(FieldName.USER_NAME);
					item.setTitle(username);
					String fullname = doc.get(FieldName.USER_FULLNAME);
					//hacker - contributor is current user fullname
					item.setContributor(fullname);
					if(userReadingService != null)
						item.setDesc(userReadingService.getUserByName(username).getSetting().getStatus());
					item.setFragment(createFragment(hl,  fullname));
					
				} else if (Integer.valueOf(SharedConstants.SEARCH_ROLE).toString().equals(docType)) {
					item.setSpaceUname(doc.get(FieldName.ROLE_NAME));
					item.setTitle(doc.get(FieldName.ROLE_DISPLAY_NAME));
					item.setDesc(doc.get(FieldName.ROLE_DESC));
					//item.setFragment("");
					
				} else if (Integer.valueOf(SharedConstants.SEARCH_ATTACHMENT).toString().equals(docType)) {
					item.setTitle(doc.get(FieldName.FILE_NAME));
					item.setDesc(doc.get(FieldName.FILE_COMMENT));
					item.setItemUid(doc.get(FieldName.FILE_NODE_UUID));
					item.setSpaceUname(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME));
					String text = doc.get(FieldName.TEXT);
					//does not mark file content fragment, because it does not store in index
					String fragment = createFragment(hl, StringUtil.join(" ", item.getDesc(),text));
					item.setFragment((fragment == null || fragment.trim().length() == 0)? ("Comment: "+item.getDesc()):fragment);
				}
				viewableMatchedResults.add(item);
			}
		}
		return len;
	}
	
	private boolean isAllowView(Document doc, User user){
		long s = System.currentTimeMillis();
		boolean readAllow = true;
		int docType = Integer.valueOf(doc.get(FieldName.DOC_TYPE));
		String spaceUname;
		if (docType == SharedConstants.SEARCH_SPACE) {
			// check space permission
			spaceUname = doc.get(FieldName.SPACE_UNIXNAME);
			//only private space need check if this space allow read or not, for public space, space is always searchable.
			if(securityService.isPrivateSpace(spaceUname)){
				readAllow = securityService.isAllowSpaceReading(spaceUname, user);
			}
		} else if (docType == SharedConstants.SEARCH_PAGE) {
			readAllow = securityService.isAllowPageReading(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME), 
					doc.get(FieldName.PAGE_UUID), user);
		} else if (docType == SharedConstants.SEARCH_WIDGET) {
			readAllow = securityService.isAllowWidget(OPERATIONS.READ, doc.get(FieldName.WIDGET_KEY),user);
			
		} else if (docType == SharedConstants.SEARCH_COMMENT) {
			readAllow = securityService.isAllowPageReading(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME), 
					doc.get(FieldName.PAGE_UUID), user);
		} else if (docType == SharedConstants.SEARCH_PAGE_TAG) {
			//even space is public, tag is only searchable for read permission user. 
			readAllow = securityService.isAllowSpaceReading(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME), user);
		} else if (docType == SharedConstants.SEARCH_ATTACHMENT) {
			if(Boolean.valueOf(doc.get(FieldName.FILE_SHARED)).booleanValue()){
				//even space is public, tag is only searchable for read permission user. 
				//shared, only need check space level permission
				readAllow = securityService.isAllowSpaceReading(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME), user);
			}else{
				//non-shared, need check page level permission
				readAllow = securityService.isAllowPageReading(doc.get(FieldName.UNSEARCH_SPACE_UNIXNAME), 
						doc.get(FieldName.PAGE_UUID), user);
			}
		} 
		
		log.info("Check read permission for search result takes {}ms", (System.currentTimeMillis() - s ));
		//skip TYPE_SPACE_TAG and TYPE_USER: both all compliant with Instance Reading Permission, which checked before search start.
		return readAllow;
	}
	
	/**
	 * Match all given name-value pairs, return combined fragment. For example, spaceUname and space desc have matched
	 * fragment, then these 2 pieces are merge into one String fragment and return.
	 * @param namedValues
	 * @return
	 * @throws IOException
	 */
	private String createFragment(Highlighter hl, String content) throws IOException {
		if (content == null)
			return "";

		if(hl == null)
			return content;
		
		TokenStream tokenStream = new StandardAnalyzer(LuceneVersion.VERSION).tokenStream(FieldName.CONTENT, new StringReader(content));
		String frag;
		try {
			frag = hl.getBestFragments(tokenStream, content, 3, "...");
		} catch (InvalidTokenOffsetsException e) {
			log.error("Highlight fragment error",e);
			frag = StringUtils.abbreviate(content, FRAGMENT_LEN);
		}
		
		return frag;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	public void setUserReadingService(UserReadingService userReadingService) {
		this.userReadingService = userReadingService;
	}


}
