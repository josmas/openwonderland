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
package org.jdesktop.wonderland.client.jme;

import java.awt.Canvas;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * An interface for interacting with the main JME frame
 * @author jkaplan
 */
public interface MainFrame {
    /**
     * Get the underlying Swing frame object
     * @return the Swing frame
     */
    public JFrame getFrame();

     /**
     * Returns the canvas of the frame.
     */
    public Canvas getCanvas ();

    /**
     * Returns the panel of the frame in which the 3D canvas resides.
     */
    public JPanel getCanvas3DPanel ();

    /**
     * Add the specified menu item to the tool menu.
     *
     * TODO - design a better way to manage the menus and toolsbars
     *
     * @param menuItem
     */
    public void addToToolMenu(JMenuItem menuItem);

    /**
     * Add the specified menu item to the edit menu.
     *
     * TODO - design a better way to manage the menus and toolsbars
     *
     * @param menuItem
     */
    public void addToEditMenu(JMenuItem menuItem);

    /**
     * Set the server URL in the location field
     * @param serverURL the server URL to set
     */
    public void setServerURL(String serverURL);

    /**
     * Add a listener that will be notified when the server URL changes
     * (i.e. when the user types a new location in the location bar)
     * @param listener the listener to add
     */
    public void addServerURLListener(ServerURLListener listener);

    /**
     * A listener that will be notified when the server URL changes
     */
    public interface ServerURLListener {
        /**
         * A request to change the server URL to the given URL
         * @param serverURL the new server URL to connect to
         */
        public void serverURLChanged(String serverURL);

        /**
         * A request to log out of the current server
         */
        public void logout();
    }
}
