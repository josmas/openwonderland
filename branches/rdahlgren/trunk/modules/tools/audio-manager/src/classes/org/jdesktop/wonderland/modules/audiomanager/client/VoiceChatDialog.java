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

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.PlayTreatmentMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatDialOutMessage;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;

import java.util.logging.Logger;

import java.awt.Point;

/**
 *
 * @author  jp
 */
public class VoiceChatDialog extends javax.swing.JFrame implements PresenceManagerListener,
	KeypadListener, DisconnectListener, MemberChangeListener {

    private static final Logger logger =
            Logger.getLogger(VoiceChatDialog.class.getName());
    private ChatType chatType = ChatType.PRIVATE;
    private AudioManagerClient client;
    private WonderlandSession session;
    private CellID cellID;
    private PresenceInfo caller;
    private PresenceManager pm;
    private int groupNumber;

    private PresenceInfo mostRecentDialout;
    
    /** Creates new form VoiceChatDialog */
    public VoiceChatDialog() {
        initComponents();
    }

    public VoiceChatDialog(AudioManagerClient client, WonderlandSession session,
            CellID cellID, PresenceInfo caller) throws IOException {

        this.client = client;
        this.cellID = cellID;
        this.session = session;
        this.caller = caller;

        initComponents();

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

	client.addDisconnectListener(this);

        setVisible(true);
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	logger.finer("PRESENCEINFO CHANGED:  " + type + " " + presenceInfo);
	//pm.dump();
        setBuddyList();
    }

    public void usernameAliasChanged(PresenceInfo presenceInfo) {
        setBuddyList();
    }

    private String[] currentArray = new String[0];

    private void setBuddyList() {
        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> userList = new ArrayList();

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

            if (info.callID == null) {
                // It's a virtual player, skip it.
                continue;
            }

            userList.add(info.usernameAlias);
        }

	String[] userArray = userList.toArray(new String[0]);

	SortUsers.sort(userArray);

	boolean needToUpdate = false;

	if (currentArray.length == userArray.length) {
	    for (int i = 0; i < currentArray.length; i++) {
	        if (currentArray[i].equals(userArray[i]) == false) {
		    needToUpdate = true;
		    break;
		}
	    }
	} else {
	    needToUpdate = true;
	}

	if (needToUpdate) {
	    currentArray = userArray;
            buddyList.setListData(userArray);
	}

	enableButtons();
    }

    private void enableButtons() {
        if (buddyList.getSelectedValues().length > 0 ||
                (phoneNumberTextField.getText().replaceAll(" ", "").length() > 0 &&
                nameTextField.getText().replaceAll(" ", "").length() > 0)) {

            joinButton.setEnabled(true);
	    getRootPane().setDefaultButton(joinButton);
        } else {
            joinButton.setEnabled(false);
        }
    }

    public void disconnected() {
	setVisible(false);
    }

    public void memberChange(PresenceInfo member, boolean added) {
	if (added == false) {
	    return;
	}

	if (mostRecentDialout != null && 
		mostRecentDialout.userID.getUsername().equals(member.userID.getUsername())) {

	    mostRecentDialout = member;
	}
    }

    public void setMemberList(PresenceInfo[] memberList) {
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
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        phoneNumberTextField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        joinButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        buddyList = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        keyPadButton = new javax.swing.JButton();

        setTitle("Voice Chat");

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setText("Secret");
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(publicRadioButton);
        publicRadioButton.setText("Public");
        publicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                publicRadioButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Phone Number:");

        phoneNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberTextFieldActionPerformed(evt);
            }
        });
        phoneNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                phoneNumberTextFieldKeyReleased(evt);
            }
        });

        jLabel4.setText("Users:");

        joinButton.setText("Join");
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buddyList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        buddyList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                buddyListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(buddyList);

        jLabel2.setText("Privacy:");

        jLabel5.setText("Name:");

        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        keyPadButton.setText("KeyPad");
        keyPadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyPadButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(keyPadButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 94, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 65, Short.MAX_VALUE)
                                .add(joinButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel3)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                .add(jLabel5)
                                .add(jLabel2)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(secretRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(privateRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(publicRadioButton))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                            .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))))
                .add(63, 63, 63))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(secretRadioButton)
                    .add(privateRadioButton)
                    .add(publicRadioButton))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .add(18, 18, 18)
                .add(keyPadButton)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(cancelButton)
                            .add(joinButton))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    setVisible(false);
}//GEN-LAST:event_cancelButtonActionPerformed

