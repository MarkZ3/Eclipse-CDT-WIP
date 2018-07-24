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

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

public class GenerateConstructorUsingFieldsRefactoringWizard extends RefactoringWizard {

	private final GenerateConstructorUsingFieldsRefactoring refactoring;

	public GenerateConstructorUsingFieldsRefactoringWizard(
			GenerateConstructorUsingFieldsRefactoring refactoring) {
		super(refactoring, WIZARD_BASED_USER_INTERFACE);
		this.refactoring = refactoring;
	}
	
	@Override
	protected void addUserInputPages() {
		UserInputWizardPage page = new GenerateConstructorUsingFieldsInputPage(refactoring.getContext());
		addPage(page);
	}

}
