package com.edgenius.wiki.search.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.springframework.beans.factory.InitializingBean;

public class ParallelSearcherFactory implements InitializingBean{
	private Directory[] directories;
	private IndexReader[] readers;
	ExecutorService exectorService = Executors.newCachedThreadPool();
	
	public IndexSearcher getSearcher(){
		
		
		IndexSearcher searcher = new IndexSearcher(reader , exectorService);
		searcher.
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		
		readers = new IndexReader[directories.length]; 
		for (int idx=0; idx< directories.length; idx++) {
			readers[idx] = IndexReader.open(directories[idx], true);
		}
	}

	public Directory[] getDirectories() {
		return directories;
	}

	public void setDirectories(Directory[] directories) {
		this.directories = directories;
	}



}
