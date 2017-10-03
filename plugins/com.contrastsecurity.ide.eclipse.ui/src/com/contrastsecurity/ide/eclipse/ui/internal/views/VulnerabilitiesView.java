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
import java.util.List;

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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.core.extended.ExtendedContrastSDK;
import com.contrastsecurity.ide.eclipse.core.extended.HttpRequestResource;
import com.contrastsecurity.ide.eclipse.core.extended.StoryResource;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.cache.ContrastCache;
import com.contrastsecurity.ide.eclipse.ui.cache.Key;
import com.contrastsecurity.ide.eclipse.ui.internal.job.RefreshJob;
import com.contrastsecurity.ide.eclipse.ui.internal.model.AbstractPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ApplicationUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ConfigurationPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.LoadingPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.MainPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ServerUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityDetailsPage;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityDetailsTab;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityPage;
import com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;

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
	 * The mouse event should trigger to show vulnerability details view on overview tab.
	 */
	private final static int VIEW_VULNERABILITY_OVERVIEW_ACTION = 0;
	/**
	 * The mouse event should trigger to show vulnerability details view on Events tab.
	 */
	private final static int VIEW_VULNERABILITY_EVENTS_ACTION = 1;
	/**
	 * The mouse event should trigger to take the user to vulnerability on browser.
	 * @warning Might not work if the default organization its different from current one on eclipse plugin.
	 */
	private final static int SHOW_VULNERABILITY_IN_BROWSER_ACTION = 2;

	private TableViewer viewer;
	private Action saveFilterAction;
	private Action refreshAction;
	private Action openPreferencesPage;
	private Action doubleClickAction;
	private Label statusLabel;
	private ExtendedContrastSDK sdk = ContrastCoreActivator.getContrastSDK();
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

	private ISelectionChangedListener listener = new ISelectionChangedListener() {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			startRefreshJob();
		}
	};

	/**
	 * The constructor.
	 */
	public VulnerabilitiesView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		createList(parent);
		// refreshTraces();
		// viewer.setLabelProvider(new TraceLabelProvider());
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		refreshJob = new RefreshJob("Refresh ...", this);
		refreshJob.schedule();
	}

	private void createList(Composite parent) {
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

	private VulnerabilityPage createNoVulnerabilitiesPage(PageBook book) {
		VulnerabilityPage noVulnerabilitiesPage = new VulnerabilityPage(book, SWT.NONE, this);
		noVulnerabilitiesPage.getLabel().setText("0 Vulnerabilities");
		Label label = new Label(noVulnerabilitiesPage, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		label.setLayoutData(gd);
		label.setText("No vulnerabilities were found.");
		return noVulnerabilitiesPage;
	}

	private VulnerabilityPage createMainPage(PageBook book) {
		VulnerabilityPage mainPage = new MainPage(book, SWT.NONE, this);
		createViewer(mainPage);
		return mainPage;
	}

	private void addListeners(VulnerabilityPage page) {
		page.getServerCombo().addSelectionChangedListener(listener);
		page.getApplicationCombo().addSelectionChangedListener(listener);
	}

	private void removeListeners(VulnerabilityPage page) {
		page.getServerCombo().removeSelectionChangedListener(listener);
		page.getApplicationCombo().removeSelectionChangedListener(listener);
	}

	private void createViewer(Composite composite) {
		viewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(gd);
		viewer.setLabelProvider(new VulnerabilityLabelProvider());
		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(80);
		column.setText("Severity");

		column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(600);
		column.setText("Vulnerability");

		column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(400);
		column.setText("Actions");
		viewer.getTable().addMouseListener(new MouseListener() {

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

		column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(100);
		column.setText("");

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
	}
	
	/**
	 * Based on the mouse event shows the user the vulnerability in browser or its details on the plugin.
	 * @param xCoord Mouse event X coordinate.
	 * @param yCoord Mouse event Y coordinate.
	 * @param isDoubleClick Whether the mouse event is a double click event or not.
	 */
	private void openVulnerabilityByMouseEvent(int xCoord, int yCoord, boolean isDoubleClick) {
		if (getOrgUuid() == null)
			return;
		
		ISelection sel = viewer.getSelection();
		
		if (sel instanceof IStructuredSelection && ((IStructuredSelection)sel).getFirstElement() instanceof Trace) {
			final Trace trace = (Trace) ((IStructuredSelection)sel).getFirstElement();
			
			int action = getActionFromClick(isDoubleClick, new Point(xCoord, yCoord));
			
			if(VIEW_VULNERABILITY_OVERVIEW_ACTION == action && !trace.getTitle().contains(Constants.UNLICENSED))
				showVulnerabiltyDetails(trace, VulnerabilityDetailsTab.OVERVIEW);
			else if(VIEW_VULNERABILITY_EVENTS_ACTION == action && !trace.getTitle().contains(Constants.UNLICENSED))
				showVulnerabiltyDetails(trace, VulnerabilityDetailsTab.EVENTS);
			else if(SHOW_VULNERABILITY_IN_BROWSER_ACTION == action) {
				try {
					openTraceInBrowser(trace); 
				}
				catch (Exception e1) {
					ContrastUIActivator.log(e1); 
				}
			}
		}
	}
	
	/**
	 * Determines what action should be performed based on the mouse event.
	 * @param isDoubleClick Whether the mouse event that triggered this was a double click event.
	 * @param point The position of the click on the TableView.
	 * @return Action constant that represents what should be done based on the mouse event.
	 */
	private int getActionFromClick(boolean isDoubleClick, Point point) {
		ViewerCell cell = viewer.getCell(point);
		
		if(cell != null) {
			int columnIndex = cell.getColumnIndex();
			if(isDoubleClick && (columnIndex == 0 || columnIndex == 1))
				return VIEW_VULNERABILITY_EVENTS_ACTION;
			else if(columnIndex == 2)
				return VIEW_VULNERABILITY_OVERVIEW_ACTION;
			else if(columnIndex == 3)
				return SHOW_VULNERABILITY_IN_BROWSER_ACTION;
		}
		
		return NO_ACTION;
	}
	
	private void showVulnerabiltyDetails(Trace trace, VulnerabilityDetailsTab tab) {
		BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
			public void run() {
				StoryResource story = null;
				EventSummaryResource eventSummary = null;
				HttpRequestResource httpRequest = null;
				try {
					Key key = new Key(ContrastUIActivator.getOrgUuid(), trace.getUuid());
					story = getStory(key);
					eventSummary = getEventSummary(key);
					httpRequest = getHttpRequest(key);
				} catch (IOException | UnauthorizedException e1) {
					ContrastUIActivator.log(e1);
				}
				detailsPage.setStory(story);
				detailsPage.setEventSummaryResource(eventSummary);
				detailsPage.setHttpRequest(httpRequest);
				detailsPage.createAdditionalTabs();
				removeListeners(currentPage);
				book.showPage(detailsPage);
				detailsPage.setDefaultSelection(tab);
				activePage = detailsPage;
				refreshAction.setEnabled(false);
				detailsPage.setTrace(trace);
			}

		});
	}

	private StoryResource getStory(Key key) throws IOException, UnauthorizedException {
		StoryResource story = contrastCache.getStoryResources().get(key);
		if (story == null) {
			story = sdk.getStory(key.getOrgUuid(), key.getTraceId());
			contrastCache.getStoryResources().put(key, story);
		}
		return story;
	}

	private EventSummaryResource getEventSummary(Key key) throws IOException, UnauthorizedException {
		EventSummaryResource  eventSummary = contrastCache.getEventSummaryResources().get(key);
		if (eventSummary == null) {
			eventSummary = sdk.getEventSummary(key.getOrgUuid(), key.getTraceId());
			contrastCache.getEventSummaryResources().put(key, eventSummary);
		}
		return eventSummary;
	}

	private HttpRequestResource getHttpRequest(Key key) throws IOException, UnauthorizedException {
		HttpRequestResource httpRequest = contrastCache.getHttpRequestResources().get(key);
		if (httpRequest == null) {
			httpRequest = sdk.getHttpRequest(key.getOrgUuid(), key.getTraceId());
			contrastCache.getHttpRequestResources().put(key, httpRequest);
		}
		return httpRequest;
	}
	public void refreshTraces() {
		if (activePage != mainPage && activePage != noVulnerabilitiesPage && activePage != configurationPage) {
			return;
		}
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				statusLabel.setText("");
				if (viewer != null && !viewer.getTable().isDisposed()) {
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
					if (viewer != null && !viewer.getTable().isDisposed()) {
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
				final Long[] selectedServerId = new Long[1];
				final String[] selectedAppId = new String[1];
				final ISelection[] selectedServer = new ISelection[1];
				final ISelection[] selectedApp = new ISelection[1];
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						selectedServerId[0] = getSelectedServerId();
						selectedAppId[0] = getSelectedAppId();
						selectedServer[0] = currentPage.getServerCombo().getSelection();
						selectedApp[0] = currentPage.getApplicationCombo().getSelection();
					}
				});

				final Traces traces = getTraces(orgUuid, selectedServerId[0], selectedAppId[0]);
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						if (viewer != null && !viewer.getTable().isDisposed()) {
							//Refresh filters
							currentPage.updateApplicationCombo(orgUuid, true);
							currentPage.updateServerCombo(orgUuid, true);
							//Refresh traces and selections
							refreshUI(traces, selectedServer[0], selectedApp[0]);
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
						if (viewer != null && !viewer.getTable().isDisposed()) {
							statusLabel.setText("Server error: " + e.getMessage());
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
	 * @param traces New traces list.
	 * @param selectedServer Combo selection for server list.
	 * @param selectedApp Combo selection for application list.
	 */
	private void refreshUI(Traces traces, ISelection selectedServer, ISelection selectedApp) {
		if (traces != null && traces.getTraces() != null) {
			Trace[] traceArray = traces.getTraces().toArray(new Trace[0]);
			viewer.setInput(traceArray);
		}
		if (traces != null && traces.getTraces() != null && traces.getTraces().size() > 0) {
			if (activePage != mainPage) {
				book.showPage(mainPage);
				activePage = mainPage;
				currentPage = mainPage;
			}
			
			currentPage.getServerCombo().setSelection(selectedServer);
			currentPage.getApplicationCombo().setSelection(selectedApp);
			
			addListeners(mainPage);
			refreshAction.setEnabled(true);
			currentPage.getLabel().setText(traces.getTraces().size() + " Vulnerabilities");
		} else {
			if (activePage != noVulnerabilitiesPage) {
				book.showPage(noVulnerabilitiesPage);
				activePage = noVulnerabilitiesPage;
				currentPage = noVulnerabilitiesPage;
			}
			
			currentPage.getServerCombo().setSelection(selectedServer);
			currentPage.getApplicationCombo().setSelection(selectedApp);
			
			refreshAction.setEnabled(true);
			addListeners(noVulnerabilitiesPage);
		}
		viewer.getControl().getParent().layout(true, true);
		viewer.getControl().getParent().redraw();
	}

	private void noOrgUuid(Exception e) {
		statusLabel.setText("Server error: " + e.getMessage());
		viewer.refresh();
		if (currentPage == noVulnerabilitiesPage || currentPage == mainPage) {
			addListeners(currentPage);
		}
	}

	private void startRefreshTraces() {
		showLoadingPage();
		viewer.setInput(new Trace[0]);
		currentPage.getLabel().setText("0 Vulnerabilities");
		refreshAction.setEnabled(false);
		removeListeners(mainPage);
		removeListeners(noVulnerabilitiesPage);
		contrastCache.clear();
	}

	private Traces getTraces(String orgUuid, Long serverId, String appId) throws IOException, UnauthorizedException {
		if (orgUuid == null) {
			return null;
		}
		if (appId == null) {
			appId = Constants.ALL_APPLICATIONS;
		}
		if (serverId == null || serverId < Constants.ALL_SERVERS) {
			serverId = Constants.ALL_SERVERS;
		}
		Traces traces = null;
		if (serverId == Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTracesInOrg(orgUuid, null);
		} else if (serverId == Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			traces = sdk.getTraces(orgUuid, appId, null);
		} else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
			TraceFilterForm form = getServerTraceForm(serverId);
			traces = sdk.getTracesInOrg(orgUuid, form);
		} else if (serverId != Constants.ALL_SERVERS && !Constants.ALL_APPLICATIONS.equals(appId)) {
			TraceFilterForm form = getServerTraceForm(serverId);
			traces = sdk.getTraces(orgUuid, appId, form);
		}
		return traces;
	}

	private TraceFilterForm getServerTraceForm(Long selectedServerId) {
		TraceFilterForm form = new TraceFilterForm();
		List<Long> serverIds = new ArrayList<>();
		serverIds.add(selectedServerId);
		form.setServerIds(serverIds);
		return form;
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
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
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
		manager.add(saveFilterAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openPreferencesPage);
		manager.add(refreshAction);
		manager.add(saveFilterAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openPreferencesPage);
		manager.add(refreshAction);
		manager.add(saveFilterAction);
	}

	private void makeActions() {
		openPreferencesPage = new Action() {
			public void run() {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
						ContrastPreferencesPage.ID, null, null);
				dialog.open();
				sdk = ContrastCoreActivator.getContrastSDK();
				startRefreshJob();
			}
		};
		openPreferencesPage.setText("Contrast Preferences Page");
		openPreferencesPage.setToolTipText("Open Contrast Preferences Page");
		openPreferencesPage.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		refreshAction = new Action() {
			public void run() {
				startRefreshJob();
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh vulnerabilities from server");
		refreshAction.setImageDescriptor(
				ContrastUIActivator.imageDescriptorFromPlugin(ContrastUIActivator.PLUGIN_ID, "/icons/refresh_tab.gif"));
		saveFilterAction = new Action() {
			public void run() {
				saveFilter();
			}
		};
		saveFilterAction.setText("Save");
		saveFilterAction.setToolTipText("Save Filter");
		saveFilterAction.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				dblClickAction(obj);
			}
		};
	}

	protected void dblClickAction(Object object) {
	}

	protected void saveFilter() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		Long serverId = getSelectedServerId();
		prefs.putLong(Constants.SERVER_ID, serverId);
		String appId = getSelectedAppId();
		prefs.put(Constants.APPLICATION_ID, appId);
	}

	private String getSelectedAppId() {
		ISelection sel = currentPage.getApplicationCombo().getSelection();
		if (sel instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof ApplicationUIAdapter) {
				return ((ApplicationUIAdapter) element).getId();
			}
		}
		return Constants.ALL_APPLICATIONS;
	}

	private Long getSelectedServerId() {
		ISelection sel = currentPage.getServerCombo().getSelection();
		if (sel instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof ServerUIAdapter) {
				return ((ServerUIAdapter) element).getId();
			}
		}
		return Constants.ALL_SERVERS;
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	@Override
	public void setFocus() {
		if (viewer != null || !viewer.getControl().isDisposed()) {
			viewer.getControl().setFocus();
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
		String teamServerUrl = ContrastCoreActivator.getPreferences().get(Constants.TEAM_SERVER_URL,
				Constants.TEAM_SERVER_URL_VALUE);
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

	public ExtendedContrastSDK getSdk() {
		return sdk;
	}

	public void refreshSdk() {
		sdk = ContrastCoreActivator.getContrastSDK();
	}

}
