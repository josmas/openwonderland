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
package org.jdesktop.wonderland.modules.sas.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Holds the launch requests which have not yet been satisified.
 *
 * @author deronj
 */
class PendingLaunches implements Serializable {

    private HashMap<String,LinkedList<SasServer.LaunchRequest>> execCapToLaunchReqList =
        new HashMap<String,LinkedList<SasServer.LaunchRequest>>();

    void add (SasServer.LaunchRequest req) {
        LinkedList<SasServer.LaunchRequest> reqs = execCapToLaunchReqList.get(req.executionCapability);
        if (reqs == null) {
            reqs = new LinkedList<SasServer.LaunchRequest>();
            execCapToLaunchReqList.put(req.executionCapability, reqs);
        }
        reqs.add(req);
    }

    /**
     * Return the list of pending launches for this execution capability.
     */
    LinkedList<SasServer.LaunchRequest> getPendingLaunches (String executionCapability) {
        return execCapToLaunchReqList.get(executionCapability);
    }
}
