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
package org.jdesktop.wonderland.modules.textchat.client;

import java.lang.ref.WeakReference;
import org.jdesktop.wonderland.client.ClientPlugin;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.common.annotation.Plugin;

/**
 * Client-side plugin for the text chat feature.
 *
 * XXX Does not work with federation XXX
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Plugin
public class TextChatClientPlugin implements ClientPlugin {

    private WeakReference<ChatManager> chatManagerRef = null;

    /**
     * @inheritDoc()
     */
    public void initialize(ServerSessionManager loginInfo) {
        // Delegate to the manager class that handles all chat
        ChatManager chatManager = new ChatManager(loginInfo);
        chatManagerRef = new WeakReference(chatManager);
    }
}
