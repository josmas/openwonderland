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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.ClientSession;
import java.io.Serializable;
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
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.setup.CellExtensionTypeFactory;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;

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
            ClientSession session, Properties properties) {
        // ignore
    }

    public void clientDisconnected(WonderlandClientSender sender,
            ClientSession session) {
        // ignore
    }

    public void messageReceived(WonderlandClientSender sender,
            ClientSession session, Message message) {
        
        Logger logger = Logger.getLogger(CellEditConnectionHandler.class.getName());
        
        // Find the appropriate parent cell given in the message
        CellEditMessage editMessage = (CellEditMessage) message;
        if (editMessage.getEditType() == EditType.CREATE_CELL) {
            // Fetch an instance of the cell setup class
            String uri = ((CellCreateMessage)editMessage).getAssetURI();
            String extension = uri.substring(uri.lastIndexOf(".") + 1);
            logger.warning("URI " + uri + " EXT " + extension);
            BasicCellSetup setup = CellExtensionTypeFactory.getCellSetup(extension, uri);
            if (setup == null) {
                logger.warning("[EDIT] Unable get cell setup for " + uri);
                return;
            }
            String className = setup.getServerClassName();
            logger.warning("[EDIT] Class name " + className);

            CellMO cellMO = CellMOFactory.loadCellMO(className);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to load cell MO: " + className + " for " + uri);
                return;
            }

            /* Call the cell's setup method */
            try {
                ((BeanSetupMO) cellMO).setupCell(setup);
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
}
