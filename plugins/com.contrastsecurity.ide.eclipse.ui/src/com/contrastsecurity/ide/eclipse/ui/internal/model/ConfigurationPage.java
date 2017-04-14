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
package com.contrastsecurity.ide.eclipse.ui.internal.model;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage;
import com.contrastsecurity.ide.eclipse.ui.internal.views.VulnerabilitiesView;

public class ConfigurationPage extends AbstractPage {

	public ConfigurationPage(Composite parent, int style, VulnerabilitiesView vulnerabilitiesView) {
		super(parent, style, vulnerabilitiesView);
		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		Composite comboComposite = new Composite(this, SWT.NONE);
		comboComposite.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		comboComposite.setLayoutData(gd);

		Label textLabel = new Label(comboComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		textLabel.setLayoutData(gd);
		textLabel.setText("Contrast is not configured to report to a Team Server");
		
		Label preferencesLink = new Label(comboComposite, SWT.NONE);
		gd = new GridData(SWT.END, SWT.FILL, false, false);
		preferencesLink.setLayoutData(gd);
		preferencesLink.setText("Contrast Preferences");
		preferencesLink.setForeground(Constants.LINK_COLOR);
		preferencesLink.addMouseTrackListener(new MouseTrackListener() {

			@Override
			public void mouseHover(MouseEvent e) {
				//
			}

			@Override
			public void mouseExit(MouseEvent e) {
				preferencesLink.setForeground(Constants.LINK_COLOR);
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				preferencesLink.setForeground(Constants.LINK_COLOR_HOVER);
			}
		});
		preferencesLink.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
						getVulnerabilitiesView().getSite().getShell(), ContrastPreferencesPage.ID, null, null);
				dialog.open();
				vulnerabilitiesView.refreshSdk();
				vulnerabilitiesView.refreshTraces();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});
	}

}
