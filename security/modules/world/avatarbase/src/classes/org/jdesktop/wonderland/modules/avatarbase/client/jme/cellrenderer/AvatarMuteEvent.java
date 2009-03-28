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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import org.jdesktop.wonderland.client.input.Event;

/**
 * Event indicating mute or unmute
 *
 * @author jprovino
 */
public class AvatarMuteEvent extends Event {

    private String username;
    private boolean isMuted;

    public AvatarMuteEvent(String username, boolean isMuted) {
	this.username = username;
        this.isMuted = isMuted;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setIsMuted(boolean isMuted) {
	this.isMuted = isMuted;
    }

    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public Event clone(Event evt) {
        if (evt == null) {
            evt = new AvatarMuteEvent(username, isMuted);
        } else {
            AvatarMuteEvent e = (AvatarMuteEvent) evt;

	    e.setUsername(username);
            e.setIsMuted(isMuted);
        }

        super.clone(evt);
        return evt;
    }

    public String toString() {
	return "AvatarMuteEvent:  " + username + " isMuted " + isMuted;
    }

}
