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

package org.jdesktop.wonderland.server.eventrecorder;

import java.util.HashSet;
import java.util.Set;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;



/**
 * Responsible for recording events in Wonderland.
 * Manages a singleton instance of itself.
 * @author Bernard Horan
 */
public class RecorderManager implements EventRecorder {
    private static RecorderManager DEFAULT_MANAGER;

    private Set<EventRecorder> recorders = new HashSet<EventRecorder>();

    /**
     * Return the singleton instance of the RecorderManager
     */
    public static RecorderManager getDefaultManager() {
        if (DEFAULT_MANAGER == null) {
            DEFAULT_MANAGER = new RecorderManager();
        }
        return DEFAULT_MANAGER;
    }

    /** Creates a new instance of RecorderManager */
    RecorderManager() {
    }

    /**
     * Record the message from the sender
     * @param sender the sender of the message
     * @param clientID the id of the client sending the message
     * @param message
     */
    public void recordMessage(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
        for (EventRecorder recorder : recorders) {
            recorder.recordMessage(sender, clientID, message);
        }
    }

    public boolean isRecording() {
        for (EventRecorder recorder : recorders) {
            if (recorder.isRecording()) {
                return true;
            }
        }
        return false;
    }



    /**
     *
     * @param recorder
     */
    public void register(EventRecorder recorder) {
        recorders.add(recorder);
    }

    /**
     *
     * @param recorder
     */
    public void unregister(EventRecorder recorder) {
        recorders.remove(recorder);
    }

}
