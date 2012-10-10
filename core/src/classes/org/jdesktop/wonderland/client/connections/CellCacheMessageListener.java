/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.connections;

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.utils.Observer;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.state.CellClientState;

/**
 * Listener interface for cell cache action messages
 */
@ExperimentalAPI
public interface CellCacheMessageListener extends Observer {

    /**
     * Load the cell and prepare it for use
     * @param cellID
     * @param className
     * @param computedWorldBounds
     * @param parentCellID
     * @param cellTransform
     * @param setup
     */
    public Cell loadCell(CellID cellID, String className, BoundingVolume localBounds, CellID parentCellID, CellTransform cellTransform, CellClientState setup, String cellName);

    /**
     * (Re)configures an existing cell with a new client state
     *
     * @param cellID The unique ID of the cell
     * @param clientState The new client state for the cell
     * @param cellName The (new) name of the cell
     */
    public void configureCell(CellID cellID, CellClientState clientState, String cellName);

    /**
     * Unload the cell. This removes the cell from memory but will leave
     * cell data cached on the client
     * @param cellID
     */
    public void unloadCell(CellID cellID);

    /**
     * Delete the cell and all its content from the client
     * @param cellID
     */
    public void deleteCell(CellID cellID);

    /**
     * Changes the parent of the cell.
     *
     * @param cellID The Cell ID of the Cell to move
     * @param parentCellID The Cell ID of the new parent
     * @param cellTransform The new local transform of the Cell
     */
    public void changeParent(CellID cellID, CellID parentCellID, CellTransform cellTransform);
    
}
