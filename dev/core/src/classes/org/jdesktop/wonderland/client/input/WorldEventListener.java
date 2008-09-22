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
package org.jdesktop.wonderland.client.input;

import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A convenience super class for event listeners which operate only when the
 * global event mode is <code>WORLD</code>. <code>consumeEvent</code> rejects events when the
 * global event mode is not <code>WORLD</code>. 
 * 
 * @author deronj
 */

@ExperimentalAPI
public class WorldEventListener extends EventListenerBaseImpl  {

    /**
     * {@inheritDoc}
     * <br><br>    
     * Note on subclassing: subclasses should call the following code as the first statement in the
     * overridden method:
     * <br><br>
     * <code>
     * if (!super.consumeEvent(event, entity)) { return false; }
     * </code>
     * <br><br>
     * This will make sure that the event mode is properly checked.
     */
    @Override
    public boolean consumeEvent (Event event, Entity entity) {
	return InputManager.inputManager().isWorldMode();
    }
}
