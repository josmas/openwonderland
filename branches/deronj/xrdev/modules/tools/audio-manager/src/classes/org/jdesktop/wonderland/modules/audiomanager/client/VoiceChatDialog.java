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

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
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
public class VoiceChatDialog extends javax.swing.JFrame {

    private static final Logger logger =
	Logger.getLogger(VoiceChatDialog.class.getName());

    private static ConcurrentHashMap<String, VoiceChatDialog> dialogs = 
	new ConcurrentHashMap();

    private Flasher flasher;

    private VoiceChatMessage.ChatType chatType = VoiceChatMessage.ChatType.PRIVATE;

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

	chatGroupText.setText(caller.userID.getUsername());
	setVisible(true);
    }

    public VoiceChatDialog(JList userJList, DefaultListModel userList) {
        initComponents();

        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

	int[] selectedIndices = userJList.getSelectedIndices();

        String s = "";

	for (int i = 0; i < selectedIndices.length; i++) {
	    //s += ((User) userList.get(selectedIndices[i])).getUserName() + " ";
        }

	privateRadioButton.doClick();
	privateRadioButton.setSelected(true);

	Cell cell = ClientContext.getCellCache(session).getCell(cellID);

	String user = cell.getCellCache().getViewCell().getIdentity().getUsername();

	PresenceInfo[] callerList = pm.getUserPresenceInfo(user);

        if (callerList == null) {
            logger.warning("Cannot find PresenceInfo for " + user);
	    joinButton.setEnabled(false);
	    statusLabel.setText("Unknown user: " + user);
        } else {
	    caller = callerList[0];
	}

	chatGroupText.setText(caller.userID.getUsername());

	chatterText.setText(s);

	leaveButton.setEnabled(false);
	busyButton.setEnabled(false);

	setVisible(true);
    }

    public static VoiceChatDialog getVoiceChatDialog(String chatGroup) {
	return dialogs.get(chatGroup);
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
	    PresenceInfo[] calleeList, VoiceChatMessage.ChatType chatType) {

	this.caller = caller;
	this.chatType = chatType;

	chatGroupText.setText(group);
	chatGroupText.setEnabled(false);

	String s = "";

	for (int i = 0; i < calleeList.length; i++ ) {
	    if (i > 0) {
		s += " ";
	    }

	    s += calleeList[i].userID.getUsername();
	}

	chatterText.setText(caller.userID.getUsername() + " " + s);
	chatterText.setEnabled(false);

	if (chatType == VoiceChatMessage.ChatType.SECRET) {
	    secretRadioButton.setSelected(true);
	} else if (chatType == VoiceChatMessage.ChatType.PRIVATE) {
	    privateRadioButton.setSelected(true);
	} else if (chatType == VoiceChatMessage.ChatType.PUBLIC) {
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
        chatterText = new javax.swing.JTextField();
        joinButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        chatGroupText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        leaveButton = new javax.swing.JButton();
        busyButton = new javax.swing.JButton();
        updateJButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Call Dialog");
        setName("Form"); // NOI18N

        statusLabel.setName("statusLabel"); // NOI18N

        chatterText.setName("chatterText"); // NOI18N
        chatterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatterTextActionPerformed(evt);
            }
        });
        chatterText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                chatterTextKeyTyped(evt);
            }
        });

        joinButton.setText("Join");
        joinButton.setName("joinButton"); // NOI18N
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Chatters:");
        jLabel1.setName("jLabel1"); // NOI18N

        chatGroupText.setName("chatGroupText"); // NOI18N
        chatGroupText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatGroupTextActionPerformed(evt);
            }
        });

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .add(30, 30, 30))
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, statusLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chatterText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chatGroupText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(layout.createSequentialGroup()
                                .add(updateJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18)
                                .add(busyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(29, 29, 29)
                                .add(leaveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(18, 18, 18))
                            .add(layout.createSequentialGroup()
                                .add(37, 37, 37)
                                .add(secretRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(privateRadioButton)
                                .add(68, 68, 68)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(publicRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 11, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(joinButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .add(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chatterText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chatGroupText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(publicRadioButton)
                    .add(privateRadioButton)
                    .add(secretRadioButton))
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(updateJButton)
                    .add(busyButton)
                    .add(joinButton)
                    .add(leaveButton))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void chatterTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chatterTextKeyTyped
}//GEN-LAST:event_chatterTextKeyTyped

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

        session.send(client, new VoiceChatJoinAcceptedMessage(chatGroup, callee, chatType));
	leaveButton.setEnabled(true);
	joinButton.setEnabled(false);
	return;
    }

    String chatters = chatterText.getText();

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

private void chatGroupTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatGroupTextActionPerformed
    if (chatGroupText.getText().length() == 0) {
	joinButton.setEnabled(false);
    } else {
	joinButton.setEnabled(true);
    }
}//GEN-LAST:event_chatGroupTextActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = VoiceChatMessage.ChatType.PRIVATE;
    
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
    chatType = VoiceChatMessage.ChatType.SECRET;
    
    if (leaveButton.isEnabled() == true) {
	joinButtonActionPerformed(evt);
    }
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
    chatType = VoiceChatMessage.ChatType.PUBLIC;
    
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

private void chatterTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatterTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chatterTextActionPerformed

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
    private javax.swing.JTextField chatGroupText;
    private javax.swing.JTextField chatterText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton leaveButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton updateJButton;
    // End of variables declaration//GEN-END:variables

}
