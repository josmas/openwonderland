/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import com.jme.app.mtgame.entity.*;
import com.jme.input.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.awt.AWTEvent;

/**
 * This is the default input manager.  It listens to mouse and 
 * keyboard events via jME.  It currently routes events to all
 * Entities listening.
 * 
 * @author Doug Twilleager
 */
public class DefaultInputManager extends InputManager implements KeyListener, 
        MouseListener, MouseMotionListener, MouseWheelListener {
    /**
     * The list of entities interested in key events
     */
    private ArrayList keyListeners = new ArrayList();
    
    /**
     * The list of entities interested in mouse events
     */
    private ArrayList mouseListeners = new ArrayList();

    /**
     * A state variable tracking whether or not we are listening for mouse events
     */
    private boolean mouseListening = false;
    
    /**
     * A state variable tracking whether or not we are listening for key events
     */
    private boolean keyListening = false;
    
    /**
     * The RenderManager
     */
    private RenderManager renderManager = null;
        
    /**
     * The EntityManager
     */
    private EntityManager entityManager = null;
    
    /**
     * The default constructor
     */
    public DefaultInputManager () {
    }
    
    /**
     * The initialize method
     */
    public void initialize(RenderManager rm, EntityManager em) {
        renderManager = rm;
        entityManager = em;
    }
    
    /**
     * This method adds an entity to the list of those tracking key events
     * @param e The interested entity
     */
    public void addAWTKeyListener(AWTEventListener listener) {
        synchronized (keyListeners) {
            keyListeners.add(listener);
            if (!keyListening) {
                renderManager.trackKeyInput(this);
                keyListening = true;
            }
        }
    }

    /**
     * This method removes an entity from the list of those tracking key events
     * @param e The uinterested entity
     */    
    public void removeAWTKeyListener(AWTEventListener listener) {
        synchronized (keyListeners) {
            keyListeners.remove(listener);
            if (keyListeners.size() == 0) {
                renderManager.untrackKeyInput(this);
                keyListening = false;
            }
        }
    }
    
    /**
     * This method adds an entity to the list of those tracking mouse events
     * @param e The interested entity
     */    
    public void addAWTMouseListener(AWTEventListener listener) {
        synchronized (mouseListeners) {
            mouseListeners.add(listener);
            if (!mouseListening) {
                renderManager.trackMouseInput(this);
                mouseListening = true;
            }
        }
    }

    /**
     * This method removes an entity from the list of those tracking mouse events
     * @param e The uinterested entity
     */        
    public void removeAWTMouseListener(AWTEventListener listener) {
        synchronized (mouseListeners) {
            mouseListeners.remove(listener);
            if (mouseListeners.size() == 0) {
                renderManager.untrackMouseInput(this);
                mouseListening = false;
            }
        }
    }

    /**
     * An internal method to make dispatching easier
     * @param e
     */
    private void dispatchKeyEvent(AWTEvent e) {
        AWTEventListener l = null;
        
        synchronized (keyListeners) {
            for (int i=0; i<keyListeners.size(); i++) {
                l = (AWTEventListener) keyListeners.get(i);
                l.eventDispatched(e);
            }
        }
        entityManager.triggerAWTEvent();        
    }
    
    
    /**
     * An internal method to make dispatching easier
     * @param e
     */
    private void dispatchMouseEvent(AWTEvent e) {
        AWTEventListener l = null;
        
        synchronized (mouseListeners) {
            for (int i=0; i<mouseListeners.size(); i++) {
                l = (AWTEventListener) mouseListeners.get(i);
                l.eventDispatched(e);
            }
        }
        entityManager.triggerAWTEvent();        
    }
    
    /**
     * The methods used by AWT to notify us of mouse events
     */
    public void keyPressed(KeyEvent e) {
        dispatchKeyEvent(e);
        //System.out.println("keyPressed: " + e);
    }

    public void keyReleased(KeyEvent e) {
        dispatchKeyEvent(e);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.out.println("USING THE ESCAPE HATCH!");
            System.exit(0);
        }
        //System.out.println("keyReleased: " + e);
    }

    
    public void keyTyped(KeyEvent e) {
        dispatchKeyEvent(e);
        //System.out.println("keyTyped: " + e);
    }

    public void mouseClicked(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseClicked: " + e);
    }

    public void mouseEntered(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseEntered: " + e);
    }

    public void mouseExited(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseExited: " + e);
    }

    public void mousePressed(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mousePressed: " + e);
    }

    public void mouseReleased(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseReleased: " + e);
    }

    public void mouseDragged(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseDragged: " + e);
    }

    public void mouseMoved(MouseEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseMoved: " + e);
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        dispatchMouseEvent(e);
        //System.out.println("mouseWheelMoved: " + e);
    }
}
