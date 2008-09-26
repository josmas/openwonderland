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

package org.jdesktop.wonderland.wfs.loader;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.wfs.cell.CellMOFactory;
import org.jdesktop.wonderland.wfs.utils.WFSCellList;
import org.jdesktop.wonderland.wfs.utils.WFSCellList.Cell;
import org.jdesktop.wonderland.wfs.utils.WFSCellMap;
import org.jdesktop.wonderland.wfs.utils.WFSLoaderDefs;
import org.jdesktop.wonderland.wfs.utils.WFSLoaderUtils;
import org.jdesktop.wonderland.wfs.utils.WFSRoots;



/**
 * The WFSLoader class is responsible for loading a WFS from the HTTP-based
 * WFS service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSLoader {
    /* The logger for the wfs loader */
    private static final Logger logger = Logger.getLogger(WFSLoader.class.getName());
    
    /* Conatins a map of canonical cell names in WFS to cell objects */
    private WFSCellMap<ManagedReference<CellMO>> cellMOMap = new WFSCellMap();
    
    /* Contains a map of canonical cell names in WFS to last modified dates */
    private WFSCellMap<Long> cellModifiedMap = new WFSCellMap();
    
    /** Default Constructor */
    public WFSLoader() {
    }
    
    /**
     * Returns the error logger associated with this class.
     * 
     * @return The error logger
     */
    public static Logger getLogger() {
        return WFSLoader.logger;
    }
    
    /**
     * Returns the parent cell MO class, using the canonical name of its parent
     * cell given during its creation.
     */
    private ManagedReference<CellMO> getParentCellReference(String canonicalName) {
        /*
         * Try to fetch the parent from the cache of cells and their canonical
         * names. If the name does not exist, then we assume it is the root
         * cell and return null.
         */
        return this.cellMOMap.get(canonicalName);
    }

    /**
     * Loads all of the WFSs into the world. Each "root" WFS is based in its
     * own WFSCellMO class
     */
    public void load() {
        /* First fetch all of the individual WFSs there are in the system. */
        WFSRoots wfsRoots = WFSLoaderUtils.getWFSRoots();
        if (wfsRoots == null) {
            logger.info("WFSLoader: did not find any valid WFS roots");
            return;
        }
        
        /*
         * Iterator through all of the roots. For each, create a new WFSCellMO
         * class and load in its cells. Just take the first one for now!
         */
        String rootName = wfsRoots.getRoots()[0];
//        for (String rootName : wfsRoots.getRoots()) {
//            logger.info("WFSLoader: loading the WFS root " + rootName);
//        
//            /*
//             * Attempt to create a new MO based upon the WFS root. We need to setup
//             * some basic properties about the cell by hand (e.g.name).
//             */
//            WFSCellMO mo = new WFSCellMO(rootName);
//            mo.setName(rootName);
//
//            try {
//                AppContext.getDataManager().setBinding(mo.getBindingName(), mo);
//                WonderlandContext.getCellManager().insertCellInWorld(mo);
//            } catch (java.lang.Exception excp) {
//                logger.severe("Unable to load WFS into world: " + excp.toString());
//            }
//            logger.info("WFSLoader: WFSCellMO (ID=" + mo.getCellID().toString() +
//                    ") name=" + rootName);
            
            /* Load in the cells in the WFS based upon this root */
            this.loadWFSRoot(rootName);
//        }
    }
    
    /**
     * Loads a WFS root into the world, based in the given WFSCellMO with a
     * unique root name.
     * 
     * @param rootName The unique root name of the WFS
     */
    private void loadWFSRoot(String rootName) {
        /* A queue (last-in, first-out) containing a list of cell to search down */
        LinkedList<WFSCellList> children = new LinkedList<WFSCellList>();

        /* Find the children in the top-level directory and go! */
        WFSCellList dir = WFSLoaderUtils.getWFSRootChildren(rootName);
        if (dir == null) {
            /* Log an error and return, though this should never happen */
            logger.warning("WFSLoader: did not find root directory for wfs " + rootName);
            return;
        }
        children.addFirst(dir);
        
        /*
         * Loop until the 'children' Queue is entirely empty, which means that
         * we have loaded all of the cells and have searched all possible sub-
         * directories. The loadCells() method will add entries to children as
         * needed.
         */
        while (children.isEmpty() == false) {
            /* Fetch and remove the first on the list and load */
            WFSCellList childdir = children.removeFirst();
            if (childdir == null) {
                /* Log an error and continue, though this should never happen */
                logger.warning("WFSLoader: could not fetch child dir in WFS " + rootName);
                continue;
            }
            logger.info("WFSLoader: processing children in " + childdir.getRelativePath());
            
            /* Recursively load the cells for this child */
            this.loadCells(rootName, childdir, children);
        }
        
        /*
         * Once we are done, we need to place the mapping of cells into the
         * data manager
         */
        DataManager dm = AppContext.getDataManager();
        dm.setBinding(WFSLoaderDefs.WFS_OBJECT_MAP, this.cellMOMap);
        dm.setBinding(WFSLoaderDefs.WFS_MODIFIED_MAP, this.cellModifiedMap);

        /*
         * Create the reload phase and initial it to none.
         */
        WFSReloadPhase phase = new WFSReloadPhase();
        dm.setBinding(WFSLoaderDefs.WFS_RELOAD_PHASE, phase);
    }
    
    /**
     * Recurisvely loads cells from a given child directory (dir) in the WFS
     * given by root. If this child has any children directories, then add
     * to the children parameter.
     * 
     * @param root The root directory of the WFS being loaded
     * @param dir The current directory of children to load
     * @param children A list of child directories remaining to be loaded
     */
    private void loadCells(String root, WFSCellList dir, LinkedList<WFSCellList> children) {
        /*
         * Fetch an array of the names of the child cells. Check this is not
         * null, although this getChildren() should return an empty array
         * instead.
         */
        Cell childs[] = dir.getChildren();
        if (childs == null) {
            logger.warning("WSLoader: could not read children in WFS " + root);
            return;
        }
        
        /*
         * Loop throuch each of the child names and attempt to create a cell
         * based upon it. Then update the cell map to indicate that the object
         * exists and the last time it was modified on disk.
         */
        for (Cell child : childs) {
            logger.info("WFSLoader: processing child " + child.name);
            
            /*
             * Fetch the relative path of the parent. Check if null, although
             * this should never be the case. Then fetch the parent cell object.
             */
            String relativePath = dir.getRelativePath();
            if (relativePath == null) {
                logger.warning("WFSLoader: null relative path for cell " + child.name);
                continue;
            }
            ManagedReference<CellMO> parentRef = this.getParentCellReference(relativePath);
            
            /*
             * Download and parse the cell configuration information. Create a
             * new cell based upon the information.
             */
            BasicCellSetup setup = WFSLoaderUtils.getWFSCell(root, relativePath, child.name);
            if (setup == null) {
                logger.info("WFSLoader: unable to read cell setup info " + relativePath + "/" + child.name);
                continue;
            }
            logger.info(setup.toString());
            
            /*
             * If the cell is at the root, then the relative path will be "/"
             * and we do not want to prepend it to the cell path.
             */
            String cellPath = relativePath + "/" + child.name;
            if (relativePath.compareTo("") == 0) {
                cellPath = child.name;
            }

            /*
             * Create the cell and pass it the setup information
             */
            String className = setup.getServerClassName();
            CellMO cellMO = CellMOFactory.loadCellMO(className);
            if (cellMO == null) {
                /* Log a warning and move onto the next cell */
                logger.warning("Unable to load cell MO: " + className);
                continue;
            }
                
            /* Call the cell's setup method */
            try {
                ((BeanSetupMO)cellMO).setupCell(setup);
            } catch (ClassCastException cce) {
                logger.log(Level.WARNING, "Error setting up new cell " +
                        cellMO.getName() + " of type " +
                        cellMO.getClass() + ", it does not implement " +
                        "BeanSetupMO.", cce);
                continue;
            }
            
            /*
             * Add the child to the cell hierarchy. If the cell has no parent,
             * then we insert it directly into the world
             */
            try {
                if (parentRef == null) {
                    WonderlandContext.getCellManager().insertCellInWorld(cellMO);
                }
                else {
                    logger.info("WFSLoader: Adding child (ID=" + cellMO.getCellID().toString() +
                            ") to parent (ID=" + parentRef.get().getCellID().toString() + ")");
                    parentRef.get().addChild(cellMO);
                    logger.info("WFSLoader: Parent Cell ID=" + cellMO.getParent().getCellID().toString());
                    Collection<ManagedReference<CellMO>> refs = cellMO.getParent().getAllChildrenRefs();
                    Iterator<ManagedReference<CellMO>> it = refs.iterator();
                    while (it.hasNext() == true) {
                        logger.info("WFSLoader: Child Cell=" + it.next().get().getCellID().toString());
                    }
                    logger.info("WFSLoader: Cell Live: " + cellMO.isLive());
                }
            } catch (MultipleParentException excp) {
                logger.log(Level.WARNING, "Attempting to add a new cell with " +
                        "multiple parents: " + cellMO.getName());
                continue;
            }
            
            /*
             * Since we are loading cells for the first time, we put the cell
             * in both the cell object and last modified reference map. We
             * add the cell to its parent. If the parent is null, we add to the
             * root.
             */
            ManagedReference<CellMO> cellRef = AppContext.getDataManager().createReference(cellMO);
            this.cellMOMap.put(cellPath, cellRef);
            this.cellModifiedMap.put(cellPath, child.lastModified);
            logger.info("WFSLoader: putting " + cellPath + " (ID=" + cellMO.getCellID().toString() + ") into map with " + child.lastModified);
            logger.info(setup.toString());
            
            /*
             * See if the cell has any children and add to the linked list.
             */
            WFSCellList newChildren = WFSLoaderUtils.getWFSChildren(root, cellPath);
            if (newChildren != null) {
                children.addLast(newChildren);
            }
        }
    }
}
