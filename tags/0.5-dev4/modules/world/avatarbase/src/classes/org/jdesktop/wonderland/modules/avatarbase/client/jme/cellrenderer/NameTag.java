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


import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.TransformChangeListener;
import org.jdesktop.wonderland.client.cell.TransformChangeListener.ChangeSource;

import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;

import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.SceneWorker;



/**
 * @author jprovino
 */
public class NameTag implements TransformChangeListener {

    private final Cell cell;
    private final String name;
    private final float height;
    private Node nameTagRoot;
    private Entity labelEntity;
    private Spatial q;

    /**
     * @deprecated
     */
    public NameTag(Cell cell, String name, float height) {
        this(cell, name, height, null);
    }

    public NameTag(Cell cell, String name, float height, Entity parentEntity) {
        this.cell = cell;
        this.name = name;
        this.height = height;

        cell.addTransformChangeListener(this);

        WorldManager worldManager = ClientContextJME.getWorldManager();

        Vector3f translation = new Vector3f();

        cell.getLocalTransform().getTranslation(translation);

        nameTagRoot = new Node();
        nameTagRoot.setLocalTranslation(translation);

        ZBufferState zbuf = (ZBufferState) worldManager.getRenderManager().createRendererState(RenderState.RS_ZBUFFER);
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        nameTagRoot.setRenderState(zbuf);

        labelEntity = new Entity("NameTag");
        labelEntity.addComponent(RenderComponent.class,
                worldManager.getRenderManager().createRenderComponent(nameTagRoot));
        setNameTag(name);

//        if (parentEntity!=null) {
//            System.err.println("ADDING NAMETAG TO "+parentEntity);
//            parentEntity.addEntity(labelEntity);
//        } else
            worldManager.addEntity(labelEntity);
    }

    public void transformChanged(Cell cell, ChangeSource source) {
        if (cell.getCellID().equals(this.cell.getCellID()) == false) {
            System.out.println("SHOULDN'T HAPPEN!");
            return;
        }

        Vector3f translation = new Vector3f();
        cell.getLocalTransform().getTranslation(translation);
        setLocalTranslation(translation);
    }

    public void setLocalTranslation(final Vector3f pos) {
        if (nameTagRoot != null) {
            SceneWorker.addWorker(new WorkCommit() {
                public void commit() {
                    nameTagRoot.setLocalTranslation(pos);
                    ClientContextJME.getWorldManager().addToUpdateList(nameTagRoot);
                }
            });
        }
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

    public void done() {
        removeNameTag();

        cell.removeTransformChangeListener(this);
    }

    private void removeNameTag() {
        if (nameTagRoot == null) {
            return;
        }

        WorldManager worldManager = ClientContextJME.getWorldManager();
        worldManager.removeEntity(labelEntity);
        labelEntity.removeComponent(RenderComponent.class);
        nameTagRoot.detachChild(q);
        worldManager.addToUpdateList(nameTagRoot);
        return;
    }

    public void setNameTag(String name) {
        if (q != null) {
            nameTagRoot.detachChild(q);
        }

        TextLabel2D label = new TextLabel2D(name);
        q = label.getBillboard(0.3f);
        q.setLocalTranslation(0, height, 0);

        Matrix3f rot = new Matrix3f();
        rot.fromAngleAxis((float) Math.PI, new Vector3f(0f, 1f, 0f));
        q.setLocalRotation(rot);

        nameTagRoot.attachChild(q);

        Vector3f pos = new Vector3f();
        cell.getLocalTransform().getTranslation(pos);
        nameTagRoot.setLocalTranslation(pos);
        ClientContextJME.getWorldManager().addToUpdateList(nameTagRoot);
    }
}
