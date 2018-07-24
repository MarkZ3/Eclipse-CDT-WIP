/*******************************************************************************
 * Copyright (c) 2013 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.generateconstructor.usingfields;

import junit.framework.Test;

import org.eclipse.ltk.core.refactoring.Refactoring;

import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields.GenerateConstructorUsingFieldsContext;
import org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields.GenerateConstructorUsingFieldsRefactoring;

public class GenerateConstructorUsingFieldsTest extends RefactoringTestBase {

	private String[] selectedFields;
	private boolean definitionSeparate = false;
	private boolean initializeMembers = false;
	private GenerateConstructorUsingFieldsRefactoring refactoring;

	public static Test suite() {
		return suite(GenerateConstructorUsingFieldsTest.class);
	}

	@Override
	protected Refactoring createRefactoring() {
		if (ascendingVisibilityOrder) {
			getPreferenceStore().setValue(PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER,
					ascendingVisibilityOrder);
		}
		refactoring = new GenerateConstructorUsingFieldsRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}
	
	@Override
	protected void simulateUserInput() {
		GenerateConstructorUsingFieldsContext context = refactoring.getContext();

		if (selectedFields != null) {
			for (String name : selectedFields) {
				context.selectField(name);
			}
		}

		context.setDefinitionSeparate(definitionSeparate);
	}
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	///*$*//*$$*/
	//private:
	//	int a;
	//};
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//public:
	//	Person(int a);
	//
	//private:
	//	int a;
	//};
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//====================
	//#include "A.h"
	//
	//Person::Person(int a) {
	//}
	public void testSingleField() throws Exception {
		definitionSeparate = true;
		selectedFields = new String[] { "number" };
		assertRefactoringSuccess();
	}
	
	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	/*$*/int a, b;/*$$*/
	//	int c;
	//};
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//public:
	//	Person(int a, int b) {
	//	}
	//
	//private:
	//	int a, b;
	//	int c;
	//};
	//
	//#endif /* A_H_ */
	public void testSelection() throws Exception {
		assertRefactoringSuccess();
	}

}
