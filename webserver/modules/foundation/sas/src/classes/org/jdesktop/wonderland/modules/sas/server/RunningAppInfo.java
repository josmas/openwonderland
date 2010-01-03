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
package org.jdesktop.wonderland.modules.sas.server;

import com.sun.sgs.app.ManagedObject;
import java.io.Serializable;
import java.util.HashMap;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import com.sun.sgs.app.AppContext;
import java.util.LinkedList;

/**
 * Information about provider apps that are running.
 */

@ExperimentalAPI
public class RunningAppInfo implements ManagedObject, Serializable {

    class AppInfo implements Serializable {
        public ProviderProxy provider;
        public CellID cellID;
        public AppInfo (ProviderProxy provider, CellID cellID) {
            this.provider = provider;
            this.cellID = cellID;
        }
    }

    private HashMap<MessageID,AppInfo> runningAppMap = new HashMap<MessageID,AppInfo>();

    void addAppInfo (MessageID msgID, ProviderProxy provider, CellID cellID) {
        AppInfo msgInfo = new AppInfo(provider, cellID);
        runningAppMap.put(msgID, msgInfo);
        AppContext.getDataManager().markForUpdate(this);
    }

    void removeAppInfo (MessageID msgID) {
        runningAppMap.remove(msgID);
        AppContext.getDataManager().markForUpdate(this);
    }

    AppInfo getAppInfo (MessageID msgID) {
        return runningAppMap.get(msgID);
    }

    /**
     * Removes all app infos that are for the given provider.
     */
    void removeAppInfosForProvider (ProviderProxy provider) {
        LinkedList<MessageID> removeList = new LinkedList<MessageID>();
        for (MessageID msgID : runningAppMap.keySet()) {
            AppInfo appInfo = runningAppMap.get(msgID);
            if (appInfo.provider == provider) {
                removeList.add(msgID);
            }
        }        
        for (MessageID msgIDToRemove : removeList) {
            runningAppMap.remove(msgIDToRemove);
        }
        removeList = null;
        AppContext.getDataManager().markForUpdate(this);
    }

    /**
     * Removes all app infos that are for the given provider.
     */
    void removeAppInfosForCellAndProvider (ProviderProxy provider, CellID cellID) {
        LinkedList<MessageID> removeList = new LinkedList<MessageID>();
        for (MessageID msgID : runningAppMap.keySet()) {
            AppInfo appInfo = runningAppMap.get(msgID);
            if (appInfo.provider == provider && appInfo.cellID == cellID) {
                removeList.add(msgID);
            }
        }        
        for (MessageID msgIDToRemove : removeList) {
            runningAppMap.remove(msgIDToRemove);
        }
        removeList = null;
        AppContext.getDataManager().markForUpdate(this);
    }

    // Given an an app identified by a provider and a cell, returns the launch message ID
    // for that app. TODO: someday: assumes only one app launched per cell.
    MessageID getLaunchMessageIDForCellAndProvider (ProviderProxy provider, CellID cellID) {
        for (MessageID msgID : runningAppMap.keySet()) {
            AppInfo appInfo = runningAppMap.get(msgID);
            if (appInfo.provider == provider && appInfo.cellID == cellID) {
                return msgID;
            }
        }        
        return null;
    }
}
