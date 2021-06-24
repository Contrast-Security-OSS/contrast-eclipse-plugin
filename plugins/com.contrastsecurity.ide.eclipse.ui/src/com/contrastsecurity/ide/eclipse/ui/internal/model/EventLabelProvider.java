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

import java.util.ResourceBundle;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.unbescape.html.HtmlEscape;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.models.EventItem;
import com.contrastsecurity.models.EventResource;

public class EventLabelProvider extends OwnerDrawLabelProvider {


	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

	private static final String INTERESTING_SECURITY_EVENT_OCCURED_ON_DATA = resource.getString("SECURITY_EVENT");
	private TreeViewer viewer;

	public EventLabelProvider(TreeViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	protected void measure(Event event, Object element) {
//		event.width = viewer.getTree().getColumn(event.index).getWidth();
//		if (event.width == 0)
//			return;
//		if (element instanceof EventItem) {
//			if ( EventResource.CONTENT.equals( ((EventItem)element).getType())) {
//				Point extent = event.gc.stringExtent("Test");
//				event.height = (int) (extent.y * 2);
//			}
//		}
	}

	@Override
	protected void paint(Event event, Object element) {
		if (element instanceof EventItem) {
			EventItem eventItem = (EventItem) element;
			Color background = event.gc.getBackground();
			Color foreground = event.gc.getForeground();
			Font font = event.gc.getFont();
			FontData[] fontData = font.getFontData();
			for (int i = 0; i < fontData.length; i++) {
				fontData[i].setStyle(SWT.BOLD);
				fontData[i].setHeight(fontData[i].getHeight() + 1);
			}
			Font boldFont = null;
			try {
				boldFont = new Font(Display.getCurrent(), fontData);
				int x = event.x + 15;
				switch (eventItem.getType()) {
				case EventResource.RED:
					x = x + 15;
					event.gc.setForeground(Constants.CREATION_COLOR);
					break;
				case EventResource.CONTENT:
					x = x + 15;
					event.gc.setForeground(Constants.CONTENT_COLOR);
					break;
				case EventResource.CODE:
					x = x + 15;
					event.gc.setForeground(Constants.CODE_COLOR);
					break;
				case EventResource.BOLD:
					event.gc.setFont(boldFont);
					break;
				default:
					break;
				}
				event.gc.setBackground(Constants.ITEM_BACKGROUND_COLOR);
				//Rectangle clipping = event.gc.getClipping();
				//event.gc.setClipping(clipping.x, clipping.y, viewer.getTree().getColumn(0).getWidth(), clipping.height + 5);
				Rectangle bounds = event.getBounds();
				bounds.width = viewer.getTree().getColumn(0).getWidth();
				bounds.height += 5;
				event.gc.fillRectangle(bounds);
				//event.gc.setClipping(clipping.x, clipping.y, clipping.width, clipping.height);
				String value = eventItem.getValue();
				if (value != null) {
					//value = StringEscapeUtils.unescapeHtml(value);
					value = HtmlEscape.unescapeHtml(value);
				}
				event.gc.drawString(eventItem.getValue(), x, event.y);
			} finally {
				if (boldFont != null) {
					boldFont.dispose();
				}
			}
			event.gc.setBackground(background);
			event.gc.setForeground(foreground);
			event.gc.setFont(font);
		} else {
			if (element instanceof EventResource) {
				EventResource eventResource = (EventResource) element;
				Color background = event.gc.getBackground();
				Color foreground = event.gc.getForeground();
				//Rectangle clipping = event.gc.getClipping(); //TODO Remove if not used
				// event.gc.setLineWidth(1);
				Point size = event.gc.stringExtent(INTERESTING_SECURITY_EVENT_OCCURED_ON_DATA);
				Color color = getColor(eventResource.getType());
				if (color != null) {
					event.gc.setBackground(color);
				} else {
					event.gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				}
				event.gc.fillOval(event.x + 8, event.y + size.y / 2 - 2, 10, 10);
				event.gc.setBackground(background);
//				if (color != null) {
//					event.gc.setForeground(color);
//				} else {
//					event.gc.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
//				}
				String type = eventResource.getDescription().toUpperCase();
				event.gc.drawString(type, event.x + 30, event.y + 2);
				size = event.gc.stringExtent(INTERESTING_SECURITY_EVENT_OCCURED_ON_DATA);
				event.gc.setBackground(background);
				event.gc.setForeground(foreground);
			}

		}
	}

	private Color getColor(String type) {

		Color color = null;
		if (type != null) {
			switch (type.toLowerCase()) {
			case "creation":
				color = Constants.ICON_COLOR_CREATION;
				break;
			case "trigger":
				color = Constants.ICON_COLOR_TRIGGER;
				break;
			case "tag":
				color = Constants.TAG_COLOR;
				break;
			default:
				color = Constants.ICON_COLOR_PROPAGATION;
				break;
			}
		}
		return color;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void erase(Event event, Object element) {
		event.detail &= ~SWT.FOREGROUND;
	}

}
