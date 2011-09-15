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
package com.edgenius.wiki.blogsync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogPostMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.PageContent;
import com.edgenius.wiki.model.PageProgress;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.service.CommentException;
import com.edgenius.wiki.service.DuplicatedPageException;
import com.edgenius.wiki.service.SpaceNotFoundException;
import com.edgenius.wiki.util.CommentComparator;
import com.edgenius.wiki.util.WikiUtil;

/**
 * @author Dapeng.Ni
 */
public class WordpressProxy extends AbstractBlogProxy{
	//some commands need appKey - maybe need as input parameter later 
	private static final int APP_KEY = 1;

	
	public List<BlogMeta> getUsersBlogs(int type, String xmlrpc, String user, String pass) throws BlogSyncException {
		String command = null;
		Object[] params = null;
		if(StringUtils.isBlank(xmlrpc))
			return null;
		
		//refine xmlrpc URL which is from user input
		//this is only require in this method, beside this method, all URL is correct one which retrieve from this response of this request.
		xmlrpc = xmlrpc.toLowerCase();
		if(!xmlrpc.startsWith("http://")
				|| xmlrpc.startsWith("https://"))
			xmlrpc = "http://" + xmlrpc;
		
		switch (type) {
		case BlogMeta.TYPE_WORDPRESS:
			command = "metaWeblog.getUsersBlogs";
			params = new Object[] { APP_KEY, user, pass };
			if(!xmlrpc.endsWith("/xmlrpc.php")){
				if(!xmlrpc.endsWith("/"))
					xmlrpc = xmlrpc + "/";
				xmlrpc += "xmlrpc.php";
			}
			break;
		default:
			log.error("Unsupported blog type {}", type);
			return null;
		}

		if (command == null) {
			return null;
		}
		List<BlogMeta> blogs = new ArrayList<BlogMeta>();
		
		Object rs= callService(xmlrpc, command, params);
		if(rs instanceof Object[]){
			 Object[] val = (Object[]) rs;
			for (Object obj : val) {
				BlogMeta meta = new BlogMeta();
				Map<String, Object> map = (Map<String, Object>) obj;
				meta.setType(type);
				meta.setId((String) map.get("blogid"));
				meta.setXmlrpc(xmlrpc);
				meta.setUrl((String) map.get("url"));
				meta.setName((String) map.get("blogName"));
				meta.setAdmin((Boolean) map.get("isAdmin"));
				blogs.add(meta);
			}
		}
		return blogs;
	}
	
