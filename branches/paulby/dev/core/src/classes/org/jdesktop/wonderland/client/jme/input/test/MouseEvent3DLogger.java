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
package org.jdesktop.wonderland.client.jme.input.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.WorldEventClassListener;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A test listener for mouse events. Add this to an entity and it will log all mouse events that
 * occur over the entity when the event mode is WORLD.
 *
 * @author deronj
 */

@ExperimentalAPI
public class MouseEvent3DLogger extends WorldEventClassListener {

    private static final Logger logger = Logger.getLogger(MouseEvent3DLogger.class.getName());

    static {
	logger.setLevel(Level.INFO);
    }

    /**
     * Consume all mouse events.
     */
    public Class[] eventClassesToConsume () {
	return new Class[] { MouseEvent3D.class };
    }

    public void commitEvent (Event event, Entity entity) {
	logger.info("Received mouse event, entity = " + entity + ", event = " + event);
    }
}

