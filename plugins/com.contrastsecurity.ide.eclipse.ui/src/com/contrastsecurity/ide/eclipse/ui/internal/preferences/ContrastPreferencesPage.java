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
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.constants.Constants;
import com.contrastsecurity.ide.eclipse.core.constants.SettingsConstants;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;
import com.contrastsecurity.ide.eclipse.core.util.ConnectionConfigUtil;
import com.contrastsecurity.ide.eclipse.core.util.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.PreferencesLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.sdk.ContrastSDK;

public class ContrastPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage";
	private static final String BLANK = "";
	
	private Button testConnection;
	private Label testConnectionLabel;
	
	private TableViewer table;
	private CheckboxTableViewer checkTable;
	private Map<String, ConnectionConfig> configs;
	private String selectedConfigId;
	
	private Button addConnectionBtn;
	private Button editConnectionBtn;
	private Button deleteConnectionBtn;
	
	private ConnectionConfigDialog configDialog;

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
		prefs.put(SettingsConstants.CURRENT_URL, Constants.TEAM_SERVER_URL_VALUE);
		super.performDefaults();
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		ContrastCoreActivator.saveConfigurations(configs);
		
		if(StringUtils.isNotBlank(selectedConfigId))
			ContrastCoreActivator.setSelectedConfiguration(selectedConfigId, configs.get(selectedConfigId));
		else
			ContrastCoreActivator.clearSelectedConfig();
		
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
		
		table = new TableViewer(composite, SWT.CHECK | SWT.SINGLE | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLabelProvider(new PreferencesLabelProvider());
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 6));
		
		TableColumn column = new TableColumn(table.getTable(), SWT.NONE);
		column.setText("Username");
		column.setWidth(180);
		
		column = new TableColumn(table.getTable(), SWT.NONE);
		column.setText("Organization name");
		column.setWidth(180);
		
		column = new TableColumn(table.getTable(), SWT.NONE);
		column.setText("Organization UUID");
		column.setWidth(220);
		
		table.getTable().setHeaderVisible(true);
		table.getTable().setLinesVisible(true);
		table.setContentProvider(ArrayContentProvider.getInstance());
		TableLayout tableLayout = new TableLayout();
		table.getTable().setLayout(tableLayout);
		
		GridData btnGd = new GridData(SWT.RIGHT, SWT.TOP, false, false, 1, 1);
		addConnectionBtn = UIElementUtils.createButton(composite, btnGd, "Add");
		editConnectionBtn = UIElementUtils.createButton(composite, btnGd, "Edit");
		deleteConnectionBtn = UIElementUtils.createButton(composite, btnGd, "Delete");
		
		configs = ContrastCoreActivator.getConfigurations();
		table.setInput(configs.values());
		
		checkTable = new CheckboxTableViewer(table.getTable());
		checkTable.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				checkTable.setAllChecked(false);
				checkTable.setChecked(event.getElement(), true);
				ConnectionConfig config = (ConnectionConfig) event.getElement();
				selectedConfigId = ConnectionConfigUtil.generateConfigurationKey(config);
			}
		});
		
		String selectedKey = ContrastCoreActivator.getSelectedConfigKey();
		if(StringUtils.isNotBlank(selectedKey) && configs.containsKey(selectedKey)) {
			selectedConfigId = selectedKey;
			table.setSelection(new StructuredSelection(configs.get(selectedKey)), true);
			checkTable.setChecked(configs.get(selectedKey), true);
		}
		
		IConnectionConfigListener listener = new IConnectionConfigListener() {
			
			@Override
			public void onConnectionSave(ConnectionConfig config) {
				String key = ConnectionConfigUtil.generateConfigurationKey(config);
				saveConnectionConfig(config, key);
				updateSelection(key);
				enableTestConnection();
				editConnectionBtn.setEnabled(true);
				deleteConnectionBtn.setEnabled(true);
			}
			
			@Override
			public void onConnectionUpdate(ConnectionConfig config, String previousKey) {
				String key = ConnectionConfigUtil.generateConfigurationKey(config);
				updateConnectionConfig(config, key, previousKey);
			}
		};
		
		addConnectionBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				onAddConfigPressed(listener);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		editConnectionBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				onEditConfigPressed(listener);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		deleteConnectionBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDeleteConfigPressed();
				enableTestConnection();
				enableEditConnection();
				enableDeleteConnection();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		UIElementUtils.createLabel(composite, BLANK);
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		testConnection = UIElementUtils.createButton(composite, gd, "Test Connection");
		testConnection.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				testConnection(composite);
			}

		});
		
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 3, 1);
		testConnectionLabel = UIElementUtils.createBasicLabel(composite, gd, "");
		
		enableTestConnection();
		enableEditConnection();
		enableDeleteConnection();
		
		return composite;
	}
	
	private void enableTestConnection() {
		testConnection.setEnabled(!configs.isEmpty());
	}
	
	private void enableEditConnection() {
		if(table.getSelection() instanceof IStructuredSelection && table.getStructuredSelection().getFirstElement() != null)
			editConnectionBtn.setEnabled(true);
		else
			editConnectionBtn.setEnabled(false);
	}
	
	private void enableDeleteConnection() {
		deleteConnectionBtn.setEnabled(configs.size() > 0);
	}
	
	//===================== Selection listener functions ========================
	private void testConnection(Composite composite) {
		ConnectionConfig config = configs.get(selectedConfigId);
		
		final String url = config.getTsUrl();
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
						ContrastSDK sdk = new ContrastSDK(config.getUsername(), config.getServiceKey(),
								config.getApiKey(), url);
						try {
							Organization organization = Util.getDefaultOrganization(sdk);
							if (organization == null || organization.getOrgUuid() == null) {
								testConnectionLabel.setText("Connection is correct, but no default organizations found.");
							} else {
								testConnectionLabel.setText("Connection confirmed!");
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
	
	//===================== Configuration buttons listener functions ========================
	private void onAddConfigPressed(final IConnectionConfigListener listener) {
		String lastUsername = "";
		String lastServiceKey = "";
		String lastTsUrl = "";
		
		if(StringUtils.isNotBlank(selectedConfigId)) {
			ConnectionConfig config = configs.get(selectedConfigId);
			lastUsername = config.getUsername();
			lastServiceKey = config.getServiceKey();
			lastTsUrl = config.getTsUrl();
		}
		
		configDialog = new ConnectionConfigDialog(getShell(), lastUsername, lastServiceKey, lastTsUrl, listener);
		configDialog.open();
	}
	
	private void onEditConfigPressed(final IConnectionConfigListener listener) {
		ISelection selection = table.getSelection();
		
		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof ConnectionConfig) {
			ConnectionConfig config = (ConnectionConfig) ((IStructuredSelection) selection).getFirstElement();
			configDialog = new ConnectionConfigDialog(getShell(), config, listener);
			configDialog.open();
		}
	}
	
	private void onDeleteConfigPressed() {
		ISelection selection = table.getSelection();
		
		if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof ConnectionConfig) {
			ConnectionConfig config = (ConnectionConfig) ((IStructuredSelection) selection).getFirstElement();
			String configKey = ConnectionConfigUtil.generateConfigurationKey(config);
			table.remove(config);
			configs.remove(configKey);
			
			if(configs.size() > 0) {
				ConnectionConfig firstConfig = (ConnectionConfig) table.getElementAt(0);
				table.setSelection(new StructuredSelection(firstConfig), true);
				
				if(!selectedConfigId.equals(configKey))
					return;
				
				checkTable.setChecked(firstConfig, true);
				selectedConfigId = ConnectionConfigUtil.generateConfigurationKey(firstConfig);
			}
			else if(configs.size() == 0)
				selectedConfigId = "";
		}
	}
	//===================== Configuration changes listener functions ========================
	private void saveConnectionConfig(ConnectionConfig config, String key) {
		configs.put(key, config);
		if(configDialog != null)
			configDialog.close();
		
		table.setInput(configs.values());
	}
	
	private void updateSelection(String key) {
		table.setSelection(new StructuredSelection(configs.get(key)), true);
		
		if(table.getTable().getItemCount() == 1) {
			checkTable.setChecked(configs.get(key), true);
			selectedConfigId = key;
		}
		else {
			ConnectionConfig selected = configs.get(selectedConfigId);
			checkTable.setChecked(selected, true);
		}
	}
	
	private void updateConnectionConfig(ConnectionConfig config, String newKey, String previousKey) {
		saveConnectionConfig(config, newKey);
		
		if(newKey.equals(previousKey))
			return;
		
		ConnectionConfig previousConfig = configs.get(previousKey);
		configs.remove(previousKey);
		table.remove(previousConfig);
		table.setSelection(new StructuredSelection(config), true);
		
		if(selectedConfigId.equals(previousKey)) {
			checkTable.setChecked(config, true);
			selectedConfigId = newKey;
		}
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

}
