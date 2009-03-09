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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.sas.server;

import java.io.Serializable;
import java.lang.String;
import java.util.HashMap;
import java.util.LinkedList;
import org.jdesktop.wonderland.common.cell.CellID;

/**
 * Holds the launch requests which have not yet been satisified.
 *
 * @author deronj
 */
class PendingLaunches implements Serializable {

    static class LaunchRequest implements Serializable {
        CellID cellID;
        String executionCapability;
        String appName;
        String command;

        LaunchRequest (CellID cellID, String executionCapability, String appName, String command) {
            this.cellID = cellID;
            this.executionCapability = executionCapability;
            this.appName = appName;
            this.command = command;
        }
    }

    private HashMap<String,LinkedList<LaunchRequest>> execCapToLaunchReqList =
        new HashMap<String,LinkedList<LaunchRequest>>();

    void add (LaunchRequest req) {
        LinkedList<LaunchRequest> reqs = execCapToLaunchReqList.get(req.executionCapability);
        if (reqs == null) {
            reqs = new LinkedList<LaunchRequest>();
            execCapToLaunchReqList.put(req.executionCapability, reqs);
        }
        reqs.add(req);
    }

    /**
     * Return the list of pending launches for this execution capability.
     */
    LinkedList<LaunchRequest> getPendingLaunches (String executionCapability) {
        return execCapToLaunchReqList.get(executionCapability);
    }
}
