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
package org.jdesktop.wonderland.modules.appbase.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A user input control arbiter whose <code>hasControl</code> method always returns true.
 * An example usage is for HUD components.
 *
 * @author deronj
 */

@ExperimentalAPI
public class ControlArbAlways extends ControlArb {

    /**
     * {@inheritDoc}
     * <br>
     * This method always returns true.
     */
    @Override
    public boolean hasControl () {
	return true;
    }

    /**
     * {@inheritDoc}
     */
    public void deliverEvent (Window2D window, KeyEvent event) {
	window.deliverEvent(event);
    }

    /**
     * {@inheritDoc}
     */
    public void deliverEvent (Window2D window, MouseEvent event) {
	window.deliverEvent(event);
    }
}
