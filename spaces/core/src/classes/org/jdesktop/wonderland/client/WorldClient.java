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
package org.jdesktop.wonderland.client;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.avatar.LocalAvatar;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellCacheConnection;
import org.jdesktop.wonderland.client.cell.CellManager;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author paulby
 */
public class WorldClient {
    private static final Logger logger = Logger.getLogger(WorldClient.class.getName());
    
    // properties
    private Properties props;
    
    // standard properties
    private static final String SERVER_NAME_PROP = "sgs.server";
    private static final String SERVER_PORT_PROP = "sgs.port";
    private static final String USER_NAME_PROP   = "cellboundsviewer.username";
    
    // default values
    private static final String SERVER_NAME_DEFAULT = "localhost";
    private static final String SERVER_PORT_DEFAULT = "1139";
    private static final String USER_NAME_DEFAULT   = "test";
   
    private CellClientSession session;
    
    private LocalAvatar localAvatar;
    
//    private Vector3f location = new Vector3f();
//    private static final float STEP = 2f;
    private Cache boundsPanel = new Cache();
    
    private JmeClientMain ui;

    
    /** Creates new form CellBoundsViewer */
    public WorldClient(String[] args) {
        // load properties from file
//        if (args.length == 1) {
//            props = loadProperties(args[0]);
//        } else {
//            props = loadProperties(null);
//        }
//   
//        String serverName = props.getProperty(SERVER_NAME_PROP,
//                                              SERVER_NAME_DEFAULT);
//        String serverPort = props.getProperty(SERVER_PORT_PROP,
//                                              SERVER_PORT_DEFAULT);
//        String userName   = props.getProperty(USER_NAME_PROP,
//                                              USER_NAME_DEFAULT);
//        
//        
//        WonderlandServerInfo server = new WonderlandServerInfo(serverName,
//                                                  Integer.parseInt(serverPort));
//        
//        LoginParameters loginParams = new LoginParameters(userName, 
//                                                          "test".toCharArray());
//        
//        
//        // create a session
//        session = new CellClientSession(server) {
//            @Override
//            protected CellCache createCellCache() {
//                getCellCacheConnection().addListener(boundsPanel);
//                return boundsPanel;
//            }
//        };
//        ClientContext3D.registerCellCache(boundsPanel, session);
//        
//        boundsPanel.setSession(session);
//        
//        localAvatar = session.getLocalAvatar();
//                
//        try {
//            session.login(loginParams);
//        } catch (LoginFailureException ex) {
//            logger.log(Level.SEVERE, "Login Failure", ex);
//        }
        
        // Initialize the user interface
        ui = new JmeClientMain(new String[0]);
    }
    
    
    class Cache implements CellCacheConnection.CellCacheMessageListener, CellCache {
        
        // BoundsPanel actually wraps the cacheImpl
        private CellCacheBasicImpl cacheImpl;
        private CellClientSession session;
        
        public Cache() {
        }
        
        public WonderlandSession getSession() {
            return session;
        }
        
        public void setSession(CellClientSession session) {
            this.session = session;
            
            // setup internal cache
            cacheImpl = new CellCacheBasicImpl(session, 
                                               session.getCellCacheConnection(), 
                                               session.getCellChannelConnection());
        }
        
        public Cell loadCell(CellID cellID, 
                String className, 
                BoundingVolume localBounds, 
                CellID parentCellID, 
                CellTransform cellTransform, 
                CellSetup setup,
                String cellName) {
            return cacheImpl.loadCell(cellID, 
                               className, 
                               localBounds, 
                               parentCellID, 
                               cellTransform, 
                               setup,
                               cellName);
        }

        public void unloadCell(CellID cellID) {
            cacheImpl.unloadCell(cellID);
        }

        public void deleteCell(CellID cellID) {
            cacheImpl.deleteCell(cellID);
        }

        /**
         * The cell has moved. If it's an movable cell the transform has already
         * been updated, so just process the cache update. If its not a
         * movable cell then update the transform and cache.
         * 
         * @param cellID
         * @param cellTransform
         */
        public void moveCell(CellID cellID, CellTransform cellTransform) {
            cacheImpl.moveCell(cellID, cellTransform);
        }
        
        /*************************************************
         * CellCache implementation
         *************************************************/
        public Cell getCell(CellID cellId) {
            return cacheImpl.getCell(cellId);
        }

        
        /*************************************************
         * End CellCache implementation
         *************************************************/
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WorldClient(args);
            }
        });
    }
    
    private static Properties loadProperties(String fileName) {
        // start with the system properties
        Properties props = new Properties(System.getProperties());
    
        // load the given file
        if (fileName != null) {
            try {
                props.load(new FileInputStream(fileName));
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Error reading properties from " +
                           fileName, ioe);
            }
        }
        
        return props;
    }
   
}
