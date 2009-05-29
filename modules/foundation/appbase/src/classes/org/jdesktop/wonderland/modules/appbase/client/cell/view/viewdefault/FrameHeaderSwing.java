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
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventListenerBaseImpl;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.modules.appbase.client.App2D;
import org.jdesktop.wonderland.modules.appbase.client.ControlArb;
import org.jdesktop.wonderland.modules.appbase.client.ControlArbSingle;
import org.jdesktop.wonderland.modules.appbase.client.Window2D;
import org.jdesktop.wonderland.modules.appbase.client.view.Gui2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DDisplayer;
import org.jdesktop.wonderland.modules.appbase.client.view.View2DEntity;
import org.jdesktop.wonderland.modules.appbase.client.view.WindowSwingHeader;

/**
 * The frame header (top side) for Frame2DCellSwing. Uses a WindowSwing.
 *
 * @author deronj
 */
@ExperimentalAPI
public class FrameHeaderSwing
    extends FrameComponent
    implements HeaderPanel.Container, MouseListener, MouseMotionListener
{
    // TODO: New UI: add zones: move planar, move z, rotate
    private WindowSwingHeader headerWindow;

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

    /* The view of this header in the cell displayer. */
    private View2DEntity frameView;

    /** 
     * An event listener which accepts (consumes) events for this WindowSwing if 
     * it has control. 
     *
     * Note that consumed events are sent directly to Swing,
     * *NOT* to the compute/commitEvent methods of this listener!
     * There is special code in InputPicker to make this happen.
     */
     private EventListenerBaseImpl consumingListener = new ConsumeOnControlListener();

    /** True if a drag is active. */
    private boolean dragging;

    /** The mouse press point in local coordinates. */
    private Vector2f dragStartLocal;

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
        headerWindow = new WindowSwingHeader(app, viewWindow, 1, 1, view.getPixelScale(), 
                                             "Header Window for " + view.getName(), view);
        headerWindow.setCoplanar(true);

        headerPanel = new HeaderPanel();
        JmeClientMain.getFrame().getCanvas3DPanel().add(headerPanel);
        headerPanel.setContainer(this);
        headerWindow.setComponent(headerPanel);

        headerPanel.addMouseListener(this);
        headerPanel.addMouseMotionListener(this);

        // Unless we do this the interior of the frame will deliver events 
        // to the control arb of the application and they will look like they
        // are coming from the interior of the main window's view. We don't want this.
        View2DDisplayer displayer = view.getDisplayer();
        frameView = (View2DEntity) headerWindow.getView(displayer);
        frameView.disableGUI();

        // Arrange for InputPicker to send the event to swing when the app has control.
        consumingListener.addToEntity(frameView.getEntity());
    }

    private class ConsumeOnControlListener extends EventListenerBaseImpl {
        @Override
        public boolean consumesEvent (Event event) {

            /* Note: I thought that I could shut off header events using this technique.
               But it doesn't act as I would suspect. For example, it allows the control
               change (click) event through to WindowSwingEmbeddedToolkit, but the event
               disappears after that! Fortunately, there is an alternate technique that 
               works: check for control in each FrameHeaderSwing mouse event handler.

            if (app.getControlArb().hasControl()) {
                System.err.println("COCL.consumesEvent, true because have control");
                return true;
            }

            // Always let the control change event through, even if app doesn't have control
            if (event instanceof MouseEvent3D) {
                MouseEvent me = (MouseEvent)((MouseEvent3D)event).getAwtEvent();
                boolean result = Gui2D.isChangeControlEvent(me);
                System.err.println("COCL.consumesEvent, result = " + result);
                return result;
            }

            return false;
            */

            return true;
        }
        public boolean propagatesToParent (Event event) {
            return false;
        }
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
        headerPanel.removeMouseMotionListener(this);

        Entity viewEntity = getEntity();
        if (viewEntity != null) {
            consumingListener.removeFromEntity(viewEntity);
        }

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

        headerWindow.setPixelOffset(x, y);
        headerWindow.setSize(width, height);
    }

    /**
     * Set the title displayed in the header.
     *
     * @param text The new title.
     */
    public void setTitle(String title) {
        headerPanel.setTitle(title);
    }

    /**
     * Set the controller displayed in the header.
     *
     * @param text The new controller.
     */
    public void setController(String controller) {
        headerPanel.setController(controller);
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
            return;
        } 

        if (!app.getControlArb().hasControl()) return;

        if (e.getID() == MouseEvent.MOUSE_CLICKED &&
            e.getButton() == MouseEvent.BUTTON1 &&
            e.getModifiersEx() == 0) {
            view.getWindow().restackToTop();
        }
    }

    public void mouseEntered(MouseEvent e) {
        if (!app.getControlArb().hasControl()) return;
    }

    public void	mouseExited(MouseEvent e) {
        if (!app.getControlArb().hasControl()) return;
    }

    public void	mousePressed(MouseEvent e) {

        // Is this a Window menu event? Display menu even when we don't have control.
        if (e.getID() == MouseEvent.MOUSE_PRESSED &&
            e.getButton() == MouseEvent.BUTTON3 &&
            e.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK) {
            view.getWindow().displayWindowMenu(view.getEntity(), e);
            return;
        }

        if (!app.getControlArb().hasControl()) return;

        // TODO: the following drag code only works for secondary windows. Eventually 
        // upgrade it to work with primary windows also.
        if (view.getType() != View2D.Type.SECONDARY) return;

        //System.err.println("******* Press event " + e);

        if (!dragging && 
            e.getButton() == MouseEvent.BUTTON1 &&
            e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {

            dragging = true;

            Vector2f pixelScale = view.getPixelScale();
            dragStartLocal = new Vector2f();
            dragStartLocal.x = e.getX() * pixelScale.x;
            dragStartLocal.y = -e.getY() * pixelScale.y;

            view.userMovePlanarStart();
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (!app.getControlArb().hasControl()) return;

        // TODO: the following drag code only works for secondary windows. Eventually 
        // upgrade it to work with primary windows also.
        if (view.getType() != View2D.Type.SECONDARY) return;

        //System.err.println("******* Drag event " + e);
        if (dragging) {

            Vector2f pixelScale = view.getPixelScale();
            Vector2f dragCurrentLocal = new Vector2f();
            dragCurrentLocal.x = e.getX() * pixelScale.x;
            dragCurrentLocal.y = -e.getY() * pixelScale.y;

            Vector2f dragVectorLocal = dragCurrentLocal.subtractLocal(dragStartLocal);

            System.err.println("dragVectorLocal = " + dragVectorLocal);
            view.userMovePlanarUpdate(new Vector2f(dragVectorLocal.x, dragVectorLocal.y));
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (!app.getControlArb().hasControl()) return;

        // TODO: the following drag code only works for secondary windows. Eventually 
        // upgrade it to work with primary windows also.
        if (view.getType() != View2D.Type.SECONDARY) return;

        if (e.getButton() == MouseEvent.BUTTON1) {
            if (dragging) {
                dragging = false;
                view.userMovePlanarFinish();
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (!app.getControlArb().hasControl()) return;
    }

    // For ortho subwindow debug: set to true to debug ortho subwindows with close button
    private static final boolean orthoSubwindowDebug = false;

    public void close () {
        Window2D viewWindow = view.getWindow();
        if (orthoSubwindowDebug) {
            viewWindow.toggleOrtho();
        } else {
            viewWindow.closeUser();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateControl(ControlArb controlArb) {
        super.updateControl(controlArb);

        if (controlArb instanceof ControlArbSingle) {
            ControlArbSingle ca = (ControlArbSingle) controlArb;
            setController(ca.getController());
        } else {
            // TODO: someday: if it's Multi it would be nice to display the number of users controlling,
            // or actually a list of users controlling.
        }
    }
}

