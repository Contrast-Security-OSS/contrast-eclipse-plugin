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

import java.net.URLDecoder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.unbescape.html.HtmlEscape;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.models.HttpRequestResponse;

public class HttpRequestTab extends Composite {

	private StyledText area;
	private HttpRequestResponse httpRequest;

	public HttpRequestTab(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		setLayoutData(gd);
		Composite control = new Composite(this, SWT.NONE);
		control.setLayout(new GridLayout());
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		control.setLayoutData(gd);
		area = new StyledText(control, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		area.setLayoutData(gd);
		area.setEditable(false);
	}

	public StyledText getArea() {
		return area;
	}

	public HttpRequestResponse getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequestResponse httpRequest) {
		this.httpRequest = httpRequest;
		area.setText(Constants.BLANK);
		if (httpRequest != null && httpRequest.getHttpRequest() != null
				&& httpRequest.getHttpRequest().getText() != null) {

			area.setText(Util.filterHeaders(httpRequest.getHttpRequest().getText(), "\n"));
		} else if (httpRequest != null && httpRequest.getReason() != null) {
			area.setText(httpRequest.getReason());
		}
		String text = area.getText();
		// text = StringEscapeUtils.unescapeHtml(text);
		text = HtmlEscape.unescapeHtml(text);
		try {
			text = URLDecoder.decode(text, "UTF-8");
		} catch (Exception e) {
			// ignore
			if (ContrastUIActivator.getDefault().isDebugging()) {
				ContrastUIActivator.log(e);
			}
		}
		if (text.contains(Constants.TAINT) && text.contains(Constants.TAINT_CLOSED)) {

			String currentString = text;
			int start = text.indexOf(Constants.TAINT);
			currentString = currentString.replace(Constants.TAINT, "");
			int end = currentString.indexOf(Constants.TAINT_CLOSED);
			if (end > start) {
				currentString = currentString.replace(Constants.TAINT_CLOSED, "");
				area.setText(currentString);
				StyleRange styleRange = new StyleRange();
				styleRange.start = start;
				styleRange.length = end - start;
				styleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
				area.setStyleRange(styleRange);
			}
		}
	}

}
