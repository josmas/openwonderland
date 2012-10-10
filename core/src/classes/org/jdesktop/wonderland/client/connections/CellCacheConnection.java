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
package org.jdesktop.wonderland.client.connections;

import com.jme.bounding.BoundingVolume;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.ClientView;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.client.comms.BaseConnection;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.utils.Observable;
import org.jdesktop.wonderland.client.utils.Observer;
import org.jdesktop.wonderland.common.cell.CellCacheConnectionType;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.ViewCreateResponseMessage;
import org.jdesktop.wonderland.common.cell.messages.CellHierarchyMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageList;

/**
 * Handler for Cell cache information
 * @author jkaplan
 */
@ExperimentalAPI
public class CellCacheConnection extends BaseConnection<CellCacheMessageListener> {
    private static final Logger logger = Logger.getLogger(CellCacheConnection.class.getName());
    
//    private ArrayList<CellCacheMessageListener> listeners = new ArrayList();
//    private Observable<CellCacheMessageListener> observable = new Observable<CellCacheMessageListener>();
    private ClientView clientView;
    private CellID viewCellID = null;
    
    public CellCacheConnection() {
        this.clientView = ClientContextJME.getClientView();
    }
    
    /**
     * Get the type of client
     * @return CellClientType.CELL_CLIENT_TYPE
     */
    public ConnectionType getConnectionType() {
        return CellCacheConnectionType.CLIENT_TYPE;
    }

//    /**
//     * Add a listener for cell cache actions. This should be called during setup
//     * not once the system is running
//     * @param listener
//     */
//    public void addObserver(CellCacheMessageListener listener) {
//        listeners.add(listener);
//        observable.addObserver(listener);
//    }
    
    private ViewCreateResponseMessage registerView(String viewID) {
        try {
            ViewCreateResponseMessage response = 
                    (ViewCreateResponseMessage)sendAndWait(
                            CellHierarchyMessage.newSetAvatarMessage(viewID));

            return response;
        } catch (InterruptedException ex) {
            Logger.getLogger(CellCacheConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Handle a message from the server
     * @param message the message to handle
     */
    public void handleMessage(Message message) {
        if (message instanceof MessageList) {
            List<Message> list = ((MessageList)message).getMessages();
            for(Message m : list)
                handleMessage(m);
            
            observable.fire("loading-finished", true);
            logger.warning("LOADING-FINISHED!");
            return;
        }
        
        if (!(message instanceof CellHierarchyMessage))
            throw new RuntimeException("Unexpected message type "+message.getClass().getName());
        
        CellHierarchyMessage msg = (CellHierarchyMessage)message;
        switch(msg.getActionType()) {
            case LOAD_CELL :
//                for(CellCacheMessageListener l : listeners) {
//                    l.loadCell(msg.getCellID(),
//                                msg.getCellClassName(),
//                                msg.getLocalBounds(),
//                                msg.getParentID(),
//                                msg.getCellTransform(),
//                                msg.getSetupData(),
//                                msg.getCellName()
//                                );
//                    observable.fire("load-cell", msg);
//                }
                observable.fire("load-cell", msg);
//                if (viewCellID!=null && viewCellID.equals(msg.getCellID())) {
//
//                    clientView.viewCellConfigured(viewCellID);
//                    // We only need notification once
//                    viewCellID = null;
//                }
                break;

            case CONFIGURE_CELL:
                // Update recieving a "configure cell" message, dispatch to all
                // of the listeners. A "configure" message simply send a new
                // client cell state to an already existing cell.
//                for (CellCacheMessageListener l : listeners) {
//                    l.configureCell(msg.getCellID(), msg.getSetupData(), msg.getCellName());
//                }
                observable.fire("configure-cell", msg);
                break;
            case UNLOAD_CELL :
//                for(CellCacheMessageListener l : listeners) {
//                    l.unloadCell(msg.getCellID());
//                }
                observable.fire("unload-cell", msg);
                break;
            case DELETE_CELL :
//                for(CellCacheMessageListener l : listeners) {
//                    l.deleteCell(msg.getCellID());
//                }
                observable.fire("delete-cell", msg);
                break;
            case CHANGE_PARENT:
                // Unused at the moment, CellEditConnectionHandler processes reparenting
//                for(CellCacheMessageListener l : listeners) {
//                    l.changeParent(msg.getCellID(), msg.getParentID(), msg.getCellTransform());
//                }
                break;
            default :
                logger.warning("Message type not implemented "+msg.getActionType());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(WonderlandSession session, Properties props)
            throws ConnectionFailureException
    {
        super.connect(session, props);
        ViewCreateResponseMessage msg = registerView(clientView.getViewID());
        clientView.serverInitialized(msg);
        viewCellID = msg.getViewCellID();
    }

    public CellID getViewCellID() {
        return viewCellID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnected() {
        // remove any action listeners
//        listeners.clear();
        observable.clear();
        
      
    }
    
}
