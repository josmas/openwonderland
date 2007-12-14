/**
 * Project Wonderland
 *
 * $RCSfile: WonderlandBoot.java,v $
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
 * $Revision: 1.23 $
 * $Date: 2007/12/03 16:03:41 $
 * $State: Exp $
 */
package org.jdesktop.wonderland.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.AppListener;
import com.sun.sgs.app.Channel;
import com.sun.sgs.app.ChannelManager;
import com.sun.sgs.app.ClientSession;
import com.sun.sgs.app.ClientSessionListener;
import com.sun.sgs.app.Delivery;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Logger;
import org.jdesktop.wonderland.common.config.WonderlandConfig;
import org.jdesktop.wonderland.common.messages.ProtocolVersion;
import org.jdesktop.wonderland.server.cell.MasterCellCacheMO;

/**
 * SGS Boot class for Wonderland
 */
//public class WonderlandBoot
//	implements SimBoot<WonderlandBoot>, SimUserListener {

public class WonderlandBoot implements AppListener, Serializable {
    
    private final static Logger logger = Logger.getLogger("wonderland.session");
    private Channel userChangeChannel;
    
    public ClientSessionListener loggedIn(ClientSession session) {
        //user has  logged in
        
        session.send(ProtocolVersion.getLocalVersion().getBytes());
        
        UserManager userManager = WonderlandContext.getUserManager();
        if (userManager.getUserCount()>=userManager.getUserLimit()) {
            session.send(new ErrorMessage("User limit exceeded").getBytes());
            session.disconnect();
        } else if (userManager.isLoggedIn(session.getName())) {
            session.send(new ErrorMessage("User is already logged on").getBytes());
            session.disconnect();
        }
        
        logger.info("User "+session.getName()+" has logged in.");

        return new WonderlandSessionListener(session);
    }
    
    /**
     * Initialize the server
     * 
     * @param props
     */
    public void initialize(Properties props) {
        logger.info("Wonderland");
        logger.info("Protocol Version "+ProtocolVersion.getLocalVersion().toString());
        // Create the channels.  This will only happen
        // once per restart from empty object store state
        // This is what we want.  They will exists from this
        // point on and the handles returned remain valid across
        // tasks and even system restarts
        
        new ChecksumManager(WonderlandConfig.getBaseURL()); // Use static getter to get reference to GLO
//        new ServerManagerGLO();      // Use static getter to get reference to GLO
        new UserManager();           // Use static getter to get reference to GLO    
        new MasterCellCacheMO();    // Use static getter to get reference to GLO    
                
//        ChannelManager chanMgr = AppContext.getChannelManager();
//        userChangeChannel = chanMgr.createChannel(ChannelInfo.USER_CHANGE, null, Delivery.RELIABLE);
    }
        
//    private static boolean serverPlatformIsUnix () {
//	String osName = System.getProperty("os.name");
//	return "Linux".equals(osName) || "SunOS".equals(osName);
//    }
}   
