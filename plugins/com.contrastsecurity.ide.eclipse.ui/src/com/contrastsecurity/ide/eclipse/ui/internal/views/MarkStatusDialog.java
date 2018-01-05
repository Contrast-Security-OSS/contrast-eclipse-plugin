package com.contrastsecurity.ide.eclipse.ui.internal.views;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.extended.TraceStatusRequest;
import com.contrastsecurity.ide.eclipse.ui.internal.model.StatusConstants;

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
	private String status;
	private boolean commentsEnabled;
	
	private Combo reasonCombo;
	private Text noteText;//TODO Needs to be resized correctly when reason combo is not present.
	private Button okButton;
	
	public MarkStatusDialog(Shell shell, ExtendedContrastSDK extendedContrastSDK, String status, boolean commentsEnabled) {
		super(shell);
		this.extendedContrastSDK = extendedContrastSDK;
		this.status = status;
		this.commentsEnabled = commentsEnabled;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite contentComposite = new Composite(container, SWT.NONE);

		contentComposite.setLayout(new GridLayout(2, false));
		
		if(StatusConstants.NOT_A_PROBLEM.equals(status)) {
			createLabel(contentComposite, "Reason");
			reasonCombo = createCombo(contentComposite, REASON_LIST);
			createLabel(contentComposite, "Note");
		}
		else
			createLabel(contentComposite, "Comments");
		
		noteText = createText(contentComposite, 10);
		
		return container;
	}
	
	@Override
	public void create() {
		super.create();
		
		getShell().setText(String.format(TITLE_TEXT, status));
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
		
		if(StatusConstants.NOT_A_PROBLEM.equals(status)) {
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
		//TODO Implement extra behavior
		if(commentsEnabled)
			markStatus(false);
		else
			super.cancelPressed();
	}
	
	@Override
	protected void okPressed() {
		//TODO Implement extra behavior
		markStatus(true);
		super.okPressed();
	}
	
	private void markStatus(boolean addComments) {
		TraceStatusRequest request = new TraceStatusRequest();
		request.setStatus(status);
		request.setCommentPrefrence(commentsEnabled);
		if(addComments)
			request.setNote(noteText.getText());
		if(reasonCombo != null)
			request.setSubstatus(reasonCombo.getText());
		
		System.out.println("Request sent");
		//TODO Send request
	}
	
	private Label createLabel(Composite parent, String text) {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(gd);
		label.setText(text);
		return label;
	}
	
	private Combo createCombo(Composite parent, String[] items) {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(gd);
		combo.setItems(items);
		
		return combo;
	}
	
	private Text createText(Composite parent, int verticalSpan) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalSpan = verticalSpan;
		
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		text.setLayoutData(gd);
		
		return text;
	}

}
