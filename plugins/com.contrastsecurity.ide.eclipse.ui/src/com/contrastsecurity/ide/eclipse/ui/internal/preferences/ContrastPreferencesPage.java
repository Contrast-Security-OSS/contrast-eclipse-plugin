/*******************************************************************************
 * Copyright (c) 2014 Software Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License, version 2 
 * (GPL-2.0) which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Contributors:
 *     Haris Peco - initial API and implementation
 *******************************************************************************/
package com.contrastsecurity.ide.eclipse.ui.internal.preferences;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.sdk.ContrastSDK;

public class ContrastPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage";
	private static final String BLANK = "";
	private final static String URL_SUFFIX = "/Contrast/api";
	
	private Text teamServerText;
	private Text serviceKeyText;
	private Text apiKeyText;
	private Text usernameText;
	private Button testConnection;
	private Label testConnectionLabel;
	private Text organizationUuidText;
	
	private Combo organizationCombo;
	
	private Button addOrganizationBtn;
	private Button editOrganizationBtn;
	private Button deleteOrganizationBtn;

	public ContrastPreferencesPage() {
		setPreferenceStore(ContrastCoreActivator.getDefault().getPreferenceStore());
		setTitle("Contrast IDE");
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		prefs.put(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
		initPreferences();
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		verifyTeamServerUrl();
		
		ContrastCoreActivator.saveSelectedPreferences(
				teamServerText.getText(), 
				serviceKeyText.getText(), 
				apiKeyText.getText(), 
				usernameText.getText(), 
				organizationCombo.getText(), 
				organizationUuidText.getText());
		
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);
		
		UIElementUtils.createLabel(composite, "Contrast URL:");
		teamServerText = UIElementUtils.createText(composite, 2, 1);
		teamServerText.setToolTipText("This should be the address of your TeamServer from which vulnerability data should be retrieved.\n If you’re using our SaaS, it’s okay to leave this in its default.");

		UIElementUtils.createLabel(composite, "Username:");
		usernameText = UIElementUtils.createText(composite, 2, 1);
		
		UIElementUtils.createLabel(composite, "Service Key:");
		serviceKeyText = UIElementUtils.createText(composite, 2, 1);
		serviceKeyText.setToolTipText("You can find your Service Key at the bottom of your Account Profile, under \"Your Keys\".");
		
		UIElementUtils.createLabel(composite, "Organization: ");
		createOrganizationCombo(composite);
		createOrganizationButtons(composite);
		
		UIElementUtils.createLabel(composite, BLANK);
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		testConnection = UIElementUtils.createButton(composite, gd, "Test Connection");
		testConnection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				verifyTeamServerUrl();
				testConnection(composite);
			}

		});
		
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		testConnectionLabel = UIElementUtils.createBasicLabel(composite, gd, "");

		Group defaultOrganizationGroup = new Group(composite, SWT.NONE);
		defaultOrganizationGroup.setLayout(new GridLayout(2, false));
		defaultOrganizationGroup.setText("Selected Organization");
		gd = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
		defaultOrganizationGroup.setLayoutData(gd);

		UIElementUtils.createLabel(defaultOrganizationGroup, "API Key:");
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		apiKeyText = UIElementUtils.createBasicText(defaultOrganizationGroup, gd, SWT.BORDER);
		apiKeyText.setEditable(false);
		
		UIElementUtils.createLabel(defaultOrganizationGroup, "UUID:");
		organizationUuidText = UIElementUtils.createBasicText(defaultOrganizationGroup, gd, SWT.BORDER);
		organizationUuidText.setEditable(false);
		
		initPreferences();
		enableTestConnection();
		enableOrganizationViews();
		teamServerText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
				enableOrganizationViews();
			}
		});
		usernameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
				enableOrganizationViews();
			}
		});
		apiKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
			}
		});
		serviceKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
				enableOrganizationViews();
			}
		});
		return composite;
	}
	
	private void verifyTeamServerUrl() {
		String tsUrl = teamServerText.getText();
		
		if(tsUrl.endsWith(URL_SUFFIX))
			return;
		
		tsUrl = StringUtils.stripEnd(tsUrl, "/");
		if(tsUrl.endsWith(URL_SUFFIX)) {
			teamServerText.setText(tsUrl);
			return;
		}
		
		char lastChar = tsUrl.charAt(tsUrl.length() - 1);
		for(int i = URL_SUFFIX.length() - 1; i > -1; i--) {
			if(lastChar == URL_SUFFIX.charAt(i) && tsUrl.endsWith(URL_SUFFIX.substring(0, i + 1))) {
				teamServerText.setText(tsUrl + URL_SUFFIX.substring(i + 1));
				return;
			}
		}
		
		teamServerText.setText(tsUrl + URL_SUFFIX);
	}
	
	private void createOrganizationCombo(final Composite parent) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		
		organizationCombo = new Combo(parent, SWT.READ_ONLY);
		//Initialize combo with last chosen value
		String[] list = ContrastCoreActivator.getOrganizationList();
		if(list.length > 0) {
			organizationCombo.setItems(list);
			String orgName = ContrastCoreActivator.getDefaultOrganization();
			if(orgName != null)
				organizationCombo.select(ArrayUtils.indexOf(list, orgName));
		}
		
		organizationCombo.setLayoutData(gd);
		organizationCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onOrganizationSelected();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nohing */ }
		});
	}

	private void createOrganizationButtons(final Composite parent) {
		Composite orgComposite = new Composite(parent, SWT.NULL);
		orgComposite.setLayout(new GridLayout(3, false));
		orgComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 3, 1));
		GridData gd = new GridData(SWT.RIGHT_TO_LEFT, SWT.FILL, false, false, 1, 1);
		
		addOrganizationBtn = UIElementUtils.createButton(orgComposite, gd, "Add");
		editOrganizationBtn = UIElementUtils.createButton(orgComposite, gd, "Edit");
		deleteOrganizationBtn = UIElementUtils.createButton(orgComposite, gd, "Delete");
		
		addOrganizationBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				verifyTeamServerUrl();
				onOrganizationCreated(parent.getShell());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing*/ }
		});
		
		editOrganizationBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				verifyTeamServerUrl();
				onOrganizationEdited(parent.getShell());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing */ }
		});
		
		deleteOrganizationBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onOrganizationDeleted();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) { /* Does nothing */ }
		});
		enableOrganizationViews();
	}
	
	private void enableOrganizationViews() {
		if(StringUtils.isBlank(usernameText.getText()) 
				|| StringUtils.isBlank(teamServerText.getText()) 
				|| StringUtils.isBlank(serviceKeyText.getText())) {
			
			organizationCombo.setEnabled(false);
			addOrganizationBtn.setEnabled(false);
			editOrganizationBtn.setEnabled(false);
			deleteOrganizationBtn.setEnabled(false);
			organizationCombo.setEnabled(false);
			
			return;
		}
		else
			addOrganizationBtn.setEnabled(true);
		
		if(organizationCombo.getItemCount() > 0) {
			organizationCombo.setEnabled(true);
			
			if(StringUtils.isNotBlank(organizationCombo.getText())) {
				editOrganizationBtn.setEnabled(true);
				deleteOrganizationBtn.setEnabled(true);
			}
		}
		else {
			editOrganizationBtn.setEnabled(false);
			deleteOrganizationBtn.setEnabled(false);
			organizationCombo.setEnabled(false);
		}
	}
	
	private void enableTestConnection() {
		testConnection.setEnabled(!usernameText.getText().isEmpty() && !teamServerText.getText().isEmpty()
				&& !apiKeyText.getText().isEmpty() && !serviceKeyText.getText().isEmpty());
	}

	private void initPreferences() {
		teamServerText.setText(ContrastCoreActivator.getTeamServerUrl());
		serviceKeyText.setText(ContrastCoreActivator.getServiceKey());
		apiKeyText.setText(ContrastCoreActivator.getSelectedApiKey());
		usernameText.setText(ContrastCoreActivator.getUsername());
		
		organizationUuidText.setText(ContrastCoreActivator.getSelectedOrganizationUuid());
	}
	
	//===================== Selection listeners ========================
	private void testConnection(Composite composite) {
		final String url = teamServerText.getText();
		URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e1) {
        	MessageDialog.openError(getShell(), "Exception", "Invalid URL.");
			testConnectionLabel.setText("Connection failed!");
			return;
        }
        if (!u.getProtocol().startsWith("http")) {
        	MessageDialog.openError(getShell(), "Exception", "Invalid protocol.");
			testConnectionLabel.setText("Connection failed!");
			return;
        }
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						ContrastSDK sdk = new ContrastSDK(usernameText.getText(), serviceKeyText.getText(),
								apiKeyText.getText(), url);
						try {
							Organization organization = Util.getDefaultOrganization(sdk);
							if (organization == null || organization.getOrgUuid() == null) {
								testConnectionLabel.setText("Connection is correct, but no default organizations found.");
							} else {
								testConnectionLabel.setText("Connection confirmed!");
							}
						} catch (IOException e1) {
							showErrorMessage(e1, getShell(), "Connection error", "Could not connect to Contrast. Please verify that the URL is correct and try again.");
						} catch (UnauthorizedException e1) {
							showErrorMessage(e1, getShell(), "Access denied", "Verify your credentials and make sure you have access to the selected organization.");
						} catch (Exception e1) {
							showErrorMessage(e1, getShell(), "Unknown error", "Unknown exception. Please inform an admin about this.");
						}
						finally {
							composite.layout(true, true);
							composite.redraw();
						}
					}
				});

			}
		};
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		Shell shell = win != null ? win.getShell() : null;
		try {
			new ProgressMonitorDialog(shell).run(true, true, op);
		} catch (InvocationTargetException | InterruptedException e1) {
			ContrastUIActivator.log(e1);
		}
	}
	
	private void showErrorMessage(final Exception e, final Shell shell, final String title, final String message) {
		ContrastUIActivator.log(e);
		testConnectionLabel.setText("Connection failed!");
		
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(100);
				}
				catch(InterruptedException e) {
					//Do nothing
				}
				finally {
					UIElementUtils.ShowErrorMessageFromAnotherThread(Display.getDefault(), shell, title, message);
				}
			}
		}).start();
	}
	
	//===================== Organization combo listeners ========================
	private void onOrganizationSelected() {
		String orgName = organizationCombo.getText();
		if(StringUtils.isNotBlank(orgName)) {
			OrganizationConfig config = ContrastCoreActivator.getOrganizationConfiguration(orgName);
			apiKeyText.setText(config.getApiKey());
			organizationUuidText.setText(config.getOrganizationUUIDKey());
			
			editOrganizationBtn.setEnabled(true);
			deleteOrganizationBtn.setEnabled(true);
		}
		else {
			editOrganizationBtn.setEnabled(false);
			deleteOrganizationBtn.setEnabled(false);
		}
	}
	
	//===================== Organization Buttons listeners ========================
	private void onOrganizationCreated(Shell shell) {
		OrganizationPreferencesDialog dialog = new OrganizationPreferencesDialog(shell, usernameText.getText(),
				serviceKeyText.getText(), teamServerText.getText());
		dialog.create();
		if(dialog.open() == Window.OK) {
			int selected = organizationCombo.getSelectionIndex();
			organizationCombo.setItems(ContrastCoreActivator.getOrganizationList());
			if(organizationCombo.getItemCount() == 1)
				organizationCombo.select(0);
			else if(selected > -1)
				organizationCombo.select(selected);
			
			enableOrganizationViews();
		}
	}
	
	private void onOrganizationEdited(Shell shell) {
		OrganizationPreferencesDialog dialog = new OrganizationPreferencesDialog(shell, usernameText.getText(), 
				serviceKeyText.getText(), teamServerText.getText(), organizationCombo.getText());
		dialog.create();
		if(dialog.open() == Window.OK) {
			if(!dialog.getIsOrganizationCreated()) {
				OrganizationConfig config = ContrastCoreActivator.getOrganizationConfiguration(organizationCombo.getText());
				apiKeyText.setText(config.getApiKey());
				organizationUuidText.setText(config.getOrganizationUUIDKey());
			}
			else {
				String orgName = organizationCombo.getText();
				String[] list = ContrastCoreActivator.getOrganizationList();
				organizationCombo.setItems(list);
				organizationCombo.select(ArrayUtils.indexOf(list, orgName));
			}
		}
	}
	
	private void onOrganizationDeleted() {
		ContrastCoreActivator.removeOrganization(organizationCombo.getSelectionIndex());
		organizationCombo.removeAll();
		organizationCombo.setItems(ContrastCoreActivator.getOrganizationList());
		apiKeyText.setText("");
		organizationUuidText.setText("");
		enableOrganizationViews();
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

}
