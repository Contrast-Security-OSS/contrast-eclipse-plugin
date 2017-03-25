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

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;

public class EventsTab extends AbstractTab {

	private EventSummaryResource eventSummary;
	private TreeViewer viewer;

	public EventsTab(Composite parent, int style) {
		super(parent, style);
		TreeColumnLayout layout = new TreeColumnLayout();
		getControl().setLayout(layout);
		viewer = new TreeViewer(getControl(), SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTree().setLayoutData(gd);
		viewer.getTree().setHeaderVisible(true);

		TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		typeColumn.getColumn().setText("Type");
		layout.setColumnData(typeColumn.getColumn(), new ColumnWeightData(200)); 

		TreeViewerColumn codeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		codeColumn.getColumn().setText("Code");
		layout.setColumnData(codeColumn.getColumn(), new ColumnWeightData(300)); 

		TreeViewerColumn htmlColumn = new TreeViewerColumn(viewer, SWT.NONE);
		htmlColumn.getColumn().setText("Snapshot");
		layout.setColumnData(htmlColumn.getColumn(), new ColumnWeightData(200)); 

		viewer.setLabelProvider(new EventLabelProvider());
		viewer.setContentProvider(new EventContentProvider());
	}

	public void setEventSummary(EventSummaryResource eventSummary) {
		this.eventSummary = eventSummary;
		if (eventSummary != null) {
			viewer.setInput(eventSummary.getEvents().toArray(new EventResource[0]));
		} else {
			viewer.setInput(new EventResource[0]);
		}
		getControl().getParent().layout(true, true);
		getControl().redraw();
	}

	public EventSummaryResource getEventSummary() {
		return eventSummary;
	}

}
