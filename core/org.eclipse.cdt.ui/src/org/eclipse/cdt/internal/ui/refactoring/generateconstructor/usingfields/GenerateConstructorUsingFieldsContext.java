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
import java.util.HashMap;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class GenerateConstructorUsingFieldsContext implements ITreeContentProvider {

	ICPPASTCompositeTypeSpecifier currentClass = null;
	
	public ArrayList<ICPPASTBaseSpecifier> baseClasses = new ArrayList<ICPPASTBaseSpecifier>();
	public HashMap<ICPPASTBaseSpecifier, ArrayList<ICPPConstructor>> baseClassesConstrutors = new HashMap<ICPPASTBaseSpecifier, ArrayList<ICPPConstructor>>();
	public HashMap<ICPPASTBaseSpecifier, ICPPConstructor> selectedbaseClassesConstrutors = new HashMap<ICPPASTBaseSpecifier, ICPPConstructor>();
	public ArrayList<GenerateConstructorInsertEditProvider> selectedFields = new ArrayList<GenerateConstructorInsertEditProvider>();
	public ArrayList<GenerateConstructorInsertEditProvider> treeFields = new ArrayList<GenerateConstructorInsertEditProvider>();
	ArrayList<GenerateConstructorInsertEditProvider> existingFields = new ArrayList<GenerateConstructorInsertEditProvider>();

	private boolean separateDefinition = false;
	
	/* Declaration options */
	
	public boolean addConstToObjects = false;
	public boolean passObjectsByReference = false;
	
	/* Definition options */
	
	public final static int INITMEMBERS_INITLIST = 0;
	public final static int INITMEMBERS_ASSIGNEMENT = 1;
	
	public boolean initializeMembers = false;
	public int initializeMembersMethod = INITMEMBERS_INITLIST;
	private boolean initializeOtherMembers = false;
	
	
	@Override
	public Object[] getElements(Object inputElement) {
		return treeFields.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}
	
	public ArrayList<GenerateConstructorInsertEditProvider> getSelectedFieldsInOrder() {
		ArrayList<GenerateConstructorInsertEditProvider> selectedFieldsInOrder = new ArrayList<GenerateConstructorInsertEditProvider>();
		
		for(GenerateConstructorInsertEditProvider treeField : treeFields) {
			if(selectedFields.contains(treeField)) {
				selectedFieldsInOrder.add(treeField);
			}
		}
		
		return selectedFieldsInOrder;
	}

	public void setSeparateDefinition(boolean implementationInHeader) {
		this.separateDefinition = implementationInHeader;
	}

	public boolean isSeparateDefinition() {
		return separateDefinition;
	}

	public boolean isInitializeOtherMembers() {
		return initializeOtherMembers;
	}

	public void setInitializeOtherMembers(boolean initializeOtherMembers) {
		this.initializeOtherMembers = initializeOtherMembers;
	}

}
