/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree.uiwidgets;

import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.ui.templateengine.event.PatternEvent;
import org.eclipse.cdt.ui.templateengine.event.PatternEventListener;
import org.eclipse.cdt.ui.templateengine.uitree.UIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * By extending Composite we can create our own Container. UIComposite can act
 * as the bridge between the UIPage and the UIWidgets contained in that page.
 * The PatternEvents generated by the UIWidgets will be fired to the UIPage
 * which is a PatternEventListener.
 */

public class UIComposite extends Composite {

	/**
	 * The group UIElement corresponding to this UIPage.
	 */
	private UIElement uiElement;

	/**
	 * The list of PatternEventListeners.
	 */
	private Vector<PatternEventListener> vector;

	/**
	 * parent Composite, and The UIElement corresponding to this page.
	 *
	 * @param parent
	 * @param uiElement
	 */
	public UIComposite(Composite parent, UIElement uiElement, Map<String, String> valueStore) {
		super(parent, SWT.NONE);

		vector = new Vector<PatternEventListener>();
		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 10;
		layout.marginHeight = 5;
		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		this.uiElement = uiElement;
	}

	/**
	 * add a PatternListener to the list.
	 *
	 * @param patternListener
	 */
	public void addPatternListener(PatternEventListener patternListener) {
		vector.add(patternListener);
	}

	/**
	 * remove the PatternListener from the list.
	 *
	 * @param patternListener
	 */
	public void removePatternListener(PatternEventListener patternListener) {
		vector.remove(patternListener);
	}

	/**
	 * On occurrence of PatternEvent this method is called to invoke
	 * patternPerformed on all the registered listeners. In our application, we
	 * will have just one registered listener.
	 */

	public void firePatternEvent(PatternEvent patternEvent) {
		for (int i = 0; i < vector.size(); i++) {
			vector.get(i).patternPerformed(patternEvent);
		}
	}

	/**
	 * This method will invoke the getValues on UIElement (group Element), which
	 * in turn will invoke the getValues on the UIElement (widgets). This
	 * returns an HashMap of Values.
	 */
	public Map<String, String> getPageData() {
		return uiElement.getValues();
	}

	/**
	 * return the UIElement(group UI Element) represented by this UIComposite.
	 *
	 * @return UIElement.
	 */
	public UIElement getUIElement() {
		return uiElement;
	}

	/**
	 * This information is used by UIPages to enable or disable the next button.
	 */
	public boolean isValid() {
		return uiElement.isValid();
	}

}
