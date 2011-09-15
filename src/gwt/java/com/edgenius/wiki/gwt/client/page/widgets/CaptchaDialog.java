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
package com.edgenius.wiki.gwt.client.page.widgets;

import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.page.CommentPanel;
import com.edgenius.wiki.gwt.client.page.EditPanel;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.ButtonBar;
import com.edgenius.wiki.gwt.client.widgets.ButtonIconBundle;
import com.edgenius.wiki.gwt.client.widgets.CaptchaWidget;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class CaptchaDialog extends DialogBox{
	public static final int TYPE_MOVE_PAGE = 10;

	public static final int TYPE_COPY_PAGE = 11;
	
	private CaptchaWidget captcha = new CaptchaWidget();
	private Button okBtn = new Button(Msg.consts.ok(),ButtonIconBundle.tickImage());
	private Button cancelBtn = new Button(Msg.consts.cancel(),ButtonIconBundle.crossImage());
	private MessageWidget message = new MessageWidget();

	/**
	 * Save page captcha dialog
	 */
	public CaptchaDialog(final EditPanel editPanel, final boolean forceSave){
		
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				editPanelSave(editPanel, forceSave);
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				editPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	editPanelSave(editPanel, forceSave);
                }
			}
		});
	}

	/**
	 * Restore history  captcha dialog
	 */
	public CaptchaDialog(final EditPanel editPanel, final String spaceUname, final String currPageTitle, final int version) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				restoreHistory(editPanel, spaceUname, currPageTitle, version);
			}


		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				//editPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	restoreHistory(editPanel, spaceUname, currPageTitle, version);
                }
			}
		});
	}
	/**
	 * Save comment captcha dialog
	 */
	public CaptchaDialog(final CommentPanel commentPanel, final String text, final Integer parentUid, final Integer rootUid) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				commentPanelSave(commentPanel, text, parentUid, rootUid);
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				commentPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	commentPanelSave(commentPanel, text, parentUid, rootUid);
                }
			}
		});
	}
	/**
	 * Remove page captcha dialog
	 */
	public CaptchaDialog(final EditPanel editPanel) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				removePage(editPanel);
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				//commentPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	removePage(editPanel);
                }
			}

		});
	}

	/**
	 * Move or copy page captcha dialog
	 */
	public CaptchaDialog(final int type, final EditPanel editPanel) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				copyOrMovePage(type, editPanel);
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				//commentPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	copyOrMovePage(type, editPanel);
                }
			}

		});
	}

	/**
	 * Upload attachment captcha dialog
	 */
	public CaptchaDialog(final AttachmentPanel attachmentPanel, final boolean toView, final int draftStatus) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				uploadAttachment(attachmentPanel,toView,draftStatus);
			}
		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				//commentPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	uploadAttachment(attachmentPanel,toView,draftStatus);
                }
			}

		});
	}
	/**
	 * Edit tag in view panel.
	 */
	public CaptchaDialog(final TagsPanel tagsPanel) {
		buildPanel();
		okBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				okBtn.setBusy(true);
				updateTag(tagsPanel);
			}

		});
		cancelBtn.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				CaptchaDialog.this.hidebox();
				//commentPanel.saveFunctionDone();
			}
		});
		captcha.getCaptchaInputWidget().addKeyDownHandler(new KeyDownHandler(){
			public void onKeyDown(KeyDownEvent event) {
				// Return clicked
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER){
                	okBtn.setBusy(true);
                	updateTag(tagsPanel);
                }
			}

		});
		
	}

	public void enableSubmit() {
		okBtn.setBusy(false);
	}
	
	//********************************************************************
	//               private methods
	//********************************************************************
	private void updateTag(TagsPanel tagsPanel) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		
		tagsPanel.updateTagWithCaptha(captchaResponse, this);
	}
	private void uploadAttachment(AttachmentPanel attachmentPanel, boolean toView, int draftStatus) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		
		attachmentPanel.uploadWithCaptcha(captchaResponse, toView, draftStatus,this);
	}

	/**
	 * @param type
	 * @param editPanel
	 */
	private void copyOrMovePage(int type, EditPanel editPanel) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		
		if(type == TYPE_COPY_PAGE){
			editPanel.copyPageWithCaptcha(captchaResponse, this);
		}
		if(type == TYPE_MOVE_PAGE){
			editPanel.movePageWithCaptcha(captchaResponse, this);
		}
		
	}

	/**
	 * @param commentPanel
	 * @param text
	 * @param parentUid
	 * @param rootUid
	 */
	private void commentPanelSave(final CommentPanel commentPanel, final String text, final Integer parentUid,
			final Integer rootUid) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		commentPanel.saveCommentWithCaptcha(text, parentUid, rootUid, captchaResponse);
		//don't hide this dialogue until save response return
	}


	/**
	 * @param editPanel
	 * @param forceSave
	 */
	private void editPanelSave(final EditPanel editPanel, final boolean forceSave) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		editPanel.saveWithCaptcha(forceSave, captchaResponse, this);
		//don't hide this dialogue until save response return
	}
	
	private void restoreHistory(EditPanel editPanel, String spaceUname, String currPageTitle, int version) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		
		editPanel.restoreHistoryWithCaptcha(spaceUname, currPageTitle, version,captchaResponse,this);
		//don't hide this dialogue until save response return
	}
	

	private void removePage(EditPanel editPanel) {
		String captchaResponse = validCaptcha();
		if(captchaResponse == null)
			return;
		
		editPanel.removePageWithCaptcha(captchaResponse,this);
		//don't hide this dialogue until save response return
		
	}
	private void buildPanel(){
		this.setIcon(ButtonIconBundle.userImage());
		this.setText(Msg.consts.captcha_title());
		captcha.enable();
		
		ButtonBar btnPanel = getButtonBar();
		btnPanel.add(cancelBtn);
		btnPanel.add(okBtn);
		
		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(captcha);
		panel.add(btnPanel);
		this.setWidget(panel);
		okBtn.setBusy(false);
		
		panel.setCellHorizontalAlignment(message, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setCellHorizontalAlignment(captcha, HasHorizontalAlignment.ALIGN_CENTER);
		panel.setWidth("100%");
	}
	/**
	 * @return
	 */
	private String validCaptcha() {
		String captchaResponse = captcha.getCaptchaInput();
		if(captchaResponse == null || captchaResponse.trim().length() == 0){
			Window.alert(Msg.consts.captcha_required());
			enableSubmit();
			return null;
		}
		return captchaResponse;
	}
	/**
	 * 
	 */
	public void refreshCaptch() {
		message.cleanMessage();
		message.error(Msg.consts.wrong_captcha());

		if(captcha.isEnabled())
			captcha.refresh();
		
	}


}
