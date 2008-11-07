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

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * A message from a client requesting that a ChannelComponent is created
 * for a cell
 * 
 * @author paulby
 */
public class CellChannelComponentRequestMessage extends CellMessage {

    public CellChannelComponentRequestMessage(CellID cellID) {
        super(cellID);
    }
}
