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

package org.jdesktop.wonderland.modules.phone.client.cell;

//import org.jdesktop.wonderland.avatarorb.client.cell.AvatarOrbCell;

import  org.jdesktop.wonderland.modules.phone.common.CallListing;

import com.sun.sgs.client.ClientChannel;

import java.util.logging.Logger;

import org.jdesktop.wonderland.client.cell.Cell;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.phone.common.messages.PhoneMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneCellMessage;
import org.jdesktop.wonderland.modules.phone.common.messages.PhoneCellMessage.PhoneAction;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.SwingUtilities;

/**
 *
 * @author jprovino
 */
public class PhoneMessageHandler {

    private static final Logger logger =
            Logger.getLogger(PhoneMessageHandler.class.getName());

    private static final float HOVERSCALE = 1.5f;
    private static final float NORMALSCALE = 1.25f;
    
    private CallListing mostRecentCallListing;
         
    private boolean projectorState;
    
    private ProjectorStateUpdater projectorStateUpdater;

    private WonderlandSession session;

    private String name;

    private PhoneClient client;
        
    private PhoneForm phoneForm;

    public PhoneMessageHandler(PhoneClient client, WonderlandSession session) {
	this.client = client;
	this.session = session;

	logger.warning("CREATED PHONEMESSAGEHANDLER");

	phoneForm = new PhoneForm(client, session, this, false, "1", true);
	phoneForm.setVisible(true);
    }

    public WonderlandSession getSession() {
	return session;
    }
	
