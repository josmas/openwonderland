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
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellClientState;
import org.jdesktop.wonderland.modules.appbase.common.cell.App2DCellServerState;
import com.jme.bounding.BoundingBox;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.ViewComponentServerState;

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

    /** The view transform of the cell creator. */
    private CellTransform creatorViewTransform;

    /** Whether the initial placement of the cell has been completed. */
    private boolean initialPlacementDone;

    /** Create an instance of App2DCellMO. */
    public App2DCellMO() {
        // Unfortunately, the bounds cannot be modified later, so we need to leave
        // enough space for a fairly large window. A window can easily be 1K x 1K,
        // 4K x 4K is the max, so 2K x 2K seems like a reasonable number. Also 
        // unfortunately, we don't know the pixel scale at this point so, out of 
        // desparation, we choose the default value of 0.01 meters per pixel.
        // This gives values of approx. A radius of 10 x 10 for the local width and height
        // of the bounds. 5 meters (in front) should be reasonable for the depth because the 
        // step per window stack level is only 0.01 meter and the stack never gets too large.
        super(new BoundingBox(new Vector3f(), 10, 10, 5), new CellTransform(null, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerState(CellServerState state) {
        super.setServerState(state);
        App2DCellServerState serverState = (App2DCellServerState) state;
        pixelScale = new Vector2f(serverState.getPixelScaleX(), serverState.getPixelScaleY());

        ViewComponentServerState vcss =
                (ViewComponentServerState) serverState.getComponentServerState(ViewComponentServerState.class);
        if (vcss != null) {
	    creatorViewTransform = vcss.getCellTransform();
        }
	logger.warning("Cell creator view transform = " + creatorViewTransform);
    }

    /**
     * Fill in the given client state with the cell server state.
     */
    protected void populateClientState(App2DCellClientState clientState) {
        clientState.setPixelScale(pixelScale);
        clientState.setCreatorViewTransform(creatorViewTransform);
        clientState.setInitialPlacementDone(initialPlacementDone);
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

    void setInitialPlacementDone (boolean done) {
        initialPlacementDone = done;
    }

    boolean isInitialPlacementDone () {
        return initialPlacementDone;
    }

}
