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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.http.RuleSeverity;
import com.contrastsecurity.http.TraceFilterForm;
import com.contrastsecurity.ide.eclipse.core.Constants;
import com.contrastsecurity.ide.eclipse.core.ContrastCoreActivator;
import com.contrastsecurity.ide.eclipse.core.Util;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ApplicationUIAdapter;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ContrastLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.internal.model.ServerUIAdapter;
import com.contrastsecurity.models.Application;
import com.contrastsecurity.models.Applications;
import com.contrastsecurity.models.Server;
import com.contrastsecurity.models.Servers;

public class FilterDialog extends Dialog {

	private int currentOffset = 0;
	private static final int PAGE_LIMIT = 20;
	private TraceFilterForm traceFilterForm;

	private ComboViewer serverCombo;
	private ComboViewer applicationCombo;
	private ComboViewer lastDetectedCombo;
	private DateTime dateTimeFrom;
	private DateTime dateTimeTo;

	private Servers servers;
	private Applications applications;

	private Button severityLevelNoteButton;
	private Button severityLevelMediumButton;
	private Button severityLevelCriticalButton;
	private Button severityLevelLowButton;
	private Button severityLevelHighButton;

	private Button statusAutoRemediatedButton;
	private Button statusNotAProblemButton;
	private Button statusFixedButton;
	private Button statusConfirmedButton;
	private Button statusRemediatedButton;
	private Button statusBeingTrackedButton;
	private Button statusSuspiciousButton;
	private Button statusReportedButton;
	private Button statusUntrackedButton;

	private IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();

	private ISelectionChangedListener serverComboBoxListener = new ISelectionChangedListener() {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {

			String orgUuid = ContrastCoreActivator.getSelectedOrganizationUuid();
			updateApplicationCombo(orgUuid, true, applications);
		}
	};

