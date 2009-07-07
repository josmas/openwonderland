/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath" 
 * exception as provided by Sun in the License file that accompanied 
 * this code.
 */
package org.jdesktop.wonderland.modules.appbase.server.cell;

import com.jme.math.Vector2f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;

/**
 * An abstract server-side app.base cell for 2D apps. 
 * Intended to be subclassed by server-side 2D app cells.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class App2DCellMO extends AppCellMO {

    /** The pixel scale. */
    protected Vector2f pixelScale;

    /** Create an instance of App2DCellMO. */
    public App2DCellMO() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        App2DCellServerState serverState = (App2DCellServerState) state;
        pixelScale = new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY());
    }

    /**
     * Fill in the given client state with the cell server state.
     */
    protected void populateClientState(App2DCellClientState clientState) {
        clientState.setPixelScale(pixelScale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            return null;
        }

        App2DCellServerState state = (App2DCellServerState) stateToFill;
        state.setPixelScaleX(pixelScale.getX());
        state.setPixelScaleY(pixelScale.getY());

        return super.getServerState(stateToFill);
    }
}
