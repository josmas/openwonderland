/*
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
package org.jdesktop.wonderland.modules.hud.client;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 * A 2D frame for a HUDComponent2D
 *
 * @author nsimpson
 */
public class HUDFrame2D extends HUDComponent2D
        implements ActionListener, MouseListener, MouseMotionListener {

    private static final Logger logger = Logger.getLogger(HUDFrame2D.class.getName());
    private HUDComponent hudComponent;
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    private List<ActionListener> eventListeners;

    public HUDFrame2D(JComponent component, HUDComponent hudComponent) {
        super(component);
        this.hudComponent = hudComponent;
    }

    public void addActionListener(ActionListener listener) {
        if (eventListeners == null) {
            eventListeners = Collections.synchronizedList(new LinkedList());
        }
        eventListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    public void setTransparent() {
//        Node node = frameView.getNode();
//        Entity entity = frameView.getEntity();
//        WorldManager wm = ClientContextJME.getWorldManager();
//        BlendState as = (BlendState) wm.getRenderManager().createRendererState(RenderState.RS_BLEND);
//        as.setEnabled(true);
//        as.setBlendEnabled(true);
//        as.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
//        as.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
//        node.setRenderState(as);
//        RenderComponent rc = entity.getComponent(RenderComponent.class);
//        AlphaProcessor proc = new AlphaProcessor("", wm, rc, 0.01f);
//        entity.addComponent(AlphaProcessor.class, proc);
    }

    public void actionPerformed(ActionEvent e) {
        logger.info("action performed: " + e);

        if (e.getActionCommand().equals("close")) {
            hudComponent.setVisible(false);
        } else if (e.getActionCommand().equals("minimize")) {
            //minimizeComponent(comp);
        }
    }

    public void mouseMoved(MouseEvent e) {
        //logger.info("mouse moved to: " + e.getPoint());
    }

    public void mouseEntered(MouseEvent e) {
        logger.finest("mouse entered");
    }

    public void mouseExited(MouseEvent e) {
        logger.finest("mouse exited");
    }

    public void mousePressed(MouseEvent e) {
        logger.finest("mouse pressed at: " + e.getPoint());
    }

    public void mouseReleased(MouseEvent e) {
        logger.finest("mouse released at: " + e.getPoint());
        dragging = false;
    }

    public void mouseClicked(MouseEvent e) {
        logger.finest("mouse click at: " + e.getPoint());
    }

    public void mouseDragged(MouseEvent e) {
        logger.finest("mouse dragged to: " + e.getPoint());
        if (!dragging) {
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
            dragging = true;
        }

        // calculate new location of HUD component
        Point location = hudComponent.getLocation();
        int xDelta = e.getX() - dragOffsetX;
        int yDelta = e.getY() - dragOffsetY;
        location.setLocation(location.getX() + xDelta, location.getY() - yDelta);

        // move the HUD component
        hudComponent.setLocation(location);
    }
}
