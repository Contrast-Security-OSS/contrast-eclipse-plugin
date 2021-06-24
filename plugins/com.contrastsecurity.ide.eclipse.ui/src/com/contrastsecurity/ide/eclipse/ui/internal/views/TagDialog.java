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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.contrastsecurity.ide.eclipse.ui.ContrastUIActivator;
import com.contrastsecurity.ide.eclipse.ui.internal.model.TagLabelProvider;
import com.contrastsecurity.ide.eclipse.ui.util.UIElementUtils;
import com.contrastsecurity.models.TagsResponse;

public class TagDialog extends Dialog {

	private ComboViewer tagsComboViewer;
	private Text createTagText;
	private TableViewer tableViewer;
	private TagsResponse traceTagsResource;
	private TagsResponse orgTagsResource;

	private List<String> newTraceTags = null;

	static ResourceBundle resource = ResourceBundle.getBundle("OSGI-INF/l10n.bundle");

	private ISelectionChangedListener tagsComboViewerListener = new ISelectionChangedListener() {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			String tag = null;
			ISelection sel = tagsComboViewer.getSelection();
			if (sel instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection) sel).getFirstElement();
				if (element instanceof String) {
					tag = (String) element;
				}
			}
			if (tag != null) {
				applyTag(tag);
			}
		}
	};

	public TagDialog(Shell parentShell, TagsResponse traceTagsResource, TagsResponse orgTagsResource) {
		super(parentShell);
		this.traceTagsResource = traceTagsResource;
		this.orgTagsResource = orgTagsResource;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Composite comboComposite = new Composite(container, SWT.NONE);
		comboComposite.setLayout(new GridLayout(2, false));
		UIElementUtils.createLabel(comboComposite, resource.getString("APPLY_EXISTING_TAG"));
		tagsComboViewer = UIElementUtils.createComboViewer(comboComposite);
		populateTagsComboViewer(tagsComboViewer, traceTagsResource, orgTagsResource);
		tagsComboViewer.addSelectionChangedListener(tagsComboViewerListener);
		tagsComboViewer.getCombo().addListener(SWT.MouseVerticalWheel, new Listener() {

			@Override
			public void handleEvent(Event arg0) {
				arg0.doit = false;

			}
		});

		Composite createTagComposite = new Composite(container, SWT.NONE);
		createTagComposite.setLayout(new GridLayout(3, false));
		UIElementUtils.createLabel(createTagComposite, resource.getString("CREATE_AND_APPLY_NEW_TAG"));
		createTagText = new Text(createTagComposite, SWT.BORDER);

		Button createTagButton = createButton(createTagComposite, resource.getString("CREATE_TAG"));

		createTagButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String newTag = createTagText.getText();
				applyTag(newTag);
			}
		});

		Composite appliedTagsComposite = new Composite(container, SWT.NONE);
		appliedTagsComposite.setLayout(new GridLayout(2, false));

		UIElementUtils.createLabel(appliedTagsComposite, resource.getString("APPLIED_TAGS"));
		tableViewer = createTableViewer(container);

		String[] traceTagsArray = traceTagsResource.getTags().toArray(new String[traceTagsResource.getTags().size()]);
		tableViewer.setInput(traceTagsArray);

		return container;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 370);
	}

	private Button createButton(Composite composite, String text) {
		Button button = new Button(composite, SWT.PUSH);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		button.setLayoutData(gd);
		button.setText(text);
		return button;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(resource.getString("TAG_VULNERABILITY"));
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		newTraceTags = Arrays.asList((String[]) tableViewer.getInput());
		super.okPressed();
	}

	private TableViewer createTableViewer(Composite composite) {
		TableViewer tableViewer = new TableViewer(composite,
				SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.setLabelProvider(new TagLabelProvider());

		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		TableLayout layout = new TableLayout();
		tableViewer.getTable().setLayout(layout);

		TableColumn tagColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		tagColumn.setText(resource.getString("TAG_VULNERABILITY"));
		tagColumn.setWidth(290);
		TableColumn removeColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		removeColumn.setText(resource.getString("REMOVE_TAG"));
		removeColumn.setWidth(70);

		tableViewer.getTable().addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {

			}

			@Override
			public void mouseDown(MouseEvent e) {
				onRemoveButtonClick(e.x, e.y);
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		tableViewer.getTable().addListener(SWT.PaintItem, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (event.index == 1) {
					Image tmpImage = null;
					try {
						tmpImage = ContrastUIActivator.getImage("/icons/remove.png");
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int tmpWidth = 0;
					int tmpHeight = 0;
					int tmpX = 0;
					int tmpY = 0;

					tmpWidth = tableViewer.getTable().getColumn(event.index).getWidth();
					tmpHeight = ((TableItem) event.item).getBounds().height;

					tmpX = tmpImage.getBounds().width;
					tmpX = (tmpWidth / 2 - tmpX / 2);
					tmpY = tmpImage.getBounds().height;
					tmpY = (tmpHeight / 2 - tmpY / 2);
					if (tmpX <= 0)
						tmpX = event.x;
					else
						tmpX += event.x;
					if (tmpY <= 0)
						tmpY = event.y;
					else
						tmpY += event.y;
					event.gc.drawImage(tmpImage, tmpX, tmpY);
				}
			}
		});
		return tableViewer;
	}

	private void onRemoveButtonClick(int xCoord, int yCoord) {
		ISelection sel = tableViewer.getSelection();

		if (sel instanceof IStructuredSelection && ((IStructuredSelection) sel).getFirstElement() instanceof String) {
			final String tag = (String) ((IStructuredSelection) sel).getFirstElement();

			ViewerCell cell = tableViewer.getCell(new Point(xCoord, yCoord));
			if (cell != null) {
				int columnIndex = cell.getColumnIndex();
				if (columnIndex == 1) {
					removeTag(tag);
				}

			}
		}
	}

	private void populateTagsComboViewer(ComboViewer comboViewer, TagsResponse traceTagsResource,
			TagsResponse orgTagsResource) {

		List<String> orgTags = orgTagsResource.getTags();
		List<String> traceTags = traceTagsResource.getTags();
		List<String> tagsToAdd = new ArrayList<>();

		for (String tag : orgTags) {
			if (!traceTags.contains(tag)) {
				tagsToAdd.add(tag);
			}
		}

		if (!tagsToAdd.isEmpty()) {
			Set<String> lastDetectedValues = new LinkedHashSet<>();
			lastDetectedValues.addAll(tagsToAdd);

			comboViewer.setInput(lastDetectedValues);
			comboViewer.setSelection(new StructuredSelection(tagsToAdd.get(0)));
		}
	}

	private void applyTag(String tag) {
		if (!tag.isEmpty()) {

			tagsComboViewer.removeSelectionChangedListener(tagsComboViewerListener);
			tagsComboViewer.remove(tag);
			tagsComboViewer.addSelectionChangedListener(tagsComboViewerListener);

			if (!createTagText.getText().isEmpty()) {
				createTagText.setText("");
			}

			String[] tagsArray = (String[]) tableViewer.getInput();

			String[] newTagsArray = Arrays.copyOf(tagsArray, tagsArray.length + 1);

			newTagsArray[newTagsArray.length - 1] = tag;
			tableViewer.setInput(newTagsArray);
		}
	}

	private void removeTag(String tag) {
		if (traceTagsResource.getTags().contains(tag) || orgTagsResource.getTags().contains(tag)) {
			tagsComboViewer.removeSelectionChangedListener(tagsComboViewerListener);
			tagsComboViewer.add(tag);
			tagsComboViewer.addSelectionChangedListener(tagsComboViewerListener);
		}

		String[] tagsArray = (String[]) tableViewer.getInput();

		String[] newData = (String[]) ArrayUtils.removeElement(tagsArray, tag);
		tableViewer.setInput(newData);
	}

	public List<String> getNewTraceTags() {
		return newTraceTags;
	}
}
