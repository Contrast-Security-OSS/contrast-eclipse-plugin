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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.ui.internal.model.StatusConstants;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.StatusRequest;


public class MarkStatusDialog extends Dialog {

	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");


	private final static String TITLE_TEXT = resource.getString("MARK_AS_LABEL");
	
	private final static String NOT_A_PROBLEM = resource.getString("VULNERABILITY_STATUS_NOT_A_PROBLEM_STRING_LABEL");
	private final static String SUSPICIOUS = resource.getString("VULNERABILITY_STATUS_SUSPICIOUS_LABEL");
	private final static String CONFIRMED = resource.getString("VULNERABILITY_STATUS_CONFIRMED_LABEL");
	private final static String REMEDIATED = resource.getString("VULNERABILITY_STATUS_REMEDIATED_LABEL");
	private final static String REPORTED = resource.getString("VULNERABILITY_STATUS_REPORTED_LABEL");
	private final static String FIXED = resource.getString("VULNERABILITY_STATUS_FIXED_LABEL");
	
	private final static String URL = resource.getString("TRUSTED_URL_LABEL");
	private final static String FP = resource.getString("FALSE_POSITIVE_LABEL");
	private final static String IC= resource.getString("INTERNAL_CONTROL_LABEL");
	private final static String EC = resource.getString("EXTERNAL_CONTROL_LABEL");
	private final static String OT = resource.getString("OTHER_LABEL");
	
	
	//For combo box 
	private final static String[] STATUS_LIST = {
			NOT_A_PROBLEM,
			SUSPICIOUS,
			CONFIRMED,
			REMEDIATED,
			REPORTED,
			FIXED
	};
	

	//For combo box 
	private final static String[] REASON_LIST = { 
			URL, 
			FP,
			IC,
			EC,
			OT
	};

	
	
	private String traceId;
	private String status;
	private String visualStatus;
	private StatusRequest request;
	
	private Combo statusCombo;
	private Combo reasonCombo;
	private Text noteText;
	
	public MarkStatusDialog(Shell shell, String traceId) {
		super(shell);
		this.traceId = traceId;
		status = visualStatus = StatusConstants.CONFIRMED;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite contentComposite = new Composite(container, SWT.NONE);

		contentComposite.setLayout(new GridLayout(2, false));

		UIElementUtils.createLabel(contentComposite, resource.getString("MARK_AS_LABEL"));
		statusCombo = UIElementUtils.createCombo(contentComposite, STATUS_LIST);
		UIElementUtils.createLabel(contentComposite, resource.getString("REASON_LABEL"));
		reasonCombo = UIElementUtils.createCombo(contentComposite, REASON_LIST);
		UIElementUtils.createLabel(contentComposite, resource.getString("COMMENT_LABEL"));
		noteText = UIElementUtils.createMultiText(contentComposite, 10);

		statusCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				status = visualStatus = statusCombo.getText();
				
				if (status.equals(NOT_A_PROBLEM) || status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING) || status.equals(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM)) {
					reasonCombo.setEnabled(true);
					status = Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING;					
				} else if (status.equals(CONFIRMED)) {
					reasonCombo.setEnabled(false);
					status = Constants.VULNERABILITY_STATUS_CONFIRMED;
				} else if (status.equals(SUSPICIOUS)) {
					reasonCombo.setEnabled(false);
					status = Constants.VULNERABILITY_STATUS_SUSPICIOUS;
				} else if (status.equals(REMEDIATED)) {
					reasonCombo.setEnabled(false);
					status = Constants.VULNERABILITY_STATUS_REMEDIATED;
				} else if (status.equals(REPORTED)) {
					reasonCombo.setEnabled(false);
					status = Constants.VULNERABILITY_STATUS_REPORTED;
				} else if (status.equals(FIXED)) {
					reasonCombo.setEnabled(false);
					status = Constants.VULNERABILITY_STATUS_FIXED;
				}
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
		markStatus();
	}
	
	private void markStatus() {
		List<String> traces = new ArrayList<>();
		traces.add(traceId);
		
		request = new StatusRequest();
		request.setTraces(traces);
		request.setStatus(status);
		if(StringUtils.isNotBlank(noteText.getText())) {
			request.setNote(noteText.getText());
		}


		if(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING.equals(status)) {
			String substatusRequest = "Other";
			if(reasonCombo.getText().equals(URL)) {
				substatusRequest = Constants.URL;
			}
			else if(reasonCombo.getText().equals(FP)) {
				substatusRequest = Constants.FP;
			}
			else if(reasonCombo.getText().equals(IC)) {
				substatusRequest = Constants.IC;
			}
			else if(reasonCombo.getText().equals(EC)) {
				substatusRequest = Constants.EC;
			} else {
				substatusRequest = Constants.OT;
			}
			
			request.setSubstatus(substatusRequest);
		}
		
		
		super.okPressed();
	}
	
	public StatusRequest getTraceStatusRequest() {
		return request;
	}
	
	public String getSelectedStatus() {
		return visualStatus;
	}

}
