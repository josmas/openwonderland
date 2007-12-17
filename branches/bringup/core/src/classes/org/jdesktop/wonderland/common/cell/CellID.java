/**
 * Project Looking Glass
 *
 * $RCSfile: CellID.java,v $
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
 * $Revision: 1.4 $
 * $Date: 2007/07/09 22:09:02 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common.cell;

import java.io.Serializable;
import java.nio.ByteBuffer;
import org.jdesktop.lg3d.wonderland.darkstar.common.messages.DataElement;

/**
 * Basic CellID
 * 
 * 
 * @author paulby
 */
public class CellID extends DataElement implements Serializable {
    
    long id;
    
    /**
     * Creates a new instance of CellID
     */
    public CellID(long id) {
        this.id = id;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof CellID)
            if (((CellID) obj).id==id)
                return true;
        return false;
    }
    
    public int hashCode() {
        return (int)id;
    }
    
    public String toString() {
        return Long.toString(id);
    }
    
    /**
     *  Pack cellID into buffer for network
     */
    public void put(ByteBuffer buffer) {
        buffer.putLong(id);
    }
    
    public static CellID value(ByteBuffer buffer) {
        long id = buffer.getLong();
        return new CellID(id);
    }

    public static CellID value(String string) {
        return new CellID(Long.parseLong(string));
    }
    
    /**
     *  Size of packed object in bytes
     */
    public int getSize() {
        return Long.SIZE/8;
    }

}
