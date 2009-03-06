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

import org.jdesktop.wonderland.modules.audiomanager.common.messages.GetUserListMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.MuteCallMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.SpeakingMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatBusyMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage;

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

    private CellID cellID;

    private Flasher flasher;

    private VoiceChatMessage.ChatType chatType = 
	VoiceChatMessage.ChatType.PRIVATE;

    private AudioManagerClient client;
    private WonderlandSession session;

    private String caller;

    /** Creates new form VoiceChatDialog */
    public VoiceChatDialog(AudioManagerClient client, WonderlandSession session, CellID cellID) 
	    throws IOException {

	this.client = client;
	this.session = session;
	this.cellID = cellID;

        initComponents();
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);

	Cell cell = ClientContext.getCellCache(session).getCell(cellID);

	try {
	    caller = cell.getCellCache().getViewCell().getIdentity().getUsername();
	} catch (Exception e) {
	    throw new IOException(e.getMessage());
	}

	callerText.setText(caller);
	callerText.setEnabled(false);

	chatGroupText.setText(caller);

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

	caller = cell.getCellCache().getViewCell().getIdentity().getUsername();

	callerText.setText(caller);
	callerText.setEnabled(false);

	chatGroupText.setText(caller);

	chatterText.setText(s);

	leaveButton.setEnabled(false);
	busyButton.setEnabled(false);

	logger.fine("VOICE DIALOG IS VISIBLE!");
	setVisible(true);
    }

    public static VoiceChatDialog getVoiceChatDialog(String chatGroup) {
	return dialogs.get(chatGroup);
    }

    public void setChatters(String chatters) {
	chatterText.setText(chatters);
    }

    public void requestToJoin(String group, String caller, String calleeList,
	    VoiceChatMessage.ChatType chatType) {

	this.chatType = chatType;

	callerText.setText(caller);
	callerText.setEnabled(false);
	chatGroupText.setText(group);
	chatGroupText.setEnabled(false);
	chatterText.setText(caller + " " + calleeList);
	chatterText.setEnabled(false);

	if (chatType == VoiceChatMessage.ChatType.SECRET) {
	    secretRadioButton.setSelected(true);
	} else if (chatType == VoiceChatMessage.ChatType.PRIVATE) {
	    privateRadioButton.setSelected(true);
	} else if (chatType == VoiceChatMessage.ChatType.PUBLIC) {
	    publicRadioButton.setSelected(true);
	}
	
	flasher = new Flasher(
	    caller + " wants to have a " + chatType + " chat.");

	busyButton.setEnabled(true);
	leaveButton.setEnabled(false);

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
        jLabel3 = new javax.swing.JLabel();
        callerText = new javax.swing.JTextField();
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

        jLabel3.setText("Caller:");
        jLabel3.setName("jLabel3"); // NOI18N

        callerText.setName("callerText"); // NOI18N

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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(158, 158, 158)
                        .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 107, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, callerText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chatterText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, chatGroupText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(52, 52, 52)
                                .add(secretRadioButton)
                                .add(18, 18, 18)
                                .add(privateRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(publicRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 29, Short.MAX_VALUE))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .add(updateJButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 99, Short.MAX_VALUE)
                                .add(busyButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(51, 51, 51)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(leaveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(joinButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 71, Short.MAX_VALUE))))
                .add(57, 57, 57))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(callerText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(chatterText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(chatGroupText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(secretRadioButton)
                    .add(privateRadioButton)
                    .add(publicRadioButton)
                    .add(joinButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(leaveButton)
                    .add(busyButton)
                    .add(updateJButton))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void chatterTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_chatterTextKeyTyped
}//GEN-LAST:event_chatterTextKeyTyped

private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
    
    String caller = callerText.getText();
    String chatters = chatterText.getText();

    chatters = chatters.replaceAll(" " + caller, "");
    chatters = chatters.replaceAll(caller + " ", "");
    chatters = chatters.replaceAll(caller, "");

    String chatGroup = chatGroupText.getText();

    if (chatGroup.length() == 0) {
        chatGroup = caller + "-" + cellID.toString();
    }

    logger.warning("JOIN chatGroup " + chatGroup + " caller " + caller
	+ " chatters " + chatters + " chatType " + chatType);

    statusLabel.setText(chatType + " Chat");

    if (stopFlasher() == true) {
	Cell cell = ClientContext.getCellCache(session).getCell(cellID);

	caller = cell.getCellCache().getViewCell().getIdentity().getUsername();

	chatters = "";
    }

    session.send(client, new VoiceChatJoinMessage(chatGroup, caller, chatters, chatType));

    logger.warning("Sent join message, about to enable leave button");

    leaveButton.setEnabled(true);

    dialogs.put(chatGroup, this);
}//GEN-LAST:event_joinButtonActionPerformed

private void chatGroupTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatGroupTextActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chatGroupTextActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = VoiceChatMessage.ChatType.PRIVATE;
}//GEN-LAST:event_privateRadioButtonActionPerformed

private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
    String chatGroup = chatGroupText.getText();

    if (chatGroup.length() == 0) {
        chatGroup = cellID.toString();
    }

    leaveButton.setEnabled(false);
    busyButton.setEnabled(false);

    VoiceChatMessage chatMessage = new VoiceChatLeaveMessage(chatGroup, cellID.toString());

    session.send(client, chatMessage);

    dialogs.remove(chatGroup);

    setVisible(false);
}//GEN-LAST:event_leaveButtonActionPerformed

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    chatType = VoiceChatMessage.ChatType.SECRET;
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
    chatType = VoiceChatMessage.ChatType.PUBLIC;
}//GEN-LAST:event_publicRadioButtonActionPerformed

private void busyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_busyButtonActionPerformed

    stopFlasher();

    VoiceChatMessage chatMessage =
        new VoiceChatBusyMessage(chatGroupText.getText(), callerText.getText(), 
	chatterText.getText(), chatType);

    session.send(client, chatMessage);

    busyButton.setEnabled(false);
}//GEN-LAST:event_busyButtonActionPerformed

private void updateJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateJButtonActionPerformed
    VoiceChatMessage chatMessage = new VoiceChatInfoRequestMessage(chatGroupText.getText());

    session.send(client, chatMessage);
}//GEN-LAST:event_updateJButtonActionPerformed

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

	    setTitle(text);
	}

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton busyButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField callerText;
    private javax.swing.JTextField chatGroupText;
    private javax.swing.JTextField chatterText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton leaveButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JButton updateJButton;
    // End of variables declaration//GEN-END:variables

}
