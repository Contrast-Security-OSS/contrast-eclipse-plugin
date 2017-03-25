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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ScrollBar;

public class ContrastTab extends Composite {

	private Composite control;
	
	public ContrastTab(Composite parent, int style) {
		super(parent, style);
		initialize();
	}

	private void initialize() {
		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		
		ScrolledComposite sc = new ScrolledComposite(this, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setLayout(new GridLayout());
		sc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sc.setAlwaysShowScrollBars(false);  
		sc.setExpandVertical(true);
		sc.setExpandHorizontal(true);
		
		control = new Composite(sc, SWT.BORDER);
		control.setLayout(new GridLayout());
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		control.setLayoutData(gd);
		
		sc.setContent(control);
		sc.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.addControlListener(new ControlAdapter() {
		
	        @Override 
	        public void controlResized(ControlEvent e) { 
	            Rectangle r = sc.getClientArea(); 
	            Control content = sc.getContent(); 
	            if (content != null && r != null) { 
	                Point minSize = content.computeSize(r.width, SWT.DEFAULT); 
	                sc.setMinSize(minSize); 
	                ScrollBar vBar = sc.getVerticalBar(); 
	                vBar.setPageIncrement(r.height); 
	            } 
	        } 
	      });
	}

	public Composite getControl() {
		return control;
	}

}
