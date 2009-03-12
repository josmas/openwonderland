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
package org.jdesktop.wonderland.client.jme.dnd.spi;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Handler for different data flavors for drag-and-drop. Classes implement this
 * interface to handle different DataFlavor objects when dropped into the
 * world.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public interface DataFlavorHandlerSPI {

    /**
     * Returns an array of DataFlavors that this class supports. If none, then
     * return an empty array.
     *
     * @param An array of DataFlavor objects
     */
    public DataFlavor[] getDataFlavors();

    /**
     * Handles when an item has been dropped into the world with a data flavor
     * supported by this class.
     *
     * @param transferable The dropped transferable
     * @param dataFlavor The data flavor of the transferable
     * @param dropLocation The point at which the item was dropped
     */
    public void handleDrop(Transferable transferable, DataFlavor dataFlavor, Point dropLocation);
}
