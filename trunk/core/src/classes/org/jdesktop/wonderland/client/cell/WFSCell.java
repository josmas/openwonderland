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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Client side class for WFS cells. Although the WFS cell is present in the
 * hierarchy, they are invisible. It is kept in the hierarchy because it
 * could be useful at some point to know how the cells are containing with
 * separate WFSs.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class WFSCell extends Cell {

    /**
     * Constructor, takes the unique cell ID as an argument.
     * 
     * @param cellID The unique cell identifier
     */
    public WFSCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }
}
