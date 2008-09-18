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
package org.jdesktop.wonderland.client.jme.input;

/**
 * A singleton container for all of the processor objects in the Wonderland 3D event input subsystem.
 *
 * @author deronj
 */

@ExperimentalAPI
public class InputManager3D extends InputManager {

    /**
     * {@inheritDoc}
     */
    protected void initializeEntityResolver () {
	entityResolver = EntityResolver3D.getEntityResolver();
    }

    /**
     * {@inheritDoc}
     */
    protected void initializeEventDistributor () {
	eventDistributor = EventDistributor3D.getEventDistributor();
    }
}
