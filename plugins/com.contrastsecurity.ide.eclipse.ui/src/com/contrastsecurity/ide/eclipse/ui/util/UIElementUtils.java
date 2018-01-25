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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UIElementUtils {
	
	//====================  Label  ====================
	
	public static Label createLabel(Composite parent, String text) {
		return createLabel(parent, text, 1, 1);
	}
	
	public static Label createLabel(Composite parent, String text, int hSpan, int vSpan) {
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, hSpan, vSpan);
		return createBasicLabel(parent, gd, text);
	}
	
	public static Label createBasicLabel(Composite parent, GridData gd, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(gd);
		label.setText(text);
		
		return label;
	}
	
	public static Label createLogoLabel(Composite composite, Image image) {
		GridData gd = new GridData(SWT.END, SWT.FILL, true, false);
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(gd);
		label.setImage(image);
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
	
	public static Combo createCombo(Composite parent, String[] items, int horizontalSpan, int verticalSpan){
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, horizontalSpan, verticalSpan);
		
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(gd);
		combo.setItems(items);
		
		return combo;
	}
	
	public static ComboViewer createComboViewer(Composite composite) {
		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		comboViewer.getControl().setFont(composite.getFont());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		return comboViewer;
	}
	
	//====================  Text  ====================
	
	public static Text createMultiText(Composite parent, int verticalSpan) {
		return createMultiText(parent, verticalSpan, null);
	}
	
	public static Text createMultiText(Composite parent, int verticalSpan, Integer widthHint) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalSpan = verticalSpan;
		if(widthHint != null)
			gd.widthHint = widthHint;
		
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		text.setLayoutData(gd);
		
		return text;
	}
	
	public static Text createText(Composite parent, int hSpan, int vSpan) {
		return createText(parent, hSpan, vSpan, SWT.BORDER);
	}
	
	public static Text createText(Composite parent, int hSpan, int vSpan, int style) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, hSpan, vSpan);
		return createBasicText(parent, gd, style);
	}
	
	public static Text createBasicText(Composite parent, GridData gd, int style) {
		Text text = new Text(parent, style);
		text.setLayoutData(gd);
		return text;
	}
	
	//====================  Button  ====================
	
	public static Button createButton(Composite parent, String text) {
		return createButton(parent, text, null);
	}
	
	public static Button createButton(Composite parent, String text, Integer widthHint) {
		Button button = createBasicButton(parent, 1, 1, widthHint);
		button.setText(text);
		return button;
	}
	
	public static Button createButton(Composite parent, String text, int hSpan, int vSpan) {
		Button button = createBasicButton(parent, hSpan, vSpan, null);
		button.setText(text);
		return button;
	}
	
	public static Button createButton(Composite parent, Image image) {
		return createButton(parent, image, null);
	}
	
	public static Button createButton(Composite parent, Image image, Integer widthHint) {
		Button button = createBasicButton(parent, 1, 1, widthHint);
		button.setImage(image);
		return button;
	}
	
	public static Button createBasicButton(Composite parent, int hSpan, int vSpan, Integer widthHint) {
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, hSpan, vSpan);
		if(widthHint != null)
			gd.widthHint = widthHint;
		
		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(gd);
		
		return button;
	}
	
	public static Button createButton(Composite parent, GridData gd, String text) {
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
	
	/**
	 * Shows an error message box with the given parameters. This method should be used when trying to show it from other thread than the UI one. 
	 * @param display Current SWT display.
	 * @param shell Parent shell.
	 * @param title Box title.
	 * @param message The message to be displayed.
	 */
	public static void ShowErrorMessageFromAnotherThread(Display display, Shell shell, String title, String message) {
		display.asyncExec(new Runnable() {
			
			@Override
			public void run() {
				MessageDialog.openError(shell, title, message);
			}
		});
	}

}
