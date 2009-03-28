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
 * Event indicating start / stop speaking
 *
 * @author jprovino
 */
public class AvatarSpeakingEvent extends Event {

    private String username;
    private boolean isSpeaking;

    public AvatarSpeakingEvent(String username, boolean isSpeaking) {
	this.username = username;
        this.isSpeaking = isSpeaking;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getUsername() {
	return username;
    }

    public void setIsSpeaking(boolean isSpeaking) {
	this.isSpeaking = isSpeaking;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    @Override
    public Event clone(Event evt) {
        if (evt == null) {
            evt = new AvatarSpeakingEvent(username, isSpeaking);
        } else {
            AvatarSpeakingEvent e = (AvatarSpeakingEvent) evt;

	    e.setUsername(username);
            e.setIsSpeaking(isSpeaking);
        }

        super.clone(evt);
        return evt;
    }

    public String toString() {
	return "AvatarSpeakingEvent:  " + username + " isSpeaking " + isSpeaking;
    }

}
