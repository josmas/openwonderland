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

package org.jdesktop.wonderland.wfs.loader;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;


/**
 * The WFSReloadPhase class represents a point during a reload of the cells.
 * This is used to keep track of where the WFS reload mechanism is, so that
 * upon failure, the reload state can be recovered and memory is not leaked.
 * <p>
 * The phases of a WFS reload are defined as follows:
 * <p>
 * PHASE 0: NONE. The WFS Reloader mechanism is in the NONE phase if no reload
 * is going on.
 * <p>
 * PHASE 1: FETCH. During the FETCH phase, the WFS Reloader queries the WFS web
 * service for the list of cells that currently exist and their last modified
 * date values.
 * <p>
 * PHASE 2: COMPARE. During the COMPARE phase, the WFS Reloader compares the
 * list of cells within the WFS to the list of cells currently in-world and
 * produces three lists: a list of new cells to be added to the world, a list
 * of cells to be removed from the world, and a list of cells to be modified.
 * <p>
 * PHASE 3: REMOVE. During the REMOVE phase, the WFS Reloader removes cells
 * from the world that no longer exist in the WFS.
 * <p>
 * PHASE 4: MODIFY. During the MODIFY phase, the WFS Reloader modifies all
 * cells by replacing the existing cells with the new cells with modified
 * states.
 * <p>
 * PHASE 5: ADD. During the ADD phase, the WFS Reloader adds all new cells to
 * the world from the cell objects it has already created.
 * <p>
 * Each individual update to the cell object structure is quick enough to happen
 * within a single Darkstar transaction. If the Darkstar node on which the WFS
 * Reloader runs crashes during one of the phases, upon restart, the WFS
 * Reloader recovers gracefully. If the WFS Reloader is in Phases 1 or 2
 * during a crash, upon restart, it erases all temporary work up to that point
 * during the phase and restarts the phase. If the WFS Reloader crashes during
 * phase 3, 4, or 5, it picks up where it left off.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSReloadPhase implements ManagedObject, Serializable {

    /* An enumeration listing all of the reload phases */
    public enum Phase { NONE, FETCH, COMPARE, REMOVE, MODIFY, ADD }
    
    /* The current phase */
    private Phase phase = Phase.NONE;

    
    /** Default constructor */
    public WFSReloadPhase() {
    }
    
    /**
     * Sets the current reload phase.
     * 
     * @param phase The current reload phase
     */
    public void setPhase(Phase phase) {
        this.phase = phase;
    }
    
    /**
     * Returns the current reload phase.
     * 
     * @return The reload phase
     */
    public Phase getPhase() {
        return this.phase;
    }
}
