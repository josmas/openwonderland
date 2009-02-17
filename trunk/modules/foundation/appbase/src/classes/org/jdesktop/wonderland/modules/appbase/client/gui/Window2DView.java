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
package org.jdesktop.wonderland.modules.appbase.client.gui;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import com.jme.image.Texture;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;

/**
 * A window view is a visual representation of a window. A window can
 * have multiple views. Each view has a space in which it resides,
 * for example the world, the HUD, etc. 
 *
 * NOTE: concrete subclasses provide static factory creation methods.
 *
 * @author deronj
 */
@ExperimentalAPI
public abstract class Window2DView extends WindowView {

    /** A change flag which indicates all attributes of the window changed */
    public static final int CHANGED_ALL = -1;
    /** A change flag which indicates that the window visibility changed */
    public static final int CHANGED_VISIBILITY = 0x01;
    /** A change flag which indicates that the window transform changed */
    public static final int CHANGED_TRANSFORM = 0x02;
    /** A change flag which indicates that the window size changed */
    public static final int CHANGED_SIZE = 0x04;
    /** A change flag which indicates that the app's window stack changed */
    public static final int CHANGED_STACK = 0x08;
    /** A change flag which indicates that the top level attribute of the window changed */
    public static final int CHANGED_TOP_LEVEL = 0x10;
    /** A change flag which indicates that the window title changed */
    public static final int CHANGED_TITLE = 0x20;
    /** The texture of the window. */
    protected Texture texture;

    /** 
     * Create a new instance of Window2DView.
     *
     * @param window The window this view displays.
     * @param spaceName The GUI space in which the view resides.
     */
    public Window2DView(Window2D window, String spaceName) {
        super(window, spaceName);
    }

    /** 
     * Clean up resources 
     */
    @Override
    public void cleanup() {
        super.cleanup();
        texture = null;
    }

    /**
     * Updates all view based on current window state.
     *
     * @param changeMask This is an OR of all of the change flags (e.g. Window2DView.CHANGED_xxxx).
     * These indicate which window attributes have changed. These changes will be reflected in the view.
     */
    public abstract void update(int changeMask);

    /** 
     * Converts the given 3D mouse event into a 2D event and forwards it along  to the view's controlArb.
     *
     * @param window The window this view displays.
     * @param me3d The 3D mouse event to deliver.
     */
    public abstract void deliverEvent(Window2D window, MouseEvent3D me3d);

    // TODO: temporary until ImageGraphics can be fixed to allocated texture id when necessary
    public void forceTextureIdAssignment() {
    }

    /**
     * Enables/disables hud mode. When in hud mode the size, position, and stacking of the view
     * are determined by the methods setHudLocation, setHudSize, setHudConfiguration and
     * setHudZOrder. When in perspective mode the size, position and stacking are determined 
     * by the view's parent window and containing cell attributes.
     *
     * The default is enable = false.
     *
     * TODO: Temporary only. Will later be obsoleted by WindowView.addToHUD/removeFromHUD.
     *
     * @param enable If true the view is in hud mode, otherwise it is in perspective (world) mode.
     */
    public abstract void setHud (boolean enable);
    
    /**
     * Specifies the location of the view when it is hud mode. The units are pixels.
     * The origin of the HUD is the lower-left corner of the client window's drawing subwindow.
     * the width and height of the HUD plane are the same as the width and height of the subwindow.
     * Furthermore, (x, y) specifies the position of the CENTER of the view relative to HUD origin.
     *
     * @param x The x location.
     * @param y The y location.
     */
    public abstract void setHudLocation (int x, int y);

    /**
     * Returns the X location of the view when it is in hud mode.
     */
    public abstract int getHudX ();

    /**
     * Returns the X location of the view when it is in hud mode.
     */
    public abstract int getHudY ();

    /**
     * Specifies the size of the view in pixels when it is in hud mode. 
     * @param width The width of the view.
     * @param height The height of the view.
     */
    public abstract void setHudSize(int width, int height);

    /**
     * Returns the width of the view when it is in hud mode.
     */
    public abstract int getHudWidth ();

    /**
     * Returns the height of the view when it is in hud mode.
     */
    public abstract int getHudHeight ();

    /**
     * Effectively the same as setHudLocation(x, y) followed by setHudSize(width, height) except
     * that the change happens atomically with respect to rendering.
     */
    public abstract void setHudConfiguration(int x, int y, int width, int height);
    
    /**
     * Specify the hud Z order of the view when it is in hud mode.
     *
     * TODO: temporary: this will be obsoleted by the HUD stack.
     */
    public abstract void setHudZOrder (int zOrder);
}
