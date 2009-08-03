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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;
import org.jdesktop.wonderland.client.jme.JmeClientMain;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.client.login.SessionLifecycleListener;
import org.jdesktop.wonderland.modules.textchat.client.TextChatConnection.TextChatListener;

/**
 * Manages all of the Text Chat windows for the client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ChatManager implements TextChatListener {

    private static Logger logger = Logger.getLogger(ChatManager.class.getName());
    // a mapping from named text chats to HUD components
    private Map<String, WeakReference<HUDComponent>> textChatHUDRefMap = null;
    // a further mapping from a text chat HUD component to the underlying
    // TextChatPanel
    private Map<HUDComponent, WeakReference<TextChatPanel>> textChatPanelRefMap = null;
    private JCheckBoxMenuItem textChatMenuItem = null;
    private TextChatConnection textChatConnection = null;
    private String localUserName = null;
    private ServerSessionManager loginInfo = null;
    private SessionLifecycleListener sessionListener = null;

    public ChatManager(final ServerSessionManager loginInfo) {
        this.loginInfo = loginInfo;
        textChatHUDRefMap = new HashMap();
        textChatPanelRefMap = new HashMap();

        // create the text chat all HUD component
        HUDComponent textChatHUDComponent = createTextChatHUD("");
        textChatHUDComponent.setName("Text Chat All");

        // keep a weak reference to it so that it gets garbage collected
        final WeakReference<HUDComponent> hudPanelRef = new WeakReference(textChatHUDComponent);
        textChatHUDRefMap.put("", new WeakReference(textChatHUDComponent));

        // Create the global text chat menu item. Listen for when it is
        // selected or de-selected and show/hide the frame as appropriate.
        textChatMenuItem = new JCheckBoxMenuItem("Text Chat All");
        textChatMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                HUDComponent textChatHUDComponent = hudPanelRef.get();
                boolean show = !textChatHUDComponent.isVisible();
                textChatMenuItem.setState(show);
                textChatHUDComponent.setVisible(show);
            }
        });
        textChatMenuItem.setEnabled(false);

        // Add the global text chat menu item to the "Window" menu
        JmeClientMain.getFrame().addToWindowMenu(textChatMenuItem, 2);

        // Wait for a primary session to become active. When it does, then
        // we enable the menu items and set the primary sessions on their
        // objects.
        sessionListener = new SessionLifecycleListener() {

            public void sessionCreated(WonderlandSession session) {
                // Do nothing for now
            }

            public void primarySession(WonderlandSession session) {
                setPrimarySession(session);
            }
        };
        loginInfo.addLifecycleListener(sessionListener);

        // XXX Check if we already have primary session, should be handled
        // by addLifecycleListener
        if (loginInfo.getPrimarySession() != null) {
            setPrimarySession(loginInfo.getPrimarySession());
        }
    }

    private HUDComponent createTextChatHUD(final String userKey) {
        final TextChatPanel textChatPanel = new TextChatPanel();

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        HUDComponent textChatHUDComponent = mainHUD.createComponent(textChatPanel);
        textChatHUDComponent.setIcon(new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/textchat/client/resources/UserListChatText32x32.png")));
        textChatHUDComponent.setPreferredLocation(Layout.SOUTHWEST);
        mainHUD.addComponent(textChatHUDComponent);

        textChatHUDComponent.addEventListener(new HUDEventListener() {

            public void HUDObjectChanged(HUDEvent e) {
                // Remove from the map which will let it garbage collect
                if (e.getEventType() == HUDEventType.CLOSED) {
                    synchronized (textChatHUDRefMap) {
                        e.getObject().setVisible(false);
                        // TODO: really dispose of HUD component
                        WeakReference<HUDComponent> ref = textChatHUDRefMap.get(userKey);
                        if (ref != null) {
                            HUDComponent textChatHUDComponent = ref.get();
                            textChatHUDRefMap.remove(userKey);
                            textChatPanelRefMap.remove(textChatHUDComponent);
                        }
                    }
                }
            }
        });

        textChatPanelRefMap.put(textChatHUDComponent, new WeakReference(textChatPanel));

        return textChatHUDComponent;
    }

    /**
     * Unregister and menus we have created, etc.
     */
    public void unregister() {
        // Close down all of the individual text chat windows
        for (Map.Entry<String, WeakReference<HUDComponent>> entry : textChatHUDRefMap.entrySet()) {
            HUDComponent component = entry.getValue().get();
            component.setVisible(false);
            // TODO: really dispose of HUD component
        }
        textChatHUDRefMap.clear();

        // remove the session listener
        loginInfo.removeLifecycleListener(sessionListener);

        // Remove the menu item
        JmeClientMain.getFrame().removeFromWindowMenu(textChatMenuItem);
    }

    /**
     * Sets the primary session, when it is made the primary session. This
     * turns on everything: enables the menu items, displays the global chat
     * dialog.
     */
    private void setPrimarySession(WonderlandSession session) {
        // Capture the local user name for later use
        localUserName = session.getUserID().getUsername();

        // Create a new custom connection to receive text chats. Register a
        // listener that handles new text messages. Will display them in the
        // window.
        textChatConnection = new TextChatConnection();
        textChatConnection.addTextChatListener(this);

        // Open the text chat connection. If unsuccessful, then log an error
        // and return.
        try {
            textChatConnection.connect(session);
        } catch (ConnectionFailureException excp) {
            logger.log(Level.WARNING, "Unable to establish a connection to " +
                    "the chat connection.", excp);
            return;
        }

        // Next, for the global chat, set its information and make it visible
        // initially.
        HUDComponent textChatHUDComponent = textChatHUDRefMap.get("").get();
        TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();

        textChatPanel.setActive(textChatConnection, localUserName, "");
        textChatMenuItem.setEnabled(true);
        textChatMenuItem.setSelected(true);
        textChatHUDComponent.setVisible(true);
    }

    /**
     * Creates a new text chat window, given the remote participants user name
     * and displays it.
     *
     * @param remoteUser The remote participants user name
     */
    public void startChat(String remoteUser) {
        // Do all of this synchronized. This makes sure that multiple text chat
        // window aren't create if a local user clicks to create a new text
        // chat and a message comes in for that remote user.
        synchronized (textChatHUDRefMap) {
            // Check to see if the text chat window already exists. If so, then
            // we do nothing and return.
            WeakReference<HUDComponent> ref = textChatHUDRefMap.get(remoteUser);
            if (ref != null) {
                return;
            }

            // Otherwise, create the frame, add it to the map, and display
            HUDComponent textChatHUDComponent = createTextChatHUD(remoteUser);
            textChatHUDComponent.setName("Text Chat" + " (" + remoteUser + ")");
            textChatHUDRefMap.put(remoteUser, new WeakReference(textChatHUDComponent));
            TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();
            textChatPanel.setActive(textChatConnection, localUserName, remoteUser);
            textChatHUDComponent.setVisible(true);
        }
    }

    /**
     * Deactivates the text chat given the remote user's name, if such a frame
     * exists. Displays a message in the window and turns off its GUI.
     *
     * @param remoteUser The remote participants user name
     */
    public void deactivateChat(String remoteUser) {
        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats
        synchronized (textChatHUDRefMap) {
            // Check to see if the text chat window exists. If not, then do
            // nothing.
            WeakReference<HUDComponent> ref = textChatHUDRefMap.get(remoteUser);
            if (ref == null) {
                return;
            }
            HUDComponent textChatHUDComponent = ref.get();
            TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();
            textChatPanel.deactivate();
        }
    }

    /**
     * Re-activates the text chat given the remote user's name, if such a frame
     * exists. Displays a message in the window and turns on its GUI.
     */
    public void reactivateChat(String remoteUser) {
        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats
        synchronized (textChatHUDRefMap) {
            // Check to see if the text chat window exists. If not, then do
            // nothing.
            WeakReference<HUDComponent> ref = textChatHUDRefMap.get(remoteUser);
            if (ref == null) {
                return;
            }
            HUDComponent textChatHUDComponent = ref.get();
            TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();
            textChatPanel.reactivate();
        }
    }

    /**
     * @inheritDoc()
     */
    public void textMessage(String message, String fromUser, String toUser) {
        // Fetch the frame associated with the user. If the "to" user is an
        // empty string, then this is a "global" message and we fetch its
        // frame. It should exist. We always add the message, no matter whether
        // the frame is visible or not.
        if (toUser == null || toUser.equals("") == true) {
            WeakReference<HUDComponent> ref = textChatHUDRefMap.get("");
            if (ref == null) {
                return;
            }
            HUDComponent textChatHUDComponent = ref.get();
            TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();

            textChatPanel.appendTextMessage(message, fromUser);
            return;
        }

        // Otherwise, the "toUser" is for this specific user. We fetch the
        // frame associated with the "from" user. If it exists (which also
        // means it is visible, then add the message.
        synchronized (textChatHUDRefMap) {

            WeakReference<HUDComponent> ref = textChatHUDRefMap.get(fromUser);
            if (ref != null) {
                HUDComponent textChatHUDComponent = ref.get();
                TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();
                textChatPanel.appendTextMessage(message, fromUser);
                return;
            }

            // Finally, we reached here when we have a message from a specific
            // user, but the frame does not exist, and is not visible. So we
            // create it and add to the map and display it.
            HUDComponent textChatHUDComponent = createTextChatHUD(fromUser);
            textChatHUDRefMap.put(fromUser, new WeakReference(textChatHUDComponent));
            TextChatPanel textChatPanel = textChatPanelRefMap.get(textChatHUDComponent).get();
            textChatPanel.setActive(textChatConnection, toUser, fromUser);
            textChatPanel.appendTextMessage(message, fromUser);
            textChatHUDComponent.setVisible(true);
        }
    }
}
