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
package org.jdesktop.wonderland.modules.xremwin.client;

import com.jme.math.Vector2f;
import java.math.BigInteger;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.WindowConventional;

/**
 * The Xremwin window class. 
 *
 * @author deronj
 */
@ExperimentalAPI
public class WindowXrw extends WindowConventional {

    /** The X11 window ID */
    private int wid;
    /** 
     * Non-zero indicates that the window is a transient window and the value is the window it is 
     * transient for. TODO: write code that uses this.
     */
    private WindowXrw winTransientFor;
    /** A temporary buffer used by syncSlavePixels. */
    private byte[] tmpPixelBytes;

    /**
     * Create a new WindowXrw instance and its "World" view.
     *
     * @param app The application to which this window belongs.
     * @param x The X11 x coordinate of the top-left corner window.
     * @param y The X11 y coordinate of the top-left corner window.
     * @param borderWidth The X11 border width.
     * @param decorated Whether the window is decorated with a frame.
     * @param pixelScale The size of the window pixels.
     * @param wid The X11 window ID.
     * @throws Instantiation if the window cannot be created.
     */
    WindowXrw(App2D app, int x, int y, int width, int height, int borderWidth,
              boolean decorated, Vector2f pixelScale, int wid) 
        throws InstantiationException {

        super(app, width, height, decorated, borderWidth, pixelScale,
              "WindowXrw " + wid + " for app " + app.getName());

        this.wid = wid;

        // Determine whether this window is transient for another
        int transientForWid = ((AppXrw) app).getTransientForWid(wid);
        if (transientForWid != 0) {
            winTransientFor = AppXrw.widToWindow.get(transientForWid);
        }

        setOffset(x, y);
    }

    /**
     * Clean up resources.
     */
    @Override
    public void cleanup() {
        super.cleanup();
        winTransientFor = null;
    }

    /**
     * Returns the window's wid.
     */
    public int getWid() {
        return wid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userClose() {
        super.userClose();

        // Notify the Xremwin server and other clients
        ((AppXrw) app).getClient().closeWindow(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userToFront() {
        super.toFront();

        // Notify the Xremwin server and other clients
        ((AppXrw) app).getClient().windowToFront(this);
    }

    /**
     * Change the visibility of the window 
     *
     * @param visible Whether the window should be visible.
     * @param winTransientFor If non-null, the window whose visibility is being changed
     * is a transient window for winTransientFor.
     */
    public void setVisibleApp(boolean visible, WindowXrw winTransientFor) {
        this.winTransientFor = winTransientFor;
        super.setVisibleApp(visible);
    }

    /** 
     * Returns the window for which this window is a transient.
     */
    public WindowXrw getTransientFor() {
        return winTransientFor;
    }

    /** 
     * Used only by App Base Master on a popup window: Specify the parent of this popup.
     */
    public void setPopupParent(WindowXrw parent) {
        // TODO
    }

    /**
     * Specify the user who is now controlling the application to which this window belongs.
     *
     * @param userName The controlling user.
     */
    public void setControllingUser(String userName) {
        ((ControlArbXrw) app.getControlArb()).setController(userName);
    }

    /**
     * Returns the name of the controlling user.
     */
    public String getControllingUser() {
       // TODO: return ((ControlArbXrw) app.getControlArb()).getController();
       return null;
    }

    /**
     * Used only by App Base Master during a new slave connection.
     * Sends the pixels of the entire window to the given slave.
     *
     * @param slaveID The slave to which to send the window pixels.
     */
    public void syncSlavePixels(BigInteger slaveID) {

        // Resize temporary buffer (if necessary)
        int numBytes = getWidth() * getHeight() * 4;
        if (tmpPixelBytes == null || tmpPixelBytes.length < numBytes) {
            tmpPixelBytes = new byte[numBytes];
        }

        // Get pixels in a byte array
        getPixelBytes(tmpPixelBytes, 0, 0, getWidth(), getHeight());

        ClientXrw client = ((AppXrw) app).getClient();
        ((ClientXrwMaster) client).writeSyncSlavePixels(slaveID, tmpPixelBytes);
    }
}
