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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.OrganizationConfig;

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
	
	public static String[] getOrganizationList() {
		initPrefs();
		String orgListString = prefs.get(Constants.ORGANIZATION_LIST, "");
		
		return Util.getListFromString(orgListString);
	}
	
	public static String getDefaultOrganization() {
		initPrefs();
		
		return prefs.get(Constants.ORGNAME, null);
	}
	
	public static boolean saveOrganizationList(String[] list) {
		return saveOrganizationList(list, true);
	}
	
	public static boolean saveOrganizationList(String[] list, boolean shouldFlush) {
		initPrefs();
		
		String stringList = Util.getStringFromList(list);
		
		prefs.put(Constants.ORGANIZATION_LIST, stringList);
		
		if(shouldFlush)
			return flushPrefs();
		
		return true;
	}
	
	public static void removeOrganization(final int position) {
		String[] orgArray = getOrganizationList();
		String organization = orgArray[position];
		orgArray = (String[]) ArrayUtils.remove(orgArray, position);
		saveOrganizationList(orgArray, false);
		
		prefs.remove(organization);
		
		flushPrefs();
	}
	
	public static boolean saveNewOrganization(final String organization, final String apiKey, final String organizationUuid) {
		initPrefs();
		
		String[] list = getOrganizationList();
		list = (String[]) ArrayUtils.add(list, organization);
		saveOrganizationList(list, false);
		
		prefs.put(organization, apiKey + ";" + organizationUuid);
		
		return flushPrefs();
	}
	
	public static OrganizationConfig getOrganizationConfiguration(final String organization) {
		initPrefs();
		
		String config = prefs.get(organization, "");
		
		if(StringUtils.isBlank(config))
			return null;
		
		String[] configArray = Util.getListFromString(config);
		
		return new OrganizationConfig(configArray[0], configArray[1]);
	}
	
	public static String getTeamServerUrl() {
		initPrefs();
		
		return prefs.get(Constants.TEAM_SERVER_URL, "");
	}
	
	public static String getSelectedApiKey() {
		initPrefs();
		
		return prefs.get(Constants.API_KEY, "");
	}
	
	public static String getServiceKey() {
		initPrefs();
		
		return prefs.get(Constants.SERVICE_KEY, "");
	}
	
	public static String getUsername() {
		initPrefs();
		
		return prefs.get(Constants.USERNAME, "");
	}
	
	public static String getSelectedOrganization() {
		initPrefs();
		
		return prefs.get(Constants.ORGNAME, "");
	}
	
	public static String getSelectedOrganizationUuid() {
		initPrefs();
		
		return prefs.get(Constants.ORGUUID, "");
	}
	
	public static boolean editOrganization(final String organization, final String apiKey, final String organizationUuid) throws OrganizationNotFoundException {
		initPrefs();
		
		if(prefs.get(organization, null) == null)
			throw new OrganizationNotFoundException("Organization does not exists");
		
		prefs.put(organization, apiKey + ";" + organizationUuid);
		
		return flushPrefs();
	}
	
	public static boolean saveSelectedPreferences(final String teamServerUrl, final String serviceKey, final String apiKey, 
			final String username, final String orgName, final String orgUuid) {
		initPrefs();
		
		prefs.put(Constants.TEAM_SERVER_URL, teamServerUrl);
		prefs.put(Constants.SERVICE_KEY, serviceKey);
		prefs.put(Constants.API_KEY, apiKey);
		prefs.put(Constants.USERNAME, username);
		prefs.put(Constants.ORGNAME, orgName);
		prefs.put(Constants.ORGUUID, orgUuid);
		
		return flushPrefs();
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
		IEclipsePreferences prefs = getPreferences();
		String username = prefs.get(Constants.USERNAME, null);
		if (username == null || username.isEmpty()) {
			return null;
		}
		String serviceKey = prefs.get(Constants.SERVICE_KEY, null);
		if (serviceKey == null || serviceKey.isEmpty()) {
			return null;
		}
		String apiKey = prefs.get(Constants.API_KEY, null);
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}
		String url = prefs.get(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
		if (url == null || url.isEmpty()) {
			return null;
		}
		return getContrastSDK(username, apiKey, serviceKey, url);
	}
	
	public static ExtendedContrastSDK getContrastSDKByOrganization(final String organizationName) {
		IEclipsePreferences prefs = getPreferences();
		String username = prefs.get(Constants.USERNAME, null);
		if (username == null || username.isEmpty()) {
			return null;
		}
		
		if(StringUtils.isBlank(organizationName))
			return null;
		
		OrganizationConfig config = getOrganizationConfiguration(organizationName);
		if(config == null)
			return null;
		
		String serviceKey = prefs.get(Constants.SERVICE_KEY, null);
		if (serviceKey == null || serviceKey.isEmpty()) {
			return null;
		}
		String apiKey = config.getApiKey();
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}
		String url = prefs.get(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
		if (url == null || url.isEmpty()) {
			return null;
		}
		return getContrastSDK(username, apiKey, serviceKey, url);
	}
	
	public static ExtendedContrastSDK getContrastSDK(final String apiKey) {
		IEclipsePreferences prefs = getPreferences();
		String username = prefs.get(Constants.USERNAME, null);
		if (username == null || username.isEmpty()) {
			return null;
		}
		String serviceKey = prefs.get(Constants.SERVICE_KEY, null);
		if (serviceKey == null || serviceKey.isEmpty()) {
			return null;
		}
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}
		String url = prefs.get(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
		if (url == null || url.isEmpty()) {
			return null;
		}
		return getContrastSDK(username, apiKey, serviceKey, url);
	}
	
	public static ExtendedContrastSDK getContrastSDK(final String username, final String apiKey, 
			final String serviceKey, final String teamServerUrl) {
		initPrefs();
		
		ExtendedContrastSDK sdk = new ExtendedContrastSDK(username, serviceKey, apiKey, teamServerUrl);
		sdk.setReadTimeout(5000);
		
		return sdk;
	}

}
