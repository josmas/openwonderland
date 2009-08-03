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

import java.awt.Color;
import java.awt.Font;

import java.util.HashMap;

/**
 * @author jprovino
 */
public class NameTag implements TransformChangeListener {

    public static final Color SPEAKING_COLOR = Color.RED;
    public static final Color NOT_SPEAKING_COLOR = new Color(1f, 1f, 1f);
    public static final Color CONE_OF_SILENCE_COLOR = Color.BLACK;

    public static final Font REAL_NAME_FONT = Font.decode("Sans PLAIN 40");
    public static final Font ALIAS_NAME_FONT = Font.decode("Sans ITALIC 40");

    private Color foregroundColor = NOT_SPEAKING_COLOR;;

    private Color backgroundColor = new Color(0f, 0f, 0f);

    private Font font = REAL_NAME_FONT;

    public static final String LEFT_MUTE = "[";
    public static final String RIGHT_MUTE = "]";
    public static final String SPEAKING = "...";

    public enum EventType {
        STARTED_SPEAKING,
        STOPPED_SPEAKING,
        MUTE,
        UNMUTE,
        CHANGE_NAME,
	ENTERED_CONE_OF_SILENCE,
	EXITED_CONE_OF_SILENCE
    }

    private final Cell cell;
    private final float height;
    private String name;
    private Node nameTagRoot;
    private Entity labelEntity;
    private Spatial q;

    private String usernameAlias;

    private static HashMap<Cell, NameTag> cellNameTags = new HashMap();

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

	cellNameTags.put(cell, this);

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

    public static NameTag findNameTag(Cell cell) {
	return cellNameTags.get(cell);
    }

    public void transformChanged(Cell cell, ChangeSource source) {
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

    private boolean done;

    public void done() {
	if (done) {
	    return;
	}

	done = true;

	cellNameTags.remove(cell);

	/*
	 * Done automatically when cell is removed.
	 */
        //cell.removeTransformChangeListener(this);

        WorldManager worldManager = ClientContextJME.getWorldManager();
        worldManager.removeEntity(labelEntity);
        labelEntity.removeComponent(RenderComponent.class);
        nameTagRoot.detachChild(q);
        worldManager.addToUpdateList(nameTagRoot);
    }

    public static String getDisplayName(String name, boolean isSpeaking, boolean isMuted) {
        if (isMuted) {
            return LEFT_MUTE + name + RIGHT_MUTE;
        }

        if (isSpeaking) {
            return name + SPEAKING;
        }

        return name;
    }

    public static String getUsername(String name) {
	String s = name.replaceAll("\\" + LEFT_MUTE, "");

	s = s.replaceAll("\\" + RIGHT_MUTE, "");

	return s.replaceAll("\\" + SPEAKING, "");
    }

    public void setForegroundColor(Color foregroundColor) {
	this.foregroundColor = foregroundColor;
    }

    public Color getForegroundColor() {
	return foregroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
	this.backgroundColor = backgroundColor;
    }

    public Color getBackgroundColor() {
	return backgroundColor;
    }

    public void setFont(Font font) {
	this.font = font;
    }

    private boolean inConeOfSilence;

    public void setNameTag(EventType eventType, String username, String usernameAlias) {
	setNameTag(eventType, username, usernameAlias, null, null);
    }

    public void setNameTag(EventType eventType, String username, String usernameAlias, 
	    Color foregroundColor, Font font) {

	this.usernameAlias = usernameAlias;

	if (eventType == EventType.ENTERED_CONE_OF_SILENCE) {
	    inConeOfSilence = true;
	} else if (eventType == EventType.EXITED_CONE_OF_SILENCE) {
	    inConeOfSilence = false;
	    setForegroundColor(NOT_SPEAKING_COLOR);
	}
	    
	String displayName = usernameAlias;

	switch (eventType) {
	case STARTED_SPEAKING:
 	    displayName = getDisplayName(usernameAlias, true, false);
	    setForegroundColor(SPEAKING_COLOR);
	    break;
	
	case STOPPED_SPEAKING:
 	    displayName = getDisplayName(usernameAlias, false, false);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;

	case MUTE:
 	    displayName = getDisplayName(usernameAlias, false, true);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;
	
	case UNMUTE:
 	    displayName = getDisplayName(usernameAlias, false, false);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;

	case CHANGE_NAME:
	    break;
	}

	if (inConeOfSilence) {
	    setForegroundColor(CONE_OF_SILENCE_COLOR);
	}

	if (name.equals(usernameAlias) == false) {
	    setFont(ALIAS_NAME_FONT);
	}

	if (foregroundColor != null) {
	    setForegroundColor(foregroundColor);
	}

	if (font != null) {
	    setFont(font);
	}

	setNameTag(displayName);
    }

    public void setNameTag(String name) {
        if (q != null) {
            nameTagRoot.detachChild(q);
        }

        TextLabel2D label = new TextLabel2D(name, foregroundColor, backgroundColor);

	if (font != null) {
	    label.setFont(font);
	}

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