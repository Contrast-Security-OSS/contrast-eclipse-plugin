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

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.models.CustomRecommendation;
import com.contrastsecurity.models.CustomRuleReferences;
import com.contrastsecurity.models.RuleReferences;
import com.contrastsecurity.models.RecommendationResponse;

public class RecommendationTab extends AbstractTab {

	private RecommendationResponse recommendationResource;

	public RecommendationTab(Composite parent, int style) {
		super(parent, style);
	}

	public RecommendationResponse getRecommendationResource() {
		return recommendationResource;
	}

	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

	public void setRecommendationResource(RecommendationResponse recommendationResource) {
		this.recommendationResource = recommendationResource;
		Composite control = getControl();
		Control[] children = control.getChildren();
		for (Control child : children) {
			child.dispose();
		}

		if (recommendationResource != null && recommendationResource.getRecommendation() != null
				&& recommendationResource.getCustomRecommendation() != null
				&& recommendationResource.getRuleReferences() != null
				&& recommendationResource.getCustomRuleReferences() != null) {

			String formattedRecommendationText = recommendationResource.getRecommendation().getFormattedText();
			String openTag = null;
			String closeTag = null;

			if (formattedRecommendationText.contains(Constants.OPEN_TAG_C_SHARP_BLOCK)) {
				openTag = Constants.OPEN_TAG_C_SHARP_BLOCK;
				closeTag = Constants.CLOSE_TAG_C_SHARP_BLOCK;
			} else if (formattedRecommendationText.contains(Constants.OPEN_TAG_HTML_BLOCK)) {
				openTag = Constants.OPEN_TAG_HTML_BLOCK;
				closeTag = Constants.CLOSE_TAG_HTML_BLOCK;
			} else if (formattedRecommendationText.contains(Constants.OPEN_TAG_JAVA_BLOCK)) {
				openTag = Constants.OPEN_TAG_JAVA_BLOCK;
				closeTag = Constants.CLOSE_TAG_JAVA_BLOCK;
			} else if (formattedRecommendationText.contains(Constants.OPEN_TAG_XML_BLOCK)) {
				openTag = Constants.OPEN_TAG_XML_BLOCK;
				closeTag = Constants.CLOSE_TAG_XML_BLOCK;
			} else if (formattedRecommendationText.contains(Constants.OPEN_TAG_JAVASCRIPT_BLOCK)) {
				openTag = Constants.OPEN_TAG_JAVASCRIPT_BLOCK;
				closeTag = Constants.CLOSE_TAG_JAVASCRIPT_BLOCK;
			}

			String[] codeBlocks = StringUtils.substringsBetween(formattedRecommendationText, openTag, closeTag);
			String[] textBlocks = StringUtils.substringsBetween(formattedRecommendationText, closeTag, openTag);

			String textBlockFirst = StringUtils.substringBefore(formattedRecommendationText, openTag);
			String textBlockLast = StringUtils.substringAfterLast(formattedRecommendationText, closeTag);

			insertTextBlock(control, textBlockFirst);

			if (codeBlocks != null && codeBlocks.length > 0) {
				for (int i = 0; i < codeBlocks.length; i++) {

					String textToInsert = StringEscapeUtils.unescapeHtml(codeBlocks[i]);
					createStyledTextCodeBlock(control, textToInsert);

					if (textBlocks != null && textBlocks.length > 0 && i < codeBlocks.length - 1) {
						insertTextBlock(control, textBlocks[i]);
					}
				}
			}

			insertTextBlock(control, textBlockLast);

			CustomRecommendation customRecommendation = recommendationResource.getCustomRecommendation();
			String customRecommendationText = customRecommendation.getText() == null ? Constants.BLANK
					: customRecommendation.getText();
			if (!customRecommendationText.isEmpty()) {
				customRecommendationText = parseMustache(customRecommendationText);

				createLabel(control, customRecommendationText);
			}

			Composite cweComposite = new Composite(control, SWT.NONE);
			cweComposite.setLayout(new RowLayout());

			Label cweHeaderLabel = createLabel(cweComposite, resource.getString("CWE_LABEL"));
			cweHeaderLabel.setLayoutData(new RowData(100, 15));
			Link cweLink = createLinkFromUrlString(cweComposite, recommendationResource.getCwe());
			cweLink.setLayoutData(new RowData());

			Composite owaspComposite = new Composite(control, SWT.NONE);
			owaspComposite.setLayout(new RowLayout());

			Label owaspHeaderLabel = createLabel(owaspComposite, resource.getString("OWASP_LABEL"));
			owaspHeaderLabel.setLayoutData(new RowData(100, 15));
			Link owaspLink = createLinkFromUrlString(owaspComposite, recommendationResource.getOwasp());
			owaspLink.setLayoutData(new RowData());

			RuleReferences ruleReferences = recommendationResource.getRuleReferences();
			String ruleReferencesText = ruleReferences.getText() == null ? Constants.BLANK : ruleReferences.getText();
			if (!ruleReferencesText.isEmpty()) {

				Composite referencesComposite = new Composite(control, SWT.NONE);
				referencesComposite.setLayout(new RowLayout());

				Label referencesHeaderLabel = createLabel(referencesComposite, resource.getString("REFERENCES_LABEL"));
				referencesHeaderLabel.setLayoutData(new RowData(100, 15));

				String firstLink = StringUtils.substringBefore(ruleReferencesText, Constants.MUSTACHE_NL);
				Link referencesLink = createLinkFromUrlString(referencesComposite, firstLink);
				referencesLink.setLayoutData(new RowData());

				String[] links = StringUtils.substringsBetween(ruleReferencesText, Constants.MUSTACHE_NL,
						Constants.MUSTACHE_NL);

				if (links != null && links.length > 0) {
					for (String link : links) {
						Link linkObject = createLinkFromUrlString(referencesComposite, link);
						linkObject.setLayoutData(new RowData());
					}
				}
			}
			CustomRuleReferences customRuleReferences = recommendationResource.getCustomRuleReferences();
			if (StringUtils.isNotEmpty(customRuleReferences.getText())) {
				formatLinks(control, customRuleReferences.getFormattedText());
			}
		}

		ScrolledComposite sc = (ScrolledComposite) control.getParent();

		Rectangle r = sc.getClientArea();
		Control content = sc.getContent();
		if (content != null && r != null) {
			Point minSize = content.computeSize(r.width, SWT.DEFAULT);
			sc.setMinSize(minSize);
			ScrollBar vBar = sc.getVerticalBar();
			vBar.setPageIncrement(r.height);
		}

	}

