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
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;

public final class CompositeTypeSpecFinder extends ASTVisitor {
	private final int selectionStartOffset;
	private int currentNodeLength = Integer.MAX_VALUE;
	
	IASTCompositeTypeSpecifier compositeTypeSpecifier;
	{
		shouldVisitDeclSpecifiers = true;
	}

	public CompositeTypeSpecFinder(int selectionStartOffset) {
		this.selectionStartOffset = selectionStartOffset;
	}
	
	public IASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		return compositeTypeSpecifier;
	}

	@Override
	public int visit(IASTDeclSpecifier declSpec) {

		if (declSpec instanceof IASTCompositeTypeSpecifier) {
			IASTFileLocation loc = declSpec.getFileLocation();
			if ((compositeTypeSpecifier == null || selectionStartOffset > loc.getNodeOffset()
					&& selectionStartOffset < loc.getNodeOffset() + loc.getNodeLength())
					&& currentNodeLength > loc.getNodeLength()) {
				compositeTypeSpecifier = (IASTCompositeTypeSpecifier) declSpec;
				currentNodeLength = loc.getNodeLength();
			}
		}

		return super.visit(declSpec);
	}

}
