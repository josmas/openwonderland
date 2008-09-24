/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.client.app.base;

import java.util.ArrayList;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.client.cell.CellCache;

/**
 * The generic application cell superclass. Created with a subclass-specific constructor.
 *
 * @author deronj
 */ 

@ExperimentalAPI
public abstract class AppCell extends Cell {

    /** A list of all app cells in existence */
    private static ArrayList<AppCell> appCells = new ArrayList<AppCell>();

    /**
     * If non-null, this is the master app which was awaiting its cell.
     * If null, this is a slave cell.
     */
    protected App app;

    /**
     * Create an instance of AppCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public AppCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
	appCells.add(this);
    }

    /**
     * Clean up resources held. 
     */
    public void cleanup () {
	synchronized (appCells) {
	    appCells.remove(this);
	}
	if (app != null) {
	    app.cleanup();
	}
	app = null;
    }

    /** 
     * Return the app type of this cell.
     */
    public abstract AppType getAppType ();

    /**
     * Associate the app with a cell. May only be called one time.
     *
     * @param app The world cell containing the app.
     * @throws IllegalArgumentException If the app already is associated with a cell .
     * @throws IllegalStateException If the cell is already associated with an app.
     */
    public void setApp (App app) 
	throws IllegalArgumentException, IllegalStateException 
    {
	if (app == null) {
	    throw new NullPointerException();
	}
	if (app.getCell() != null) {
	    throw new IllegalArgumentException("App already has a cell");
	}
	if (this.app != null) {
	    throw new IllegalStateException("Cell already has an app");
	}

	this.app = app;
	updateBoundsFromApp();
    }

    /**
     * Get the app associated with this cell.
     */
    public App getApp () {
	return app;
    }

    /**
     * Update cell bounds from the app bounds.
     */
    private void updateBoundsFromApp () {
	if (app == null) {
	    return;
	}
	//TODO: How do we do this?
    }

    /**
     * Returns the string representation of this object.
     */
    @Override
    public String toString () {
        StringBuffer buf = new StringBuffer();
        buf.append(super.toString());
	buf.append(",app=[" + app + "]");
        return buf.toString();
    }	
}
