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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

public class GenerateConstructorLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element != null) {
			if (element instanceof GenerateConstructorInsertEditProvider) {
				GenerateConstructorInsertEditProvider gcInsertEditProv = (GenerateConstructorInsertEditProvider) element;
				ASTAccessVisibility visibility = ASTAccessVisibility.PUBLIC;
				switch (gcInsertEditProv.getVisibility()) {
				case ICPPASTVisibilityLabel.v_private:
					visibility = ASTAccessVisibility.PRIVATE;
					break;

				case ICPPASTVisibilityLabel.v_protected:
					visibility = ASTAccessVisibility.PROTECTED;
					break;
				case ICPPASTVisibilityLabel.v_public:
					visibility = ASTAccessVisibility.PUBLIC;
					break;
				}
				return CElementImageProvider.getFieldImageDescriptor(visibility).createImage();
			}
		}
		return null;
	}

}
