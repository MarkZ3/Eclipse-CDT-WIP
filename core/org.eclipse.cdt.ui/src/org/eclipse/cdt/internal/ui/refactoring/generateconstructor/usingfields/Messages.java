/*******************************************************************************
 * Copyright (c) 2010, 2019 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String GenerateConstructorUsingFields_Name;
	public static String GenerateConstructorUsingFieldsInputPage_header;
	public static String GenerateConstructorUsingFields_NoFields;
	public static String GenerateConstructorUsingFields_NoCassDefFound;
	public static String GenerateConstructorUsingFieldsInputPage_SelectAll;
	public static String GenerateConstructorUsingFieldsInputPage_DeselectAll;
	public static String GenerateConstructorUsingFieldsInputPage_SeparateDefinition;
	public static String GenerateConstructorUsingFieldsRefactoring_NoImplFile;
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.ui.refactoring.generateconstructor.usingfields.messages";//$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