	private Link createLinkFromUrlString(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		text = "<a>" + text + "</a>";
		link.setText(text);
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					openLinkInBrowser(event.text);
				} catch (PartInitException | MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		return link;
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setText(text);
		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				try {
					openLinkInBrowser(event.text);
				} catch (PartInitException | MalformedURLException e) {
					e.printStackTrace();
				}
			}
		});
		return link;
	}

	public void openLinkInBrowser(String urlString) throws MalformedURLException, PartInitException {
		if (!urlString.isEmpty()) {
			URL url = new URL(urlString);
			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
		}
	}

	private Label createLabel(Composite composite, String text) {
		Label label = new Label(composite, SWT.WRAP | SWT.LEFT);
		GridData gd = new GridData(SWT.HORIZONTAL, SWT.TOP, true, false, 1, 1);
		label.setLayoutData(gd);
		label.setText(text);
		return label;
	}

	private StyledText createStyledTextCodeBlock(Composite composite, String text) {
		final StyledText textArea = new StyledText(composite, SWT.WRAP);
		final int padding = 5;
		textArea.setLeftMargin(padding);
		textArea.setRightMargin(padding);
		textArea.setTopMargin(padding);
		textArea.setBottomMargin(padding);
		textArea.setWordWrap(true);
		textArea.setCaret(null);
		textArea.setEditable(false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		textArea.setLayoutData(gd);
		textArea.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
		textArea.setText(text);
		return textArea;
	}

	private void insertTextBlock(Composite composite, String text) {

		if (text != null && !text.isEmpty()) {
			String[] links = StringUtils.substringsBetween(text, Constants.OPEN_TAG_LINK, Constants.CLOSE_TAG_LINK);

			if (links != null && links.length > 0) {

				String[] textBlocks = StringUtils.substringsBetween(text, Constants.CLOSE_TAG_LINK,
						Constants.OPEN_TAG_LINK);

				String textBlockFirst = StringUtils.substringBefore(text, Constants.OPEN_TAG_LINK);
				String textBlockLast = StringUtils.substringAfterLast(text, Constants.CLOSE_TAG_LINK);

				createStyledTextBlock(composite, parseMustache(textBlockFirst));

				for (int i = 0; i < links.length; i++) {

					int indexOfDelimiter = links[i].indexOf(Constants.LINK_DELIM);
					String formattedLink = "<a href=\"" + links[i].substring(0, indexOfDelimiter) + "\">"
							+ links[i].substring(indexOfDelimiter + Constants.LINK_DELIM.length()) + "</a>";
					createLink(composite, formattedLink);

					if (textBlocks != null && textBlocks.length > 0 && i < links.length - 1) {
						createStyledTextBlock(composite, parseMustache(textBlocks[i]));
					}
				}
				createStyledTextBlock(composite, parseMustache(textBlockLast));
			} else {
				createStyledTextBlock(composite, parseMustache(text));
			}
		}
	}

	private StyledText createStyledTextBlock(Composite composite, String text) {

		if (text != null && !text.isEmpty()) {
			int paramStart = 0;
			int paramLength = 0;
			Color color = null;

			if (text.contains(Constants.OPEN_TAG_GOOD_PARAM)) {

				int indexOfGoodParamOpenTag = text.indexOf(Constants.OPEN_TAG_GOOD_PARAM);
				int indexOfGoodParamCloseTag = text.indexOf(Constants.CLOSE_TAG_GOOD_PARAM);
				paramStart = indexOfGoodParamOpenTag;
				paramLength = indexOfGoodParamCloseTag
						- (indexOfGoodParamOpenTag + Constants.OPEN_TAG_GOOD_PARAM.length());

				color = Constants.GOOD_PARAM_COLOR;
				text = text.replace(Constants.OPEN_TAG_GOOD_PARAM, "");
				text = text.replace(Constants.CLOSE_TAG_GOOD_PARAM, "");

			} else if (text.contains(Constants.OPEN_TAG_BAD_PARAM)) {

				int indexOfBadParamOpenTag = text.indexOf(Constants.OPEN_TAG_BAD_PARAM);
				int indexOfBadParamCloseTag = text.indexOf(Constants.CLOSE_TAG_BAD_PARAM);
				paramStart = indexOfBadParamOpenTag;
				paramLength = indexOfBadParamCloseTag
						- (indexOfBadParamOpenTag + Constants.OPEN_TAG_BAD_PARAM.length());

				color = Constants.CREATION_COLOR;
				text = text.replace(Constants.OPEN_TAG_BAD_PARAM, "");
				text = text.replace(Constants.CLOSE_TAG_BAD_PARAM, "");
			}

			StyledText styledText = new StyledText(composite, SWT.WRAP);
			styledText.setText(text);
			styledText.setCaret(null);
			styledText.setBackground(composite.getBackground());
			styledText.setEditable(false);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
			styledText.setLayoutData(gd);

			if (paramStart != 0 && paramLength != 0 && color != null) {
				StyleRange styleRange = new StyleRange();
				styleRange.start = paramStart;
				styleRange.length = paramLength;
				styleRange.foreground = color;
				styledText.setStyleRange(styleRange);
			}

			return styledText;
		} else
			return null;
	}

	private void formatLinks(Composite composite, String text) {
		String linkStartKey = "https://";
		String linkEndKey = "{{{nl}}}";
		
		String labelText = text.replaceAll("<br>", "\n").replaceAll("<b>", "\n").replaceAll("</b>", "").replaceAll("</br>", "");
		
		while (labelText.contains(linkStartKey)){
			int linkStart = labelText.indexOf(linkStartKey);
            int linkEnd = labelText.indexOf(linkEndKey);

			if (linkStart >= 0 && linkEnd >= 0){
				Label label = new Label(composite, SWT.WRAP | SWT.LEFT);
				label.setText(labelText.substring(0, linkStart));	
			
				Link link = createLinkFromUrlString(composite, labelText.substring(linkStart, linkEnd));
		
				labelText = labelText.subSequence(linkEnd+linkEndKey.length(), labelText.length()).toString();
			}
			else{
				Label label = new Label(composite, SWT.WRAP | SWT.LEFT);

				label.setText(parseMustache(text));
				labelText = "";
			}
		}
	 
	 
	 }

	private String parseMustache(String text) {
		if (text != null) {
			try {
				text = URLDecoder.decode(text, "UTF-8");
			} catch (Exception ignored) {
			}
			text = StringEscapeUtils.unescapeHtml(text);
			for (String mustache : Constants.MUSTACHE_CONSTANTS) {
				text = text.replace(mustache, Constants.BLANK);
			}

			return text;
		} else {
			return null;
		}

	}
}
