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
package org.jdesktop.wonderland.client.cell.utils;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.Map;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.CellEditChannelConnection;
import org.jdesktop.wonderland.client.cell.view.ViewCell;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ViewManager;
import org.jdesktop.wonderland.client.login.LoginManager;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.messages.CellCreateMessage;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;

/**
 * A collection of useful utility routines pertaining to Cells.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellUtils {

    private static Logger logger = Logger.getLogger(CellUtils.class.getName());

    /**
     * Creates a cell in the world given the CellServerState of the cell and
     * the linear distance away from the avatar to initially place the cell.
     * Throws CellCreationException upon failure. If the given CellServerState
     * is null, this method simply does not create a Cell.
     *
     * @param state The cell server state for the new cell
     * @param distance The linear distance away from the avatar
     * @throw CellCreationException Upon error creating the cell
     */
    public static void createCell(CellServerState state, float distance)
           throws CellCreationException {

        // Check to see if the Cell server state is null, and fail quietly if
        // so
        if (state == null) {
            logger.fine("Creating cell with null server state. Returning.");
            return;
        }

        // Fetch the current transform from the view manager. Find the current
        // position of the camera and its look direction.
        ViewManager manager = ViewManager.getViewManager();
        ViewCell viewCell = manager.getPrimaryViewCell();
//        Vector3f cameraPosition = manager.getCameraPosition(null);
        Vector3f avatarPosition = viewCell.getWorldTransform().getTranslation(null);
        Vector3f cameraLookDirection = manager.getCameraLookDirection(null);

        // For now, the origin of the Cell is right on top of the avatar
//        Vector3f origin = cameraPosition;
        
        // HACK ALERT: If we find a "sizing-hint" field in the Cell server state
        // meta data, then we use that to place the Cell 1 unit away from the
        // camera.
//        Map<String, String> metadata = state.getMetaData();
//        String sizingHint = metadata.get("sizing-hint");
//        if (sizingHint != null) {
//            float sizing = Float.parseFloat(sizingHint);
//            distance = 1.0f + sizing;
//        }


        // Hard-code distance away to 1.0.
        distance = 1.0f;

        // Compute the new vector away from the camera position to be a certain
        // number of scalar units away
        float lengthSquared = cameraLookDirection.lengthSquared();
        float factor = (distance * distance) / lengthSquared;
        Vector3f origin = avatarPosition.add(cameraLookDirection.mult(factor));

        System.out.println("AVATAR POSITION " + avatarPosition);
        System.out.println("CAMERA LOOK AT " + cameraLookDirection);
        System.out.println("LENGTH SQ " + lengthSquared);
        System.out.println("DISTANCE SQ " + (distance * distance));
//        System.out.println("SIZING HINT " + sizingHint);
        System.out.println("FACTOR " + factor);
        System.out.println("CELL ORIGIN " + origin);
        
        // Create a position component that will set the initial origin
        PositionComponentServerState position = new PositionComponentServerState();
        position.setTranslation(origin);
        Quaternion quaternion = new Quaternion();
        quaternion.lookAt(cameraLookDirection.negate(), new Vector3f(0, 1, 0));
        position.setRotation(quaternion);
        state.addComponentServerState(position);

        // Send the message to the server
        WonderlandSession session = LoginManager.getPrimary().getPrimarySession();
        CellEditChannelConnection connection = (CellEditChannelConnection)
                session.getConnection(CellEditConnectionType.CLIENT_TYPE);
        CellCreateMessage msg = new CellCreateMessage(null, state);
        connection.send(msg);
    }

    /**
     * Requests the server to delete the given cell from the world
     * of the primary session. Returns true if the deletion succeeds.
     * <br><br>
     * Note: currently always returns true because the server doesn't send 
     * any response to the cell delete message.
     */
    public static boolean deleteCell (Cell cell) {

        WonderlandSession session = cell.getCellCache().getSession();
        CellEditChannelConnection connection = (CellEditChannelConnection)
            session.getConnection(CellEditConnectionType.CLIENT_TYPE);
        CellDeleteMessage msg = new CellDeleteMessage(cell.getCellID());
        connection.send(msg);

        // TODO: there really really should be an OK/Error response from the server!

        return true;
    }
}
