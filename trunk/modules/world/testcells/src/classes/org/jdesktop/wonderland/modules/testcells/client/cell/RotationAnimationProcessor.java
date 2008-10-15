/*
 * Copyright 2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jdesktop.wonderland.modules.testcells.client.cell;

import org.jdesktop.mtgame.*;
import com.jme.scene.Node;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.scenario.animation.Clip;

/**
 * This is a simple test processor that rotates a node around the Y axis
 * a little bit every frame
 * 
 * @author Doug Twilleager
 */
public class RotationAnimationProcessor extends ProcessorComponent {     
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;
    /**
     * The current degrees of rotation
     */
    private float radians = 0.0f;

    /**
     * The increment to rotate each frame
     */
    private float increment = 0.0f;
    
    /**
     * The rotation matrix to apply to the target
     */
    private Quaternion quaternion = new Quaternion();
    
    /**
     * The rotation target
     */
    private Node target = null;
    
    /**
     * A name
     */
    private String name = null;
    
    /**
     * The constructor
     */
    public RotationAnimationProcessor(String name, WorldManager worldManager, Node target, float increment) {
        this.worldManager = worldManager;
        this.target = target;
        this.increment = increment;
        this.name = name;
        setArmingCondition(new NewFrameCondition(this));

        Clip clip2 = Clip.create(1000, this, "angle", new Float(0), new Float(Math.PI*2));
        clip2.start();
    }

    public void setAngle(float radians) {
        synchronized(quaternion) {
            System.out.println("SetAngle "+radians);
            this.radians = radians;
        }
    }

    public float getAngle() {
        return radians;
    }
    
    public String toString() {
        return (name);
    }
    
    /**
     * The initialize method
     */
    public void initialize() {
        setArmingCondition(new NewFrameCondition(this));
    }
    
    /**
     * The Calculate method
     */
    public void compute(ProcessorArmingCollection collection) {
    }

    /**
     * The commit method
     */
    public void commit(ProcessorArmingCollection collection) {
        synchronized(quaternion) {
            System.out.println("Commit "+radians);
            quaternion.fromAngles(0.0f, radians, 0.0f);
            target.setLocalRotation(quaternion);
            worldManager.addToUpdateList(target);
        }
    }
}
