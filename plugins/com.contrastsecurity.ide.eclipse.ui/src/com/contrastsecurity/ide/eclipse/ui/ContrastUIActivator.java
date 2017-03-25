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
package com.contrastsecurity.ide.eclipse.ui;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.models.Trace;

/**
 * The activator class controls the plug-in life cycle
 */
public class ContrastUIActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.contrastsecurity.ide.eclipse.ui"; //$NON-NLS-1$

	// The shared instance
	private static ContrastUIActivator plugin;
	
	/**
	 * The constructor
	 */
	public ContrastUIActivator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ContrastUIActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static Image getImage(String path) {
		ImageRegistry registry = getDefault().getImageRegistry();
		Image image = registry.get(path);
		if (image == null) {
			image = getImageDescriptor(path).createImage();
			registry.put(path, image);
		}
		return image;
	}
	
	public static void log(Throwable e) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e ));
	}
	
	public static void logInfo(String message) {
		if (plugin.isDebugging()) {
			plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
		}
	}
	
	public static void logWarning(String message) {
		plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
	}
	
	public static Image getSeverityImage(Trace element) {
		switch (element.getSeverity()) {
		case "Note":
			return ContrastUIActivator.getImage("/icons/note.png");
		case "High":
			return ContrastUIActivator.getImage("/icons/high.png");
		case "Medium":
			return ContrastUIActivator.getImage("/icons/medium.png");
		case "Low":
			return ContrastUIActivator.getImage("/icons/low.png");
		case "Critical":
			return ContrastUIActivator.getImage("/icons/critical.png");
		}
		return null;
	}
	
	public static String getOrgUuid() {
		String orgUuid = null;
		try {
			orgUuid = Util.getDefaultOrganizationUuid();
		} catch (IOException | UnauthorizedException e) {
			log(e);
		}
		return orgUuid;
	}
	
	public static String removeHtmlMarkup(String html) {
		html = html.replace("<span class='normal-code'>", "");
		html = html.replace("<span class='code-string'>", "");
		html = html.replace("<span class='taint'>", "");
		html = html.replace("<i>", "");
		html = html.replace("</i>", "");
		html = html.replaceAll("</span>", "");
		return html;
	}

}
