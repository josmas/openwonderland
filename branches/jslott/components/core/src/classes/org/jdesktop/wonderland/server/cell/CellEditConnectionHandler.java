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
import org.jdesktop.wonderland.common.cell.messages.CellEditMessage;
import org.jdesktop.wonderland.common.cell.messages.CellEditMessage.EditType;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
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
            CellID cellID = ((CellDeleteMessage)editMessage).getCellID();
            CellMO cellMO = CellManagerMO.getCell(cellID);
            CellMO parentMO = cellMO.getParent();
            parentMO.removeChild(cellMO);
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
