/*******************************************************************************
 * Copyright (c) 2011 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

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

					returnType = getStrippedType(returnType);
					if(returnType instanceof IBasicType) {
						return null;
					}
					
					IBinding finalReturnType = null;
					if(returnType instanceof IBinding) {
						finalReturnType = (IBinding) returnType;
					}
					
					while(returnType instanceof IQualifierType) {
						returnType = ((IQualifierType) returnType).getType();
					}
					
					if (returnType instanceof ICompositeType) {
						ICompositeType iCompositeType = (ICompositeType) returnType;
						finalReturnType = iCompositeType;

						if (returnType instanceof ICPPTemplateInstance) {
							ICPPTemplateInstance icppTemplateInstance = (ICPPTemplateInstance) returnType;
							finalReturnType = icppTemplateInstance.getTemplateDefinition();

						}
					}
					
					if(returnType instanceof ITypedef) {
						finalReturnType = (ITypedef) returnType;
						if(finalReturnType instanceof ICPPSpecialization) {
							ICPPSpecialization icppSpecialization = (ICPPSpecialization) finalReturnType;
							finalReturnType = icppSpecialization.getSpecializedBinding();
						}
					}
					
					if(finalReturnType != null) {

						ICElementHandle[] foundElements = IndexUI.findAllDefinitions(index, finalReturnType);

						if (foundElements.length == 0) {
							foundElements = new ICElementHandle[1];
							foundElements[0] = IndexUI.findAnyDeclaration(index, null, finalReturnType);
						}
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

	/**
	 * @param returnType
	 */
	protected IType getStrippedType(IType returnType) {
		while (returnType instanceof IPointerType || returnType instanceof ICPPReferenceType || returnType instanceof IArrayType) {
			if (returnType instanceof IPointerType) {
				IPointerType iPointerType = (IPointerType) returnType;
				returnType = iPointerType.getType();
			} else if (returnType instanceof ICPPReferenceType) {
				ICPPReferenceType icppReferenceType = (ICPPReferenceType) returnType;
				returnType = icppReferenceType.getType();
			} else if (returnType instanceof IArrayType) {
				IArrayType iArrayType = (IArrayType) returnType;
				returnType = iArrayType.getType();
			}
		}

		return returnType;
	}

	@Override
	protected IHyperlink[] getNonASTBasedHyperlinks(IRegion region, IAction openAction, IDocument document,
			IWorkingCopy workingCopy) {
		return null;
	}
}
