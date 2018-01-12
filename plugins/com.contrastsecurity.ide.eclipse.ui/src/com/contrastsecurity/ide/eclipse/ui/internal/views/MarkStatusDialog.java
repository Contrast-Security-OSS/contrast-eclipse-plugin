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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
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
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;

public class MarkStatusDialog extends Dialog {
	
	private final static String TITLE_TEXT = "Mark as";
	private final static String[] STATUS_LIST = {
			StatusConstants.CONFIRMED,
			StatusConstants.SUSPICICIOUS,
			StatusConstants.NOT_A_PROBLEM,
			StatusConstants.REMEDIATED,
			StatusConstants.REPORTED,
			StatusConstants.FIXED
	};
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
	private IStatusListener listener;
	
	private Combo statusCombo;
	private Combo reasonCombo;
	private Text noteText;
	
	public MarkStatusDialog(Shell shell, ExtendedContrastSDK extendedContrastSDK, String traceId) {
		super(shell);
		this.extendedContrastSDK = extendedContrastSDK;
		this.traceId = traceId;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite contentComposite = new Composite(container, SWT.NONE);

		contentComposite.setLayout(new GridLayout(2, false));
		
		UIElementUtils.createLabel(contentComposite, "Mark as");
		statusCombo = UIElementUtils.createCombo(contentComposite, STATUS_LIST);
		UIElementUtils.createLabel(contentComposite, "Reason");
		reasonCombo = UIElementUtils.createCombo(contentComposite, REASON_LIST);
		UIElementUtils.createLabel(contentComposite, "Comment");
		noteText = UIElementUtils.createText(contentComposite, 10);
		
		statusCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println(statusCombo.getText());
				status = statusCombo.getText();
				
				if(StatusConstants.NOT_A_PROBLEM.equals(status)) {
					reasonCombo.setEnabled(true);
					status = Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING;
				}
				else
					reasonCombo.setEnabled(false);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		return container;
	}
	
	@Override
	public void create() {
		super.create();
		
		getShell().setText(TITLE_TEXT);
		reasonCombo.setEnabled(false);
		statusCombo.select(0);
		reasonCombo.select(0);
	}
	
	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		markStatus(true);
	}
	
	public void setStatusListener(IStatusListener listener) {
		this.listener = listener;
	}
	
	private void markStatus(boolean addComments) {
		List<String> traces = new ArrayList<>();
		traces.add(traceId);
		
		TraceStatusRequest request = new TraceStatusRequest();
		request.setTraces(traces);
		request.setStatus(status);
		if(StringUtils.isNotBlank(noteText.getText()))
			request.setNote(noteText.getText());
		if(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING.equals(status))
			request.setSubstatus(reasonCombo.getText());
		
		System.out.println(request.toString());
		
		try {
			BaseResponse response = extendedContrastSDK.markStatus(ContrastUIActivator.getOrgUuid(), request);
			if(response.isSuccess()) {
				if(listener != null)
					listener.onStatusChange(statusCombo.getText());
				super.okPressed();
			}
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
