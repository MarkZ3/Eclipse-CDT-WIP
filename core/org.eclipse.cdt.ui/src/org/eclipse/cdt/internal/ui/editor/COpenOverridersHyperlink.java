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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;

import org.eclipse.cdt.internal.ui.util.StatusLineHandler;
import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;

public class COpenOverridersHyperlink extends CElementHyperlink {

	IWorkbenchPage workbenchPage;
	IASTName name;
	IAction typeHierarchyAction;
	IEditorSite editorSite;
	
	public COpenOverridersHyperlink(IRegion region, IAction openAction, IAction typeHierarchyAction, IASTName selectedName, IEditorSite iEditorSite) {
		super(region, openAction);
		editorSite = iEditorSite;
		workbenchPage = iEditorSite.getPage();
		name = selectedName;
		this.typeHierarchyAction = typeHierarchyAction;
	}

	@Override
	public void open() {
		final IIndex index = name.getTranslationUnit().getIndex();
		try {
			index.acquireReadLock();
			IBinding[] virtualOverriders = ClassTypeHelper.findOverriders(index, (ICPPMethod)name.resolveBinding());
			switch (virtualOverriders.length) {
			case 0:
				super.open();
				StatusLineHandler.clearStatusLine(editorSite);
				StatusLineHandler.showStatusLineMessage(editorSite, CEditorMessages.OpenOverriders_NotFound);
				break;
			case 1:
				ICElement[] cElementHandles = IndexUI.findRepresentative(index, virtualOverriders[0]);
				EditorOpener.open(workbenchPage, cElementHandles[0]);
				break;
			default:
				typeHierarchyAction.run();
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} catch (InterruptedException e) {
			CUIPlugin.log(e);
		} finally {
			index.releaseReadLock();
		}
	}
	
	@Override
	public String getHyperlinkText() {
		return CEditorMessages.OpenOverriders_label;
	}

}
