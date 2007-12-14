/*
 * Project Looking Glass
 * 
 * $RCSfile: ProtocolVersion.java,v $
 * 
 * Copyright (c) 2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.7 $
 * $Date: 2007/11/29 23:14:50 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.common.messages;

import java.nio.ByteBuffer;

/**
 * 
 * increment MINOR_VERSION whenever any new data is added to objects in
 * darkstar.common or darkstar.common.message objects
 *
 * @author paulby
 */
public class ProtocolVersion extends Message {

    private int MAJOR_VERSION = 1;
    private int MINOR_VERSION = 7;
    private int SUB_VERSION = 0;
    
    private static ProtocolVersion localVersion = new ProtocolVersion();
    
    @Override
    public String toString() {
        return MAJOR_VERSION+"."+MINOR_VERSION+"_"+SUB_VERSION;
    }
    
    public static ProtocolVersion getLocalVersion() {
        return localVersion;
    }
    /**
     *  Return true if supplied server version is compatible with this ProtocolVersion
     **/
    public boolean compatibleWithServerVersion(ProtocolVersion serverVersion) {
        if (serverVersion.MAJOR_VERSION==MAJOR_VERSION &&
                serverVersion.MINOR_VERSION==MINOR_VERSION &&
                serverVersion.SUB_VERSION<=SUB_VERSION)
            return true;
        
        return false;
    }

    protected void extractMessageImpl(ByteBuffer data) {
        MAJOR_VERSION = DataInt.value(data);
        MINOR_VERSION = DataInt.value(data);
        SUB_VERSION = DataInt.value(data);
    }

    protected void populateDataElements() {
        dataElements.add(new DataInt(MAJOR_VERSION));
        dataElements.add(new DataInt(MINOR_VERSION));
        dataElements.add(new DataInt(SUB_VERSION));
    }
}
