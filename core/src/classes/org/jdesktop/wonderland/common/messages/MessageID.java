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
package org.jdesktop.wonderland.common.messages;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * A unique identifier for a message.  Unique MessageIDs can be generated using
 * the MessageIDGenerator included as part of this class.  Custom 
 * MessageIDGenerators can be used as well as the default generator.
 * @author jkaplan
 */
@ExperimentalAPI
public class MessageID implements Externalizable {
    /** the default message id generator */
    private static MessageIDGenerator idGen = new DefaultMessageIDGenerator();
    
    /** the encoding to use for the id */
    private static final String ENCODING = "US-ASCII";
            
    /** the unique identifying string for this message */
    private String id;
    
    /**
     * No-arg constructor required for externalizable
     */
    public MessageID() {
        this (null);
    }
    
    /** 
     * Create a messageID from the given String.  To generate a brand new,
     * unique messageID, use the generateMessageID() method instead.
     */
    public MessageID(String id) {
        this.id = id;
    }

    /**
     * Generate a new, unique messageID
     * @return a new, unique messageID generated using the default
     * MessageIDGenerator
     */
    public static MessageID generateMessageID() {
        return idGen.generateID();
    }
    
    /**
     * Set the default message ID generator
     * @param idGen the new default message ID generator
     */
    public static void setMessageIDGenerator(MessageIDGenerator idGen) {
        MessageID.idGen = idGen;
    }
    
    /**
     * Write a message ID to a stream.  A message ID is written in
     * the following form: 
     * <length:int><id:bytes> where length is the length of the id converted
     * to the US-ASCII encoding.
     * @param out the output to write to
     * @throws IOException if there is an error writing
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] idBytes = id.getBytes(ENCODING);
        out.writeInt(idBytes.length);
        out.write(idBytes);
        out.flush();
    }

    /**
     * Read an id from  stream
     * @param in the input to read from
     * @throws IOException if there is an error reading
     */
    public void readExternal(ObjectInput in) throws IOException {
        int idLen = in.readInt();
        byte[] idBytes = new byte[idLen];
        in.read(idBytes);
        
        id = new String(idBytes, ENCODING);
    }
    
    /**
     * Compare two message ids based on the underlying ID string
     * @param o the other object to compare
     * @return true if the object is a MessageID which has the same underlying
     * ID as this object.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageID)) {
            return false;
        }
        
        MessageID m = (MessageID) o;
        
        if (id == null) {
            return (m.id == null);
        } else {
            return id.equals(m.id);
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
 
    @Override
    public String toString() {
        return "{MessageID: " + id + "}";
    }
    
    /**
     * A utility class for generating message ids. Use the 
     * setMessageIDGenerator() to change the default generator.
     */
    @ExperimentalAPI
    public interface MessageIDGenerator {
        /**
         * Generate the next unique message ID
         */
        public MessageID generateID();
    }
    
    /**
     * The default MessageIDGenerator uses a static long to generate message
     * ids.
     */
    @ExperimentalAPI
    public static class DefaultMessageIDGenerator 
            implements MessageIDGenerator 
    {
        /** the current id */
        private static long id;
        
        /**
         * {@inheritDoc}
         */
        public synchronized MessageID generateID() {
            return new MessageID(String.valueOf(++id));
        }
    }
}
