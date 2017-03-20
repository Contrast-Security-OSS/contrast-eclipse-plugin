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

public class OverviewTab extends Composite {

	private Composite control;

	public OverviewTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		control = new Composite(this, SWT.BORDER);
		control.setLayout(new GridLayout());
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		control.setLayoutData(gd);
	}

	public Composite getControl() {
		return control;
	}

}
