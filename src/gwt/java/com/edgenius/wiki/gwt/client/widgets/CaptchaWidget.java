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

import java.util.Date;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class CaptchaWidget extends SimplePanel{

	private VerticalPanel panel = new VerticalPanel();
	private TextBox captchaInput = new TextBox();
	
	//if page has more than one captcha image, this uid will helpful during refresh update.
	private String uid = HTMLPanel.createUniqueId();
	public CaptchaWidget(){
		panel.setSize("100%", "100%");
		captchaInput.setStyleName(Css.FORM_INPUT);
		this.setWidget(panel);
	}

	/**
	 * 
	 */
	public void refresh() {
		Date now = new Date();
		//must give a parameter name "refresh=", otherwise, ognl throw NumberFormatExcetpion!
		DOM.setElementAttribute(DOM.getElementById("captchaImg"+uid),"src",GwtClientUtils.getBaseUrl() +"captcha.do?refresh="+now.getTime());
		captchaInput.setText("");
		captchaInput.setFocus(true);
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		
		return (panel.getWidgetCount() > 0)?true:false;
	}

	/**
	 * 
	 */
	public void enable() {
		//DONOT user visible or invisible to switch enable/disable
		panel.clear();
		Date now = new Date();
		//must give a parameter name "refresh=", otherwise, ognl throw NumberFormatExcetpion!
		final HTML captcha = new HTML("<image id='captchaImg"+uid+ "' src='"+GwtClientUtils.getBaseUrl() +"captcha.do?refresh="+now.getTime()+"'>");
		ClickLink refreshCaptcha = new ClickLink(Msg.consts.refresh_image());
		refreshCaptcha.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				refresh();
			}
		});
		
		captchaInput.setName("j_captcha_response");
//		Label desc = new Label("input are lower case");
		panel.add(captcha);
		panel.add(refreshCaptcha);
		panel.add(captchaInput);
//		panel.add(desc);
		panel.setCellHorizontalAlignment(captcha, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(refreshCaptcha, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(captchaInput, HasHorizontalAlignment.ALIGN_CENTER);
//		panel.setCellHorizontalAlignment(desc, HasHorizontalAlignment.ALIGN_CENTER);
	}

	/**
	 * 
	 */
	public void disable() {
		panel.clear();
		
	}

	/**
	 * @return
	 */
	public String getCaptchaInput() {
		return captchaInput.getText();
	}
	
	/**
	 * @return, for Enter check
	 */
	public TextBox getCaptchaInputWidget() {
		return captchaInput;
	}
	
}
