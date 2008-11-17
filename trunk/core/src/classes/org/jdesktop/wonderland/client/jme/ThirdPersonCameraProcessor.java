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
package org.jdesktop.wonderland.client.jme;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.event.KeyEvent;
import org.jdesktop.mtgame.ProcessorArmingCollection;
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
public class ThirdPersonCameraProcessor extends CameraProcessor {

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
    
    public ThirdPersonCameraProcessor() {
        wm = ClientContextJME.getWorldManager();

        // TODO this should be done in setEnabled
        ClientContextJME.getInputManager().addGlobalEventListener(new EventClassFocusListener() {
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
                }
            }
        });
    }
    
    @Override
    public void compute(ProcessorArmingCollection arg0) {
    }

    @Override
    public void commit(ProcessorArmingCollection arg0) {
        synchronized(this) {
            if (commitRequired) {
                cameraNode.setLocalRotation(rotation);
                cameraNode.setLocalTranslation(translation);
                wm.addToUpdateList(cameraNode);
                commitRequired = false;
            }

        }
    }

    @Override
    public void initialize() {
        // Camera is chained so does not need it's own ArmingCondition
    }

    @Override
    public void viewMoved(CellTransform worldTransform) {
        synchronized(this) {
            translation = worldTransform.getTranslation(translation);
            tmp = translation.clone();
            rotation = worldTransform.getRotation(rotation);
            viewRot.set(rotation);
            viewTranslation.set(translation);


            Vector3f cameraTrans = rotation.mult(offset);
//            System.out.println("Camera trans "+cameraTrans );
            translation.addLocal(cameraTrans);
            commitRequired=true;

            rotation.lookAt(rotation.mult(cameraLook), yUp);
        }
    }



}
