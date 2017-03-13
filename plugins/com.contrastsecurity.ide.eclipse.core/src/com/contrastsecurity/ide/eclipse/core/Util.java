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

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.sdk.ContrastSDK;

public class Util {
	
	public static String getOrgUuid(ContrastSDK sdk) throws IOException, UnauthorizedException {
		Organizations organizations = sdk.getProfileOrganizations();
		String orgUiid = null;
		if (organizations.getOrganizations() != null && organizations.getOrganizations().size() > 0) {
			orgUiid = organizations.getOrganizations().get(0).getOrgUuid();
		}
		return orgUiid;
	}

}
