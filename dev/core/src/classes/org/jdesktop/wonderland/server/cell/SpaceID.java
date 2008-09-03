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
package org.jdesktop.wonderland.server.cell;

import java.io.Serializable;

/**
 *
 * @author paulby
 */
public class SpaceID implements Serializable {

    private long id;

    SpaceID(long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SpaceID) {
            if (((SpaceID)o).id==id)
                return true;
            else
                return false;
        } return false;
    }
    
    @Override
    public String toString() {
        return Long.toString(id);
    }
}
