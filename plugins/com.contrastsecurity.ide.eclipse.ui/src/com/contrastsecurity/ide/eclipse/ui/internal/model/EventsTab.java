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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
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
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.contrastsecurity.models.EventItem;
import com.contrastsecurity.models.EventResource;
import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.models.EventSummaryResponse;

public class EventsTab extends AbstractTab {

	private EventSummaryResponse eventSummary;
	private TreeViewer viewer;
	ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

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
							final String str = eventItem.getValue();
							final String typeName;
							final int lineNumber;
							try {
								typeName = getTypeName(str);
								lineNumber = getLineNumber(str);
							} catch (CoreException e1) {
								ErrorDialog.openError(ContrastUIActivator.getActiveWorkbenchShell(), resource.getString("ERROR"),
										resource.getString("STACKTRACE_ERROR"), e1.getStatus());
								return;
							}
							Job search = new Job(resource.getString("SEARCHING_FOR_CODE")) {
								@Override
								protected IStatus run(IProgressMonitor monitor) {
									Set<IType> result = null;
									try {
										if (str.contains(".java")) {
											result = findTypeInWorkspace(typeName);
											searchCompleted(result, typeName, lineNumber, null);
										} else {
											List<IFile> resultFile = findFileInWorkspace(typeName);
											searchCompleted(resultFile, typeName, lineNumber, null);
										}
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

	private IType getTypeFromActiveProject(Set<IType> inputSet) {

		IProject iProject = getIProjectFromActiveEditor();
		if (iProject != null) {
			for (IType file : inputSet) {
				if (file.getResource().getProject().equals(iProject)) {
					return file;
				}
			}
		}
		return null;
	}

	private IProject getIProjectFromActiveEditor() {
		IEditorPart ieditorpart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (ieditorpart != null) {
			IEditorInput input = ieditorpart.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IResource iResource = ((IFileEditorInput) input).getFile();
				return iResource.getProject();
			}
		}
		return null;
	}

	private void searchCompleted(Object result, final String typeName, final int lineNumber, final IStatus status) {
		UIJob job = new UIJob(resource.getString("SEARCH_COMPLETE")) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!validateSearchResult(result)) {
					if (status == null) {
						MessageDialog.openInformation(ContrastUIActivator.getActiveWorkbenchShell(), "Information",
								resource.getString("SOURCE_NOT_FOUND_FOR") + typeName);
					} else {
						ContrastUIActivator.statusDialog(resource.getString("SOURCE_NOT_FOUND"), status);
					}
				} else {
					processSearchResult(result, typeName, lineNumber);
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	@SuppressWarnings("unchecked")
	private void processSearchResult(Object result, String typeName, int lineNumber) {
		if (result instanceof Set<?>) {
			IEditorPart editorPart = null;

			IType iType = getTypeFromActiveProject((Set<IType>) result);

			for (IType file : (Set<IType>) result) {
				try {
					if (!file.equals(iType)) {
						editorPart = EditorUtility.openInEditor(file, true);
					}
				} catch (PartInitException e1) {
					ContrastUIActivator.statusDialog(resource.getString("ERROR"), e1.getStatus());
					return;
				}
				if (editorPart != null)
					openEditor(editorPart, lineNumber, typeName);
			}

			if (iType != null) {
				try {
					editorPart = EditorUtility.openInEditor(iType, true);
				} catch (PartInitException e1) {
					ContrastUIActivator.statusDialog(resource.getString("ERROR"), e1.getStatus());
					return;
				}
				if (editorPart != null)
					openEditor(editorPart, lineNumber, typeName);
			}

		} else {
			List<IFile> matches = (List<IFile>) result;
			for (IFile file : matches) {
				IEditorPart editorPart;
				try {
					editorPart = EditorUtility.openInEditor(file, true);
				} catch (PartInitException e1) {
					ContrastUIActivator.statusDialog(resource.getString("ERROR"), e1.getStatus());
					return;
				}
				if (editorPart != null)
					openEditor(editorPart, lineNumber, typeName);
			}
		}
	}

	private void openEditor(IEditorPart editorPart, final int lineNumber, final String typeName) {
		if (editorPart != null) {
			try {
				if (editorPart instanceof ITextEditor && lineNumber >= 0) {
					ITextEditor textEditor = (ITextEditor) editorPart;
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IEditorInput editorInput = editorPart.getEditorInput();
					provider.connect(editorInput);
					IDocument document = provider.getDocument(editorInput);
					try {
						IRegion line = document.getLineInformation(lineNumber == 0 ? 0 : lineNumber - 1);
						textEditor.selectAndReveal(line.getOffset(), line.getLength());
					} catch (BadLocationException e) {
						MessageDialog.openInformation(ContrastUIActivator.getActiveWorkbenchShell(),
								resource.getString("INVALID_LINE"), (lineNumber + 1) + resource.getString("INVALID_LINE_FILE") + typeName);
					}
					provider.disconnect(editorInput);
				}
			} catch (CoreException e) {
				ContrastUIActivator.statusDialog(e.getStatus().getMessage(), e.getStatus());
			}
		}
	}

	private boolean validateSearchResult(Object result) {
		if (result instanceof Set<?>)
			return (result != null && ((Set<?>) result).size() > 0);
		else
			return (result != null && ((List<?>) result).size() > 0);
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
				resource.getString("UNABLE_TO_PARSE"), null);
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
					resource.getString("UNABLE_TO_PARSE"), e);
				throw new CoreException(status);
			}
		}
		IStatus status = new Status(IStatus.ERROR, ContrastUIActivator.PLUGIN_ID, 0,
			resource.getString("UNABLE_TO_PARSE"), null);
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

