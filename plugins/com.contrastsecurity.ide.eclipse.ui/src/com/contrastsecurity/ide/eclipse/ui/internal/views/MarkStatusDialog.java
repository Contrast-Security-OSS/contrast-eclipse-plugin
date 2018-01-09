/*******************************************************************************
 * Copyright (c) 2017 Contrast Security.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under 
 * the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License.
 * 
 * The terms of the GNU GPL version 3 which accompanies this distribution
 * and is available at https://www.gnu.org/licenses/gpl-3.0.en.html
 * 
 * Contributors:
 *     Contrast Security - initial API and implementation
 *******************************************************************************/
package com.contrastsecurity.ide.eclipse.ui.internal.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.extended.BaseResponse;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.extended.TraceStatusRequest;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.StatusConstants;
import com.contrastsecurity.ide.eclipse.ui.util.SystemUtils;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;

public class MarkStatusDialog extends Dialog {
	
	private final static String TITLE_TEXT = "Mark as %s";
	private final static String[] REASON_LIST = { 
			"Url is only accessible by trusted powers", 
			"False positive",
			"Goes through an internal security protocol",
			"Attack is defended by an external control",
			"Other"
			};
	
	private ExtendedContrastSDK extendedContrastSDK;
	private String traceId;
	private String status;
	private boolean commentsEnabled;
	
	private Combo reasonCombo;
	private Text noteText;
	private Button okButton;
	
	public MarkStatusDialog(Shell shell, ExtendedContrastSDK extendedContrastSDK, String traceId, String status, boolean commentsEnabled) {
		super(shell);
		this.extendedContrastSDK = extendedContrastSDK;
		this.traceId = traceId;
		this.status = status;
		this.commentsEnabled = commentsEnabled;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite contentComposite = new Composite(container, SWT.NONE);

		contentComposite.setLayout(new GridLayout(2, false));
		
		if(StatusConstants.NOT_A_PROBLEM.equals(status)) {
			UIElementUtils.createLabel(contentComposite, "Reason");
			reasonCombo = UIElementUtils.createCombo(contentComposite, REASON_LIST);
			UIElementUtils.createLabel(contentComposite, "Note");
			noteText = UIElementUtils.createText(contentComposite, 10);
		}
		else {
			UIElementUtils.createLabel(contentComposite, "Comments");
			noteText = UIElementUtils.createText(contentComposite, 10, 200);
		}
		
		return container;
	}
	
	@Override
	public void create() {
		super.create();
		
		getShell().setText(String.format(TITLE_TEXT, status));
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		
		if(StatusConstants.NOT_A_PROBLEM.equals(status)) {
			status = Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING;
			okButton.setText("Mark status");
			reasonCombo.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					okButton.setEnabled(true);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
		}
		else {
			getButton(IDialogConstants.CANCEL_ID).setText("No thanks");
			okButton.setText("Add comments");
			if(SystemUtils.isMacOS()) {
				okButton.setSize(120, 29);
				okButton.setLocation(120, 16);
			}
			noteText.addSegmentListener(e -> {
				if(StringUtils.isBlank(noteText.getText()))
					okButton.setEnabled(false);
				else
					okButton.setEnabled(true);
			});
		}
	}
	
	@Override
	protected void cancelPressed() {
		if(commentsEnabled)
			markStatus(false);
		else
			super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		markStatus(true);
	}
	
	private void markStatus(boolean addComments) {
		List<String> traces = new ArrayList<>();
		traces.add(traceId);
		
		TraceStatusRequest request = new TraceStatusRequest();
		request.setTraces(traces);
		request.setStatus(status);
		request.setCommentPrefrence(commentsEnabled);
		if(addComments)
			request.setNote(noteText.getText());
		if(reasonCombo != null)
			request.setSubstatus(reasonCombo.getText());
		
		try {
			BaseResponse response = extendedContrastSDK.markStatus(ContrastUIActivator.getOrgUuid(), request);
			if(response.isSuccess())
				super.okPressed();
		}
		catch (UnauthorizedException e1) {
			ContrastUIActivator.log(e1);
			UIElementUtils.ShowErrorMessage(getShell(), "You don't have authority to perform this operation.");
		} 
		catch (IOException e1) {
			ContrastUIActivator.log(e1);
			UIElementUtils.ShowErrorMessage(getShell(), "Request failed. If error persists, contact support.");
		}
	}
	
	

}
