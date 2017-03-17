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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ApplicationUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ContrastLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ServerUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.VulnerabilityLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.internal.preferences.ContrastPreferencesPage;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;
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

	private TableViewer viewer;
	private Action saveFilterAction;
	private Action refreshAction;
	private Action openPreferencesPage;
	private Action doubleClickAction;

	private ComboViewer serverCombo;

	private ComboViewer applicationCombo;

	private Label statusLabel;

	ContrastSDK sdk = ContrastCoreActivator.getContrastSDK();

	private Label countLabel;

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
		refreshTraces();
		// viewer.setLabelProvider(new TraceLabelProvider());
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void createList(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		Composite comboComposite = new Composite(composite, SWT.NONE);
		comboComposite.setLayout(new GridLayout(3, false));
		countLabel = new Label(comboComposite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		countLabel.setLayoutData(gd);
		// countLabel.setText("Show Vulnerabilities:");
		String orgUuid = getOrgUuid();
		createServerCombo(comboComposite, orgUuid);
		createApplicationCombo(comboComposite, orgUuid);
		createViewer(composite);
		
		statusLabel = new Label(composite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		statusLabel.setLayoutData(gd);
		statusLabel.setText("test");
	}

	private void createViewer(Composite composite) {
		viewer = new TableViewer(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(gd);
		viewer.setLabelProvider(new VulnerabilityLabelProvider());
		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(100);
		column.setText("Severity");
		
		column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(600);
		column.setText("Vulnerability");

		column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setWidth(150);
		column.setText("Actions");
		viewer.getTable().addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				if (getOrgUuid() == null) {
					return;
				}
				Point point = new Point(e.x, e.y);
				ViewerCell cell = viewer.getCell(point);
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) sel).getFirstElement();
					if (selected instanceof Trace) {
						if (cell != null && cell.getColumnIndex() == 2) {
							System.out.println("View Details");
						}
						if (cell != null && cell.getColumnIndex() == 3) {
							try {
								// https://apptwo.contrastsecurity.com/Contrast/static/ng/index.html#/orgUuid/vulns/<VULN_ID>/overview
								String teamServerUrl = ContrastCoreActivator.getPreferences().get(Constants.TEAM_SERVER_URL, Constants.TEAM_SERVER_URL_VALUE);
								teamServerUrl = teamServerUrl.trim();
								if (teamServerUrl != null && teamServerUrl.endsWith("/api")) {
									teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 4);
								}
								if (teamServerUrl != null && teamServerUrl.endsWith("/api/")) {
									teamServerUrl = teamServerUrl.substring(0, teamServerUrl.length() - 5);
								}
								String urlStr = teamServerUrl + "/static/ng/index.html#/" + getOrgUuid() + "/vulns/" + ((Trace)selected).getUuid() + "/overview";
								URL url = new URL(urlStr);
								PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(url);
							} catch (Exception e1) {
								ContrastUIActivator.log(e1);
							}
						}
					}
				}
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				
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

	private void refreshTraces() {
		String orgUuid;
		viewer.setInput(new Trace[0]);
		statusLabel.setText("");
		countLabel.setText("0 Vulnerabilities");
		try {
			orgUuid = Util.getDefaultOrganizationUuid();
		} catch (IOException | UnauthorizedException e) {
			ContrastUIActivator.log(e);
			statusLabel.setText("Server error: " + e.getMessage());
			viewer.refresh();
			return;
		}
		if (orgUuid != null) {
			try {
				Long selectedServerId = getSelectedServerId();
				String selectedAppId = getSelectedAppId();
				Traces traces = getTraces(orgUuid, selectedServerId, selectedAppId);
				if (traces != null && traces.getTraces() != null) {
					Trace[] traceArray = traces.getTraces().toArray(new Trace[0]);
					viewer.setInput(traceArray);
				}
				if (traces != null && traces.getTraces() != null && traces.getTraces().size() > 0) {
					countLabel.setText(traces.getTraces().size() + " Vulnerabilities");
					viewer.getTable().setHeaderVisible(true);
				} else {
					statusLabel.setText("No vulnerabilities were found.");
					viewer.getTable().setHeaderVisible(false);
				}
				viewer.getControl().getParent().layout(true, true);
				viewer.getControl().getParent().redraw();
			} catch (IOException | UnauthorizedException e) {
				ContrastUIActivator.log(e);
				statusLabel.setText("Server error: " + e.getMessage());
				viewer.refresh();
				return;
			}

		}
	}

	private Traces getTraces(String orgUuid, Long serverId, String appId) throws IOException, UnauthorizedException {
		if (orgUuid == null) {
			return null;
		}
		if (appId == null) {
			appId = Constants.ALL_APPLICATIONS;
		}
		if (serverId == 0 || serverId < Constants.ALL_SERVERS) {
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
		} else if (serverId != Constants.ALL_SERVERS && Constants.ALL_APPLICATIONS.equals(appId)) {
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
		super.dispose();
	}

	private void createApplicationCombo(Composite composite, String orgUuid) {
		applicationCombo = new ComboViewer(composite, SWT.READ_ONLY);
		applicationCombo.getControl().setFont(composite.getFont());
		applicationCombo.setLabelProvider(new ContrastLabelProvider());
		applicationCombo.setContentProvider(new ArrayContentProvider());
		List<ApplicationUIAdapter> contrastApplications = new ArrayList<>();
		int count = 0;
		if (orgUuid != null) {
			Applications applications = null;
			try {
				applications = sdk.getApplications(orgUuid);
			} catch (IOException | UnauthorizedException e) {
				ContrastUIActivator.log(e);
			}
			if (applications != null && applications.getApplications() != null
					&& applications.getApplications().size() > 0) {
				for (Application application : applications.getApplications()) {
					ApplicationUIAdapter app = new ApplicationUIAdapter(application, application.getName());
					contrastApplications.add(app);
					count++;
				}
			}
		}
		ApplicationUIAdapter allApplications = new ApplicationUIAdapter(null, "All Applications(" + count + ")");
		contrastApplications.add(allApplications);
		applicationCombo.setInput(contrastApplications);
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		String appId = prefs.get(Constants.APPLICATION_ID, Constants.ALL_APPLICATIONS);
		ApplicationUIAdapter selected = allApplications;
		for (ApplicationUIAdapter adapter : contrastApplications) {
			if (appId.equals(adapter.getId())) {
				selected = adapter;
				break;
			}
		}
		applicationCombo.setInput(contrastApplications);
		applicationCombo.setSelection(new StructuredSelection(selected));
		applicationCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshTraces();
			}
		});
	}

	private String getOrgUuid() {
		String orgUuid = null;
		try {
			orgUuid = Util.getDefaultOrganizationUuid();
		} catch (IOException | UnauthorizedException e) {
			ContrastUIActivator.log(e);
		}
		return orgUuid;
	}

	private void createServerCombo(Composite composite, String orgUuid) {
		serverCombo = new ComboViewer(composite, SWT.READ_ONLY);
		serverCombo.getControl().setFont(composite.getFont());
		serverCombo.setLabelProvider(new ContrastLabelProvider());
		serverCombo.setContentProvider(new ArrayContentProvider());
		List<ServerUIAdapter> contrastServers = new ArrayList<>();
		int count = 0;
		if (orgUuid != null) {
			Servers servers = null;
			try {
				servers = sdk.getServers(orgUuid, null);
			} catch (IOException | UnauthorizedException e) {
				ContrastUIActivator.log(e);
			}
			if (servers != null && servers.getServers() != null) {
				for (Server server : servers.getServers()) {
					ServerUIAdapter contrastServer = new ServerUIAdapter(server, server.getName());
					contrastServers.add(contrastServer);
					count++;
				}
			}
		}
		ServerUIAdapter allServers = new ServerUIAdapter(null, "All Servers(" + count + ")");
		contrastServers.add(allServers);
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		long serverId = prefs.getLong(Constants.SERVER_ID, Constants.ALL_SERVERS);
		ServerUIAdapter selected = allServers;
		for (ServerUIAdapter adapter : contrastServers) {
			if (serverId == adapter.getId()) {
				selected = adapter;
				break;
			}
		}
		serverCombo.setInput(contrastServers);
		serverCombo.setSelection(new StructuredSelection(selected));
		serverCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshTraces();
			}
		});
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
		//manager.add(openPreferencesPage);
		manager.add(refreshAction);
		//manager.add(new Separator());
		manager.add(saveFilterAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		//manager.add(openPreferencesPage);
		manager.add(refreshAction);
		manager.add(saveFilterAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		//manager.add(openPreferencesPage);
		manager.add(refreshAction);
		manager.add(saveFilterAction);
	}

	private void makeActions() {
		openPreferencesPage = new Action() {
			public void run() {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(getSite().getShell(),
						ContrastPreferencesPage.ID, null, null);
				dialog.open();
			}
		};
		openPreferencesPage.setText("Contrast Preferences Page");
		openPreferencesPage.setToolTipText("Open Contrast Preferences Page");
		openPreferencesPage.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEF_VIEW));
		refreshAction = new Action() {
			public void run() {
				refreshView();
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
		// TODO Auto-generated method stub

	}

	protected void saveFilter() {
		IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
		Long serverId = getSelectedServerId();
		prefs.putLong(Constants.SERVER_ID, serverId);
		String appId = getSelectedAppId();
		prefs.put(Constants.APPLICATION_ID, appId);
	}

	private String getSelectedAppId() {
		ISelection sel = applicationCombo.getSelection();
		if (sel instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof ApplicationUIAdapter) {
				return ((ApplicationUIAdapter) element).getId();
			}
		}
		return Constants.ALL_APPLICATIONS;
	}

	private Long getSelectedServerId() {
		ISelection sel = serverCombo.getSelection();
		if (sel instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) sel).getFirstElement();
			if (element instanceof ServerUIAdapter) {
				return ((ServerUIAdapter) element).getId();
			}
		}
		return Constants.ALL_SERVERS;
	}

	protected void refreshView() {
		refreshTraces();
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

}
