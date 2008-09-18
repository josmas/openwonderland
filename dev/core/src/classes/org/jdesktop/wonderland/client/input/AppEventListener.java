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

/**
 * A convenience class for event listeners which operate only when the
 * global event mode is APP. <code>consumeEvent</code> rejects events when the
 * global event mode is not APP. 
 *
 * @author deronj
 */

@ExperimentalAPI
public class AppEventListener extends EventListenerBaseImpl  {

    /**
     * {@inheritDoc}
     */
    public boolean consumeEvent (Event event) {
	return InputManager.isAppMode();
    }
}
