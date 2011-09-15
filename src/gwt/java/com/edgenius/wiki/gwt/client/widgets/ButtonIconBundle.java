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

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Image;

/**
 * For http://groups.google.com/group/Google-Web-Toolkit/browse_thread/thread/cf6bf23f13d84de5 issue,
 * I have to create ImageBundle for each image. Using ImageBundle will auto transparent to PNG image.
 * 
 * @author Dapeng.Ni
 */
public class ButtonIconBundle {
//	public class I{
//		private static ButtonIconBundle iconBundle;
//		public static ButtonIconBundle get(){
//			if(iconBundle == null)
//				iconBundle = (ButtonIconBundle) GWT.create(ButtonIconBundle.class);
//			return iconBundle;
//		}
//	}
	interface I1 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/admin.png")
		public ImageResource admin();
	}
	
	public static Image adminImage(){
		I1 iconBundle = (I1) GWT.create(I1.class);
		return new Image(iconBundle.admin());
	}
	interface I2 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/create.png")
		public ImageResource create();
	}
	
	public static Image createImage(){
		I2 iconBundle = (I2) GWT.create(I2.class);
		return new Image(iconBundle.create());
	}
	interface I3 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/login.png")
		public ImageResource login();
	}
	

	public static Image login() {
		I3 iconBundle = (I3) GWT.create(I3.class);
		return new Image(iconBundle.login());
	}

	
	interface I4 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_undo.png")
		public ImageResource arrow_undo();
	}
	
	public static Image arrow_undoImage(){
		I4 iconBundle = (I4) GWT.create(I4.class);
		return new Image(iconBundle.arrow_undo());
	}
	interface I5 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/cross.png")
		public ImageResource cross();
	}
	
	public static Image crossImage(){
		I5 iconBundle = (I5) GWT.create(I5.class);
		return new Image(iconBundle.cross());
	}
	interface I6 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/edit.png")		
		public ImageResource edit();
	}
	
	public static Image editImage(){
		I6 iconBundle = (I6) GWT.create(I6.class);
		return new Image(iconBundle.edit());
	}
	interface I7 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/group.png")
		public ImageResource group();
	}
	
	public static Image groupImage(){
		I7 iconBundle = (I7) GWT.create(I7.class);
		return new Image(iconBundle.group());
	}
	interface I8 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/user.png")		
		public ImageResource user();
	}
	
	public static Image userImage(){
		I8 iconBundle = (I8) GWT.create(I8.class);
		return new Image(iconBundle.user());
	}
	interface I9 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/disk.png")		
		public ImageResource disk();
	}
	
	public static Image diskImage(){
		I9 iconBundle = (I9) GWT.create(I9.class);
		return new Image(iconBundle.disk());
	}
	interface I10 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/savedraft.png")			
		public ImageResource savedraft();
	}
	
	public static Image savedraftImage(){
		I10 iconBundle = (I10) GWT.create(I10.class);
		return new Image(iconBundle.savedraft());
	}
	interface I11 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/eye.png")			
		public ImageResource eye();
	}
	
	public static Image eyeImage(){
		I11 iconBundle = (I11) GWT.create(I11.class);
		return new Image(iconBundle.eye());
	}
	interface I12 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/menu.png")			
		public ImageResource menu();
	}
	
	public static Image menuImage(){
		I12 iconBundle = (I12) GWT.create(I12.class);
		return new Image(iconBundle.menu());
	}
	interface I13 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/book_open.png")			
		public ImageResource book_open();
	}
	
	public static Image book_openImage(){
		I13 iconBundle = (I13) GWT.create(I13.class);
		return new Image(iconBundle.book_open());
	}
	interface I14 extends ClientBundle{

		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/tick.png")			
		public ImageResource tick();
	}
	
	public static Image tickImage(){
		I14 iconBundle = (I14) GWT.create(I14.class);
		return new Image(iconBundle.tick());
	}

	interface I15 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/restore.png")			
		public ImageResource restore();
	}

	public static Image resore() {
		I15 iconBundle = (I15) GWT.create(I15.class);
		return new Image(iconBundle.restore());
	}

	interface I16 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/diff.png")			
		public ImageResource diff();
	}
	public static Image diff() {
		I16 iconBundle = (I16) GWT.create(I16.class);
		return new Image(iconBundle.diff());
	}
	
	interface I17 extends ClientBundle{
		@Source("com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/email.png")			
		public ImageResource email();
	}
	public static Image email() {
		I17 iconBundle = (I17) GWT.create(I17.class);
		return new Image(iconBundle.email());
	}
	//********************************************************************
	//               ICON
	//********************************************************************
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/admin.png
//	 */
//	public ImageResource admin();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/create.png
//	 */
//	public ImageResource create();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_right.png
//	 */
//	public ImageResource arrow_right();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/arrow_undo.png
//	 */
//	public ImageResource arrow_undo();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/cross.png
//	 */
//	public ImageResource cross();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/edit.png
//	 */
//	public ImageResource edit();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/group.png
//	 */
//	public ImageResource group();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/user.png
//	 */
//	public ImageResource user();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/disk.png
//	 */
//	public ImageResource disk();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/savedraft.png
//	 */
//	public ImageResource savedraft();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/eye.png
//	 */
//	public ImageResource eye();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/menu.png
//	 */
//	public ImageResource menu();
//	/**
//	 * @gwt.resource com/edgenius/wiki/gwt/public/resources/images/geniuswiki/default/icons/book_open.png
//	 */
//	public ImageResource book_open();
}
