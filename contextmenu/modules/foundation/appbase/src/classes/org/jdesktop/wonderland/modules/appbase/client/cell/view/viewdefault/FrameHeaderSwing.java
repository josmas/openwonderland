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
import java.awt.Component;
import java.awt.Color;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwing;
import org.jdesktop.wonderland.modules.appbase.client.view.Gui2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;

/**
 * The frame header (top side) for Frame2DCellSwing. Uses a WindowSwing.
 *
 * @author deronj
 */
@ExperimentalAPI
public class FrameHeaderSwing
    extends FrameComponent
    implements HeaderPanel.Container, MouseListener
{
    // TODO: New UI: add zones: move planar, move z, rotate
    private WindowSwing headerWindow;

    /** The AWT background color of the header window. */
    private Color bkgdColor;

    /** The panel displayed in the frame. */
    private HeaderPanel headerPanel;

    /** The app of the window of the view which this frame decorates. */
    private App2D app;

    /** The view of this header. */
    private View2DCell view;

    /** Whether the frame is visible. */
    private boolean visible;

    /**
     * Create a new instance of FrameHeaderSwing.
     *
     * @param view The view the frame encloses.
     * @param closeListeners The listeners to be notified when the header's close button is pressed.
     */
    public FrameHeaderSwing(View2DCell view, LinkedList<Frame2DCell.CloseListener> closeListeners) {
        super("FrameHeaderSwing for " + view, view, null);

        this.view = view;
        Window2D viewWindow = view.getWindow();
        app = viewWindow.getApp();
        headerWindow = new WindowSwing(app, Window2D.Type.POPUP, viewWindow, 1, 1, false,
                view.getPixelScale(), "Header Window for " + view.getName());
        headerWindow.setCoplanar(true);

        headerPanel = new HeaderPanel();
        JmeClientMain.getFrame().getCanvas3DPanel().add(headerPanel);
        headerPanel.setContainer(this);
        headerWindow.setComponent(headerPanel);

        // TODO: window close: maintain list of close listeners
        // Call them on close button press.

        headerPanel.addMouseListener(this);

        // Unless we do this the interior of the frame will deliver events 
        // to the control arb of the application and they will look like they
        // are coming from the interior of the main window's view. We don't want this.
        View2DDisplayer displayer = view.getDisplayer();
        View2D frameView = headerWindow.getView(displayer);
        ((View2DEntity)frameView).disableGUI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        super.cleanup();

        setVisible(false);

        if (headerWindow != null) {
            headerWindow.cleanup();
            headerWindow = null;
        }

        headerPanel.removeMouseListener(this);
        view = null;
    }

    /**
     * Specify the visibility of this header.
     */
    public void setVisible (boolean visible) {
        if (this.visible == visible) return;
        this.visible = visible;
        headerWindow.setVisibleApp(visible);
        headerWindow.setVisibleUser(view.getDisplayer(), visible);
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

        // Calculate preferred height
        headerWindow.setSize(null);
        headerWindow.validate();
        Component embeddedComp = headerWindow.getComponent();
        int preferredHeight = embeddedComp.getHeight();
        
        // Calculate size. This is essentially the same as for FrameSide TOP, but converted to pixels.
        float innerWidth = view.getDisplayerLocalWidth();
        float innerHeight = view.getDisplayerLocalHeight();
        float sideThickness = Frame2DCell.SIDE_THICKNESS;
        int width = (int) ((innerWidth + 2f * sideThickness) / pixelScale.x);
        int height = preferredHeight;

        // Calculate the pixel offset of the upper-left of the header relative to the 
        // upper-left of the view. Note that we need to calculate x so that the header
        // left side aligns with the left side of the frame.
        int x = (int) (-sideThickness / pixelScale.x);
        int y = -height;

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
            headerPanel.setTitle(title);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setColor(ColorRGBA color) {
        bkgdColor = new Color(color.r, color.g, color.b, color.a);
        if (headerPanel != null) {
            headerPanel.setBackground(bkgdColor);
        }
    }

    /**
     * {@inheritDoc}
     */
    public ColorRGBA getColor() {
        return new ColorRGBA(bkgdColor.getRed()/255.0f, 
                             bkgdColor.getGreen()/255.0f, 
                             bkgdColor.getBlue()/255.0f, 
                             bkgdColor.getAlpha()/255.0f);
    }


    public void	mouseClicked(MouseEvent e) {
        if  (Gui2D.isChangeControlEvent(e)) {
            ControlArb appControlArb = app.getControlArb();
            if (appControlArb.hasControl()) {
                appControlArb.releaseControl();
            } else {
                appControlArb.takeControl();
            }
        } 
    }

    public void mouseEntered(MouseEvent e) {
    }
    public void	mouseExited(MouseEvent e) {
    }
    public void	mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
}

