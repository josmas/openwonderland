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

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatHoldMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.comms.WonderlandSession;


import java.util.ArrayList;
import java.util.Arrays;

import java.util.logging.Logger;


import java.awt.Point;

/**
 *
 * @author  jp
 */
public class InCallDialog extends javax.swing.JFrame implements KeypadListener,
        PresenceManagerListener, MemberChangeListener, DisconnectListener {

    private static final Logger logger =
            Logger.getLogger(InCallDialog.class.getName());
    private AudioManagerClient client;
    private WonderlandSession session;
    private CellID cellID;
    private String group;
    private PresenceInfo presenceInfo;
    private PresenceManager pm;

    /** Creates new form InCallDialog */
    public InCallDialog() {
        initComponents();
    }

    public InCallDialog(AudioManagerClient client, WonderlandSession session,
            CellID cellID, String group, ChatType chatType) {

        this.client = client;
        this.session = session;
        this.cellID = cellID;
        this.group = group;

        initComponents();

	setTitle(group);

	leaveButton.setEnabled(false);
	holdButton.setEnabled(false);

        if (chatType == ChatType.SECRET) {
            secretRadioButton.setSelected(true);
        } else if (chatType == ChatType.PRIVATE) {
            privateRadioButton.setSelected(true);
        } else if (chatType == ChatType.PUBLIC) {
            publicRadioButton.setSelected(true);
        }

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

        presenceInfo = pm.getPresenceInfo(cellID);

        memberList.setListData(new String[0]);

        client.addMemberChangeListener(group, this);

        client.addInCallDialog(group, this);

	client.addDisconnectListener(this);

	holdOtherCalls();

        session.send(client, new VoiceChatInfoRequestMessage(group));

        setVisible(true);
    }

    private ArrayList<PresenceInfo> members = new ArrayList();

    public void setMemberList(PresenceInfo[] memberList) {
	//pm.dump();

        synchronized (members) {
            members.clear();

            for (int i = 0; i < memberList.length; i++) {
		//System.out.println("InCall adding to members " + memberList[i]);
                members.add(memberList[i]);
            }
        }

        setMemberList();
    }


    public void memberChange(PresenceInfo member, boolean added) {
	if (added) {
	    addMember(member);
	} else {
	    removeMember(member);
 	}
    }

    private void addMember(PresenceInfo member) {
	if (holdDialog != null && holdDialog.isVisible()) {
	    hold(false);
	}

	//System.out.println("InCall addMember " + member);

        synchronized (members) {
            if (memberscontains(member) == false) {
                members.add(member);
	    }
        }

	setMemberList();
    }

    private void removeMember(PresenceInfo member) {
	//System.out.println("InCall remove Member " + member);

        synchronized (members) {
            members.remove(member);
        }

	setMemberList();
    }

    public void presenceInfoChanged(PresenceInfo info, ChangeType type) {
	setMemberList();
    }

    public void usernameAliasChanged(PresenceInfo info) {
	setMemberList();
    }

    private boolean memberscontains(PresenceInfo presenceInfo) {
	for (PresenceInfo info : members) {
	    //if (info.usernameAlias.equals(presenceInfo.userNameAlias())
	    if (info.callID.equals(presenceInfo.callID)) {
		return true;
	    }
	}

	return false;
    }

    private String[] currentArray = new String[0];

    private int setMemberList() {
	//System.out.println("-----------InCallDialog------------");
	//pm.dump();
	//System.out.println("-----------InCallDialog------------");

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

        ArrayList<String> memberData = new ArrayList();
        ArrayList<String> selectableMemberData = new ArrayList();

        for (int i = 0; i < presenceInfoList.length; i++) {
            PresenceInfo info = presenceInfoList[i];

            if (info.callID == null) {
                // It's a virtual player, skip it.
		//System.out.println("InCall:  skipping virtual player " + info);
                continue;
            }

	    if (memberscontains(info) == false) {
		//System.out.println("InCall:  Members doesn't contain member " + info);
		for (PresenceInfo member : members) {
		    //System.out.println("Member:  " + member);
		}
		continue;
	    }

	    if (info.clientID == null || info.equals(presenceInfo)) {
		//System.out.println("InCall: adding to selectable " + info);
	        selectableMemberData.add(NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                    info.isMuted));
	    } else {
		//System.out.println("InCall: adding to non-selectable " + info);
	        memberData.add(NameTagNode.getDisplayName(info.usernameAlias, info.isSpeaking,
                    info.isMuted));
	    }    
	}

	String[] memberArray = memberData.toArray(new String[0]);

	SortUsers.sort(memberArray);

	//System.out.println("memberData size " + memberData.size() + " sorted " + memberArray.length);

        memberList.setListData(memberArray);

	String[] selectableMemberArray = selectableMemberData.toArray(new String[0]);

	SortUsers.sort(selectableMemberArray);

        boolean needToUpdate = false;

        if (currentArray.length == selectableMemberArray.length) {
            for (int i = 0; i < selectableMemberArray.length; i++) {
                if (currentArray[i].equals(selectableMemberArray[i]) == false) {
                    needToUpdate = true;
                    break;
                }
            }
        } else {
	    needToUpdate = true;
	}

        if (needToUpdate) {
	    currentArray = selectableMemberArray;
            selectableMemberList.setListData(selectableMemberArray);
	}

	return selectableMemberData.size();
    }

    public void disconnected() {
	makeInvisible();
    }

    private void makeInvisible() {
	setVisible(false);

	if (keypad != null) {
	    keypad.setVisible(false);
	}

	if (addMemberDialog != null) {
	    addMemberDialog.setVisible(false);
	}

	if (holdDialog != null) {
	    holdDialog.setVisible(false);
	}
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
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        memberList = new javax.swing.JList();
        addMemberButton = new javax.swing.JButton();
        holdButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        publicRadioButton = new javax.swing.JRadioButton();
        leaveButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        selectableMemberList = new javax.swing.JList();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 15));
        jLabel1.setText("In Call");

        memberList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        memberList.setEnabled(false);
        memberList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                memberListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(memberList);

        addMemberButton.setText("Add Member");
        addMemberButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMemberButtonActionPerformed(evt);
            }
        });

        holdButton.setText("Hold");
        holdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Privacy:");

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setText("Secret");
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(privateRadioButton);
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

        leaveButton.setText("Leave");
        leaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leaveButtonActionPerformed(evt);
            }
        });

        selectableMemberList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        selectableMemberList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                selectableMemberListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(selectableMemberList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(141, 141, 141)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .add(holdButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(addMemberButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(leaveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 88, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel2)
                                        .add(18, 18, 18)
                                        .add(secretRadioButton)
                                        .add(18, 18, 18)
                                        .add(privateRadioButton)
                                        .add(18, 18, 18)
                                        .add(publicRadioButton))
                                    .add(jScrollPane1)
                                    .add(jScrollPane2))))))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 77, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(secretRadioButton)
                    .add(privateRadioButton)
                    .add(publicRadioButton))
                .add(19, 19, 19)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(holdButton)
                    .add(leaveButton)
                    .add(addMemberButton))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private KeypadDialog keypad;

