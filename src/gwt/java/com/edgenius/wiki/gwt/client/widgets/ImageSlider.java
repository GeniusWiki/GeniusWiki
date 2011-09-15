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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Dapeng.Ni
 */
public class ImageSlider extends SimplePanel implements ClickHandler{
	private static int IMAGE_WIDTH_DEFAULT = 180;
	private static int IMAGE_HEIGHT_DEFAULT = 80;
	private static int HEIGHT_DEFAULT = 100;
	private static int SHOW_DEFAULT = 2;
	
	private int showCount;
	private boolean showAltText;
	private int imageWidth;
	private int imageHeight;
	private int sliderHeight;
	
	private HorizontalPanel rowPanel = new HorizontalPanel();
	private LinkedHashMap<String, Image> images;
	private List<ImageSlideListener> listeners = new ArrayList<ImageSlideListener>();
	private String selectedID;
	private SlideButton leftBtn = new SlideButton(true,this);
	private SlideButton rightBtn = new SlideButton(false,this);
	private int pageNumber = 0;
	//true, then show AltText under image as text bar.
	
	public ImageSlider(){
		this(false,HEIGHT_DEFAULT,IMAGE_WIDTH_DEFAULT,IMAGE_HEIGHT_DEFAULT,SHOW_DEFAULT);
	}
	public ImageSlider(boolean showAltText){
		this(showAltText,HEIGHT_DEFAULT,IMAGE_WIDTH_DEFAULT,IMAGE_HEIGHT_DEFAULT,SHOW_DEFAULT);
	}
	public ImageSlider(boolean showAltText, int sliderHeight, int imageWidth, int imageHeight, int showPerPage){
		this.sliderHeight = sliderHeight;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.showCount = showPerPage;
		this.showAltText = showAltText;
		
		HorizontalPanel main = new HorizontalPanel();
		main.add(leftBtn);
		main.add(rowPanel);
		main.add(rightBtn);
		main.setCellHorizontalAlignment(rowPanel, HasHorizontalAlignment.ALIGN_CENTER);
		
		leftBtn.setEnable(false);
		rightBtn.setEnable(false);
		
		this.setWidth("100%");
		this.setStyleName(Css.IMAGE_SLIDE);
		this.setWidget(main);
	}
	public void clear(){
		rowPanel.clear();
	}
	/**
	 * Set text message on panel, it will clear images list.
	 */
	public void setMessage(Widget message) {
		this.clear();
		rowPanel.add(message);
	}

	/**
	 * show or hide loading indicator.  
	 * 
	 * This method will clear panel.
	 * 
	 * @param showLoading
	 */
	public void showLoading(boolean showLoading){
		this.clear();
		
		if(showLoading){
			Image loading = new Image(IconBundle.I.get().loadingBar());
			rowPanel.add(loading);
			rowPanel.setCellHorizontalAlignment(loading, HasHorizontalAlignment.ALIGN_CENTER);
			rowPanel.setCellVerticalAlignment(loading, HasVerticalAlignment.ALIGN_MIDDLE);
		}
	}
	/**
	 * All all images to replace the existed images.   
	 * @param images Key(or unique name) and image widget.
	 */
	public void addImages(LinkedHashMap<String, Image> images){
		rowPanel.clear();
		this.images = images;
		
		leftBtn.setVisible(false);
		rightBtn.setVisible(false);
		rowPanel.setVisible(false);
		
		if(images == null || images.size() == 0){
			return;
		}
		if(images.size() > showCount){
			leftBtn.setVisible(true);
			rightBtn.setVisible(true);
		}
		rowPanel.setVisible(true);
		
		leftBtn.setHeight(sliderHeight+"px");
		rightBtn.setHeight(sliderHeight+"px");
		rowPanel.setHeight(sliderHeight+"px");
		
		Iterator<Entry<String, Image>> iter = images.entrySet().iterator();
		for(int idx=0;idx<showCount && iter.hasNext() ;idx++){
			Entry<String, Image> entry = iter.next();
			setImageToPanel(entry.getKey(), entry.getValue());
		}
		
		for (Image image : images.values()) {
			image.addClickHandler(this);
		}
		
		if(images.size() > showCount){
			rightBtn.setEnable(true);
		}
	}

	public void addImageSlideListener(ImageSlideListener listener){
		listeners.add(listener);
	}

