/**
 * Project Wonderland
 *
 * $Id$
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
 */
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Any cell which has it's own channel must implement this interface
 * 
 * @author paulby
 */
@ExperimentalAPI
public interface ChannelCell {

    public void handleMessage(CellMessage message);
    
    // TODO various send methods required, cell to server, cell to cell, cell to channel
    public void send(CellMessage message);
}
