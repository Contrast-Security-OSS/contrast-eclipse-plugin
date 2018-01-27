package com.contrastsecurity.ide.eclipse.ui.internal.preferences;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;

public class ConnectionConfigDialog extends TitleAreaDialog {
	
	private final static String NEW_DIALOG_TITLE = "Add new Organization";
	private final static String NEW_DIALOG_INFO = "In order to add a new organization for this user, its required to add"
			+ " its API key to retrieve organizations data.";
	
	private final static String EDIT_DIALOG_TITLE = "Edit organization";
	private final static String EDIT_DIALOG_INFO = "To edit an organization, you require API key to retrieve its data.";
	
	private final static String API_KEY_LABEL_TEXT = "API Key: ";
	private final static String ORGANIZATION_NAME_LABEL_TEXT = "Organization name: ";
	private final static String ORGANIZATION_ID_LABEL_TEXT = "Organization UUID: ";
	private final static String RETRIEVE_ORGANIZATIONS_BTN_TEXT = "Retrieve organizations";
	
	private final static String URL_SUFFIX = "/Contrast/api";
	
	private Button okButton;
	
	private IConnectionConfigListener listener;
	
	private Text tsUrlText;
	private Text usernameText;
	private Text serviceKeyText;
	private Text apiKeyText;
	//private Text orgNameText;
	private Combo organizationCombo;
	private Text orgIdText;
	
	private Button getOrgBtn;
	
	private final boolean isNewOrganization;
	
	private final ConnectionConfig config;
	
	private boolean isOrganizationCreated;
	
	private List<Organization> orgList;
	
	public ConnectionConfigDialog(Shell parentShell, String username, String serviceKey, String teamServerUrl, IConnectionConfigListener listener) {
		super(parentShell);
		
		config = new ConnectionConfig();
		config.setTsUrl(teamServerUrl);
		config.setUsername(username);
		config.setServiceKey(serviceKey);
		this.isNewOrganization = true;
		this.listener = listener;
	}
	
	public ConnectionConfigDialog(Shell parentShell, ConnectionConfig config, IConnectionConfigListener listener) {
		super(parentShell);
		
		this.config = config;
		this.isNewOrganization = false;
		this.listener = listener;
	}
	
	@Override
	public void create() {
		super.create();
		
		okButton = getButton(IDialogConstants.OK_ID);
		
		if(isNewOrganization) {
			setTitle(NEW_DIALOG_TITLE);
			setMessage(NEW_DIALOG_INFO, IMessageProvider.INFORMATION);
			okButton.setEnabled(false);
		}
		else {
			setTitle(EDIT_DIALOG_TITLE);
			setMessage(EDIT_DIALOG_INFO, IMessageProvider.INFORMATION);
			okButton.setEnabled(true);
		}
	}
	
	@Override
	public Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout(3, false);
		container.setLayout(layout);
		
		UIElementUtils.createLabel(container, "Contrast URL: ");
		tsUrlText = UIElementUtils.createText(container, 2, 1);
		tsUrlText.setToolTipText("This should be the address of your TeamServer from which vulnerability data should be retrieved.\n If you’re using our SaaS, it’s okay to leave this in its default.");
		
		UIElementUtils.createLabel(container, "Username: ");
		usernameText = UIElementUtils.createText(container, 2, 1);
		
		UIElementUtils.createLabel(container, "Service Key: ");
		serviceKeyText = UIElementUtils.createText(container, 2, 1);
		serviceKeyText.setToolTipText("You can find your Service Key at the bottom of your Account Profile, under \"Your Keys\".");
		
		UIElementUtils.createLabel(container, "API Key: ");
		apiKeyText = UIElementUtils.createText(container, 2, 1);
		apiKeyText.setToolTipText("You can find your organization API key in the Organization Settings, in the API section.");
		
		createOrganizationCombo(container);
		
		GridData btnGd = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		getOrgBtn = UIElementUtils.createButton(container, btnGd, "Load");
		getOrgBtn.setEnabled(false);
		
		UIElementUtils.createLabel(container, "Organization UUID: ");
		orgIdText = UIElementUtils.createText(container, 2, 1);
		orgIdText.setEnabled(false);
		
		tsUrlText.setText(config.getTsUrl());
		usernameText.setText(config.getUsername());
		serviceKeyText.setText(config.getServiceKey());
		
		if(!isNewOrganization) {
			apiKeyText.setText(config.getApiKey());
			//orgNameText.setText(config.getOrgName());
			orgIdText.setText(config.getOrgId());
		}
		
