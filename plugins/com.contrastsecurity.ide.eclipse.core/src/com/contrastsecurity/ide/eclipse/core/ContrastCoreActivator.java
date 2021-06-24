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

import java.util.jar.Manifest;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.contrastsecurity.ide.eclipse.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.http.IntegrationName;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.sdk.ContrastSDK;

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

	public static boolean saveNewOrganization(final String organizationName, final String contrastUrl, final String username,
			final String serviceKey, final String apiKey, final String organizationUuid) {
		initPrefs();

		String[] list = getOrganizationList();
		list = (String[]) ArrayUtils.add(list, organizationName);
		saveOrganizationList(list, false);

		prefs.put(organizationName, contrastUrl + ";" + username + ";" + serviceKey + ";" + apiKey + ";" + organizationUuid);

		return flushPrefs();
	}

	public static OrganizationConfig getOrganizationConfiguration(final String organization) {
		initPrefs();

		String config = prefs.get(organization, "");

		if(StringUtils.isBlank(config))
			return null;

		String[] configArray = Util.getListFromString(config);

		// Compatibility fix for the previous plugin version
		if (configArray.length == 2) {
			removeOrganization(ArrayUtils.indexOf(getOrganizationList(), organization));

			saveNewOrganization(organization, prefs.get(Constants.TEAM_SERVER_URL, ""),
					prefs.get(Constants.USERNAME, ""),
					prefs.get(Constants.SERVICE_KEY, ""), configArray[0], configArray[1]);
			config = prefs.get(organization, "");
			configArray = Util.getListFromString(config);
		}

		return new OrganizationConfig(configArray[0], configArray[1], configArray[2], configArray[3], configArray[4]);
	}

	public static String getSelectedOrganization() {
		initPrefs();

		return prefs.get(Constants.ORGNAME, "");
	}

	public static String getSelectedOrganizationUuid() {
		if (getOrganizationConfiguration(getSelectedOrganization()) != null) {
			return getOrganizationConfiguration(getSelectedOrganization()).getOrganizationUUIDKey();
		} else {
			return null;
		}

	}

	public static boolean editOrganization(final String organization, final String apiKey, final String organizationUuid) throws OrganizationNotFoundException {
		initPrefs();

		if(prefs.get(organization, null) == null)
			throw new OrganizationNotFoundException("Organization does not exists");

		prefs.put(organization, apiKey + ";" + organizationUuid);

		return flushPrefs();
	}

	public static boolean saveSelectedPreferences(final String orgName) {
		initPrefs();

		prefs.put(Constants.ORGNAME, orgName);

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

	public static ContrastSDK getContrastSDK() {

		initPrefs();

		String organizationName = prefs.get(Constants.ORGNAME, "");

		if (organizationName == null || organizationName.isEmpty()) {
			return null;
		}
		return getContrastSDKByOrganization(organizationName);
	}

	public static ContrastSDK getContrastSDKByOrganization(final String organizationName) {

		if(StringUtils.isBlank(organizationName))
			return null;

		OrganizationConfig config = getOrganizationConfiguration(organizationName);
		if(config == null)
			return null;

		String url = config.getContrastUrl();
		if (url == null || url.isEmpty()) {
			return null;
		}
		String username = config.getUsername();
		if (username == null || username.isEmpty()) {
			return null;
		}
		String serviceKey = config.getServiceKey();
		if (serviceKey == null || serviceKey.isEmpty()) {
			return null;
		}
		String apiKey = config.getApiKey();
		if (apiKey == null || apiKey.isEmpty()) {
			return null;
		}

		return getContrastSDK(username, apiKey, serviceKey, url);
	}

	public static ContrastSDK getContrastSDK(final String username, final String apiKey,
			final String serviceKey, final String teamServerUrl) {

		Manifest manifest = new Manifest();
		ContrastSDK sdk = new ContrastSDK.Builder(username, serviceKey, apiKey).withApiUrl(teamServerUrl).withIntegrationName(IntegrationName.ECLIPSE_IDE).withVersion(manifest.getAttributes("Bundle-Version").toString()).build();

		sdk.setReadTimeout(5000);

		return sdk;
	}

}
