package com.contrastsecurity.ide.eclipse.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.extended.HttpRequestResource;
import com.contrastsecurity.ide.eclipse.core.extended.StoryResource;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;

public class SdkTest {
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

	ExtendedContrastSDK sdk;
	
	@BeforeClass
	public static void initRequiredParams() {
		USERNAME = System.getProperty("username");
		API_KEY = System.getProperty("apiKey");
		SERVICE_KEY = System.getProperty("serviceKey");
		REST_API_URL = System.getProperty("restApiUrl");
		ORGANIZATION_UUID = System.getProperty("organizationId");
	}

	@Before
	public void init() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		prefs.put(Constants.USERNAME, USERNAME);
		prefs.put(Constants.API_KEY, API_KEY);
		prefs.put(Constants.SERVICE_KEY, SERVICE_KEY);
		prefs.put(Constants.TEAM_SERVER_URL, REST_API_URL);
		sdk = ContrastCoreActivator.getContrastSDK();
	}

	private TraceFilterForm getServerTraceForm(Long selectedServerId) {
		TraceFilterForm form = new TraceFilterForm();
		List<Long> serverIds = new ArrayList<>();
		serverIds.add(selectedServerId);
		form.setServerIds(serverIds);
		return form;
	}

	@Test
	public void getAllTracesTest() throws UnauthorizedException, IOException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
			assertTrue(!traces.getTraces().isEmpty());
		}

	}

	@Test
	public void getTracesForEachApplicationTest() throws UnauthorizedException, IOException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);

		if (!applications.getApplications().isEmpty()) {
			for (Application application : applications.getApplications()) {
				Traces traces = sdk.getTraces(ORGANIZATION_UUID, application.getId(), null);
				for (Trace trace : traces.getTraces()) {
					assertTrue(trace.getTitle().length() > 0);
				}
			}
		}

	}

	@Test
	public void getTracesWithFilter() throws IOException, UnauthorizedException {
		Servers servers = sdk.getServers(ORGANIZATION_UUID, null);

		if (!servers.getServers().isEmpty()) {
			for (Server server : servers.getServers()) {
				TraceFilterForm form = getServerTraceForm(server.getServerId());
				Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, form);

				if (traces.getCount() > 0) {
					for (Trace trace : traces.getTraces()) {
						assertTrue(trace.getTitle().length() > 0);
					}
				}
			}
		}
	}

	@Test
	public void getStoryTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
			assertTrue(!traces.getTraces().isEmpty());
			StoryResource story = sdk.getStory(ORGANIZATION_UUID, traces.getTraces().get(0).getUuid());
			assertNotNull(story.getStory());
		}

	}

	@Test
	public void getEventSummaryTest() throws IOException, UnauthorizedException {
		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
			assertTrue(!traces.getTraces().isEmpty());

			for (Trace trace : traces.getTraces()) {
				EventSummaryResource eventSummary = sdk.getEventSummary(ORGANIZATION_UUID, trace.getUuid());
				if (!eventSummary.getEvents().isEmpty()) {
					for (EventResource event : eventSummary.getEvents()) {
						assertTrue(event.getDescription().length() > 0);
					}
				}
			}

		}
	}

	@Test
	public void getHttpRequestTest() throws IOException, UnauthorizedException {
		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
			assertTrue(!traces.getTraces().isEmpty());

			for (Trace trace : traces.getTraces()) {
				HttpRequestResource httpRequest = sdk.getHttpRequest(ORGANIZATION_UUID, trace.getUuid());
				if (httpRequest.getHttpRequest() != null) {
					assertTrue(httpRequest.getHttpRequest().getFormattedText().length() > 0);
				}
			}

		}
	}

}
