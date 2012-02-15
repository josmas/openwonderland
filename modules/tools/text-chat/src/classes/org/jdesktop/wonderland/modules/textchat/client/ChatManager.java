/**
 * Open Wonderland
 *
 * Copyright (c) 2010 - 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
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
import org.jdesktop.wonderland.modules.textchat.client.TextChatConnection.TextChatListener;

/**
 * Manages all of the Text Chat tabs in the HUD for the client.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 * @author Ronny Standtke <ronny.standtke@fhnw.ch>
 */
public class ChatManager implements TextChatListener {

    private static final Logger LOGGER =
            Logger.getLogger(ChatManager.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/textchat/client/resources/Bundle");
    // A mapping from user names to text chat TextChatPanel. NOTE: Accesses to
    // this Map must by synchronized, and only takes place on the AWT Event Thread.
    private Map<String, WeakReference<TextChatPanel>> userPanelRefMap;
    private JCheckBoxMenuItem textChatMenuItem;
    private TextChatConnection textChatConnection;
    private String localUserName;
    private TextChatPanelTabbedPane chatContainerPane = new TextChatPanelTabbedPane();
    private static final String TEXT_CHAT_ALL = "";
    private HUDComponent chatHud = null;

    /**
     * Singleton to hold instance of ChatManager. This holder class is loaded
     * on the first execution of ChatManager.getChatManager().
     */
    private static class ChatManagerHolder {

        private final static ChatManager manager = new ChatManager();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final ChatManager getChatManager() {
        return ChatManagerHolder.manager;
    }

    /**
     * Private constructor, singelton pattern
     */
    private ChatManager() {
        userPanelRefMap = new HashMap<String, WeakReference<TextChatPanel>>();

        // Create the global text chat menu item. Listen for when it is
        // selected or de-selected and show/hide the frame as appropriate. This
        // menu item will get added/removed for each primary session.
        textChatMenuItem = new JCheckBoxMenuItem(BUNDLE.getString("Text_Chat"));
        textChatMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // Fetch the global text chat menu item and make it visible
                // if not already so.
                boolean show = !chatHud.isVisible();
                textChatMenuItem.setState(show);
                //issue #174 hud visibility management
                if (show) {
                    chatHud.setMaximized();
                }
                chatHud.setVisible(show);
            }
        });
        textChatMenuItem.setEnabled(false);
    }

    /**
     * Registers the primary session
     * @param session the primary session
     */
    public void register(WonderlandSession session) {
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
            LOGGER.log(Level.WARNING, "Unable to establish a connection to " +
                    "the chat connection.", excp);
            return;
        }

        // Create the main HUD component and the default "All" panel, on the AWT Event Thread.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                chatHud = createTextChatHUD();
                chatHud.setName(BUNDLE.getString("Text_Chat"));
                // Create a new text chat Swing Panel
                createTabInHUD(null, "Text_Chat_All", TEXT_CHAT_ALL, "");
                textChatMenuItem.setEnabled(true);
                textChatMenuItem.setSelected(true);
                chatHud.setVisible(true);
                // Listen for when the HUD component closes. When it does, we
                // need to set the state of the checkbox menu item.
                chatHud.addEventListener(new HUDEventListener() {

                    public void HUDObjectChanged(HUDEvent event) {
                        // If the window is being closed, we need to update the
                        // state of the checkbox menu item, but we must do this
                        // in the AWT Event Thread.
                    	
                    	// modified for fixing issue #174 hud visibility management
                    	HUDEventType hudEventType = event.getEventType();
                    	if (hudEventType == HUDEventType.MINIMIZED
                    			|| hudEventType == HUDEventType.MAXIMIZED
                    			|| hudEventType == HUDEventType.CLOSED) {
                    		final boolean isSelected = hudEventType == HUDEventType.MAXIMIZED;
                    		SwingUtilities.invokeLater(new Runnable() {
                    			public void run() {
                    				textChatMenuItem.setSelected(isSelected);
                    			}
                    		});
                    	}
                    }
                });

                // Add the global text chat menu item to the "Window" menu. Note
                // we need to do this after the global text chat window is
                // create, otherwise there is a (small) chance that a user can
                // select the menu item before the text chat panel has been
                // created.
                JmeClientMain.getFrame().addToWindowMenu(textChatMenuItem, 2);
            }
        });
    }

    /**
     * Creates and returns a new text chat HUD Component.
     *
     * NOTE: This method assumes it is being called on the AWT Event Thread
     */
    private HUDComponent createTextChatHUD() {

        // Create a new HUD Panel. It still isn't visible.
        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        HUDComponent hudComponent = mainHUD.createComponent(chatContainerPane);
        hudComponent.setIcon(new ImageIcon(getClass().getResource(
                "/org/jdesktop/wonderland/modules/textchat/client/resources/" +
                "UserListChatText32x32.png")));
        hudComponent.setPreferredLocation(Layout.SOUTHWEST);
        mainHUD.addComponent(hudComponent);

        return hudComponent;
    }

    /**
     * Creates a new tab in the chat HUD. The panel may exist already and in that
     * case we just create a tab to host the panel.
     * Finally puts the reference to the panel in the user-panel map.
     * @param existingTextChatPanel if null creates a new one, if not it had been
     * created before but the user has closed the tab.
     * @param textChatWith Text (from bundle)
     * @param user user to establish the chat with
     * @param message Message to append if receiving a message from another user
     * for the first time (If they have opened a direct chat to us). Will be empty
     * in the case that we are opening conversation (or text chat all).
     */
    private void createTabInHUD(TextChatPanel existingTextChatPanel,
            String textChatWith, String user, String message){
        String name = BUNDLE.getString(textChatWith);
        name = MessageFormat.format(name, user);
        final TextChatPanel textChatPanel;
        if (existingTextChatPanel == null)
            textChatPanel = new TextChatPanel();
        else
            textChatPanel = existingTextChatPanel;

        chatContainerPane.tabPanel.add(name, textChatPanel);
        chatContainerPane.tabPanel.setTabPlacement(JTabbedPane.BOTTOM);
        int indexOfTextChatPanel = chatContainerPane.tabPanel.indexOfComponent(textChatPanel);
        if (user.equals(TEXT_CHAT_ALL)){ //Do not allow to close the main chat panel
            chatContainerPane.tabPanel.setTabComponentAt(indexOfTextChatPanel, 
                    new TextChatPanelTabbedPane(chatContainerPane.tabPanel, false));
        }
        else {
            chatContainerPane.tabPanel.setTabComponentAt(indexOfTextChatPanel, 
                    new TextChatPanelTabbedPane(chatContainerPane.tabPanel, true));
        }

        textChatPanel.setActive(textChatConnection, localUserName, user);
        if (!message.equals("")){ //someone opened conversation with us
            textChatPanel.appendTextMessage(message, user);
            colourInTab(indexOfTextChatPanel);
        }
        else {// we started the conversation 'against' another avatar
            chatContainerPane.tabPanel.setSelectedIndex(indexOfTextChatPanel);
            textChatPanel.getMessageTextField().requestFocusInWindow();
        }

        userPanelRefMap.put(user, new WeakReference<TextChatPanel>(textChatPanel));
    }

    private void colourInTab(int indexOfTextChatPanel) {
        ((TextChatPanelTabbedPane)chatContainerPane.tabPanel.
                getTabComponentAt(indexOfTextChatPanel)).label.setForeground(Color.RED);
    }

    /**
     * Unregister and menus we have created, etc.
     * NOTE: (by Design) this is only called on 'Logout' but never on 'Exit'
     * Potential TODO: Register a UserListener to do clean up on 'Exit'
     */
    public void unregister() {
        // First remove the listen for incoming text chat messages.
        textChatConnection.removeTextChatListener(this);

        // Next, remove the menu item. We need to do this before we shut down
        // all of the text chat windows, otherwise there is a (small) chance
        // that a user can select the menu item after the chat windows have
        // been destroyed.
        JmeClientMain.getFrame().removeFromWindowMenu(textChatMenuItem);

        // Finally, close down all of the individual text chat windows and clear
        // out the maps of text chat panels. We need to do this on the AWT
        // Even Thread.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                chatHud.setVisible(false);//TODO really dispose of the HUD

                // Clear out the map of all of the text chat panels.
                userPanelRefMap.clear();

                // Disconnect from the text chat connection. Note that we should
                // do this AFTER we close the text chat windows, otherwise there
                // is a (small) chance that someone will type in a window when
                // there is no active connection (which may result in a some-
                // what hamrless exception, but it's good to avoid if we can).
                textChatConnection.disconnect();
            }
        });
    }

    /**
     * Creates a new text chat tab, given the remote participants user name
     * and displays it.
     *
     * @param remoteUser The remote participants user name
     */
    public void startChat(final String remoteUser) {

        // Do all of this synchronized. This makes sure that multiple text chat
        // window aren't created if a local user clicks to create a new text
        // chat and a message comes in for that remote user. We do this on the
        // AWT Event Thread to achieve proper synchronization.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // Check to see if the text chat panel already exists. If so,
                // check if a tab exists, select it and return if it does.
                WeakReference<TextChatPanel> ref = userPanelRefMap.get(remoteUser);
                TextChatPanel textChatPanel = null;
                if (ref != null) {
                    textChatPanel = ref.get();
                    if (chatContainerPane.tabPanel.indexOfComponent(textChatPanel) != -1) {
                        chatContainerPane.tabPanel.setSelectedComponent(ref.get());
                        return;
                    }
                }

                // Otherwise, create the frame, add it to the map, and display
                createTabInHUD(textChatPanel, "Text_Chat_With", remoteUser, "");
            }
        });
    }

    /**
     * Deactivates the text chat given the remote user's name, if such a frame
     * exists. Displays a message in the window and turns off its GUI.
     *
     * @param remoteUser The remote participants user name
     */
    public void deactivateChat(final String remoteUser) {

        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats. We do this on the AWT Event Thread to achieve
        // proper synchronization.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // Check to see if the text chat window exists. If not, then do
                // nothing.
                WeakReference<TextChatPanel> ref = userPanelRefMap.get(remoteUser);
                if (ref == null) {
                    return;
                }
                TextChatPanel textChatPanel = ref.get();
                textChatPanel.deactivate();
            }
        });
    }

    /**
     * Re-activates the text chat given the remote user's name, if such a frame
     * exists. Displays a message in the window and turns on its GUI.
     * @param remoteUser the remote user's name
     */
    public void reactivateChat(final String remoteUser) {

        // Do all of this synchronized, so that we do not interfere with the
        // code to create chats. We do this on the AWT Event Thread to achieve
        // proper synchronization.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // Check to see if the text chat window exists. If not, then do
                // nothing.
                WeakReference<TextChatPanel> ref = userPanelRefMap.get(remoteUser);
                if (ref == null) {
                    return;
                }
                TextChatPanel textChatPanel = ref.get();
                textChatPanel.reactivate();
            }
        });
    }

    /**
     * @inheritDoc()
     */
    public void textMessage(final String message, final String fromUser,
            final String toUser) {

        // We do all of this on the AWT Event Thread to achieve the proper
        // synchronization on userPanelRefMap while also doing the proper
        // Swing stuff.
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // If the "to" user is an empty string, then this is a "global" message
                // It should exist. We always add the message, no
                // matter whether the frame is visible or not.
                if (toUser == null || toUser.equals("") == true) {
                    WeakReference<TextChatPanel> ref = userPanelRefMap.get(TEXT_CHAT_ALL);
                    if (ref == null) {
                        return;
                    }

                    TextChatPanel textChatPanel = ref.get();
                    textChatPanel.appendTextMessage(message, fromUser);
                    int textChatPanelIndex = chatContainerPane.tabPanel.indexOfComponent(textChatPanel);
                    if (textChatPanelIndex != chatContainerPane.tabPanel.getSelectedIndex())
                        colourInTab(textChatPanelIndex);
                    return;
                }

                // Otherwise, the "toUser" is for this specific user. We fetch
                // the frame associated with the "from" user. If it exists, and 
                // it's contained in a tab, then add the message.
                WeakReference<TextChatPanel> ref = userPanelRefMap.get(fromUser);
                TextChatPanel textChatPanel = null;
                if (ref != null) {
                    textChatPanel = ref.get();
                    int textChatPanelIndex = chatContainerPane.tabPanel.indexOfComponent(textChatPanel);
                    if (textChatPanelIndex != -1) {
                        textChatPanel.appendTextMessage(message, fromUser);
                        if (textChatPanelIndex != chatContainerPane.tabPanel.getSelectedIndex())
                        colourInTab(textChatPanelIndex);
                        return;
                    }
                }
                // Finally, we reached here when we have a message from a
                // specific user, but the panel or the tab does not exist.
                // So we create it and display it.
                createTabInHUD(textChatPanel, "Text_Chat_With", fromUser, message);
            }
        });
    }
}
