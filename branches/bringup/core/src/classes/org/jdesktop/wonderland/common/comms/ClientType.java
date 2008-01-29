/**
 * Project Wonderland
 *
 * $RCSfile:$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision:$
 * $Date:$
 * $State:$
 */
package org.jdesktop.wonderland.common.comms;

import java.io.Serializable;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * The type of a Wonderland client.
 * @author jkaplan
 */
@ExperimentalAPI
public class ClientType implements Serializable {
    private String type;
    
    /**
     * Create a new client type with the given type name
     * @param type the name of the type
     */
    public ClientType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        
        // all client types are comparable to each other
        if (!(obj instanceof ClientType)) {
            return false;
        }
        
        final ClientType other = (ClientType) obj;
        if (this.type != other.type && 
                (this.type == null || !this.type.equals(other.type)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return type;
    }
}
