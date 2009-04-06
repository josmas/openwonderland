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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.client.ClientContext;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinAcceptedMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import java.io.IOException;

import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;

import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import java.awt.Point;

/**
 *
 * @author  jprovino
 */
public class VoiceChatDialog extends javax.swing.JFrame implements PresenceManagerListener {

    private static final Logger logger =
	Logger.getLogger(VoiceChatDialog.class.getName());

    private static ConcurrentHashMap<String, VoiceChatDialog> dialogs = 
	new ConcurrentHashMap();

    private Flasher flasher;

    private ChatType chatType = ChatType.PRIVATE;

    private AudioManagerClient client;
    private WonderlandSession session;

    private CellID cellID;

    private PresenceInfo caller;

    private PresenceManager pm;

    /** Creates new form VoiceChatDialog */
    public VoiceChatDialog(AudioManagerClient client, CellID cellID,
	    WonderlandSession session, PresenceInfo caller) throws IOException {

	this.client = client;
	this.cellID = cellID;
	this.session = session;
	this.caller = caller;

        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

	pm = PresenceManagerFactory.getPresenceManager(session);

	pm.addPresenceManagerListener(this);

	chatGroupText.setText(caller.userID.getUsername());
	setVisible(true);
    }

    public static VoiceChatDialog getVoiceChatDialog(String chatGroup) {
	return dialogs.get(chatGroup);
    }

    public void userAdded(PresenceInfo presenceInfo) {
        setUserList();
    }

    public void userRemoved(PresenceInfo presenceInfo) {
        setUserList();
    }

    private void setUserList() {
	WonderlandIdentity[] userIDArray = pm.getAllUsers();

        String[] userData = new String[userIDArray.length];

        for (int i = 0; i < userIDArray.length; i++) {
            userData[i] = userIDArray[i].getUsername();
        }

        userList.setListData(userData);
    }

    public void setChatters(PresenceInfo[] chatters) {
	String s = "";

	for (int i = 0; i < chatters.length; i++) {
	    if (i > 0) {
		s += " ";
	    }

	    s += chatters[i].userID.getUsername();
	}

	chatterText.setText(s);
    }

    public void requestToJoin(String group, PresenceInfo caller, 
	    PresenceInfo[] calleeList, ChatType chatType) {

	this.caller = caller;
	this.chatType = chatType;

	chatGroupText.setText(group);
	chatGroupText.setEnabled(false);

	String s = "";

	if (calleeList != null) {
	    for (int i = 0; i < calleeList.length; i++ ) {
	        if (i > 0) {
		    s += " ";
	        }

	        s += calleeList[i].userID.getUsername();
	    }
	}

	chatterText.setText(s);

	if (chatType == ChatType.SECRET) {
	    secretRadioButton.setSelected(true);
	} else if (chatType == ChatType.PRIVATE) {
	    privateRadioButton.setSelected(true);
	} else if (chatType == ChatType.PUBLIC) {
	    publicRadioButton.setSelected(true);
	}
	
	flasher = new Flasher(
	    caller.userID.getUsername() + " wants to have a " + chatType + " chat.");

	busyButton.setEnabled(true);
	leaveButton.setEnabled(false);
	joinButton.setEnabled(true);

	statusLabel.setText("");

	setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        statusLabel = new javax.swing.JLabel();
        joinButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        leaveButton = new javax.swing.JButton();
        busyButton = new javax.swing.JButton();
        updateJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        chatterText = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        chatGroupText = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Call Dialog");
        setName("Form"); // NOI18N

        statusLabel.setName("statusLabel"); // NOI18N

        joinButton.setText("Join");
        joinButton.setName("joinButton"); // NOI18N
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("User List:");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText("Chat Group:");
        jLabel2.setName("jLabel2"); // NOI18N

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setText("Secret");
        secretRadioButton.setName("secretRadioButton"); // NOI18N
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");
        privateRadioButton.setName("privateRadioButton"); // NOI18N
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(publicRadioButton);
        publicRadioButton.setText("Public");
        publicRadioButton.setName("publicRadioButton"); // NOI18N
        publicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioButtonActionPerformed(evt);
            }
        });

        leaveButton.setText("Leave");
        leaveButton.setEnabled(false);
        leaveButton.setName("leaveButton"); // NOI18N
        leaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaveButtonActionPerformed(evt);
            }
        });

        busyButton.setText("Busy");
        busyButton.setEnabled(false);
        busyButton.setName("busyButton"); // NOI18N
        busyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                busyButtonActionPerformed(evt);
            }
        });

        updateJButton.setText("Update");
        updateJButton.setName("updateJButton"); // NOI18N
        updateJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateJButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        userList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        userList.setName("userList"); // NOI18N
        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        jLabel3.setText("Chatters:");
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        chatterText.setEditable(false);
        chatterText.setName("chatterText"); // NOI18N
        jScrollPane2.setViewportView(chatterText);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        chatGroupText.setName("chatGroupText"); // NOI18N
        jScrollPane3.setViewportView(chatGroupText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(updateJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(26, 26, 26)
                        .add(busyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(26, 26, 26)
                        .add(leaveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                        .add(joinButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(87, 87, 87)
                        .add(secretRadioButton)
                        .add(35, 35, 35)
                        .add(privateRadioButton)
                        .add(41, 41, 41)
                        .add(publicRadioButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(33, 33, 33)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(14, 14, 14))
                    .add(layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(18, 18, 18)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(secretRadioButton)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(privateRadioButton)
                        .add(publicRadioButton)))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(updateJButton)
                    .add(busyButton)
                    .add(leaveButton)
                    .add(joinButton))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
    String chatGroup = chatGroupText.getText();

    if (stopFlasher() == true) {
	/*
	 * Someone has asked us to join.
	 */
        PresenceInfo callee = pm.getPresenceInfo(cellID);

        if (callee == null) {
	    logger.warning("Can't find presence info for " + cellID);
	    return;
        }

        dialogs.put(chatGroup, this);

        session.send(client, new VoiceChatJoinAcceptedMessage(chatGroup, callee, chatType));
	leaveButton.setEnabled(true);
	joinButton.setEnabled(false);
	return;
    }

    String chatters = "";

    Object[] selectedValues = userList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
	if (i > 0) {
	    chatters += " ";
	}

	chatters += (String) selectedValues[i];
    }

    String callerString = caller.userID.getUsername();

    chatters = chatters.replaceAll(" " + callerString, "");
    chatters = chatters.replaceAll(callerString + " ", "");
    chatters = chatters.replaceAll(callerString, "");

    logger.info("JOIN chatGroup " + chatGroup + " caller " + caller
	+ " chatters " + chatters + " chatType " + chatType);

    statusLabel.setText(chatType + " Chat");

    PresenceInfo[] chattersInfo = new PresenceInfo[0];

    if (chatters.length() > 0) {
	chattersInfo = getPresenceInfo(chatters);

        if (chattersInfo == null) {
	    statusLabel.setText("Unknown user in list:  " + chatters);
	    return;
	}
    }

    session.send(client, new VoiceChatJoinMessage(chatGroup, caller, chattersInfo, chatType));

    logger.info("Sent join message, about to enable leave button");

    busyButton.setEnabled(false);
    leaveButton.setEnabled(true);

    setTitle("Chatting...");

    dialogs.put(chatGroup, this);
}//GEN-LAST:event_joinButtonActionPerformed

private PresenceInfo[] getPresenceInfo(String users) {
    String[] tokens = users.split(" ");

    PresenceInfo[] info = new PresenceInfo[tokens.length];

    for (int i = 0; i < tokens.length; i++) {
	PresenceInfo[] userInfo = pm.getUserPresenceInfo(tokens[i]);

	if (userInfo == null) {
	    logger.warning("No PresenceInfo for " + tokens[i]);
	    return null;
	}

	info[i] = userInfo[0];

	checkLength(userInfo);
    }

    return info;
}

private void checkLength(PresenceInfo[] info) {
    if (info.length > 1) {
	logger.info("More than one PresenceInfo, using first:");

	for (int i = 0; i < info.length; i++) {
	    logger.info("  " + info[i]);
	}
    }
}

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = ChatType.PRIVATE;
    
    if (leaveButton.isEnabled() == true) {
	joinButtonActionPerformed(evt);
    }
}//GEN-LAST:event_privateRadioButtonActionPerformed

