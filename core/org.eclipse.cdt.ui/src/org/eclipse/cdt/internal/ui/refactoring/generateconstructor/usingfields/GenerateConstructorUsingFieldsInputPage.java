/*******************************************************************************
 * Copyright (c) 2010, 2019 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class GenerateConstructorUsingFieldsInputPage extends UserInputWizardPage {

	private ContainerCheckedTreeViewer variableSelectionView;
	private GenerateConstructorUsingFieldsContext context;
	private GenerateConstructorLabelProvider labelProvider;

	public GenerateConstructorUsingFieldsInputPage(GenerateConstructorUsingFieldsContext context) {
		super(Messages.GenerateConstructorUsingFields_Name);
		this.context = context;
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.GenerateConstructorUsingFields_Name);
		setMessage(Messages.GenerateConstructorUsingFieldsInputPage_header);

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));

		createFieldsManagementComposite(comp);

		createOptionComposite(comp);

		setControl(comp);
		comp.pack();
	}

	private void createFieldsManagementComposite(Composite comp) {
		Composite fieldsManagementComp = new Composite(comp, SWT.NONE);
		fieldsManagementComp.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		fieldsManagementComp.setLayoutData(gd);

		createFieldsManagementTree(fieldsManagementComp);
		gd = new GridData(GridData.FILL_BOTH);
		variableSelectionView.getTree().setLayoutData(gd);

		Composite btComp = createFieldManagementButtonsComposite(fieldsManagementComp);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		btComp.setLayoutData(gd);
	}

	private void createFieldsManagementTree(Composite comp) {
		variableSelectionView = new ContainerCheckedTreeViewer(comp, SWT.BORDER);
		variableSelectionView.setContentProvider(context);
		labelProvider = new GenerateConstructorLabelProvider();
		variableSelectionView.setLabelProvider(labelProvider);

		variableSelectionView.setInput(""); //$NON-NLS-1$

		for (GenerateConstructorInsertEditProvider obj : context.getSelectedFieldsInOrder()) {
			variableSelectionView.setSubtreeChecked(obj, true);
		}

		variableSelectionView.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getElement() instanceof GenerateConstructorInsertEditProvider) {
					GenerateConstructorInsertEditProvider editProvider = (GenerateConstructorInsertEditProvider) event
							.getElement();
					editProvider.setSelected(event.getChecked());
				}
			}
		});
	}

	private Composite createFieldManagementButtonsComposite(Composite comp) {
		Composite btComp = new Composite(comp, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 4;
		btComp.setLayout(layout);

		final Button selectAll = new Button(btComp, SWT.PUSH);
		selectAll.setText(Messages.GenerateConstructorUsingFieldsInputPage_SelectAll);
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = context.getElements(null);
				for (Object treeItem : items) {
					variableSelectionView.setChecked(treeItem, true);
					if (treeItem instanceof GenerateConstructorInsertEditProvider) {
						GenerateConstructorInsertEditProvider generateConstructorInsertEditProvider = (GenerateConstructorInsertEditProvider) treeItem;
						generateConstructorInsertEditProvider.setSelected(true);
					}
				}
			}
		});

		final Button deselectAll = new Button(btComp, SWT.PUSH);
		deselectAll.setText(Messages.GenerateConstructorUsingFieldsInputPage_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = context.getElements(null);
				for (Object treeItem : items) {
					variableSelectionView.setChecked(treeItem, false);
					if (treeItem instanceof GenerateConstructorInsertEditProvider) {
						GenerateConstructorInsertEditProvider generateConstructorInsertEditProvider = (GenerateConstructorInsertEditProvider) treeItem;
						generateConstructorInsertEditProvider.setSelected(false);
					}
				}
			}
		});

		return btComp;
	}

	private void createOptionComposite(Composite comp) {
		Composite optionComposite = new Composite(comp, NONE);
		optionComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		optionComposite.setLayoutData(gd);

		final Button definitionSeparate = new Button(optionComposite, SWT.CHECK);
		definitionSeparate.setText(Messages.GenerateConstructorUsingFieldsInputPage_SeparateDefinition);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		definitionSeparate.setLayoutData(gd);
		definitionSeparate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				context.setDefinitionSeparate(definitionSeparate.getSelection());
			}
		});

		optionComposite.pack();
	}
}
