/**
 * Project Wonderland
 *
 * $RCSfile: LogControl.java,v $
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
 * $Revision: 1.3 $
 * $Date: 2007/10/23 18:27:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.comms;

import com.sun.sgs.client.ClientChannel;
import com.sun.sgs.client.ClientChannelListener;
import org.jdesktop.wonderland.ExperimentalAPI;

/**
 * When a new channel is joined, the name of the channel is used to determine
 * the listener factory that will be used to generate the ClientChannelListener
 * to receieve messages for that channel.
 * <p>
 * Factories are tried in the order they are registed.  By convention, channels 
 * in Wonderland are identified by a prefix.  Typically, a factory will look
 * at the prefix of the channel name to determine if it can return a listener
 * for the given channel.
 * 
 * @author kaplanj
 */
@ExperimentalAPI
public interface ChannelListenerFactory {
    /**
     * Generate a listener for the given client and channel.  If this method
     * returns a non-null listener, it will be used to listen for messages
     * on the given channel. 
     * @param client the client that was requested to join the given channel
     * @param channel the channel to join
     * @return the listener to use for the given channel, or null if this 
     * factory doesn't handle listeners for the given channel.
     */
    public ClientChannelListener createListener(BaseClient client,
                                                ClientChannel channel);
}
