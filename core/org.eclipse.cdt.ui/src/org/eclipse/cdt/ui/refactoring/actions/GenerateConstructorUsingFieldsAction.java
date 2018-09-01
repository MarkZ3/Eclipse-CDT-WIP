/*******************************************************************************
 * Copyright (c) 2010, 2018 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields.GenerateConstructorUsingFieldsRefactoringRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ui.IEditorPart;

/**
 * @since 6.5
 * @noextend This class is not intended to be subclassed by clients.
 */
public class GenerateConstructorUsingFieldsAction extends RefactoringAction {

	public GenerateConstructorUsingFieldsAction() {
		super(Messages.GenerateConstructorUsingFields_label);
	}

	public GenerateConstructorUsingFieldsAction(IEditorPart editor) {
		super(Messages.GenerateConstructorUsingFields_label);
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		IResource res= wc.getResource();
		if (res instanceof IFile) {
			new GenerateConstructorUsingFieldsRefactoringRunner((IFile) res, s, wc, shellProvider, wc.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		new GenerateConstructorUsingFieldsRefactoringRunner(null, null, elem, shellProvider, elem.getCProject()).run();
	}

}
