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

import java.io.IOException;
import java.io.OutputStream;
import org.jdesktop.wonderland.common.comms.WonderlandObjectOutputStream;

/**
 * An output stream specifically for writing serialized Messages.  This
 * class extends WonderlandObjectOutputStream to provide special handling
 * for Messages. 
 * 
 * @author jkaplan
 */
public class MessageOutputStream extends WonderlandObjectOutputStream {
    /**
     * Create a new message input stream.
     * @param out the output stream to write to
     * @throws IOException if there is an error creating the stream
     */
    public MessageOutputStream(OutputStream out) 
            throws IOException 
    {
        super (out);
    }
    
    /**
     * Write a message to the underlying stream.
     * <p>
     * A message is written in three steps.  First, the messageID is written to
     * the stream, followed by the client id to send the message to, followed
     * by the rest of the message contents.  This mechanism is to give the
     * maximum chance that the message id can be read, even in the face of other
     * errors.
     * 
     * @param message the message to write
     * @param clientID the clientID to send with the message.  This clientID
     * determines which client or client handler on the receiver processes 
     * the message.
     */
    public void writeMessage(Message message, short clientID)
        throws IOException
    {
        // first write the message ID
        writeObject(message.getMessageID());
    
        // now write the client ID
        writeShort(clientID);
        
        // finally, write the serialized message
        writeObject(message);
    }
}
