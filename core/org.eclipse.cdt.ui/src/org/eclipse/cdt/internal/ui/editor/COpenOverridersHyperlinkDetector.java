/*******************************************************************************
 * Copyright (c) 2010, 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.typehierarchy.TypeHierarchyUI;

public class COpenOverridersHyperlinkDetector extends CElementHyperlinkDetector {

	@Override
	protected IHyperlink createHyperLink(IRegion region, IASTName selectedName, IAction openAction) {
		if(selectedName != null) {
			IBinding binding = selectedName.resolveBinding();
			if(binding instanceof ICPPMethod) {
				ICPPMethod method = (ICPPMethod)binding;
				if(method.isPureVirtual() && TypeHierarchyUI.isValidInput(binding)) {
					ITextEditor textEditor= (ITextEditor)getAdapter(ITextEditor.class);
					if(textEditor != null) {
						return new COpenOverridersHyperlink(region, openAction, textEditor.getAction("OpenHierarchy"), //$NON-NLS-1$
								selectedName, textEditor.getEditorSite());
					}
				}
			}
		}
		return null;
	}
	
	@Override
	protected IHyperlink[] getNonASTBasedHyperlinks(IRegion region, IAction openAction, IDocument document,
			IWorkingCopy workingCopy) {
		return null;
	}

}
