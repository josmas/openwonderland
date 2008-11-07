/*
 * IncomingCallHandler.java  (2008)
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * 
 * Sun Microsystems, Inc. has intellectual property rights relating to
 * technology embodied in the product that is described in this document. In
 * particular, and without limitation, these intellectual property rights may
 * include one or more of the U.S. patents listed at http://www.sun.com/patents
 * and one or more additional patents or pending patent applications in the
 * U.S. and in other countries.
 * 
 * SUN PROPRIETARY/CONFIDENTIAL.
 * 
 * U.S. Government Rights - Commercial software. Government users are subject
 * to the Sun Microsystems, Inc. standard license agreement and applicable
 * provisions of the FAR and its supplements.
 * 
 * Use is subject to license terms.
 * 
 * This distribution may include materials developed by third parties. Sun, Sun
 * Microsystems, the Sun logo, Java, Jini, Solaris and Sun Ray are trademarks
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 * 
 * UNIX is a registered trademark in the U.S. and other countries, exclusively
 * licensed through X/Open Company, Ltd.
 */

package org.jdesktop.wonderland.modules.phone.server.cell;

import com.sun.sgs.app.AppContext;

import org.jdesktop.wonderland.common.cell.CellID;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import com.sun.mpk20.voicelib.app.Call;
import com.sun.mpk20.voicelib.app.CallSetup;
import com.sun.mpk20.voicelib.app.Player;
import com.sun.mpk20.voicelib.app.PlayerSetup;
import com.sun.mpk20.voicelib.app.Spatializer;
import com.sun.mpk20.voicelib.app.DefaultSpatializer;
import com.sun.mpk20.voicelib.app.ManagedCallBeginEndListener;
import com.sun.mpk20.voicelib.app.ManagedCallStatusListener;
import com.sun.mpk20.voicelib.app.VoiceManager;

import com.sun.voip.CallParticipant;

import com.sun.voip.client.connector.CallStatus;
import com.sun.voip.client.connector.CallStatusListener;

import java.math.BigInteger;

import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import com.jme.math.Vector3f;

/**
 * Listen for incoming calls, play treatments to prompt the caller for 
 * the phone to call.  Transfer the call to that phone.
 */
