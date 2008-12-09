/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.avatarbase.client.jme.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableComponent;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.modules.avatarbase.common.cell.messages.MovableAvatarMessage;

/**
 *
 * @author paulby
 */
public class MovableAvatarComponent extends MovableComponent {

    public MovableAvatarComponent(Cell cell) {
        super(cell);
    }

//    public void localMoveRequest(CellTransform transform,
//                                 final CellMoveModifiedListener listener) {
//
//        // make sure we are connected to the server
//        if (channelComp == null ||
//                channelComp.getStatus() != ClientConnection.Status.CONNECTED) {
//            logger.warning("Cell channel not connected when moving cell " +
//                           cell.getCellID());
//            return;
//        }
//
//        // TODO throttle sends, we should only send so many times a second.
//        if (listener!=null) {
//            throw new RuntimeException("NOT IMPLEMENTED");
//        } else {
//            channelComp.send(
//                MovableAvatarMessage.newMoveRequestMessage(cell.getCellID(),
//                                                    transform.getTranslation(null),
//                                                    transform.getRotation(null)));
//        }
//
//        cell.setLocalTransform(transform, TransformChangeListener.ChangeSource.LOCAL);
//    }
}
