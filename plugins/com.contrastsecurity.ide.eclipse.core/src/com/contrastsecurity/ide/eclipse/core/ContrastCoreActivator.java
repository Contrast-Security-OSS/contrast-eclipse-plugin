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
package com.contrastsecurity.ide.eclipse.core;

import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.contrastsecurity.ide.eclipse.core.constants.SettingsConstants;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.ConnectionConfig;
import com.contrastsecurity.ide.eclipse.core.util.ConnectionConfigUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class ContrastCoreActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.contrastsecurity.ide.eclipse.core"; //$NON-NLS-1$

	// The shared instance
	private static ContrastCoreActivator plugin;
	
	private static IEclipsePreferences prefs;

	/**
	 * The constructor
	 */
	public ContrastCoreActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
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
	public static ContrastCoreActivator getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		plugin.getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
	}

	public static void logInfo(String message) {
		if (plugin.isDebugging()) {
			plugin.getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
		}
	}

	public static void logWarning(String message) {
		plugin.getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
	}

	public static IEclipsePreferences getPreferences() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
	}
	
	public static void initPrefs() {
		if(prefs == null)
			prefs = getPreferences();
	}
	
	/*==============  Configuration functions ========================*/
	public static Map<String, ConnectionConfig> getConfigurations() {
		initPrefs();
		
		String configs = prefs.get(SettingsConstants.CONNECTION_CONFIGURATION, "");
		return ConnectionConfigUtil.convertToMap(configs);
	}
	
	public static boolean saveConfigurations(Map<String, ConnectionConfig> configs) {
		initPrefs();
		
		String configString = ConnectionConfigUtil.convertFromMap(configs);
		prefs.put(SettingsConstants.CONNECTION_CONFIGURATION, configString);
		
		return flushPrefs();
	}
	
	public static boolean setSelectedConfiguration(String selection, ConnectionConfig config) {
		initPrefs();
		
		prefs.put(SettingsConstants.SELECTED_CONFIGURATION, selection);
		prefs.put(SettingsConstants.CURRENT_USERNAME, config.getUsername());
		prefs.put(SettingsConstants.CURRENT_URL, config.getTsUrl());
		prefs.put(SettingsConstants.CURRENT_SERVICE_KEY, config.getServiceKey());
		prefs.put(SettingsConstants.CURRENT_API_KEY, config.getApiKey());
		prefs.put(SettingsConstants.CURRENT_ORG_ID, config.getOrgId());
		prefs.put(SettingsConstants.CURRENT_ORG_NAME, config.getOrgName());
		
		return flushPrefs();
	}
	
	public static ConnectionConfig getSelectedConfig() {
		initPrefs();
		
		ConnectionConfig config = new ConnectionConfig();
		
		config.setTsUrl(prefs.get(SettingsConstants.CURRENT_URL, ""));
		config.setUsername(prefs.get(SettingsConstants.CURRENT_USERNAME, ""));
		config.setServiceKey(prefs.get(SettingsConstants.CURRENT_SERVICE_KEY, ""));
		config.setApiKey(prefs.get(SettingsConstants.CURRENT_API_KEY, ""));
		config.setOrgId(prefs.get(SettingsConstants.CURRENT_ORG_ID, ""));
		config.setOrgName(prefs.get(SettingsConstants.CURRENT_ORG_NAME, ""));
		
		return config;
	}
	
	public static String getSelectedConfigKey() {
		initPrefs();
		
		return prefs.get(SettingsConstants.SELECTED_CONFIGURATION, "");
	}
	
	/*==============  Preferences functions  ========================*/
	
	public static String getTeamServerUrl() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_URL, "");
	}
	
	public static String getSelectedApiKey() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_API_KEY, "");
	}
	
	public static String getServiceKey() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_SERVICE_KEY, "");
	}
	
	public static String getUsername() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_USERNAME, "");
	}
	
	public static String getSelectedOrganization() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_ORG_NAME, "");
	}
	
	public static String getSelectedOrganizationUuid() {
		initPrefs();
		
		return prefs.get(SettingsConstants.CURRENT_ORG_ID, "");
	}
	
	public static boolean flushPrefs() {
		if(prefs == null)
			return false;
		
			try {
				prefs.flush();
				return true;
			}
			catch(BackingStoreException e) {
				e.printStackTrace();
				return false;
			}
	}

	public static ExtendedContrastSDK getContrastSDK() {
		ConnectionConfig config = getSelectedConfig();
		if(config.getUsername() == null || config.getUsername().isEmpty())
			return null;
		if(config.getServiceKey() == null || config.getServiceKey().isEmpty())
			return null;
		if(config.getApiKey() == null || config.getApiKey().isEmpty())
			return null;
		if(config.getTsUrl() == null || config.getTsUrl().isEmpty())
			return null;
		
		return getContrastSDK(config.getUsername(), config.getApiKey(), config.getServiceKey(), config.getTsUrl());
	}
	
	public static ExtendedContrastSDK getContrastSDK(final String username, final String apiKey, 
			final String serviceKey, final String teamServerUrl) {
		initPrefs();
		
		ExtendedContrastSDK sdk = new ExtendedContrastSDK(username, serviceKey, apiKey, teamServerUrl);
		sdk.setReadTimeout(5000);
		
		return sdk;
	}

}
