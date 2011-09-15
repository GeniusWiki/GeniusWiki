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
package com.edgenius.wiki.gwt.client.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.edgenius.wiki.gwt.client.model.BlogCategory;
import com.edgenius.wiki.gwt.client.model.BlogMeta;
import com.edgenius.wiki.gwt.client.model.BlogPostMeta;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class BlogExtraInfoPanel extends Composite {

	@UiTemplate("wordpress-info.ui.xml")
	interface WrodpressUiBinder extends UiBinder<Widget, WordpressExtraInfoPanel> {}
	private static WrodpressUiBinder wpBinder = GWT.create(WrodpressUiBinder.class);
	
	@UiTemplate("blogger-info.ui.xml")
	interface BloggerUiBinder extends UiBinder<Widget, BloggerExtraInfoPanel> {}
	private static BloggerUiBinder bgBinder = GWT.create(BloggerUiBinder.class);
	
	private List<AbstractBlogExtraInfoPanel> panels = new ArrayList<AbstractBlogExtraInfoPanel>();
	
	public BlogExtraInfoPanel(Collection<BlogMeta> linkedBlogs) {

		VerticalPanel main = new VerticalPanel();
		for (BlogMeta blog : linkedBlogs) {
			Widget panel = new FlowPanel();
			switch(blog.getType()){
				case 1: 
					WordpressExtraInfoPanel wp = new WordpressExtraInfoPanel();
					panel = wpBinder.createAndBindUi(wp);
					wp.setBlogMeta(blog);
					panels.add(wp);
					break;
				case 2: 
					BloggerExtraInfoPanel bg = new BloggerExtraInfoPanel();
					panel = bgBinder.createAndBindUi(bg);
					bg.setBlogMeta(blog);
					panels.add(bg);
					break;
			}
			main.add(panel);
		}
		this.initWidget(main);
	}
	/**
	 * @return
	 */
	public List<BlogMeta> getValues() {
		List<BlogMeta> list = new ArrayList<BlogMeta>(); 
		for (AbstractBlogExtraInfoPanel panel : panels) {
			list.add(panel.getValue());
		}
			
		return list;
	}
	/**
	 * @return 
	 */
	public boolean hasNotify() {
		for (AbstractBlogExtraInfoPanel panel : panels) {
			if(panel.isNotify())
				return true;
		}
		return false;
	}
	//********************************************************************
	//               private classes
	//********************************************************************
	abstract static class AbstractBlogExtraInfoPanel extends Composite{
		public abstract BlogMeta getValue();
		public abstract boolean isNotify();
		public abstract void setBlogMeta(BlogMeta blog);
	}
	static class BloggerExtraInfoPanel extends AbstractBlogExtraInfoPanel {
		@UiField CheckBox notify;
		
		@Override
		public BlogMeta getValue() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isNotify() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setBlogMeta(BlogMeta blog) {
			// TODO Auto-generated method stub
			
		}
		
	}
	static class WordpressExtraInfoPanel extends AbstractBlogExtraInfoPanel {

		@UiField TextArea excerptBox;
		@UiField VerticalPanel categoriesPanel;
		@UiField CheckBox notify;
		
		private List<CheckBox> categories = new ArrayList<CheckBox>();
		private String blogKey;
		
		public void setBlogMeta(BlogMeta blog){
			BlogPostMeta value = blog.getPostValue();
			String[] selectedCategories = null;
			if(value != null){
				selectedCategories = value.getCategories();
				notify.setValue(value.isSync());
				excerptBox.setText(value.getExcerpt());
			}
			
			List<BlogCategory> linkBlogCategories = blog.getCategories();
			if(linkBlogCategories != null){
				for (BlogCategory category : linkBlogCategories) {
					CheckBox box = new CheckBox(category.getName());
					box.setFormValue(category.getName());
					if(StringUtil.containsIgnoreCase(selectedCategories, category.getName())){
						box.setValue(true);
					}
					categoriesPanel.add(box);
					categories.add(box);
				}
			}
			blogKey = blog.getKey();
		}
		
		public BlogMeta getValue(){
			BlogMeta blog = new BlogMeta();
			BlogPostMeta post = new BlogPostMeta();
			blog.setPostValue(post);
			blog.setType(1);
			
			//blog key
			post.setBlogKey(blogKey);
			//excerpt
			post.setExcerpt(excerptBox.getText());
			
			//selected categories
			List<String> cateList = new ArrayList<String>();
			for(CheckBox box:categories){
				if(box.getValue()){
					cateList.add(box.getFormValue());
				}
			}
			
			post.setCategories(cateList.toArray(new String[cateList.size()]));
			
			return blog;
		}

		@Override
		public boolean isNotify() {
			return notify.getValue();
		}

	}

}
