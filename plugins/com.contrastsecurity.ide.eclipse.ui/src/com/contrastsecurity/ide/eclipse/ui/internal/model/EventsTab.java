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
package com.contrastsecurity.ide.eclipse.ui.internal.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.contrastsecurity.ide.eclipse.core.extended.EventItem;
import com.contrastsecurity.ide.eclipse.core.extended.EventResource;
import com.contrastsecurity.ide.eclipse.core.extended.EventSummaryResource;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;

public class EventsTab extends AbstractTab {

	private EventSummaryResource eventSummary;
	private TreeViewer viewer;

	public EventsTab(Composite parent, int style) {
		super(parent, style);
		TreeColumnLayout layout = new TreeColumnLayout();
		getControl().setLayout(layout);
		viewer = new TreeViewer(getControl(), SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getTree().setLayoutData(gd);

		TreeViewerColumn typeColumn = new TreeViewerColumn(viewer, SWT.NONE);
		layout.setColumnData(typeColumn.getColumn(), new ColumnWeightData(400));

		viewer.setLabelProvider(new EventLabelProvider(viewer));
		viewer.setContentProvider(new EventContentProvider());
		viewer.getTree().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				ISelection sel = viewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) sel).getFirstElement();
					if (selected instanceof EventItem) {
						EventItem eventItem = (EventItem) selected;
						if (eventItem.isStacktrace()) {
							String str = eventItem.getValue();
							String typeName;
							int lineNumber;
							try {
								typeName = getTypeName(str);
								lineNumber = getLineNumber(str);
							} catch (CoreException e1) {
								ErrorDialog.openError(ContrastUIActivator.getActiveWorkbenchShell(), "Error",
										"Stacktrace Error", e1.getStatus());
								return;
							}
							Job search = new Job("Searching for code in workspace...") {
								@Override
								protected IStatus run(IProgressMonitor monitor) {
									Set<IType> result = null;
									try {
										result = findTypeInWorkspace(typeName);
										searchCompleted(result, typeName, lineNumber, null);
									} catch (CoreException e) {
										searchCompleted(null, typeName, lineNumber, e.getStatus());
									}
									return Status.OK_STATUS;
								}

							};
							search.schedule();
						}
					}
				}
			}
		});
	}

	private void searchCompleted(final Set<IType> set, final String typeName, final int lineNumber,
			final IStatus status) {
		UIJob job = new UIJob("Search complete") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (set == null || set.size() <= 0) {
					if (status == null) {
						MessageDialog.openInformation(ContrastUIActivator.getActiveWorkbenchShell(), "Information",
								"Source not found for " + typeName);
					} else {
						ContrastUIActivator.statusDialog("Source not found", status);
					}
				} else {
					processSearchResult(set, typeName, lineNumber);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	private void processSearchResult(Set<IType> set, String typeName, int lineNumber) {
		// FIXME multiple result set.size > 1
		if (set != null && set.size() >= 1) {
			IEditorPart editorPart;
			try {
				IType source = set.iterator().next();
				editorPart = EditorUtility.openInEditor(source);
			} catch (PartInitException e1) {
				ContrastUIActivator.statusDialog("Error", e1.getStatus());
				return;
			}
			if (editorPart != null) {
				try {
					if (editorPart instanceof ITextEditor && lineNumber >= 0) {
						ITextEditor textEditor = (ITextEditor) editorPart;
						IDocumentProvider provider = textEditor.getDocumentProvider();
						IEditorInput editorInput = editorPart.getEditorInput();
						provider.connect(editorInput);
						IDocument document = provider.getDocument(editorInput);
						try {
							IRegion line = document.getLineInformation(lineNumber == 0 ? 0: lineNumber - 1);
							textEditor.selectAndReveal(line.getOffset(), line.getLength());
						} catch (BadLocationException e) {
							MessageDialog.openInformation(ContrastUIActivator.getActiveWorkbenchShell(),
									"Invalid line number",
									(lineNumber + 1) + " is not valid line number in " + typeName);
						}
						provider.disconnect(editorInput);
					}
				} catch (CoreException e) {
					ContrastUIActivator.statusDialog(e.getStatus().getMessage(), e.getStatus());
				}
			}
		} else {
			MessageDialog.openInformation(ContrastUIActivator.getActiveWorkbenchShell(), "Information",
					"Source not found for " + typeName);
		}
	}

	private String getTypeName(String stacktrace) throws CoreException {
		int start = stacktrace.lastIndexOf('(');
		int end = stacktrace.indexOf(':');
		if (start >= 0 && end > start) {
			String typeName = stacktrace.substring(start + 1, end);
			typeName = JavaCore.removeJavaLikeExtension(typeName);
			String qualifier = stacktrace.substring(0, start);
			start = qualifier.lastIndexOf('.');
			if (start >= 0) {
				start = new String((String) qualifier.subSequence(0, start)).lastIndexOf('.');
				if (start == -1) {
					start = 0;
				}
			}
			if (start >= 0) {
				qualifier = qualifier.substring(0, start);
			}
			if (qualifier.length() > 0) {
				typeName = qualifier + "." + typeName;
			}
			return typeName;
		}
		IStatus status = new Status(IStatus.ERROR, ContrastUIActivator.PLUGIN_ID, 0,
				"Unable to parse type name from stacktrace", null);
		throw new CoreException(status);
	}

	private int getLineNumber(String stacktrace) throws CoreException {
		int index = stacktrace.lastIndexOf(':');
		if (index >= 0) {
			String numText = stacktrace.substring(index + 1);
			index = numText.indexOf(')');
			if (index >= 0) {
				numText = numText.substring(0, index);
			}
			try {
				return Integer.parseInt(numText);
			} catch (NumberFormatException e) {
				IStatus status = new Status(IStatus.ERROR, ContrastUIActivator.PLUGIN_ID, 0,
						"Unable to parse line number from stacktrace", e);
				throw new CoreException(status);
			}
		}
		IStatus status = new Status(IStatus.ERROR, ContrastUIActivator.PLUGIN_ID, 0,
				"Unable to parse line number from stacktrace", null);
		throw new CoreException(status);
	}

	private static Set<IType> findTypeInWorkspace(String typeName) throws CoreException {
		int dot = typeName.lastIndexOf('.');
		char[][] qualifications;
		String simpleName;
		if (dot != -1) {
			qualifications = new char[][] { typeName.substring(0, dot).toCharArray() };
			simpleName = typeName.substring(dot + 1);
		} else {
			qualifications = null;
			simpleName = typeName;
		}
		char[][] typeNames = new char[][] { simpleName.toCharArray() };

		class ResultException extends RuntimeException {
			private static final long serialVersionUID = 1L;
			private final IType fType;

			public ResultException(IType type) {
				fType = type;
			}
		}
		TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
			@Override
			public void acceptTypeNameMatch(TypeNameMatch match) {
				throw new ResultException(match.getType());
			}
		};
		Set<IType> set = new HashSet<>();
		try {
			new SearchEngine().searchAllTypeNames(qualifications, typeNames, SearchEngine.createWorkspaceScope(),
					requestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		} catch (ResultException e) {
			set.add(e.fType);
		}
		return set;
	}

	public void setEventSummary(EventSummaryResource eventSummary) {
		this.eventSummary = eventSummary;
		if (eventSummary != null) {
			viewer.setInput(eventSummary.getEvents().toArray(new EventResource[0]));
		} else {
			viewer.setInput(new EventResource[0]);
		}
		getControl().getParent().layout(true, true);
		getControl().redraw();
	}

	public EventSummaryResource getEventSummary() {
		return eventSummary;
	}

}
