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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.contrastsecurity.ide.eclipse.ui.internal.views.VulnerabilitiesView;

public class LoadingPage extends AbstractPage {

	public LoadingPage(Composite parent, int style, VulnerabilitiesView vulnerabilitiesView) {
		super(parent, style, vulnerabilitiesView);
		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		Composite comboComposite = new Composite(this, SWT.NONE);
		comboComposite.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		comboComposite.setLayoutData(gd);
		
		Label imageLabel = new Label(comboComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		imageLabel.setLayoutData(gd);
		
		Label textLabel = new Label(comboComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		textLabel.setLayoutData(gd);
		textLabel.setText("Loading...");
	}

}
