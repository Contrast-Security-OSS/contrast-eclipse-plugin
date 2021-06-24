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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.contrastsecurity.models.EventItem;
import com.contrastsecurity.models.EventResource;

public class EventContentProvider implements ITreeContentProvider {
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return (EventResource[]) inputElement;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof EventResource) {
			EventResource eventResource = (EventResource) parentElement;

			if(eventResource.getCollapsedEvents() != null && !eventResource.getCollapsedEvents().isEmpty())
				return eventResource.getCollapsedEvents().toArray();

			return eventResource.getItems();
		}
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof EventItem) {
			return ((EventItem) element).getParent();
		}
		else if(element instanceof EventResource)
			return ((EventResource) element).getParent();

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof EventResource) {
			return true;
		}
		return false;
	}

}
