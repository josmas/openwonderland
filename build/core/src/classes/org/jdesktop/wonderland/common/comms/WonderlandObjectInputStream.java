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
package org.jdesktop.wonderland.common.comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;

/**
 * A specialized ObjectInputStream that reduces the size of serialized core
 * wonderland objects. For known classes this stream reads an (int) id instead
 * of the large serialization class header. For unknown classes it reads
 * the class name in the stream, which again is usually much smaller than 
 * the serialization header. 
 * 
 * @author paulby
 */public class WonderlandObjectInputStream extends ObjectInputStream {

    private static HashMap<Integer, String> idToDesc = new HashMap();

    static {
        WonderlandObjectOutputStream.populateIdToDesc(idToDesc);        
    }
    
    public WonderlandObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected ObjectStreamClass readClassDescriptor() throws ClassNotFoundException, IOException {
        ObjectStreamClass ret;
        int id = readInt();

        if (id == WonderlandObjectOutputStream.UNKNOWN_DESCRIPTOR) {
            String className = readUTF();
//            System.err.println("WonderlandInputStream reading NEW_DESC "+className);
            ret = ObjectStreamClass.lookup(Class.forName(className));
        } else {
            ret = ObjectStreamClass.lookup(Class.forName(idToDesc.get(id)));
        }
        if (ret == null) {
            throw new IOException("Unknown class ID " + id);
        }
        
        return ret;
    }
}
