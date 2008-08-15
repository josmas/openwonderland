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
package org.jdesktop.wonderland.client.jme;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import com.jme.scene.Node;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.ClientContext;
import org.jdesktop.wonderland.client.avatar.LocalAvatar;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellCacheConnection;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;


/**
 *
 * @author paulby
 */
public class ClientManager {
    private static final Logger logger = Logger.getLogger(ClientManager.class.getName());
    
    // properties
    private Properties props;
    
    // standard properties
    private static final String SERVER_NAME_PROP = "sgs.server";
    private static final String SERVER_PORT_PROP = "sgs.port";
    private static final String USER_NAME_PROP   = "cellboundsviewer.username";
    
    // default values
    private static final String SERVER_NAME_DEFAULT = "localhost";
    private static final String SERVER_PORT_DEFAULT = "1139";
    private static final String USER_NAME_DEFAULT   = "jmetest";
   
    private CellClientSession session;
    
    private LocalAvatar localAvatar;
    
//    private Vector3f location = new Vector3f();
//    private static final float STEP = 2f;
    private Cache boundsPanel = new Cache();
    
    private Vector3f previousPos = new Vector3f();
    
    public ClientManager() {
        props = loadProperties("run.properties");
   
        String serverName = props.getProperty(SERVER_NAME_PROP,
                                              SERVER_NAME_DEFAULT);
        String serverPort = props.getProperty(SERVER_PORT_PROP,
                                              SERVER_PORT_DEFAULT);
        String userName   = props.getProperty(USER_NAME_PROP,
                                              USER_NAME_DEFAULT);
        
        
        WonderlandServerInfo server = new WonderlandServerInfo(serverName,
                                                  Integer.parseInt(serverPort));
        
        LoginParameters loginParams = new LoginParameters(userName, 
                                                          "test".toCharArray());
        
        
        // create a session
        session = new CellClientSession(server) {
            @Override
            protected CellCache createCellCache() {
                getCellCacheConnection().addListener(boundsPanel);
                return boundsPanel;
            }
        };
        ClientContext.registerCellCache(boundsPanel, session);
        
        boundsPanel.setSession(session);
        
        localAvatar = session.getLocalAvatar();
                
        try {
            session.login(loginParams);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login Failure", ex);
        }
        
    }
    
    void nodeMoved(Node node) {
        if (node instanceof CameraNode) {
            Vector3f v3f = node.getWorldTranslation();
            if (!previousPos.equals(v3f)) {
                localAvatar.localMoveRequest(v3f, null);
                previousPos.set(v3f);
            }
        }
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
            Cell ret = cacheImpl.loadCell(cellID, 
                               className, 
                               localBounds, 
                               parentCellID, 
                               cellTransform, 
                               setup,
                               cellName);
            logger.warning("Loaded Cell "+ret.getClass().getName());
            
            CellRenderer rend = ret.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (ret!=null && rend!=null) {
                logger.warning("Got entity "+rend);
                if (rend instanceof CellRendererJME)
                    JmeClientMain.getWorldManager().addEntity(((CellRendererJME)rend).getEntity());
                else
                    logger.warning("Unexpected renderer class "+rend.getClass().getName());
            } else {
                logger.warning("No Entity for Cell "+ret.getClass().getName());
            }
            return ret;
        }

        public void unloadCell(CellID cellID) {
            Cell cell = cacheImpl.getCell(cellID);
            CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (cell!=null && rend!=null) {
                if (rend instanceof CellRendererJME)
                    JmeClientMain.getWorldManager().removeEntity(((CellRendererJME)rend).getEntity());
                else
                    logger.warning("Unexpected renderer class "+rend.getClass().getName());
            }
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
        
        
        private Entity createCellEntity(CellID cellID) {
            Entity entity = new Entity("Cell_"+cellID);
            
            Cell cell = getCell(cellID);
             
            
            return entity;
        }
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
