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
import org.eclipse.swt.graphics.Image;

import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;

public class TagLabelProvider extends StyledCellLabelProvider {

	private Image getImage(int columnIndex) {
		if (columnIndex == 1) {
			return ContrastUIActivator.getImage("/icons/remove.png");
		} else {
			return null;
		}
	}

	private String getText(Object element, int columnIndex) {
		String elementToReturn = null;
		if (element instanceof String) {
			switch (columnIndex) {
			case 0:
				elementToReturn = (String) element;
				break;
			case 1:
				if (getImage(columnIndex) == null) {
					elementToReturn = "Remove";
				}
			default:
				break;
			}
		}
		return elementToReturn;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();

		int index = cell.getColumnIndex();
		switch (index) {
		case 0:
			String title = getText(element, index);
			cell.setText(title);
			break;
		case 1:
			// Image image = getImage(index);
			// cell.setImage(image);
			break;
		default:
			break;
		}
		super.update(cell);
	}
}
