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

/**
 * Contains what the SAS server knows about a SAS provider.
 *
 * @author deronj
 */

class ProviderInfo implements Serializable {

    /** The client ID of the provider. */
    //    private WonderlandClientID clientID;

    /** The provider's sender. */
    //private WonderlandClientSender sender;

    /** The set of execution capabilities provided by this provider. */
    //HashSet<String,Boolean> executionCapabilities = new HashSet<String,Boolean>();

    /*
    ProviderInfo (WonderlandClientID clientID, WonderlandClientSender sender) {
        this.clientID = clientID;
        this.sender = sender;
    }

    void synchronized addExecutionCapability (String executionCapability) {
        executionCapabilities.put(executionCapability, new Boolean(true));
    }

    void synchronized removeExecutionCapability (String executionCapability) {
        executionCapabilities.remove(executionCapability);
    }
    */

    /**
     * Does this provider provide the given execution capability?
     * @param executionCapability The execution capability to check for.
     */
    /*
    boolean synchronized provides (String executionCapability) {
        return executionCapabilities.get(executionCapability) != null;
    }
    */
}
