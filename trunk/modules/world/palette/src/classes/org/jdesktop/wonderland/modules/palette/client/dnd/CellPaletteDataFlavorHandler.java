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
package org.jdesktop.wonderland.modules.palette.client.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.utils.CellCreationException;
import org.jdesktop.wonderland.client.cell.utils.CellUtils;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.modules.palette.client.CellPalette;

/**
 * Handles data flavors of serialized CellServerState classes, registered with
 * the drag-and-drop manager when items are dragged from the Cell Palette into
 * the world.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellPaletteDataFlavorHandler implements DataFlavorHandlerSPI {

    private final DataFlavor dataFlavor = new DataFlavor(CellServerState.class, "CellServerState");

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    /**
     * @inheritDoc()
     */
    public boolean accept(Transferable transferable, DataFlavor dataFlavor) {
       // Just accept everything sent out way
        return true;
    }

    /**
     * @inheritDoc()
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {
        try {
            // Fetch the CellServerState from the dropped transferable and
            // create an instance of the cell on the server
            CellServerState state = (CellServerState) transferable.getTransferData(dataFlavor);

            // Create the new cell at a distance away from the avatar
            CellUtils.createCell(state);

        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(CellPaletteDataFlavorHandler.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CellPaletteDataFlavorHandler.class.getName()).log(Level.WARNING, null, ex);
        } catch (CellCreationException ex) {
            Logger.getLogger(CellPaletteDataFlavorHandler.class.getName()).log(Level.WARNING, null, ex);
        }
    }
}
