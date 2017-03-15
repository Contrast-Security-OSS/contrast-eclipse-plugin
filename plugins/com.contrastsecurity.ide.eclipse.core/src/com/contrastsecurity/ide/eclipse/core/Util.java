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

import java.io.IOException;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.contrastsecurity.exceptions.UnauthorizedException;
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
	
	public static String getDefaultOrganizationUuid() throws IOException, UnauthorizedException {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		String uuid = prefs.get(Constants.ORGUUID, null);
		if (uuid == null) {
			Organization organization = getDefaultOrganization(ContrastCoreActivator.getContrastSDK());
			if (organization != null) {
				prefs.put(Constants.ORGNAME, organization.getName());
				prefs.put(Constants.ORGUUID, organization.getOrgUuid());
				return organization.getOrgUuid();
			}
		}
		return uuid;
	}

}
