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

import org.jdesktop.mtgame.processor.WorkProcessor.WorkCommit;

import org.jdesktop.wonderland.client.jme.utils.TextLabel2D;

import org.jdesktop.wonderland.client.jme.ClientContextJME;

import java.awt.Color;
import java.awt.Font;

import java.util.HashMap;

/**
 * TODO make this a component
 *
 * @author jprovino
 */
public class NameTagNode extends Node {

    public static final Color SPEAKING_COLOR = Color.RED;
    public static final Color NOT_SPEAKING_COLOR = new Color(1f, 1f, 1f);
    public static final Color CONE_OF_SILENCE_COLOR = Color.BLACK;

    public static final String DEFAULT_FONT_NAME = "Sans";
    public static final String DEFAULT_FONT_NAME_TYPE = "PLAIN";
    public static final String DEFAULT_FONT_ALIAS_TYPE = "ITALIC";

    public static final int SMALL_FONT_SIZE = 10;
    public static final int DEFAULT_FONT_SIZE = 40;
    public static final int LARGE_FONT_SIZE = 50;

    public static final Font REAL_NAME_FONT = 
        fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_NAME_TYPE, DEFAULT_FONT_SIZE);

    public static final Font ALIAS_NAME_FONT = 
        fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_ALIAS_TYPE, DEFAULT_FONT_SIZE);

    private Color foregroundColor = NOT_SPEAKING_COLOR;
    
    private Color backgroundColor = new Color(0f, 0f, 0f);

    private Font font = REAL_NAME_FONT;

    public static final String LEFT_MUTE = "[";
    public static final String RIGHT_MUTE = "]";

    public static final String SPEAKING = "...";

    private boolean done;

    private TextLabel2D label=null;

    public enum EventType {
        STARTED_SPEAKING,
        STOPPED_SPEAKING,
        MUTE,
        UNMUTE,
        CHANGE_NAME,
	ENTERED_CONE_OF_SILENCE,
	EXITED_CONE_OF_SILENCE,
	HIDE,
	SMALL_FONT,
	REGULAR_FONT,
	LARGE_FONT
    }
    private final float height;
    private String name;
    private Spatial q;
    private String usernameAlias;
    private boolean visible;
    private int fontSize;

    private static HashMap<String, NameTagNode> nameTagMap = new HashMap();

    private static Font fontDecode (String fontName, String fontType, int fontSize) {
        return Font.decode(fontName + " " + fontType + " " + fontSize);
    }

    public NameTagNode(String name, float height) {
        this.name = name;
        this.height = height;
        visible = true;

	nameTagMap.put(name, this);

        setNameTag(name);
    }

    public void done() {
        if (done) {
            return;
        }

        done = true;

	nameTagMap.remove(name);
    
        detachChild(q);
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
        updateFont();
    }

    /**
     * Sets the font to a font of the default name but with the given size.
     */
    public void setSizedDefaultFont(int fontSize) {
        this.fontSize = fontSize;
        Font newFont;
        if (name.equals(usernameAlias) == false) {
            newFont = fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_ALIAS_TYPE, fontSize);
        } else {
            newFont = fontDecode(DEFAULT_FONT_NAME, DEFAULT_FONT_NAME_TYPE, fontSize);
	}
        setFont(newFont);
    }

    /**
     * Controls whether the name tag is actually visible.
     */
    public void setVisible (boolean visible) {
        this.visible = visible;
        setNameTag(name);
    }

    /**
     * Returns whether the name tag is visible. 
     */
    public boolean isVisible () {
        return visible;
    }

    public static void setOtherNameTags(EventType eventType, String username, String usernameAlias) {
	String[] keys = nameTagMap.keySet().toArray(new String[0]);

	for (int i = 0; i < keys.length; i++) {
	    if (keys[i].equals(username)) {
		continue;
	    }

	    NameTagNode nameTag = nameTagMap.get(keys[i]);
	    nameTag.setNameTag(eventType, username, usernameAlias);
	}
    }

    private boolean inConeOfSilence;
    private boolean isSpeaking;
    private boolean isMuted;

    public void setNameTag(EventType eventType, String username, String usernameAlias) {
        setNameTag(eventType, username, usernameAlias, foregroundColor, font);
    }

    public void setNameTag(EventType eventType, String username, String usernameAlias,
            Color foregroundColor, Font font) {

        this.usernameAlias = usernameAlias;

	switch (eventType) {
	case HIDE:
	    setVisible(false);
	    return;

	case SMALL_FONT:
	    setSizedDefaultFont(SMALL_FONT_SIZE);
	    setVisible(true);
	    return;
	
	case REGULAR_FONT:
	    setSizedDefaultFont(DEFAULT_FONT_SIZE);
	    setVisible(true);
	    return;
	
	case LARGE_FONT:
	    setSizedDefaultFont(LARGE_FONT_SIZE);
	    setVisible(true);
	    return;
	}
	
	if (eventType == EventType.ENTERED_CONE_OF_SILENCE) {
	    inConeOfSilence = true;
	    return;
	} else if (eventType == EventType.EXITED_CONE_OF_SILENCE) {
	    inConeOfSilence = false;
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    return;
	}
	    
	String displayName = usernameAlias;

	switch (eventType) {
	case STARTED_SPEAKING:
	    isSpeaking = true;
 	    displayName = getDisplayName(usernameAlias, true, false);
	    setForegroundColor(SPEAKING_COLOR);
	    break;
	
	case STOPPED_SPEAKING:
	    isSpeaking = false;
 	    displayName = getDisplayName(usernameAlias, false, false);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;

	case MUTE:
	    isMuted = true;
 	    displayName = getDisplayName(usernameAlias, false, true);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;
	
	case UNMUTE:
	    isMuted = false;
 	    displayName = getDisplayName(usernameAlias, false, false);
	    setForegroundColor(NOT_SPEAKING_COLOR);
	    break;

        case CHANGE_NAME:
	    this.usernameAlias = usernameAlias;
	    displayName = getDisplayName(usernameAlias, isSpeaking, isMuted);
            break;
        }

        if (inConeOfSilence) {
            setForegroundColor(CONE_OF_SILENCE_COLOR);
        }

        if (name.equals(usernameAlias) == false) {
            setFont(ALIAS_NAME_FONT);
        } else {
            setFont(REAL_NAME_FONT);
	}

        if (foregroundColor != null) {
            setForegroundColor(foregroundColor);
        }

        if (font != null) {
            setFont(font);
        }

        setNameTag(displayName);
    }

    public void setNameTag(final String name) {
        ClientContextJME.getSceneWorker().addWorker(new WorkCommit() {

            public void commit() {
                setNameTagImpl(name);
                ClientContextJME.getWorldManager().addToUpdateList(NameTagNode.this);
            }
        });
    }

    private void setNameTagImpl(String name) {
        if (visible && name != null) {
            if (label==null) {
                label = new TextLabel2D(name, foregroundColor, backgroundColor, 0.3f, true, font);
                label.setLocalTranslation(0, height, 0);

                Matrix3f rot = new Matrix3f();
                rot.fromAngleAxis((float) Math.PI, new Vector3f(0f, 1f, 0f));
                label.setLocalRotation(rot);

                attachChild(label);
            } else {
                label.setText(name, foregroundColor, backgroundColor);
            }
        } else {
            if (label != null) {
                detachChild(label);
                label = null;
            }
        }
    }

    private void updateFont () {
        if (label != null && font != null) {
            ClientContextJME.getSceneWorker().addWorker(new WorkCommit() {
                public void commit() {
                    label.setFont(font);
                    ClientContextJME.getWorldManager().addToUpdateList(NameTagNode.this);
                }
            });
        }        
    }
}
