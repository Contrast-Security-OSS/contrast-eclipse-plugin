package com.contrastsecurity.ide.eclipse.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Before;
import org.junit.Test;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.FilterForm;
import com.contrastsecurity.http.ServerFilterForm;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.extended.EventDetails;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.extended.HttpRequestResource;
import com.contrastsecurity.ide.eclipse.core.extended.StoryResource;
import com.contrastsecurity.models.AgentType;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Coverage;
import com.contrastsecurity.models.Libraries;
import com.contrastsecurity.models.Library;
import com.contrastsecurity.models.Organization;
import com.contrastsecurity.models.Organizations;
import com.contrastsecurity.models.Rules;
import com.contrastsecurity.models.Rules.Rule;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.TraceFilter;
import com.contrastsecurity.models.TraceListing;
import com.contrastsecurity.models.Traces;

public class SdkTest {
	/**
	 * Team server username. Required to run any events test.
	 */
	private final static String USERNAME = "$user_name";
	/**
	 * Team server organization API key. Required to run any events test.
	 */
	private final static String API_KEY = "$api_key";
	/**
	 * Team server organization service key. Required to run any events test.
	 */
	private final static String SERVICE_KEY = "$service_key";
	/**
	 * Team server API URL. Required to run any events test.
	 */
	private final static String REST_API_URL = "https://$domain/Contrast/api";

	/**
	 * Organization UUID. Required to run when testing retrieval of an event.
	 */
	private final static String ORGANIZATION_UUID = "$org_UUID";

