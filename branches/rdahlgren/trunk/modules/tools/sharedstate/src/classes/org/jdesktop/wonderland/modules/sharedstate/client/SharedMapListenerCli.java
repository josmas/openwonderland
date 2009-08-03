/**
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
package org.jdesktop.wonderland.modules.sharedstate.client;

import java.math.BigInteger;
import org.jdesktop.wonderland.modules.sharedstate.common.SharedData;

/**
 * Notification when a property changes
 * @author jkaplan
 */
public interface SharedMapListenerCli {
    /**
     * Notification that a property changed
     * @param map the map where the change occured
     * @param senderID the id of the initiator of the change, or null
     * if the change was initiated by the server
     * @param propertyName the name of the property that changed
     * @param oldvalue the old value of the property
     * @param newValue the new value of the property
     */
    public void propertyChanged(SharedMapCli map, BigInteger senderID,
                                String propertyName, SharedData oldvalue,
                                SharedData newValue);
}
