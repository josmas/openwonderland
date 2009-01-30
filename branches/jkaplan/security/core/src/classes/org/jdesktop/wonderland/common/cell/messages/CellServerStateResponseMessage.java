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

import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * A response message giving the setup class for a cell. This message is always
 * sent when a query is made for the cell setup.
 * 
 * @author Jordasn Slott <jslott@dev.java.net>
 */
@InternalAPI
public class CellServerStateResponseMessage extends ResponseMessage {

    private CellServerState cellSetup;
    
    /**
     * Constructor, takes the ID of the message and the cell setup class.
     *
     * @param messageID the id of the message to which we are responding
     * @param viewCellID the id of the view cell
     */
    public CellServerStateResponseMessage(MessageID messageID, CellServerState cellSetup) {
        super (messageID);
        this.cellSetup = cellSetup;
    }

    public CellServerState getCellServerState() {
        return cellSetup;
    }
}
