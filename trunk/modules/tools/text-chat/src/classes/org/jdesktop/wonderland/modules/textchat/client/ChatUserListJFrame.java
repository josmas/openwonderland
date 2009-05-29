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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession;
import org.jdesktop.wonderland.common.auth.WonderlandIdentity;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

/**
 * A list of users that someone can start a chat with.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ChatUserListJFrame extends javax.swing.JFrame {

    private Logger logger = Logger.getLogger(ChatUserListJFrame.class.getName());
    private PresenceManager presenceManager = null;
    private ChatManager chatManager = null;
    final private DefaultListModel listModel;
    private Map<String, WonderlandIdentity> userMap = null;
    private WonderlandIdentity localUserIdentity = null;

    /** Creates new form UserListJFrame */
    public ChatUserListJFrame(WonderlandIdentity id, ChatManager manager) {
        userMap = Collections.synchronizedMap(new HashMap());
        chatManager = manager;
        localUserIdentity = id;
        initComponents();

        // Set the list model on the list so we can add to it
        userList.setModel(listModel = new DefaultListModel());

        // Enable the "Start Chat" button only if an item is selected
        startChatButton.setEnabled(false);
        userList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                startChatButton.setEnabled(userList.getSelectedIndex() != -1);
            }
        });

        // Upon a double-click of the user list, activate the start button
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = userList.locationToIndex(e.getPoint());
                    userList.setSelectedIndex(index);
                    startChatButton.doClick();
                }
            }
        });

        // When the "Start Chat" button is selected, tell the chat manager to
        // begin a new chat
        startChatButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selectedUser = (String)userList.getSelectedValue();
                if (selectedUser == null) {
                    logger.warning("No user selected on chat window");
                    return;
                }

                WonderlandIdentity id = userMap.get(selectedUser);
                if (id == null) {
                    logger.warning("No ID found for user " + selectedUser);
                    return;
                }
                String remoteUser = id.getUsername();
                chatManager.startChat(remoteUser);
            }
        });
    }

    /**
     * Sets the primary session for the text chat. This turns on everything.
     *
     * @param session The primary Wonderland session
     */
    public void setPrimarySession(WonderlandSession session) {
        // Create the new presence manager with the given session and initialize
        // the list of users
        presenceManager = PresenceManagerFactory.getPresenceManager(session);
        initUserList();

         // Listen for changes in the users, and update the list as appropriate
        presenceManager.addPresenceManagerListener(new PresenceManagerListener() {
            public void userAdded(PresenceInfo pInfo) {
                // Add the user if it does not already exist and it is not the
                // same as this user.
                String displayName = getDisplayName(pInfo.userID);
                if (userMap.containsKey(displayName) == false) {
                    if (pInfo.userID.equals(localUserIdentity) == false) {
                        userMap.put(displayName, pInfo.userID);
                        listModel.addElement(displayName);
                    }
                }

                // Check to see if a frame already exists from a previous
                // session. If so, then reactivate it.
                chatManager.reactivateChat(pInfo.userID.getUsername());
            }

            public void userRemoved(PresenceInfo pInfo) {
                // Remove the user if it does exist and it is not the same as
                // this user.
                String displayName = getDisplayName(pInfo.userID);
                if (userMap.containsKey(displayName) == true) {
                    if (pInfo.userID.equals(localUserIdentity) == false) {
                        userMap.remove(displayName);
                        listModel.removeElement(displayName);
                    }
                }

                // Ask the Chat Manager if it has a frame corresponding to the
                // user chat. If so, then it de-activates it.
                chatManager.deactivateChat(pInfo.userID.getUsername());
            }

            public void presenceInfoChanged(PresenceInfo pInfo, ChangeType type) {
                // Dispatch toe userAdded() or userRemoved()
                if (type == ChangeType.USER_ADDED) {
                    userAdded(pInfo);
                }
                else if (type == ChangeType.USER_REMOVED) {
                    userRemoved(pInfo);
                }
            }

            public void usernameAliasChanged(PresenceInfo arg1) {
                // do nothing
            }
        });
    }

    /**
     * Initializes the user list to all users except the local one.
     */
    private void initUserList() {
        for (PresenceInfo info : presenceManager.getAllUsers()) {
            WonderlandIdentity id = info.userID;
            if (id.equals(localUserIdentity) == false) {
                String displayName = getDisplayName(id);
                userMap.put(displayName, id);
                listModel.addElement(displayName);
            }
        }
    }

    /**
     * Given the user's presence info, returns its display name in the list
     */
    private String getDisplayName(WonderlandIdentity userID) {
        String fullName = userID.getFullName();
        String userName = userID.getUsername();
        String displayName = fullName + " (" + userName + ")";
        return displayName;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        userListScrollPane = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        startChatButton = new javax.swing.JButton();

        setTitle("Private Text Chat");
        getContentPane().setLayout(new java.awt.GridBagLayout());

        userListScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        userListScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        userList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        userList.setVisibleRowCount(15);
        userListScrollPane.setViewportView(userList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(userListScrollPane, gridBagConstraints);

        startChatButton.setText("Start Chat");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 0);
        getContentPane().add(startChatButton, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton startChatButton;
    private javax.swing.JList userList;
    private javax.swing.JScrollPane userListScrollPane;
    // End of variables declaration//GEN-END:variables
}
