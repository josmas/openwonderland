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

import java.io.InputStreamReader;
import java.net.URL;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.wfs.loader.CellLoader;


/**
 * The CellLoaderUtils contains a collection of static utility methods to load
 * WFS information from the WFS web service.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellLoaderUtils {
    /* The base URL of the loader */
    private static final String BASE_URL = "http://localhost:8080/wonderland-web-wfs/wfs/";
    //private static final String BASE_URL = System.getProperty("wonderland.server.wfs.url");
    
    /**
     * Returns the list of cells in the WFS as a hashmap. The list of cells
     * is ordered so that parent cells appear before child cells. Takes the WFS
     * URI of the WFS root.
     */
    public static CellList getWFSCells(String root, boolean reload) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + root + "/cells/?reload=" + Boolean.toString(reload));
            return CellList.decode("", url.openStream());
        } catch (java.lang.Exception excp) {
            return null;
        }
    }

    /**
     * Returns the cell's setup information, null upon error. The canonicalName
     * argument must never begin with a "/".
     */
    public static BasicCellSetup getWFSCell(String root, String canonicalName) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + root + "/cell/" + canonicalName);
            
            /* Read in and parse the cell setup information */
            InputStreamReader isr = new InputStreamReader(url.openStream());
            return BasicCellSetup.decode(isr);
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
            return null;
        }
    }
    
    /**
     * Returns the cell's setup information, null upon error. The relativePath
     * argument must never begin with a "/". For a cell in the root path, use
     * an empty string for the relative path argument
     */
    public static BasicCellSetup getWFSCell(String root, String relativePath, String name) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = null;
            if (relativePath.compareTo("") == 0) {
                url = new URL(BASE_URL + root + "/cell/" + name);
            }
            else {
                url = new URL(BASE_URL + root + "/cell/" + relativePath + "/" + name);
            }
            
            /* Read in and parse the cell setup information */
            InputStreamReader isr = new InputStreamReader(url.openStream());
            return BasicCellSetup.decode(isr);
        } catch (java.lang.Exception excp) {
            System.out.println(excp.toString());
            return null;
        }
    }
    
    /**
     * Returns the children of the root WFS path, given the name of the WFS
     * root.
     */
    public static CellList getWFSRootChildren(String root) {
        try {
            URL url = new URL(BASE_URL + root + "/directory/");
            return CellList.decode("", url.openStream());
        } catch (java.lang.Exception excp) {
            return null;
        }            
    }
    
    /**
     * Returns the children of the WFS path. The relativePath argument must
     * never begin with a "/".
     */
    public static CellList getWFSChildren(String root, String canonicalName) {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + root + "/directory/" + canonicalName);
            return CellList.decode(canonicalName, url.openStream());
        } catch (java.lang.Exception excp) {
            return null;
        }        
    }
    
    /**
     * Returns all of the WFS root names or null upon error
     */
    public static CellRoots getWFSRoots() {
        /*
         * Try to open up a connection the Jersey RESTful resource and parse
         * the stream. Upon error return null.
         */
        try {
            URL url = new URL(BASE_URL + "roots");
            CellLoader.getLogger().info("WFS: Loading roots at " + url.toExternalForm());
            return CellRoots.decode(url.openStream());
        } catch (java.lang.Exception excp) {
            CellLoader.getLogger().info("WFS: Error loading roots: " + excp.toString());
            return null;
        }
    }
}
