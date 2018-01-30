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

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;

public class PreferencesLabelProvider extends StyledCellLabelProvider {

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		if (element instanceof ConnectionConfig) {
			ConnectionConfig config = (ConnectionConfig) element;
			int index = cell.getColumnIndex();
			switch (index) {
			case 0:
				cell.setText(config.getUsername());
				break;
			case 1:
				cell.setText(config.getOrgName());
				break;
			case 2:
				cell.setText(config.getOrgId());
				break;
			default:
				break;
			}
			if (index == 0) {

			}
		}
		super.update(cell);
	}

}
