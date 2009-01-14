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
package org.jdesktop.wonderland.client.cell;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.view.AvatarCell;
import org.jdesktop.wonderland.client.comms.ClientConnection;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableAvatarMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;

/**
 * A component that extends MovableComponent to add additional information
 * for animating avatars.
 *
 * @author paulby
 */
public class MovableAvatarComponent extends MovableComponent {
    private int trigger;
    private boolean pressed;

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
                                 int trigger,
                                 boolean pressed,
                                 CellMoveModifiedListener listener) {

        synchronized(this) {
            this.trigger = trigger;
            this.pressed = pressed;
            super.localMoveRequest(transform, null);
        }
    }

    @Override
    protected CellMessage createMoveRequestMessage(CellTransform transform) {
        return MovableAvatarMessage.newMoveRequestMessage(cell.getCellID(),
                                                    transform.getTranslation(null),
                                                    transform.getRotation(null),
                                                    trigger,
                                                    pressed);
    }

    @Override
    protected ResponseListener createMoveResponseListener(final CellMoveModifiedListener listener) {
        throw new RuntimeException("Not supported");
    }

    @Override
    protected Class getMessageClass() {
        return MovableAvatarMessage.class;
    }

    @Override
    protected Class getLookupClass() {
        return MovableComponent.class;
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
