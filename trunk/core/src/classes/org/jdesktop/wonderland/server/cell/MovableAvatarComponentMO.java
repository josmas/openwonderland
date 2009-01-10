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
package org.jdesktop.wonderland.server.cell;

import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;

/**
 *
 * @author paulby
 */
public class MovableAvatarComponentMO extends MovableComponentMO {

    public MovableAvatarComponentMO(CellMO cell) {
        super(cell);
    }

    @Override
    protected Class getMessageClass() {
        return MovableAvatarMessage.class;
    }

    @Override
    public void moveRequest(WonderlandClientID clientID, MovableMessage msg) {
        MovableAvatarMessage aMsg = (MovableAvatarMessage) msg;
        CellTransform transform = new CellTransform(msg.getRotation(), msg.getTranslation());

        CellMO cell = cellRef.getForUpdate();
        ChannelComponentMO channelComponent;
        cell.setLocalTransform(transform);

        channelComponent = channelComponentRef.getForUpdate();

        if (cell.isLive()) {
            channelComponent.sendAll(clientID, MovableAvatarMessage.newMovedMessage(cell.getCellID(), transform, aMsg.getTrigger(), aMsg.isPressed()));
        }
    }

    @Override
    protected String getClientClass() {
        return "org.jdesktop.wonderland.client.cell.MovableAvatarComponent";
    }

    /**
     * Return the class used to reference this component. Usually this will return
     * the class of the component, but in some cases, such as the ChannelComponentMO
     * subclasses of ChannelComponentMO will return their parents class
     * @return
     */
    @Override
    protected Class getLookupClass() {
        return MovableComponentMO.class;
    }
}
