/*******************************************************************************
 * Copyright (c) 2011, 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.model.ext.ICElementHandle;

import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class CReturnTypeHyperlinkDetector extends CElementHyperlinkDetector {
	@Override
	protected IHyperlink createHyperLink(IRegion region, IASTName selectedName, IAction openAction) {
		if (selectedName != null) {
			IIndex index = selectedName.getTranslationUnit().getIndex();
			try {
				index.acquireReadLock();
				IBinding binding = selectedName.resolveBinding();
				if (binding instanceof IFunction) {
					IType returnType = ((ICPPFunction) binding).getType().getReturnType();
					returnType = SemanticUtil.getUltimateType(returnType, false);
					
					if(returnType instanceof IBasicType || !(returnType instanceof IBinding)) {
						return null;
					}
					
					IBinding returnTypeBinding = (IBinding) returnType;
					
					// TODO: steal from somewhere else? Can it be chained?
					// class, struct, union... could be templated
					if (returnType instanceof ICompositeType) {
						returnTypeBinding = (ICompositeType) returnType;

						if (returnType instanceof ICPPTemplateInstance) {
							ICPPTemplateInstance icppTemplateInstance = (ICPPTemplateInstance) returnType;
							returnTypeBinding = icppTemplateInstance.getTemplateDefinition();

						}
					}

					// TODO: need this??
//					if(returnType instanceof ITypedef) {
//						returnTypeBinding = (ITypedef) returnType;
//						if(finalReturnType instanceof ICPPSpecialization) {
//							ICPPSpecialization icppSpecialization = (ICPPSpecialization) finalReturnType;
//							returnTypeBinding = icppSpecialization.getSpecializedBinding();
//						}
//					}
					
					if(returnTypeBinding != null) {
						ICElementHandle[] foundElements = IndexUI.findRepresentative(index, returnTypeBinding);						
						if (foundElements.length > 0 && foundElements[0] != null) {
							ITextEditor textEditor = (ITextEditor) getAdapter(ITextEditor.class);
							if (textEditor != null) {
								return new CReturnTypeHyperlink(region, textEditor.getEditorSite().getPage(), foundElements[0]);
							}
						}
					}
				}
			} catch (InterruptedException e) {
				CUIPlugin.log(e);
			} catch (CoreException e) {
				CUIPlugin.log(e);
			} finally {
				index.releaseReadLock();
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
