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
package org.jdesktop.wonderland.modules.appbase.client.cell;

import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import org.jdesktop.wonderland.client.cell.utils.CellPlacementUtils;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.modules.appbase.client.FirstVisibleInitializer;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault.Frame2DCell;

/**
 * Used to move an cell to its initial location before the first time something
 * something is made visible within the cell. (This is usually done by the
 * first slave that gets around to it. Or, in the case, of a user-launched
 * app cell, by the master.
 *
 *
 * @author deronj
 */
@ExperimentalAPI
public class FirstVisibleInitializerCell implements FirstVisibleInitializer {

    private static final Logger logger = Logger.getLogger(FirstVisibleInitializerCell.class.getName());

    /** The cell to be initialized. */
    private App2DCell cell;

    /** The view transform of the cell creator. */
    private CellTransform creatorViewTransform;

    public FirstVisibleInitializerCell (App2DCell cell, CellTransform creatorViewTransform) {
        this.cell = cell;
        this.creatorViewTransform = creatorViewTransform;
        logger.info("creatorViewTransform = " + creatorViewTransform);
    }

    /** {@inheritDoc} */
    public void initialize (float width3D, float height3D) {
        logger.info("FVI.initialize, wh3D = " + width3D + ", " + height3D);

        // Include the frame header and footer
        float height = height3D + Frame2DCell.HEADER_HEIGHT + Frame2DCell.SIDE_THICKNESS;

        // Determine the "first visible bounds" based on the size of the first-visible window
        BoundingBox bbox = new BoundingBox(new Vector3f(), width3D/2.0f, height/2.0f, 1f);
        logger.info("new bbox = " + bbox);
        
        // Calculate the "best" initial cell transform, based on the size of the first
        // window made visible.
        CellTransform ct = CellPlacementUtils.getCellTransform(null, bbox, creatorViewTransform);
        if (ct != null) {
            logger.info("Best initial cell transform = " + ct);
            cell.performFirstMove(ct);
        }
    }
}
