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
package com.edgenius.wiki.gwt.client.widgets;

import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Dapeng.Ni
 */
public interface IconBundle extends ClientBundle {
	public class I{
		private static IconBundle iconBundle;
		public static IconBundle get(){
			if(iconBundle == null)
				iconBundle = (IconBundle) GWT.create(IconBundle.class);
			return iconBundle;
		}
		/*
		 * Loading image is different size with others, put it here
		 */
		public static Image loading(){
			return new Image(GwtClientUtils.getBaseUrl()+"static/images/large-loading.gif");
		}
		public static Image indicator(){
			return new Image(GwtClientUtils.getBaseUrl()+"static/images/indicator.gif");
		}
		public static Image barIndicator(){
			return new Image(GwtClientUtils.getBaseUrl()+"static/images/barindicator.gif");
		}

	}
	
	
	//********************************************************************
	//               General
	//********************************************************************
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/loadingbar.gif")
	public ImageResource loadingBar();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/close.png")
	public ImageResource close();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/closegrey.png")
	public ImageResource closeDisable();
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/refresh.png")
	public ImageResource refresh();

	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/group.png")
	public ImageResource group();
	//********************************************************************
	//               Home Dashboard
	//********************************************************************
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/feed.png")
	public ImageResource feed();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/export.png")
	public ImageResource export();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/edit.png")
	public ImageResource edit();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/pin.png")
	public ImageResource pin();
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/pin_dis.png")
	public ImageResource pin_dis();
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/pin_small.png")
	public ImageResource pin_small();

	
	//********************************************************************
	//               Attachment collapse/expand
	//********************************************************************
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_close.png")
	public ImageResource closeArrow();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_open.png")
	public ImageResource openArrow();
	//********************************************************************
	//               Attachment/tree/help
	//********************************************************************
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/attach.png")
	public ImageResource attach();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/tree.png")
	public ImageResource tree();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/help.png")
	public ImageResource help();
	//********************************************************************
	//               Message
	//********************************************************************
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/warning.png")
	public ImageResource warning();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/error.png")
	public ImageResource error();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/info.png")
	public ImageResource info();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/check.png")
	public ImageResource success();

	//********************************************************************
	//               Favorite / Watch on view page
	//********************************************************************
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/favorite.png")
	public ImageResource favorite();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/nonfavorite.png")
	public ImageResource nonfavorite();
	

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/watch.png")
	public ImageResource watch();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/nonwatch.png")
	public ImageResource nonwatch();
	//********************************************************************
	//               Others
	//********************************************************************
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/printer.png")
	public ImageResource printer();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/comment.png")
	public ImageResource comment();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/expand.png")
	public ImageResource expand();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/collapse.png")
	public ImageResource collapse();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_up.png")
	public ImageResource upload();
	//********************************************************************
	//               Security
	//********************************************************************
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/tick.png")
	public ImageResource tick();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/cross.png")
	public ImageResource cross();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/bin_closed.png")
	public ImageResource bin_close();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/draft.png")
	public ImageResource draft();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/message.png")
	public ImageResource message();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/star.png")
	public ImageResource star();
	
	
	//********************************************************************
	//              Dialog
	//********************************************************************
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/application_add.png")
	public ImageResource application_add();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/lock.png")
	public ImageResource lock();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/lock_add.png")
	public ImageResource lock_add();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/wand.png")
	public ImageResource wand();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/email.png")
	public ImageResource email();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/disconnect.png")
	public ImageResource disconnect();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/connect.png")
	public ImageResource connect();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/connect_error.png")
	public ImageResource connect_error();
	
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/page_copy.png")
	public ImageResource page_copy();
	
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/page_go.png")
	public ImageResource page_go();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/bullet_red.png")
	public ImageResource bullet_red();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/bullet_green.png")
	public ImageResource bullet_green();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/bullet_yellow.png")
	public ImageResource bullet_yellow();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/history.png")
	public ImageResource history();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/new_window.png")
	public ImageResource new_window();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/home_link.png")
	public ImageResource home_link();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_right.png")
	public ImageResource arrow_right();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_left.png")
	public ImageResource arrow_left();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/home.png")
	public ImageResource home();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/calendar.png")
	public ImageResource calendar();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/note.png")
	public ImageResource note();
	
	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/note_edit.png")
	public ImageResource note_edit();

	@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/shell.png")
	public ImageResource shell();
	
}

