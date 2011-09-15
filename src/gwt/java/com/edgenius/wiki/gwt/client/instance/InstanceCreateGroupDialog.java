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
package com.edgenius.wiki.gwt.client.instance;

import com.edgenius.wiki.gwt.client.ControllerFactory;
import com.edgenius.wiki.gwt.client.Css;
import com.edgenius.wiki.gwt.client.GwtClientUtils;
import com.edgenius.wiki.gwt.client.i18n.Msg;
import com.edgenius.wiki.gwt.client.model.RoleModel;
import com.edgenius.wiki.gwt.client.server.SecurityControllerAsync;
import com.edgenius.wiki.gwt.client.server.utils.GwtUtils;
import com.edgenius.wiki.gwt.client.server.utils.SharedConstants;
import com.edgenius.wiki.gwt.client.server.utils.StringUtil;
import com.edgenius.wiki.gwt.client.widgets.Button;
import com.edgenius.wiki.gwt.client.widgets.DialogBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBox;
import com.edgenius.wiki.gwt.client.widgets.FormTextBoxValidCallback;
import com.edgenius.wiki.gwt.client.widgets.IconBundle;
import com.edgenius.wiki.gwt.client.widgets.MessageWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Dapeng.Ni
 */
public class InstanceCreateGroupDialog extends DialogBox {
	private MessageWidget message = new MessageWidget();

	private Image groupIcon = new Image(IconBundle.I.get().group());

	public InstanceCreateGroupDialog() {
		final Button okBtn = new Button(Msg.consts.add());
		Button cancelBtn = new Button(Msg.consts.cancel());

		final GroupForm groupForm = new GroupForm(okBtn);
		okBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				okBtn.setEnabled(false);
				groupForm.submit();
			}
		});
		cancelBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				InstanceCreateGroupDialog.this.hidebox();
			}
		});
		getButtonBar().add(cancelBtn);
		getButtonBar().add(okBtn);

		this.setText(Msg.consts.add_group());
		this.setIcon(groupIcon);

		VerticalPanel panel = new VerticalPanel();
		panel.add(message);
		panel.add(groupForm);
		this.setWidget(panel);
	}

	public native void roleAdded(String fullname)/*-{
		$wnd.roleCreated(fullname);
	}-*/;

	public class GroupForm extends Composite implements SubmitHandler, AsyncCallback<RoleModel>,
			FormTextBoxValidCallback {

		private FormTextBox newRole = new FormTextBox();

		private TextBox newDesc = new TextBox();

		private FormPanel form = new FormPanel();

		private Button sender;

		public GroupForm(Button okBtn) {
			this.sender = okBtn;
			form.addSubmitHandler(this);
			FlexTable t1 = new FlexTable();
			Label newRoleL = new Label(Msg.consts.group_name());
			Label newDescL = new Label(Msg.consts.description());
			t1.setWidget(0,0,newRoleL);
			t1.setWidget(0,1,newRole);
			t1.setWidget(1,0,newDescL);
			t1.setWidget(1,1,newDesc);
			newRoleL.setStyleName(Css.FORM_LABEL);
			newDescL.setStyleName(Css.FORM_LABEL);
			newRole.valid(Msg.consts.name(), true, 0, 0, this);
			form.setWidget(t1);
			initWidget(form);
		}

		public void submit() {
			form.submit();
		}

		public void onSubmit(SubmitEvent event) {
			// make RPC call
			if (newRole.isValidForSubmit()) {
				SecurityControllerAsync securityController = ControllerFactory.getSecurityController();
				securityController.saveRole(newRole.getText(), newDesc.getText(), this);
			}
			// do nothing
			event.cancel();
		}

		public void onFailure(Throwable error) {
			if (sender != null)
				sender.setEnabled(true);
			GwtClientUtils.processError(error);
		}

		/**
		 * role saved/update success
		 */
		public void onSuccess(RoleModel model) {
			if (sender != null)
				sender.setEnabled(true);

			if (!GwtClientUtils.preSuccessCheck(model, message)) {
				return;
			}
			// clean role form input fields for next input
			newRole.setText("");
			newDesc.setText("");

			// insert this role to this page role list, also update user panel's
			// role list
			InstanceCreateGroupDialog.this.hidebox();
			roleAdded(model.getDisplayName());
		}

		public String onBlurValid(Object source) {
			if(source == newRole){
				String name = StringUtil.trimToEmpty(newRole.getText());
				String es = GwtUtils.validateMatch(name, "[a-zA-Z0-9]", "");
				if(es != null)
					return Msg.consts.error_input_letter_number_only();
				
		
				if (name.startsWith(SharedConstants.SPACE_ROLE_DEFAULT_PREFIX)) {
					return Msg.consts.err_role_prefix_not_allow();
				}
			}
			return null;
		}

		public String onKeyUpValid(Object source) {
			return null;
		}
	}

}
