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
import org.jdesktop.wonderland.modules.appbase.client.App;
import org.jdesktop.wonderland.modules.appbase.client.Window2DView;
import org.jdesktop.wonderland.modules.appbase.client.WindowConventional;

/**
 * The Xremwin window class. 
 *
 * @author deronj
 */
@ExperimentalAPI
class WindowXrw extends WindowConventional {

    /** The X11 window ID */
    private int wid;
    /** 
     * Non-zero indicates that the window is a transient window and the value is the window it is transient for.
     * TODO: write code that uses this.
     */
    private WindowXrw winTransientFor;
    /** The X11 x coordinate of the top-left corner window */
    private int x;
    /** The X11 y coordinate of the top-left corner window */
    private int y;

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
    WindowXrw(App app, int x, int y, int width, int height, int borderWidth,
            boolean decorated, Vector2f pixelScale, int wid)
            throws InstantiationException {

        // In X11, decorated windows are top-level and have a frame
        super(app, width, height, decorated, borderWidth, pixelScale);
        this.x = x;
        this.y = y;
        this.wid = wid;

        // Determine whether this window is transient for another
        int transientForWid = ((AppXrw) app).getTransientForWid(wid);
        if (transientForWid != 0) {
            winTransientFor = AppXrw.widToWindow.get(transientForWid);
        }
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
     * Sets the window's location within the window system screen.
     * This routine makes the corresponding update the the visual representation of the window.
     *
     * @param x The X coordinate of the window (relative to its parent).
     * @param y The Y coordinate of the window (relative to its parent).
     */
    public void setLocation(int x, int y) {
        update(setLocationNoUpdate(x, y));
    }

    /**
     * Sets the window's location within the window system screen.
     * This routine doesn't change visual representation of the window.
     *
     * @param x The X coordinate of the window (relative to its parent).
     * @param y The Y coordinate of the window (relative to its parent).
     */
    public int setLocationNoUpdate(int x, int y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            return Window2DView.CHANGED_TRANSFORM;
        }
        return 0;
    }

    /** 
     * Returns the window's screen x location 
     */
    public int getX() {
        return x;
    }

    /** 
     * Returns the window's screen y location 
     */
    public int getY() {
        return y;
    }

    /**
     * Set both the window size and the sibling above in the stack in the same call.
     * This routine makes the corresponding update the the visual representation of the window.
     *
     * @param x The X coordinate of the window (relative to its parent).
     * @param y The Y coordinate of the window (relative to its parent).
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param If non-null, the window that is to be positioned below this window in the window stack.
     */
    public void configure(int x, int y, int width, int height, WindowXrw sibWin) {
        update(configureNoUpdate(x, y, width, height, sibWin));
    }

    /**
     * Set both the window size and the sibling above in the stack in the same call.
     * This routine doesn't change visual representation of the window.
     *
     * @param x The X coordinate of the window (relative to its parent).
     * @param y The Y coordinate of the window (relative to its parent).
     * @param width The width of the window (in pixels).
     * @param height The height of the window (in pixels).
     * @param If non-null, the window that is to be positioned below this window in the window stack.
     */
    protected int configureNoUpdate(int x, int y, int width, int height, WindowXrw sibWin) {
        int chgMask = 0;
        chgMask |= setLocationNoUpdate(x, y);
        chgMask |= super.configureNoUpdate(width, height, sibWin);
        return chgMask;
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
    public void setVisible(boolean visible, WindowXrw winTransientFor) {
        this.winTransientFor = winTransientFor;
        super.setVisible(visible);
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
        return ((ControlArbXrw) app.getControlArb()).getController();
    }

    /**
     * Used only by App Base Master during a new slave connection.
     * Sends the pixels of the entire window to the given slave.
     *
     * @param slaveID The slave to which to send the window pixels.
     */
    public void syncSlavePixels(BigInteger slaveID) {
        /* TODO
        BufferedImage image = imageComp.getImage();
        WritableRaster ras = image.getRaster();
        DataBufferInt dataBuf = (DataBufferInt) ras.getDataBuffer();
        int[] srcPixels = dataBuf.getData();

        // TODO: eventually preallocate this and grow the array
        int[] dstPixels = new int[getWidth() * getHeight()];

        extractPixelIntBuf(dstPixels, srcPixels, 0, 0, getWidth(), getHeight(), image.getWidth());

        //System.err.println("performSyncSlavePixels, wh = " + getWidth() + ", " + getHeight());
        //debugPrintSlaveSyncPixels(dstPixels);

        ClientXrw client = ((AppXrw) app).getClient();
        ((ClientXrwMaster) client).writeSyncSlavePixels(slave, dstPixels, getWidth(), getHeight());
         */
    }
}
