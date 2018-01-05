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
package com.contrastsecurity.ide.eclipse.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UIElementUtils {
	
	//====================  Label  ====================
	
	public static Label createLabel(Composite parent, String text) {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(gd);
		label.setText(text);
		return label;
	}
	
	//====================  ComboView  ====================
	
	public static Combo createCombo(Composite parent, String[] items) {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(gd);
		combo.setItems(items);
		
		return combo;
	}
	
	//====================  Text  ====================
	
	public static Text createText(Composite parent, int verticalSpan) {
		return createText(parent, verticalSpan, null);
	}
	
	public static Text createText(Composite parent, int verticalSpan, Integer widthHint) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalSpan = verticalSpan;
		if(widthHint != null)
			gd.widthHint = widthHint;
		
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		text.setLayoutData(gd);
		
		return text;
	}
	
	//====================  Button  ====================
	
	public static Button createButton(Composite parent, String text) {
		return createButton(parent, text, null);
	}
	
	public static Button createButton(Composite parent, String text, Integer widthHint) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		if(widthHint != null)
			gd.widthHint = widthHint;
		
		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(gd);
		button.setText(text);
		
		return button;
	}
	
	//====================  MenuItem  ====================
	
	public static MenuItem generateMenuItem(Menu parent, String text, int style, SelectionListener listener) {
		MenuItem item = new MenuItem(parent, style);
		item.setText(text);
		item.addSelectionListener(listener);
		return item;
	}
	
	//====================  MessageBox  ====================
	
	/**
	 * Instantiates and shows an error MessageBox with the given text.
	 * @param shell Parent shell.
	 * @param message The message to be displayed.
	 */
	public static void ShowErrorMessage(Shell shell, String message) {
		MessageBox box = new MessageBox(shell, SWT.ICON_ERROR);
		box.setMessage(message);
		box.open();
	}

}
