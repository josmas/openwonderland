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
import org.jdesktop.wonderland.common.InternalAPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * A convenience super class for event class listeners which operate only when the
 * global event mode is <code>APP</code>. <code>consumeEvent</code> rejects events when the
 * global event mode is not <code>APP</code>. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppEventClassListener extends EventClassListener  {

    /**
     * INTERNAL ONLY.
     */
    @InternalAPI
    @Override
    public boolean consumeEvent (Event event, Entity entity) {
	if (InputManager.inputManager().isAppMode()) {
	    return super.consumeEvent(event, entity);
	} else {
	    return false;
	}
    }
}
