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
package org.jdesktop.wonderland.modules.palette.client.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * A Transferable used for drag-and-drop that corresponds to a serialized object
 * for CellServerState.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellServerStateTransferable implements Transferable {

    private CellServerState cellServerState = null;
    private static DataFlavor dataFlavor = new DataFlavor(CellServerState.class, "CellServerState");

    /**
     * Constructor, takes the CellServer State to transfer
     */
    public CellServerStateTransferable(CellServerState cellServerState) {
        this.cellServerState = cellServerState;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { dataFlavor };
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return dataFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(dataFlavor) == false) {
            throw new UnsupportedFlavorException(flavor);
        }
        return cellServerState;
    }
}
