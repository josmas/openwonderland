/*
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
package org.jdesktop.wonderland.modules.audiomanager.client.voicechat;

import org.jdesktop.wonderland.modules.audiomanager.client.AudioManagerClient;
import org.jdesktop.wonderland.modules.audiomanager.client.MemberChangeListener;

import org.jdesktop.wonderland.modules.audiomanager.client.voicechat.AddHUDPanel.Mode;

import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.audio.EndCallMessage;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatDialOutMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * @author nsimpson
 */
public class AddUserPanel extends javax.swing.JPanel implements 
	 PresenceManagerListener, MemberChangeListener {

    private static final Logger logger = Logger.getLogger(AddUserPanel.class.getName());

    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;
    private PresenceInfo caller;
    private String group;

    private Mode mode = Mode.INITIATE;

    private ChatType chatType = ChatType.PRIVATE;

    private DefaultListModel userListModel;

    private PrivacyPanel privacyPanel;

    private boolean personalPhone;

    public AddUserPanel(AudioManagerClient client, WonderlandSession session,
            PresenceInfo myPresenceInfo, PresenceInfo caller, String group) {

	this.client = client;
	this.session = session;
	this.myPresenceInfo = myPresenceInfo;
	this.caller = caller;
	this.group = group;

	System.out.println("GROUP IS " + group);

        initComponents();

	userListModel = new DefaultListModel();
        addUserList.setModel(userListModel);
	
        members.add(myPresenceInfo);

        if (caller.equals(myPresenceInfo) == false) {
            members.add(caller);
            //addToUserList(caller);
        }
     
        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

	client.addMemberChangeListener(group, this);

	privacyPanel = new PrivacyPanel();

	privacyPanel.addSecretRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		secretButtonActionPerformed(e);
	    }
	});

	privacyPanel.addPrivateRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		privateButtonActionPerformed(e);
	    }
	});

	privacyPanel.addPublicRadioButtonActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		publicButtonActionPerformed(e);
	    }
	});

        addUserDetailsPanel.add(privacyPanel, BorderLayout.CENTER);
        validate();
    }

    public void setVisible(boolean isVisible, Mode mode) {
	this.mode = mode;

	setVisible(isVisible);

	if (isVisible == false) {
	    return;
	}

	if (mode.equals(Mode.ADD)) {
	    addNonMembers();
	} else if (mode.equals(Mode.INITIATE)) {
	    addAllUsers();
	} else if (mode.equals(Mode.IN_PROGRESS)) {
	    addMembers();
	}
    }

    public void secretButtonActionPerformed(ActionEvent e) {
	chatType = chatType.SECRET;

	if (mode.equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    public void privateButtonActionPerformed(ActionEvent e) {
	chatType = chatType.PRIVATE;

	if (mode.equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    public void publicButtonActionPerformed(ActionEvent e) {
	chatType = chatType.PUBLIC;

	if (mode.equals(Mode.IN_PROGRESS) == false) {
	    return;
	}

	changePrivacy();
    }

    private void changePrivacy() {
	ArrayList<PresenceInfo> users = getSelectedValues();

        if (users.contains(myPresenceInfo) == false) {
            session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo, new PresenceInfo[0], chatType));
        }

        for (PresenceInfo info : users) {
            /*
             * You can only select yourself or outworlders
             */
            if (info.clientID != null) {
                continue;
            }

            session.send(client, new VoiceChatJoinMessage(group, info, new PresenceInfo[0], chatType));
        }
    }

    public void showPrivacyPanel(boolean showPrivacy) {
        addUserDetailsPanel.setVisible(showPrivacy);
    }

    public void callUser(String name, String number) {
	    PresenceInfo[] info = pm.getAllUsers();

        for (int i = 0; i < info.length; i++) {
            if (info[i].usernameAlias.equals(name) ||
                    info[i].userID.getUsername().equals(name)) {

                //statusLabel.setText("Name is already being used!");
		System.out.println("Name is already being used!");
                return;
            }
	}

        personalPhone = true;

        session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
                new PresenceInfo[0], chatType));

        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        String callID = sc.getCallID();

        PresenceInfo presenceInfo = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), callID);

        pm.addPresenceInfo(presenceInfo);

        addToUserList(presenceInfo);
        session.send(client, new VoiceChatDialOutMessage(group, callID, chatType, presenceInfo, number));
    }

    public void inviteUsers() {
	ArrayList<PresenceInfo> usersToInvite = getSelectedValues();
	usersToInvite.remove(myPresenceInfo);
	inviteUsers(usersToInvite);
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
	clearUserList();

        for (PresenceInfo info : usersToInvite) {
	    addToUserList(info);
            invitedMembers.add(info);

            session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
                usersToInvite.toArray(new PresenceInfo[0]), chatType));
        }
    }


    private ArrayList<PresenceInfo> getSelectedValues() {
	Object[] selectedValues = addUserList.getSelectedValues();

	ArrayList<PresenceInfo> usersToInvite = new ArrayList();

	if (selectedValues.length == 0) {
	    return new ArrayList<PresenceInfo>();
        }

	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

            usersToInvite.add(info);
        }

	return usersToInvite;
    }

    private void clearUserList() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                userListModel.clear();
            }
        });
    }

    private void addElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                userListModel.removeElement(usernameAlias);
                userListModel.addElement(usernameAlias);
            }
        });
    }

    private void removeElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                userListModel.removeElement(usernameAlias);
            }
        });
    }

    private void addToUserList(PresenceInfo info) {
        removeFromUserList(info);

        String name = NameTagNode.getDisplayName(info.usernameAlias,
                info.isSpeaking, info.isMuted);

        addElement(name);
    }

    private void removeFromUserList(PresenceInfo info) {
        String name = NameTagNode.getDisplayName(info.usernameAlias, false, false);
        removeElement(name);

        name = NameTagNode.getDisplayName(info.usernameAlias, false, true);
        removeElement(name);

        name = NameTagNode.getDisplayName(info.usernameAlias, true, false);
        removeElement(name);
    }

    private void addNonMembers() {
	clearUserList();

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

	for (int i = 0; i < presenceInfoList.length; i++) {
	    PresenceInfo info = presenceInfoList[i];

	    if (members.contains(info)) {
                removeFromUserList(info);
            } else {
                addToUserList(info);
            }
	}
    }

    private void addAllUsers() {
	clearUserList();

        PresenceInfo[] presenceInfoList = pm.getAllUsers();

	for (int i = 0; i < presenceInfoList.length; i++) {
	    addToUserList(presenceInfoList[i]);
	}
    }

    private void addMembers() {
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	switch (mode) {
	case ADD:
	    switch (type) {
	    case USER_ADDED:
		if (members.contains(presenceInfo)) {
		    return;
		}
		addToUserList(presenceInfo);
		break;

	    case USER_REMOVED:
		removeFromUserList(presenceInfo);
	        break;
	    }

	    break;

	case INITIATE:
	    switch (type) {
            case USER_ADDED:
		if (presenceInfo.equals(myPresenceInfo)) {
		    removeFromUserList(presenceInfo);
		    break;
		}

		addToUserList(presenceInfo);
		break;

	    case USER_REMOVED:
		removeFromUserList(presenceInfo);
		if (personalPhone) {
		    if (presenceInfo.clientID == null && members.size() == 1) {
			leave();
		    }
		}
	        break;
	    }

	    break;

	case IN_PROGRESS:
	    switch (type) {
            case USER_ADDED:
		if (members.contains(presenceInfo)) {
		    addToUserList(presenceInfo);
		} else {
		    removeFromUserList(presenceInfo);
		}
		break;

	    case USER_REMOVED:
		removeFromUserList(presenceInfo);
	        break;

	    case UPDATED:
	 	addToUserList(presenceInfo);
		break;

	    case USER_IN_RANGE:
		break;

	    case USER_OUT_OF_RANGE:
		break;
	    }
	}
    }

    private ArrayList<PresenceInfo> members = new ArrayList();
    private ArrayList<PresenceInfo> invitedMembers = new ArrayList();

    public void memberChange(PresenceInfo presenceInfo, boolean added) {
	if (added) {
	    if (members.contains(presenceInfo) == false) {
		members.add(presenceInfo);
	    }
	    presenceInfoChanged(presenceInfo, ChangeType.USER_ADDED);
	} else {
	    members.remove(presenceInfo);
	    presenceInfoChanged(presenceInfo, ChangeType.USER_REMOVED);

	    if (personalPhone && members.size() == 1) {
                leave();
            }
	}
    }

    public void setMemberList(PresenceInfo[] memberList) {
    }

    private void leave() {
        session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
    }

    public void hangup() {
       ArrayList<PresenceInfo> membersInfo = getSelectedValues();

        for (PresenceInfo info : membersInfo) {
            if (info.clientID != null) {
                continue;
            }

            session.send(client, new EndCallMessage(info.callID, "Terminated with malice"));
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
        addUserScrollPane = new javax.swing.JScrollPane();
        addUserList = new javax.swing.JList();
        addUserDetailsPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(0, 95));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(295, 95));

        addUserScrollPane.setMinimumSize(new java.awt.Dimension(23, 89));
        addUserScrollPane.setName("addUserScrollPane"); // NOI18N

        addUserList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        addUserList.setName("addUserList"); // NOI18N
        addUserList.setVisibleRowCount(5);
        addUserScrollPane.setViewportView(addUserList);

        addUserDetailsPanel.setBackground(new java.awt.Color(0, 0, 0));
        addUserDetailsPanel.setName("addUserDetailsPanel"); // NOI18N
        addUserDetailsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(addUserDetailsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
            .addComponent(addUserScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(addUserScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(addUserDetailsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 11, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel addUserDetailsPanel;
    private javax.swing.JList addUserList;
    private javax.swing.JScrollPane addUserScrollPane;
    private javax.swing.ButtonGroup buttonGroup1;
    // End of variables declaration//GEN-END:variables
}
