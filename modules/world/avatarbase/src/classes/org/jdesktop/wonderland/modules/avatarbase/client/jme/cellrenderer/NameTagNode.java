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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.Spatial;

import java.awt.Color;
import java.awt.Font;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;



import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTag.EventType;



/**
 * @author paulby
 */
public class NameTagNode extends Node {

    private final String tagName;
    private final float height;
    private Spatial q;

    public NameTagNode(String name, float height) {
        this.tagName = name;
        this.height = height;

        WorldManager worldManager = ClientContextJME.getWorldManager();

        setNameTagImpl(name);

    }

    public void setSpeaking(boolean isSpeaking) {
        String tmp = this.tagName;

        if (isSpeaking) {
            tmp += "...";
        }

        setNameTag(tmp);
    }

    public void setMute(boolean isMuted) {
        String tmp = this.tagName;

        if (isMuted) {
            tmp = "[" + tmp + "]";
        }

        setNameTag(tmp);
    }

    public void setNameTag(final String name) {
        ClientContextJME.getSceneWorker().addWorker(new WorkCommit() {
            public void commit() {
                setNameTagImpl(name);
                ClientContextJME.getWorldManager().addToUpdateList(NameTagNode.this);
            }
        });

    }

    void setNameTag(EventType eventType, String username, String usernameAlias, Color foregroundColor, Font font) {
        setNameTag(username);
    }

    private void setNameTagImpl( String name) {
        if (q != null) {
            detachChild(q);
        }

        TextLabel2D label = new TextLabel2D(name);
        q = label.getBillboard(0.3f);
        q.setLocalTranslation(0, height, 0);

        Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis((float) Math.PI, new Vector3f(0f, 1f, 0f));
        q.setLocalRotation(rot);

        attachChild(q);
    }
}
