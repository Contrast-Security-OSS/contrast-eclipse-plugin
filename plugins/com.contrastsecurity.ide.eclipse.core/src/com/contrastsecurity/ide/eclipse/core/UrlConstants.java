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

public final class UrlConstants {

	public final static String EVENT_DETAILS = "/ng/%s/traces/%s/events/%s/details?expand=skip_links";
	public final static String EVENT_SUMMARY = "/ng/%s/traces/%s/events/summary?expand=skip_links";
	public final static String HTTP_REQUEST = "/ng/%s/traces/%s/httprequest?expand=skip_links";
	public final static String RECOMMENDATION = "/ng/%s/traces/%s/recommendation";
	public final static String TRACE = "/ng/%s/traces/%s/story?expand=skip_links";
	public final static String TRACE_TAGS = "/ng/%s/tags/traces/trace/%s";
	public final static String ORG_TAGS = "/ng/%s/tags/traces";
	public final static String TRACE_TAGS_DELETE = "/ng/%s/tags/trace/%s";
	public final static String MARK_STATUS = "/ng/%s/orgtraces/mark";
	public final static String GET_TRACE = "/ng/%s/orgtraces/filter/%s";

}