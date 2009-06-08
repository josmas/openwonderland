/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.security.client;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.common.security.Action;
import org.jdesktop.wonderland.modules.security.common.ActionDTO;
import org.jdesktop.wonderland.modules.security.common.messages.PermissionsChangedMessage;
import org.jdesktop.wonderland.modules.security.common.messages.PermissionsRequestMessage;
import org.jdesktop.wonderland.modules.security.common.messages.PermissionsResponseMessage;

/**
 * Client-side representation of the security component contains the
 * permissions for this particular client.
 * @author jkaplan
 */
public class SecurityComponent extends CellComponent {
    private static final Logger logger =
            Logger.getLogger(SecurityComponent.class.getName());

    /**
     * The set of permissions this user has for this cell.  If this value
     * is null, it means the permissions are not yet calculated.
     */
    private Set<Action> granted;

    /** 
     * The channel to listen for messages over
     */
    @UsesCellComponent
    private ChannelComponent channel;

    /**
     * The message receiver to handle messages, or null if listeners
     * are not registered
     */
    private SecurityMessageReceiver receiver = null;

    public SecurityComponent(Cell cell) {
        super (cell);
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {
            case ACTIVE:
                if (increasing) {
                    if (receiver == null) {
                        receiver = new SecurityMessageReceiver();
                        channel.addMessageReceiver(PermissionsChangedMessage.class,
                                                   receiver);
                    }
                } else {
                    channel.removeMessageReceiver(PermissionsChangedMessage.class);
                    receiver = null;
                }
                break;
            case DISK:
                break;
        }
    }

    /**
     * Determine if the permissions have been set, or if they need to be
     * requested
     */
    public synchronized boolean hasPermissions() {
        return (granted != null);
    }

    /**
     * Get this user's permissions
     * @return the set of permissions for this user, or null if the
     * permissions are not calculated
     */
    public synchronized Set<Action> getPermissions()
        throws InterruptedException
    {
        if (granted == null) {
            // request the permissions from the server
            ResponseMessage rm = channel.sendAndWait(new PermissionsRequestMessage());
            if (rm instanceof PermissionsResponseMessage) {
                granted = new LinkedHashSet<Action>();
                for (ActionDTO a : ((PermissionsResponseMessage) rm).getGranted()) {
                    granted.add(a.getAction());
                }
            }
        }
        
        return granted;
    }


    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
    }

    class SecurityMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            if (message instanceof PermissionsChangedMessage) {
                // reset our view of granted permissions.  The next time someone
                // requests them, they will be re-fetched from the server.
                synchronized (this) {
                    granted = null;
                }
            }
        }
    }
}
