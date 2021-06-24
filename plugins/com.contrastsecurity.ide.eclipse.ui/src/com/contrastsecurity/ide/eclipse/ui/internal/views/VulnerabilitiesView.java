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
package com.contrastsecurity.ide.eclipse.ui.internal.views;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.core.internal.preferences.OrganizationConfig;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.cache.ContrastCache;
import com.contrastsecurity.ide.eclipse.ui.cache.Key;
import com.contrastsecurity.ide.eclipse.ui.internal.job.RefreshJob;
import com.contrastsecurity.ide.eclipse.ui.internal.model.AbstractPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ConfigurationPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.IFilterListener;
import com.contrastsecurity.ide.eclipse.ui.internal.model.IPageLoaderListener;
import com.contrastsecurity.ide.eclipse.ui.internal.model.LoadingPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.MainPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.SeverityFilterListener;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityDetailsPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityDetailsTab;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityPage;
import com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.EventSummaryResponse;
import com.contrastsecurity.models.HttpRequestResponse;
import com.contrastsecurity.models.RecommendationResponse;
import com.contrastsecurity.models.Servers;
import com.contrastsecurity.models.StoryResponse;
import com.contrastsecurity.models.TagsResponse;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;
import com.contrastsecurity.sdk.ContrastSDK;

/**
 * Vulnerabilities View
 */
