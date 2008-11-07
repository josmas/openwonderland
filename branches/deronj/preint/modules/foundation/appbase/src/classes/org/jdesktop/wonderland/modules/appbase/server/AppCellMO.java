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
package org.jdesktop.wonderland.modules.appbase.server;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.common.AppCellConfig;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;


/**
 * A server-side <code>app.base</code> app cell.
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppCellMO extends CellMO { 

    /** Default constructor, used when the cell is created via WFS */
    public AppCellMO() {
	super();
    }
    
    /**
     * Creates a new instance of <code>AppCellMO</code> with the specified localBounds and transform.
     * If either parameter is null an IllegalArgumentException will be thrown.
     *
     * @param localBounds the bounds of the new cell, must not be null.
     * @param transform the transform for this cell, must not be null.
     */
    public AppCellMO (BoundingVolume localBounds, CellTransform transform) {
        super(localBounds, transform);
    }
    
    /** 
     * Return the app type of this cell.
     */
    public abstract AppTypeMO getAppType ();

    /** 
     * {@inheritDoc}
     */
    @Override
    protected CellConfig getCellConfig (ClientSession clientSession, ClientCapabilities capabilities) {
        return new AppCellConfig();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void setupCell(BasicCellSetup setupData) {
        super.setupCell(setupData);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void reconfigureCell (BasicCellSetup setup) {
        setupCell(setup);
    }
}
