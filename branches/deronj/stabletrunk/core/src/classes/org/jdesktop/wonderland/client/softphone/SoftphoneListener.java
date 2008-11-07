/**
 * Project Looking Glass
 *
 * $RCSfile: SoftphoneListener.java,v $
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
 * $Revision: 1.4 $
 * $Date: 2007/10/26 19:03:44 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.client.softphone;

/*
 * Listener for softphone changes
 */
public interface SoftphoneListener {

    public void softphoneVisible(boolean isVisible);
    public void softphoneMuted(boolean muted);
    public void softphoneConnected(boolean connected);
    public void softphoneExited();

    /*
     * This will allow some feedback to the user that the mic gain is too high.
     */
    public void microphoneGainTooHigh();
}
