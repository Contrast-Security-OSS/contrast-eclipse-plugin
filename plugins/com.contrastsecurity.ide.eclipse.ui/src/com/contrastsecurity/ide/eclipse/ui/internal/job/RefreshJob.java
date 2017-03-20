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
package com.contrastsecurity.ide.eclipse.ui.internal.job;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.ui.internal.views.VulnerabilitiesView;

public class RefreshJob extends Job {

	private VulnerabilitiesView vulnerabilitiesView;

	public RefreshJob(String name, VulnerabilitiesView vulnerabilitiesView) {
		super(name);
		this.vulnerabilitiesView = vulnerabilitiesView;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		vulnerabilitiesView.refreshTraces();
		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}
		schedule(Constants.REFRESH_DELAY);
		return Status.OK_STATUS;
	}

}