    public void processMessage(final PhoneMessage message) {
        CallListing listing = message.getCallListing();
        
        switch (message.getAction()) {       
        case START_HOVER:
            //handleHoverOther(message.getSelectionID(), true);
            //handleHoverOther(true);
            break;
            
        case STOP_HOVER:
            //handleHoverOther(message.getSelectionID(), false);
            //handleHoverOther(false);
            break;
      
        case PLACE_CALL:                
            if (message.wasSuccessful()) {
               
                listing = message.getCallListing();
                  
                if (mostRecentCallListing == null ||
			listing.equals(mostRecentCallListing) == false) {

		    break;
		}

		/*
		 * Make sure the most recent listing has the right private 
		 * client name.
		 */
		mostRecentCallListing.setPrivateClientName(
		    listing.getPrivateClientName());

		/*
		 * Set the call ID used by the server.
		 */
		mostRecentCallListing.setCallID(listing.getCallID());

                /*
		 * This is a confirmation msg for OUR call. 
		 * Update the form's selection.                        
		 */
                if (listing.isPrivate()) {
                    //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(true);
                }
            } else {
                logger.warning("PhoneCellGLO echo: Failed PLACE_CALL!");
            }
            break;
            
        case JOIN_CALL:
            //Hearing back from the server means this call has joined the world.
            if (message.wasSuccessful()) {               
                if (mostRecentCallListing == null || 
		        listing.equals(mostRecentCallListing) == false) {

		    break;
		}

                //This is a JOIN confirmation msg for OUR call. So we should no longer be whispering...
                 //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(false);                       
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        phoneForm.setCallEstablished(false);
                    }
                });
                
            } else {
                logger.warning("PhoneCellGLO echo: Failed JOIN_CALL");
	    }
                        
            break;        
            
	case LOCK_OR_UNLOCK:
	    phoneForm.changeLocked(message.getLocked(), message.wasSuccessful());
	    break;

        case END_CALL:
            if (message.wasSuccessful()) {    
                if (mostRecentCallListing == null || 
		        listing.equals(mostRecentCallListing) == false) {

		    break;
		}
                
                if (listing.isPrivate()) {
                    //This was a private call...
                    //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(false); 
                }
            } else {
                logger.warning("PhoneCellGLO echo: Failed END_CALL");
            }
            break;
            
        case CALL_INVITED:
            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		break;  // we didn't start this call
	    }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    phoneForm.setCallInvited();
                }
            });
            break;
            
        case CALL_ESTABLISHED:
            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		break;  // we didn't start this call
	    }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
		    synchronized (phoneForm) {
                        phoneForm.setCallEstablished(
			    mostRecentCallListing.isPrivate());
		    }
                }
            });
            
            break;
            
        case CALL_ENDED:
            if (mostRecentCallListing == null ||
		    listing.equals(mostRecentCallListing) == false) {

		break;
	    }

            if (mostRecentCallListing.isPrivate()) {
		//This was a private call...
                //ChannelController.getController().getLocalUser().getAvatarCell().setUserWhispering(false); 
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    phoneForm.setCallEnded(message.getReasonCallEnded());
                }
            });
            break;
        }
        
    }    
    
    public void leftChannel(ClientChannel arg0) {
        // ignore
    }
    
    public void placeCall(CallListing listing) {
	 CellID clientCellID = 
	    ((CellClientSession) session).getLocalAvatar().getViewCell().getCellID();

        PhoneCellMessage msg = new PhoneCellMessage(PhoneAction.PLACE_CALL, clientCellID,
	        PhoneCell.getInstance().getCellID(), listing);

	logger.warning("Sending place call message " + clientCellID + " " 
	    + listing);

	synchronized (phoneForm) {
            session.send(client, msg);    
        
            mostRecentCallListing = listing;      
	}
    }
    
    public void joinCall() {
	CellID clientCellID = 
	    ((CellClientSession) session).getLocalAvatar().getViewCell().getCellID();

        PhoneCellMessage msg = new PhoneCellMessage(PhoneAction.JOIN_CALL, 
	    clientCellID, clientCellID, mostRecentCallListing);

        session.send(client, msg);
    }
    
    public void endCall() {        
	CellID clientCellID = 
	    ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();

        PhoneCellMessage msg = new PhoneCellMessage(PhoneAction.END_CALL,
	    clientCellID, clientCellID, mostRecentCallListing);

        session.send(client, msg); 
    }
    
    public void dtmf(char c) {
        String treatment = "dtmf:" + c;

	CellID clientCellID = 
	    ((CellClientSession)session).getLocalAvatar().getViewCell().getCellID();

        PhoneCellMessage msg = new PhoneCellMessage(clientCellID, null, 
	    mostRecentCallListing, treatment, true);

	session.send(client, msg);
    }
    
        public void processEvent() {
                // react to mouse enter/exit events
        }

}
    
    class doPhoneFormRunnable implements Runnable {
        
        private PhoneForm phoneForm;

        public doPhoneFormRunnable(PhoneForm phoneForm) {
            this.phoneForm = phoneForm;
        }
        
        public void run() {                                        
            //Pop up a phoneForm here and get the address info.            
            phoneForm.setVisible(true);
        }        
    }
      
    class ProjectorStateUpdater extends Thread {

        private boolean running = true;
        
	public ProjectorStateUpdater() {
	    start();
	}

	public void run() {
	    while (running) {
		updateProjectorState();

		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
	    }
	}
        
        public void kill() {
            running = false;
        }

        private void updateProjectorState() {
        
            //boolean targetState = !callListingMap.isEmpty();
            boolean targetState = false;

            //ArrayList<Cell> childList = new ArrayList<Cell>();
            //getAllContainedCells(childList);
            //Iterator<Cell> iter = childList.iterator();
            //while(iter.hasNext()) {
            //    Cell c = iter.next();
                //if (c instanceof AvatarOrbCell) {
                //    targetState = true;
                //    break;
                //}
            //}
        
            //Are we switching states?
            //if (projectorState == targetState){
	    //    return;
	    //}

	    if (targetState){
                //Turn on        
		//cellLocal.addChild(projectorBG);
            } else {
                //Turn off
                //projectorBG.detach();
            }
            
            //projectorState = targetState;
	}
    
    }
