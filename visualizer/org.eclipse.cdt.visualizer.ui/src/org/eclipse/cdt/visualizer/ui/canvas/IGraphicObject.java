/*******************************************************************************
 * Copyright (c) 2012, 2014 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *     Xavier Raynaud (Kalray) - Bug 430804
 *******************************************************************************/

package org.eclipse.cdt.visualizer.ui.canvas;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;


// ---------------------------------------------------------------------------
// IGraphicObject
// ---------------------------------------------------------------------------

/**
 * An object that can be displayed and manipulated on a GraphicCanvas.
 */
public interface IGraphicObject
{
	// --- methods ---
	
	/** Paints object using specified graphics context.
	 *  If decorations is false, draws ordinary object content.
	 *  If decorations is true, paints optional "decorations" layer.
	 */
	public void paint(GC gc, boolean decorations);

	/**
	 * Return the tooltip to display when mouse stays on this object.
	 * It may return <code>null</code> if there is nothing to display.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @return the tooltip to display on this object.
	 */
	public String getTooltip(int x, int y);
	
	/** Returns true if object has decorations to paint. */
	public boolean hasDecorations();
	
	/** Gets model data (if any) associated with this graphic object */
	public Object getData();
	
	/** Sets model data (if any) associated with this graphic object */
	public void setData(Object data);
	
	/** Whether graphic object contains the specified point. */
	public boolean contains(int x, int y);
	
	/** Returns true if element bounds are within specified rectangle. */
	public boolean isWithin(Rectangle region);
}