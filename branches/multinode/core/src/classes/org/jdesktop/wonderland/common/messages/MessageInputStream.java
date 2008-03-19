/**
 * Project Wonderland
 *
 * $RCSfile$
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
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.common.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.jdesktop.wonderland.common.comms.SessionInternalClientType;
import org.jdesktop.wonderland.common.comms.WonderlandObjectInputStream;

/**
 * An input stream specifically for reading serialized messages.  This
 * class extends WonderlandObjectInputStream to provide special handling
 * for Messages.  The speicifics of how messages are encoded are detailed
 * in the docs for MessageOutputStream.  Only messages written using a
 * MessageOutputStream can be read with this class.
 * 
 * @see MessageOutputStream
 * @author jkaplan
 */
public class MessageInputStream extends WonderlandObjectInputStream {
    /**
     * Create a new message input stream.
     * @param is the input stream to read from
     * @throws IOException if there is an error creating the stream
     */
    public MessageInputStream(InputStream is) throws IOException {
        super (is);
    }
    
    /**
     * Create a message input stream that reads from the given ByteBuffer
     * @param data the ByteBuffer to read data from
     * @throws IOException if there is an error creating the stream
     */
    public MessageInputStream(ByteBuffer data) throws IOException {
        this (getInputStream(data));
    }
    
    /**
     * Read a message from the underlying stream
     * @return a message read from the stream
     * @throws IOException if there is an error reading the message
     */
    public ReceivedMessage readMessage() throws IOException {
        MessageID messageID = null;
        short clientID = SessionInternalClientType.SESSION_INTERNAL_CLIENT_ID;
        
        try {
            // first read the message ID
            messageID = (MessageID) readObject();
            
            // next the client ID
            clientID = readShort();
            
            // finally the message
            Message message = (Message) readObject();
            
            // now put it all together
            message.setMessageID(messageID);
            return new ReceivedMessage(message, clientID);
            
        } catch (IOException ioe) {
            throw new ExtractMessageException(messageID, clientID, ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new ExtractMessageException(messageID, clientID, cnfe);
        }
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
    public class ReceivedMessage {
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
}
