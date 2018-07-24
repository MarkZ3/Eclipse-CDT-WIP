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

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;

public class GenerateConstructorInsertEditProvider  implements Comparable<GenerateConstructorInsertEditProvider>{


	private int visibility;

	private IASTDeclarator fieldDeclarator;

	private IASTDeclSpecifier fieldDeclSpecifier;
	
	public GenerateConstructorInsertEditProvider(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator, int visibility) {
		super();
		this.fieldDeclSpecifier = declSpecifier;
		this.fieldDeclarator = declarator;
		this.visibility = visibility;
	}

	@Override
	public String toString(){
		IASTPointerOperator[] iastPointerOperators = fieldDeclarator.getPointerOperators();
		String operatorsStr = new String();
		for(IASTPointerOperator iastPointerOperator : iastPointerOperators) {
			if (iastPointerOperator instanceof IASTPointer) {
				operatorsStr += " * "; //$NON-NLS-1$
			} else if (iastPointerOperator instanceof ICPPASTReferenceOperator) {
				operatorsStr += " & "; //$NON-NLS-1$
			}
		}
		return fieldDeclarator.getName().toString() 
		+ " : " + fieldDeclSpecifier.toString() + operatorsStr; //$NON-NLS-1$
	}
	
	public IASTDeclSpecifier getDeclSpecifier() {
		return fieldDeclSpecifier;
	}
	
	public IASTDeclarator getFieldDeclarator() {
		return fieldDeclarator;
	}
	
	@Override
	public int compareTo(GenerateConstructorInsertEditProvider o) {
		return toString().compareTo(o.toString());
	}
	
	public int getVisibility() {
		return visibility;
	}

}
