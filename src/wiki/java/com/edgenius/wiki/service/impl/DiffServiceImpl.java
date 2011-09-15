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
package com.edgenius.wiki.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.suigeneris.jrcs.diff.AddDelta;
import org.suigeneris.jrcs.diff.ChangeDelta;
import org.suigeneris.jrcs.diff.Chunk;
import org.suigeneris.jrcs.diff.DeleteDelta;
import org.suigeneris.jrcs.diff.Delta;
import org.suigeneris.jrcs.diff.Diff;
import org.suigeneris.jrcs.diff.DifferentiationFailedException;
import org.suigeneris.jrcs.diff.Revision;
import org.suigeneris.jrcs.diff.RevisionVisitor;
import org.suigeneris.jrcs.diff.myers.MyersDiff;

import com.edgenius.wiki.dao.HistoryDAO;
import com.edgenius.wiki.dao.PageDAO;
import com.edgenius.wiki.model.History;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.service.DeltaObject;
import com.edgenius.wiki.service.DiffException;
import com.edgenius.wiki.service.DiffService;

/**
 * @author Dapeng.Ni
 */
public class DiffServiceImpl implements DiffService {
	private static final Logger log = LoggerFactory.getLogger(DiffServiceImpl.class);
	private static final String HTML_LINE_BREAK = "<br>";

	private PageDAO pageDAO;
	private HistoryDAO historyDAO;

	private String DIFF_START = "<div class=\"diff-text\">";
	private String DIFF_END = "</div>";
	private String INSERT_START = "<font color=\"#8000FF\"><span class=\"diff-insertion\">";
	private String INSERT_END = "</span></font>";
	private String DEL_START = "<strike><font color=\"red\"><span class=\"diff-deletion\">";
	private String DEL_END = "</span></font></strike>";

	//NOTE: \n will replace to \r
	private char[] WORD_DELIM = new char[]{' ','\t','\r','\f'};
	
	
	public String diffToHtml(Integer uid1, Integer uid2,boolean byword) throws DiffException {
		//current page: the passed in uid should be null.
		if(uid1 == null && uid2 == null){
			throw new DiffException("Unable compare with both null");
		}
		try {
			String content1;
			String content2; 
			if(uid1 == null){
				History page2  = historyDAO.get(uid2);
				Page page1 = pageDAO.getByUuid(page2.getPageUuid());
				content1 = page1.getContent().getContent();
				content2 = page2.getContent().getContent();
			}else if(uid2 == null){
				History page1  = historyDAO.get(uid1);
				Page page2 = pageDAO.getByUuid(page1.getPageUuid());
				content1 = page1.getContent().getContent();
				content2 = page2.getContent().getContent();
			}else{
				History page1  = historyDAO.get(uid1);
				History page2  = historyDAO.get(uid2);
				content1 = page1.getContent().getContent();
				content2 = page2.getContent().getContent();
			}

			//split by word to compare
			List<DeltaObject> list = doDiff(content1, content2, byword);
			return renderHtml(list);
		} catch (DifferentiationFailedException e) {
			log.error("Diff engine exception:" , e);
			throw new DiffException(e);
		}
		
	}

	public List<DeltaObject> diffToObjectList(String text1, String text2, boolean byword) throws DiffException{
		try {
			return doDiff(text1, text2, byword);
		} catch (DifferentiationFailedException e) {
			log.error("Diff engine exception:" , e);
			throw new DiffException(e);
		}
	}
	
	//********************************************************************
	//               private method
	//********************************************************************
	private List<DeltaObject> doDiff(String wikiOld, String wikiNew, boolean byWord) throws DifferentiationFailedException {

		String[] alpha = split(wikiOld,byWord);
		String[] beta = split(wikiNew,byWord);

		Revision rev = Diff.diff(alpha, beta, new MyersDiff());

		List<DeltaObject> list = new ArrayList<DeltaObject>();
		RevisionVisitorImpl cm = new RevisionVisitorImpl(list,alpha);
		rev.accept(cm);
		
		//append last part unchanged text
		cm.close();
		
		return list;
	}
	
	private String renderHtml(List<DeltaObject> list) {
		StringBuffer sb = new StringBuffer(DIFF_START);
		for (DeltaObject delta : list) {
			if(delta.type == DeltaObject.NOCHANGE){
				sb.append(delta.content);
			}else if(delta.type == DeltaObject.ADD){
				sb.append(INSERT_START);
				sb.append(delta.content);
				sb.append(INSERT_END);
			}else if(delta.type == DeltaObject.DEL){
				sb.append(DEL_START);
				sb.append(delta.content);
				sb.append(DEL_END);
			}
		}
		sb.append(DIFF_END);
		return sb.toString();
	}

