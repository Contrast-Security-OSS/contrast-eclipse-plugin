package com.contrastsecurity.ide.eclipse.core.integration;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.constants.SettingsConstants;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;

/**
 * 
 * @author Robert
 * All integration tests require the following "hard coded" information: username, API key, service key
 * and team server API URL. This is mainly because when test is run it does not take the current workspace
 * preferences, so you need to specify them.
 */
public class EventsTest {
	
	/**
	 * Team server username. Required to run any events test.
	 */
	private static String USERNAME;
	/**
	 * Team server organization API key. Required to run any events test.
	 */
	private static String API_KEY;
	/**
	 * Team server organization service key. Required to run any events test.
	 */
	private static String SERVICE_KEY;
	/**
	 * Team server API URL. Required to run any events test.
	 */
	private static String REST_API_URL;
	
	/**
	 * Organization UUID. Required to run when testing retrieval of an event.
	 */
	private static String ORGANIZATION_UUID;
	/**
	 * Trace (vulnerability) UUID. Required to run when testing retrieval of an event.
	 */
	private static String TRACE_ID;
	
	ExtendedContrastSDK sdk;
	
	@BeforeClass
	public static void initRequiredParams() throws Exception {
		USERNAME = System.getProperty("username");
		API_KEY = System.getProperty("apiKey");
		SERVICE_KEY = System.getProperty("serviceKey");
		REST_API_URL = System.getProperty("restApiUrl");
		ORGANIZATION_UUID = System.getProperty("organizationId");
		TRACE_ID = System.getProperty("traceId");
		
		System.out.print("TraceId: " + TRACE_ID);
	}
	
	@Before
	public void init() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		prefs.put(SettingsConstants.CURRENT_USERNAME, USERNAME);
		prefs.put(SettingsConstants.CURRENT_API_KEY, API_KEY);
		prefs.put(SettingsConstants.CURRENT_SERVICE_KEY, SERVICE_KEY);
		prefs.put(SettingsConstants.CURRENT_URL, REST_API_URL);
	}
	
	@Test
	public void retrieveVulnerabilityEventsDescriptionTest() throws UnauthorizedException, IOException {
		sdk = ContrastCoreActivator.getContrastSDK();
		assertNotNull(sdk);
		
		EventSummaryResource summary = sdk.getEventSummary(ORGANIZATION_UUID, TRACE_ID);
		assertTrue(!summary.getEvents().isEmpty());
		
		for(EventResource event : summary.getEvents()) {
			assertNotNull(event.getDescription());
			assertNotNull(event.getId());
			assertNotNull(event.getType());
			
			if(event.getCollapsedEvents() != null && !event.getCollapsedEvents().isEmpty()) {
				for(EventResource childEvent : event.getCollapsedEvents()) {
					assertNotNull(childEvent.getDescription());
					assertNotNull(childEvent.getId());
					assertNotNull(childEvent.getType());
					assertNotEquals(0, childEvent.getItems().length);
				}
			}
			else
				assertNotEquals(0, event.getItems().length);
		}
	}

}