	ExtendedContrastSDK sdk;

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
	public void getTracesInOrg() throws IOException, UnauthorizedException {
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

	@Test
	public void getEventDetailsTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
			assertTrue(!traces.getTraces().isEmpty());

			for (Trace trace : traces.getTraces()) {
				EventSummaryResource eventSummary = sdk.getEventSummary(ORGANIZATION_UUID, trace.getUuid());
				if (!eventSummary.getEvents().isEmpty()) {
					for (EventResource event : eventSummary.getEvents()) {

						if (event.getCollapsedEvents() == null || event.getCollapsedEvents().isEmpty()) {
							EventDetails eventDetails = sdk.getEventDetails(ORGANIZATION_UUID, trace.getUuid(), event);
							assertTrue(!eventDetails.getMessages().isEmpty());
						}
					}
				}
			}
		}
	}

	@Test
	public void getProfileOrganizationsTest() throws IOException, UnauthorizedException {
		Organizations organizations = sdk.getProfileOrganizations();

		if (!organizations.getOrganizations().isEmpty()) {
			for (Organization organization : organizations.getOrganizations()) {
				assertTrue(organization.getName().length() > 0);
				assertTrue(organization.getDateFormat().length() > 0);
				assertTrue(organization.getName().length() > 0);
				assertTrue(organization.getOrgUuid().length() > 0);
				assertTrue(organization.getTimeFormat().length() > 0);
				assertTrue(organization.getTimeZone().length() > 0);
			}
		}
	}

	@Test
	public void getProfileDefaultOrganizationsTest() throws IOException, UnauthorizedException {

		Organizations organizations = sdk.getProfileDefaultOrganizations();
		Organization organization = organizations.getOrganization();
		assertTrue(organization.getName().length() > 0);
		assertTrue(organization.getDateFormat().length() > 0);
		assertTrue(organization.getName().length() > 0);
		assertTrue(organization.getOrgUuid().length() > 0);
		assertTrue(organization.getTimeFormat().length() > 0);
		assertTrue(organization.getTimeZone().length() > 0);

	}

	@Test
	public void getApplicationTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application application : applications.getApplications()) {
				Applications apps = sdk.getApplication(ORGANIZATION_UUID, application.getId());
				Application app = apps.getApplication();

				assertTrue(app.getName().length() > 0);
				assertTrue(app.getCodeShorthand().length() > 0);
				assertTrue(app.getId().length() > 0);
				assertTrue(app.getLanguage().length() > 0);
				assertTrue(app.getPath().length() > 0);
				assertTrue(app.getSizeShorthand().length() > 0);
				assertTrue(app.getStatus().length() > 0);
			}

		}
	}

	@Test
	public void getApplicationSecondTest() throws IOException, UnauthorizedException {

		EnumSet<FilterForm.ApplicationExpandValues> expandValues = EnumSet.of(FilterForm.ApplicationExpandValues.SCORES,
				FilterForm.ApplicationExpandValues.LICENSE, FilterForm.ApplicationExpandValues.TRACE_BREAKDOWN);

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);

		if (!applications.getApplications().isEmpty()) {
			for (Application application : applications.getApplications()) {

				Applications apps = sdk.getApplication(ORGANIZATION_UUID, application.getId(), expandValues);
				Application app = apps.getApplication();

				assertTrue(app.getName().length() > 0);
				assertTrue(app.getCodeShorthand().length() > 0);
				assertTrue(app.getId().length() > 0);
				assertTrue(app.getLanguage().length() > 0);
				assertTrue(app.getPath().length() > 0);
				assertTrue(app.getSizeShorthand().length() > 0);
				assertTrue(app.getStatus().length() > 0);
			}
		}
	}

	@Test
	public void getApplicationsTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				assertTrue(app.getName().length() > 0);
				assertTrue(app.getCodeShorthand().length() > 0);
				assertTrue(app.getId().length() > 0);
				assertTrue(app.getLanguage().length() > 0);
				assertTrue(app.getPath().length() > 0);
				assertTrue(app.getSizeShorthand().length() > 0);
				assertTrue(app.getStatus().length() > 0);
			}
		}

	}

	@Test
	public void getCoverageTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				Coverage coverage = sdk.getCoverage(ORGANIZATION_UUID, app.getId());
				assertNotNull(coverage);
			}
		}

	}

	@Test
	public void getLibrariesTest() throws IOException, UnauthorizedException {
		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				Libraries libraries = sdk.getLibraries(ORGANIZATION_UUID, app.getId());
				assertTrue(libraries.getAverageScoreLetter().length() > 0);
				for (Library library : libraries.getLibraries()) {
					assertTrue(library.getAppLanguage().length() > 0);
					assertTrue(library.getFilename().length() > 0);
					assertTrue(library.getFileVersion().length() > 0);
					assertTrue(library.getGrade().length() > 0);
					assertTrue(library.getLatestVersion().length() > 0);
					assertTrue(library.getVersion().length() > 0);
				}
			}
		}
	}

	@Test
	public void getLibrariesSecondTest() throws IOException, UnauthorizedException {

		EnumSet<FilterForm.LibrariesExpandValues> expandValues = EnumSet.of(FilterForm.LibrariesExpandValues.VULNS);

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				Libraries libraries = sdk.getLibraries(ORGANIZATION_UUID, app.getId(), expandValues);
				assertTrue(libraries.getAverageScoreLetter().length() > 0);
				for (Library library : libraries.getLibraries()) {
					assertTrue(library.getAppLanguage().length() > 0);
					assertTrue(library.getFilename().length() > 0);
					assertTrue(library.getFileVersion().length() > 0);
					assertTrue(library.getGrade().length() > 0);
					assertTrue(library.getLatestVersion().length() > 0);
					assertTrue(library.getVersion().length() > 0);
				}
			}
		}
	}

	@Test
	public void getServersTest() throws IOException, UnauthorizedException {

		Servers servers = sdk.getServers(ORGANIZATION_UUID, null);
		if (!servers.getServers().isEmpty()) {
			for (Server server : servers.getServers()) {
				assertTrue(server.getAgentVersion().length() > 0);
				assertTrue(server.getContainer().length() > 0);
				assertTrue(server.getEnvironment().length() > 0);
				assertTrue(server.getHostname().length() > 0);
				assertTrue(server.getLogLevel().length() > 0);
				assertTrue(server.getName().length() > 0);
				assertTrue(server.getPath().length() > 0);
				assertTrue(server.getStatus().length() > 0);
				assertTrue(server.getType().length() > 0);
			}
		}

	}

	@Test
	public void getServersWithFilterTest() throws IOException, UnauthorizedException {

		EnumSet<ServerFilterForm.ServerExpandValue> expandValues = EnumSet
				.of(ServerFilterForm.ServerExpandValue.APPLICATIONS);

		ServerFilterForm filterForm = new ServerFilterForm();
		filterForm.setExpand(expandValues);

		Servers servers = sdk.getServers(ORGANIZATION_UUID, filterForm);

		if (!servers.getServers().isEmpty()) {
			for (Server server : servers.getServers()) {
				assertTrue(server.getAgentVersion().length() > 0);
				assertTrue(server.getContainer().length() > 0);
				assertTrue(server.getEnvironment().length() > 0);
				assertTrue(server.getHostname().length() > 0);
				assertTrue(server.getLogLevel().length() > 0);
				assertTrue(server.getName().length() > 0);
				assertTrue(server.getPath().length() > 0);
				assertTrue(server.getStatus().length() > 0);
				assertTrue(server.getType().length() > 0);
				List<Application> applications = server.getApplications();
				if (!applications.isEmpty()) {
					for (Application app : applications) {
						assertTrue(app.getName().length() > 0);
						assertTrue(app.getId().length() > 0);
						assertTrue(app.getLanguage().length() > 0);
						assertTrue(app.getPath().length() > 0);
					}
				}

			}
		}

	}

	@Test
	public void getTracesTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				Traces traces = sdk.getTraces(ORGANIZATION_UUID, app.getId(), null);
				assertTrue(!traces.getTraces().isEmpty());
				for (Trace trace : traces.getTraces()) {
					assertTrue(trace.getCategory().length() > 0);
					assertTrue(trace.getConfidence().length() > 0);
					assertTrue(trace.getImpact().length() > 0);
					assertTrue(trace.getLanguage().length() > 0);
					assertTrue(trace.getLikelihood().length() > 0);
					assertTrue(trace.getPlatform().length() > 0);
					assertTrue(trace.getRule().length() > 0);
					assertTrue(trace.getSeverity().length() > 0);
					assertTrue(trace.getStatus().length() > 0);
					assertTrue(trace.getTitle().length() > 0);
					assertTrue(trace.getTitle().length() > 0);
					assertTrue(trace.getUuid().length() > 0);
				}
			}
		}
	}

	@Test
	public void getTracesInOrgTest() throws IOException, UnauthorizedException {
		Traces traces = sdk.getTracesInOrg(ORGANIZATION_UUID, null);
		if (!traces.getTraces().isEmpty()) {
			for (Trace trace : traces.getTraces()) {
				assertTrue(trace.getCategory().length() > 0);
				assertTrue(trace.getConfidence().length() > 0);
				assertTrue(trace.getImpact().length() > 0);
				assertTrue(trace.getLanguage().length() > 0);
				assertTrue(trace.getLikelihood().length() > 0);
				assertTrue(trace.getPlatform().length() > 0);
				assertTrue(trace.getRule().length() > 0);
				assertTrue(trace.getSeverity().length() > 0);
				assertTrue(trace.getStatus().length() > 0);
				assertTrue(trace.getTitle().length() > 0);
				assertTrue(trace.getTitle().length() > 0);
				assertTrue(trace.getUuid().length() > 0);
			}
		}
	}

	@Test
	public void getTraceFiltersTest() throws IOException, UnauthorizedException {

		Applications applications = sdk.getApplications(ORGANIZATION_UUID);
		if (!applications.getApplications().isEmpty()) {
			for (Application app : applications.getApplications()) {
				TraceListing traceListing = sdk.getTraceFilters(ORGANIZATION_UUID, app.getId());
				List<TraceFilter> traceFilters = traceListing.getFilters();
				for (TraceFilter traceFilter : traceFilters) {
					assertTrue(traceFilter.getKeycode().length() > 0);
					assertTrue(traceFilter.getLabel().length() > 0);
				}
			}
		}
	}

	// @Test
	// public void getTracesWithFilterTest() throws IOException,
	// UnauthorizedException {
	//
	// Applications applications = sdk.getApplications(ORGANIZATION_UUID);
	// if (!applications.getApplications().isEmpty()) {
	// for (Application app : applications.getApplications()) {
	//
	// Traces traces = sdk.getTracesWithFilter(ORGANIZATION_UUID, app.getId(),
	// TraceFilterType.MODULES,
	// TraceFilterKeycode.ALL_ISSUES, new TraceFilterForm());
	//
	// for (Trace trace : traces.getTraces()) {
	// System.out.println(trace.getCategory());
	// }
	// }
	// }
	// }

	@Test
	public void getRulesTest() throws IOException, UnauthorizedException {
		Rules rules = sdk.getRules(ORGANIZATION_UUID);
		if (!rules.getRules().isEmpty()) {
			for (Rule rule : rules.getRules()) {
				assertTrue(rule.getCategory().length() > 0);
				assertTrue(rule.getConfidence().length() > 0);
				assertTrue(rule.getCwe().length() > 0);
				assertTrue(rule.getDescription().length() > 0);
				assertTrue(rule.getImpact().length() > 0);
				assertTrue(rule.getLikelihood().length() > 0);
				assertTrue(rule.getName().length() > 0);
				assertTrue(rule.getOwasp().length() > 0);
				assertTrue(rule.getServiceLevel().length() > 0);
				assertTrue(rule.getSeverity().length() > 0);
				assertTrue(rule.getTitle().length() > 0);
			}
		}
	}

	@Test
	public void getAgentTest() throws IOException, UnauthorizedException {

		byte[] agent = sdk.getAgent(AgentType.JAVA, ORGANIZATION_UUID);
		assertNotNull(agent);

	}
}
