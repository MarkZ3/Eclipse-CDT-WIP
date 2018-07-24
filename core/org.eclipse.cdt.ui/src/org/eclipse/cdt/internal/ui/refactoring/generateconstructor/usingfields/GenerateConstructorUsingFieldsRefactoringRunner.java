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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;

public class GenerateConstructorUsingFieldsRefactoringRunner extends RefactoringRunner {

	public GenerateConstructorUsingFieldsRefactoringRunner(IFile file, ISelection selection,
			ICElement element, IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		GenerateConstructorUsingFieldsRefactoring refactoring = new GenerateConstructorUsingFieldsRefactoring(
				element, selection, project);
		GenerateConstructorUsingFieldsRefactoringWizard wizard = new GenerateConstructorUsingFieldsRefactoringWizard(
				refactoring);
		run(wizard, refactoring, RefactoringSaveHelper.SAVE_NOTHING);
	}
}