	private String[] split(String text, boolean byWord){
		if(text == null || text.length() == 0){
			return new String[]{""};
		}
		if(!byWord) //by line
			return  Diff.stringToArray(text);
		
		List<String> words = new ArrayList<String>();
		int size = text.length();
		char curr,last=text.charAt(0);
		StringBuffer word = new StringBuffer();
		for(int idx=0;idx < size;idx++){
			curr = text.charAt(idx);
			//TODO: does it need do in save wikitext in DB?
			//replace all possible \r\n to \r so that newline can be identify same word
			if(curr == '\n'){
				//if it is \r\n or \n\r, ignore this char
				if(last == '\r' || (idx<size-1 && text.charAt(idx+1)=='\r'))
					continue;
				else
					curr='\r';
			}
			//DONOT do entity change, 
//			}else if(curr =='&'){
//				word.append("&amp;");
//				last=curr;
//				continue;
//			}else if(curr =='<'){
//				word.append("&lt;");
//				last=curr;
//				continue;
//			}else if(curr =='>'){
//				word.append("&gt;");
//				last=curr;
//				continue;
//			}else if(curr =='"'){
//				word.append("&quot;");
//				last=curr;
//				continue;
//			}
			if(isDelim(curr)){
				if(curr != last){
					//get one word separate by space
					words.add(word.toString());
					word = new StringBuffer();
				}
			}else if(isDelim(last)){
				//curr is not delim, check if it is last is delim, if so, means it is end of group delim chars
				if(curr != last){
					words.add(word.toString());
					word = new StringBuffer();
				}
			}
			if(curr == '\r')
				word.append(HTML_LINE_BREAK);
			else
				word.append(curr);
			last = curr;
		}
		//append last part word
		if(word.length() > 0)
			words.add(word.toString());
		
		return (String[]) words.toArray(new String[words.size()]);
		
	}
	private boolean isDelim(char in){
		for (char delim : WORD_DELIM) {
			if(in == delim)
				return true;
		}
		return false;
	}
    private class RevisionVisitorImpl  implements RevisionVisitor {
    	private List<DeltaObject> list;
    	private String[] origStrList;
		private int scanIndex;
		
		public RevisionVisitorImpl(List<DeltaObject> list, String[] origStrList) {
			this.list  = list;
			this.origStrList = origStrList;
		}
		public void visit(Revision rev) {
			//nothing do currently
		}

		public void visit(DeleteDelta delta) {
			handleUnchanged(delta);
			DeltaObject obj = new DeltaObject(DeltaObject.DEL,getChunkStr(delta.getOriginal()));
			list.add(obj);
		}

		public void visit(ChangeDelta delta) {
			handleUnchanged(delta);
			DeltaObject org = new DeltaObject(DeltaObject.DEL,getChunkStr(delta.getOriginal()));
			DeltaObject rev = new DeltaObject(DeltaObject.ADD,getChunkStr(delta.getRevised()));
			list.add(org);
			list.add(rev);
		}

		public void visit(AddDelta delta) {
			handleUnchanged(delta);
			DeltaObject obj = new DeltaObject(DeltaObject.ADD,getChunkStr(delta.getRevised()));
			list.add(obj);			
		}
		
		public void close() {
			for (int j = scanIndex; j < origStrList.length; j++){
				DeltaObject org = new DeltaObject(DeltaObject.NOCHANGE,origStrList[j]);
				list.add(org);
			}
		}
		private String getChunkStr(Chunk chunk){
			if(chunk != null)
				return chunk.toString().toString();
			return "";
		}
		private void handleUnchanged(Delta delta) {
			Chunk orig = delta.getOriginal();
			//append original text to List<DeltaObject> array
			if (orig.first() > scanIndex) {
				//collect all consequence unchanged text into one object, rather than split them into multiple pieces
				DeltaObject sum = new DeltaObject(DeltaObject.NOCHANGE,"");
				for (int j = scanIndex; j < orig.first(); j++){
					sum = new DeltaObject(DeltaObject.NOCHANGE,sum.content + origStrList[j]);
				}
				list.add(sum);
			}
			scanIndex = orig.last() + 1;
			
		}
    	
    }

    
	//********************************************************************
	//               Set / Get
	//********************************************************************
	public void setPageDAO(PageDAO pageDAO) {
		this.pageDAO = pageDAO;
	}

	public void setHistoryDAO(HistoryDAO historyDAO) {
		this.historyDAO = historyDAO;
	}

}
