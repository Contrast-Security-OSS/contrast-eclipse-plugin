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
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.jar.Manifest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.IntegrationName;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.sdk.ContrastSDK;

public class ContrastPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage";
	private final static String URL_SUFFIX = "/Contrast/api";
	private Text teamServerText;
	private Text usernameText;
	private Text serviceKeyText;
	private Text apiKeyText;
	private Label testConnectionLabel;
	private Text organizationUuidText;
	private Button addOrganizationBtn;
	private Button deleteOrganizationBtn;
	private TableViewer tableViewer;
	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

	public ContrastPreferencesPage() {
		setPreferenceStore(ContrastCoreActivator.getDefault().getPreferenceStore());
		setTitle("Contrast IDE");
		noDefaultAndApplyButton();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		final IStructuredSelection selection = tableViewer.getStructuredSelection();

		if (selection != null) {
			String organizationName = "";
			if (selection.getFirstElement() != null) {
				organizationName = (String) selection.getFirstElement();
				// if no organization is selected, save the first org from the table
			} else if (tableViewer.getTable().getItemCount() > 0) {
				organizationName = tableViewer.getTable().getItem(0).getText();
			}
			ContrastCoreActivator.saveSelectedPreferences(organizationName);
		}

		return super.performOk();
	}

	@Override
	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		final GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(gd);

		Group defaultOrganizationGroup = new Group(composite, SWT.NONE);
		defaultOrganizationGroup.setLayout(new GridLayout(2, false));
		defaultOrganizationGroup.setText(resource.getString("ADD_ORGANIZATION"));
		gd = new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1);
		defaultOrganizationGroup.setLayoutData(gd);

		UIElementUtils.createLabel(defaultOrganizationGroup, "Contrast URL:");
		teamServerText = UIElementUtils.createText(defaultOrganizationGroup, 2, 1);
		teamServerText.setToolTipText(resource.getString("URL_INFORMATION"));

		UIElementUtils.createLabel(defaultOrganizationGroup, resource.getString("USERNAME"));
		usernameText = UIElementUtils.createText(defaultOrganizationGroup, 2, 1);

		UIElementUtils.createLabel(defaultOrganizationGroup, resource.getString("SERVICE_KEY"));
		serviceKeyText = UIElementUtils.createText(defaultOrganizationGroup, 2, 1);
		serviceKeyText.setToolTipText(resource.getString("SERVICE_KEY_INFO"));

		UIElementUtils.createLabel(defaultOrganizationGroup, resource.getString("API_KEY"));
		apiKeyText = UIElementUtils.createText(defaultOrganizationGroup, 2, 1, SWT.PASSWORD | SWT.BORDER);

		UIElementUtils.createLabel(defaultOrganizationGroup, resource.getString("ORG_ID"));
		organizationUuidText = UIElementUtils.createText(defaultOrganizationGroup, 2, 1);

		gd = new GridData(SWT.LEFT_TO_RIGHT, SWT.CENTER, false, false, 1, 1);
		addOrganizationBtn = UIElementUtils.createButton(defaultOrganizationGroup, gd, resource.getString("ADD"));

		gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		testConnectionLabel = UIElementUtils.createBasicLabel(defaultOrganizationGroup, gd, "");

		tableViewer = createTableViewer(composite);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				enableOrganizationViews();
			}

		});

		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		deleteOrganizationBtn = UIElementUtils.createButton(composite, gd, resource.getString("REMOVE"));

		addOrganizationBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				verifyTeamServerUrl();
				retrieveOrganizationName(composite);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				/* Does nothing */ }
		});

		deleteOrganizationBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (tableViewer.getTable().getSelectionIndex() != -1) {
					onOrganizationDeleted(tableViewer.getTable().getSelectionIndex());
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				/* Does nothing */ }
		});

		teamServerText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableOrganizationViews();
			}
		});
		usernameText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableOrganizationViews();
			}
		});
		apiKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableOrganizationViews();
			}
		});
		serviceKeyText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableOrganizationViews();
			}
		});

		organizationUuidText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				enableOrganizationViews();

			}

		});

		enableOrganizationViews();

		return composite;
	}

	private void verifyTeamServerUrl() {
		String tsUrl = teamServerText.getText();

		tsUrl = StringUtils.stripEnd(tsUrl, "/");
		if (tsUrl.endsWith(URL_SUFFIX)) {
			teamServerText.setText(tsUrl);
			return;
		}

		char lastChar = tsUrl.charAt(tsUrl.length() - 1);
		for (int i = URL_SUFFIX.length() - 1; i > -1; i--) {
			if (lastChar == URL_SUFFIX.charAt(i) && tsUrl.endsWith(URL_SUFFIX.substring(0, i + 1))) {
				teamServerText.setText(tsUrl + URL_SUFFIX.substring(i + 1));
				return;
			}
		}

		teamServerText.setText(tsUrl + URL_SUFFIX);
	}

	private void enableOrganizationViews() {
		if (StringUtils.isBlank(usernameText.getText()) || StringUtils.isBlank(teamServerText.getText())
				|| StringUtils.isBlank(serviceKeyText.getText()) || StringUtils.isBlank(apiKeyText.getText())
				|| StringUtils.isBlank(organizationUuidText.getText())) {

			addOrganizationBtn.setEnabled(false);
		} else {
			addOrganizationBtn.setEnabled(true);
		}
		if (tableViewer.getTable().getItemCount() > 0 && tableViewer.getTable().getSelectionIndex() != -1) {
			deleteOrganizationBtn.setEnabled(true);
		} else {
			deleteOrganizationBtn.setEnabled(false);
		}
	}

	private void testConnection(Composite composite) {
		final String url = teamServerText.getText();
		URL u;
		try {
			u = new URL(url);
		} catch (MalformedURLException e1) {
			MessageDialog.openError(getShell(), resource.getString("EXCEPTION"), resource.getString("INVALID_URL"));
			testConnectionLabel.setText(resource.getString("CONNECTION_FAILED"));
			return;
		}
		if (!u.getProtocol().startsWith("http")) {
			MessageDialog.openError(getShell(), resource.getString("EXCEPTION"), resource.getString("INVALID_PROTOCOL"));
			testConnectionLabel.setText(resource.getString("CONNECTION_FAILED"));
			return;
		}
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						Manifest manifest = new Manifest();
						ContrastSDK sdk = new ContrastSDK.Builder(usernameText.getText(), serviceKeyText.getText(), apiKeyText.getText()).withApiUrl(url).withIntegrationName(IntegrationName.ECLIPSE_IDE).withVersion(manifest.getAttributes("Bundle-Version").toString()).build();
						try {
							Organization organization = Util.getDefaultOrganization(sdk);
							if (organization == null || organization.getOrgUuid() == null) {
								testConnectionLabel.setText(resource.getString("NO_ORG_FOUND"));
							} else {
								testConnectionLabel.setText(resource.getString("CONFIRMED_CONNECTION"));
							}
						} catch (IOException e1) {
							showErrorMessage(e1, getShell(), resource.getString("CONNECTION_ERROR"), resource.getString("CONTRAST_ERROR"));
						} catch (UnauthorizedException e1) {
							showErrorMessage(e1, getShell(), resource.getString("ACCESS_DENIED"), resource.getString("VERIFY_CREDENTIALS_ERROR"));
						} catch (Exception e1) {
							showErrorMessage(e1, getShell(), resource.getString("UNKNOWN_ERROR"), resource.getString("UNKNOWN_ERROR_INFO"));
						} finally {
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
		testConnectionLabel.setText(resource.getString("CONNECTION_FAILED"));

		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Do nothing
				} finally {
					UIElementUtils.ShowErrorMessageFromAnotherThread(Display.getDefault(), shell, title, message);
				}
			}
		}).start();
	}

	private void onOrganizationDeleted(final int position) {
		ContrastCoreActivator.removeOrganization(position);
		tableViewer.setInput(ContrastCoreActivator.getOrganizationList());

		if (tableViewer.getTable().getItemCount() > 0) {
			tableViewer.getTable().setSelection(0);
		}

		enableOrganizationViews();
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

	private TableViewer createTableViewer(Composite composite) {
		TableViewer tableViewer = new TableViewer(composite,
				SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableViewer.getTable().setLayoutData(gd);

		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		TableLayout layout = new TableLayout();
		tableViewer.getTable().setLayout(layout);

		TableColumn orgNameColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		orgNameColumn.setText(resource.getString("ORGANIZATION"));
		orgNameColumn.setWidth(180);

		String[] list = ContrastCoreActivator.getOrganizationList();
		tableViewer.setInput(list);

		if (list.length > 0) {
			String orgName = ContrastCoreActivator.getDefaultOrganization();
			if (orgName != null && !orgName.isEmpty()) {
				tableViewer.getTable().setSelection(ArrayUtils.indexOf(list, orgName));
			} else {
				tableViewer.getTable().setSelection(0);
			}

		}

		return tableViewer;
	}

	private void retrieveOrganizationName(Composite composite) {

		final String url = teamServerText.getText();
		URL u;
		try {
			u = new URL(url);
		} catch (MalformedURLException e1) {
			MessageDialog.openError(getShell(), resource.getString("EXCEPTION"), resource.getString("INVALID_URL"));
			testConnectionLabel.setText(resource.getString("CONNECTION_FAILED"));
			return;
		}
		if (!u.getProtocol().startsWith("http")) {
			MessageDialog.openError(getShell(), resource.getString("EXCEPTION"), resource.getString("INVALID_PROTOCOL"));
			testConnectionLabel.setText(resource.getString("CONNECTION_FAILED"));
			return;
		}

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {

						ContrastSDK sdk = ContrastCoreActivator.getContrastSDK(usernameText.getText(),
								apiKeyText.getText(), serviceKeyText.getText(), url);

						try {
							Organizations organizations = sdk.getProfileOrganizations();
							if (organizations.getOrganizations() != null
									&& !organizations.getOrganizations().isEmpty()) {

								for (Organization organization : organizations.getOrganizations()) {

									if (organization.getOrgUuid().equals(organizationUuidText.getText())) {

										// Check if organization is already saved
										if (ContrastCoreActivator
												.getOrganizationConfiguration(organization.getName()) == null) {
											ContrastCoreActivator.saveNewOrganization(organization.getName(),
													teamServerText.getText(), usernameText.getText(),
													serviceKeyText.getText(), apiKeyText.getText(),
													organizationUuidText.getText());

											teamServerText.setText("");
											usernameText.setText("");
											serviceKeyText.setText("");
											apiKeyText.setText("");
											organizationUuidText.setText("");

											String[] organizationsArray = (String[]) tableViewer.getInput();

											String[] newOrganizationsArray = Arrays.copyOf(organizationsArray,
													organizationsArray.length + 1);

											newOrganizationsArray[newOrganizationsArray.length - 1] = organization
													.getName();
											tableViewer.setInput(newOrganizationsArray);
											if (newOrganizationsArray.length == 1) {
												tableViewer.getTable().setSelection(0);
											}
										} else {
											testConnectionLabel.setText(resource.getString("ORGANIZATION_EXISTS"));
										}
										break;
									}
								}
							}
						} catch (IOException e1) {
							showErrorMessage(e1, getShell(), resource.getString("CONNECTION_ERROR"), resource.getString("CONTRAST_ERROR"));
						} catch (UnauthorizedException e1) {
							showErrorMessage(e1, getShell(), resource.getString("ACCESS_DENIED"), resource.getString("VERIFY_CREDENTIALS_ERROR"));
						} catch (Exception e1) {
							showErrorMessage(e1, getShell(), resource.getString("UKNOWN_ERROR"), resource.getString("UNKNOWN_ERROR_INFO"));
						} finally {
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
}
