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

package org.jdesktop.wonderland.wfs.event;

import java.util.EventListener;

/**
 * The WFSListener is used to register interest for changes to a WFS. Several
 * actions may occur to the state of a cell:
 * <p>
 * 1. The attributes of a cell may change, in which case the cellAttributeUpdate()
 *    method on this interface is called.
 * 2. Children may be added to a cell, in which case the cellChildrenAdded()
 *    method on this interface is called.
 * 3. Children may be removed from a cell, in which case the cellChildrenRemoved()
 *    method on this interface is called.
 * 4. The cell itself may be removed, in which case the cellRemoved() method on
 *    this interface is called.
 * <p>
 * This event is delivered in its own thread to the listener. Note that since
 * other updates to the state of the WFS may happen before a listener receives
 * this event, it is not a guaranteed contract for the state of the WFS cell.
 * It is up to the listener to verify the state of the cell.
 * <p>
 * Note that events are delivered when the WFS is updated in memory and not
 * when the WFS is committed out to disk.
 * <p>
 * These methods pass back the WFSCell in question (via the WFSEvent object).
 * Note that upon cell removal, the WFSCell object is removed from the hierarchy
 * and is considered invalid and should be garbage collected.
 * <p>
 * Classes that implement this interface should never hold strong references
 * to the WFSCell class. In the case of cell deletion, implementations of this
 * interface should store the cell's name at the outset and any other useful
 * information from the cell it wants to access after deletion.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface WFSListener extends EventListener {
    /**
     * Called when the attribute file of a cell has changed in memory. If the
     * cell's attributes have not changed, but a thread calls WFSCell.setCellSetupUpdate(),
     * then this method will be called at that time.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellAttributeUpdate(WFSEvent event);
    
    /**
     * Called when children have been added to this cell.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellChildrenAdded(WFSEvent event);
    
    /**
     * Called when children have been removed from this cell.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellChildrenRemoved(WFSEvent event);
    
    /**
     * Called when this cell has been removed from the hierarchy.
     * 
     * @param cellEvent The WFSCellEvent object
     */
    public void cellRemoved(WFSEvent event);
}
