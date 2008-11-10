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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.cell.Cell.RendererType;
import org.jdesktop.wonderland.client.cell.selection.SelectionManager;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseDraggedEvent3D;
import org.jdesktop.wonderland.common.cell.CellEditConnectionType;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.messages.CellDeleteMessage;

/**
 *
 * @author jordanslott
 */
public class SelectableComponent extends CellComponent {
    public SelectableComponent(Cell cell) {
        super(cell);
    }
    
    @Override
    public void setStatus(CellStatus status) {
        super.setStatus(status);
        Logger logger = Logger.getLogger(SelectableComponent.class.getName());
        logger.warning("[SELECT] status " + status);

        if (status == CellStatus.ACTIVE) {
            CellRenderer r = cell.getCellRenderer(RendererType.RENDERER_JME);
            if (r == null) {
                return;
            }
            CellRendererJME jme = (CellRendererJME) r;
            Entity entity = jme.getEntity();
            logger.warning("[SELECT] entity " + entity);
            if (entity == null) {
                return;
            }
            logger.warning("[SELECT] " + entity.getName());
            
            MouseEventListener listener = new MouseEventListener();
            listener.addToEntity(entity);
            KeyEventListener l2 = new KeyEventListener();
            l2.addToEntity(entity);

        }
    }
    
    private Vector3f startLocation = null;
    
    class MouseEventListener extends EventClassListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { MouseButtonEvent3D.class, MouseDraggedEvent3D.class };
        }

        // Note: we don't override computeEvent because we don't do any computation in this listener.

        @Override
        public void commitEvent(Event event) {
            Logger logger = Logger.getLogger(SelectableComponent.class.getName());
            SelectionManager manager = SelectionManager.getSelectionManager();

            logger.warning("SELECT EVENT");
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D mbe = (MouseButtonEvent3D)event;
                if (mbe.isPressed() == true) {
                    startLocation = mbe.getIntersectionPointWorld();
                    //logger.warning("SELECTED LOCAL " + mbe.getIntersectionPointLocal());
                    logger.warning("SELECTED GLOBAL " + mbe.getIntersectionPointWorld());
                    manager.setSelectedCell(cell, event.getEntity(), startLocation);
                    logger.warning("MOUSE PRESSED " + startLocation);
                }
            }
            else if (event instanceof MouseDraggedEvent3D) {
                MouseDraggedEvent3D mde = (MouseDraggedEvent3D)event;
                Vector3f newLocation = mde.getIntersectionPointWorld();
                Vector3f delta = newLocation.subtract(startLocation);
                CellTransform transform = cell.getLocalTransform();
                Vector3f trans = transform.getTranslation(null);
                trans = trans.add(delta);
                transform.setTranslation(trans);
                MovableComponent mc = cell.getComponent(MovableComponent.class);
                startLocation = newLocation;
                mc.localMoveRequest(transform);
                manager.setSelectedCell(cell, event.getEntity(), startLocation);
                logger.warning("MOUSE DRAGGED");
            }
        }
    }

    class KeyEventListener extends EventClassFocusListener {
        @Override
        public Class[] eventClassesToConsume() {
            return new Class[] { KeyEvent3D.class };
        }

        @Override
        public void commitEvent(Event event) {
            Logger logger = Logger.getLogger(SelectableComponent.class.getName());
            if (((KeyEvent3D)event).isTyped() == true) {
                KeyEvent3D keyEvent = (KeyEvent3D) event;
                char keyChar = keyEvent.getKeyChar();

                CellTransform transform = cell.getLocalTransform();
                Vector3f trans = transform.getTranslation(null);
                Vector3f scale = transform.getScaling(null);
                Quaternion rotation = transform.getRotation(null);

                if (keyChar == '0') {
                    CellDeleteMessage msg = new CellDeleteMessage(cell.getCellID());
                    WonderlandSession session = cell.getCellCache().getSession();
                    CellEditChannelConnection connection = (CellEditChannelConnection) session.getConnection(CellEditConnectionType.CLIENT_TYPE);
                    connection.send(msg);
                }
                else if (keyChar == 'U') {
                    trans = trans.add(new Vector3f(0, 0, 1));
                }
                else if (keyChar == 'O') {
                    trans = trans.add(new Vector3f(0, 0, -1));
                }
                else if (keyChar == 'I') {
                    trans = trans.add(new Vector3f(0, 1, 0));
                }
                else if (keyChar == 'K') {
                    trans = trans.add(new Vector3f(0, -1, 0));
                }
                else if (keyChar == 'J') {
                    trans = trans.add(new Vector3f(-1, 0, 0));
                }
                else if (keyChar == 'L') {
                    trans = trans.add(new Vector3f(1, 0, 0));
                }
                else if (keyChar == '+') {
                    logger.warning("SCALE 1 " + scale.toString());
                    scale = scale.mult((float)1.1);
                    logger.warning("SCALE 2 " + scale.toString());
                }
                else if (keyChar == '-') {
                    scale = scale.mult((float)0.9);
                }
                else if (keyChar == '<') {
                    Vector3f axis = new Vector3f((float)1.0, (float)0.0, (float)0.0);
                    Quaternion quat = new Quaternion().fromAngleAxis((float)1.0, axis);
                    rotation = rotation.mult(quat);
                }
                else if (keyChar == '>') {
                    Vector3f axis = new Vector3f((float)0.0, (float)1.0, (float)0.0);
                    Quaternion quat = new Quaternion().fromAngleAxis((float)1.0, axis);
                    rotation = rotation.mult(quat);
                }
                else if (keyChar == '?') {
                    Vector3f axis = new Vector3f((float)0.0, (float)0.0, (float)1.0);
                    Quaternion quat = new Quaternion().fromAngleAxis((float)1.0, axis);
                    rotation = rotation.mult(quat);
                }
                
                transform.setTranslation(trans);
                transform.setScaling(scale);
                transform.setRotation(rotation);
                MovableComponent mc = cell.getComponent(MovableComponent.class);
                mc.localMoveRequest(transform);
            }
        }
    }
}
