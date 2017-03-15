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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.contrastsecurity.exceptions.UnauthorizedException;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ApplicationUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ContrastLabelProvider;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;
import com.contrastsecurity.models.Trace;
import com.contrastsecurity.models.Traces;
import com.contrastsecurity.sdk.ContrastSDK;

/**
 * Mockup View
 */

public class VulnerabilitiesView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.contrastsecurity.ide.eclipse.ui.views.VulnerabilitiesView";

	private TableViewer viewer;
	private Action saveFilterAction;
	private Action refreshAction;
	private Action doubleClickAction;

	private ComboViewer serverCombo;

	private ComboViewer applicationCombo;

	private String appId;

	private Image eyeImage;

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
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		Composite comboComposite = new Composite(composite, SWT.NONE);
		comboComposite.setLayout(new GridLayout(3, false));
		Label label = new Label(comboComposite, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		label.setLayoutData(gd);
		label.setText("Show Vulnerabilities:");
		String orgUuid = getUuid();
		createServerCombo(comboComposite, orgUuid);
		createApplicationCombo(comboComposite, orgUuid);
		viewer = new TableViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTable().setLayoutData(gd);
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(500);
		column.getColumn().setText("Title");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof Trace) {
					return ((Trace)element).getTitle();
				}
				return super.getText(element);
			}
			
			public Image getImage(Object obj) {
				if (obj instanceof Trace) {
					Trace trace = (Trace) obj;
					return getImageInternal(trace.getSeverity());
				}
				return null;
			}
			
			private Image getImageInternal(String severity) {
				switch (severity) {
				case "Note":
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
				case "High":
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				case "Medium":
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				case "Low":
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_CLEAR);
				}
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}
		});
		column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(50);
		column.getColumn().setText("Action");
		column.getColumn().setMoveable(true);
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof Trace) {
					return getEyeImage();
				}
				return super.getImage(element);
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		ContrastSDK sdk = ContrastCoreActivator.getContrastSDK();
		// FIXME appId
		if (orgUuid != null && appId != null) {
			try {
				Traces traces = sdk.getTraces(orgUuid, appId, null);
				if (traces != null && traces.getTraces() != null) {
					Trace[] traceArray = traces.getTraces().toArray(new Trace[0]);
					viewer.setInput(traceArray);
				}
			} catch (IOException | UnauthorizedException e) {
				ContrastUIActivator.log(e);
			}

		} else {
			viewer.setInput(new Trace[0]);
		}
		//viewer.setLabelProvider(new TraceLabelProvider());
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	protected Image getEyeImage() {
		if (eyeImage == null) {
			eyeImage = ContrastUIActivator.getImageDescriptor("/icons/eye.png").createImage();
		}
		return eyeImage;
	}

	@Override
	public void dispose() {
		if (eyeImage != null) {
			eyeImage.dispose();
		}
		super.dispose();
	}

	private void createApplicationCombo(Composite composite, String orgUuid) {
		applicationCombo = new ComboViewer(composite, SWT.READ_ONLY);
		applicationCombo.getControl().setFont(composite.getFont());
		applicationCombo.setLabelProvider(new ContrastLabelProvider());
		applicationCombo.setContentProvider(new ArrayContentProvider());
		ContrastSDK sdk = ContrastCoreActivator.getContrastSDK();
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
				appId = applications.getApplications().get(0).getId();
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
		applicationCombo.setSelection(new StructuredSelection(allApplications));
	}

	private String getUuid() {
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
		ContrastSDK sdk = ContrastCoreActivator.getContrastSDK();
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
		serverCombo.setInput(contrastServers);
		serverCombo.setSelection(new StructuredSelection(allServers));
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
		manager.add(refreshAction);
		manager.add(new Separator());
		manager.add(saveFilterAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(saveFilterAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(saveFilterAction);
	}

	private void makeActions() {
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
		// TODO Auto-generated method stub

	}

	protected void refreshView() {
		// TODO Auto-generated method stub

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
