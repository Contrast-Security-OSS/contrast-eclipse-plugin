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

import org.apache.commons.lang.StringEscapeUtils;
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
import com.contrastsecurity.ide.eclipse.core.extended.EventItem;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;

public class EventLabelProvider extends OwnerDrawLabelProvider {

	private static final String INTERESTING_SECURITY_EVENT_OCCURED_ON_DATA = "INTERESTING SECURITY EVENT OCCURED ON DATA";
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
				Rectangle clipping = event.gc.getClipping();
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
				String type = eventResource.getTypeDescription().toUpperCase();
				event.gc.drawString(type, event.x + 30, event.y + 2);
				size = event.gc.stringExtent(INTERESTING_SECURITY_EVENT_OCCURED_ON_DATA);
				event.gc.setBackground(background);
				event.gc.setForeground(foreground);
				Font oldFont = event.gc.getFont();
				FontData[] fontData = oldFont.getFontData();
				for (int i = 0; i < fontData.length; i++) {
					fontData[i].setStyle(SWT.BOLD);
				}
				FontData[] fontDataItalic = oldFont.getFontData();
				for (int i = 0; i < fontDataItalic.length; i++) {
					fontDataItalic[i].setStyle(SWT.ITALIC);
				}
				//Font boldFont = null;
				Font italicFont = null;
				int x = event.x + 45 + size.x;
				int startX = x;
				try {
					//boldFont = new Font(Display.getCurrent(), fontData);
					italicFont = new Font(Display.getCurrent(), fontDataItalic);
					int maxWidth = getMaxWidth(event, oldFont);
					if (clipping.width > x + maxWidth) {
						event.gc.setClipping(clipping.x, clipping.y, x + maxWidth, clipping.height);
					}
					String currentString = eventResource.getRawCodeRecreation();
					if (currentString != null) {
						//currentString = StringEscapeUtils.unescapeHtml(currentString);
						currentString = HtmlEscape.unescapeHtml(currentString);
					}
					Helper helper = new Helper();
					helper.currentString = currentString;
					helper.x = x;
					while (helper != null && !helper.currentString.isEmpty()) {
						helper = parseString(event, helper, /*boldFont ,*/ oldFont, italicFont, foreground);
					}
					x = startX + maxWidth + 45;
					event.gc.setClipping(clipping.x, clipping.y, clipping.width, clipping.height);
					helper.currentString = eventResource.getHtmlDataSnapshot();
					helper.x = x;
					while (helper != null && !helper.currentString.isEmpty()) {
						helper = parseString(event, helper, /*boldFont,*/ oldFont, italicFont, foreground);
					}
				} finally {
					//if (boldFont != null) {
					//	boldFont.dispose();
					//}
					if (italicFont != null) {
						italicFont.dispose();
					}
					event.gc.setClipping(clipping.x, clipping.y, clipping.width, clipping.height);
				}

			}

		}
	}

	private int getMaxWidth(Event event, Font boldFont) {
		int maxWidth = Constants.MAX_WIDTH;
		EventResource[] input = (EventResource[]) viewer.getInput();
		if (input != null) {
			Font font = event.gc.getFont();
			event.gc.setFont(boldFont);
			for (EventResource eventResource:input) {
				String str = eventResource.getRawCodeRecreation();
				str = str.replace(Constants.SPAN_CLASS_CODE_STRING, "");
				str = str.replace(Constants.SPAN_CLASS_NORMAL_CODE, "");
				str = str.replace(Constants.SPAN_CLASS_TAINT, "");
				str = str.replace(Constants.SPAN_CLOSED, "");
				str = str.replace(Constants.ITALIC_OPENED, "");
				str = str.replace(Constants.ITALIC_CLOSED, "");
				Point extent = event.gc.stringExtent(str);
				maxWidth = Math.max(maxWidth, extent.x);
			}
			event.gc.setFont(font);
		}
		
		
		return maxWidth;
	}

	private Color getColor(String type) {
		if (type != null) {
			switch (type.toLowerCase()) {
			case "creation":
			case "trigger":
				return Constants.CREATION_COLOR;
			case "p2o":
			case "o2r":
				return Constants.P20_COLOR;
			case "tag":
				return Constants.TAG_COLOR;
			default:
				break;
			}
		}
		return null;
	}

	private Helper parseString(Event event, Helper helper, /*Font boldFont, */ Font oldFont, Font italicFont,
			Color foreground) {
		if (helper == null) {
			return helper;
		}
		String currentString = helper.currentString;
		int x = helper.x;
		if (currentString == null || currentString.isEmpty()) {
			return null;
		}
		if (currentString.startsWith(Constants.TAINT)) {
			currentString = currentString.substring(Constants.TAINT.length());
			int index = currentString.indexOf(Constants.TAINT_CLOSED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				event.gc.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				event.gc.setForeground(foreground);
				currentString = currentString.substring(str.length() + Constants.TAINT_CLOSED.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		}
		if (currentString.startsWith(Constants.SPAN_CLASS_NORMAL_CODE)) {
			currentString = currentString.substring(Constants.SPAN_CLASS_NORMAL_CODE.length());
			int index = currentString.indexOf(Constants.SPAN_CLOSED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				//event.gc.setFont(boldFont);
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				//event.gc.setFont(oldFont);
				currentString = currentString.substring(str.length() + Constants.SPAN_CLOSED.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		} else if (currentString.startsWith(Constants.SPAN_CLASS_CODE_STRING)) {
			currentString = currentString.substring(Constants.SPAN_CLASS_CODE_STRING.length());
			int index = currentString.indexOf(Constants.SPAN_CLOSED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				event.gc.setForeground(Constants.LINK_COLOR2);
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				event.gc.setForeground(foreground);
				currentString = currentString.substring(str.length() + Constants.SPAN_CLOSED.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		} else if (currentString.startsWith(Constants.SPAN_CLASS_TAINT)) {
			currentString = currentString.substring(Constants.SPAN_CLASS_TAINT.length());
			int index = currentString.indexOf(Constants.SPAN_CLOSED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				event.gc.setForeground(Constants.CREATION_COLOR);
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				event.gc.setForeground(foreground);
				currentString = currentString.substring(str.length() + Constants.SPAN_CLOSED.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		} else if (currentString.startsWith(Constants.ITALIC_OPENED)) {
			currentString = currentString.substring(Constants.ITALIC_OPENED.length());
			int index = currentString.indexOf(Constants.ITALIC_CLOSED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				event.gc.setFont(italicFont);
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				event.gc.setFont(oldFont);
				currentString = currentString.substring(str.length() + Constants.ITALIC_CLOSED.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		} else if (currentString.contains(Constants.SPAN_OPENED)) {
			int index = currentString.indexOf(Constants.SPAN_OPENED);
			if (index > -1) {
				String str = currentString.substring(0, index);
				//event.gc.setFont(boldFont);
				event.gc.drawString(str, x, event.y);
				x = x + event.gc.stringExtent(str).x;
				//event.gc.setFont(oldFont);
				currentString = currentString.substring(str.length());
			} else {
				//event.gc.setFont(boldFont);
				event.gc.drawString(currentString, x, event.y);
				//event.gc.setFont(oldFont);
				return null;
			}
		} else {
			//event.gc.setFont(boldFont);
			event.gc.drawString(currentString, x, event.y);
			//event.gc.setFont(oldFont);
			return null;
		}
		helper.currentString = currentString;
		helper.x = x;
		return helper;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void erase(Event event, Object element) {
		event.detail &= ~SWT.FOREGROUND;
	}

	private static class Helper {
		String currentString;
		int x;
	}

}