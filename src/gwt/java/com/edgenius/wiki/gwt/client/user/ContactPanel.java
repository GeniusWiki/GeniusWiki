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
package com.edgenius.wiki.gwt.client.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.UserModel;
import com.edgenius.wiki.gwt.client.server.utils.BooleanUtil;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBoxValidCallback;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Dapeng.Ni
 */
public class ContactPanel extends FlexTable implements FormTextBoxValidCallback{
	private boolean editing = false;
	
	private FormTextBox fullname;
	private FormTextBox email;
	private Integer userUid;
	private List<FormTextBox> textboxList;
	private List<CheckBox> checkboxList;
	public ContactPanel(){
		this(false);
	}
	public ContactPanel(boolean editing){
		this.editing = editing;
	}
	public void setUser(UserModel model){
		//clean table first
		this.removeAllRows();
		
		int row = 0;
		this.getColumnFormatter().setStyleName(0, Css.NOWRAP);
		this.getColumnFormatter().setWidth(0, "1%");
		this.getColumnFormatter().setWidth(1, "98%");
		this.getColumnFormatter().setWidth(2, "1%");
		this.setCellPadding(3);
		this.setWidth("100%");
		
		if(editing){
			textboxList = new ArrayList<FormTextBox>();
			checkboxList = new ArrayList<CheckBox>();
			userUid = model.getUid();
			
			fullname = new FormTextBox();
			fullname.setName("user.fullname");
			fullname.valid(Msg.consts.full_name(), true, 0, PersonalProfile.LOGIN_FULLNAME_LEN, null);
			fullname.setStyleName(Css.FORM_INPUT);
			fullname.setText(model.getFullname());
			
			Label fullnameLabel = new Label(Msg.consts.full_name());
			fullnameLabel.setStyleName(Css.FORM_LABEL);
			this.setWidget(row, 0, fullnameLabel);
			this.setWidget(row, 1, fullname);
			row++;
		}
		
		for (Entry<String,LinkedHashMap<String, String>> entry : model.getContacts().entrySet()) {
			LinkedHashMap<String, String> values = entry.getValue();
			if(values == null || values.size() == 0){
				//E.g., if user public profile page, "Contact" group is hidden, so its title won't display as well.
				continue;
			}
			Label cTitle = new Label(entry.getKey());
			cTitle.setStyleName(Css.HEADING2);
			cTitle.addStyleName(Css.UNDERLINE);
			this.setWidget(row, 0, cTitle);
			this.getFlexCellFormatter().setColSpan(row, 0, editing?2:3);
			row++;
			
			//get linked flag - although we know only Twitter has linked attribute so far, we still use this common way for future.
			//see comments on UserUtil.copyUserContactToModel();
			Map<String, Boolean> linked = new HashMap<String, Boolean>();
			for (String key : values.keySet()) {
				if(key.endsWith("_linked")){
					String name = key.substring(0,key.length() - 7);
					linked.put(name, BooleanUtil.toBoolean(values.get(key)));
				}
			}
			
			for (Entry<String, String> contact : values.entrySet()) {
				String name = contact.getKey();
				if(name.endsWith("_linked")){
					continue;
				}
				Label lb = new Label(name);
				lb.setStyleName(Css.FORM_LABEL);
				this.setWidget(row, 0, lb);
				
				if(editing){
					//email
					FormTextBox text = new FormTextBox();
					if(SharedConstants.USERSETTING_PROP_NAME_EMAIL.equalsIgnoreCase(name)){
						email = text;
						text.valid(Msg.consts.email(), true, 0, PersonalProfile.LOGIN_EMAIL_LEN, this);
					}else{
						textboxList.add(text);
					}
					text.setName(name);
					text.setText(contact.getValue());
					
					text.setStyleName(Css.FORM_INPUT);
					this.setWidget(row, 1, text);
					
					if(linked.containsKey(name)){
						row++;
						CheckBox box = new CheckBox(Msg.consts.linked());
						box.setName(name+"_linked");
						box.setValue(linked.get(name));
						this.setWidget(row, 1, box);
						checkboxList.add(box);
					}
				}else{
					if(contact.getValue() != null){
						Label info = new Label(contact.getValue());
						this.setWidget(row, 1, info);
					}else{
						//just a placeholder
						this.setWidget(row, 1, new Label(" "));
					}		
					if(linked.containsKey(name) && linked.get(name)){
						//show linked image
						Image linkedImg = new Image(IconBundle.I.get().connect());
						linkedImg.setTitle(Msg.consts.linked());
						this.setWidget(row, 2, linkedImg);
					}else{
						//just a placeholder
						this.setWidget(row, 2, new Label(" "));
					}
				}
				row++;
			}
		}

	}
	/**
	 * @return
	 */
	public boolean isValidForm() {
		boolean valid;
		valid = email.isValidForSubmit();
		valid = fullname.isValidForSubmit() && valid;
		return valid;
	}
	/**
	 * Only valid at editing mode
	 * @return
	 */
	public UserModel getUserModel() {
		UserModel user = new UserModel();
		user.setUid(userUid);
		user.setFullname(fullname.getText());
		user.setEmail(email.getText());
		LinkedHashMap<String, LinkedHashMap<String, String>> contacts = new LinkedHashMap<String, LinkedHashMap<String,String>>();
		//group doesn't matter when send back to server. just create a dummy one
		LinkedHashMap<String, String> group = new LinkedHashMap<String, String>();
		contacts.put("dummy", group);
		user.setContacts(contacts);
		
		for (CheckBox box : checkboxList) {
			group.put(box.getName(), box.getValue().toString());
		}
		for (FormTextBox box : textboxList) {
			group.put(box.getName(), box.getText());
		}
		return user;
	}
	
	public String onKeyUpValid(Object source) {
		return null;
	}
	public String onBlurValid(Object source) {
		if(source == email){
			return GwtUtils.validateEmail(email.getText());
		}	
		return null;
	}


}
