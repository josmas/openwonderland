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

import org.jdesktop.wonderland.client.BaseClientPlugin;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar;
import org.jdesktop.wonderland.client.cell.view.LocalAvatar.ViewCellConfiguredListener;
import org.jdesktop.wonderland.client.comms.CellClientSession;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 *
 * @author JagWire
 */
@Plugin
public class UserListClientPlugin extends BaseClientPlugin 
    implements ViewCellConfiguredListener, SessionLifecycleListener 
{
    private ServerSessionManager session;

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
        WonderlandUserList.INSTANCE.initialize();
        UserListPresenterManager.INSTANCE.intialize();
        UserListPresenterManager.INSTANCE.showActivePresenter();
    }

    public void sessionCreated(WonderlandSession session) {
        
//        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();
//        
//        avatar.addViewCellConfiguredListener(this);
//        if (avatar!= null) {
//            viewConfigured(avatar);
//        }
    }

    public void primarySession(WonderlandSession session) {
        LocalAvatar avatar = ((CellClientSession) session).getLocalAvatar();        
        avatar.addViewCellConfiguredListener(this);
    }
}
