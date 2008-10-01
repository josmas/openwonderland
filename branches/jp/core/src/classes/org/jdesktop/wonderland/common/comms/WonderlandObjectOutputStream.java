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

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.util.HashMap;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.messages.MovableMessage;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.common.messages.MessageID;

/**
 * A specialized ObjectInputStream that reduces the size of serialized core
 * wonderland objects. For known classes this stream stores a (int) id instead
 * of the large serialization class header. For unknown classes it stores
 * the class name in the stream, which again is usually much smaller than 
 * the serialization header. 
 * 
 * @author paulby
 */
public class WonderlandObjectOutputStream extends ObjectOutputStream {

    protected static final int UNKNOWN_DESCRIPTOR = Integer.MIN_VALUE;
    protected static int firstID = UNKNOWN_DESCRIPTOR+1;
    
    private static HashMap<String, Integer> descToId = new HashMap();
        
    private static Class[] coreClass = new Class[] {
        MovableMessage.class,
        MovableMessage.ActionType.class,
        CellMessage.class,
        MessageID.class,
        Enum.class,
        Vector3f.class,
        Quaternion.class,
        Message.class,
        CellID.class,
        
    };
  
    static {
        populateDescToId(descToId);
    }
    
    public WonderlandObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeClassDescriptor(ObjectStreamClass desc) throws IOException {
        
        // Now send the users class descriptor
        Integer idObj = descToId.get(desc.getName());
        if (idObj == null) {
//            System.err.println("First classDescriptor for "+desc.getName()+"  "+descToId.size());
            writeInt(UNKNOWN_DESCRIPTOR);
            writeUTF(desc.forClass().getName());
        } else {
            writeInt(idObj);
        }
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
