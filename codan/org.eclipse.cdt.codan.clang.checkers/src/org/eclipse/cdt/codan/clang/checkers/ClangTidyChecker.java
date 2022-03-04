/*******************************************************************************
 * Copyright (c) 2022 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.clang.checkers;

import java.io.File;

import org.eclipse.cdt.codan.core.cxx.externaltool.AbstractExternalToolBasedChecker;
import org.eclipse.cdt.codan.core.cxx.externaltool.ConfigurationSettings;

public class ClangTidyChecker extends AbstractExternalToolBasedChecker {

	private static final String TOOL_NAME = "Clang-Tidy"; //$NON-NLS-1$
	private static final String EXECUTABLE_NAME = "clang-tidy"; //$NON-NLS-1$
	private static final String DEFAULT_ARGS = ""; //$NON-NLS-1$

	public ClangTidyChecker() {
		super(new ConfigurationSettings(TOOL_NAME, new File(EXECUTABLE_NAME), DEFAULT_ARGS));
	}

	@Override
	protected String[] getParserIDs() {
		return new String[] { "org.eclipse.cdt.core.GCCErrorParser" }; //$NON-NLS-1$
	}

	@Override
	protected String getReferenceProblemId() {
		return "org.eclipse.cdt.codan.checkers.clang.clangtidy.warning"; //$NON-NLS-1$
	}

}
