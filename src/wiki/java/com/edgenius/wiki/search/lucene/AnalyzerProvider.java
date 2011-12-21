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

import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.beans.factory.InitializingBean;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.edgenius.core.Global;
import com.edgenius.wiki.search.service.FieldName;
import com.edgenius.wiki.search.service.LowerCaseAnalyzer;

/**
 * @author Dapeng.Ni
 */
public class AnalyzerProvider implements InitializingBean{

	private PerFieldAnalyzerWrapper indexAnalyzer;
	private Analyzer searchAnalyzer;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		loadAnalyzer();
	}

	public void loadAnalyzer() {
		
		Analyzer baseAnalyzer;
		//index
		if(Global.isLanguage(Locale.CHINESE)){
			baseAnalyzer = new MMSegAnalyzer(); //PaodingAnalyzer()
		}else{
			baseAnalyzer = new StandardAnalyzer(LuceneConfig.VERSION);
		}
		
		indexAnalyzer = new PerFieldAnalyzerWrapper(baseAnalyzer); 
		indexAnalyzer.addAnalyzer(FieldName.UNSEARCH_SPACE_UNIXNAME,new LowerCaseAnalyzer());
		indexAnalyzer.addAnalyzer(FieldName.CONTRIBUTOR,new LowerCaseAnalyzer());
		indexAnalyzer.addAnalyzer(FieldName.KEY,new LowerCaseAnalyzer());
	
		//search
		searchAnalyzer = baseAnalyzer;
	}

	/**
	 * @return the indexAnalyzer
	 */
	public Analyzer getIndexAnalyzer() {
		return indexAnalyzer;
	}

	/**
	 * @return the searchAnalyzer
	 */
	public Analyzer getSearchAnalyzer() {
		return searchAnalyzer;
	}

}
