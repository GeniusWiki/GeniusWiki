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
package com.edgenius.wiki.gwt.client.space;

import java.util.LinkedHashMap;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.ThemeListModel;
import com.edgenius.wiki.gwt.client.model.ThemeModel;
import com.edgenius.wiki.gwt.client.server.ThemeControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.ErrorCode;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.ImageSlider;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Dapeng.Ni
 */
public class ThemeListPanel extends SimplePanel implements AsyncCallback<ThemeListModel>{

	
	private DialogBox targetDlg;
	private ImageSlider slider = new ImageSlider(true);
	public ThemeListPanel(){
		
		this.add(slider);
		this.setStyleName(Css.THEME_LIST);
		
		slider.showLoading(true);
		
	}

	public void load(String spaceUname) {
		ThemeControllerAsync themeController = ControllerFactory.getThemeController();
		themeController.getAvailableTheme(spaceUname,this);
	}
	
	public void onFailure(Throwable error) {
		slider.showLoading(false);
		GwtClientUtils.processError(error);
	}
	public void onSuccess(ThemeListModel model) {
		slider.showLoading(false);
		
		if(ErrorCode.hasError(model)){
			MessageWidget message = new MessageWidget();
			slider.setMessage(message);
			if(!GwtClientUtils.preSuccessCheck(model,message)){
				return;
			}
		}
		if(model.themeList != null && model.themeList.size() > 0){
			LinkedHashMap<String, Image> images = new LinkedHashMap<String, Image>();
			
			for(ThemeModel tModel: model.themeList){
				Image image = new Image(tModel.previewImageName);
				image.setAltText(tModel.title);
				image.setTitle(tModel.description);
				
				images.put(tModel.name, image);
				//initial select - first if no selected.
				if(model.chosenThemeName == null){
					model.chosenThemeName = tModel.name;
				}
			}
			slider.addImages(images);
			slider.setSelect(model.chosenThemeName);
		}else{
			slider.setMessage(new Label(Msg.consts.no_theme()));
		}
		
		if(targetDlg != null){
			if(targetDlg.isShowing())
				targetDlg.center();
		}
	}
	/**
	 * @param dlg
	 */
	public void setTarget(DialogBox dlg) {
		this.targetDlg = dlg;
	}

	/**
	 * @return
	 */
	public String getSelected() {
		return slider.getSelected();
	}
}
