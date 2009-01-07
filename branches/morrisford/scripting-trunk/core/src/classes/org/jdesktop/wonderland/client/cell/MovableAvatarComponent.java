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

import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;

/**
 * A component that extends MovableComponent to add additional information
 * for animating avatars.
 *
 * @author paulby
 */
public class MovableAvatarComponent extends MovableComponent {

    public MovableAvatarComponent(Cell cell) {
        super(cell);
    }


    @Override
    public void localMoveRequest(CellTransform transform) {
        localMoveRequest(transform, -1, false, null);
    }

    @Override
    public void localMoveRequest(CellTransform transform,
                                 final CellMoveModifiedListener listener) {
        localMoveRequest(transform, -1, false, listener);
    }


    public void localMoveRequest(CellTransform transform,
                                 final int trigger,
                                 final boolean pressed,
                                 final CellMoveModifiedListener listener) {

        // make sure we are connected to the server
        if (channelComp == null ||
                channelComp.getStatus() != ClientConnection.Status.CONNECTED) {
            logger.warning("Cell channel not connected when moving cell " +
                           cell.getCellID());
            return;
        }

        // TODO throttle sends, we should only send so many times a second.
        if (listener!=null) {
            throw new RuntimeException("NOT IMPLEMENTED");
        } else {
            channelComp.send(
                MovableAvatarMessage.newMoveRequestMessage(cell.getCellID(),
                                                    transform.getTranslation(null),
                                                    transform.getRotation(null),
                                                    trigger,
                                                    pressed));
        }

        applyLocalTransformChange(transform, TransformChangeListener.ChangeSource.LOCAL);
    }

    @Override
    protected Class getMessageClass() {
        return MovableAvatarMessage.class;
    }

    @Override
    protected void serverMoveRequest(MovableMessage msg) {
        super.serverMoveRequest(msg);

            ((AvatarCell)cell).triggerAction(1, true);


        MovableAvatarMessage mam = (MovableAvatarMessage) msg;
        if (mam.getTrigger()!=-1) {
            ((AvatarCell)cell).triggerAction(mam.getTrigger(), mam.isPressed());
        }
    }

}
