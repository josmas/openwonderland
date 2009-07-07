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

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.hud.HUDAnimation;
import org.jdesktop.wonderland.client.hud.HUDComponent;

/**
 * A MoveLinearHUDAnimation moves a HUDComponent at a constant speed over
 * time.
 *
 * @author nsimpson
 */
public class MoveLinearHUDAnimation implements HUDAnimation {

    private static final Logger logger = Logger.getLogger(MoveLinearHUDAnimation.class.getName());
    private boolean animating = false;
    private long duration = 1000; // milliseconds

    public MoveLinearHUDAnimation() {
    }

    /**
     * {@inheritDoc}
     */
    public void setAnimated(HUDComponent component) {
    }

    /**
     * {@inheritDoc}
     */
    public HUDComponent getAnimated() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void startAnimation() {
        animating = true;
    }

    /**
     * {@inheritDoc}
     */
    public void stopAnimation() {
        animating = false;
    }

    /**
     *{@inheritDoc}
     */
    public boolean isAnimating() {
        return animating;
    }

    /**
     * {@inheritDoc}
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * {@inheritDoc}
     */
    public long getDuration() {
        return duration;
    }
}
