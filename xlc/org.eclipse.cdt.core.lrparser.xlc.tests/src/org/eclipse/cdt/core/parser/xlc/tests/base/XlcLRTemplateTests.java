/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.xlc.tests.base;

import org.eclipse.cdt.core.lrparser.tests.LRTemplateTests;
import org.eclipse.cdt.core.lrparser.xlc.XlcCLanguage;
import org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;

import junit.framework.TestSuite;

public class XlcLRTemplateTests extends LRTemplateTests {
	public static TestSuite suite() {
		return suite(XlcLRTemplateTests.class);
	}

	protected ILanguage getCLanguage() {
		return XlcCLanguage.getDefault();
	}

	protected ILanguage getCPPLanguage() {
		return XlcCPPLanguage.getDefault();
	}
}