private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
    leaveButton.setEnabled(false);
    busyButton.setEnabled(false);

    String chatGroup = chatGroupText.getText();

    VoiceChatMessage chatMessage = new VoiceChatLeaveMessage(chatGroup, pm.getPresenceInfo(cellID));

    session.send(client, chatMessage);

    dialogs.remove(chatGroup);

    setVisible(false);
}//GEN-LAST:event_leaveButtonActionPerformed

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    chatType = ChatType.SECRET;
    
    if (leaveButton.isEnabled() == true) {
	joinButtonActionPerformed(evt);
    }
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
    chatType = ChatType.PUBLIC;
    
    if (leaveButton.isEnabled() == true) {
	joinButtonActionPerformed(evt);
    }
}//GEN-LAST:event_publicRadioButtonActionPerformed

private void busyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_busyButtonActionPerformed

    stopFlasher();

    PresenceInfo callee = pm.getPresenceInfo(cellID);

    VoiceChatMessage chatMessage =
        new VoiceChatBusyMessage(chatGroupText.getText(), caller, callee, chatType);

    session.send(client, chatMessage);

    busyButton.setEnabled(false);
    setVisible(false);
}//GEN-LAST:event_busyButtonActionPerformed

private void updateJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateJButtonActionPerformed
    VoiceChatMessage chatMessage = new VoiceChatInfoRequestMessage(chatGroupText.getText());

    session.send(client, chatMessage);
}//GEN-LAST:event_updateJButtonActionPerformed

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
// TODO add your handling code here:
}//GEN-LAST:event_userListValueChanged

    private boolean stopFlasher() {
	if (flasher == null) {
	    return false;
	}

	flasher.done();
	return true;
    }

    class Flasher extends Thread {

	String text;

	private boolean done;

	public Flasher(String text) {
	    this.text = text;
	    start();
	}

	public void done() {
	    done = true;
	}

	public void run() {
	    boolean state = true;

	    while (!done) {
		if (state == true) {
		    setTitle(text);
		} else {
		    setTitle("");
		}
                try {
                    sleep(500);
                } catch(InterruptedException e) {
                }

		state = !state;
	    }    

	    setTitle("Chatting...");
	}

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton busyButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextPane chatGroupText;
    private javax.swing.JTextPane chatterText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton leaveButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton updateJButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
