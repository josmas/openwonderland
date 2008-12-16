/**
 * Project Looking Glass
 * 
 * $RCSfile: FakeVoiceHandler.java,v $
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
 * $Revision: 1.8 $
 * $Date: 2008/06/12 18:48:17 $
 * $State: Exp $ 
 */

package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.voip.client.connector.CallStatusListener;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.NameNotBoundException;
import com.sun.voip.client.connector.CallStatus;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class is meant to mimic the VoiceManager interfaces, yet provide
 * no actual output. It is used for the Phone Cells "Demonstration Mode".
 * @author jh215363
 */
public class FakeVoiceManager /*implements VoiceManager*/ {

    private static final Logger logger =
        Logger.getLogger(FakeVoiceManager.class.getName());
    
    private static FakeVoiceManager instance;
    
    public static final String NAME = FakeVoiceManager.class.getName();

    public static final String DS_PREFIX = NAME + ".";
    private static final String DS_FAKE_CALL_STATUS_LISTENERS =
        DS_PREFIX + "FakeCallStatusListeners";
    
    public static FakeVoiceManager getInstance() {
        if (instance == null) {
            instance = new FakeVoiceManager();        
	}

        return instance;
    }
    
    private FakeVoiceManager() {
        DataManager dm = AppContext.getDataManager();

        try {
            dm.getBinding(DS_FAKE_CALL_STATUS_LISTENERS);
        } catch (NameNotBoundException e) {
            try {
                dm.setBinding(DS_FAKE_CALL_STATUS_LISTENERS, new FakeCallStatusListeners());
            }  catch (RuntimeException re) {
                logger.fine("failed to bind pending map " + re.getMessage());
                throw re;
            }
        }
    }
    
    public static final class FakeCallStatusListeners extends HashMap<ManagedReference, String> 
    	    implements ManagedObject, Serializable {

         private static final long serialVersionUID = 1;
    }
    
    public void setupCall(String callID, String sipUrl) {
    }
    
    public void endCall(String callID) {
        DataManager dm = AppContext.getDataManager();
        FakeCallStatusListeners callList = (FakeCallStatusListeners)
                dm.getBinding(DS_FAKE_CALL_STATUS_LISTENERS);
        
        Iterator iter = callList.keySet().iterator();
        
        HashMap options = new HashMap();
        options.put("CallId", callID);
        CallStatus newStatus = CallStatus.getInstance("", CallStatus.ENDED, options);  
       
        while(iter.hasNext()) {
            ManagedReference ref = (ManagedReference)iter.next();
            String mappedCallID = callList.get(ref);
                
            if (mappedCallID == null || mappedCallID.compareTo(callID) == 0) {
                try {
                    ManagedCallStatusListener listener = (ManagedCallStatusListener)
			ref.getForUpdate();
                    listener.callStatusChanged(newStatus); 
                } catch (Exception ex) {
                    //HARRISTODO: Catch this properly
                }
            }                
        }

        logger.info("Ended fake call. CallID: " + callID);
    }
    
    public void addCallStatusListener(ManagedCallStatusListener listener) {
        addCallStatusListener(listener, null);
    }

    public void addCallStatusListener(ManagedCallStatusListener listener, 
	String callID) {
                
        if (listener != null) {
            DataManager dm = AppContext.getDataManager();
            
            FakeCallStatusListeners callList = (FakeCallStatusListeners)
                dm.getBinding(DS_FAKE_CALL_STATUS_LISTENERS);
            
            ManagedReference ref = AppContext.getDataManager().createReference(listener);
            
            synchronized(callList) {
                callList.put(ref, callID);                
            }
            logger.fine("Added CallStatusListener " + listener + " to callID: " + callID);
        }
    }
	
    public void removeCallStatusListener(ManagedCallStatusListener toRemove) {
        DataManager dm = AppContext.getDataManager();
        FakeCallStatusListeners callList = (FakeCallStatusListeners)
                dm.getBinding(DS_FAKE_CALL_STATUS_LISTENERS);
        
        //Iterator listIter = callList.values().iterator();
        Iterator iter = callList.keySet().iterator();
        
        synchronized(callList) {
            callList.remove(toRemove);
        }
        
        logger.fine("Removed CallStatusListener: " + toRemove);
    }

}