	public void post(BlogMeta blog, Page page) throws BlogSyncException{
		String command = null;
		Object[] params = null;
		Map<String, Object> struct = new HashMap<String, Object>();
		
		//check if update or insert
		String postID = null;
		if(page.getPageProgress() != null && page.getPageProgress().getLinkExtID() != null)
			postID = page.getPageProgress().getLinkExtID();
		
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				if(postID != null){
					command = "metaWeblog.editPost";
					params = new Object[] { postID , blog.getUsername(), blog.getPassword(), struct};
				}else{
					command = "metaWeblog.newPost";
					params = new Object[] { blog.getId(), blog.getUsername(), blog.getPassword(), struct};
				}
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		
		BlogPostMeta meta = null;
		if(page.getPageProgress() != null){
			meta = page.getPageProgress().getLinkPostExtInfo(blog.getKey());
		}
		struct.put("title", page.getTitle());
		
		String content = getBlogContent(page);
		struct.put("description", content);
		if(meta != null){
			if(meta.getCategories() != null)
				struct.put("categories", meta.getCategories());
			if(!StringUtils.isBlank(meta.getExcerpt()))
				struct.put("mt_excerpt", meta.getExcerpt());
		}
		//it is ok to separate by comma - wordpress can split them.
		if(!StringUtils.isBlank(page.getTagString()))
			struct.put("mt_keywords", page.getTagString());
		//TODO: draft
		struct.put("post_status", "publish");
//		struct.put("date_created_gmt", toGTM(page.getCreatedDate()));
		
		
		Object val = callService(blog.getXmlrpc(), command, params);
		//please note: insert return postID(String), update return boolean
		if(val != null && page.getPageProgress() != null && page.getPageProgress().getLinkExtID() == null){
			//new post - update postID from return
			page.getPageProgress().setLinkExtID((String) val);
			pageService.saveOrUpdatePageProgress(page.getPageProgress());
		}
	}
	public void removePost(BlogMeta blog, String postID) throws BlogSyncException{
		String command = null;
		Object[] params = null;
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				command = "metaWeblog.deletePost";
				params = new Object[] {APP_KEY, postID,blog.getUsername(), blog.getPassword()};
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		
		Object val = callService(blog.getXmlrpc(), command, params);
		log.info("Remove post {} return {}", postID,val);
		
	}
	public void downloadPosts(String spaceUname, BlogMeta blog, int limit) throws BlogSyncException {
		String command = null;
		Object[] params = null;
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				command = "metaWeblog.getRecentPosts";
				params = new Object[] { blog.getId(), blog.getUsername(), blog.getPassword(), limit };
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		
		Object rs = callService(blog.getXmlrpc(), command, params);
		if(rs instanceof Object[]){
			 Object[] val = (Object[]) rs;
			for (Object obj : val) {
				Map<String, Object> map = (Map<String, Object>) obj;
				log.debug("{} gets blog post {}", blog.getXmlrpc(), map);
				
				String status = (String) map.get("post_status");
				if(!"publish".equalsIgnoreCase(status)){
					//draft? - please ensure - draft has not wp_slug!
					continue;
				}
				BlogPostMeta postMeta = new BlogPostMeta(); 
				//TODO: check categories - if it does not exist in SpaceSetting, then add new to SpaceSetting.
				List<Object> ca = Arrays.asList((Object[]) map.get("categories"));
				postMeta.setCategories(ca.toArray(new String[ca.size()]));
				postMeta.setExcerpt((String) map.get("mt_excerpt"));
				
				String pageTitle = (String) map.get("title");
				String content = (String) map.get("description");
				String tags = (String) map.get("mt_keywords");
				Date createdDate = fixInputTimezone((Date) map.get("date_created_gmt"));
				
				Page page = null;
				try {
					page = pageService.getCurrentPageByTitleWithoutSecurity(spaceUname, pageTitle, false);
				} catch (SpaceNotFoundException e) {
					log.error("Unable to get page by title:" + pageTitle, e);
				}
				
				//I use wordpress export/import function test. It looks it check post title and create data as key for duplicated post 
				if(page == null  || !page.getCreatedDate().equals(createdDate)){
					//update to new version
					page = new Page();
					Space space = new Space();
					space.setUnixName(spaceUname);
					page.setSpace(space);
					page.setContent(new PageContent());
					PageProgress patt = new PageProgress();
					page.setPageProgress(patt);
				}else{
					//verify if there is content change - if no, skip it
					if(content.length() == page.getContent().getContent().length()){
						if(content.equals(page.getContent().getContent())){
							continue;
						}
					}
					page.setVersion(page.getVersion()+1);
				}
				
				page.setTagString(tags);
				page.setTitle(pageTitle);
				WikiUtil.setTouchedInfo(userReadingService, page);
				//update create date to original one
				page.setCreatedDate(createdDate);
				
				page.getPageProgress().addLinkExtInfoObject(postMeta);
				page.getPageProgress().setLinkExtID((String) map.get("postid"));
				String markupText = renderService.renderHTMLtoMarkup(spaceUname, content);
				page.getContent().setContent(markupText);
				try {
					pageService.savePage(page, WikiConstants.NOTIFY_NONE, true);
				} catch (DuplicatedPageException e) {
					log.warn("Blog post has duplicated title {}", page.getTitle());
				} catch (Exception e) {
					log.error("Blog post can not be saved:" + page.getTitle(),e);
				}
	
				//unused field from XMLRPC of wordpress.
	//			map.get("mt_allow_comments");
	//			map.get("permaLink");
	//			map.get("link");
	//			map.get("userid");
	//			map.get("mt_text_more");
	//			map.get("mt_allow_pings");
	//			map.get("wp_password");
	//			map.get("custom_fields");
	//			map.get("wp_author_display_name");
	//			map.get("wp_author_id");
				
			}
		}
	}

