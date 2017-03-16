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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.sdk.ContrastSDK;

public class ContrastPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage";
	private static final String BLANK = "";
	private Text teamServerText;
	private Text serviceKeyText;
	private Text apiKeyText;
	private Text usernameText;
	private Button testConnection;
	private Label testConnectionLabel;
	private Text defaultOrganizationNameText;
	private Text defaultOrganizationUuidText;

	public ContrastPreferencesPage() {
		setPreferenceStore(ContrastCoreActivator.getDefault().getPreferenceStore());
		setTitle("Contrast IDE");
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		IEclipsePreferences prefs = getPreferences();
		prefs.put(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
		initPreferences();
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IEclipsePreferences prefs = getPreferences();
		prefs.put(Constants.TEAM_SERVER_URL, teamServerText.getText());
		prefs.put(Constants.SERVICE_KEY, serviceKeyText.getText());
		prefs.put(Constants.API_KEY, apiKeyText.getText());
		prefs.put(Constants.USERNAME, usernameText.getText());
		prefs.put(Constants.ORGNAME, defaultOrganizationNameText.getText());
		prefs.put(Constants.ORGUUID, defaultOrganizationUuidText.getText());
		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);
		createLabel(composite, "Team Server:");
		teamServerText = new Text(composite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		teamServerText.setLayoutData(gd);
		addWarn(composite, "This should be the address of your TeamServer from which vulnerability data");
		addWarn(composite, "should be retrieved. If you’re using our SaaS, it’s okay to leave this in its default.");

		createLabel(composite, "Username:");
		usernameText = new Text(composite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		usernameText.setLayoutData(gd);
		createLabel(composite, "Service Key:");
		serviceKeyText = new Text(composite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		serviceKeyText.setLayoutData(gd);
		createLabel(composite, "API Key:");
		apiKeyText = new Text(composite, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 2;
		apiKeyText.setLayoutData(gd);
		addWarn(composite, "Your Service Key and API key are available by logging into your TeamServer using");
		addWarn(composite, "your regular account credentials. Go \"My Account\", then \"API Key\".");
		createLabel(composite, BLANK);
		testConnection = new Button(composite, SWT.PUSH);
		testConnection.setText("Test Connection");
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		testConnection.setLayoutData(gd);
		testConnection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String url = teamServerText.getText();
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
										defaultOrganizationNameText.setText(organization.getName() == null ? "" : organization.getName());
										defaultOrganizationUuidText.setText(organization.getOrgUuid() == null ? "" : organization.getOrgUuid());
									}
								} catch (IOException | UnauthorizedException e1) {
									ContrastUIActivator.log(e1);
									MessageDialog.openError(getShell(), "Error from server", e1.getMessage());
									testConnectionLabel.setText("Connection failed!");
								} catch (Exception e1) {
									ContrastUIActivator.log(e1);
									MessageDialog.openError(getShell(), "Exception", "Unknown exception. Check Team Server URL.");
									testConnectionLabel.setText("Connection failed!");
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

		});
		testConnectionLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		testConnectionLabel.setLayoutData(gd);

		Group defaultOrganizationGroup = new Group(composite, SWT.NONE);
		defaultOrganizationGroup.setLayout(new GridLayout(2, false));
		defaultOrganizationGroup.setText("Default Organization");
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		defaultOrganizationGroup.setLayoutData(gd);

		createLabel(defaultOrganizationGroup, "Name:");
		defaultOrganizationNameText = new Text(defaultOrganizationGroup, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		defaultOrganizationNameText.setLayoutData(gd);
		defaultOrganizationNameText.setEditable(false);
		createLabel(defaultOrganizationGroup, "Uuid:");
		defaultOrganizationUuidText = new Text(defaultOrganizationGroup, SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		defaultOrganizationUuidText.setLayoutData(gd);
		defaultOrganizationUuidText.setEditable(false);
		initPreferences();
		enableTestConnection();
		teamServerText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
			}
		});
		usernameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableTestConnection();
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
			}
		});
		return composite;
	}

	private void enableTestConnection() {
		testConnection.setEnabled(!usernameText.getText().isEmpty() && !teamServerText.getText().isEmpty()
				&& !apiKeyText.getText().isEmpty() && !serviceKeyText.getText().isEmpty());
	}

	private Label createLabel(final Composite composite, String name) {
		GridData gd;
		Label label = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		label.setLayoutData(gd);
		label.setText(name);
		return label;
	}

	private void addWarn(final Composite composite, String warn) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(warn);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
	}

	private void initPreferences() {
		IEclipsePreferences prefs = getPreferences();
		teamServerText.setText(prefs.get(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE));
		serviceKeyText.setText(prefs.get(Constants.SERVICE_KEY, BLANK));
		apiKeyText.setText(prefs.get(Constants.API_KEY, BLANK));
		usernameText.setText(prefs.get(Constants.USERNAME, BLANK));
		defaultOrganizationNameText.setText(prefs.get(Constants.ORGNAME, BLANK));
		defaultOrganizationUuidText.setText(prefs.get(Constants.ORGUUID, BLANK));
	}

	private IEclipsePreferences getPreferences() {
		return ContrastCoreActivator.getPreferences();
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

}
