/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.client.jme;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.CameraNode;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassFocusListener;
import org.jdesktop.wonderland.client.jme.input.KeyEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * A very simplistic third person camera model
 * 
 * @author paulby
 */
public class ThirdPersonCameraProcessor implements CameraController {

    private Quaternion rotation = new Quaternion();
    private Vector3f translation = new Vector3f();
    protected float cameraZoom = 0.2f;
    protected Vector3f offset = new Vector3f(0,2.2f,-6);
    private boolean commitRequired = false;
    private Quaternion viewRot = new Quaternion();
    private Vector3f viewTranslation = new Vector3f();

    protected Vector3f cameraLook = new Vector3f(0,0,1);
    private Vector3f yUp = new Vector3f(0,1,0);

    private Vector3f tmp=new Vector3f();

    private WorldManager wm;

    private CameraNode cameraNode;

    private EventClassFocusListener listener = null;

    private boolean enabled = false;

    private int mouseX = 0;
    private int mouseY = 0;
    private float elevation = 0f;
    private float angle = 0f;

    private Vector3f avatarPos = new Vector3f();
    private Quaternion avatarRot = new Quaternion();
    
    public ThirdPersonCameraProcessor() {
        wm = ClientContextJME.getWorldManager();
    }
    
    public void compute() {
    }

    public void commit() {
        if (commitRequired) {
            cameraNode.setLocalRotation(rotation);
            cameraNode.setLocalTranslation(translation);
            wm.addToUpdateList(cameraNode);
            commitRequired = false;
        }
    }

    @Override
    public void viewMoved(CellTransform worldTransform) {
        avatarPos = worldTransform.getTranslation(avatarPos);
        avatarRot = worldTransform.getRotation(avatarRot);
        update(avatarPos, avatarRot );
    }

    private void update(Vector3f tIn, Quaternion rIn) {
        translation.set(tIn);
        tmp = translation.clone();
        rotation.set(rIn);
        viewRot.set(rotation);
        viewTranslation.set(translation);

        Vector3f cameraTrans = rotation.mult(offset);
//            System.out.println("Camera trans "+cameraTrans );
        translation.addLocal(cameraTrans);

        rotation.lookAt(rotation.mult(cameraLook), yUp);
        commitRequired=true;
    }

    public void setEnabled(boolean enabled, CameraNode cameraNode) {
        if (this.enabled==enabled)
            return;
        this.enabled = enabled;

        // Called on the compute thread, therefore does not need to be synchronized
        this.cameraNode = cameraNode;
        if (enabled) {
            if (listener==null) {
                listener = new EventClassFocusListener() {
                    @Override
                    public Class[] eventClassesToConsume () {
                        return new Class[] { KeyEvent3D.class, MouseEvent3D.class };
                    }

                    @Override
                    public void commitEvent (Event event) {
                        if (event instanceof KeyEvent3D) {
                            KeyEvent key = (KeyEvent) ((KeyEvent3D)event).getAwtEvent();
                            if (key.getKeyCode()==KeyEvent.VK_EQUALS) {
                                offset.z += cameraZoom;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            } else if (key.getKeyCode()==KeyEvent.VK_MINUS) {
                                offset.z -= cameraZoom;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            }
                        } else if (event instanceof MouseEvent3D) {
                            MouseEvent mouse = (MouseEvent)((MouseEvent3D)event).getAwtEvent();
                            if (mouse instanceof MouseWheelEvent) {
                                int clicks = ((MouseWheelEvent)mouse).getWheelRotation();
                                offset.z -= cameraZoom*clicks;
                                viewMoved(new CellTransform(viewRot, viewTranslation));
                            } else if (mouse.isControlDown()) {
                                int diffX = mouse.getX() - mouseX;
                                int diffY = mouse.getY() - mouseY;

                                float scale =  mouse.isShiftDown()? 4f : 16f;

                                elevation += Math.toRadians(diffY)/scale;
                                if (elevation>Math.PI/2)
                                    elevation = (float)Math.PI/2;
                                else if (elevation<-Math.PI/2)
                                    elevation = -(float)Math.PI/2;

                                angle += Math.toRadians(diffX)/scale;
                                if (angle>Math.PI/2)
                                    angle = (float)Math.PI/2;
                                else if (angle<-Math.PI/2)
                                    angle = -(float)Math.PI/2;

                                cameraLook.set((float)Math.sin(angle), (float)Math.sin(elevation), 1);
                                cameraLook.normalize();

                                mouseX = mouse.getX();
                                mouseY = mouse.getY();
                                update(avatarPos, avatarRot);
                            } else {
                                mouseX = mouse.getX();
                                mouseY = mouse.getY();
                            }
                        }
                    }
                };
            }
            ClientContextJME.getInputManager().addGlobalEventListener(listener);
        } else {
            ClientContextJME.getInputManager().removeGlobalEventListener(listener);
        }
    }
}