	public void setSelect(String key){
		selectedID = key;
		for(Iterator<Widget> iter = rowPanel.iterator();iter.hasNext();){
			//reset all image as unselected first
			Widget widget = iter.next();
			if(!(widget instanceof VerticalKeyPanel))
				continue;
			
			widget.setStyleName(Css.DESELECTED);
			
			//mark selected
			if(StringUtil.equals(key,((VerticalKeyPanel)widget).getKey())){
				widget.setStyleName(Css.SELECTED);
			}
			
		}

	}
	
	public String getSelected(){
		return selectedID;
	}
	public void onClick(ClickEvent event) {
		for (Entry<String, Image> entry : images.entrySet()) {
			if(StringUtil.equals(selectedID,entry.getKey()))
				continue;
			
			Image image = entry.getValue();
			if(image == event.getSource()){
				setSelect(entry.getKey());
				for (ImageSlideListener listener : listeners) {
					listener.selected(selectedID,image);
				}
				break;
			}
		}
	}
	/**
	 * if slide button click, then this method is execute to tell other slide button to enable
	 */
	private void slide(SlideButton sender) {
		if(sender == leftBtn){
			rightBtn.setEnable(true);
		}else{
			leftBtn.setEnable(true);
		}
	}
	/**
	 * @param entry
	 */
	private void setImageToPanel(String key, Image img) {
		VerticalKeyPanel imgPanel = new VerticalKeyPanel(key);
		img.setWidth(imageWidth +"px");
		img.setHeight(imageHeight + "px");
		imgPanel.add(img);
		if(StringUtil.equals(selectedID,key)){
			imgPanel.setStyleName(Css.SELECTED);	
		}else{
			imgPanel.setStyleName(Css.DESELECTED);
		}
		
		if(this.showAltText){
			Label alt = new Label(img.getAltText());
			alt.setStyleName(Css.TITLE);
			imgPanel.add(alt);
			imgPanel.setCellHorizontalAlignment(alt, HasHorizontalAlignment.ALIGN_CENTER);
		}
		rowPanel.add(imgPanel);
	}
	
	/**
	 * Arrow button for listing previous or next.  
	 * @author Dapeng.Ni
	 */
	private class SlideButton extends FocusPanel implements ClickHandler{
		
		private boolean left;
		private boolean enable;
		private Image arrowImg;
		private Image arrowImgDis;
		private ImageSlider slider;
		
		public SlideButton(boolean left, ImageSlider slider){
			this.slider = slider;
			this.addClickHandler(this);
			this.left = left; 
			if(left){
				arrowImg = new Image(ResultImageBundle.I.get().prev());
				arrowImgDis = new Image(ResultImageBundle.I.get().prevDisable());
			}else{
				arrowImg = new Image(ResultImageBundle.I.get().next());
				arrowImgDis = new Image(ResultImageBundle.I.get().nextDisable());
			}
			DOM.setStyleAttribute(arrowImgDis.getElement(), "padding-top", (sliderHeight/2-10)+"px");
			DOM.setStyleAttribute(arrowImg.getElement(), "padding-top", (sliderHeight/2-10)+"px");
			this.setStyleName(Css.IMAGE_SLIDE_BTN);
			this.setWidget(arrowImgDis);
		}
		
		public void setEnable(boolean enable) {
			this.enable = enable;
			this.clear();
			if(enable){
				this.setStyleName(Css.ENABLE);
				this.setWidget(arrowImg);
			}else{
				this.setStyleName(Css.DISABLE);
				this.setWidget(arrowImgDis);
			}
		}
		public void onClick(ClickEvent event) {
			if(!this.enable){
				return;
			}
			if(!left){
				pageNumber++;
				
				int start = pageNumber * showCount;
				if(images.size() > start){
					slider.slide(this);
					fillImages();
				}
				if(images.size() <= (start+showCount)){
					setEnable(false);
				}
			}else{
				if(pageNumber == 0)
					return;
				
				pageNumber--;
				
				fillImages();
				slider.slide(this);
				if(pageNumber == 0){
					setEnable(false);
				}
			}
		}

		/**
		 * @return
		 */
		private void fillImages() {
			int start = pageNumber * showCount;
			int pointer=0,idx=0;
			rowPanel.clear();
			for (Entry<String, Image> entry : images.entrySet()) {
				pointer++;
				if(pointer <= start)
					continue;
				if(idx >= showCount)
					break;
				
				setImageToPanel(entry.getKey(), entry.getValue());
				idx++;
			}
		}
		
	}
}