private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
    String group = caller.userID.getUsername() + "-" + groupNumber++;

    ArrayList<PresenceInfo> chattersInfo = new ArrayList();

    Object[] selectedValues = buddyList.getSelectedValues();

    for (int i = 0; i < selectedValues.length; i++) {
	PresenceInfo[] info = pm.getAliasPresenceInfo((String) selectedValues[i]);

	if (info == null || info.length == 0) {
	    System.out.println("No PresenceInfo for " + (String) selectedValues[i]);
	    continue;
	}

	chattersInfo.add(info[0]);
    }

    String callerString = caller.usernameAlias;

    InCallDialog inCallDialog = new InCallDialog(client, session, cellID, group, chatType);

    inCallDialog.setLocation(new Point((int) getLocation().getX() + getWidth(), (int) getLocation().getY()));

    String name = nameTextField.getText();

    session.send(client, new VoiceChatJoinMessage(group, caller, chattersInfo.toArray(new PresenceInfo[0]), chatType));

    logger.info("Sent join message, about to enable leave button");

    if (phoneNumberTextField.getText().length() > 0 && name.length() > 0) {
	mostRecentDialout = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), null);
	mostRecentDialout.usernameAlias = name;

	client.addMemberChangeListener(group, this);

	session.send(client, new VoiceChatDialOutMessage(group, caller.callID, chatType, mostRecentDialout, 
	    phoneNumberTextField.getText()));

	nameTextField.setText("");
	phoneNumberTextField.setText("");
	joinButton.setEnabled(false);
    }
}//GEN-LAST:event_joinButtonActionPerformed

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    chatType = ChatType.SECRET;
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = ChatType.PRIVATE;
}//GEN-LAST:event_privateRadioButtonActionPerformed

private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
    chatType = ChatType.PUBLIC;
}//GEN-LAST:event_publicRadioButtonActionPerformed

private void phoneNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldActionPerformed
    enableButtons();
    if (joinButton.isEnabled()) {
	joinButton.doClick();
    }
}//GEN-LAST:event_phoneNumberTextFieldActionPerformed

private void buddyListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_buddyListValueChanged
    enableButtons();
}//GEN-LAST:event_buddyListValueChanged

private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
    enableButtons();
    if (joinButton.isEnabled()) {
	joinButton.doClick();
    }
}//GEN-LAST:event_nameTextFieldActionPerformed

private KeypadDialog keypad;

private void keyPadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyPadButtonActionPerformed
    if (keypad == null) {
        keypad = new KeypadDialog(this);
        keypad.setListener(this);
        keypad.setLocation(new Point((int) getLocation().getX() + getWidth(), (int) getLocation().getY()));
    }

    keypad.setVisible(true);
}//GEN-LAST:event_keyPadButtonActionPerformed

private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
    enableButtons();
}//GEN-LAST:event_nameTextFieldKeyReleased

private void phoneNumberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldKeyReleased
    enableButtons();
}//GEN-LAST:event_phoneNumberTextFieldKeyReleased

public void keypadPressed(char key) {
    System.out.println("Got key " + key);

    if (mostRecentDialout == null) {
	return;
    }

    session.send(client, new PlayTreatmentMessage(mostRecentDialout.callID, "dtmf:" + key));
}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new VoiceChatDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList buddyList;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton keyPadButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField phoneNumberTextField;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    // End of variables declaration//GEN-END:variables
}
