/**
 * Project Wonderland
 *
 * $RCSfile: AssetDB.java,v $
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
 * $Revision: 1.15 $
 * $Date: 2007/08/07 17:01:12 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.common.comms;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A specialized ObjectOutputStream that assumes that exactly the same
 * classes are available on either end of the stream. With this assumption this
 * stream can significantly reduce the size of serialized objects by only
 * sending the large header once for a given class. For subsequent transmission
 * of an object of a given class an id is sent.
 * 
 * @author paulby
 */
public class WonderlandObjectOutputStream extends ObjectOutputStream {

    protected static final int NEW_DESCRIPTOR = Integer.MIN_VALUE;
    protected static final int REMOVE_DESCRIPTOR = NEW_DESCRIPTOR+1;
    protected static int firstID = REMOVE_DESCRIPTOR+1;
    
    private ReferenceQueue refQueue;
    
    private HashMap<Integer, WeakReference<ObjectStreamClass>> idToDesc = new HashMap();
    private HashMap<WeakReference<ObjectStreamClass>, Integer> descToId = new HashMap();
    
    private int nextID = firstID;

    public WonderlandObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        refQueue = new ReferenceQueue();
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        // First send any pending remove messages
        WeakReference ref = (WeakReference) refQueue.poll();
        while (ref != null) {
            int id = descToId.remove(ref);
            idToDesc.remove(id);
                
            writeInt(REMOVE_DESCRIPTOR);
            writeInt(id);
            
            System.err.println("****** GC'ed ref "+id);
            
            ref = (WeakReference) refQueue.poll();
        }
        
        // Now send the users class descriptor
        Integer idObj = descToId.get(desc);
        if (idObj == null) {
            idObj = new Integer(nextID++);
            WeakReference descRef = new WeakReference(desc, refQueue); 
            idToDesc.put(idObj, descRef);
            descToId.put(descRef, idObj);
            writeInt(NEW_DESCRIPTOR);
            super.writeClassDescriptor(desc);
        }
        writeInt(idObj);
    }
}
