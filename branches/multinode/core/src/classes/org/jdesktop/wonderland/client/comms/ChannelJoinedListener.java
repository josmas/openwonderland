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
 * When a new channel is joined, ChannelJoinedListeners are used to generate
 * the listener that will receieve messages for that channel.
 * <p>
 * ChannelJoinedListeners are tried in the order they are registed.  By 
 * convention, channels in Wonderland are identified by a prefix.  Typically, 
 * a factory will look at the prefix of the channel name to determine if it can
 * return a listener for the given channel.
 * 
 * @author kaplanj
 */
@ExperimentalAPI
public interface ChannelJoinedListener {
    /**
     * Generate a listener for the given channel initiated on the given session.  
     * If this method returns a non-null listener, it will be used to listen 
     * for messages on the given channel.  The first non-null value returned
     * by a ChannelJoinedListener will be used as the listener for the
     * given channel.
     *  
     * @param session the session that received the channel join request
     * @param channel the channel to join
     * @return the listener to use for the given channel, or null if this 
     * factory doesn't handle listeners for the given channel.
     */
    public ClientChannelListener joinedChannel(WonderlandSession session,
                                               ClientChannel channel);
}
