package com.contrastsecurity.ide.eclipse.ui.internal.preferences;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
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

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.OrganizationNotFoundException;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;

public class OrganizationPreferencesDialog extends TitleAreaDialog {
	
	private final static String NEW_DIALOG_TITLE = "Add new Organization";
	private final static String NEW_DIALOG_INFO = "In order to add a new organization for this user, its required to add"
			+ " its API key to retrieve organizations data.";
	
	private final static String EDIT_DIALOG_TITLE = "Edit organization";
	private final static String EDIT_DIALOG_INFO = "To edit an organization, you require API key to retrieve its data.";
	
	private final static String API_KEY_LABEL_TEXT = "API Key: ";
	private final static String ORGANIZATION_NAME_LABEL_TEXT = "Organization name: ";
	private final static String ORGANIZATION_ID_LABEL_TEXT = "Organization UUID: ";
	private final static String RETRIEVE_ORGANIZATIONS_BTN_TEXT = "Retrieve organizations";
	
	private final static int SAVE_SUCCESSFUL = 1;
	private final static int ORG_ALREADY_EXISTS = 2;
	private final static int SAVE_FAILED = 3;
	private final static int SAVE_REJECTED = 4;
	
	private Text apiKeyText;
	private Combo organizationCombo;
	private Text organizationIdText;
	private Button retrieveOrganizationsButton;
	
	private Button okButton;
	
	private final String organizationName;
	private final boolean isNewOrganization;
	
	private final String teamServerUrl;
	private final String username;
	private final String serviceKey;
	
	private boolean isOrganizationCreated;
	
	private List<Organization> orgList;
	
	public OrganizationPreferencesDialog(Shell parentShell, String username, String serviceKey, String teamServerUrl) {
		super(parentShell);
		
		this.username = username;
		this.serviceKey = serviceKey;
		this.teamServerUrl = teamServerUrl;
		this.isNewOrganization = true;
		this.organizationName = null;
	}
	
	public OrganizationPreferencesDialog(Shell parentShell, String username, String serviceKey, String teamServerUrl, String organizationName) {
		super(parentShell);
		
		this.username = username;
		this.serviceKey = serviceKey;
		this.teamServerUrl = teamServerUrl;
		this.organizationName = organizationName;
		this.isNewOrganization = false;
	}
	
	@Override
	public void create() {
		super.create();
		if(isNewOrganization) {
			setTitle(NEW_DIALOG_TITLE);
			setMessage(NEW_DIALOG_INFO, IMessageProvider.INFORMATION);
		}
		else {
			setTitle(EDIT_DIALOG_TITLE);
			setMessage(EDIT_DIALOG_INFO, IMessageProvider.INFORMATION);
		}
		
		okButton = getButton(IDialogConstants.OK_ID);
		okButton.setEnabled(false);
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		
		createApiKeyText(container);
		createOrganizationCombo(container);
		createOrganizationIdText(container);
		createRetrieveOrganizationsButton(container);
		
		if(!isNewOrganization) {
			OrganizationConfig config = ContrastCoreActivator.getOrganizationConfiguration(organizationName);
			if(config != null) {
				apiKeyText.setText(config.getApiKey());
				organizationIdText.setText(config.getOrganizationUUIDKey());
			}
		}
		
		return area;
	}
	
	private void createApiKeyText(Composite container) {
		Label apiKeyLabel = new Label(container, SWT.NONE);
		apiKeyLabel.setText(API_KEY_LABEL_TEXT);
		
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		apiKeyText = UIElementUtils.createBasicText(container, gd, SWT.BORDER);
		apiKeyText.setToolTipText("You can find your organization API key in the Organization Settings, in the API section.");
	}
	
