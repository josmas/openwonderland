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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;
import javax.swing.JComponent;

/**
 * A 2D frame for a HUDComponent2D
 *
 * @author nsimpson
 */
public class HUDFrameHeader2D extends HUDComponent2D implements ActionListener, MouseMotionListener {

    private static final Logger logger = Logger.getLogger(HUDFrameHeader2D.class.getName());
    private List<ActionListener> actionListeners;
    private List<MouseListener> mouseListeners;
    private List<MouseMotionListener> mouseMotionListeners;

    public HUDFrameHeader2D(JComponent component) {
        super(component);
    }

    public void addActionListener(ActionListener listener) {
        if (actionListeners == null) {
            actionListeners = Collections.synchronizedList(new LinkedList());
        }
        actionListeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        if (actionListeners != null) {
            actionListeners.remove(listener);
        }
    }

    public void notifyActionListeners(ActionEvent e) {
        if (actionListeners != null) {
            ListIterator<ActionListener> iter = actionListeners.listIterator();
            while (iter.hasNext()) {
                ActionListener listener = iter.next();
                listener.actionPerformed(e);
            }
            iter = null;
        }
    }

    public void actionPerformed(ActionEvent e) {
        e.setSource(this);
        notifyActionListeners(e);
    }

    public void addMouseMotionListener(MouseMotionListener listener) {
        if (mouseMotionListeners == null) {
            mouseMotionListeners = Collections.synchronizedList(new LinkedList());
        }
        mouseMotionListeners.add(listener);
    }

    public void removeMouseMotionListener(MouseMotionListener listener) {
        if (mouseMotionListeners != null) {
            mouseMotionListeners.remove(listener);
        }
    }

    public void addMouseListener(MouseListener listener) {
        if (mouseListeners == null) {
            mouseListeners = Collections.synchronizedList(new LinkedList());
        }
        mouseListeners.add(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        if (mouseListeners != null) {
            mouseListeners.remove(listener);
        }
    }

    public void notifyMouseMotionListeners(MouseEvent e) {
        if (mouseMotionListeners != null) {
            e.setSource(this);
            ListIterator<MouseMotionListener> iter = mouseMotionListeners.listIterator();
            while (iter.hasNext()) {
                MouseMotionListener listener = iter.next();

                switch (e.getID()) {
                    case MouseEvent.MOUSE_MOVED:
                        listener.mouseMoved(e);
                        break;
                    case MouseEvent.MOUSE_DRAGGED:
                        listener.mouseDragged(e);
                        break;
                    default:
                        break;
                }
            }
            iter = null;
        }
    }

    public void notifyMouseListeners(MouseEvent e) {
        if (mouseListeners != null) {
            e.setSource(this);
            ListIterator<MouseListener> iter = mouseListeners.listIterator();
            while (iter.hasNext()) {
                MouseListener listener = iter.next();

                switch (e.getID()) {
                    case MouseEvent.MOUSE_ENTERED:
                        listener.mouseEntered(e);
                        break;
                    case MouseEvent.MOUSE_EXITED:
                        listener.mouseExited(e);
                        break;
                    case MouseEvent.MOUSE_PRESSED:
                        listener.mousePressed(e);
                        break;
                    case MouseEvent.MOUSE_RELEASED:
                        listener.mouseReleased(e);
                        break;
                    case MouseEvent.MOUSE_CLICKED:
                        listener.mouseReleased(e);
                        break;
                    default:
                        break;
                }
            }
            iter = null;
        }
    }

    public void mouseMoved(MouseEvent e) {
        notifyMouseMotionListeners(e);
    }

    public void mouseDragged(MouseEvent e) {
        notifyMouseMotionListeners(e);
    }

    public void mouseEntered(MouseEvent e) {
        notifyMouseListeners(e);
    }

    public void mouseExited(MouseEvent e) {
        notifyMouseListeners(e);
    }

    public void mousePressed(MouseEvent e) {
        notifyMouseListeners(e);
    }

    public void mouseReleased(MouseEvent e) {
        notifyMouseListeners(e);
    }

    public void mouseClicked(MouseEvent e) {
        notifyMouseListeners(e);
    }
}
