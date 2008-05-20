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
package org.jdesktop.wonderland.client.jme;

import com.jme.renderer.Camera;
import com.jme.scene.Node;
import com.jme.system.DisplaySystem;
import com.jme.util.Timer;

/**
 *
 * @author paulby
 */
public class RenderInfo {

    private Node root;
    private Camera camera;
    private DisplaySystem display;
    private Timer timer = Timer.getTimer();

    public Node getRoot() {
        return root;
    }

    void setRoot(Node root) {
        this.root = root;
    }

    public Camera getCamera() {
        return camera;
    }

    void setCamera(Camera camera) {
        this.camera = camera;
    }

    public DisplaySystem getDisplay() {
        return display;
    }

    void setDisplay(DisplaySystem display) {
        this.display = display;
    }
    
    public Timer getTimer() {
        return timer;
    }
    
    void updateTimer() {
        timer.update();
    }
}
