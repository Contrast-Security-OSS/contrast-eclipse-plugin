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

import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;

public class ContrastPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text teamServerText;
	private Text serviceKeyText;
	private Text apiKeyText;

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
		createLabel(composite, "");
		Button testConnection = new Button(composite, SWT.PUSH);
		testConnection.setText("Test Connection");
		gd = new GridData(SWT.CENTER, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		testConnection.setLayoutData(gd);
		initPreferences();
		return composite;
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
		serviceKeyText.setText(prefs.get(Constants.SERVICE_KEY, ""));
		apiKeyText.setText(prefs.get(Constants.API_KEY, ""));
	}
	
	private IEclipsePreferences  getPreferences() {
		return DefaultScope.INSTANCE.getNode(ContrastCoreActivator.PLUGIN_ID);
	}

	@Override
	public void init(IWorkbench workbench) {
		// Nothing to do
	}

}
