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
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellCacheBasicImpl;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.WonderlandServerInfo;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.modules.ModulePluginList;
import org.jdesktop.wonderland.client.modules.ModuleUtils;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import sun.misc.Service;


/**
 * Manage the connection between this client and the wonderland server
 * 
 * TODO RENAME, there must be a better name for this class !
 * 
 * @author paulby
 */
public class ClientManager {
    private static final Logger logger = Logger.getLogger(ClientManager.class.getName());
    
    private CellClientSession session;
    
    private LocalAvatar localAvatar;
    
//    private Vector3f location = new Vector3f();
//    private static final float STEP = 2f;
    private JmeCellCache cellCache = null;
    
//    private Vector3f previousPos = new Vector3f();
//    private Quaternion previousRot = new Quaternion();
    
    // the classloader to use with this manager
    private ClassLoader loader;
    
    public ClientManager(String serverName, int serverPort, String userName) {
        
        WonderlandServerInfo server = new WonderlandServerInfo(serverName,
                                                  serverPort);
        
        LoginParameters loginParams = new LoginParameters(userName, 
                                                          "test".toCharArray());
        
        
        // setup a classloader with the module jars
        loader = setupClassLoader();
        
        // load any client plugins from that class loader
        Iterator<ClientPlugin> it = Service.providers(ClientPlugin.class,
                                                      loader);
        while (it.hasNext()) {
            ClientPlugin plugin = it.next(); 
            plugin.initialize();
        }
        
        // create a session
        session = new CellClientSession(server, loader) {
            // createCellCache is called in the constructor fo CellClientSession
            // so the cellCache will be set before we proceed
            @Override
            protected CellCache createCellCache() {
                cellCache = new JmeCellCache(this);  // this session
                getCellCacheConnection().addListener(cellCache);
                return cellCache;
            }
        };
        ClientContextJME.getWonderlandSessionManager().registerSession(session);
                   
        localAvatar = session.getLocalAvatar();
                
        try {
            session.login(loginParams);
        } catch (LoginFailureException ex) {
            logger.log(Level.SEVERE, "Login Failure", ex);
        }
        
    }
    
    private ClassLoader setupClassLoader() {
        ModulePluginList list = ModuleUtils.fetchPluginJars();
        List<URL> urls = new ArrayList<URL>();
        
        for (String uri : list.getJarURIs()) {
            try {
                urls.add(new URL(uri));
            } catch (Exception excp) {
                excp.printStackTrace();
           }
        }
        
        return new URLClassLoader(urls.toArray(new URL[0]));
    }
    
//    void nodeMoved(Node node) {
//        if (node instanceof CameraNode) {
//            Vector3f v3f = node.getWorldTranslation();
//            Quaternion rot = node.getWorldRotation();
//            if (!previousPos.equals(v3f) || !previousRot.equals(rot)) {
//                localAvatar.localMoveRequest(v3f, rot);
//                previousPos.set(v3f);
//                previousRot.set(rot);
//            }
//        }
//    }
    
    class JmeCellCache extends CellCacheBasicImpl {
                
        public JmeCellCache(CellClientSession session) {
            super(session, loader,
                  session.getCellCacheConnection(), 
                  session.getCellChannelConnection());
        }
        
        @Override
        public Cell loadCell(CellID cellID, 
                String className, 
                BoundingVolume localBounds, 
                CellID parentCellID, 
                CellTransform cellTransform, 
                CellConfig setup,
                String cellName) {
            Cell ret = super.loadCell(cellID, 
                               className, 
                               localBounds, 
                               parentCellID, 
                               cellTransform, 
                               setup,
                               cellName);
//            logger.warning("Loaded Cell "+ret.getClass().getName());
            
            CellRenderer rend = ret.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (ret!=null && rend!=null) {
                if (rend instanceof CellRendererJME) {
                    Entity parentEntity= findParentEntity(ret.getParent());
                    Entity thisEntity = ((CellRendererJME)rend).getEntity();
                    
                    // TODO When subentities work uncomment this if test
                    if (parentEntity!=null)
                        parentEntity.addEntity(thisEntity);
                    else
                        JmeClientMain.getWorldManager().addEntity(thisEntity);
                    
                    if (parentEntity!=null && thisEntity!=null) {                        
                        RenderComponent parentRendComp = (RenderComponent) parentEntity.getComponent(RenderComponent.class);
                        RenderComponent thisRendComp = (RenderComponent)thisEntity.getComponent(RenderComponent.class);
                        if (parentRendComp!=null && parentRendComp.getSceneRoot()!=null && thisRendComp!=null) {
                            thisRendComp.setAttachPoint(parentRendComp.getSceneRoot());
                        }
                    }
                    
                } else
                    logger.warning("Unexpected renderer class "+rend.getClass().getName());
            } else {
                logger.info("No Entity for Cell "+ret.getClass().getName());
            }
            return ret;
        }

        @Override
        public void unloadCell(CellID cellID) {
            Cell cell = getCell(cellID);
            CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (cell!=null && rend!=null) {
                if (rend instanceof CellRendererJME)
                    JmeClientMain.getWorldManager().removeEntity(((CellRendererJME)rend).getEntity());
                else
                    logger.warning("Unexpected renderer class "+rend.getClass().getName());
            }
            super.unloadCell(cellID);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setViewCell(ViewCell viewCell) {
            super.setViewCell(viewCell);
            ClientContextJME.getViewManager().attach(viewCell);
        }
        
        /**
         * Traverse up the cell hierarchy and return the first Entity
         * @param cell
         * @return
         */
        private Entity findParentEntity(Cell cell) {
            if (cell==null)
                return null;
            
            CellRenderer rend = cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
            if (cell!=null && rend!=null) {
                if (rend instanceof CellRendererJME) {
//                    System.out.println("FOUND PARENT ENTITY on CELL "+cell.getName());
                    return ((CellRendererJME)rend).getEntity();
                }
            }
            
            return findParentEntity(cell.getParent());
        }
    }
    

   
}
