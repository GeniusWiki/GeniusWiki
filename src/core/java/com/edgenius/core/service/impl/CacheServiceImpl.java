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
package com.edgenius.core.service.impl;

import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.edgenius.core.service.CacheService;
/**
 * 
 * @author Dapeng.Ni
 */
public class CacheServiceImpl implements CacheService{
	private static final Logger log = LoggerFactory.getLogger(CacheServiceImpl.class);
	
	private List<Ehcache> availableCache;

	public void printPolicyCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("policyCache")){
				print(cache,"PolicyCache");
			}else if(cache.getName().equals("spaceReadingCache")){
				print(cache,"SpaceReadingCache");
			}else if(cache.getName().equals("pageReadingCache")){
				print(cache,"PageReadingCache");
			}else if(cache.getName().equals("pageTreeCache")){
				print(cache,"PageTreeCache");				
			}else if(cache.getName().equals("tagCache")){
				print(cache,"TagCache");
			}else if(cache.getName().equals("userCache")){
				print(cache,"UserCache");
			}else if(cache.getName().equals("themeCache")){
				print(cache,"ThemeCache");
			}else if(cache.getName().equals("loginTimesCache")){
				print(cache,"LoginTimesCache");
			}else if(cache.getName().equals("pageEditingCache")){
				print(cache,"PageEditingCache");
			}
		}
	}
	//JDK1.6 @Override
	public void reset(int cachePolicy) {
		
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_POLICY){
			resetPolicyCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_SPACE_READING){
			resetSpaceReadingCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_PAGE_READING){
			resetPageReadingCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_USER_READING){
			resetUserCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_PAGE_TREE_READING){
			resetPageTreeCacheCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_TAG_READING){
			resetTagCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_LOGINTIMES_READING){
			resetLoginTimesCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_THEMES){
			resetThemeCache();
		}
		if(cachePolicy == CACHE_ALL || cachePolicy == CACHE_PAGE_EDITING){
			resetPageEditingCache();
		}
		
	}
	public void resetPolicyCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("policyCache")){
				cache.removeAll();
				log.info("Policy Cache is clean");
			}
		}
	}
	public void resetSpaceReadingCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("spaceReadingCache")){
				cache.removeAll();
				log.info("SpaceReadingCache Cache is clean");
			}
		}
	}
	public void resetPageReadingCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("pageReadingCache")){
				cache.removeAll();
				log.info("PageReadingCache Cache is clean");
			}
		}
	}
	public void resetPageTreeCacheCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("pageTreeCache")){
				cache.removeAll();
				log.info("PageTreeCache Cache is clean");
			}
		}
	}
	public void resetTagCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("tagCache")){
				cache.removeAll();
				log.info("TagCache Cache is clean");
			}
		}
	}
	public void resetUserCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("userCache")){
				cache.removeAll();
				log.info("UserCache Cache is clean");
			}
		}
	}
	public void resetLoginTimesCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("loginTimesCache")){
				cache.removeAll();
				log.info("LoginTimesCache Cache is clean");
			}
		}
	}
	public void resetThemeCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("themeCache")){
				cache.removeAll();
				log.info("Theme Cache is clean");
			}
		}
	}
	public void resetPageEditingCache(){
		for (Ehcache cache : availableCache) {
			if(cache.getName().equals("pageEditingCache")){
				cache.removeAll();
				log.info("Page Editing Cache is clean");
			}
		}
	}
	//********************************************************************
	//               private method
	//********************************************************************
	private void print(Ehcache cache, String cacheName){
		List<Object> keys = cache.getKeys();
		if(keys != null){
			for (Object key: keys) {
				Element value = cache.get(key);
				log.info("["+cacheName+":" + key+"="+ (value!=null?value.getObjectValue():"null") + "]");
			}
		}
	}
	//********************************************************************
	//               Set / Get
	//********************************************************************

	public void setAvailableCache(List<Ehcache> availableCache) {
		this.availableCache = availableCache;
	}
	

}
