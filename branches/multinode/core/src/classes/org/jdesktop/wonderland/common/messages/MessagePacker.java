/**
 * Project Wonderland
 *
 * $Id$
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
 */
package org.jdesktop.wonderland.common.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.comms.SessionInternalClientType;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;
import org.jdesktop.wonderland.common.comms.WonderlandObjectOutputStream;

/**
 *
 * Utility class to pack and unpack messages to/from ByteBuffers.
 * 
 * 
 * @author paulby
 */
public class MessagePacker {

    /**
     * Pack the given message into a ByteBuffer ready
     * for transmission over the network
     * @param message
     * @return
     */
    public static ByteBuffer pack(Message message, short clientID) throws PackerException {
        return serializationPack(message, clientID);
    }
    
    private static ByteBuffer serializationPack(Message message, short clientID) throws PackerException {
        WonderlandObjectOutputStream out = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            out = new WonderlandObjectOutputStream(baos);
            // first write the message ID
            out.writeObject(message.getMessageID());

            // now write the client ID
            out.writeShort(clientID);

            // finally, write the serialized message
            out.writeObject(message);
            out.close();

            byte[] buf = baos.toByteArray();
            return ByteBuffer.wrap(buf);
        } catch (IOException ex) {
            Logger.getLogger(MessagePacker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(MessagePacker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        throw new PackerException();
    }
    
    /**
     * Give a ByteBuffer unpack it's data and return
     * the message it represents
     * @param buf
     * @return
     */
    public static ReceivedMessage unpack(ByteBuffer buf) throws PackerException {
        return serializationUnpack(buf);
    }
        
    private static ReceivedMessage serializationUnpack(ByteBuffer buf) {            
        
        WonderlandObjectInputStream in = null;
        try {
            in = new WonderlandObjectInputStream(getInputStream(buf));
            
            // first read the message ID
            MessageID messageID = (MessageID) in.readObject();

            // next the client ID
            short clientID = in.readShort();

            // finally the message
            Message message = (Message)in.readObject();

            // now put it all together
            message.setMessageID(messageID);
            return new ReceivedMessage(message, clientID);
        } catch (IOException ex) {
            Logger.getLogger(MessagePacker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessagePacker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(MessagePacker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    /**
     * Convert a ByteBuffer into an input stream
     * @param data the byte array
     * @return an input stream for reading from the ByteBuffer
     */
    protected static InputStream getInputStream(ByteBuffer data) {
         // get a byte buffer from the data
         ByteArrayInputStream bais;
         if (data.hasArray()) {
            // optimized method uses the backing array for this
            // ByteBuffer directly
            bais = new ByteArrayInputStream(data.array(),
                                            data.arrayOffset(),
                                            data.remaining());
         } else {
             // copy contents of ByteBuffer into a byte array
             byte[] arr = new byte[data.remaining()];
             data.get(arr);
             bais = new ByteArrayInputStream(arr);
         }
         
         return bais;
    }
    
    /**
     * A description of a received message.  This includes both the clientID 
     * the message was sent with and the contents of the message.
     */
    public static class ReceivedMessage {
        /** the message */
        private final Message message;
        
        /** the clientID sent with the message */
        private final short clientID;
    
        /**
         * Create a new ReceivedMessage 
         */
        private ReceivedMessage(Message message, short clientID) {
            this.message = message;
            this.clientID = clientID;
        }
        
        /**
         * Get the message
         */
        public Message getMessage() {
            return message;
        }
        
        /**
         * Get the client ID
         */
        public short getClientID() {
            return clientID;
        }
    }
    
    public static class PackerException extends Exception {
        
        /**
         * Returnt the message id for the message that failed, if known. Otherwise
         * return null
         * @return messageID, or null
         */
        public MessageID getMessageID() {
            return null;
        }
        
        public short getClientID() {
            return Short.MIN_VALUE;
        }
    }

}