private void keyPadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyPadButtonActionPerformed
    if (keypad == null) {
        keypad = new KeypadDialog(this);
        keypad.setListener(this);
        if (addMemberDialog == null) {
            keypad.setLocation(new Point((int) getLocation().getX() + getWidth(), (int) getLocation().getY()));
        } else {
            keypad.setLocation(new Point((int) addMemberDialog.getLocation().getX() + addMemberDialog.getWidth(),
                    (int) addMemberDialog.getLocation().getY()));
        }
    }

    keypad.setVisible(true);
}//GEN-LAST:event_keyPadButtonActionPerformed

    public void keypadPressed(char key) {
        System.out.println("Got key " + key);
    }
    private AddMemberDialog addMemberDialog;

private void addMemberButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMemberButtonActionPerformed
    if (addMemberDialog == null) {
        addMemberDialog = new AddMemberDialog(client, session, cellID, group, this);

        if (keypad == null) {
            addMemberDialog.setLocation(new Point((int) getLocation().getX() + getWidth(), (int) getLocation().getY()));
        } else {
            addMemberDialog.setLocation(new Point((int) keypad.getLocation().getX() + keypad.getWidth(),
                    (int) keypad.getLocation().getY()));
        }
    }

    addMemberDialog.setVisible(true);
}//GEN-LAST:event_addMemberButtonActionPerformed

private void holdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdButtonActionPerformed
    hold(true);
}//GEN-LAST:event_holdButtonActionPerformed

private HoldDialog holdDialog;

private void hold(boolean onHold) {
    if (holdDialog == null) {
	if (onHold == false) {
	    return;
	}

        holdDialog = new HoldDialog(client, session, group, this);
        Point location = new Point((int) (getLocation().getX() + getWidth()),
                (int) getLocation().getY());
        holdDialog.setLocation(location);
    }

    if (memberscontains(presenceInfo) == false) {
	return;
    }

    holdDialog.setVisible(onHold);
    setVisible(!onHold);
    setHold(onHold);
}

