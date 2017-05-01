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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.unbescape.html.HtmlEscape;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.extended.Chapter;
import com.contrastsecurity.ide.eclipse.core.extended.PropertyResource;
import com.contrastsecurity.ide.eclipse.core.extended.Risk;
import com.contrastsecurity.ide.eclipse.core.extended.StoryResource;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;

public class OverviewTab extends AbstractTab {

	private StoryResource story;

	public OverviewTab(Composite parent, int style) {
		super(parent, style);
	}
	
	public void setStory(StoryResource story) {
		this.story = story;
		Composite control = getControl();
		Control[] children = control.getChildren();
		for (Control child : children) {
			child.dispose();
		}
		if (story != null && story.getStory() != null && story.getStory().getChapters() != null
				&& story.getStory().getChapters().size() > 0) {
			for (Chapter chapter : story.getStory().getChapters()) {
				String text = chapter.getIntroText() == null ? Constants.BLANK : chapter.getIntroText();
				String areaText = chapter.getBody() == null ? Constants.BLANK : chapter.getBody();
				if (areaText.isEmpty()) {
					List<PropertyResource> properties = chapter.getPropertyResources();
					if (properties != null && properties.size() > 0) {
						PropertyResource property = properties.get(0);
						areaText = property.getName() == null ? Constants.BLANK : property.getName();
					}
				}
				//new Label(control, SWT.NONE);
				Label label = new Label(control, SWT.WRAP | SWT.LEFT);
				GridData gd = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
				label.setLayoutData(gd);
				text = parseMustache(text);
				label.setText(text);
				//new Label(control, SWT.NONE);

				if (!areaText.isEmpty()) {
					final StyledText textArea = new StyledText(control, SWT.WRAP);
					final int padding = 5;
					textArea.setLeftMargin(padding);
					textArea.setRightMargin(padding);
					textArea.setTopMargin(padding);
					textArea.setBottomMargin(padding);
					textArea.setWordWrap(true);
					textArea.setCaret(null);
					gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
					textArea.setLayoutData(gd);
					textArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
					areaText = parseMustache(areaText);
					textArea.setText(areaText);
					//new Label(control, SWT.NONE);
				}
			}
			if (story.getStory().getRisk() != null) {
				Risk risk = story.getStory().getRisk();
				String riskText = risk.getText() == null ? Constants.BLANK : risk.getText();
				if (!riskText.isEmpty()) {
					Label label = new Label(control, SWT.WRAP | SWT.LEFT);
					GridData gd = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
					label.setLayoutData(gd);
					riskText = parseMustache(riskText);
					label.setText(riskText);
					//new Label(control, SWT.NONE);
				}
			}
		}
	}

	private String parseMustache(String text) {
		text = text.replace(Constants.MUSTACHE_NL, Constants.BLANK);
		//text = StringEscapeUtils.unescapeHtml(text);
		text = HtmlEscape.unescapeHtml(text);
		try {
			text = URLDecoder.decode(text, "UTF-8");
		} catch (Exception e) {
			// ignore
			if (ContrastUIActivator.getDefault().isDebugging()) {
				ContrastUIActivator.log(e);
			}
		}
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		// FIXME
		text = text.replace("{{#code}}", "");
		text = text.replace("{{/code}}", "");
		return text;
	}

	public StoryResource getStory() {
		return story;
	}

}
