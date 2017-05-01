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
package com.contrastsecurity.ide.eclipse.ui.cache;

public class Key {
	private String orgUuid;
	private String traceId;

	public String getOrgUuid() {
		return orgUuid;
	}

	public Key(String orgUuid, String traceId) {
		this.orgUuid = orgUuid;
		this.traceId = traceId;
	}

	public void setOrgUuid(String orgUuid) {
		this.orgUuid = orgUuid;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orgUuid == null) ? 0 : orgUuid.hashCode());
		result = prime * result + ((traceId == null) ? 0 : traceId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (orgUuid == null) {
			if (other.orgUuid != null)
				return false;
		} else if (!orgUuid.equals(other.orgUuid))
			return false;
		if (traceId == null) {
			if (other.traceId != null)
				return false;
		} else if (!traceId.equals(other.traceId))
			return false;
		return true;
	}

}
