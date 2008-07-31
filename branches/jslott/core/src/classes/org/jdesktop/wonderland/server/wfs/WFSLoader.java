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

package org.jdesktop.wonderland.server.wfs;

import com.jme.math.Vector3f;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import org.jdesktop.wonderland.cells.BasicCellSetup;
import org.jdesktop.wonderland.common.cell.MultipleParentException;
import org.jdesktop.wonderland.server.WonderlandContext;
import org.jdesktop.wonderland.server.cell.StaticModelCellMO;
import org.jdesktop.wonderland.wfs.WFSCellChildren;
import org.jdesktop.wonderland.wfs.WFSRoots;

/**
 * The WFSLoader class is responsible for loading a WFS from the HTTP-based
 * WFS service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSLoader {

    /* The base URL of the loader */
    private static final String BASE_URL = "http://localhost:9998/wfs/";
    
    /** Constructor */
    public WFSLoader() {
    }
    
    public void load() {
        /* A queue (last-in, first-out) containing a list of cell to search down */
        LinkedList<WFSCellChildren> children = new LinkedList<WFSCellChildren>();
        
        /* First fetch all of the individual WFSs there are in the system. */
        WFSRoots wfsRoots = this.getWFSRoots();
        if (wfsRoots == null) {
            System.out.println("bad wfs roots");
            return;
        }
        
        /* take only the first root for now! XXX */
        String root = wfsRoots.getRoots()[0];
        
        System.out.println("WFSLoader: looking for children under root " + root);

        /* Find the children in the top-level directory and go! */
        WFSCellChildren dir = this.getWFSRootChildren(root);
        children.addFirst(dir);
        
        /*
         * Loop until the 'children' Queue is entirely empty, which means that
         * we have loaded all of the cells and have searched all possible sub-
         * directories. The loadCells() method will add entries to children as
         * needed.
         */
        while (children.isEmpty() == false) {
            /* Fetch and remove the first on the list and load */
            WFSCellChildren childdir = children.removeFirst();
            
            System.out.println("WFSLoader: processing children in " + childdir.getRelativePath());
            
            this.loadCells(root, childdir, children);
        }
    }
    
    private void loadCells(String root, WFSCellChildren dir , LinkedList<WFSCellChildren> children) {
        /*
         * Loop through all of the children. Download and parse the cell info.
         * See if the cell has children of its own.
         */
        WFSCellChildren.CellChild childs[] = dir.getChildren();
        for (WFSCellChildren.CellChild child : childs) {
            
            System.out.println("WFSLoader: processing child " + child.name);
            
            /*
             * Download and parse the cell configuration information. Create a
             * new cell based upon the information.
             */
            BasicCellSetup setup = this.getWFSCell(root, dir.getRelativePath(), child.name);
            if (setup == null) {
                System.out.println("can't read cell!");
                continue;
            }
            
            /* Create the cell */
            StaticModelCellMO cell = new StaticModelCellMO();
            cell.setupCell(setup);
            try {
                WonderlandContext.getCellManager().insertCellInWorld(cell);
            } catch (MultipleParentException excp) {
                System.out.println(excp.toString());
            }
            
            /*
             * See if the cell has any children and add to the linked list. We
             * first construct the new relative path. If the current relative
             * path is an empty string, then we make sure the new relative path
             * does not have a "/".
             */
            String newRelativePath = dir.getRelativePath() + "/" + child.name;
            if (dir.getRelativePath().compareTo("") == 0) {
                newRelativePath = child.name;
            }
            WFSCellChildren newChild = this.getWFSChildren(root, newRelativePath);
            if (newChild != null) {
                children.addLast(newChild);
            }
        }
    }
    
    /**
     * Returns the cell's setup information, null upon error. The relativePath
     * argument must never begin with a "/". For a cell in the root path, use
     * an empty string for the relative path argument
     */
    private BasicCellSetup getWFSCell(String root, String relativePath, String name) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = null;
            if (relativePath.compareTo("") == 0) {
                url = new URL(BASE_URL + root + "/" + name + "/cell");
            }
            else {
                url = new URL(BASE_URL + root + "/" + relativePath + "/" + name + "/cell");
            }
            
            /* Read in and parse the cell setup information */
            InputStreamReader isr = new InputStreamReader(url.openStream());
            BasicCellSetup setup = BasicCellSetup.decode(isr);
            return setup;
        } catch (java.lang.Exception excp) {
            return null;
        }
    }
    
    /**
     * Returns the children of the root WFS path, given the name of the WFS
     * root.
     */
    private WFSCellChildren getWFSRootChildren(String root) {
        try {
            URL url = new URL(BASE_URL + root + "//directory");
            InputStream is = url.openStream();
            WFSCellChildren wfsChildren = WFSCellChildren.decode("", is);
            return wfsChildren;
        } catch (java.lang.Exception excp) {
            return null;
        }            
    }
    
    /**
     * Returns the children of the WFS path. The relativePath argument must
     * never begin with a "/".
     */
    private WFSCellChildren getWFSChildren(String root, String relativePath) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + root + "/" + relativePath + "/directory");
            InputStream is = url.openStream();
            WFSCellChildren wfsChildren = WFSCellChildren.decode(relativePath, is);
            return wfsChildren;
        } catch (java.lang.Exception excp) {
            return null;
        }        
    }
    
    /**
     * Returns all of the WFS root names or null upon error
     */
    private WFSRoots getWFSRoots() {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + "roots");
            InputStream is = url.openStream();
            WFSRoots wfsRoots = WFSRoots.decode(is);
            return wfsRoots;
        } catch (java.lang.Exception excp) {
            return null;
        }
    }
    
    public static void main(String args[]) {
        new WFSLoader().load();
    }
}
