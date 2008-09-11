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

package org.jdesktop.wonderland.wfs.utils;

/**
 * The WFSLoaderDefs class contains some useful public definitions for the WFS
 * loader mechanism. It contains all of the names that objects are bound to in
 * the Darkstar data manager.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSLoaderDefs {

    /* Binding name for the map of canonical cell names to last modified date */
    public static final String WFS_MODIFIED_MAP = "WFS_MODIFIED_MAP";
    
    /* Binding name for the map of canonical cell names to cell references */
    public static final String WFS_OBJECT_MAP = "WFS_OBJECT_MAP";
    
    /* Binding name for the reload phase */
    public static final String WFS_RELOAD_PHASE = "WFS_RELOAD_PHASE";
    
    /* Binding name for list of cells just downloaded from WFS */
    public static final String WFS_CELL_LIST = "WFS_CELL_LIST";
    
    /* Binding name for the list of added cells */
    public static final String WFS_ADDED_CELLS = "WFS_ADDED_CELLS";
    
    /* Binding name for the list of modified cells */
    public static final String WFS_MODIFIED_CELLS = "WFS_MODIFIED_CELLS";
    
    /* Binding name for the list of removed cells */
    public static final String WFS_REMOVED_CELLS = "WFS_REMOVED_CELLS";
}
