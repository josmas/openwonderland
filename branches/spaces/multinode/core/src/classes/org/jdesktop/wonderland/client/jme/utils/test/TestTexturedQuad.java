/*
 * Copyright (c) 2003-2007 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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

package org.jdesktop.wonderland.client.jme.utils.test;

import com.jme.app.SimpleGame;
import com.jme.image.Texture;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.state.RenderState;
import com.jme.util.TextureManager;
import org.jdesktop.wonderland.client.jme.utils.TexturedQuad;

public class TestTexturedQuad extends SimpleGame {

    private TexturedQuad quad;
    //private Quad quad;

    private Quaternion rotQuat = new Quaternion();

    private float angle = 0;

    private Vector3f axis = new Vector3f(0, 1, 0);

    public static void main(String[] args) {
        TestTexturedQuad app = new TestTexturedQuad();
        app.setDialogBehaviour(FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        app.start();
    }

    /**
     * Rotates the dome
     */
    protected void simpleUpdate() {
        if (tpf < 1) {
            angle = angle + (tpf * 1);
            if (angle > 360) {
                angle = 0;
            }
        }
        rotQuat.fromAngleAxis(angle, axis);
        quad.setLocalRotation(rotQuat);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.jme.app.SimpleGame#initGame()
     */
    protected void simpleInitGame() {
        display.setTitle("Wonderland JME - TexturedQuad");
        lightState.setEnabled(false);

	Texture texture = TextureManager.loadTexture(
			      "/home/dj/jme/cvs/jme/src/jmetest/data/images/Monkey.jpg",
			      Texture.MM_LINEAR_LINEAR,
			      Texture.FM_LINEAR);
	//texture.setWrap(Texture.WM_WRAP_S_WRAP_T);
	texture.setApply(Texture.AM_REPLACE);

	quad = new TexturedQuad(texture, "TexturedQuad", 20, 20);
	//quad = new Quad("Quad", 20, 20);
        quad.setLocalTranslation(new Vector3f(0, 0, -40));

        rootNode.attachChild(quad);
    }
}