		ContrastTypeNameMatchRequestor contrastTypeNameMatchRequestor = new ContrastTypeNameMatchRequestor();

		new SearchEngine().searchAllTypeNames(qualifications, typeNames, SearchEngine.createWorkspaceScope(),
				contrastTypeNameMatchRequestor, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);

		return contrastTypeNameMatchRequestor.getTypeNameMatches();
	}

	/**
	 * Searches for file with a given name.
	 *
	 * @param filename
	 *            The file name with extension included.
	 * @return IFile object or null if the file is not found.
	 * @throws CoreException
	 *             If the request fails.
	 */
	private static List<IFile> findFileInWorkspace(String filename) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		String[] name = StringUtils.split(filename, "(");
		List<IFile> matches = new ArrayList<>();

		for (IProject project : root.getProjects())
			findFile(name[name.length - 1], project, matches);

		return matches;
	}

	/**
	 * Recursively searches for a file with a given name.
	 *
	 * @param filename
	 *            The file name with extension.
	 * @param resource
	 *            The resource to evaluate with the file name. Allowed types:
	 *            IProject, IFolder and IFile.
	 * @return An IFile object or null if the file is not found.
	 * @throws CoreException
	 *             If the request fails.
	 */
	private static List<IFile> findFile(final String filename, IResource resource, List<IFile> matches)
			throws CoreException {
		if (resource instanceof IFile) {
			IFile file = (IFile) resource;
			if (StringUtils.equals(filename, file.getName()))
				matches.add(file);

			return matches;
		} else if (resource instanceof IFolder && !((IFolder) resource).getName().equals("target")) {
			IFolder folder = (IFolder) resource;

			for (IResource res : folder.members())
				findFile(filename, res, matches);
		} else if (resource instanceof IProject && ((IProject) resource).isOpen()) {
			IProject project = (IProject) resource;

			for (IResource res : project.members())
				findFile(filename, res, matches);
		}

		return matches;
	}

	public void setEventSummary(EventSummaryResponse eventSummary) {
		this.eventSummary = eventSummary;
		if (eventSummary != null) {
			viewer.setInput(eventSummary.getEvents().toArray(new EventResource[0]));
		} else {
			viewer.setInput(new EventResource[0]);
		}
		getControl().getParent().layout(true, true);
		getControl().redraw();
	}

	public EventSummaryResponse getEventSummary() {
		return eventSummary;
	}

}
