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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.EntityMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;

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
    
//    private HashMap<Integer, WeakReference<ObjectStreamClass>> idToDesc = new HashMap();
    private HashMap<String, Integer> descToId = new HashMap();
    
    
    private static Class[] coreClass = new Class[] {
        EntityMessage.class,
        EntityMessage.ActionType.class,
        CellMessage.class,
        MessageID.class,
        Enum.class,
        Vector3f.class,
        Quaternion.class,
        Message.class,
        CellID.class,
        
    };
    
    private int nextID = firstID+coreClass.length;

    public WonderlandObjectOutputStream(OutputStream out) throws IOException {
        super(out);
        refQueue = new ReferenceQueue();
        populateDescToId(descToId);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        // First send any pending remove messages
//        WeakReference ref = (WeakReference) refQueue.poll();
//        while (ref != null) {
//            int id = descToId.remove(ref);
////            idToDesc.remove(id);
//                
//            writeInt(REMOVE_DESCRIPTOR);
//            writeInt(id);
//            
//            ref = (WeakReference) refQueue.poll();
//        }
        
        // Now send the users class descriptor
        Integer idObj = descToId.get(desc.getName());
        if (idObj == null) {
//            System.err.println("First classDescriptor for "+desc.getName()+"  "+descToId.size());
            idObj = new Integer(nextID++);
//            WeakReference<String> descRef = new WeakReference(desc.getName(), refQueue); 
//            idToDesc.put(idObj, descRef);
            descToId.put(desc.getName(), idObj);
            writeInt(NEW_DESCRIPTOR);
//            super.writeClassDescriptor(desc);
            writeUTF(desc.forClass().getName());
        }
        writeInt(idObj);
    }
    
    static void populateDescToId(HashMap<String, Integer> map) {
        int id = firstID;
        for(Class clazz : coreClass) {
            map.put(clazz.getName(), id++);
        }
    }
    
    static void populateIdToDesc(HashMap<Integer, String> map) {
        int id = firstID;
        for(Class clazz : coreClass) {
            map.put(id++, clazz.getName());
        }
        
    }
}