		setupListeners();
		
		return area;
	}
	
	private void setupListeners() {
		ModifyListener textsListener = new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(isNewOrganization)
					enableLoadButton();
				else
					enableOkButton();
			}
		};
		
		tsUrlText.addModifyListener(textsListener);
		usernameText.addModifyListener(textsListener);
		serviceKeyText.addModifyListener(textsListener);
		apiKeyText.addModifyListener(textsListener);
		
		getOrgBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent ev) {
				verifyTeamServerUrl();
				
				try {
					if(retrieveOrganizationName()) {
						organizationCombo.setEnabled(true);
						tsUrlText.setEnabled(false);
						usernameText.setEnabled(false);
						serviceKeyText.setEnabled(false);
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
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private void enableLoadButton() {
		boolean isEnabled = StringUtils.isNotBlank(tsUrlText.getText()) && StringUtils.isNotBlank(usernameText.getText())
				&& StringUtils.isNotBlank(serviceKeyText.getText()) && StringUtils.isNotBlank(apiKeyText.getText());
		
		getOrgBtn.setEnabled(isEnabled);
	}
	
	private void enableOkButton() {
		boolean isEnabled = StringUtils.isNotBlank(tsUrlText.getText()) && StringUtils.isNotBlank(usernameText.getText())
				&& StringUtils.isNotBlank(serviceKeyText.getText()) && StringUtils.isNotBlank(apiKeyText.getText());
		
		okButton.setEnabled(isEnabled);
	}
	
	private void verifyTeamServerUrl() {
		String tsUrl = tsUrlText.getText();
		
		if(tsUrl.endsWith(URL_SUFFIX))
			return;
		
		tsUrl = StringUtils.stripEnd(tsUrl, "/");
		if(tsUrl.endsWith(URL_SUFFIX)) {
			tsUrlText.setText(tsUrl);
			return;
		}
		
		char lastChar = tsUrl.charAt(tsUrl.length() - 1);
		for(int i = URL_SUFFIX.length() - 1; i > -1; i--) {
			if(lastChar == URL_SUFFIX.charAt(i) && tsUrl.endsWith(URL_SUFFIX.substring(0, i + 1))) {
				tsUrlText.setText(tsUrl + URL_SUFFIX.substring(i + 1));
				return;
			}
		}
		
		tsUrlText.setText(tsUrl + URL_SUFFIX);
	}
	
	private void createOrganizationCombo(Composite container) {
		UIElementUtils.createLabel(container, ORGANIZATION_NAME_LABEL_TEXT);
		
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		organizationCombo = new Combo(container, SWT.READ_ONLY);
		organizationCombo.setLayoutData(gd);
		organizationCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				orgIdText.setText(orgList.get(organizationCombo.getSelectionIndex()).getOrgUuid());
				okButton.setEnabled(true);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing */ }
		});
		organizationCombo.setEnabled(false);
		if(!isNewOrganization) {
			organizationCombo.setItems(new String[]{config.getOrgName()});
			organizationCombo.select(0);
		}
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected void okPressed() {
		config.setTsUrl(tsUrlText.getText());
		config.setUsername(usernameText.getText());
		config.setServiceKey(serviceKeyText.getText());
		config.setApiKey(apiKeyText.getText());
		config.setOrgName(organizationCombo.getText());
		config.setOrgId(orgIdText.getText());
		
		listener.onConnectionSave(config);
	}
	
	private void clearOrganizationsCombo() {
		organizationCombo.removeAll();
		organizationCombo.setEnabled(false);
	}
	
	public boolean getIsOrganizationCreated() {
		return isOrganizationCreated;
	}
	
	private boolean retrieveOrganizationName() throws IOException, UnauthorizedException {
		ExtendedContrastSDK sdk = ContrastCoreActivator.getContrastSDK(usernameText.getText(), apiKeyText.getText(), serviceKeyText.getText(), tsUrlText.getText());

		Organizations organizations = sdk.getProfileOrganizations();
		if(organizations.getOrganizations() != null && !organizations.getOrganizations().isEmpty()) {
			orgList = organizations.getOrganizations();
			String[] orgArray = Util.extractOrganizationNames(orgList);
			organizationCombo.setItems(orgArray);
			
			/*int position = ArrayUtils.indexOf(orgArray, organizationName);
			if(position != ArrayUtils.INDEX_NOT_FOUND)
				organizationCombo.select(position);*/
			
			return true;
		}
		
		return false;
	}
}
