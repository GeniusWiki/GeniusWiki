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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.html.ImageModel;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.AttachmentModel;
import com.edgenius.wiki.gwt.client.page.PageMain;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentListener;
import com.edgenius.wiki.gwt.client.page.widgets.AttachmentPanel;
import com.edgenius.wiki.gwt.client.server.constant.PageType;
import com.edgenius.wiki.gwt.client.server.utils.NameConstants;
import com.edgenius.wiki.gwt.client.server.utils.RichTagUtil;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.ClickLink;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.ImageSlideListener;
import com.edgenius.wiki.gwt.client.widgets.ImageSlider;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.edgenius.wiki.gwt.client.widgets.UploadDialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class MCEImageDialog extends MCEDialog implements ImageSlideListener, AttachmentListener{
	
	private HintTextBox box = new HintTextBox(Msg.consts.attach_url());
	private TextBox titleBox = new TextBox();
	private MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
	private SuggestBox suggest = new SuggestBox(oracle,box);
	private String spaceUname;
	private MessageWidget message = new MessageWidget();
	private ImageSlider slider = new ImageSlider();
	
	public MCEImageDialog(final TinyMCE tiny){
		super(tiny);
		
		this.spaceUname = tiny.getEditor().getSpaceUname();

		this.setText(Msg.consts.insert_image());
		Label label = new Label(Msg.consts.image());
		Label titleL = new Label(Msg.consts.title());
		
		FlexTable func = new FlexTable();
		func.setWidget(0, 0, label);
		func.setWidget(0, 1, suggest);
		func.setWidget(1, 0, titleL);
		func.setWidget(1, 1, titleBox);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(func);
		
		panel.add(slider);
		slider.addImageSlideListener(this);
		panel.setCellHorizontalAlignment(slider, HasHorizontalAlignment.ALIGN_CENTER);
		
		refreshSlider();
		
		panel.setSize("100%", "100%");
		func.setSize("100%", "100%");
		label.setStyleName(Css.FORM_LABEL);
		titleL.setStyleName(Css.FORM_LABEL);
		box.setStyleName(Css.FORM_INPUT);
		titleBox.setStyleName(Css.FORM_INPUT);
		
		final AttachmentPanel attachmentPanel = tiny.getEditor().getAttachmentPanel();
		if(attachmentPanel != null && !PageMain.isAnonymousLogin()){
			adoptElement(attachmentPanel.getPanelID());
			attachmentPanel.addAttachmentListener(this);
			
			//allow upload image
			ClickLink uploadImg = new ClickLink(Msg.consts.upload());
			uploadImg.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					UploadDialog dialog = new UploadDialog(tiny.getEditor().getAttachmentPanel(), PageMain.getSpaceUname(), PageMain.getPageUuid(), PageType.AUTO_DRAFT);
					dialog.showbox();
				};
			});
			panel.add(uploadImg);
			panel.setCellHorizontalAlignment(uploadImg, HasHorizontalAlignment.ALIGN_RIGHT);
		}
		this.setWidget(panel);
		//don't set focus as it will erase the hint text.
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//			public void execute() {
//				suggest.setFocus(true);
//			}
//		});
	}
	public void hidebox(){
		//avoid memory leak
		AttachmentPanel attPanel = tiny.getEditor().getAttachmentPanel();
		if(attPanel != null)
			attPanel.removeAttachmentListener(this);
		
		super.hidebox();
	}
	public void initField(String src, String wajax, String title){
		if(!StringUtil.isBlank(src) &&( src.startsWith("http://") || src.startsWith("https://")))
			box.setText(src);
		else if(!StringUtil.isBlank(wajax)){
			Map<String,String> wmap = RichTagUtil.parseWajaxAttribute(wajax);
			String filename = wmap.get(NameConstants.FILENAME);
			if(!StringUtil.isBlank(filename)){
				box.setText(filename);
			}
		}
			
		if(!StringUtil.isBlank(title))
			titleBox.setText(title);
	}
	
	public void selected(String name, Image image) {
		//is it good if directly close dialog and insert image or like this?
		box.setText(name);
		titleBox.setText(image.getTitle());
	}
	
	@Override
	protected void okEvent() {
		String src = box.getText();
		src = StringUtil.trim(src);
		ImageModel image = new ImageModel();
		
		tiny.restoreEditorBookmark();
		if(!src.toLowerCase().startsWith("http://") && !src.toLowerCase().startsWith("https://")){
			AttachmentModel node = findNode(src);
			if(node != null){
				image.url = GwtClientUtils.buildAttachmentURL(spaceUname,node.filename, node.nodeUuid, null,false);
				image.filename = src;
				image.title  = titleBox.getText();
				tiny.insertContent(image.toRichAjaxTag());
				this.close();
			}else{
				message.warning(Msg.consts.no_attachment_by_name());
			}
		}else{
			image.url = src;
			//TODO: special character handle
			image.title  = titleBox.getText();
			tiny.insertContent(image.toRichAjaxTag());
			this.close();
		}
		
	}
	private void refreshSlider() {
		List<AttachmentModel> atts = tiny.getEditor().getAttachmentList();
		List<String> attachs = new ArrayList<String>();
		LinkedHashMap<String, Image> images = new LinkedHashMap<String, Image>();

		if(atts != null){
			for (AttachmentModel attModel : atts) {
				//filter out non-image
				boolean isImage = false;
				for (String ext : SharedConstants.IMAGE_FILTERS) {
					if(attModel.filename.trim().toLowerCase().endsWith(ext)){
						isImage = true;
						break;
					}
				}
				if(!isImage)
					continue;
				
				attachs.add(attModel.filename);
				Image img = new Image();
				img.setTitle(attModel.desc);
				img.setUrl(GwtClientUtils.buildAttachmentURL(spaceUname,attModel.filename, attModel.nodeUuid, null,false));
				images.put(attModel.filename, img);
			}
		}
		slider.addImages(images);
		
		oracle.clear();
		oracle.addAll(attachs);

	}
	private AttachmentModel findNode(String filename){
		if(tiny.getEditor().getAttachmentList() != null){
			for (AttachmentModel att : tiny.getEditor().getAttachmentList()) {
				if(filename !=null && filename.equalsIgnoreCase(att.filename)){
					return att;
				}
			}
		}
		return null;
	}

	@Override
	public void addOrUpdateItem(List<AttachmentModel> modelList) {
		//refresh slider 
		this.refreshSlider();
	}

	@Override
	public void removeItem(String nodeUuid) {
		//refresh slider 
		this.refreshSlider();
	}
	@Override
	public void resetAttachmentPanel() {
		this.refreshSlider();
	}

}
