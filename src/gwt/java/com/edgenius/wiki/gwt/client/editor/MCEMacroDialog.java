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
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.TextModel;
import com.edgenius.wiki.gwt.client.server.utils.MacroMaker;
import com.edgenius.wiki.gwt.client.server.utils.NumberUtil;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.HintTextBox;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class MCEMacroDialog extends MCEDialog implements AsyncCallback<TextModel>{
	//logo, toc, piece, comment, signup, feedback, saveme,pageinfo,
	//code,attach,gallery, , include, portal,visible,   
	
	//one userSuggestBox
	public static final String TYPE_USER = "user";
	//filter , column , image size
	public static final String TYPE_GALLERY = "gallery";
	//filter
	public static final String TYPE_ATTACH = "attach";
	//text area
	public static final String TYPE_HTML = "html";
	//input(level), radio box (bullets/number)
	public static final String TYPE_TOC = "toc";
	//panel
	public static final String TYPE_PANEL = "panel";
	
	private String type;
	private MessageWidget message = new MessageWidget();
	private HintTextBox hbox = new HintTextBox();
	private TextArea area = new TextArea();
	//private TextBox box = new TextBox();
	private RadioButton radio1 = new RadioButton("radio");
	private RadioButton radio2 = new RadioButton("radio");
	
	public MCEMacroDialog(TinyMCE tiny, String type, String...params){
		super(tiny);
		this.type = type;
		
		VerticalPanel main = new VerticalPanel();
		FlexTable layout = new FlexTable();
		main.add(message);
		main.add(layout);
		
		if(TYPE_USER.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " " + Msg.consts.user());
			Label l1 = new Label(Msg.consts.user());
			l1.setStyleName(Css.FORM_LABEL);
			layout.setWidget(0, 0, l1);
			layout.setWidget(0, 1, hbox);
			hbox.setHint(Msg.consts.user_name());
			focusHintBox();
		}else if(TYPE_GALLERY.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " Gallery" );
			Label l1 = new Label(Msg.consts.filter());
			l1.setStyleName(Css.FORM_LABEL);
			layout.setWidget(0, 0, l1);
			layout.setWidget(0, 1, hbox);
			hbox.setHint(Msg.consts.gallery_hint());
			focusHintBox();
		}else if(TYPE_ATTACH.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " Attachment list" );
			Label l1 = new Label(Msg.consts.filter());
			l1.setStyleName(Css.FORM_LABEL);
			layout.setWidget(0, 0, l1);
			layout.setWidget(0, 1, hbox);
			hbox.setHint(Msg.consts.attach_hint());
			focusHintBox();
		}else if(TYPE_HTML.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " HTML" );
			layout.setWidget(0, 0, area);
			area.setStyleName(Css.LARGE);
			focusTextArea();
		}else if(TYPE_PANEL.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " Panel" );
			Label l1 = new Label(Msg.consts.title());
			l1.setStyleName(Css.FORM_LABEL);
			layout.setWidget(0, 0, l1);
			layout.setWidget(0, 1, hbox);
			focusHintBox();
			//no hint text in hbox, so it can be put focus... need improve later...
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				public void execute() {
					hbox.setFocus(true);
				}
			});
		}else if(TYPE_TOC.equalsIgnoreCase(type)){
			this.setText(Msg.consts.insert() + " Table of Contents" );
			Label l1 = new Label(Msg.consts.levels());
			l1.setStyleName(Css.FORM_LABEL);
			layout.setWidget(0, 0, l1);
			layout.setWidget(0, 1, hbox);
			hbox.setHint("3");
			
			Label l2 = new Label(Msg.consts.type());
			l2.setStyleName(Css.FORM_LABEL);
			layout.setWidget(1, 0, l2);
			radio1.setText(Msg.consts.numbers());
			radio2.setText(Msg.consts.bullets());
			radio1.setChecked(true);
			FlowPanel pl = new FlowPanel();
			pl.add(radio1);
			pl.add(new HTML("&nbsp;&nbsp;&nbsp;"));
			pl.add(radio2);
			layout.setWidget(1, 1, pl);
			focusHintBox();
		} 
		hbox.setStyleName(Css.FORM_INPUT);
		
		this.setWidget(main);
	}

	/**
	 * 
	 */
	private void focusTextArea() {
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				area.setFocus(true);
			}
		});
	}
	private void focusHintBox() {
		//TODO
		//as hint box can not set foucs in otherwise hintText is gone. so have to disable setFocus() here.
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//			public void execute() {
//				hbox.setFocus(true);
//			}
//		});
	}

	@Override
	protected void okEvent() {
		if(TYPE_USER.equalsIgnoreCase(type)){
			String filter = hbox.getText();
			if(!StringUtil.isBlank(filter)){
				StringBuffer macro = new StringBuffer("@");
				macro.append(filter).append("@");
				MacroRequester.buildMacroFromServer(tiny.getSpaceUname(), tiny.getPageUuid(), macro.toString(),null, null,  this);
			}
		}else if(TYPE_GALLERY.equalsIgnoreCase(type)){
			String filter = hbox.getText();
			
			StringBuffer macro = new StringBuffer("{gallery");
			if(!StringUtil.isBlank(filter)){
				macro.append(":filter=").append(filter);
			}
			macro.append("}");
			
			MacroRequester.buildMacroFromServer(tiny.getSpaceUname(), tiny.getPageUuid(), macro.toString(),
						null, tiny.getEditor().getAttachmentNodeUuidList(), this);
		}else if(TYPE_ATTACH.equalsIgnoreCase(type)){
			String filter = hbox.getText();
			
			StringBuffer macro = new StringBuffer("{attach");
			if(!StringUtil.isBlank(filter)){
				macro.append(":filter=").append(filter);
			}
			macro.append("}");
			MacroRequester.buildMacroFromServer(tiny.getSpaceUname(), tiny.getPageUuid(), macro.toString(), 
						null, tiny.getEditor().getAttachmentNodeUuidList(), this);
		}else if(TYPE_HTML.equalsIgnoreCase(type)){
			this.tiny.insertContent(area.getText());
		}else if(TYPE_PANEL.equalsIgnoreCase(type)){
			
			String title = hbox.getText();
			StringBuffer buffer = new StringBuffer();
			MacroMaker.buildPanel(HTMLPanel.createUniqueId(), buffer, title, "<br>", true);
			this.tiny.insertContent(buffer.toString());
			
		}else if(TYPE_TOC.equalsIgnoreCase(type)){
			StringBuffer macro = new StringBuffer("{toc");
			
			String levelStr = hbox.getText();
			boolean dirty = false;
			if(!StringUtil.isBlank(levelStr)){
				int level = NumberUtil.toInt(levelStr,-1);
				if(level < 1 || level > 6){
					Window.alert(Msg.consts.toc_levels_exceed_scope());
					return;
				}
				macro.append(":level=").append(level);
				dirty = true;
			}
			if(radio2.isChecked()){
				macro.append(dirty?"|":":").append("ordered=false");
			}
			macro.append("}");
			MacroRequester.buildMacroFromServer(tiny.getSpaceUname(), tiny.getPageUuid(), macro.toString(), 
					tiny.getEditor().getText(), null, this);
		}
		
		this.hidebox();
	}



	public void onFailure(Throwable caught) {
		message.error(Msg.consts.unknown_error());
	}

	public void onSuccess(TextModel result) {
		this.tiny.insertContent(result.getText());
		
	}
}
