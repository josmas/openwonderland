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
package org.jdesktop.wonderland.modules.appbase.client.cell.view.viewdefault;

import com.jme.math.Vector2f;
import com.jme.renderer.ColorRGBA;
import java.util.LinkedList;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;

/**
 * The frame header (top side) for Frame2DCellSwing. Uses a WindowSwing.
 *
 * @author deronj
 */
@ExperimentalAPI
public class FrameHeaderSwing
        extends FrameComponent
        implements HeaderPanel.Container {

    /** The height of the header */
    private static final float HEADER_HEIGHT = 1.25f;

    // TODO: has: title, controller, close button
    // TODO: has zones: move planar, move z, rotate
    private WindowSwing headerWindow;

    /** The background color of the header window. */
    private ColorRGBA bkgdColor;

    /**
     * Create a new instance of FrameHeaderSwing.
     *
     * @param view The view the frame encloses.
     * @param closeListeners The listeners to be notified when the header's close button is pressed.
     */
    public FrameHeaderSwing(View2DCell view, LinkedList<Frame2DCell.CloseListener> closeListeners) {
        super("FrameHeaderSwing for " + view, view, null);

        Window2D viewWindow = view.getWindow();
        App2D app = viewWindow.getApp();
        headerWindow = new WindowSwing(app, Window2D.Type.POPUP, viewWindow, 1, 1, false,
                view.getPixelScale(), "Header Window for " + view.getName());
        headerWindow.setCoplanar(true);

        HeaderPanel panel = new HeaderPanel();
        JmeClientMain.getFrame().getCanvas3DPanel().add(panel);
        panel.setContainer(this);
        headerWindow.setComponent(panel);

        // TODO: window close: maintain list of close listeners
        // Call them on close button press.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        super.cleanup();

        if (headerWindow != null) {
            headerWindow.cleanup();
            headerWindow = null;
        }
    }

    /**
     * Update the size and position of the header and its subcomponents.
     *
     * @throw InstantiationException if couldn't allocate resources for the visual representation.
     */
    public void update() throws InstantiationException {
        updateLayout();
    }

    /** {@inheritDoc} */
    protected void updateLayout() {
        Vector2f pixelScale = view.getPixelScale();

        // Calculate size. This is essentially the same as for FrameSide TOP, but converted to pixels.
        float innerWidth = view.getDisplayerLocalWidth();
        float innerHeight = view.getDisplayerLocalHeight();
        float sideThickness = Frame2DCell.SIDE_THICKNESS;
        int width = (int) ((innerWidth + 2f * sideThickness) / pixelScale.x);
        int height = (int) (HEADER_HEIGHT / pixelScale.y);

        // Calculate the pixel offset of the upper-left of the header relative to the 
        // upper-left of the view. Note that we need to calculate x so that the header
        // left side aligns with the left side of the frame.
        int x = (int) (-sideThickness / pixelScale.x);
        int y = (int) (-height / pixelScale.y);

        headerWindow.setOffset(x, y);
        headerWindow.setSize(width, height);
    }

    /**
     * Set the title displayed in the header.
     *
     * @param text The new title.
     */
    public void setTitle(String title) {
        if (title != null) {
            //TODO
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setColor(ColorRGBA color) {
        bkgdColor = color.clone();
        // TODO: convert to AWT color and set as bkgd color of panel
    }

    /**
     * {@inheritDoc}
     */
    public ColorRGBA getColor() {
        return bkgdColor.clone();
    }
}

