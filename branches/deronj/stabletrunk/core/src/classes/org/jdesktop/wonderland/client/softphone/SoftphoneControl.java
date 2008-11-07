/**
 * Project Looking Glass
 *
 * $RCSfile: SipStarter.java,v $
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
 * $Revision: 1.28 $
 * $Date: 2007/12/17 19:45:54 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.softphone;

import java.io.IOException;

public interface SoftphoneControl {
    public static final String SOFTPHONE_PROP =
            "org.jdesktop.wonderland.client.softphone.jar";
 
    /*
     * Start the softphone and wait for it to determine its address.
     */
    public String startSoftphone(String userName, String registrarAddress,
	    int registrarTimeout, String localHost, AudioQuality quality)
	    throws IOException;
    
    public void stopSoftphone();

    public void setCallID(String callID);

    public String getCallID();

    public void register(String registrarAddress) throws IOException;

    public boolean isRunning();

    public boolean isConnected() throws IOException;

    public void setVisible(boolean isVisible) throws IOException;

    public boolean isVisible();

    public void mute(boolean isMuted) throws IOException;

    public boolean isMuted();
    
    public void setAudioQuality(AudioQuality quality) throws IOException;

    public void sendCommandToSoftphone(String cmd) throws IOException;
    
    public void runLineTest() throws IOException;
    
    public void addListener(SoftphoneListener listener);

    public void removeListener(SoftphoneListener listener);

}
