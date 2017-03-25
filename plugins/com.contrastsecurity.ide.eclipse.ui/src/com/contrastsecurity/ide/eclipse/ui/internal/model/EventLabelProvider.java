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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.extended.EventItem;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;

public class EventLabelProvider extends OwnerDrawLabelProvider {
	
	@Override
	protected void measure(Event event, Object element) {
		if (element instanceof EventItem) {
			//measureItem(event, element);
		} else {
			//event.height = 45;
		}
	}

	protected void measureItem(Event event, Object element) {
		Widget widget = event.widget;
		if (widget instanceof Tree && (event.index == 1 || event.index == 2)) {
			if (event.index == 1 || event.index == 2) {
				String text = getStyledText(element, 1).toString();
				Point extent = event.gc.stringExtent(text);
				event.width = extent.x / 2;
				event.height = Math.max(event.height, extent.y + 2);
			}
		}
	}

	@Override
	protected void paint(Event event, Object element) {
		if (element instanceof EventItem) {
			switch (event.index) {
			case 0:
				event.gc.drawString(getStyledText(element, event.index).toString(), event.x, event.y);
				break;
			case 1:
				String text = getStyledText(element, 1).toString();
				Point extent = event.gc.stringExtent(text);
				int y = event.y + (event.height - extent.y) / 2;
				event.gc.setForeground(Constants.RULE_COLOR);
				event.gc.drawString(getStyledText(element, event.index).toString(), event.x, y);
				break;
			case 2:
				Tree tree = (Tree) event.widget;
				int offset = tree.getColumn(1).getWidth();
				text = getStyledText(element, 1).toString();
				extent = event.gc.stringExtent(text);
				y = event.y + (event.height - extent.y) / 2;
				if (((EventItem) element).getType() == null) {
					event.gc.setForeground(Constants.LINK_COLOR);
					Font font = JFaceResources.getFont("org.eclipse.debug.ui.consoleFont");
					if (font != null) {
						event.gc.setFont(font);
					}
				} else {
					event.gc.setForeground(Constants.RULE_COLOR);
				}
				event.gc.drawString(text, event.x - offset, y);
				break;
			default:
				break;
			}
		} else {
			if (event.index == 0) {
				event.gc.setFont(JFaceResources.getHeaderFont());
				event.gc.setForeground(Constants.RULE_COLOR);
			}
			int x = event.x + 5;
			if (event.index == 0 || event.index == 2) {
				String text = getStyledText(element, 1).toString();
				Point extent = event.gc.stringExtent(text);
				int y = event.y + (event.height - extent.y) / 2;
				event.gc.drawString(getStyledText(element, event.index).toString(), x, y);
			} else {
				int y = event.y;
				event.gc.drawString(getStyledText(element, event.index).toString(), x + 5, y);
			}
		}
	}

	public StyledString getStyledText(Object element, int index) {
		if (element instanceof EventResource) {
			EventResource eventResource = (EventResource) element;
			return getStyledText(eventResource, index);
		} else if (element instanceof EventItem) {
			EventItem eventItem = (EventItem) element;
			return getStyledText(eventItem, index);
		}
		return new StyledString();
	}

	private StyledString getStyledText(EventItem eventItem, int index) {
		StyledString styledString;
		switch (index) {
		case 0:
			styledString = new StyledString(eventItem.getType());
			return styledString;
		case 1:
			styledString = new StyledString(eventItem.getValue());
			return styledString;
		default:
			return new StyledString();
		}
	}

	private StyledString getStyledText(EventResource eventResource, int index) {
		StyledString styledString;
		switch (index) {
		case 0:
			styledString = new StyledString(eventResource.getTypeDescription());
			return styledString;
		case 1:
			String codeRecreation = eventResource.getCodeRecreation();
			codeRecreation = ContrastUIActivator.removeHtmlMarkup(codeRecreation);
			styledString = new StyledString(codeRecreation);
			return styledString;
		case 2:
			String snapshot = eventResource.getHtmlDataSnapshot();
			snapshot = ContrastUIActivator.removeHtmlMarkup(snapshot);
			styledString = new StyledString(snapshot);
			return styledString;
		default:
			return new StyledString();
		}
	}

	@Override
	public void dispose() {

	}

}