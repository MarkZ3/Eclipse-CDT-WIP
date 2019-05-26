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

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class GenerateConstructorUsingFieldsContext implements ITreeContentProvider {

	ICPPASTCompositeTypeSpecifier currentClass = null;

	public ArrayList<GenerateConstructorInsertEditProvider> treeFields = new ArrayList<>();
	ArrayList<GenerateConstructorInsertEditProvider> existingFields = new ArrayList<>();

	private boolean definitionSeparate = false;

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
		ArrayList<GenerateConstructorInsertEditProvider> selectedFieldsInOrder = new ArrayList<>();

		for (GenerateConstructorInsertEditProvider treeField : treeFields) {
			if (treeField.isSelected()) {
				selectedFieldsInOrder.add(treeField);
			}
		}

		return selectedFieldsInOrder;
	}

	public void setDefinitionSeparate(boolean definitionSeparate) {
		this.definitionSeparate = definitionSeparate;
	}

	public boolean isSeparateDefinition() {
		return definitionSeparate;
	}

	public void selectField(String name) {
		for (GenerateConstructorInsertEditProvider treeField : treeFields) {
			if (name.equals(String.valueOf(treeField.getFieldDeclarator().getName().getSimpleID()))) {
				treeField.setSelected(true);
			}
		}
	}

}
