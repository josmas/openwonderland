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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.client.comms.ClientConnection.Status;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 *
 * A Component that provides a cell specific communication channel with 
 * the server.
 * 
 * @author paulby
 */
@ExperimentalAPI
public abstract class ChannelComponent extends CellComponent {
    
    public ChannelComponent(Cell cell) {
        super(cell);
    }

    /**
     * Register a receiver for a specific message class. Only a single receiver
     * is allowed for each message class, calling this method to add a duplicate
     * receiver will cause an IllegalStateException to be thrown.
     * 
     * @param msgClass
     * @param receiver
     */
    public abstract void addMessageReceiver(Class<? extends CellMessage> msgClass, ComponentMessageReceiver receiver);
    
    /**
     * Remove the message receiver listening on the specifed message class
     * @param msgClass
     */
    public abstract void removeMessageReceiver(Class<? extends CellMessage> msgClass);
    
    /**
     * Dispatch messages to any receivers registered for the particular message class
     * @param sender
     * @param session
     * @param message
     */
    abstract void messageReceived(CellMessage message );
    
    public abstract Status getStatus();
    
    public abstract void send(CellMessage message, ResponseListener listener);
    
    public abstract void send(CellMessage message);
    
    /**
     * Return the class used to reference this component. Usually this will return
     * the class of the component, but in some cases, such as the ChannelComponent
     * subclasses of ChannelComponent will return their parents class
     * @return
     */
    @Override
    protected Class getLookupClass() {
        return ChannelComponent.class;
    }

    // TODO various send methods required, cell to server, cell to cell, cell to channel
    // Not sure these need to be defined in this interface, implementors should have
    // the choice of which send messages to implement and expose (if any) in a cell.
//    public void send(CellMessage message);
    
    static public interface ComponentMessageReceiver {
        public void messageReceived(CellMessage message );        
    }

    class ClassWrapper {
        private Class clazz;

        public ClassWrapper(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 59 * hash + (this.clazz != null ? this.clazz.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Class))
                return false;

            return ((Class)o).isAssignableFrom(clazz);
        }
    }
}