public class IncomingCallHandler implements ManagedCallBeginEndListener,
	ManagedCallStatusListener, Serializable {

    /** a logger */
    private static final Logger logger =
            Logger.getLogger(IncomingCallHandler.class.getName());

    private static int defaultCallAnswerTimeout = 90;  // 90 seconds

    private HashMap<String, CallHandler> callTable = new HashMap();

    private static final int DEFAULT_TIMEOUT = 30000;
    private static int timeout = DEFAULT_TIMEOUT;

    private class PhoneInfo implements Serializable {
	public CellID phoneCellID;
	public Vector3f origin;
	public String phoneNumber;
	public String phoneLocation;
	public double zeroVolumeRadius;
	public double fullVolumeRadius;

	public PhoneInfo(CellID phoneCellId, Vector3f origin, 
		String phoneNumber, String phoneLocation,
		double zeroVolumeRadius, double fullVolumeRadius) {

	    this.phoneCellID = phoneCellId;
	    this.origin = origin;    
	    this.phoneNumber = phoneNumber;
	    this.phoneLocation = phoneLocation;
	    this.zeroVolumeRadius = zeroVolumeRadius;
	    this.fullVolumeRadius = fullVolumeRadius;
	}
    }
	
    private HashMap<String, PhoneInfo> phoneMap = new HashMap();

    private ArrayList<PhoneInfo> phoneList = new ArrayList();

    private static IncomingCallHandler incomingCallHandler;

    /**
     * Constructor.
     */
    private IncomingCallHandler() {
    }

    public static IncomingCallHandler getInstance() {
	if (incomingCallHandler != null) {
	    return incomingCallHandler;
	}

	incomingCallHandler = new IncomingCallHandler();

	return incomingCallHandler;
    }

    public static void initialize() {
	VoiceManager vm = AppContext.getManager(VoiceManager.class);

	vm.addCallStatusListener(incomingCallHandler, null);

	vm.addCallBeginEndListener(incomingCallHandler);
    }

    public void addPhone(CellID phoneCellID, Vector3f origin, 
	    String phoneNumber, String phoneLocation,
	    double zeroVolumeRadius, double fullVolumeRadius) {

	logger.info("adding phone " + phoneNumber + " "
	    + phoneLocation + " zero volume radius " + zeroVolumeRadius
	    + " full volume radius " + fullVolumeRadius);

	/*
	 * Graphics interchanges y and z.  We switch them around here.
	 */
	Vector3f v = new Vector3f(origin.getX(), origin.getZ(), origin.getY());

	logger.finer("phone origin " + v);

	synchronized (phoneMap) {
	    PhoneInfo phoneInfo = 
		new PhoneInfo(phoneCellID, v, phoneNumber, phoneLocation,
		    zeroVolumeRadius, fullVolumeRadius);

	    phoneMap.put(phoneNumber, phoneInfo);

	    /*
	     * Add phoneInfo to the right place in the list so that
	     * the phone numbers are increasing.
	     */
	    int n;

	    try {
		n = Integer.parseInt(phoneNumber);
	    } catch (NumberFormatException e) {
	        phoneList.add(phoneInfo);
		return;
	    }

	    synchronized (phoneList) {
		for (int i = 0; i < phoneList.size(); i++) {
		    PhoneInfo pi = (PhoneInfo) phoneList.get(i);

		    int nn;

		    try {
			nn = Integer.parseInt(pi.phoneNumber);
	    	    } catch (NumberFormatException e) {
	        	phoneList.add(phoneInfo);
			return;
		    }

		    if (n < nn) {
	        	phoneList.add(i, phoneInfo);
			return;
		    }
		}

	        phoneList.add(phoneInfo);
	    }
	}
    }
	
    /*
     * Called when a call is established or ended.
     */
    public void callBeginEndNotification(CallStatus status) {
	logger.fine("got status " + status);

	String callId = status.getCallId();

	int code = status.getCode();

	if (callId == null) {
	    return;	// initial message doesn't have a CallId.
	}

	CallHandler callHandler;

	synchronized(callTable) {
	    callHandler = callTable.get(callId);
	}

	if (callHandler != null) {
	    callHandler.callStatusChanged(status);

            if (code == CallStatus.ENDED) {
	        synchronized(callTable) {
	            callTable.remove(callId);
	        }

	        callHandler.removeCallStatusListener(this);

		try {
		    callHandler.getCall().end(false);
		} catch (IOException e) {
		    logger.warning("Unable to end call " + callHandler.getCall()
			+ ": " + e.getMessage());
		}
	    }
	    return;
	}

	if (code != CallStatus.ESTABLISHED) {
	    return;
	}

	String incomingCall = status.getOption("IncomingCall");

	if (incomingCall == null || incomingCall.equals("false")) {
	    return;		// it's not an incoming call
	}

	/*
	 * New incoming call
	 */
	CallSetup setup = new CallSetup();
	setup.cp = new CallParticipant();	
	setup.cp.setCallId(callId);
	setup.cp.setPhoneNumber(status.getCallInfo());

	Call call;

	try {
	    call = AppContext.getManager(VoiceManager.class).createCall(callId, setup);
	} catch (IOException e) {
	    logger.warning("Unable to create call " + callId + ": " + e.getMessage());
	    return;
	}

	callHandler = new CallHandler(call, status, timeout);

	synchronized(callTable) {
	    callTable.put(callId, callHandler);
	}
    }

    public void callStatusChanged(CallStatus status) {
	String callId = status.getCallId();

	CallHandler callHandler;

	synchronized(callTable) {
	    callHandler = callTable.get(callId);
	}

	if (callHandler == null) {
	    return;
	}

	callHandler.callStatusChanged(status);
    }

    private class CallHandler extends Thread implements Serializable {

	CallStatus establishedStatus;

        private String phoneNumber = "";

 	private PhoneInfo phoneInfo;

	private int attemptCount = 0;
    
	private int state;

	private int nextPhoneIndex;

        private static final int WAITING_FOR_PHONE_NUMBER = 1;
        private static final int SELECTING_PHONE_NUMBER   = 2;
        private static final int ESTABLISHED		  = 3;
	private static final int CALL_ENDED		  = 4;

	private static final String ENTER_PHONE_NUMBER = 
	    "enter_meeting_code.au";

	private static final String BAD_PHONE_NUMBER = 
	    "bad_meeting_code_1.au";

	private static final String INCOMING_TIMEOUT =
	    "incoming_timeout.au";

	private static final String JOIN_CLICK = "joinCLICK.au";

        private String lastMessagePlayed;

 	private Call call;

	public CallHandler(Call call, CallStatus establishedStatus, 	
		int timeout) {

	    this.call = call;
	    this.establishedStatus = establishedStatus;

	    logger.info("New Call Handler for call " + call);

	    state = WAITING_FOR_PHONE_NUMBER;

	    playWaitingForPhoneNumber();

	    // TODO The thread must run as a darkstar transaction!
	    //start();
	}

	public Call getCall() {
	    return call;
	}

	public void addCallStatusListener(ManagedCallStatusListener listener) {
	    AppContext.getManager(VoiceManager.class).addCallStatusListener(listener, null);
	}

	public void removeCallStatusListener(ManagedCallStatusListener listener) {
	    AppContext.getManager(VoiceManager.class).removeCallStatusListener(listener);
	}

	private void playWaitingForPhoneNumber() {
	    playTreatment("enter_phone_number.au");
	}

	public void run() {
	    /*
	     * Timeout handler to re-prompt user
	     */
	    long startTime = System.currentTimeMillis();

	    while (state == WAITING_FOR_PHONE_NUMBER) {
		int currentState = state;

		try {
		    Thread.sleep(timeout);

		    logger.info("state is  " + state + " for call " + call);

		    if (state != WAITING_FOR_PHONE_NUMBER) {
			break;
		    }

		    if (currentState == state) {
			if (System.currentTimeMillis() - startTime >=
			        defaultCallAnswerTimeout * 1000) {
			    
			    playTreatment(INCOMING_TIMEOUT);

			    //
			    // TODO (maybe)
			    //
			    // We'd like to wait until the treatment is done
			    // before cancelling the call.
			    // Need a way to specify an end treatment after
			    // the call is started.
			    //
			    try {
				Thread.sleep(5000);	
			    } catch (InterruptedException e) {
			    }

			    cancelCall(call);
			    break;
			}

			/*
			 * FIX ME:  This has to become a darkstar transaction!
			 */
			playTreatment(lastMessagePlayed);
		    }
		} catch (InterruptedException e) {
		    logger.warning("Interrupted!");
		}
	    }
	}

        private void cancelCall(Call call) {
	    try {
	        call.end(true);
	    } catch (IOException e) {
		logger.warning("Unable to end call " + call + ": " + e.getMessage());
	    }
	}

	private void playTreatment(String treatment) {
	    try {
	        call.playTreatment(treatment);
	    } catch (IOException e) {
		logger.warning("Unable to play treat to call " + call + ": " + e.getMessage());
	    }

	    lastMessagePlayed = treatment;
	}

	public void callStatusChanged(CallStatus status) {
	    logger.fine("got status " + status);

	    int code = status.getCode();

            if (code == CallStatus.ENDED) {
                logger.fine("Call ended...");
		state = CALL_ENDED;
                return;
            }

            int ix;

	    /*
	     * We're only interested in dtmf keys
	     */
            if (code != CallStatus.DTMF_KEY) {
		return;
	    }

            String dtmfKey = status.getDtmfKey();
	    
	    if (state == WAITING_FOR_PHONE_NUMBER) {
		getPhoneNumber(dtmfKey);
	    } else if (state == SELECTING_PHONE_NUMBER) {
		selectPhoneNumber(dtmfKey);
	    }
        }

 	private void handleOption(String dtmfKey) {
	    if (dtmfKey.equals("4")) {
		playTreatment("pound.au");
		playTreatment("star.au");
		playTreatment("mute.au");
		playTreatment("unmute.au");
		playTreatment("less_volume.au");
		playTreatment("more_volume.au");

	        if (state == WAITING_FOR_PHONE_NUMBER) {
		    playWaitingForPhoneNumber();
		}

		return;
	    }
	}

	private void getPhoneNumber(String dtmfKey) {
            if (!dtmfKey.equals("#")) {
                phoneNumber += dtmfKey;  // accumulate phoneNumber
                return;
            }

	    if (phoneNumber.length() == 0) {
		state = SELECTING_PHONE_NUMBER;
		selectPhoneNumber();
		return;
	    }

            attemptCount++;

            if ((phoneInfo = getPhoneInfo()) != null) {
                logger.fine("Found phone for " + phoneNumber
		    + " at " + phoneInfo.origin);

                if (transferCall() == true) {
		    return;
                }
            }

	    playTreatment(BAD_PHONE_NUMBER);

            phoneNumber = "";
	}

	private void selectPhoneNumber() {
	    /*
	     * List the phone information and the number of people near by.
	     * Press # to skip or the phone number to select.
	     */
	    nextPhoneIndex = 0;
	    selectPhoneNumber(null);
	}
	
	private void selectPhoneNumber(String dtmfKey) {
	    if (dtmfKey != null) {
		if ((phoneInfo = getPhoneInfo(dtmfKey)) != null) {
                    if (transferCall() == false) {
			playTreatment("cant_transfer.au");
		    }

		    return;
		} else if (!dtmfKey.equals("#")) {
		    logger.fine("Unrecognized response:  " + dtmfKey);
		    playTreatment("unrecognized_respone.au");
		    return;
		}

		nextPhoneIndex++;
	    }

	    synchronized (phoneList) {
		if (nextPhoneIndex >= phoneList.size()) {
		    // play treatment saying there are no more phones
		    playTreatment("no_more_phones.au");

		    state = WAITING_FOR_PHONE_NUMBER;
		    playWaitingForPhoneNumber();
		    return;
		}

		phoneInfo = phoneList.get(nextPhoneIndex);

                Vector3f v = phoneInfo.origin;

                int n = AppContext.getManager(VoiceManager.class).getNumberOfPlayersInRange(
                    v.getX(), v.getY(), v.getZ());

		/*
		 * Play treatment saying the phone number and number of
		 * people in range.   <Press phoneNumber> to select, # to go to the next.
		 */
	        if (n == 0) {
		    playTreatment("phone_number.au;tts:" + phoneInfo.phoneNumber
			+ ";no_one.au");
	        } else if (n == 1) {
		    playTreatment("phone_number.au;tts:" + phoneInfo.phoneNumber
			+ ";one_person.au");
	        } else {
		    playTreatment("phone_number.au;tts:" + phoneInfo.phoneNumber
			+ ";has.au" + n + ";people_in_range.au");
		}

		if (phoneInfo.phoneLocation != null) {
		    playTreatment(phoneInfo.phoneLocation);
		} else {
		    playTreatment("unknown_location.au");
		}

		String s;

		if (phoneInfo.phoneNumber.equals("1")) {
		    s = "select_phone1.au;select_next.au";
		} else if (phoneInfo.phoneNumber.equals("2")) {
		    s = "select_phone2.au;select_next.au";
		} else if (phoneInfo.phoneNumber.equals("3")) {
		    s = "select_phone3.au;select_next.au";
		} else if (phoneInfo.phoneNumber.equals("4")) {
		    s = "select_phone4.au;select_next.au";
		} else if (phoneInfo.phoneNumber.equals("5")) {
		    s = "select_phone5.au;select_next.au";
		} else {
	            s = "tts:Press " + phoneInfo.phoneNumber + " to select this phone "
		        + "or pound, to skip to the next phone";
		}

	        playTreatment(s);
	    }
	}

	/*
	 * Find the conference
         */
	private PhoneInfo getPhoneInfo() {
            logger.fine("Looking for phoneNumber: " + phoneNumber);

	    return getPhoneInfo(phoneNumber);
	}

	private PhoneInfo getPhoneInfo(String phoneNumber) {
	    synchronized (phoneMap) {
	        return phoneMap.get(phoneNumber);
	    }
	}

	private boolean transferCall() {
	    try {
                logger.info("Transferring call " + call
		    + " to phone " + phoneInfo.phoneNumber);

		Vector3f origin = phoneInfo.origin;

		PlayerSetup setup = new PlayerSetup();
		setup.x = origin.getX();
		setup.y = origin.getY();
		setup.z = origin.getZ();
		setup.isOutworlder = true;
		setup.isLivePlayer = true;

		Player player = AppContext.getManager(VoiceManager.class).createPlayer(
		    call.getId(), setup);

		call.setPlayer(player);
		player.setCall(call);

if (false) {
                DefaultSpatializer extendedRadiusSpatializer = 
		    new DefaultSpatializer();

		extendedRadiusSpatializer.setZeroVolumeRadius(
		    phoneInfo.zeroVolumeRadius);
		extendedRadiusSpatializer.setFullVolumeRadius(
		    phoneInfo.fullVolumeRadius);

		/*
	 	 * Provide Outworlder with full volume for an
		 * extended radius.
		 */
                //voiceHandler.setIncomingSpatializer(callId, 
		//    extendedRadiusSpatializer);

		/*
		 * Provide Inworlders with full volume for an
		 * extended radius.
		 */
                //voiceHandler.setPublicSpatializer(callId, 
		//    extendedRadiusSpatializer);
}

                call.mute(false);
		call.transferToConference(
		    AppContext.getManager(VoiceManager.class).getConferenceId());
		
		String s;

		if (phoneInfo.phoneNumber.equals("1")) {
		    s = "xfer_phone1.au";
		} else if (phoneInfo.phoneNumber.equals("2")) {
		    s = "xfer_phone2.au";
		} else if (phoneInfo.phoneNumber.equals("3")) {
		    s = "xfer_phone3.au";
		} else if (phoneInfo.phoneNumber.equals("4")) {
		    s = "xfer_phone4.au";
		} else if (phoneInfo.phoneNumber.equals("5")) {
		    s = "xfer_phone5.au";
		} else {
		    s = "tts:transferring call to Phone number " 
		        + phoneInfo.phoneNumber;
		}

		playTreatment(s);

		String info = establishedStatus.getCallInfo();

		String phoneNumber = "callId";

		if (info != null) {
		    String[] tokens = info.split("@");

		    if (info.startsWith("sip:")) {
		        phoneNumber = tokens[2];
		    } else {
		        phoneNumber = tokens[1];
		    }
		}

		playTreatment("help.au");
		playTreatment(JOIN_CLICK);

		spawnAvatarOrb(this);

		state = ESTABLISHED;
            } catch (IOException e) {
                logger.warning(e.getMessage());
                return false;
            }

	    return true;
        }

	private void spawnAvatarOrb(CallHandler callHandler) {
 	    //Spawn the AvatarOrb to represent the new public call.
             String cellType =
                 "com.sun.labs.mpk20.avatarorb.server.cell.AvatarOrbCellGLO";
 
 	    String info = establishedStatus.getCallInfo();
 
 	    String name = "Anonymous";
 	    String number = "Unknown";
 
            if (info != null) {
                String[] tokens = info.split("@");
 
                if (info.startsWith("sip:")) {
 		    name = tokens[2];
                    number = tokens[2];
                } else {
                    name = tokens[0];
                    number = tokens[1];
                }
 	    }
 
            //CellGLO cellGLO = CellGLOFactory.loadCellGLO(cellType, 
	    //	phoneInfo.phoneCellID, call.getId(), name, number);
 
	    //callHandler.addCallStatusListener((ManagedCallStatusListener)cellGLO);
 	}
    }

}
