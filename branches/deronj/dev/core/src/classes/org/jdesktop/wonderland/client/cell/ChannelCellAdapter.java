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

import java.util.logging.Logger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Optional Adapter that implements the send support of ChannelCell
 * 
 * @author paulby
 */
@ExperimentalAPI
public class ChannelCellAdapter {

    /**
     * TODO fix method params, need to know who is sending msg
     * @param cellMessage
     */
    public static void send(CellMessage cellMessage) {
        Logger.getAnonymousLogger().warning("ChannelCellAdapter.send not implemented");
    }
}
