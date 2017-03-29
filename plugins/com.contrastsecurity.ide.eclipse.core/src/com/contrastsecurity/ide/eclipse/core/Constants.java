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
	static final String BLANK = "";
	static final String MUSTACHE_NL = "{{{nl}}}";
	// #0DA1A9
	static final Color LINK_COLOR = new Color(Display.getDefault(), 13, 161, 169);
	// #bfbfbf
	static final Color RULE_COLOR = new Color(Display.getDefault(), 191, 191, 191);

	static final Color LINK_COLOR_HOVER = Display.getCurrent().getSystemColor(SWT.COLOR_LINK_FOREGROUND);
	static final int REFRESH_DELAY = 5 * 60 * 1000; // 5 minutes
	static final Font SEVERITY_FONT = JFaceResources.getHeaderFont();
	// green - #aecd43 (r=174, g=205, b=67)
	// yellow - #f7b600 (r=247, g=182, b=0)
	// red - #e63025 (r=230, g=48, b=37)
	static final Color CREATION_COLOR = new Color(Display.getDefault(), 230, 48, 37);
	static final Color P20_COLOR = new Color(Display.getDefault(), 247, 182, 0);
	static final Color TAG_COLOR = new Color(Display.getDefault(), 174, 205, 67);

	// #165BAD
	static final Color LINK_COLOR2 = new Color(Display.getDefault(), 22,91,173);
	// #999999
	static final Color CONTENT_COLOR = new Color(Display.getDefault(), 153,153,153);
	// #1b7eb1 27,126,177
	static final Color CODE_COLOR = new Color(Display.getDefault(), 27,126,177);
	// #e0f2ef
	static final Color ITEM_BACKGROUND_COLOR = new Color(Display.getDefault(), 224,242,239);
	
}
