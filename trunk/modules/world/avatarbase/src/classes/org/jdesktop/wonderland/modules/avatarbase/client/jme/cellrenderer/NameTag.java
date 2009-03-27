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
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveListener;
import org.jdesktop.wonderland.client.cell.MovableComponent.CellMoveSource;

import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;

import org.jdesktop.wonderland.common.cell.CellTransform;

import imi.scene.PMatrix;
import imi.character.CharacterMotionListener;

/**
 * @author jprovino
 */
public class NameTag implements CellMoveListener {
    
    private Entity entity;

    private final Cell cell;
    private final String name;
    private final float height;

    public NameTag(Cell cell, String name, float height) {
	this.cell = cell;
	this.name = name;
	this.height = height;

	MovableComponent component = cell.getComponent(MovableComponent.class);

	System.out.println("MOVABLE COMPONENT FOR " + cell.getName());

	if (component != null) {
	    component.addServerCellMoveListener(this);
	}

	setNameTag(name);
    }

    public void cellMoved(CellTransform transform, CellMoveSource source) {
	System.out.println("CELL MOVED " + cell.getName());

	Vector3f translation = new Vector3f();
	transform.getTranslation(translation);
	setLocalTranslation(translation);
    }

    public void setLocalTranslation(Vector3f pos) {
	System.out.println("Set local pos " + nameTagRoot + " " + pos);
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

	MovableComponent component = cell.getComponent(MovableComponent.class);

	if (component != null) {
	    component.removeServerCellMoveListener(this);
	}
    }

    public void setNameTag(String name) {
	System.out.println("SETTING  NAME TAG TO " + name);

	WorldManager worldManager = ClientContextJME.getWorldManager();

	Vector3f localTranslation = null;

	if (labelEntity != null) {
            worldManager.removeEntity(labelEntity);
            labelEntity.removeComponent(RenderComponent.class);
	    nameTagRoot.detachChild(q);
            worldManager.addToUpdateList(nameTagRoot);
	    localTranslation = nameTagRoot.getLocalTranslation();
	    System.out.println("TRANSLATION:  " + localTranslation);
	}

	labelEntity = new Entity("NameTag");
        TextLabel2D label = new TextLabel2D(name);
        q = label.getBillboard(0.3f);

	Vector3f translation = new Vector3f();

	cell.getLocalTransform().getTranslation(translation);

	System.out.println("CELL TRANSLATION " + translation);
        q.setLocalTranslation(translation.getX(), translation.getY() + height, translation.getZ());

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
        worldManager.addEntity(labelEntity);

	if (localTranslation != null) {
	    nameTagRoot.setLocalTranslation(localTranslation);
            worldManager.addToUpdateList(nameTagRoot);
	}
    }

}
