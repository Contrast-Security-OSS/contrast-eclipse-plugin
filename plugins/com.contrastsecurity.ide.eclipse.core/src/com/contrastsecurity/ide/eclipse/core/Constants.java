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
package com.contrastsecurity.ide.eclipse.core;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public interface Constants {
	static final String TEAM_SERVER_URL = "contrast.we.url";
	static final String TEAM_SERVER_URL_VALUE = "https://app.contrastsecurity.com/Contrast/api";
	static final String SERVICE_KEY = "service.key";
	static final String API_KEY = "api.key";
	static final String USERNAME = "username";
	static final String ORGNAME = "orgname";
	static final String ORGUUID = "orguuid";
	static final String SERVER_ID = "serverId";
	static final String APPLICATION_ID = "applicationId";
	static final long ALL_SERVERS = -1l;
	static final String ALL_APPLICATIONS = "All applications";
	// #0DA1A9
	static final Color LINK_COLOR = new Color(Display.getDefault(), 13 , 161, 169);
	static final Color LINK_COLOR_HOVER = Display.getCurrent().getSystemColor(SWT.COLOR_LINK_FOREGROUND); //new Color(Display.getDefault(), 13 , 161, 169, 120);
	static final int REFRESH_DELAY = 5 * 60 * 1000; // 5 minutes
	static final Color NOTE_COLOR_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	static final Color NOTE_COLOR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	//f98a1f 249,138,31
	static final Color HIGH_COLOR_BACKGROUND = new Color(Display.getDefault(), 249 , 138, 31);
	static final Color HIGH_COLOR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	static final Color MEDIUM_COLOR_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	static final Color MEDIUM_COLOR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	static final Color LOW_COLOR_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	static final Color LOW_COLOR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	static final Color CRITICAL_COLOR_BACKGROUND = Display.getDefault().getSystemColor(SWT.COLOR_RED);;
	static final Color CRITICAL_COLOR_FOREGROUND = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
	static final Font SEVERITY_FONT = JFaceResources.getHeaderFont();
}
