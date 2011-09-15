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
package com.edgenius.wiki.gwt.client.editor;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;


/**
 * @author Dapeng.Ni
 */
public class MCEEmotionsDialog  extends DialogBox implements ClickHandler{
	
	private static final String AID = "com.edgenius.wiki.render.filter.EmotionFilter";
	//This client side, only use default skin image, after render, it will switch to current skin images by RenderContextImpl.buildSkinImageTag() method.
	private static final String RENDER_RESOURCE_PATH = "skins/default/render/";
	
	private TinyMCE tiny;

	public MCEEmotionsDialog(TinyMCE tiny){
		super(tiny.getEditor());
		
		//register this dialog to Editor, then this dialog can be hide if panel switched. 
		this.addDialogListener(tiny.getEditor());
		
		this.tiny = tiny;
		this.addStyleName(Css.MCE_EMOTION_DIALOG);
		
		FlexTable func = new FlexTable();
		Image img1 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-smile.gif");
		img1.addClickHandler(this);
		
		Image img2 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-cry.gif");
		img2.addClickHandler(this);

		Image img3 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-wink.gif");
		img3.addClickHandler(this);
		
		Image img4 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-laugh.gif");
		img4.addClickHandler(this);
		
		Image img5 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-star.gif");
		img5.addClickHandler(this);
		
		Image img6 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-yes.gif");
		img6.addClickHandler(this);
		
		Image img7 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-no.gif");
		img7.addClickHandler(this);
		
		Image img8 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/smiley-lightbulb.gif");
		img8.addClickHandler(this);
		
		Image img11 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/yes.png");
		img11.addClickHandler(this);
		Image img12 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/no.png");
		img12.addClickHandler(this);
		Image img13 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/warning.png");
		img13.addClickHandler(this);
		Image img14 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/cake.png");
		img14.addClickHandler(this);
		Image img15 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/bell.png");
		img15.addClickHandler(this);
		Image img16 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/award.png");
		img16.addClickHandler(this);
		Image img17 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/bulletgreen.png");
		img17.addClickHandler(this);
		Image img18 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/bulletred.png");
		img18.addClickHandler(this);
		
		Image img21 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/flaggreen.png");
		img21.addClickHandler(this);
		Image img22 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/flagyellow.png");
		img22.addClickHandler(this);
		Image img23 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/flagred.png");
		img23.addClickHandler(this);
		Image img24 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/idea.png");
		img24.addClickHandler(this);
		Image img25 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/flash.png");
		img25.addClickHandler(this);
		Image img26 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/dollar.png");
		img26.addClickHandler(this);
		Image img27 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/magnifier.png");
		img27.addClickHandler(this);
		Image img28 = new Image(GwtClientUtils.getBaseUrl()+RENDER_RESOURCE_PATH+"emotions/lock.png");
		img28.addClickHandler(this);
	
		
		func.setWidget(0, 0, img1);
		func.setWidget(0, 1, img2);
		func.setWidget(0, 2, img3);
		func.setWidget(0, 3, img4);
		func.setWidget(0, 4, img5);
		func.setWidget(0, 5, img6);
		func.setWidget(0, 6, img7);
		func.setWidget(0, 7, img8);
		func.setWidget(1, 0, img11);
		func.setWidget(1, 1, img12);
		func.setWidget(1, 2, img13);
		func.setWidget(1, 3, img14);
		func.setWidget(1, 4, img15);
		func.setWidget(1, 5, img16);
		func.setWidget(1, 6, img17);
		func.setWidget(1, 7, img18);
		func.setWidget(2, 0, img21);
		func.setWidget(2, 1, img22);
		func.setWidget(2, 2, img23);
		func.setWidget(2, 3, img24);
		func.setWidget(2, 4, img25);
		func.setWidget(2, 5, img26);
		func.setWidget(2, 6, img27);
		func.setWidget(2, 7, img28);
		
		func.setCellSpacing(8);
		this.setWidget(func);
		this.setText(Msg.consts.insert_smiley());
	}

	public void onClick(Widget sender) {
	}

	public void onClick(ClickEvent event) {
		String url = ((Image)event.getSource()).getUrl();
		String imgHtml = "<img aid=\""+AID+"\" src=\""+url+"\">";
		tiny.insertContent(imgHtml);
		this.hidebox();
	}

}
