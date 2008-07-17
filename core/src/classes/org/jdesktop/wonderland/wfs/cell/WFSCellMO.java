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
package org.jdesktop.wonderland.wfs.cell;

import com.jme.bounding.BoundingBox;
import com.jme.math.Vector3f;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.MovableCellMO;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;
import org.jdesktop.wonderland.server.setup.CellMOSetup;
import org.jdesktop.wonderland.wfs.InvalidWFSCellException;
import org.jdesktop.wonderland.wfs.InvalidWFSException;
import org.jdesktop.wonderland.wfs.WFS;
import org.jdesktop.wonderland.wfs.WFSCell;
import org.jdesktop.wonderland.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.wfs.WFSFactory;

/**
 * The WFSCellGLO class is a cell that represents a portion of the world which
 * was loaded from disk. The hierarchy of files and directories on disk
 * represent the structure of the world. This class traverses the directory
 * structure and loads the cells defined by the file system into memory. This
 * class maintains when the files on disk were last modified, and makes changes
 * to the world only as needed.
 * <p>
 * @author jslott
 */
public class WFSCellMO extends CellMO
    implements ManagedObject, Serializable {
    
    /* The prefix of the public binding name to find this GLO later */
    public static final String WFS_CELL_GLO = "WFS_CELL_GLO_";
    
    /* A hashmap of canonical file names and their last modified date/time */
    private HashMap<String, Long> fileModifiedMap =
        new HashMap<String, Long>();
    
    /* A hashtable of canonical file names and their referring Cell GLOs */
    private HashMap<String, ManagedReference<CellMO>> gloReferenceMap =
        new HashMap<String, ManagedReference<CellMO>>();
    
    /* The root URL of this wonderland file system */
    private URL root;
    
    /** Constructor */
    public WFSCellMO(URL root) {
        /*
         * These bounds may not entirely be correct -- a WFSCellGLO should simply
         * assume the bounds of its parent.
         */
        super(new BoundingBox(new Vector3f(), Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY), null);
        this.root = root;
        
        /* Load the world initially */
        this.reload(true);
    }
    
    /**
     * Returns the "binding name" for the WFS cell GLO so that it may easily
     * be retrieved. This binding name is simply WFS_CELL_GLO + URI.
     */
    public String getBindingName() {
        return WFSCellMO.WFS_CELL_GLO + this.root.toString();
    }
    
    /**
     * Reloads the world from disk, modifying only those parts in the world
     * which have changed. See the loadCells() routine for more information on
     * the algorithm used to load cells. After cells have been loaded, this
     * routine deletes all remaining cells from the world which are apart of
     * this WFS. This routine fails quietly -- it does not throw any exceptions
     * although it will log error messages. This routine searches the hierarchy
     * of files and directories in a breadth-first manner, this ensures that
     * all possible parent cells are created before any child cell.
     */
    public void reload(boolean init) {
        logger.log(Level.INFO, "WFS World Reload");
        
        /* A queue (last-in, first-out) containing a list of cell to search down */
        LinkedList<WFSCellDirectory> children =
            new LinkedList<WFSCellDirectory>();
        
        /* Make a copy of the current cells marked for deletion */
        HashMap<String, ManagedReference<CellMO>> deletedCells =
            (HashMap<String, ManagedReference<CellMO>>) this.gloReferenceMap.clone();
        
        /* First enumerate all of the cells in the current directory */
            WFS wfs = null;
        try {
            wfs = WFSFactory.open(this.root);
        } catch (java.io.FileNotFoundException excp) {
            logger.log(Level.SEVERE, "WFS Root File System Not Found: " + this.root, excp);
            return;
        } catch (java.io.IOException excp) {
            logger.log(Level.SEVERE, "WFS Root File System Read Error: " + this.root, excp);
            return;
        } catch (InvalidWFSException excp) {
            logger.log(Level.SEVERE, "Invalid WFS Root File System: " + this.root, excp);
            return;
        }
        
        /* Find the top level directory, add it to the list to search and go! */
        WFSCellDirectory dir = wfs.getRootDirectory();
        children.addFirst(dir);
        
        /*
         * Loop until the 'children' Queue is entirely empty, which means that
         * we have loaded all of the cells and have searched all possible sub-
         * directories. The loadCells() method will add entries to children as
         * needed.
         */
        while (children.isEmpty() == false) {
            /* Fetch and remove the first on the list and load */
            WFSCellDirectory childdir = children.removeFirst();
            this.loadCells(childdir, deletedCells, children);
        }
        
        /*
         * Delete all of the cells marked for deletion as follows: set a flag
         * to mark their deletion on the client, remove the cell GLO from the
         * hierarchy on the server, remove the references from gloReferenceMap
         * and fileModifiedMap.
         */
        Set<Map.Entry<String, ManagedReference<CellMO>>> entries =
            deletedCells.entrySet();
        Iterator<Map.Entry<String, ManagedReference<CellMO>>> iterator =
            entries.iterator();
        
        for (Map.Entry<String, ManagedReference<CellMO>> entry : entries) {
            CellMO glo = entry.getValue().getForUpdate();
            
            logger.log(Level.INFO, "WFS Reload: cell id=" + glo.getCellID() +
                " has been deleted");
            
            /*
             * Remove the cell from the parent-child hierarchy and the two hash
             * maps.
             */
            glo.detach();
            this.gloReferenceMap.remove(entry.getKey());
            this.fileModifiedMap.remove(entry.getKey());
        }
        
        /* Close any open resources associated with the world */
        wfs.close();
        
        /* Revalidate the entire world */
        if (init == false) {
            throw new RuntimeException("NOT IMPLEMENTED");
            //WonderlandContext.getCellManager().revalidate();
        }
    }
    
    @Override protected String getClientCellClassName(ClientSession clientSession,ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.client.extracells.WFSCell";
    }
    
    @Override
    public CellSetup getClientSetupData(ClientSession clientSession,ClientCapabilities capabilities) {
        return null;
    }
    
    /*-----------------------------------------------------------------------*
     * Private Utility Methods                                               *
     * ----------------------------------------------------------------------*/
    /**
     * Returns the parent cell GLO class for the given directory, using the
     * canonical name of its parent cell given during its creation. If the
     * cell GLO class does not exist, return this root WFSCellGLO class. This
     * class returns a read-only copy of the cell GLO class.
     */
    private CellMO getParentCellMO(WFSCellDirectory dir) {
        /*
         * Try to fetch the parent from the cache of cells and their canonical
         * names. If it does not exist, then we assume the parent is the WFSCellMO
         * class
         */
        WFSCell parent = dir.getAssociatedCell();
        if (parent != null) {
            ManagedReference<CellMO> moRef = this.gloReferenceMap.get(parent.getCanonicalName());
            if (moRef != null) {
                return moRef.get();
            }
        }
        return this;
    }
    
    /**
     * Given an array of WFS cells on disk, attempts to load them into the world.
     * The current state of cells loaded into the world is stored by two hash
     * maps: fileModifiedMap, which stores a list of cannonical file names in
     * the WFS and the last modified date for each file, and gloReferenceMap which
     * stores a list of cannonical file names and the GLO classes for each file.
     * These two hash maps must have identical sets of cannonical file entries.
     *
     * This routine is given a hash map of cannonical file names of cells which
     * should be deleted upon finishing the reload of the WFS. Initially, this
     * map should equal gloReferenceMap, and this routine removes entries from
     * the map as it encounters cells.
     *
     * For each cell file, this routine performs the following algorithm:
     *
     * 1. Check to see if the file is in the list of existing cells in the
     *    world (fileModifiedMap). If not, then load the cell from disk, add the
     *    cell to the world, and add the appropriate entries to fileModifiedMap
     *    and gloReferenceMap.
     *
     * 2. If the cell already exists in the world (fileModifiedMap), look at its
     *    last modified time.
     *
     *    (a) If it has not been modified more recently than the cell that has
     *        been loaded into the world, then simply remove the cell from the
     *        list to be deleted.
     *    (b) If it has been modified more recently than the cell that has been
     *        loaded into the world, then remove the existing cell from the
     *        world and add the new cell to the world. Also remove the cell from
     *        the list to be deleted.
     *
     * This routine also updates a Queue which keeps track of the subdirectories
     * which need to be visited in the breadth-first search to create all of the
     * cells. As each cell is loaded (if successful), this method checks whether
     * a directory of the proper name exists, and if so, adds it to the list of
     * cells to check.
     */
    private void loadCells(WFSCellDirectory dir,
        HashMap<String, ManagedReference<CellMO>> deletedCells,
        LinkedList<WFSCellDirectory> children) {
        
        WFSCell[] cells = dir.getCells();
        CellMO parent = this.getParentCellMO(dir);
        
        /*
         * Loop through each of the cells in the given array to compare its
         * last modified date with that stored in the fileModifiedMap
         */
        for (WFSCell cell : cells) {
            String canonical = cell.getCanonicalName();
            long modified = cell.getLastModified();
            
            logger.log(Level.INFO, "WFS Reload: Looking at cell: " + canonical +
                ", modified: " + modified + ", parent:" +
                dir.getPathName());
            
            /*
             * Check to see if the cell exists, if so, then check whether it
             * has been more recently modified. If not, then remove from the
             * list of deleted files and continue.
             */
            if (this.fileModifiedMap.containsKey(canonical) == true) {
                if (this.fileModifiedMap.get(canonical) >= modified) {
                    /* No need to do anything, just remove from deleted files */
                    logger.log(Level.INFO, "WFS Reload: cell has not been " +
                        "updated so we remove from deleted cell list " +
                        "and continue");
                    deletedCells.remove(canonical);

                    /*
                     * We still want to see whether any of its children has changed,
                     * so look to see if it has a '-wld' associated directory and
                     * add it to the list
                     */
                    WFSCellDirectory child = cell.getCellDirectory();
                    if (child != null) {
                        children.addLast(child);
                    }
                    
                    /* 
                     * There is nothing more to do for this cell.  Move on
                     * to the next one
                     */
                    continue;
                }
                
                /*
                 * Otherwise, we will mark the child for reconfiguring, first
                 * remove the cell from the list of deleted cells and the map
                 * of modified dates
                 */
                CellMO glo =
                    this.gloReferenceMap.get(canonical).getForUpdate();
                deletedCells.remove(canonical);
                this.fileModifiedMap.put(canonical, modified);
                
		// Notify the cell that it's content has changed
                glo.contentChanged();
                
                logger.log(Level.INFO, "WFS Reload: cell id=" +
                    glo.getCellID() +
                    " has been modified");
                
                /*
                 * Try re-loaded the XML file on disk into a CellProperties
                 * class, and set in the cell GLO class
                 */
                try {
                    CellMOSetup setup = cell.getCellSetup();
                    ((BeanSetupMO) glo).reconfigureCell(setup);
                } catch (java.io.FileNotFoundException excp) {
                    logger.log(Level.WARNING, "Cannot decode file: " +
                        cell.getCellName(), excp);
                } catch (InvalidWFSCellException excp) {
                    logger.log(Level.WARNING, "Invalid cell file: " +
                        excp.toString(), excp);
                } catch (ClassCastException cce) {
                    logger.log(Level.WARNING, "Unable to reconfigure cell " +
                               glo.getName() + " of type " +
                               glo.getClass() + ", not a BeanSetupMO", cce);
                }
                
                /*
                 * We still want to see whether any of its children has changed,
                 * so look to see if it has a '-wld' associated directory and
                 * add it to the list
                 */
                WFSCellDirectory child = cell.getCellDirectory();
                if (child != null) {
                    children.addLast(child);
                }
                continue;
            }
            
            /*
             * At this point, we want to load the cell into the world and update
             * the hash maps properly. We can arrive here if the cell is
             * brand new.
             */
            try {
               /* Load the cell from disk, catching exceptions below */
                CellMOSetup setup = cell.getCellSetup();
                CellMO cellMO =
                    CellMOFactory.loadCellMO(setup.getCellMOClassName());
                
                if (cellMO == null) {
                    logger.warning("Unable to load cell MO: " +
                                   setup.getCellMOClassName());
                    // skip this cell and move on
                    continue;
                }
                
                /* Call the cell's setup method */
                try {
                    ((BeanSetupMO) cellMO).setupCell(setup);
                } catch (ClassCastException cce) {
                    logger.log(Level.WARNING, "Error setting up new cell " +
                            cellMO.getName() + " of type " + 
                            cellMO.getClass() + ", it does not implement " +
                            "BeanSetupGLO.", cce);
                }
                
                ManagedReference ref =
                    AppContext.getDataManager().createReference(cellMO);
                
                /* Add the reference to the scene graph and hash maps */
                parent.addChild(cellMO);
                this.fileModifiedMap.put(canonical, modified);
                this.gloReferenceMap.put(canonical, ref);
                deletedCells.remove(canonical);
                
                /*
                 * Since the cell has been successfully loaded, then look to see
                 * if it has a '-wld' associated directory and add it to the list
                 */
                WFSCellDirectory child = cell.getCellDirectory();
                if (child != null) {
                    children.addLast(child);
                }
                
                /* Write messages to the log for good measure */
                logger.log(Level.INFO, "WFS Reload: New Properties: cell id=" +
                    cellMO.getCellID() + ", " + setup.toString());
            } catch (MultipleParentException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (java.io.FileNotFoundException excp) {
                logger.log(Level.INFO, "Cannot decode file: " +
                    cell.getCellName());
            } catch (InvalidWFSCellException excp) {
                logger.log(Level.INFO, "Invalid cell file: " +
                    excp.toString());
            }
        }
    }
}
