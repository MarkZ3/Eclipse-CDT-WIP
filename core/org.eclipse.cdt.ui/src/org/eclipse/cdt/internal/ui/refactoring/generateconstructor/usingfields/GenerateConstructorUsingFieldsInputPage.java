/*******************************************************************************
 * Copyright (c) 2010, 2013 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class GenerateConstructorUsingFieldsInputPage extends UserInputWizardPage {

	private ContainerCheckedTreeViewer variableSelectionView;
	private GenerateConstructorUsingFieldsContext context;
	private GenerateConstructorLabelProvider labelProvider;
	private Button btnDown;
	private Button btnUp;
	private Button radioAssignMem;
	private Button radioInitializerList;
	private Button initializeMem;
	private Button initializeOtherMembers;
	private TableColumn baseNameColumn;
	private Table table;
	private TableColumn baseConstructorColumn;
	
	public GenerateConstructorUsingFieldsInputPage(GenerateConstructorUsingFieldsContext context) {
		super(Messages.GenerateConstructorUsingFields_Name);
		this.context = context;
	}

	public GenerateConstructorUsingFieldsInputPage(String name, Button btnDown) {
		super(name);
		this.btnDown = btnDown;
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.GenerateConstructorUsingFields_Name);
		setMessage(Messages.GenerateConstructorUsingFieldsInputPage_header);
		
		Composite comp = new Composite(parent, SWT.NONE );
		comp.setLayout(new GridLayout(1, false));
		
		createFieldsManagementComposite(comp);
		
		createOptionComposite(comp);
		
		setControl(comp);
		comp.pack();
		comp.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				baseConstructorColumn.setWidth(table.getSize().x - baseNameColumn.getWidth() - 5);
			}
		});
		baseConstructorColumn.setWidth(table.getSize().x - baseNameColumn.getWidth());
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
		
		
		/*if (context.selectedName != null) {
			String rawSignature = context.selectedName.getRawSignature();
			for (Object obj : variableSelectionView.getVisibleExpandedElements()) {
				if (obj instanceof FieldWrapper) {
					if (obj.toString().contains(rawSignature)) {
						variableSelectionView.setSubtreeChecked(obj, true);
					}
				}
			}
		}*/
		/*Set<GenerateConstructorInsertEditProvider> checkedFunctions = context.selectedFunctions;
		for (Object currentElement : variableSelectionView.getCheckedElements()) {
			if (currentElement instanceof GenerateConstructorInsertEditProvider) {
				GenerateConstructorInsertEditProvider editProvider = (GenerateConstructorInsertEditProvider) currentElement;
				checkedFunctions.add(editProvider);
			}
		}*/
		variableSelectionView.addCheckStateListener(new ICheckStateListener() {
	
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				ArrayList<GenerateConstructorInsertEditProvider> selectedFields = context.selectedFields;
				for (Object currentElement : variableSelectionView.getCheckedElements()) {
					if (currentElement instanceof GenerateConstructorInsertEditProvider) {
						if(!selectedFields.contains(currentElement)) {
							GenerateConstructorInsertEditProvider editProvider = (GenerateConstructorInsertEditProvider) currentElement;
							selectedFields.add(editProvider);
						}
					}
				}
			}
		});
		
		variableSelectionView.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if(selection != null && selection instanceof StructuredSelection) {
					Object object = ((StructuredSelection)selection).getFirstElement();
					if(object instanceof GenerateConstructorInsertEditProvider) {
						enableDisableUpDown((GenerateConstructorInsertEditProvider)object);
					}
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
		selectAll.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = context.getElements(null);
				for (Object treeItem : items) {
					variableSelectionView.setChecked(treeItem, true);
				}
				context.selectedFields.clear();
				context.selectedFields.addAll(context.existingFields);
			}
		});
		
		final Button deselectAll = new Button(btComp, SWT.PUSH);
		deselectAll.setText(Messages.GenerateConstructorUsingFieldsInputPage_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = context.getElements(null);
				for (Object treeItem : items) {
					variableSelectionView.setChecked(treeItem, false);
				}
				context.selectedFields.clear();
			}
		});
		
		btnUp = new Button(btComp, SWT.PUSH);
		btnUp.setText(Messages.GenerateConstructorUsingFieldsInputPage_Up);
		btnUp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = variableSelectionView.getSelection();
				if(selection != null && selection instanceof StructuredSelection) {
					Object object = ((StructuredSelection)selection).getFirstElement();
					if(object instanceof GenerateConstructorInsertEditProvider) {
						moveUpFieldInTree((GenerateConstructorInsertEditProvider)object);
					}
				}
			}
		});
		
		btnDown = new Button(btComp, SWT.PUSH);
		btnDown.setText(Messages.GenerateConstructorUsingFieldsInputPage_Down);
		btnDown.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = variableSelectionView.getSelection();
				if(selection != null && selection instanceof StructuredSelection) {
					Object object = ((StructuredSelection)selection).getFirstElement();
					if(object instanceof GenerateConstructorInsertEditProvider) {
						moveDownFieldInTree((GenerateConstructorInsertEditProvider)object);
					}
				}
			}
		});
		
		Button btnReset = new Button(btComp, SWT.PUSH);
		btnReset.setText(Messages.GenerateConstructorUsingFieldsInputPage_Reset);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				resetTreeFieldsOrder();
			}
		});
		
		if(context.existingFields.size() == 1) {
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
			btnReset.setEnabled(false);
		}
		
		return btComp;
	}

	private void createOptionComposite(Composite comp) {
		Composite optionComposite = new Composite(comp, NONE);
		optionComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		optionComposite.setLayoutData(gd);
	
		Label lblConstructorCalls = new Label(optionComposite, SWT.NONE);
		
		//TODO: use a composite for constructors
		lblConstructorCalls.setText(Messages.GenerateConstructorUsingFieldsInputPage_ConstructorCalls);
		
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		table = new Table (optionComposite, SWT.BORDER);
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		baseNameColumn = new TableColumn(table, SWT.NONE);
		baseNameColumn.setText("Class");
		baseNameColumn.setWidth(100);
		
		baseConstructorColumn = new TableColumn(table, SWT.NONE);
		baseConstructorColumn.setText(Messages.GenerateConstructorUsingFieldsInputPage_ConstructorToCall);
		
		for (final ICPPASTBaseSpecifier baseSpecifier : context.baseClasses) {
			TableItem item = new TableItem (table, SWT.NONE);
			item.setData(baseSpecifier);
			item.setText(0, baseSpecifier.getName().toString());
			
			TableEditor editor = new TableEditor(table);
			final Combo combo = new Combo(table, SWT.NONE);
			combo.add("None");
			combo.setData("None", null);
			combo.select(0);
			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ICPPConstructor selectedConstructor = (ICPPConstructor) combo.getData(combo.getItem(combo.getSelectionIndex()));
					context.selectedbaseClassesConstrutors.put(baseSpecifier, selectedConstructor);
				}
			});
			ArrayList<ICPPConstructor> constructors = context.baseClassesConstrutors.get(baseSpecifier);
			for(int i = 0; i < constructors.size(); i++) {
				ICPPConstructor constructor = constructors.get(i);
				String constructorString = constructor.toString();
				combo.add(constructorString);
				combo.setData(constructorString, constructor);
				if(constructorString.endsWith("()")) { //$NON-NLS-1$
					combo.select(i + 1);
					context.selectedbaseClassesConstrutors.put(baseSpecifier, constructor);
				}
			}
			combo.pack();
			editor.grabHorizontal = true;
			editor.grabVertical = true;
			editor.minimumHeight = combo.getSize().y;
			editor.minimumWidth = combo.getSize().x;
			editor.setEditor(combo, item, 1);
		}
		
		// TODO: Do this first when refactored with extract method
		if(context.baseClasses.isEmpty()) {
			table.setEnabled(false);
		}
		
		final Button placeImplemetation = new Button(optionComposite, SWT.CHECK);
		placeImplemetation.setText(Messages.GenerateConstructorUsingFieldsInputPage_PlaceImplHeader);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		placeImplemetation.setLayoutData(gd);
		placeImplemetation.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(placeImplemetation.getSelection()) {
					context.setImplementationInHeader(true);
				} else {
					context.setImplementationInHeader(false);
				}
			}
		});
		
		createConstructorDeclarationGroup(optionComposite);
	
		createConstructorDefinitionGroup(optionComposite);
	
		optionComposite.pack();
		baseConstructorColumn.setResizable(true);
		baseConstructorColumn.setWidth(table.getSize().x - baseNameColumn.getWidth());
	}

	private void createConstructorDeclarationGroup(Composite optionComposite) {
		Group constructorDeclarationGroup = new Group(optionComposite, SWT.NONE);
		constructorDeclarationGroup.setText(Messages.GenerateConstructorUsingFieldsInputPage_ConstructorDeclaration);
		GridData gd = new GridData(GridData.FILL_BOTH);
		constructorDeclarationGroup.setLayoutData(gd);
		constructorDeclarationGroup.setLayout(new GridLayout(1, false));
		
		final Button constObjectsParams = new Button(constructorDeclarationGroup, SWT.CHECK);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		constObjectsParams.setText(Messages.GenerateConstructorUsingFieldsInputPage_AddConst);
		constObjectsParams.setLayoutData(gd);
		constObjectsParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(constObjectsParams.getSelection()) {
					context.addConstToObjects = true;
				} else {
					context.addConstToObjects = false;
				}
			}
		});
		constObjectsParams.setSelection(true);
		context.addConstToObjects = true;
		
		final Button refObjectsParams = new Button(constructorDeclarationGroup, SWT.CHECK);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		refObjectsParams.setText(Messages.GenerateConstructorUsingFieldsInputPage_PassByReference);
		refObjectsParams.setLayoutData(gd);
		refObjectsParams.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(refObjectsParams.getSelection()) {
					context.passObjectsByReference = true;
				} else {
					context.passObjectsByReference = false;
				}
			}
		});
		refObjectsParams.setSelection(true);
		context.passObjectsByReference = true;
	}

	private void createConstructorDefinitionGroup(Composite optionComposite) {
		GridData gd;
		Group constructorDefinitionGroup = new Group(optionComposite, SWT.NONE);
		constructorDefinitionGroup.setText(Messages.GenerateConstructorUsingFieldsInputPage_ConstructorDefinition);
		gd = new GridData(GridData.FILL_BOTH);
		constructorDefinitionGroup.setLayoutData(gd);
		constructorDefinitionGroup.setLayout(new GridLayout(1, false));
		
		Composite initializeMemComp = new Composite(constructorDefinitionGroup, SWT.NONE);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		initializeMemComp.setLayoutData(gd);
		initializeMemComp.setLayout(new GridLayout(1, false));
		
		initializeMem = new Button(initializeMemComp, SWT.CHECK);
		initializeMem.setText(Messages.GenerateConstructorUsingFieldsInputPage_InitMembers);
		
		radioInitializerList = new Button(initializeMemComp, SWT.RADIO);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 5;
		radioInitializerList.setText(Messages.GenerateConstructorUsingFieldsInputPage_UsingInitList);
		radioInitializerList.setLayoutData(gd);
		radioInitializerList.setSelection(true);
		radioInitializerList.setEnabled(false);
		radioInitializerList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleInitMembersOption();
			}
		});
		
		radioAssignMem = new Button(initializeMemComp, SWT.RADIO);
		gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 5;
		radioAssignMem.setText(Messages.GenerateConstructorUsingFieldsInputPage_UsingAssignments);
		radioAssignMem.setLayoutData(gd);
		radioAssignMem.setEnabled(false);
		radioAssignMem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleInitMembersOption();
			}
		});
		
		initializeMem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(initializeMem.getSelection()) {
					radioAssignMem.setEnabled(true);
					radioInitializerList.setEnabled(true);
					handleInitMembersOption();
				} else {
					radioAssignMem.setEnabled(false);
					radioInitializerList.setEnabled(false);
					handleInitMembersOption();
				}	
			}
		});
		initializeOtherMembers = new Button(initializeMemComp, SWT.CHECK);
		initializeOtherMembers.setText("Initalize non parameter members");
		initializeOtherMembers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(initializeOtherMembers.getSelection()) {
					context.setInitializeOtherMembers(true);
				} else {
					context.setInitializeOtherMembers(false);
				}
			}
		});
	}

	protected void handleInitMembersOption() {
		if (initializeMem.getSelection()) {
			context.initializeMembers = true;
			if (radioInitializerList.getSelection()) {
				resetTreeFieldsOrder();
				context.initializeMembersMethod = GenerateConstructorUsingFieldsContext.INITMEMBERS_INITLIST;
			} else {
				context.initializeMembersMethod = GenerateConstructorUsingFieldsContext.INITMEMBERS_ASSIGNEMENT;
			}
		} else {
			context.initializeMembers = false;
		}
	}

	protected void resetTreeFieldsOrder() {
		context.treeFields.clear();
		context.treeFields.addAll(context.existingFields);
		variableSelectionView.refresh();
		ISelection selection = variableSelectionView.getSelection();
		if(selection != null && selection instanceof StructuredSelection) {
			Object object = ((StructuredSelection)selection).getFirstElement();
			if(object instanceof GenerateConstructorInsertEditProvider) {
				enableDisableUpDown((GenerateConstructorInsertEditProvider)object);
			}
		}
	}

	protected void moveUpFieldInTree(GenerateConstructorInsertEditProvider genConstInsertEditProvider) {
		int index = context.treeFields.indexOf(genConstInsertEditProvider);
		if(index > 0) {
			context.treeFields.set(index, context.treeFields.get(index - 1));
			context.treeFields.set(index - 1, genConstInsertEditProvider);
			enableDisableUpDown(genConstInsertEditProvider);
			variableSelectionView.refresh();
		}
	}

	protected void moveDownFieldInTree(GenerateConstructorInsertEditProvider genConstInsertEditProvider) {
		int index = context.treeFields.indexOf(genConstInsertEditProvider);
		if(index < context.treeFields.size() - 1  && index != -1) {
			context.treeFields.set(index, context.treeFields.get(index + 1));
			context.treeFields.set(index + 1, genConstInsertEditProvider);
			enableDisableUpDown(genConstInsertEditProvider);
			variableSelectionView.refresh();
		}
	}

	private void enableDisableUpDown(GenerateConstructorInsertEditProvider object) {
		int index = context.treeFields.indexOf(object);
		if(index == 0) {
			btnUp.setEnabled(false);
			if(index == context.treeFields.size() -1) {
				btnDown.setEnabled(false);
			} else {
				btnDown.setEnabled(true);
			}
		} else if (index == context.treeFields.size() -1) {
			btnDown.setEnabled(false);
			if(index == 0) {
				btnUp.setEnabled(false);
			} else {
				btnUp.setEnabled(true);
			}
	    } else {
			btnUp.setEnabled(true);
			btnDown.setEnabled(true);
		}
	}

}
