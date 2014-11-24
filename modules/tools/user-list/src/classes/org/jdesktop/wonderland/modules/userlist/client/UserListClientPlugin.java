/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.userlist.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 *
 * @author JagWire
 */
@Plugin
public class UserListClientPlugin extends BaseClientPlugin 
    implements ViewCellConfiguredListener, SessionLifecycleListener 
{
    private ServerSessionManager session;
    private static final Logger LOGGER = Logger.getLogger(UserListClientPlugin.class.getName());

    public UserListClientPlugin() {
    }
    
    @Override
    public void initialize(ServerSessionManager loginManager) {
        this.session = loginManager;
        session.addLifecycleListener(this);
        
        super.initialize(session);
    }

    @Override
    public void cleanup() {
        UserListPresenterManager.INSTANCE.cleanup();
        WonderlandUserList.INSTANCE.cleanup();
        
        session.removeLifecycleListener(this);
        session = null;
        
        super.cleanup();
    }
    
    @Override
    public void deactivate() {
        cleanup();
    }
    
    public void viewConfigured(LocalAvatar localAvatar) {
        /*
         * Sometimes the presencemanager plugin loads after user list plugin.
         * So the user list fails to find presence manager and can't load the user list panel.
         * so create a thread which will wait untill presence manager finish loadind.
         */
        new Thread(new Runnable() {

            public void run() {
                PresenceManager manager = PresenceManagerFactory.getPresenceManager(session.getPrimarySession());
                Cell cell = ClientContextJME.getViewManager().getPrimaryViewCell();
                while(manager==null) {
                    manager = PresenceManagerFactory.getPresenceManager(session.getPrimarySession());
                }
                while(cell==null) {
                    cell = ClientContextJME.getViewManager().getPrimaryViewCell();
                }
                PresenceInfo localPresenceInfo = manager.getPresenceInfo(cell.getCellID());
                while(localPresenceInfo==null) {
                    manager = PresenceManagerFactory.getPresenceManager(session.getPrimarySession());
                    cell = ClientContextJME.getViewManager().getPrimaryViewCell();
                    localPresenceInfo = manager.getPresenceInfo(cell.getCellID());
                }
                LOGGER.warning("Presence manager is loaded.");
                WonderlandUserList.INSTANCE.initialize();
                UserListPresenterManager.INSTANCE.intialize();
                UserListPresenterManager.INSTANCE.showActivePresenter();
            }
        }).start();
    }

    public void sessionCreated(final WonderlandSession session) {
        final UserListClientPlugin object = this;
        new Thread(new Runnable() {

            public void run() {
                LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
                avatar.addViewCellConfiguredListener(object);
            }
        }).start();
        
//        if (avatar!= null) {
//            viewConfigured(avatar);
//        }
    }

    public void primarySession(WonderlandSession session) {
//        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
//        avatar.addViewCellConfiguredListener(this);
    }
}
