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

import com.jme.math.Vector3f;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.modules.palette.client.CellPalette;

/**
 * Handles data flavors of serialized CellServerState classes, registered with
 * the drag-and-drop manager when items are dragged from the Cell Palette into
 * the world.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellPaletteDataFlavorHandler implements DataFlavorHandlerSPI {

    private DataFlavor dataFlavor = new DataFlavor(CellServerState.class, "CellServerState");

    /**
     * @inheritDoc()
     */
    public DataFlavor[] getDataFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    /**
     * @inheritDoc()
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation) {
        try {
            // Fetch the CellServerState from the dropped transferable and
            // create an instance of the cell on the server
            CellServerState state = (CellServerState) transferable.getTransferData(dataFlavor);

            // Fetch the current transform from the view manager. Find the current
            // position of the camera and its look direction.
            ViewManager manager = ViewManager.getViewManager();
            Vector3f cameraPosition = manager.getCameraPosition(null);
            Vector3f cameraLookDirection = manager.getCameraLookDirection(null);

            // Compute the new vector away from the camera position to be a certain
            // number of scalar units away
            float lengthSquared = cameraLookDirection.lengthSquared();
            float factor = (CellPalette.NEW_CELL_DISTANCE * CellPalette.NEW_CELL_DISTANCE) / lengthSquared;
            Vector3f origin = cameraPosition.add(cameraLookDirection.mult(factor));

            // Create a position component that will set the initial origin
            PositionComponentServerState position = new PositionComponentServerState();
            position.setOrigin(new Origin(origin));
            state.addComponentServerState(position);

            // Send the message to the server
            WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
            CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
            CellCreateMessage msg = new CellCreateMessage(null, state);
            connection.send(msg);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(CellPaletteDataFlavorHandler.class.getName()).log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CellPaletteDataFlavorHandler.class.getName()).log(Level.WARNING, null, ex);
        }
    }

}
