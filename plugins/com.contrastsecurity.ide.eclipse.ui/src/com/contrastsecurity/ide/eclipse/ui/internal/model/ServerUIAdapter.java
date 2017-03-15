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

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.models.Server;

public class ServerUIAdapter implements IContrastLabelProvider {
	
	private Server server;
	private String name;

	public ServerUIAdapter(Server server, String name) {
		this.server = server;
		this.name = name;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return name;
	}
	
	public Long getId() {
		if (server != null) {
			return server.getServerId();
		}
		return Constants.ALL_SERVERS;
	}

}