	public void postComment(BlogMeta blog, PageComment comment) throws BlogSyncException{
		String command = null;
		String callbackCommand = null;
		Object[] params = null;
		Object[] callbackParams = null;
		Map<String, Object> struct = new HashMap<String, Object>();
		
		//check if update or insert
		String commentID =comment.getSubject();
		String postID = null;
		Page page = comment.getPage();
		if(page !=null && page.getPageProgress() != null && page.getPageProgress().getLinkExtID() != null)
			postID = page.getPageProgress().getLinkExtID();
		
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				if(commentID != null){
					command = "wp.editComment";
					params = new Object[] { blog.getId() , blog.getUsername(), blog.getPassword(),commentID,  struct};
				}else if(postID != null){
					command = "wp.newComment";
					params = new Object[] { blog.getId(), blog.getUsername(), blog.getPassword(), postID, struct};
				}else{
					log.error("Unable to get correct commentID or postID:" + commentID + ":" + postID);
					return;
				}
				callbackCommand = "wp.getComment";
				
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		struct.put("content", comment.getBody());
		if(comment.getCreator() != null){
			struct.put("author", comment.getCreator().getFullname());
			struct.put("author_email", comment.getCreator().getContact().getEmail());
		}
		
		if(comment.getParent() != null){
			if(comment.getParent().getSubject() != null)
				struct.put("comment_parent", comment.getParent().getSubject());
			else{
				log.error("Unable locate blog comment hierachy. UID {}", comment.getUid());
			}
		}
		Object val = callService(blog.getXmlrpc(), command, params);
		//insert return commentID(Integer), update return boolean.
		if(commentID == null && val instanceof Integer){
			//insert new comment
			commentID = val.toString();
			comment.setSubject(commentID);
			
			//as comment use create date as key to check if comment is same one, so here must get create date back and update
			callbackParams = new Object[] { blog.getId() , blog.getUsername(), blog.getPassword(),commentID};
			Object rs = callService(blog.getXmlrpc(), callbackCommand, callbackParams);
			if(rs instanceof Map){
				Map<String, Object> map = (Map<String, Object>) rs;
				Date createdDate = fixInputTimezone((Date) map.get("date_created_gmt"));
				comment.setCreatedDate(createdDate);
				commentService.updateComment(comment);
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public void downloadComments(String spaceUname, BlogMeta blog) throws BlogSyncException {
		String command = null;
		Object[] params = null;
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				command = "wp.getComments";
				params = new Object[] { blog.getId(), blog.getUsername(), blog.getPassword()};
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null)
			throw new BlogSyncException("Invalid space " + spaceUname);
		
		Object rs = callService(blog.getXmlrpc(), command, params);
		if(rs instanceof Object[]){
			 Object[] val = (Object[]) rs;
			//organise comments from xmlrpc response into per page 
			Map<String, List<PageComment>> comments = new HashMap<String,  List<PageComment>>();
			for (Object obj : val) {
				Map<String, Object> map = (Map<String, Object>) obj;
				if(!StringUtils.equalsIgnoreCase("approve", (String) map.get("status"))){
					continue;
				}
				
				PageComment comment = new PageComment();
				comment.setBody((String) map.get("content"));

				comment.setSubject((String) map.get("comment_id"));
				Date createdDate = fixInputTimezone((Date) map.get("date_created_gmt"));
				comment.setCreatedDate(createdDate);
				String parentID = (String) map.get("parent");
				if(!StringUtils.isBlank(parentID)
					&& (NumberUtils.toInt(parentID)) != 0){
					PageComment parent = new PageComment();
					parent.setSubject(parentID);
					comment.setParent(parent);
				}
				
				String post = (String) map.get("post_id");
				List<PageComment> list = comments.get(post);
				if(list == null){
					list = new ArrayList<PageComment>();
					comments.put(post, list);
				}
				list.add(comment);
	
			}
		
			//loop again, set page information, set real parent object,can clean existed comments from list
			for (Iterator<Entry<String, List<PageComment>>> entryIter = comments.entrySet().iterator();entryIter.hasNext();) {
				Entry<String, List<PageComment>> entry = entryIter.next();
				String postID = entry.getKey();
				
				Page page = pageService.getPageByExtLinkID(spaceUname, postID);
				
				if(page == null){
					log.warn("Unable to find page by post id {}. This may cause duplicated blog title name/", postID);
					//remove whole bunch of comments which has not linked page
					entryIter.remove();
					continue;
				}
				List<PageComment> exists = null;
				try {
					exists = commentService.getPageComments(spaceUname, page.getPageUuid());
				} catch (CommentException e) {
					log.error("Get page comments failed, to avoid duplicate comments, skip save its comment",e);
					continue;
				}
				
				
				List<PageComment> list = entry.getValue();
				for (Iterator<PageComment> iter = list.iterator();iter.hasNext();) {
					PageComment comment = iter.next();
					
					//check if this comment is already exist
					if(isExistedComment(exists, comment)){
						iter.remove();
						continue;
					}
					
					if(comment.getParent() != null){
						//lookup - and set the real parent object 
						for (PageComment parent : list) {
							if(comment.getParent().equals(parent.getSubject())){
								comment.setParent(parent);
								break;
							}
						}
					}
					//set page
					comment.setPage(page);
				}
				
			}
			//loop again to ensure all parents is before their children so that below save won't trigger object is transient exception.
			//and prepare for next loop to set level etc information
			for (Entry<String, List<PageComment>> entry: comments.entrySet()) {
				List<PageComment> sortedComments = CommentComparator.getParentBeforeSortedList(entry.getValue());
				//set level
				for (PageComment comment : sortedComments) {
					if(comment.getParent() != null){
						comment.setLevel(comment.getParent().getLevel()+1);
					}
				}
				
				//reset
				comments.put(entry.getKey(),sortedComments);
			}
			
			
			//save
			for (List<PageComment> list : comments.values()) {
				for (PageComment pageComment : list) {
					try {
						commentService.createComment(spaceUname, pageComment.getPage().getPageUuid(), pageComment, WikiConstants.NOTIFY_NONE);
					} catch (CommentException e) {
						log.error("Unable to save comments " + pageComment.getSubject(), e);
					}
				}
			}
		}
	}
	public void updateCategories(String spaceUname, BlogMeta blog) throws BlogSyncException{
		String command = null;
		Object[] params = null;
		switch (blog.getType()) {
			case BlogMeta.TYPE_WORDPRESS:
				command = "wp.getCategories";
				params = new Object[] { blog.getId(), blog.getUsername(), blog.getPassword()};
				break;
			default:
				log.error("Unsupported blog type {}", blog.getType());
				return;
		}
		Space space = spaceService.getSpaceByUname(spaceUname);
		if(space == null)
			throw new BlogSyncException("Invalid space " + spaceUname);
		
		
		Object rs= callService(blog.getXmlrpc(), command, params);
		if(rs instanceof Object[]){
			 Object[] val = (Object[]) rs;
			List<BlogCategory> categories = new ArrayList<BlogCategory>();
			for (Object obj : val) {
				Map<String, Object> map = (Map<String, Object>) obj;
				log.info("{} gets blog categories {}", blog.getXmlrpc(), map);
				BlogCategory category = new BlogCategory();
				//unused field from wordpress xmlrpc response:
				//htmlUrl=http://10.88.30.82:82/wordpress/?cat=5
				//categoryDescription=
				//rssUrl=http://10.88.30.82:82/wordpress/?feed=rss2&amp;cat=5
				category.setName((String) map.get("categoryName"));
				category.setId((String) map.get("categoryId"));
				category.setParentId((String) map.get("parentId"));
				category.setDescription((String) map.get("description"));
				categories.add(category);
			}
			
			saveCategories(blog, space, categories);
		}
	}


	
	//********************************************************************
	//               private methods
	//********************************************************************
	/**
	 * Return "date_create_gmt" time is gmt, but timezone is wrong - it is local timezone. 
	 * Here will correct timezone and return date in local timezone.
	 * @param date
	 * @return
	 */
	private Date fixInputTimezone(Date date) {
		if(date == null)
			return new Date();//???
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.set(Calendar.YEAR, date.getYear()+1900);
		cal.set(Calendar.MONTH, date.getMonth());
		cal.set(Calendar.DATE, date.getDate());
		cal.set(Calendar.HOUR_OF_DAY, date.getHours());
		cal.set(Calendar.MINUTE, date.getMinutes());
		cal.set(Calendar.SECOND, date.getSeconds());
		
		Calendar local = Calendar.getInstance(TimeZone.getDefault());
		local.setTimeInMillis(cal.getTimeInMillis());
		return local.getTime();
	}
}
