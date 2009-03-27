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
import com.jme.scene.state.RenderState;
import com.jme.scene.state.ZBufferState;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.mtgame.WorldManager;
import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;

import org.jdesktop.wonderland.client.ClientContext;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.MovableAvatarComponent;
import org.jdesktop.wonderland.client.cell.MovableComponent;

import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;

import org.jdesktop.wonderland.common.cell.CellTransform;

import imi.scene.PMatrix;
import imi.character.CharacterMotionListener;

/**
 * @author jprovino
 */
public class NameTag {
    
    private Entity entity;

    private final Cell cell;
    private final String name;

    public NameTag(Cell cell, String name) {
	this.cell = cell;
	this.name = name;
    }

    private CharacterMotionListener characterMotionListener;

    private void addGlobalListener() {
        characterMotionListener = new CharacterMotionListener() {
            public void transformUpdate(Vector3f translation, PMatrix rotation) {
                ((MovableAvatarComponent)cell.getComponent(MovableComponent.class)).localMoveRequest(new CellTransform(rotation.getRotation(), translation));

                final Vector3f pos = new Vector3f(translation);
                SceneWorker.addWorker(new WorkCommit() {
                    public void commit() {
			setLocalTranslation(pos);
                    }
                });
            }
        };
    }

    public void setLocalTranslation(Vector3f pos) {
	nameTagRoot.setLocalTranslation(pos);
	ClientContextJME.getWorldManager().addToUpdateList(nameTagRoot);
    }

    public void setSpeaking(boolean isSpeaking) {
	String name = this.name;

	if (isSpeaking) {
	    name += "...";
	}

	setNameTag(name);
    }

    public void setMute(boolean isMuted) {
	String name = this.name;

	if (isMuted) {
	    name = "[" + name + "]";
	}

	setNameTag(name);
    }

    private Node nameTagRoot;

    private Entity labelEntity;
    private Spatial q;

    public void done() {
	if (labelEntity != null) {
	    WorldManager worldManager = ClientContextJME.getWorldManager();
            worldManager.removeEntity(labelEntity);
            labelEntity.removeComponent(RenderComponent.class);
	    nameTagRoot.detachChild(q);
            worldManager.addToUpdateList(nameTagRoot);
	}
    }

    public void setNameTag(String name) {
	WorldManager worldManager = ClientContextJME.getWorldManager();

	Vector3f localTranslation = null;

	if (labelEntity != null) {
            worldManager.removeEntity(labelEntity);
            labelEntity.removeComponent(RenderComponent.class);
	    nameTagRoot.detachChild(q);
            worldManager.addToUpdateList(nameTagRoot);
	    localTranslation = nameTagRoot.getLocalTranslation();
	}

	labelEntity = new Entity("NameTag");
        TextLabel2D label = new TextLabel2D(name);
        q = label.getBillboard(0.3f);

	Vector3f translation = new Vector3f();

	cell.getLocalTransform().getTranslation(translation);

        q.setLocalTranslation(translation.getX(), translation.getY() + (float) .17, translation.getZ());

        //q.setLocalTranslation(0f, 2f, 0f);
        Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis((float) Math.PI, new Vector3f(0f,1f,0f));
        q.setLocalRotation(rot);

        nameTagRoot = new Node();
        nameTagRoot.attachChild(q);

        ZBufferState zbuf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        nameTagRoot.setRenderState(zbuf);

        labelEntity.addComponent(RenderComponent.class, worldManager.getRenderManager().createRenderComponent(nameTagRoot));
        ClientContextJME.getWorldManager().addEntity(labelEntity);

	if (localTranslation != null) {
	    nameTagRoot.setLocalTranslation(localTranslation);
	}

        worldManager.addToUpdateList(nameTagRoot);
    }

}