	private ISelectionChangedListener listener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {

			final String selection = (String) ((IStructuredSelection) lastDetectedCombo.getSelection())
					.getFirstElement();

			if (!selection.equals((Constants.LAST_DETECTED_CUSTOM))) {
				dateTimeFrom.setEnabled(false);
				dateTimeTo.setEnabled(false);
			}
			LocalDateTime localDateTime = LocalDateTime.now();
			switch (selection) {
			case Constants.LAST_DETECTED_ALL:
				prefs.remove(Constants.LAST_DETECTED_FROM);
				prefs.remove(Constants.LAST_DETECTED_TO);
				break;
			case Constants.LAST_DETECTED_HOUR:
				LocalDateTime localDateTimeMinusHour = localDateTime.minusHours(1);
				setDateTimeFromLocalDateTime(dateTimeFrom, localDateTimeMinusHour);
				break;
			case Constants.LAST_DETECTED_DAY:
				LocalDateTime localDateTimeMinusDay = localDateTime.minusDays(1);
				setDateTimeFromLocalDateTime(dateTimeFrom, localDateTimeMinusDay);
				break;
			case Constants.LAST_DETECTED_WEEK:
				LocalDateTime localDateTimeMinusWeek = localDateTime.minusWeeks(1);
				setDateTimeFromLocalDateTime(dateTimeFrom, localDateTimeMinusWeek);
				break;
			case Constants.LAST_DETECTED_MONTH:
				LocalDateTime localDateTimeMinusMonth = localDateTime.minusMonths(1);
				setDateTimeFromLocalDateTime(dateTimeFrom, localDateTimeMinusMonth);
				break;
			case Constants.LAST_DETECTED_YEAR:
				LocalDateTime localDateTimeMinusYear = localDateTime.minusYears(1);
				setDateTimeFromLocalDateTime(dateTimeFrom, localDateTimeMinusYear);
				break;
			case Constants.LAST_DETECTED_CUSTOM:
				dateTimeFrom.setEnabled(true);
				dateTimeTo.setEnabled(true);
				break;
			}
		}
	};

	private void setDateTimeFromLocalDateTime(DateTime dateTime, LocalDateTime localDateTime) {
		dateTime.setYear(localDateTime.getYear());
		dateTime.setDay(localDateTime.getDayOfMonth());
		dateTime.setMonth(localDateTime.getMonthValue() - 1);
		dateTime.setHours(localDateTime.getHour());
		dateTime.setMinutes(localDateTime.getMinute());
		dateTime.setSeconds(localDateTime.getSecond());
	}

	private void setDateTimeFromCalendar(DateTime dateTime, Calendar calendar) {
		dateTime.setYear(calendar.get(Calendar.YEAR));
		dateTime.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		dateTime.setMonth(calendar.get(Calendar.MONTH) - 1);
		dateTime.setHours(calendar.get(Calendar.HOUR_OF_DAY));
		dateTime.setMinutes(calendar.get(Calendar.MINUTE));
		dateTime.setSeconds(calendar.get(Calendar.SECOND));
	}

	public FilterDialog(Shell parentShell, Servers servers, Applications applications) {
		super(parentShell);

		this.servers = servers;
		this.applications = applications;
	}

	private String getOrgUuid() {
		String orgUuid = null;
		try {
			orgUuid = ContrastCoreActivator.getSelectedOrganizationUuid();
		} catch (Exception e) {
			ContrastUIActivator.log(e);
		}
		return orgUuid;
	}

	private void createLastDetectedCombo(Composite composite) {
		lastDetectedCombo = Util.createComboViewer(composite);

		Set<String> lastDetectedValues = new LinkedHashSet<>();
		lastDetectedValues.addAll(Arrays.asList(Constants.LAST_DETECTED_CONSTANTS));

		lastDetectedCombo.setInput(lastDetectedValues);
		lastDetectedCombo.setSelection(new StructuredSelection(Constants.LAST_DETECTED_ALL));
	}

	private ComboViewer createContrastComboViewer(Composite composite) {

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		comboViewer.getControl().setFont(composite.getFont());
		comboViewer.setLabelProvider(new ContrastLabelProvider());
		comboViewer.setContentProvider(new ArrayContentProvider());
		return comboViewer;
	}

	private void updateServerCombo(final String orgUuid, final boolean setSavedDefaults, Servers servers) {
		Set<ServerUIAdapter> contrastServers = new LinkedHashSet<>();
		int count = 0;
		if (orgUuid != null) {
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

		if (setSavedDefaults) {
			IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
			long serverId = prefs.getLong(Constants.SERVER_ID, Constants.ALL_SERVERS);
			ServerUIAdapter selected = allServers;
			for (ServerUIAdapter adapter : contrastServers) {
				if (serverId == adapter.getId()) {
					selected = adapter;
					break;
				}
			}
			serverCombo.setSelection(new StructuredSelection(selected));
		}
	}

	private void updateApplicationCombo(final String orgUuid, final boolean setSavedDefaults,
			Applications applications) {
		Set<ApplicationUIAdapter> contrastApplications = new LinkedHashSet<>();
		int count = 0;
		if (orgUuid != null) {

			Server server = null;
			ISelection sel = serverCombo.getSelection();
			if (sel instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) sel).getFirstElement();
				if (element instanceof ServerUIAdapter) {
					server = ((ServerUIAdapter) element).getServer();
				}
			}

			if (server == null) {
				if (applications != null && applications.getApplications() != null
						&& applications.getApplications().size() > 0) {
					for (Application application : applications.getApplications()) {
						ApplicationUIAdapter app = new ApplicationUIAdapter(application, application.getName());
						contrastApplications.add(app);
						count++;
						ContrastUIActivator.logInfo(application.getName());
					}
				}
			} else {
				List<Application> apps = server.getApplications();
				for (Application application : apps) {
					ApplicationUIAdapter app = new ApplicationUIAdapter(application, application.getName());
					contrastApplications.add(app);
					count++;
					ContrastUIActivator.logInfo(application.getName());
				}
			}
		}
		ApplicationUIAdapter allApplications = new ApplicationUIAdapter(null, "All Applications(" + count + ")");
		contrastApplications.add(allApplications);
		applicationCombo.setInput(contrastApplications);

		if (setSavedDefaults) {
			IEclipsePreferences prefs = ContrastCoreActivator.getPreferences();
			String appId = prefs.get(Constants.APPLICATION_ID, Constants.ALL_APPLICATIONS);
			ApplicationUIAdapter selected = allApplications;
			for (ApplicationUIAdapter adapter : contrastApplications) {
				if (appId.equals(adapter.getId())) {
					selected = adapter;
					break;
				}
			}
			applicationCombo.setSelection(new StructuredSelection(selected));
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite comboComposite = new Composite(container, SWT.NONE);
		comboComposite.setLayout(new GridLayout(2, false));

		Util.createLabel(comboComposite, "Server");
		String orgUuid = getOrgUuid();

		serverCombo = createContrastComboViewer(comboComposite);
		updateServerCombo(orgUuid, true, servers);

		serverCombo.addSelectionChangedListener(serverComboBoxListener);

		Util.createLabel(comboComposite, "Application");

		applicationCombo = createContrastComboViewer(comboComposite);
		updateApplicationCombo(orgUuid, true, applications);

		createSeverityLevelSection(container);

		createLastDetectedSection(container);

		createStatusSection(container);

		populateFiltersWithDataFromEclipsePreferences();

		return container;
	}

	private void createSeverityLevelSection(Composite container) {

		Composite severityCompositeContainer = new Composite(container, SWT.NONE);
		severityCompositeContainer.setLayout(new GridLayout(2, false));

		Util.createLabel(severityCompositeContainer, "Severity");

		Composite severityComposite = new Composite(severityCompositeContainer, SWT.NONE);
		severityComposite.setLayout(new GridLayout(3, false));

		severityLevelNoteButton = createCheckBoxButton(severityComposite, "Note");

		severityLevelMediumButton = createCheckBoxButton(severityComposite, "Medium");

		severityLevelCriticalButton = createCheckBoxButton(severityComposite, "Critical");

		severityLevelLowButton = createCheckBoxButton(severityComposite, "Low");

		severityLevelHighButton = createCheckBoxButton(severityComposite, "High");
	}

	private Button createCheckBoxButton(Composite composite, String text) {
		Button button = new Button(composite, SWT.CHECK);
		button.setText(text);
		return button;
	}

	private void createStatusSection(Composite container) {

		Composite statusCompositeContainer = new Composite(container, SWT.NONE);
		statusCompositeContainer.setLayout(new GridLayout(2, false));

		Util.createLabel(statusCompositeContainer, "Status");

		Composite statusComposite = new Composite(statusCompositeContainer, SWT.NONE);
		statusComposite.setLayout(new GridLayout(3, false));

		statusAutoRemediatedButton = createCheckBoxButton(statusComposite, "Auto-Remediated");

		statusNotAProblemButton = createCheckBoxButton(statusComposite, "Not a Problem");

		statusFixedButton = createCheckBoxButton(statusComposite, "Fixed");

		statusConfirmedButton = createCheckBoxButton(statusComposite, "Confirmed");

		statusRemediatedButton = createCheckBoxButton(statusComposite, "Remediated");

		statusBeingTrackedButton = createCheckBoxButton(statusComposite, "Being Tracked");

		statusSuspiciousButton = createCheckBoxButton(statusComposite, "Suspicious");

		statusReportedButton = createCheckBoxButton(statusComposite, "Reported");

		statusUntrackedButton = createCheckBoxButton(statusComposite, "Untracked");
	}

	private void createLastDetectedSection(Composite container) {
		Composite lastDetectedCompositeContainer = new Composite(container, SWT.NONE);
		lastDetectedCompositeContainer.setLayout(new GridLayout(3, false));

		Util.createLabel(lastDetectedCompositeContainer, "Last Detected");

		createLastDetectedCombo(lastDetectedCompositeContainer);

		Composite lastDetectedComposite = new Composite(lastDetectedCompositeContainer, SWT.NONE);
		lastDetectedComposite.setLayout(new GridLayout(2, false));

		Util.createLabel(lastDetectedComposite, "From");

		dateTimeFrom = new DateTime(lastDetectedComposite, SWT.DROP_DOWN);

		Util.createLabel(lastDetectedComposite, "To");

		dateTimeTo = new DateTime(lastDetectedComposite, SWT.DROP_DOWN);

		lastDetectedCombo.addSelectionChangedListener(listener);

		dateTimeFrom.setEnabled(false);
		dateTimeTo.setEnabled(false);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Filter");
	}

	private void saveFilter() {
		Long serverId = getSelectedServerId();
		prefs.putLong(Constants.SERVER_ID, serverId);
		String appId = getSelectedAppId();
		prefs.put(Constants.APPLICATION_ID, appId);

		prefs.putBoolean(Constants.SEVERITY_LEVEL_NOTE, severityLevelNoteButton.getSelection());
		prefs.putBoolean(Constants.SEVERITY_LEVEL_MEDIUM, severityLevelMediumButton.getSelection());
		prefs.putBoolean(Constants.SEVERITY_LEVEL_CRITICAL, severityLevelCriticalButton.getSelection());
		prefs.putBoolean(Constants.SEVERITY_LEVEL_LOW, severityLevelLowButton.getSelection());
		prefs.putBoolean(Constants.SEVERITY_LEVEL_HIGH, severityLevelHighButton.getSelection());

		prefs.putBoolean(Constants.STATUS_AUTO_REMEDIATED, statusAutoRemediatedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_NOT_A_PROBLEM, statusNotAProblemButton.getSelection());
		prefs.putBoolean(Constants.STATUS_FIXED, statusFixedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_CONFIRMED, statusConfirmedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_REMEDIATED, statusRemediatedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_BEING_TRACKED, statusBeingTrackedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_SUSPICIOUS, statusSuspiciousButton.getSelection());
		prefs.putBoolean(Constants.STATUS_REPORTED, statusReportedButton.getSelection());
		prefs.putBoolean(Constants.STATUS_UNTRACKED, statusUntrackedButton.getSelection());

		String lastDetected = (String) ((IStructuredSelection) lastDetectedCombo.getSelection()).getFirstElement();
		prefs.put(Constants.LAST_DETECTED, lastDetected);

		Calendar calendarFrom = new GregorianCalendar(dateTimeFrom.getYear(), dateTimeFrom.getMonth(),
				dateTimeFrom.getDay(), dateTimeFrom.getHours(), dateTimeFrom.getMinutes());
		Calendar calendarTo = new GregorianCalendar(dateTimeTo.getYear(), dateTimeTo.getMonth(), dateTimeTo.getDay(),
				dateTimeTo.getHours(), dateTimeTo.getMinutes());

		switch (lastDetected) {
		case Constants.LAST_DETECTED_ALL:
			prefs.remove(Constants.LAST_DETECTED_FROM);
			prefs.remove(Constants.LAST_DETECTED_TO);
			break;
		case Constants.LAST_DETECTED_CUSTOM:
			prefs.putLong(Constants.LAST_DETECTED_FROM, calendarFrom.getTimeInMillis());
			prefs.putLong(Constants.LAST_DETECTED_TO, calendarTo.getTimeInMillis());
			break;
		default:
			prefs.putLong(Constants.LAST_DETECTED_FROM, calendarFrom.getTimeInMillis());
			prefs.remove(Constants.LAST_DETECTED_TO);
			break;
		}
	}

	private void populateFiltersWithDataFromEclipsePreferences() {

		severityLevelNoteButton.setSelection(prefs.getBoolean(Constants.SEVERITY_LEVEL_NOTE, false));
		severityLevelMediumButton.setSelection(prefs.getBoolean(Constants.SEVERITY_LEVEL_MEDIUM, false));
		severityLevelCriticalButton.setSelection(prefs.getBoolean(Constants.SEVERITY_LEVEL_CRITICAL, false));
		severityLevelLowButton.setSelection(prefs.getBoolean(Constants.SEVERITY_LEVEL_LOW, false));
		severityLevelHighButton.setSelection(prefs.getBoolean(Constants.SEVERITY_LEVEL_HIGH, false));

		statusAutoRemediatedButton.setSelection(prefs.getBoolean(Constants.STATUS_AUTO_REMEDIATED, false));
		statusNotAProblemButton.setSelection(prefs.getBoolean(Constants.STATUS_NOT_A_PROBLEM, false));
		statusFixedButton.setSelection(prefs.getBoolean(Constants.STATUS_FIXED, false));
		statusConfirmedButton.setSelection(prefs.getBoolean(Constants.STATUS_CONFIRMED, false));
		statusRemediatedButton.setSelection(prefs.getBoolean(Constants.STATUS_REMEDIATED, false));
		statusBeingTrackedButton.setSelection(prefs.getBoolean(Constants.STATUS_BEING_TRACKED, false));
		statusSuspiciousButton.setSelection(prefs.getBoolean(Constants.STATUS_SUSPICIOUS, false));
		statusReportedButton.setSelection(prefs.getBoolean(Constants.STATUS_REPORTED, false));
		statusUntrackedButton.setSelection(prefs.getBoolean(Constants.STATUS_UNTRACKED, false));

		String lastDetected = prefs.get(Constants.LAST_DETECTED, "");

		if (!lastDetected.isEmpty()) {
			lastDetectedCombo.setSelection(new StructuredSelection(lastDetected));
			Long lastDetectedFrom = prefs.getLong(Constants.LAST_DETECTED_FROM, 0);
			Long lastDetectedTo = prefs.getLong(Constants.LAST_DETECTED_TO, 0);

			switch (lastDetected) {
			case Constants.LAST_DETECTED_ALL:
				break;
			case Constants.LAST_DETECTED_CUSTOM:

				if (lastDetectedFrom != 0) {
					Calendar calendarFrom = Calendar.getInstance();
					calendarFrom.setTimeInMillis(lastDetectedFrom);
					setDateTimeFromCalendar(dateTimeFrom, calendarFrom);
				}
				if (lastDetectedTo != 0) {

					Calendar calendarTo = Calendar.getInstance();
					calendarTo.setTimeInMillis(lastDetectedTo);
					setDateTimeFromCalendar(dateTimeTo, calendarTo);
				}
				break;
			default:
				if (lastDetectedFrom != 0) {
					Calendar calendarFrom = Calendar.getInstance();
					calendarFrom.setTimeInMillis(lastDetectedFrom);
					setDateTimeFromCalendar(dateTimeFrom, calendarFrom);
				}
				break;
			}
		}
	}

	private TraceFilterForm extractFiltersIntoTraceFilterForm() {

		EnumSet<RuleSeverity> severities = getSelectedSeverities();
		List<String> statuses = getSelectedStatuses();

		Long serverId = getSelectedServerId();
		String appId = getSelectedAppId();

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

		String lastDetected = (String) ((IStructuredSelection) lastDetectedCombo.getSelection()).getFirstElement();

		Calendar calendarFrom = new GregorianCalendar(dateTimeFrom.getYear(), dateTimeFrom.getMonth(),
				dateTimeFrom.getDay(), dateTimeFrom.getHours(), dateTimeFrom.getMinutes());

		Calendar calendarTo = new GregorianCalendar(dateTimeTo.getYear(), dateTimeTo.getMonth(), dateTimeTo.getDay(),
				dateTimeTo.getHours(), dateTimeTo.getMinutes());
		Date fromDate = new Date(calendarFrom.getTimeInMillis());
		Date toDate = new Date(calendarTo.getTimeInMillis());

		switch (lastDetected) {
		case Constants.LAST_DETECTED_ALL:
			break;
		case Constants.LAST_DETECTED_CUSTOM:
			form.setStartDate(fromDate);
			form.setEndDate(toDate);
			break;
		default:
			form.setStartDate(fromDate);
			break;
		}
		form.setOffset(currentOffset);

		return form;
	}

	private EnumSet<RuleSeverity> getSelectedSeverities() {

		EnumSet<RuleSeverity> severities = EnumSet.noneOf(RuleSeverity.class);
		if (severityLevelNoteButton.getSelection()) {
			severities.add(RuleSeverity.NOTE);
		}
		if (severityLevelLowButton.getSelection()) {
			severities.add(RuleSeverity.LOW);
		}
		if (severityLevelMediumButton.getSelection()) {
			severities.add(RuleSeverity.MEDIUM);
		}
		if (severityLevelHighButton.getSelection()) {
			severities.add(RuleSeverity.HIGH);
		}
		if (severityLevelCriticalButton.getSelection()) {
			severities.add(RuleSeverity.CRITICAL);
		}
		return severities;
	}

	private List<String> getSelectedStatuses() {
		List<String> statuses = new ArrayList<>();
		if (statusAutoRemediatedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_AUTO_REMEDIATED);
		}
		if (statusConfirmedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_CONFIRMED);
		}
		if (statusSuspiciousButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_SUSPICIOUS);
		}
		if (statusNotAProblemButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_NOT_A_PROBLEM_API_REQUEST_STRING);
		}
		if (statusRemediatedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_REMEDIATED);
		}
		if (statusReportedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_REPORTED);
		}
		if (statusFixedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_FIXED);
		}
		if (statusBeingTrackedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_BEING_TRACKED);
		}
		if (statusUntrackedButton.getSelection()) {
			statuses.add(Constants.VULNERABILITY_STATUS_UNTRACKED);
		}

		return statuses;
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

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		saveFilter();
		traceFilterForm = extractFiltersIntoTraceFilterForm();
		super.okPressed();
	}

	public TraceFilterForm getTraceFilterForm() {
		return traceFilterForm;
	}
}
