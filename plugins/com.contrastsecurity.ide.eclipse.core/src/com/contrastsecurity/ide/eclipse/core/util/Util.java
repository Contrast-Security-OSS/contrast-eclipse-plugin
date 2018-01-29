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
package com.contrastsecurity.ide.eclipse.core.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.constants.SettingsConstants;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.sdk.ContrastSDK;

public class Util {

	public static Organization getDefaultOrganization(ContrastSDK sdk) throws IOException, UnauthorizedException {
		if (sdk == null) {
			return null;
		}
		Organizations organizations = sdk.getProfileDefaultOrganizations();
		return organizations.getOrganization();
	}

	public static boolean hasConfiguration() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		// String uuid = prefs.get(Constants.ORGUUID, null);
		String apiKey = prefs.get(SettingsConstants.CURRENT_API_KEY, null);
		String serviceKey = prefs.get(SettingsConstants.CURRENT_SERVICE_KEY, null);
		String username = prefs.get(SettingsConstants.CURRENT_USERNAME, null);
		return apiKey != null && serviceKey != null && username != null && !apiKey.isEmpty() && !serviceKey.isEmpty()
				&& !username.isEmpty();
	}

	public static String[] extractOrganizationNames(List<Organization> orgList) {
		String[] orgArray = new String[orgList.size()];

		for (int i = 0; i < orgList.size(); i++)
			orgArray[i] = orgList.get(i).getName();

		return orgArray;
	}

	public static TraceFilterForm getTraceFilterForm(final int offset, final int limit, String sort) {
		return getTraceFilterForm(null, offset, limit, sort);
	}

	public static TraceFilterForm getTraceFilterForm(final Long selectedServerId, final int offset, final int limit,
			String sort) {
		final TraceFilterForm form = new TraceFilterForm();
		if (selectedServerId != null) {
			final List<Long> serverIds = new ArrayList<>();
			serverIds.add(selectedServerId);
			form.setServerIds(serverIds);
		}

		form.setOffset(offset);
		form.setLimit(limit);
		form.setSort(sort);

		return form;
	}

	public static TraceFilterForm getTraceFilterForm(final int offset, final int limit) {
		return getTraceFilterForm(null, offset, limit);
	}

	public static TraceFilterForm getTraceFilterForm(final Long selectedServerId, final int offset, final int limit) {
		final TraceFilterForm form = new TraceFilterForm();
		if (selectedServerId != null) {
			final List<Long> serverIds = new ArrayList<>();
			serverIds.add(selectedServerId);
			form.setServerIds(serverIds);
		}
		form.setOffset(offset);
		form.setLimit(limit);

		return form;
	}
}
