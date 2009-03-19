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
package org.jdesktop.wonderland.server.cell;

import com.jme.math.Vector3f;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;
import org.jdesktop.wonderland.common.cell.messages.CellDuplicateMessage;
import org.jdesktop.wonderland.common.cell.messages.CellEditMessage;
import org.jdesktop.wonderland.common.cell.messages.CellEditMessage.EditType;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;

/**
 * Handles CellEditMessages sent by the Wonderland client
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
class CellEditConnectionHandler implements ClientConnectionHandler, Serializable {

    public ConnectionType getConnectionType() {
        return CellEditConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        // ignore
    }

    public void clientConnected(WonderlandClientSender sender,
            WonderlandClientID clientID, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,
            WonderlandClientID clientID) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, Message message) {
        
        Logger logger = Logger.getLogger(CellEditConnectionHandler.class.getName());

        CellEditMessage editMessage = (CellEditMessage)message;
        if (editMessage.getEditType() == EditType.CREATE_CELL) {

            // The create message contains a setup class of the cell setup
            // information. Simply parse this stream, which will result in a
            // setup class of the property type.
            CellServerState setup = ((CellCreateMessage)editMessage).getCellSetup();
            
            // Fetch the server-side cell class name and create the cell
            String className = setup.getServerClassName();
            logger.warning("Attempting to load cell mo: " + className);
            CellMO cellMO = CellMOFactory.loadCellMO(className);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to load cell MO: " + className );
                return;
            }
            
            /* Call the cell's setup method */
            try {
                cellMO.setServerState(setup);
                WonderlandContext.getCellManager().insertCellInWorld(cellMO);
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, "Error setting up new cell " +
                        cellMO.getName() + " of type " +
                        cellMO.getClass() + ", it does not implement " +
                        "BeanSetupMO.", cce);
                return;
            } catch (MultipleParentException excp) {
                logger.log(Level.WARNING, "Error adding new cell " + cellMO.getName() +
                        " of type " + cellMO.getClass() + ", has multiple parents", excp);
            }
        }
        else if (editMessage.getEditType() == EditType.DELETE_CELL) {
            // Find the cell object given the ID of the cell. If the ID is
            // invalid, we just log an error and return.
            CellID cellID = ((CellDeleteMessage)editMessage).getCellID();
            CellMO cellMO = CellManagerMO.getCell(cellID);
            if (cellMO == null) {
                logger.warning("No cell found to delete with cell id " + cellID);
                return;
            }

            // Find out the parent of the cell. This may be null if the cell is
            // at the world root. This determines from where to remove the cell
            CellMO parentMO = cellMO.getParent();
            if (parentMO != null) {
                parentMO.removeChild(cellMO);
            }
            else {
                CellManagerMO.getCellManager().removeCellFromWorld(cellMO);
            }
        }
        else if (editMessage.getEditType() == EditType.DUPLICATE_CELL) {
            // Find the cell object given the ID of the cell. If the ID is
            // invalid, we just log an error and return.
            CellID cellID = ((CellDuplicateMessage)editMessage).getCellID();
            CellMO cellMO = CellManagerMO.getCell(cellID);
            if (cellMO == null) {
                logger.warning("No cell found to duplicate with cell id " + cellID);
                return;
            }
            CellMO parentCellMO = cellMO.getParent();

            // We need to fetch the current state of the cell from the cell we
            // wish to duplicate. We also need the name of the server-side cell
            // class
            CellServerState state = cellMO.getServerState(null);
            String className = state.getServerClassName();

            // Attempt to create the cell using the cell factory and the class
            // name of the server-side cell.
            CellMO newCellMO = CellMOFactory.loadCellMO(className);
            if (newCellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to duplicate cell MO: " + className);
                return;
            }

            // We want to modify the position of the new cell slight, so we
            // offset the position by (1, 1, 1).
            PositionComponentServerState position = (PositionComponentServerState)state.getComponentServerState(PositionComponentServerState.class);
            if (position == null) {
                logger.warning("Unable to determine the position of the cell " +
                        "to duplicate with id " + cellID);
                return;
            }
            Vector3f offset = new Vector3f(1, 1, 1);
            Origin origin = position.getOrigin();
            Origin newOrigin = new Origin(offset.add(new Vector3f((float)origin.x, (float)origin.y, (float)origin.z)));
            position.setOrigin(newOrigin);
            state.addComponentServerState(position);

            // Set the state of the new cell and add it to the same parent as
            // the old cell. If the old parent cell is null, we just insert it
            // as root.
            newCellMO.setServerState(state);
            try {
                if (parentCellMO == null) {
                    WonderlandContext.getCellManager().insertCellInWorld(newCellMO);
                }
                else {
                    parentCellMO.addChild(newCellMO);
                }
            } catch (MultipleParentException excp) {
                logger.log(Level.WARNING, "Error duplicating cell " +
                        newCellMO.getName() + " of type " + newCellMO.getClass() +
                        ", has multiple parents", excp);
            }
        }
    }
    
    /**
     * Returns the base URL of the web server.
     */
    private static URL getWebServerURL() throws MalformedURLException {
        return new URL(System.getProperty("wonderland.web.server.url"));
    }
    
    /**
     * Given a base URL of the server (e.g. http://localhost:8080) returns
     * the server name and port as a string (e.g. localhost:8080). Returns null
     * if the host name is not present.
     * 
     * @return <server name>:<port>
     * @throw MalformedURLException If the given string URL is invalid
     */
    private static String getServerFromURL(URL serverURL) {
        String host = serverURL.getHost();
        int port = serverURL.getPort();
        
        if (host == null) {
            return null;
        }
        else if (port == -1) {
            return host;
        }
        return host + ":" + port;
    }
}