	private void createOrganizationCombo(Composite container) {
		Label organizationNameLabel = new Label(container, SWT.NONE);
		organizationNameLabel.setText(ORGANIZATION_NAME_LABEL_TEXT);
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		
		organizationCombo = new Combo(container, SWT.READ_ONLY);
		organizationCombo.setLayoutData(gd);
		organizationCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				organizationIdText.setText(orgList.get(organizationCombo.getSelectionIndex()).getOrgUuid());
				okButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing */ }
		});
		organizationCombo.setEnabled(false);
		if(!isNewOrganization) {
			organizationCombo.setItems(new String[]{organizationName});
			organizationCombo.select(0);
		}
	}
	
	private void createOrganizationIdText(Composite container) {
		Label serviceKeyLabel = new Label(container, SWT.NONE);
		serviceKeyLabel.setText(ORGANIZATION_ID_LABEL_TEXT);
		
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		gd.horizontalSpan = 2;
		
		organizationIdText = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		organizationIdText.setLayoutData(gd);
	}
	
	private void createRetrieveOrganizationsButton(Composite container) {
		GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		retrieveOrganizationsButton = UIElementUtils.createButton(container, gd, RETRIEVE_ORGANIZATIONS_BTN_TEXT);
		retrieveOrganizationsButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					if(retrieveOrganizationName()) {
						organizationCombo.setEnabled(true);
						apiKeyText.setEnabled(false);
					}
					else{
						MessageDialog.openError(getShell(), "", "No organization found!");
						clearOrganizationsCombo();
					}
				}
				catch(IOException e) {
					MessageDialog.openError(getShell(), "Connection error", "Connection failed.");
					clearOrganizationsCombo();
				}
				catch(UnauthorizedException e) {
					MessageDialog.openError(getShell(), "Access denied", "Unauthorized!! Verify your credentials please.");
					clearOrganizationsCombo();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing */ }
		});
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected void okPressed() {
		if(isNewOrganization) {
			switch(createOrganizationConfig()) {
				case SAVE_SUCCESSFUL:
					//Do nothing
					break;
				case ORG_ALREADY_EXISTS:
					MessageDialog.openWarning(getShell(), "Organization already exists", "Organization won't be saved. Use edit function to update its values");
					cancelPressed();
					return;
				
				case SAVE_FAILED:
				default:
					MessageDialog.openError(getShell(), "Save failed", "Unexpected error. Your organization was not saved. Contact support please.");
					cancelPressed();
					return;
			}
		}
		else {
			switch(editOrganizationConfig()) {
				case SAVE_SUCCESSFUL:
					isOrganizationCreated = true;
					break;
				case SAVE_REJECTED:
					cancelPressed();
					return;
				
				case SAVE_FAILED:
				default:
					MessageDialog.openError(getShell(), "Save failed", "Unexpected error. Your organization was not saved. Contact support please.");
					cancelPressed();
					return;
			}
		}
		
		super.okPressed();
	}
	
	private void clearOrganizationsCombo() {
		organizationCombo.removeAll();
		organizationCombo.setEnabled(false);
	}
	
	public boolean getIsOrganizationCreated() {
		return isOrganizationCreated;
	}
	
	private boolean retrieveOrganizationName() throws IOException, UnauthorizedException {
		ExtendedContrastSDK sdk = ContrastCoreActivator.getContrastSDK(username, apiKeyText.getText(), serviceKey, teamServerUrl);

		Organizations organizations = sdk.getProfileOrganizations();
		if(organizations.getOrganizations() != null && !organizations.getOrganizations().isEmpty()) {
			orgList = organizations.getOrganizations();
			String[] orgArray = Util.extractOrganizationNames(orgList);
			organizationCombo.setItems(orgArray);
			
			int position = ArrayUtils.indexOf(orgArray, organizationName);
			if(position != ArrayUtils.INDEX_NOT_FOUND)
				organizationCombo.select(position);
			
			return true;
		}
		
		return false;
	}
	
	private int createOrganizationConfig() {
		try {
			if(ContrastCoreActivator.editOrganization(organizationCombo.getText(), apiKeyText.getText(), organizationIdText.getText()))
				return SAVE_SUCCESSFUL;
			else
				return SAVE_FAILED;
		}
		catch(OrganizationNotFoundException e) {
			if(ContrastCoreActivator.saveNewOrganization(organizationCombo.getText(), apiKeyText.getText(), organizationIdText.getText()))
				return SAVE_SUCCESSFUL;
			else
				return SAVE_FAILED;
		}
	}
	
	private int editOrganizationConfig() {
		try {
			if(ContrastCoreActivator.editOrganization(organizationCombo.getText(), apiKeyText.getText(), organizationIdText.getText()))
				return SAVE_SUCCESSFUL;
			else
				return SAVE_FAILED;
		}
		catch(OrganizationNotFoundException e) {
			if(MessageDialog.openConfirm(getShell(), "Organization not found", "Do you wish to save a new configuration?")) {
				if(ContrastCoreActivator.saveNewOrganization(organizationCombo.getText(), apiKeyText.getText(), organizationIdText.getText()))
					return SAVE_SUCCESSFUL;
				else
					return SAVE_FAILED;
			}
			else
				return SAVE_REJECTED;
		}
	}
}
