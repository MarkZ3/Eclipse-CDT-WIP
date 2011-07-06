/*******************************************************************************
 * Copyright (c) 2011 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbenchPage;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.viewsupport.EditorOpener;

public class CReturnTypeHyperlink implements IHyperlink {

	private final IRegion region;
	private IWorkbenchPage workbenchPage;
	private ICElement cElementHandle;

	public CReturnTypeHyperlink(IRegion region, IWorkbenchPage workbenchPage, ICElement cElementHandle) {
		this.region = region;
		this.workbenchPage = workbenchPage;
		this.cElementHandle = cElementHandle;
	}

	public IRegion getHyperlinkRegion() {
		return region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return CEditorMessages.OpenReturnType_name;
	}

	public void open() {
		try {
			EditorOpener.open(workbenchPage, cElementHandle);
		} catch (CModelException e) {
			CUIPlugin.log(e);
		}
		
	}

}
