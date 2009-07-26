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

import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import java.io.Serializable;
import java.util.logging.Logger;
import org.jdesktop.wonderland.modules.sas.common.SasProviderConnectionType;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.modules.sas.common.SasProviderLaunchStatusMessage;
import org.jdesktop.wonderland.modules.sas.common.SasProviderAppExittedMessage;
import com.sun.sgs.app.AppContext;

/**
 * The connection between the SAS provider and the SAS server.
 * 
 * @author deronj
 */
public class SasProviderConnectionHandler implements ClientConnectionHandler, Serializable {

    private static final Logger logger = Logger.getLogger(SasProviderConnectionHandler.class.getName());
    
    /** The name of the Darkstar binding we use to store the reference to the messages in flight list. */
    private static String PROVIDER_MESSAGES_IN_FLIGHT_BINDING_NAME = 
        "org.jdesktop.wonderland.modules.sas.server.ProviderMessagesInFlight";

    /** The name of the Darkstar binding we use to store the reference to the running app list. */
    private static String RUNNING_APPS_BINDING_NAME = 
        "org.jdesktop.wonderland.modules.sas.server.RunningAppInfo";

    /** The SAS server which lives in the Wonderland server. */
    private ManagedReference<SasServer> serverRef;

    public SasProviderConnectionHandler(SasServer server) {
        super();
        serverRef = AppContext.getDataManager().createReference(server);

        ProviderMessagesInFlight messagesInFlight = new ProviderMessagesInFlight();
        AppContext.getDataManager().setBinding(PROVIDER_MESSAGES_IN_FLIGHT_BINDING_NAME, messagesInFlight);

        RunningAppInfo runningApps = new RunningAppInfo();
        AppContext.getDataManager().setBinding(RUNNING_APPS_BINDING_NAME, runningApps);
    }

    public static ProviderMessagesInFlight getProviderMessagesInFlight () {
        return (ProviderMessagesInFlight) 
            AppContext.getDataManager().getBinding(PROVIDER_MESSAGES_IN_FLIGHT_BINDING_NAME);
    }

    public static RunningAppInfo getRunningApps () {
        return (RunningAppInfo) 
            AppContext.getDataManager().getBinding(RUNNING_APPS_BINDING_NAME);
    }

    public ConnectionType getConnectionType() {
        return SasProviderConnectionType.CLIENT_TYPE;
    }

    public void registered(WonderlandClientSender sender) {
        logger.info("Sas provider connection registered.");
    }

    public void clientConnected(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Properties properties) 
    {
        SasServer server = (SasServer) serverRef.get();
        server.providerConnected(sender, clientID);
    }

    public static void addProviderMessageInFlight (MessageID msgID, ProviderProxy provider, CellID cellID) {
        getProviderMessagesInFlight().addMessageInfo(msgID, provider, cellID);
    }

    public void messageReceived(WonderlandClientSender sender, 
                                WonderlandClientID clientID,
                                Message message) 
    {
        logger.severe("***** Message received from sas provider: " + message);

        if (message instanceof SasProviderLaunchStatusMessage) {
            SasProviderLaunchStatusMessage msg = (SasProviderLaunchStatusMessage) message;
            SasProviderLaunchStatusMessage.LaunchStatus status = msg.getStatus();
            MessageID launchMsgID = msg.getLaunchMessageID();
            String connInfo = msg.getConnectionInfo();

            logger.severe("############### ProviderConnectionHandler: Launch status message received");
            logger.severe("status = " + status);
            logger.severe("launchMsgID = " + launchMsgID);
            logger.severe("connInfo = " + connInfo);

            ProviderMessagesInFlight messagesInFlight = getProviderMessagesInFlight();
            ProviderMessagesInFlight.MessageInfo msgInfo = messagesInFlight.getMessageInfo(launchMsgID);
            if (msgInfo == null || msgInfo.provider == null) {
                logger.warning("Cannot find provider proxy for message " + launchMsgID);
                return;
            }
            messagesInFlight.removeMessageInfo(launchMsgID);

            // Transition the app to the running apps list. It will stay there until the app exits
            // or the provider quits
            getRunningApps().addAppInfo(launchMsgID, msgInfo.provider, msgInfo.cellID);
            
            msgInfo.provider.appLaunchResult(status, msgInfo.cellID, connInfo);

        } else if (message instanceof SasProviderAppExittedMessage) {
            SasProviderAppExittedMessage msg = (SasProviderAppExittedMessage) message;
            MessageID launchMsgID = msg.getLaunchMessageID();
            int exitValue = msg.getExitValue();

            RunningAppInfo.AppInfo appInfo = getRunningApps().getAppInfo(launchMsgID);
            if (appInfo != null) {
                getRunningApps().removeAppInfo(launchMsgID);
                appInfo.provider.appExitted(appInfo.cellID, exitValue);
            } else {
                logger.warning("Cannot find provider to notify that the app launched with this message ID has exitted." + launchMsgID);
            }

        } else {
            logger.warning("Invalid message received, message = " + message);
        }
    }

    public void clientDisconnected(WonderlandClientSender sender, WonderlandClientID clientID) {
        logger.fine("SasProvider client disconnected.");
        SasServer server = (SasServer) serverRef.get();
        ProviderProxy provider = server.providerDisconnected(sender, clientID);
        if (provider != null) {

            ProviderMessagesInFlight messagesInFlight = getProviderMessagesInFlight();
            if (messagesInFlight != null) {
                messagesInFlight.removeMessagesForProvider(provider);
            }

            RunningAppInfo runningApps = getRunningApps();
            if (runningApps != null) {
                runningApps.removeAppInfosForProvider(provider);
            }

            provider.cleanup();
        }
    }
}
