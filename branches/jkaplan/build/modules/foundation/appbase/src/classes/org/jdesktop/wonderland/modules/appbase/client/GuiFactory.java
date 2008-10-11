/**
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
package org.jdesktop.wonderland.modules.appbase.client;

import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A factory object used for creating the user-visible objects of an app. There are two such objects:
 * <br><br>
 * 1. WindowView: The user-visible representation of a window. The term "view" is also used. The view
 * is created in some "Gui Space." This is an area of the Wonderland GUI that displays visible objects
 * to the user. At the time of this writing, the two spaces which exist are:
 * <br><br>
 * + World: The 3D virtual world space displayed in the main subwindow of the Wonderland client.
 * <br><br>
 * + HUD: The 2D planar overlay which exists "on top" of the world.
 * <br><br>
 * 2. WindowFrame: A WindowFrame is created for a particular view. It is a border (or "decoration") 
 * around the view which provides various information about the window and controls for manipulating the window.
 * A frame is displayed in the same space as the view it encloses.
 * <br><br>
 * A GuiFactory is created by an AppType and is returned by the getGuiFactory method of the AppType. 
 * <br><br>
 * Note on 2D apps: Unless overridden, the AppType2D.getGuiFactory method returns an instance of 
 * org.jdesktop.wonderland.modules.appbase.client.gui.guidefault.Gui2DFactoryDefault. This class provides
 * the default GUI for 2D apps in Wonderland. World views are displayed using a JME 3D geometry object with 
 * the window contents in a JME texture mapped to this geometry object. World frames are also implemented using JME
 * 3D objects.
 *
 * @author deronj
 */

@ExperimentalAPI
public interface GuiFactory {

    /**
     * Create a renderer object which will arrange for the given cell to be rendered.
     * @param cell The cell to be rendered.
     * @return The renderer created. 
     */
    public AppCellRenderer createCellRenderer (AppCell cell);

    /**
     * Create a window view object which displays the given window in the specified space.
     *
     * @param window The window the view will display.
     * @param spaceName The name of the space in which the view will reside.
     * @return The view created. Null indicates that this factory doesn't support the given spaceName.
     */
    public WindowView createView (Window window, String spaceName);

    /**
     * Create a window frame which wraps the view of a window. 
     *
     * @param view The view to wrap with a frame.
     * @return The frame created. 
     */
    public WindowFrame createFrame (WindowView view);
}