public class VulnerabilitiesView extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.views.VulnerabilitiesView";

	/**
	 * No action should be performed
	 */
	private final static int NO_ACTION = -1;
	/**
	 * The mouse event should trigger to show vulnerability details view on overview
	 * tab.
	 */
	private final static int VIEW_VULNERABILITY_OVERVIEW_ACTION = 0;
	/**
	 * The mouse event should trigger to show vulnerability details view on Events
	 * tab.
	 */
	private final static int VIEW_VULNERABILITY_EVENTS_ACTION = 1;
	/**
	 * The mouse event should trigger to take the user to vulnerability on browser.
	 *
	 * @warning Might not work if the default organization its different from
	 *          current one on eclipse plugin.
	 */
	private final static int SHOW_VULNERABILITY_IN_BROWSER_ACTION = 2;

	private TableViewer table;
	private Action refreshAction;
	private Action openPreferencesPage;
	private Action doubleClickAction;
	private Label statusLabel;
	private ContrastSDK sdk = ContrastCoreActivator.getContrastSDK();
	private ContrastCache contrastCache = ContrastUIActivator.getContrastCache();
	private VulnerabilityPage mainPage;
	private VulnerabilityPage noVulnerabilitiesPage;
	private VulnerabilityPage currentPage;
	private AbstractPage activePage;
	private PageBook book;
	private VulnerabilityDetailsPage detailsPage;
	private AbstractPage loadingPage;
	private AbstractPage configurationPage;
	private RefreshJob refreshJob;

	private int currentOffset = 0;
	private static final int PAGE_LIMIT = 20;
	private int total = 0;

	IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();

	TraceFilterForm currentTraceFilterForm;

	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

	private String traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;

	private IPageLoaderListener pageLoaderListener = new IPageLoaderListener() {

		@Override
		public void onPageLoad(int page) {
			currentOffset = PAGE_LIMIT * (page - 1);
			prefs.putInt(Constants.CURRENT_OFFSET, currentOffset);
			currentTraceFilterForm.setOffset(currentOffset);
			refreshTraces(false);
		}
	};

	private IFilterListener openFilterDialogButtonListener = new IFilterListener() {

		@Override
		public void onFilterLoad(Servers retrievedServers, Applications retrievedApplications) {
			if (retrievedServers != null && retrievedApplications != null) {
				final FilterDialog filterDialog = new FilterDialog(currentPage.getShell(), retrievedServers,
						retrievedApplications);
				filterDialog.create();
				filterDialog.open();

				TraceFilterForm dialogTraceFilterForm = filterDialog.getTraceFilterForm();

				if (dialogTraceFilterForm != null) {
					dialogTraceFilterForm.setSort(currentTraceFilterForm.getSort());
					dialogTraceFilterForm.setSeverities(currentTraceFilterForm.getSeverities());
					currentTraceFilterForm = dialogTraceFilterForm;
					currentTraceFilterForm.setOffset(0);
					currentTraceFilterForm.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));
					prefs.putInt(Constants.CURRENT_OFFSET, 0);
					startRefreshJob();
				}
			}

		}
	};

	private SeverityFilterListener severityFilterListener = new SeverityFilterListener() {
		@Override
		public void onSeverityFilterLoad(EnumSet<RuleSeverity> severities) {
			if (!severities.isEmpty()) {
				currentTraceFilterForm.setSeverities(severities);
			} else {
				currentTraceFilterForm.setSeverities(null);
			}
			currentTraceFilterForm.setOffset(0);
			currentTraceFilterForm.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));
			prefs.putInt(Constants.CURRENT_OFFSET, 0);

			startRefreshJob();
		}
	};

	/**
	 * The constructor.
	 */
	public VulnerabilitiesView() {
	}


	public ContrastSDK getSdk() {
		return sdk;
	}

	public void refreshSdk() {
		sdk = ContrastCoreActivator.getContrastSDK();
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		currentTraceFilterForm = getTraceFilterFormFromEclipsePreferences();
		try {
			createList(parent);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// refreshTraces();
		// viewer.setLabelProvider(new TraceLabelProvider());
		getSite().setSelectionProvider(table);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		refreshJob = new RefreshJob(resource.getString("REFRESH_LABEL"), this);
		refreshJob.schedule();
	}

	private void createList(Composite parent) throws MalformedURLException {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		book = new PageBook(composite, SWT.NONE);
		book.setLayoutData(gd);

		mainPage = createMainPage(book);
		noVulnerabilitiesPage = createNoVulnerabilitiesPage(book);

		detailsPage = new VulnerabilityDetailsPage(book, SWT.NONE, this);
		loadingPage = new LoadingPage(book, SWT.NONE, this);
		configurationPage = new ConfigurationPage(book, SWT.NONE, this);

		statusLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		statusLabel.setLayoutData(gd);

		// book.showPage(mainPage);
		// addListeners(mainPage);
		currentPage = mainPage;
		activePage = mainPage;
	}

	public void showVulnerabilityPage() {
		book.showPage(currentPage);
		activePage = currentPage;
		refreshAction.setEnabled(true);
	}

	public void showLoadingPage() {
		book.showPage(loadingPage);
		activePage = loadingPage;
		refreshAction.setEnabled(false);
	}

	public void showConfigurationPage() {
		book.showPage(configurationPage);
		activePage = configurationPage;
		refreshAction.setEnabled(Util.hasConfiguration());
	}

	private VulnerabilityPage createNoVulnerabilitiesPage(PageBook book) throws MalformedURLException {
		VulnerabilityPage noVulnerabilitiesPage = new VulnerabilityPage(book, SWT.NONE, this);
		noVulnerabilitiesPage.getLabel().setText(resource.getString("ZERO_VULNERABILITIES_LABEL"));
		Label label = new Label(noVulnerabilitiesPage, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		label.setLayoutData(gd);
		label.setText(resource.getString("NO_VULNERABILITIES_WERE_FOUND_LABEL"));
		return noVulnerabilitiesPage;
	}

	private VulnerabilityPage createMainPage(PageBook book) throws MalformedURLException {
		VulnerabilityPage mainPage = new MainPage(book, SWT.NONE, this);
		createViewer(mainPage);
		return mainPage;
	}

	private void addListeners(VulnerabilityPage page) {
		page.setPageLoaderListener(pageLoaderListener);
		page.setOpenFilterDialogButtonListener(openFilterDialogButtonListener);
		page.setSeverityFilterListener(severityFilterListener);
		// page.getOpenFilterDialogButton().addListener(SWT.Selection,
		// openFilterDialogButtonListener);
	}

	private void removeListeners(VulnerabilityPage page) {
		// page.getOpenFilterDialogButton().removeListener(SWT.Selection,
		// openFilterDialogButtonListener);
	}

	private void createViewer(Composite composite) {
		table = new TableViewer(composite, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.getTable().setLayoutData(gd);
		table.setLabelProvider(new VulnerabilityLabelProvider());
		TableColumn column = new TableColumn(table.getTable(), SWT.NONE);
		column.setWidth(80);
		column.setText(resource.getString("SEVERITY_LABEL"));

		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
					traceSort = Constants.SORT_BY_SEVERITY;
				} else {
					traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY;
				}
				prefs.put(Constants.TRACE_SORT, traceSort);
				currentTraceFilterForm.setSort(traceSort);
				refreshTraces(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		column = new TableColumn(table.getTable(), SWT.NONE);
		column.setWidth(600);
		column.setText(resource.getString("VULNERABILITY_LABEL"));

		column.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (traceSort.startsWith(Constants.SORT_DESCENDING)) {
					traceSort = Constants.SORT_BY_TITLE;
				} else {
					traceSort = Constants.SORT_DESCENDING + Constants.SORT_BY_TITLE;
				}
				prefs.put(Constants.TRACE_SORT, traceSort);
				currentTraceFilterForm.setSort(traceSort);
				refreshTraces(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

		});

		column = new TableColumn(table.getTable(), SWT.NONE);
		column.setWidth(400);
		column.setText(resource.getString("APPLICATION_LABEL"));

		table.getTable().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
				openVulnerabilityByMouseEvent(e.x, e.y, false);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				openVulnerabilityByMouseEvent(e.x, e.y, true);
			}
		});

		column = new TableColumn(table.getTable(), SWT.NONE);
		column.setWidth(100);
		column.setText("");

		table.getTable().setLinesVisible(true);
		table.getTable().setHeaderVisible(true);
		table.setContentProvider(ArrayContentProvider.getInstance());
		TableLayout layout = new TableLayout();
		table.getTable().setLayout(layout);
	}

	/**
	 * Based on the mouse event shows the user the vulnerability in browser or its
	 * details on the plugin.
	 *
	 * @param xCoord
	 *            Mouse event X coordinate.
	 * @param yCoord
	 *            Mouse event Y coordinate.
	 * @param isDoubleClick
	 *            Whether the mouse event is a double click event or not.
	 */
	private void openVulnerabilityByMouseEvent(int xCoord, int yCoord, boolean isDoubleClick) {
		if (getOrgUuid() == null)
			return;

		ISelection sel = table.getSelection();

		if (sel instanceof IStructuredSelection && ((IStructuredSelection) sel).getFirstElement() instanceof Trace) {
			final Trace trace = (Trace) ((IStructuredSelection) sel).getFirstElement();

			int action = getActionFromClick(isDoubleClick, new Point(xCoord, yCoord));

			if (VIEW_VULNERABILITY_EVENTS_ACTION == action && !trace.getTitle().contains(Constants.UNLICENSED))
				showVulnerabiltyDetails(trace, VulnerabilityDetailsTab.EVENTS);
			else if (SHOW_VULNERABILITY_IN_BROWSER_ACTION == action) {
				try {
					openTraceInBrowser(trace);
				} catch (Exception e1) {
					ContrastUIActivator.log(e1);
				}
			}
		}
	}

	/**
	 * Determines what action should be performed based on the mouse event.
	 *
	 * @param isDoubleClick
	 *            Whether the mouse event that triggered this was a double click
	 *            event.
	 * @param point
	 *            The position of the click on the TableView.
	 * @return Action constant that represents what should be done based on the
	 *         mouse event.
	 */
	private int getActionFromClick(boolean isDoubleClick, Point point) {
		ViewerCell cell = table.getCell(point);

		if (cell != null) {
			int columnIndex = cell.getColumnIndex();
			if (isDoubleClick && (columnIndex == 0 || columnIndex == 1))
				return VIEW_VULNERABILITY_EVENTS_ACTION;
			else if (columnIndex == 2)
				return VIEW_VULNERABILITY_OVERVIEW_ACTION;
			else if (columnIndex == 3)
				return SHOW_VULNERABILITY_IN_BROWSER_ACTION;
		}

		return NO_ACTION;
	}

	private void showVulnerabiltyDetails(Trace trace, VulnerabilityDetailsTab tab) {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				StoryResponse story = null;
				EventSummaryResponse eventSummary = null;
				HttpRequestResponse httpRequest = null;
				String status = null;
				RecommendationResponse recommendationResource = null;
				TagsResponse traceTagsResource = null;
				TagsResponse orgTagsResource = null;

				try {
					Key key = new Key(ContrastUIActivator.getOrgUuid(), trace.getUuid());
					Key keyForOrg = new Key(ContrastUIActivator.getOrgUuid(), null);

					story = getStory(key);
					eventSummary = getEventSummary(key);
					httpRequest = getHttpRequest(key);
					status = getVulnerabilityStatus(key);
					recommendationResource = getRecommendationResource(key);

					traceTagsResource = getTags(key);
					orgTagsResource = getTags(keyForOrg);
				} catch (IOException | UnauthorizedException e1) {
					ContrastUIActivator.log(e1);
				}
				detailsPage.setStory(story);
				detailsPage.setRecommendationResource(recommendationResource);
				detailsPage.setEventSummaryResource(eventSummary);
				detailsPage.setHttpRequest(httpRequest);
				detailsPage.setVulnerabilityStatus(StringUtils.isBlank(status) ? trace.getStatus() : status);
				detailsPage.setOrgTagsResource(orgTagsResource);
				detailsPage.setTraceTagsResource(traceTagsResource);

				detailsPage.createAdditionalTabs();
				removeListeners(currentPage);
				book.showPage(detailsPage);
				detailsPage.setDefaultSelection(tab);
				activePage = detailsPage;
				refreshAction.setEnabled(false);
				try {
					detailsPage.setTrace(trace);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
	}

	private RecommendationResponse getRecommendationResource(Key key) throws IOException, UnauthorizedException {

		RecommendationResponse recommendationResource = contrastCache.getRecommendationResources().get(key);
		if (recommendationResource == null) {
			recommendationResource = sdk.getRecommendation(key.getOrgUuid(), key.getTraceId());
			contrastCache.getRecommendationResources().put(key, recommendationResource);
		}
		return recommendationResource;
	}

	private TagsResponse getTags(Key key) throws IOException, UnauthorizedException {
		TagsResponse tagsResource = contrastCache.getTagsResources().get(key);

		if (tagsResource == null) {
			if (key.getTraceId() != null) {
				tagsResource = sdk.getTagsByTrace(key.getOrgUuid(), key.getTraceId());
			} else {
				tagsResource = sdk.getTraceTagsByOrganization(key.getOrgUuid());
			}
			contrastCache.getTagsResources().put(key, tagsResource);
		}
		return tagsResource;
	}

	private StoryResponse getStory(Key key) throws IOException, UnauthorizedException {
		StoryResponse story = contrastCache.getStoryResources().get(key);
		if (story == null) {
			story = sdk.getStory(key.getOrgUuid(), key.getTraceId());
			contrastCache.getStoryResources().put(key, story);
		}
		return story;
	}

	private EventSummaryResponse getEventSummary(Key key) throws IOException, UnauthorizedException {
		EventSummaryResponse eventSummary = contrastCache.getEventSummaryResources().get(key);
		if (eventSummary == null) {
			eventSummary = sdk.getEventSummary(key.getOrgUuid(), key.getTraceId());
			contrastCache.getEventSummaryResources().put(key, eventSummary);
		}
		return eventSummary;
	}

	private HttpRequestResponse getHttpRequest(Key key) throws IOException, UnauthorizedException {
		HttpRequestResponse httpRequest = contrastCache.getHttpRequestResources().get(key);
		if (httpRequest == null) {
			httpRequest = sdk.getHttpRequest(key.getOrgUuid(), key.getTraceId());
			contrastCache.getHttpRequestResources().put(key, httpRequest);
		}
		return httpRequest;
	}

	private String getVulnerabilityStatus(Key key) throws IOException, UnauthorizedException {
		TraceFilterForm form = new TraceFilterForm();
		form.setFilterText(key.getTraceId());
		List<Trace> traces = sdk.getTracesInOrg(key.getOrgUuid(), form).getTraces();
		return traces.get(0).getStatus();
	}

	public void refreshTraces(final boolean isFullRefresh) {
		if (activePage != mainPage && activePage != noVulnerabilitiesPage && activePage != configurationPage) {
			return;
		}
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				statusLabel.setText("");
				if (table != null && !table.getTable().isDisposed()) {
					startRefreshTraces();
				} else {
					refreshJob.cancel();
				}
			}
		});

		String orgUuid;
		try {
			orgUuid = ContrastCoreActivator.getSelectedOrganizationUuid();
		} catch (final Exception e) {
			ContrastUIActivator.log(e);
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if (table != null && !table.getTable().isDisposed()) {
						noOrgUuid(e);
					} else {
						refreshJob.cancel();
					}
				}
			});
			return;
		}
		if (orgUuid != null && !orgUuid.isEmpty()) {
			try {

				if (isFullRefresh)
					currentOffset = 0;
				final Traces traces = getTraces(currentTraceFilterForm, orgUuid);
				if (traces != null)
					total = traces.getCount();

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						if (table != null && !table.getTable().isDisposed()) {
							// Refresh filters
							if (isFullRefresh) {
								currentPage.getServers(orgUuid, true);
								currentPage.getApplications(orgUuid, true, null);
							}
							// Refresh traces and selections
							refreshUI(traces, isFullRefresh);
						} else {
							refreshJob.cancel();
						}
					}
				});
			} catch (final Exception e) {
				ContrastUIActivator.log(e);
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						if (table != null && !table.getTable().isDisposed()) {
							statusLabel.setText(resource.getString("SERVER_ERROR") + e.getMessage());
						} else {
							refreshJob.cancel();
						}
						book.showPage(noVulnerabilitiesPage);
						activePage = noVulnerabilitiesPage;
						currentPage = noVulnerabilitiesPage;
						refreshAction.setEnabled(true);
					}
				});
				return;
			} finally {
				if (currentPage == noVulnerabilitiesPage || currentPage == mainPage) {
					addListeners(currentPage);
				}
				if (Util.hasConfiguration()) {
					refreshAction.setEnabled(true);
				}
			}

		} else {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					showConfigurationPage();
					if (Util.hasConfiguration()) {
						refreshAction.setEnabled(true);
					}
				}

			});
			if (currentPage == noVulnerabilitiesPage || currentPage == mainPage) {
				addListeners(currentPage);
			}
		}
	}

	/**
	 * Makes refresh of traces list, services and applications lists.
	 *
	 * @param traces
	 *            New traces list.
	 * @param selectedServer
	 *            Combo selection for server list.
	 * @param selectedApp
	 *            Combo selection for application list.
	 * @param isFullRefresh
	 *            Indicates if this is just a page change or a UI refresh triggered
	 *            by filters or Refresh button which might change which views are
	 *            initialized again.
	 */
	private void refreshUI(Traces traces, final boolean isFullRefresh) {
		if (traces != null && traces.getTraces() != null) {
			Trace[] traceArray = traces.getTraces().toArray(new Trace[0]);
			table.setInput(traceArray);
		}
		if (traces != null && traces.getTraces() != null && traces.getTraces().size() > 0) {
			if (activePage != mainPage) {
				book.showPage(mainPage);
				activePage = mainPage;
				currentPage = mainPage;
			}

			addListeners(mainPage);
			refreshAction.setEnabled(true);
			currentPage.getLabel().setText(traces.getTraces().size() + " " + resource.getString("VULNERABILITIES_LABEL"));
		} else {
			if (activePage != noVulnerabilitiesPage) {
				book.showPage(noVulnerabilitiesPage);
				activePage = noVulnerabilitiesPage;
				currentPage = noVulnerabilitiesPage;
			}

			refreshAction.setEnabled(true);
			addListeners(noVulnerabilitiesPage);
		}

		// Refresh page combo
		if (isFullRefresh)
			currentPage.initializePageCombo(PAGE_LIMIT, total);

		table.getControl().getParent().layout(true, true);
		table.getControl().getParent().redraw();
	}

	private void noOrgUuid(Exception e) {
		statusLabel.setText(resource.getString("SERVER_ERROR") + e.getMessage());
		table.refresh();
		if (currentPage == noVulnerabilitiesPage || currentPage == mainPage) {
			addListeners(currentPage);
		}
	}

	private void startRefreshTraces() {
		showLoadingPage();
		table.setInput(new Trace[0]);
		currentPage.getLabel().setText(resource.getString("ZERO_VULNERABILITIES_LABEL"));
		refreshAction.setEnabled(false);
		removeListeners(mainPage);
		removeListeners(noVulnerabilitiesPage);
		contrastCache.clear();
	}

	private Traces getTraces(TraceFilterForm traceFilterform, String orgUuid)
			throws IOException, UnauthorizedException {
		if (orgUuid == null) {
			return null;
		}
		Traces traces = null;

		Long serverId = Constants.ALL_SERVERS;

		if (traceFilterform.getServerIds() != null && !traceFilterform.getServerIds().isEmpty()
				&& traceFilterform.getServerIds().get(0) != Constants.ALL_SERVERS) {
			serverId = traceFilterform.getServerIds().get(0);
		}

		String appId = prefs.get(Constants.APPLICATION_ID, Constants.ALL_APPLICATIONS);

		if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTracesInOrg(orgUuid, traceFilterform);
		} else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTraces(orgUuid, appId, traceFilterform);
		} else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTracesInOrg(orgUuid, traceFilterform);
		} else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTraces(orgUuid, appId, traceFilterform);
		}
		return traces;
	}

	@Override
	public void dispose() {
		if (refreshJob != null) {
			refreshJob.cancel();
		}
		super.dispose();
	}

	private String getOrgUuid() {
		return ContrastUIActivator.getOrgUuid();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				VulnerabilitiesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(table.getControl());
		table.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, table);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openPreferencesPage);
		manager.add(refreshAction);
		// manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openPreferencesPage);
		manager.add(refreshAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openPreferencesPage);
		manager.add(refreshAction);
	}

	private void makeActions() {
		openPreferencesPage = new Action() {
			public void run() {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
						ContrastPreferencesPage.ID, null, null);
				String selectedOrganization = ContrastCoreActivator.getSelectedOrganization();
				dialog.open();
				// check if selected organization has been changed in the dialog
				if (!selectedOrganization.equals(ContrastCoreActivator.getSelectedOrganization())) {
					currentOffset = 0;
					if (currentTraceFilterForm != null) {
						currentTraceFilterForm.setServerIds(null);
						currentTraceFilterForm.setAppVersionTags(null);
						currentTraceFilterForm.setOffset(currentOffset);
					}
					prefs.put(Constants.APPLICATION_ID, Constants.ALL_APPLICATIONS);
					prefs.put(Constants.TRACE_FILTER_TYPE_APP_VERSION_TAGS, "");
					prefs.putInt(Constants.CURRENT_OFFSET, currentOffset);

					sdk = ContrastCoreActivator.getContrastSDK();
					startRefreshJob();
				}
			}
		};
		openPreferencesPage.setText(resource.getString("CONTRAST_PREFERENCES_LABEL"));
		openPreferencesPage.setToolTipText(resource.getString("OPEN_CONTRAST_PREFERENCES_LABEL"));
		openPreferencesPage.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		refreshAction = new Action() {
			public void run() {
				startRefreshJob();
			}
		};
		refreshAction.setText(resource.getString("REFRESH_LABEL"));
		refreshAction.setToolTipText(resource.getString("REFRESH_TOOLTIP"));
		refreshAction.setImageDescriptor(
				ContrastUIActivator.imageDescriptorFromPlugin(ContrastUIActivator.PLUGIN_ID, "/icons/refresh_tab.gif"));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = table.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				dblClickAction(obj);
			}
		};
	}

	protected void dblClickAction(Object object) {
	}

	private void hookDoubleClickAction() {
		table.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	@Override
	public void setFocus() {
		if (table != null || !table.getControl().isDisposed()) {
			table.getControl().setFocus();
		}
	}

	private void startRefreshJob() {
		if (refreshJob.getState() == Job.NONE) {
			refreshJob.schedule();
		} else if (refreshJob.getState() == Job.SLEEPING) {
			refreshJob.wakeUp();
		}
	}

	public void openTraceInBrowser(Trace trace) throws MalformedURLException, PartInitException {
		if (trace == null) {
			return;
		}
		// https://apptwo.contrastsecurity.com/Contrast/static/ng/index.html#/orgUuid/vulns/<VULN_ID>/overview
		URL url = getOverviewUrl(trace.getUuid());
		PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
	}

	public URL getOverviewUrl(String traceId) throws MalformedURLException {

		String teamServerUrl = Constants.TEAM_SERVER_URL_VALUE;

		OrganizationConfig organizationConfig = ContrastCoreActivator.getOrganizationConfiguration(ContrastCoreActivator.getSelectedOrganization());
		if (organizationConfig != null) {
			teamServerUrl = organizationConfig.getContrastUrl();
		}

		teamServerUrl = teamServerUrl.trim();
		if (teamServerUrl != null && teamServerUrl.endsWith("/api")) {
			teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
		}
		if (teamServerUrl != null && teamServerUrl.endsWith("/api/")) {
			teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
		}
		String urlStr = teamServerUrl + "/static/ng/index.html#/" + getOrgUuid() + "/vulns/" + traceId + "/overview";
		URL url = new URL(urlStr);
		return url;
	}


	private TraceFilterForm getTraceFilterFormFromEclipsePreferences() {

		EnumSet<RuleSeverity> severities = getSelectedSeveritiesFromEclipsePreferences();
		List<String> statuses = getSelectedStatusesFromEclipsePreferences();

		Long serverId = prefs.getLong(Constants.SERVER_ID, Constants.ALL_SERVERS);
		String appId = prefs.get(Constants.APPLICATION_ID, Constants.ALL_APPLICATIONS);

		TraceFilterForm form = null;
		if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
		} else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			form = Util.getTraceFilterForm(currentOffset, PAGE_LIMIT);
		} else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
		} else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			form = Util.getTraceFilterForm(serverId, currentOffset, PAGE_LIMIT);
		}
		form.setSeverities(severities);
		form.setStatus(statuses);

		String lastDetected = prefs.get(Constants.LAST_DETECTED, "");
		if (!lastDetected.isEmpty()) {
			Long lastDetectedFrom = prefs.getLong(Constants.LAST_DETECTED_FROM, 0);
			Long lastDetectedTo = prefs.getLong(Constants.LAST_DETECTED_TO, 0);


			if(lastDetected.equals(resource.getString("LAST_DETECTED_ALL"))) {
				if (lastDetectedFrom != 0) {
					Date fromDate = new Date(lastDetectedFrom);
					form.setStartDate(fromDate);
				}
			}
			else if(lastDetected.equals(resource.getString("LAST_DETECTED_CUSTOM"))) {
				if (lastDetectedFrom != 0) {
					Date fromDate = new Date(lastDetectedFrom);
					form.setStartDate(fromDate);
				}
				if (lastDetectedTo != 0) {
					Date toDate = new Date(lastDetectedTo);
					form.setEndDate(toDate);
				}
			} else {
				if (lastDetectedFrom != 0) {
					Date fromDate = new Date(lastDetectedFrom);
					form.setStartDate(fromDate);
				}
			}
		}

		form.setOffset(prefs.getInt(Constants.CURRENT_OFFSET, 0));
		form.setSort(prefs.get(Constants.TRACE_SORT, Constants.SORT_DESCENDING + Constants.SORT_BY_SEVERITY));
		currentOffset = prefs.getInt(Constants.CURRENT_OFFSET, 0);
		form.setExpand(EnumSet.of(TraceFilterForm.TraceExpandValue.APPLICATION));

		String appVersionTag = prefs.get(Constants.TRACE_FILTER_TYPE_APP_VERSION_TAGS, "");
		if (!appVersionTag.isEmpty()) {
			form.setAppVersionTags(Collections.singletonList(appVersionTag));
		} else {
			form.setAppVersionTags(null);
		}

		return form;
	}

	private EnumSet<RuleSeverity> getSelectedSeveritiesFromEclipsePreferences() {
		EnumSet<RuleSeverity> severities = EnumSet.noneOf(RuleSeverity.class);
		if (prefs.getBoolean(Constants.SEVERITY_LEVEL_NOTE, false)) {
			severities.add(RuleSeverity.NOTE);
		}
		if (prefs.getBoolean(Constants.SEVERITY_LEVEL_LOW, false)) {
			severities.add(RuleSeverity.LOW);
		}
		if (prefs.getBoolean(Constants.SEVERITY_LEVEL_MEDIUM, false)) {
			severities.add(RuleSeverity.MEDIUM);
		}
		if (prefs.getBoolean(Constants.SEVERITY_LEVEL_HIGH, false)) {
			severities.add(RuleSeverity.HIGH);
		}
		if (prefs.getBoolean(Constants.SEVERITY_LEVEL_CRITICAL, false)) {
			severities.add(RuleSeverity.CRITICAL);
		}
		return severities;
	}

	private List<String> getSelectedStatusesFromEclipsePreferences() {

		List<String> statuses = new ArrayList<>();
		if (prefs.getBoolean(Constants.STATUS_AUTO_REMEDIATED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_AUTO_REMEDIATED);
		}
		if (prefs.getBoolean(Constants.STATUS_CONFIRMED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_CONFIRMED);
		}
		if (prefs.getBoolean(Constants.STATUS_SUSPICIOUS, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_SUSPICIOUS);
		}
		if (prefs.getBoolean(Constants.STATUS_NOT_A_PROBLEM, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING);
		}
		if (prefs.getBoolean(Constants.STATUS_REMEDIATED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_REMEDIATED);
		}
		if (prefs.getBoolean(Constants.STATUS_REPORTED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_REPORTED);
		}
		if (prefs.getBoolean(Constants.STATUS_FIXED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_FIXED);
		}
		if (prefs.getBoolean(Constants.STATUS_BEING_TRACKED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_BEING_TRACKED);
		}
		if (prefs.getBoolean(Constants.STATUS_UNTRACKED, false)) {
			statuses.add(Constants.VULNERABILITY_STATUS_UNTRACKED);
		}

		return statuses;
	}
}
