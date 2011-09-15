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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.edgenius.core.model.User;
import com.edgenius.wiki.WikiConstants;
import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.model.Page;
import com.edgenius.wiki.model.PageComment;
import com.edgenius.wiki.model.Space;
import com.edgenius.wiki.util.WikiUtil;
import com.google.gdata.client.blogger.BloggerService;
import com.google.gdata.data.Category;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Feed;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextContent;
import com.google.gdata.util.ServiceException;

/**
 * @author Dapeng.Ni
 */
public class BloggerProxy extends AbstractBlogProxy {

	private static final String METAFEED_URL = "http://www.blogger.com/feeds/default/blogs";

	private static final String FEED_URI_BASE = "http://www.blogger.com/feeds";

	private static final String POSTS_FEED_URI_SUFFIX = "/posts/default";

	private static final String COMMENTS_FEED_URI_SUFFIX = "/comments/default";

	BloggerService service = new BloggerService(WikiConstants.APP_NAME);

	public List<BlogMeta> getUsersBlogs(int type, String xmlrpc, String user, String password) throws BlogSyncException {
		try {
			service.setUserCredentials(user, password);
			URL feedUrl = new URL(METAFEED_URL);
			Feed resultFeed = service.getFeed(feedUrl, Feed.class);

			List<BlogMeta> blogs = new ArrayList<BlogMeta>();

			log.info("Gettting blog meta title {}", resultFeed.getTitle().getPlainText());

			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				Entry entry = resultFeed.getEntries().get(i);

				String blogId = entry.getId().split("blog-")[1];
				BlogMeta meta = new BlogMeta();
				meta.setXmlrpc(FEED_URI_BASE + "/" + blogId);
				meta.setUsername(user);
				meta.setPassword(password);
				meta.setUrl(entry.getHtmlLink().getHref());
				meta.setName(entry.getTitle().getPlainText());
			
				blogs.add(meta);
			}
			
			return blogs;
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}
	}

	public static void main(String[] args) throws BlogSyncException {
		BloggerProxy bp = new BloggerProxy();
		
		List<BlogMeta> blogs = bp.getUsersBlogs(2, null, "user", "pass");
		for (BlogMeta blogMeta : blogs) {
			//log.debug(blogMeta.getId() + ":"+ blogMeta.getName() + ":" + blogMeta.getXmlrpc());
			if("http://www.blogger.com/feeds/3857307".equals(blogMeta.getXmlrpc())){
				bp.downloadPosts("test", blogMeta, 500);
			}
		}
		
	}

	public void post(BlogMeta blog, Page page) throws BlogSyncException {

		Entry myEntry = new Entry();
		myEntry.setTitle(new PlainTextConstruct(page.getTitle()));
		myEntry.setContent(new PlainTextConstruct(getBlogContent(page)));
		String creator;
		String creatorUsername;
		if (page.getCreator() != null) {
			creator = page.getCreator().getFullname();
			creatorUsername = page.getCreator().getUsername();
		} else {
			// Anonymous
			User anony = WikiUtil.getAnonymous(userReadingService);
			creator = anony.getFullname();
			creatorUsername = anony.getUsername();

		}
		Person author = new Person(creator, null, creatorUsername);
		myEntry.getAuthors().add(author);
		myEntry.setDraft(false);

		// Ask the service to insert the new entry
		try {
			URL postUrl = new URL(blog.getXmlrpc() + POSTS_FEED_URI_SUFFIX);
			service.insert(postUrl, myEntry);
		} catch (MalformedURLException e) {
			throw new BlogSyncException(e);
		} catch (IOException e) {
			throw new BlogSyncException(e);
		} catch (ServiceException e) {
			throw new BlogSyncException(e);
		}
	}

	public void downloadComments(String spaceUname, BlogMeta blog) throws BlogSyncException {

		// Build comment feed URI and request comments on the specified post
		try {
			String commentsFeedUri = blog.getXmlrpc() + "/";// + postId +
															// COMMENTS_FEED_URI_SUFFIX;
			URL feedUrl = new URL(commentsFeedUri);
			Feed resultFeed = service.getFeed(feedUrl, Feed.class);

			// Display the results
			log.debug(resultFeed.getTitle().getPlainText());
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				Entry entry = resultFeed.getEntries().get(i);
				((TextContent) entry.getContent()).getContent().getPlainText();
				log.debug("\t" + entry.getUpdated().toStringRfc822());
			}
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}

	}

	public void downloadPosts(String spaceUname, BlogMeta blog, int limit) throws BlogSyncException {

		try {
			URL feedUrl = new URL(blog.getXmlrpc() + POSTS_FEED_URI_SUFFIX);
			Feed resultFeed = service.getFeed(feedUrl, Feed.class);

			// Print the results
			log.debug(resultFeed.getTitle().getPlainText());
			for (int i = 0; i < resultFeed.getEntries().size(); i++) {
				Entry entry = resultFeed.getEntries().get(i);
				Page page = new Page();
				page.setUnixName(entry.getId());
				log.debug("\t" + entry.getTitle().getPlainText());
			}
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}

	}

	public void postComment(BlogMeta blog, PageComment comment) throws BlogSyncException {
		// check if update or insert
		String postID = null;
		Page page = comment.getPage();
		if (page != null && page.getPageProgress() != null && page.getPageProgress().getLinkExtID() != null)
			postID = page.getPageProgress().getLinkExtID();

		// Build the comment feed URI
		String commentsFeedUri = blog.getXmlrpc() + "/" + postID + COMMENTS_FEED_URI_SUFFIX;
		try {
			URL feedUrl = new URL(commentsFeedUri);
			// Create a new entry for the comment and submit it to the
			// GoogleService
			Entry myEntry = new Entry();
			myEntry.setContent(new PlainTextConstruct(comment.getBody()));
			service.insert(feedUrl, myEntry);
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}

	}

	public void removePost(BlogMeta blog, String postID) throws BlogSyncException {
		try {
			URL feedUrl = new URL(blog.getXmlrpc() + "/" + postID);
			Entry post = service.getEntry(feedUrl, Entry.class);
			if (post != null) {
				URL deleteUrl = new URL(post.getEditLink().getHref());
				service.delete(deleteUrl);
			}
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}

	}

	public void updateCategories(String spaceUname, BlogMeta blog) throws BlogSyncException {
		try {
			Space space = spaceService.getSpaceByUname(spaceUname);
			if(space == null)
				throw new BlogSyncException("Invalid space " + spaceUname);
			
			List<BlogCategory> categories = new ArrayList<BlogCategory>();
			
			//get category from blogger
			service.setUserCredentials(blog.getUsername(), blog.getPassword());
			URL feedUrl = new URL(blog.getXmlrpc());
			Entry blogEntry = service.getEntry(feedUrl, Entry.class);
			Set<Category> set = blogEntry.getCategories();
			
			if(set != null && set.size() > 0){
				for (Category cate : set) {
					BlogCategory category = new BlogCategory();
					category.setName(cate.getLabel());
					category.setId(cate.getScheme());
					category.setParentId(null);
					category.setDescription(cate.getLabelLang());
					categories.add(category);
				}
			}
			
			saveCategories(blog, space, categories);
		} catch (Exception e) {
			throw new BlogSyncException(e);
		}
	}
}
