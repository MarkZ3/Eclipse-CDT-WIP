/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     IBM Corporation
 *     Anton Leherbauer (Wind River Systems)
 *     Mike Kucera - IBM
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.ms.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.parser.AbstractCLikeLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.ISourceCodeParser;
import org.eclipse.cdt.core.dom.parser.cpp.GPPParserExtensionConfiguration;
import org.eclipse.cdt.core.dom.parser.cpp.ICPPParserExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IParserSettings;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMLinkageFactory;
import org.eclipse.cdt.internal.core.pdom.dom.cpp.PDOMCPPLinkageFactory;

/**
 * Concrete ILanguage implementation for the DOM C++ parser.
 */
public class MSVCPPLanguage extends AbstractCLikeLanguage {
	public static final String ID = CCorePlugin.PLUGIN_ID + ".msvc++"; //$NON-NLS-1$

	private static final MSVCPPLanguage DEFAULT_INSTANCE = new MSVCPPLanguage();

	private static final GPPParserExtensionConfiguration CLANG_CL_PARSER_EXTENSION_CONFIG = new GPPParserExtensionConfiguration() {
		@Override
		public org.eclipse.cdt.core.dom.parser.IBuiltinBindingsProvider getBuiltinBindingsProvider() {
			return new ClangClBuiltinSymbolProvider(ParserLanguage.CPP, true);
		};
	};

	public static MSVCPPLanguage getDefault() {
		return DEFAULT_INSTANCE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter.isAssignableFrom(IPDOMLinkageFactory.class)) {
			return (T) new PDOMCPPLinkageFactory();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public int getLinkageID() {
		return ILinkage.CPP_LINKAGE_ID;
	}

	/**
	 * @since 5.4
	 */
	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration(IScannerInfo info) {
		return MSVCPPScannerExtensionConfiguration.getInstance(info);
	}

	private ICPPParserExtensionConfiguration getParserExtensionConfiguration(IScanner scanner) {
		if (scanner.getMacroDefinitions().containsKey("__clang__")) //$NON-NLS-1$
			return CLANG_CL_PARSER_EXTENSION_CONFIG;

		return MSVCPPParserExtensionConfiguration.getInstance();
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index) {
		return new GNUCPPSourceParser(scanner, parserMode, logService, getParserExtensionConfiguration(scanner), index);
	}

	@Override
	protected ISourceCodeParser createParser(IScanner scanner, ParserMode parserMode, IParserLogService logService,
			IIndex index, int options, IParserSettings settings) {
		GNUCPPSourceParser parser = new GNUCPPSourceParser(scanner, parserMode, logService,
				getParserExtensionConfiguration(scanner), index);
		if (settings != null) {
			int maximumTrivialExpressions = settings.getMaximumTrivialExpressionsInAggregateInitializers();
			if (maximumTrivialExpressions >= 0
					&& (options & OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS) != 0) {
				parser.setMaximumTrivialExpressionsInAggregateInitializers(maximumTrivialExpressions);
			}
		}
		return parser;
	}

	@Override
	protected ParserLanguage getParserLanguage() {
		return ParserLanguage.CPP;
	}
}
