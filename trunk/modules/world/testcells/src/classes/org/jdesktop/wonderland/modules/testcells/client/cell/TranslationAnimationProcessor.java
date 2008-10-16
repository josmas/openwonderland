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
import com.jme.math.Vector3f;
import com.sun.scenario.animation.TimingTarget;
import org.jdesktop.wonderland.client.jme.ClientContextJME;

/**
 * Translation Animation for use with Scenario Timing framework
 *
 * @author paulby
 */
public class TranslationAnimationProcessor extends ProcessorComponent implements TimingTarget {
    /**
     * The WorldManager - used for adding to update list
     */
    private WorldManager worldManager = null;

    private Vector3f startV3f;
    private Vector3f endV3f;
    
    private Vector3f translation = new Vector3f();

    /**
     * The rotation target
     */
    private Node target = null;
    
    /**
     * The constructor
     */
    public TranslationAnimationProcessor(Node target, Vector3f startLocation, Vector3f endLocation) {
        this.worldManager = ClientContextJME.getWorldManager();
        this.target = target;
        this.startV3f = startLocation;
        this.endV3f = endLocation;
    }

    /**
     * The initialize method
     */
    public void initialize() {
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
        synchronized(translation) {
            target.setLocalTranslation(translation);
        }
        worldManager.addToUpdateList(target);
    }
    

    public void timingEvent(float fraction, long totalElapsed) {
        synchronized(translation) {
            translation.x = (endV3f.x - startV3f.x)*fraction;
            translation.y = (endV3f.y - startV3f.y)*fraction;
            translation.z = (endV3f.z - startV3f.z)*fraction;
        }
    }

    public void begin() {
        setArmingCondition(new NewFrameCondition(TranslationAnimationProcessor.this));
    }

    public void end() {
        setArmingCondition(null);
    }

    public void pause() {
        setArmingCondition(null);
    }

    public void resume() {
        setArmingCondition(new NewFrameCondition(TranslationAnimationProcessor.this));
    }
        

}