public void setHold(boolean onHold) {
    try {
        session.send(client, new VoiceChatHoldMessage(group, presenceInfo, onHold));

	if (onHold == false) {
  	    holdOtherCalls();
	}
    } catch (IllegalStateException e) {
	endCall();
    }
}

public void holdOtherCalls() {
    InCallDialog[] inCallDialogs = client.getInCallDialogs();

    for (int i = 0; i < inCallDialogs.length; i++) {
	if (inCallDialogs[i] == this) {
	    continue;
	}

	inCallDialogs[i].hold(true);
    }
}

private void leaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leaveButtonActionPerformed
    endCall();
}//GEN-LAST:event_leaveButtonActionPerformed

    private void endCall() {
        Object[] selectedValues = selectableMemberList.getSelectedValues();

        ArrayList<PresenceInfo> membersInfo = new ArrayList();

        for (int i = 0; i < selectedValues.length; i++) {
            String usernameAlias = NameTagNode.getUsername((String) selectedValues[i]);

	    PresenceInfo[] info = pm.getAliasPresenceInfo(usernameAlias);

	    if (info == null || info.length == 0) {
		System.out.println("No presence info for " + (String) selectedValues[i]);
		continue;
	    }

	    membersInfo.add(info[0]);
        }

        if (membersInfo.size() == 0) {
            session.send(client, new VoiceChatLeaveMessage(group, presenceInfo));
	    return;
        }

	for (PresenceInfo info : membersInfo) {
	    /*
	     * You can only select yourself or outworlders
	     */
	    if (info.clientID != null && presenceInfo.equals(info) == false) {
		continue;
	    }
	    session.send(client, new VoiceChatLeaveMessage(group, info));
	}
    }

    public void endHeldCall() {
	session.send(client, new VoiceChatLeaveMessage(group, presenceInfo));
        //client.removeInCallDialog(group);
    }

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    changePrivacy(ChatType.SECRET);
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    changePrivacy(ChatType.PRIVATE);
}//GEN-LAST:event_privateRadioButtonActionPerformed

private void publicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publicRadioButtonActionPerformed
    changePrivacy(ChatType.PUBLIC);
}//GEN-LAST:event_publicRadioButtonActionPerformed

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    endCall();
    client.removeInCallDialog(group);
    client.removeMemberChangeListener(group, this);
}//GEN-LAST:event_formWindowClosing

    private void changePrivacy(ChatType chatType) {
        Object[] selectedValues = selectableMemberList.getSelectedValues();

	ArrayList<PresenceInfo> membersInfo = new ArrayList();

        for (int i = 0; i < selectedValues.length; i++) {
            String usernameAlias = NameTagNode.getUsername((String) selectedValues[i]);

	    PresenceInfo[] info = pm.getAliasPresenceInfo(usernameAlias);

	    if (info == null || info.length == 0) {
		System.out.println("No presence info for " + usernameAlias);
		continue;
	    }
        }

        if (membersInfo.size() == 0) {
            session.send(client, new VoiceChatJoinMessage(group, presenceInfo, new PresenceInfo[0], chatType));
	    return;
        }

	for (PresenceInfo info : membersInfo) {
	    /*
	     * You can only select yourself or outworlders
	     */
	    if (info.clientID != null && presenceInfo.equals(info) == false) {
		continue;
	    }
    	    session.send(client, new VoiceChatJoinMessage(group, info, new PresenceInfo[0], chatType));
	}
    }

private void memberListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_memberListValueChanged
}//GEN-LAST:event_memberListValueChanged

private void selectableMemberListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_selectableMemberListValueChanged
    Object[] selectedValues = selectableMemberList.getSelectedValues();
    if (selectedValues.length > 0) {
	leaveButton.setEnabled(true);

	for (int i = 0; i < selectedValues.length; i++) {
	    String name = NameTagNode.getUsername((String) selectedValues[i]);

	    if (name.equals(presenceInfo.usernameAlias)) {
	        holdButton.setEnabled(true);
		break;
	    }
	}
    } else {
	leaveButton.setEnabled(false);
	holdButton.setEnabled(false);
    }
    
}//GEN-LAST:event_selectableMemberListValueChanged

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new InCallDialog().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMemberButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton holdButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton leaveButton;
    private javax.swing.JList memberList;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton publicRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JList selectableMemberList;
    // End of variables declaration//GEN-END:variables
}
