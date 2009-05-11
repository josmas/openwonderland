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
package org.jdesktop.wonderland.common.cell.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Message sent to query the cell server state for a cell given its
 * unique ID.
 * <p>
 * This message can be used as a request from the client to the server. The
 * server responds with a CellServerStateResponseMessage.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class CellServerStateRequestMessage extends CellMessage {
    /**
     * Constructor that takes the unique ID of the cell and the server state
     * (used to set the state).
     * 
     * @param cellID The id of the cell
     */
    public CellServerStateRequestMessage(CellID cellID) {
        super(cellID);
    }
}
